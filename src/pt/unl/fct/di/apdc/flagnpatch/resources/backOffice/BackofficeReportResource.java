package pt.unl.fct.di.apdc.flagnpatch.resources.backOffice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.memcache.stdimpl.GCacheFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

import com.google.gson.*;

import pt.unl.fct.di.apdc.flagnpatch.entities.*;
import pt.unl.fct.di.apdc.flagnpatch.inputData.AddressData;
import pt.unl.fct.di.apdc.flagnpatch.inputData.StatusData;
import pt.unl.fct.di.apdc.flagnpatch.resources.frontOffice.ReportResource;
import pt.unl.fct.di.apdc.flagnpatch.resources.general.Utils;
import pt.unl.fct.di.apdc.flagnpatch.utils.ResourcesEnum;
import pt.unl.fct.di.apdc.flagnpatch.utils.StatusEnum;
import pt.unl.fct.di.apdc.flagnpatch.utils.StringUtil;

@Path("/backoffice/report")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class BackofficeReportResource {

    // Logger object
    private static final Logger LOG = Logger.getLogger(ReportResource.class.getName());
    // Datastore link
    private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    // Converts objects to JSON
    private final Gson g = new Gson();

    private final Queue queue = QueueFactory.getDefaultQueue();

    public BackofficeReportResource() {

    }

    @POST
    @Path("/change/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)

    public Response changeReportStatus(@PathParam("id") String reportId, String jsonString) {
        // Extracts data from custom JSON input string and creates objects from
        // it
        // TODO: criar entrada database e qs
        JsonElement jelement = new JsonParser().parse(jsonString);
        JsonObject jobject = jelement.getAsJsonObject();
        JsonObject jstatus = jobject.getAsJsonObject("status");
        JsonObject jtoken = jobject.getAsJsonObject("token");

        AuthToken token = g.fromJson(jtoken.toString(), AuthToken.class);
        StatusData inputStatus = g.fromJson(jstatus.toString(), StatusData.class);

        LOG.info("Attempting change status of the report: " + reportId);

        /* Security Validations */
        Entity user = Utils.getUser(token.user, datastore, LOG);
        if (user == null) {
            LOG.warning(Utils.OUTPUT_MISSING_USER);
            return Response.status(Status.UNAUTHORIZED).entity(g.toJson(new StringUtil(Utils.OUTPUT_MISSING_USER))).build();
        }
        Response rsp = Utils.securityValidations(user, token, ResourcesEnum.BACKOFFICE_CHANGE_REPORT_STATUS, datastore,
                LOG);
        if (rsp.getStatus() != Status.ACCEPTED.getStatusCode())
            return rsp;
        /* Security Validations */

        // Validates the status fields
        if (!inputStatus.validStatusData()) {
            // If missing or wrong fields were introduced, it returns HTTP 400
            // code
            LOG.warning(Utils.OUTPUT_WRONG_FIELDS);
            return Response.status(Status.BAD_REQUEST).entity(g.toJson(new StringUtil(Utils.OUTPUT_WRONG_FIELDS)))
                    .build();

        }

        // In case token is valid and status fields are valid, begin transaction

        TransactionOptions options = TransactionOptions.Builder.withXG(true);
        Transaction txn = datastore.beginTransaction(options);

        Entity statusLogEntity;
        StatusLog stLog;
        Key statusLogKey = null;
        String currentStatus = null;
        String newStatus = null;
        String description = null;
        try {
            Key reportKey = KeyFactory.createKey(Report.KIND, Long.parseLong(reportId));

            Filter reportFilter = new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.EQUAL, reportKey);
            Query ctrQuery = new Query(Report.KIND).setFilter(reportFilter);

            // Exception is thrown if this exists more than one entity
            Entity report = datastore.prepare(ctrQuery).asSingleEntity();

            // If the report does not exist, returns HTTP 403 code
            if (report == null) {
                txn.rollback();
                LOG.warning(Utils.OUTPUT_MISSING_REPORT);
                return Response.status(Status.FORBIDDEN).entity(g.toJson(new StringUtil(Utils.OUTPUT_MISSING_REPORT)))
                        .build();
            }
            String responsible = (String) report.getProperty(Report.PROPERTY_WORKER_RESPONSIBLE);
            if (responsible == null || !responsible.equalsIgnoreCase(token.user)) {
                txn.rollback();
                LOG.severe(Utils.OUTPUT_NO_PERMISSIONS);
                return Response.status(Status.FORBIDDEN).entity(g.toJson(new StringUtil(Utils.OUTPUT_NO_PERMISSIONS)))
                        .build();
            }
            currentStatus = (String) report.getProperty(Report.PROPERTY_STATUS_DESCRIPTION);
            newStatus = inputStatus.statusDescription;
            description = inputStatus.description;

            if (currentStatus.equalsIgnoreCase(newStatus)) {
                txn.rollback();
                LOG.warning(Utils.OUTPUT_SAME_REPORT_STATUS_AS_BEFORE);
                return Response.status(Status.CONFLICT)
                        .entity(g.toJson(new StringUtil(Utils.OUTPUT_SAME_REPORT_STATUS_AS_BEFORE))).build();
            }

            // Updates the status fields of report
            String oldDescription = (String) report.getProperty(Report.PROPERTY_STATUS_DESCRIPTION);
            report.setIndexedProperty(Report.PROPERTY_STATUS_DESCRIPTION, inputStatus.statusDescription);
            datastore.put(txn, report);

            // Creates a new object StatusLogObject that includes
            // statusLogEntity entity and StatusLog Object
            Object[] statusLogObject = Utils.newStatusLogObject(inputStatus.statusDescription, inputStatus.description,
                    reportKey, token.user);

            // gets statusLogEntity entity from the object that will be put on
            // datastore
            statusLogEntity = (Entity) statusLogObject[0];

            // gets statusLog object from the object that will be return as
            // output
            stLog = (StatusLog) statusLogObject[1];
            statusLogKey = datastore.put(txn, statusLogEntity);

            EmbeddedEntity embeddedEntity = (EmbeddedEntity) report.getProperty(Report.PROPERTY_ADDRESS);
            String county = (String) embeddedEntity.getProperty(AddressData.PROPERTY_COUNTY);
            Filter filterByCounty = new FilterPredicate(Graph.PROPERTY_COUNTY, FilterOperator.EQUAL, county);
            Filter filterByActive = new FilterPredicate(Graph.PROPERTY_ACTIVE, FilterOperator.EQUAL, true);
            Query query = new Query(Graph.KIND).setFilter(
                    CompositeFilterOperator.and(filterByActive, filterByCounty));
            Entity entity = datastore.prepare(query).asSingleEntity();

            if (entity != null) {
                long oldValue = (long) entity.getProperty(oldDescription);
                entity.setIndexedProperty(oldDescription, oldValue - 1);
                oldValue = (long) entity.getProperty(inputStatus.statusDescription);
                entity.setIndexedProperty(inputStatus.statusDescription, oldValue + 1);
                datastore.put(txn, entity);
            }

            queue.add(TaskOptions.Builder.withUrl("/rest/backoffice/report/sendNotifications")
                    .param("reportId", reportId).param("currentStatus", currentStatus).param("newStatus", newStatus)
                    .param("user", token.user).param("description", description).header("Content-Type", "application/html; charset=utf8"));

            txn.commit();
            return Response.status(Status.OK).entity(g.toJson(stLog)).build();
        } catch (PreparedQuery.TooManyResultsException e) {
            txn.rollback();
            LOG.warning(Utils.OUTPUT_REPEATED_REPORT);
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity(g.toJson(new StringUtil(Utils.OUTPUT_REPEATED_REPORT))).build();

        } finally {
            if (txn.isActive()) {
                txn.rollback();
                LOG.severe(Utils.OUTPUT_TRANSACTION_STILL_ACTIVE);
                return Response.status(Status.INTERNAL_SERVER_ERROR)
                        .entity(g.toJson(new StringUtil(Utils.OUTPUT_TRANSACTION_STILL_ACTIVE))).build();
            }
        }
    }

    @POST
    @Path("/sendNotifications")
    public void callSendNotifications(@Context HttpServletRequest httpServletRequest) {
        // depois disto gerar notificacoes
        // para cada entrada em Follower que tenha reportId igual a este,
        // gerar uma nova entrada em UserNotification

        String reportId = httpServletRequest.getParameter("reportId");
        String currentStatus = httpServletRequest.getParameter("currentStatus");
        String newStatus = httpServletRequest.getParameter("newStatus");
        String userThatModifiedState = httpServletRequest.getParameter("user");
        String description = httpServletRequest.getParameter("description");

        TransactionOptions options = TransactionOptions.Builder.withXG(true);
        Transaction txn = datastore.beginTransaction(options);

        Filter filterByReport = new FilterPredicate(Follower.PROPERTY_REPORT, FilterOperator.EQUAL, reportId);
        Query query = new Query(Follower.KIND).setFilter(filterByReport);
        Iterator<Entity> it = datastore.prepare(query).asIterator();

        while (it.hasNext()) {
            Entity follow = it.next();
            String email = (String) follow.getProperty(Follower.PROPERTY_EMAIL);

            Entity notification = new Entity(UserNotification.KIND);
            notification.setIndexedProperty(UserNotification.PROPERTY_EMAIL, email);
            notification.setIndexedProperty(UserNotification.PROPERTY_REPORT_ID, reportId);
            notification.setProperty(UserNotification.PROPERTY_WAS_SEEN, false);
            notification.setProperty(UserNotification.PROPERTY_PREV_STATUS, currentStatus);
            notification.setProperty(UserNotification.PROPERTY_NEW_STATUS, newStatus);
            notification.setProperty(UserNotification.PROPERTY_MODIFIED_DATE, Utils.generateCustomDate());
            notification.setIndexedProperty(UserNotification.PROPERTY_EXACT_DATE, System.currentTimeMillis());
            notification.setIndexedProperty(UserNotification.PROPERTY_DESCRIPTION, description);

            notification.setProperty(UserNotification.PROPERTY_MODIFIED_BY, userThatModifiedState);
            datastore.put(txn, notification);
        }
        txn.commit();
    }

    // txn = datastore.beginTransaction(options);
    // if (statusLogKey != null) {
    // // Gets the notification id (or StatusLog object id) generated by
    // // the change of status
    // long id = statusLogKey.getId();
    //
    // /*
    // * Now, we want to get the id of every user which is following the
    // * report which status was changed;
    // */
    // try {
    // ArrayList<Entity> userNotificationList = new ArrayList<Entity>();
    // Filter filter = new FilterPredicate(Follower.PROPERTY_REPORT,
    // FilterOperator.EQUAL, reportId);
    // Query query = new Query(Follower.KIND).setFilter(filter);
    // Iterator<Entity> results = datastore.prepare(query).asIterator();
    // if (!results.hasNext()) {
    // // There is no followers of this report
    // LOG.warning(Utils.OUTPUT_MISSING_LIST_REPORTS);
    // return Response.status(Status.NO_CONTENT).build();
    // }
    // /*
    // * For every user following the report, we create a new entity
    // * of the type UserNotification, which links the user's email to
    // * the notification id: this tells that this user must receive
    // * this notification.
    // *
    // * We set the property wasLoadedToClient, because the
    // * notification wasn't yet loaded to client (pushed by
    // * corresponding user)
    // *
    // * We set the property wasSeen to false, because the
    // * notification wasn't seen yet by the user.
    // *
    // */
    // while (results.hasNext()) {
    // Entity follow = results.next();
    // String email = (String) follow.getProperty(Follower.PROPERTY_EMAIL);
    //
    // Entity notification = new Entity(UserNotification.KIND);
    // notification.setProperty(UserNotification.PROPERTY_EMAIL, email);
    // notification.setProperty(UserNotification.PROPERTY_REPORT_ID, reportId);
    // notification.setProperty(UserNotification.PROPERTY_WAS_SEEN, false);
    // notification.setProperty(UserNotification.PROPERTY_PREV_STATUS,
    // currentStatus);
    // notification.setProperty(UserNotification.PROPERTY_NEW_STATUS,
    // newStatus);
    // notification.setProperty(UserNotification.PROPERTY_MODIFIED_DATE, new
    // Date());
    // notification.setProperty(UserNotification.PROPERTY_MODIFIED_BY,
    // token.user);
    // datastore.put(txn, notification);
    // }
    // datastore.put(txn, userNotificationList);
    // txn.commit();
    // LOG.fine(Utils.OUTPUT_STATUS_REGISTERED);
    // return Response.ok(g.toJson(stLog)).entity(g.toJson(new
    // StringUtil(Utils.OUTPUT_STATUS_REGISTERED))).build();
    //
    // } catch (Exception e) {
    // txn.rollback();
    // LOG.severe("Unknown Exception " + e.getMessage());
    // return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    // } finally {
    // if (txn.isActive()) {
    // txn.rollback();
    // LOG.severe(Utils.OUTPUT_TRANSACTION_STILL_ACTIVE);
    // return Response.status(Status.INTERNAL_SERVER_ERROR).entity(g.toJson(new
    // StringUtil(Utils.OUTPUT_TRANSACTION_STILL_ACTIVE)))
    // .build();
    // }
    // }
    // } else
    // return Response.status(Status.INTERNAL_SERVER_ERROR).build();

	/*
     * @PUT
	 * 
	 * @Path("/change/{id}")
	 * 
	 * @Consumes(MediaType.APPLICATION_JSON) public Response
	 * changeReportStatus(@PathParam("id") String reportId, String jsonString) {
	 * 
	 * 
	 * JsonElement jelement = new JsonParser().parse(jsonString); JsonObject
	 * jobject = jelement.getAsJsonObject();
	 * 
	 * JsonElement jstatus = jobject.get("status"); JsonObject jtoken =
	 * jobject.getAsJsonObject("token");
	 * 
	 * AuthToken token = g.fromJson(jtoken.toString(), AuthToken.class); String
	 * newStatus = g.fromJson(jstatus.toString(), String.class);
	 * 
	 * Entity user = Utils.getUser(token.user, datastore, LOG); if (user ==
	 * null) { LOG.warning(Utils.OUTPUT_MISSING_USER); return
	 * Response.status(Status.UNAUTHORIZED).entity(new
	 * StringUtil(Utils.OUTPUT_MISSING_USER)).build(); } Response rsp =
	 * Utils.securityValidations(user, token,
	 * ResourcesEnum.BACKOFFICE_CHANGE_REPORT_STATUS, datastore, LOG); if
	 * (rsp.getStatus() != Status.ACCEPTED.getStatusCode()) return rsp;
	 * 
	 * TransactionOptions options = TransactionOptions.Builder.withXG(true);
	 * Transaction txn = datastore.beginTransaction(options);
	 * 
	 * // Validacao: testar se o report existe Key reportKey =
	 * KeyFactory.createKey(Report.KIND, Long.parseLong(reportId)); try { Entity
	 * reportEntity = datastore.get(reportKey);
	 * 
	 * // Validacao: verificar se o estado passado e valido
	 * 
	 * // Validacao: verificar se o estado passado e o mesmo String
	 * currentStatus = (String)
	 * reportEntity.getProperty(Report.PROPERTY_STATUS_DESCRIPTION); if
	 * (currentStatus.equals(newStatus)) { // conflict return
	 * Response.status(Status.CONFLICT).entity( g.toJson(new
	 * StringUtil("New status is the same as the provided status."))).build(); }
	 * reportEntity.setProperty(Report.PROPERTY_STATUS_DESCRIPTION, newStatus);
	 * datastore.put(txn, reportEntity);
	 * 
	 * queue.add(TaskOptions.Builder.withUrl(
	 * "/rest/backoffice/report/sendNotifications") .param("reportId", reportId)
	 * .param("currentStatus", currentStatus) .param("newStatus", newStatus)
	 * .param("user", token.user) .header("Content-Type",
	 * "application/html; charset=utf8"));
	 * 
	 * txn.commit(); return Response.ok(g.toJson(new
	 * StringUtil("Everything went fine."))).build();
	 * 
	 * 
	 * // // depois disto gerar notificacoes // // para cada entrada em Follower
	 * que tenha reportId igual a este, // // gerar uma nova entrada em
	 * UserNotification // // Filter filterByReport = new
	 * FilterPredicate(Follower.PROPERTY_REPORT, FilterOperator.EQUAL,
	 * reportId); // Query query = new
	 * Query(Follower.KIND).setFilter(filterByReport); // Iterator<Entity> it =
	 * datastore.prepare(query).asIterator(); // // while (it.hasNext()) { //
	 * Entity follow = it.next(); // String email = (String)
	 * follow.getProperty(Follower.PROPERTY_EMAIL); // // Entity notification =
	 * new Entity(UserNotification.KIND); //
	 * notification.setProperty(UserNotification.PROPERTY_EMAIL, email); //
	 * notification.setProperty(UserNotification.PROPERTY_REPORT_ID, reportId);
	 * // notification.setProperty(UserNotification.PROPERTY_WAS_SEEN, false);
	 * // notification.setProperty(UserNotification.PROPERTY_PREV_STATUS,
	 * currentStatus); //
	 * notification.setProperty(UserNotification.PROPERTY_NEW_STATUS,
	 * newStatus); //
	 * notification.setProperty(UserNotification.PROPERTY_MODIFIED_DATE, new
	 * Date()); //
	 * notification.setProperty(UserNotification.PROPERTY_MODIFIED_BY,
	 * token.user); // datastore.put(txn, notification); // } } catch
	 * (EntityNotFoundException e) { return
	 * Response.status(Status.NOT_FOUND).entity( g.toJson(new
	 * StringUtil("Report was not found."))).build(); } finally { if
	 * (txn.isActive()) { txn.rollback(); } } }
	 */

    @POST
    @Path("/listAll")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWorkerReports(AuthToken token) {
        // Logs the starting of the operation
        LOG.info("Attempting to get all reports by backoffice user: " + token.user);

		  /* Security Validations */
        Entity user = Utils.getUser(token.user, datastore, LOG);
        if (user == null) {
            LOG.warning(Utils.OUTPUT_MISSING_USER);
            return Response.status(Status.UNAUTHORIZED).entity(g.toJson(new StringUtil(Utils.OUTPUT_MISSING_USER))).build();
        }
        Response rsp = Utils.securityValidations(user, token, ResourcesEnum.BACKOFFICE_GET_WORKER_REPORTS, datastore,
                LOG);
        if (rsp.getStatus() != Status.ACCEPTED.getStatusCode())
            return rsp;
        /* Security Validations */

        // Creation of list that will hold the reports of the logged on user
        List<Report> listOfReports = new ArrayList<>();

        // Query that given the user as ancestor, retrieves all the reports from
        // the same user
        // Filter userFilter= new FilterPredicate(Report.PROPERTY_USER,
        // FilterOperator.EQUAL, token.user);
        Filter reportFilter = new FilterPredicate(Report.PROPERTY_WORKER_RESPONSIBLE, FilterOperator.EQUAL, token.user);
        Query ctrQuery = new Query(Report.KIND).setFilter(reportFilter).addSort(Report.PROPERTY_POINTS, Query.SortDirection.DESCENDING);
        Iterator<Entity> results = datastore.prepare(ctrQuery).asIterator();
        if (!results.hasNext()) {
            // If does not exist any report on the system, it returns HTTP 204
            // code
            LOG.warning(Utils.OUTPUT_MISSING_LIST_REPORTS);
            return Response.status(Status.NO_CONTENT).build();
        }
        // While exists reports, append them to the list of reports
        while (results.hasNext()) {

            Entity report = results.next();
            String id = String.valueOf(report.getKey().getId());
            Filter filterByUser = new FilterPredicate(Follower.PROPERTY_EMAIL, FilterOperator.EQUAL, token.user);
            Filter filterByReport = new FilterPredicate(Follower.PROPERTY_REPORT, FilterOperator.EQUAL, id);
            Query query = new Query(Follower.KIND).setFilter(
                    Query.CompositeFilterOperator.and(filterByUser, filterByReport));
            Entity result = datastore.prepare(query).asSingleEntity();

            boolean isFollowing = false;

            if (result == null)
                isFollowing = false;
            else
                isFollowing = true;
            Report newReport = Utils.buildReportForOutput(report, isFollowing);
            listOfReports.add(newReport);
        }
        // Returns all the reports as array
        //  Report[] arr = new Report[listOfReports.size()];
        // listOfReports.toArray(arr);
        return Response.ok(g.toJson(listOfReports)).build();
    }

    @POST
    @Path("/listA")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getW(String jsonString) {
        // Logs the starting of the operation
        JsonElement jelement = new JsonParser().parse(jsonString);
        JsonObject jobject = jelement.getAsJsonObject();
        JsonObject jtoken = jobject.getAsJsonObject("token");
        AuthToken token = g.fromJson(jtoken.toString(), AuthToken.class);
        LOG.info("Attempting to get all reports by backoffice user: " + token.user);

			  /* Security Validations */
        Entity user = Utils.getUser(token.user, datastore, LOG);
        if (user == null) {
            LOG.warning(Utils.OUTPUT_MISSING_USER);
            return Response.status(Status.UNAUTHORIZED).entity(g.toJson(new StringUtil(Utils.OUTPUT_MISSING_USER))).build();
        }
        Response rsp = Utils.securityValidations(user, token, ResourcesEnum.BACKOFFICE_GET_WORKER_REPORTS, datastore,
                LOG);
        if (rsp.getStatus() != Status.ACCEPTED.getStatusCode())
            return rsp;
		/* Security Validations */

        // Filter userFilter= new FilterPredicate(Report.PROPERTY_USER,
        // FilterOperator.EQUAL, token.user);

        // Creation of list that will hold the reports of the logged on user
        List<Report> listOfReports = new ArrayList<Report>();

        // Query that given the user as ancestor, retrieves all the reports from
        // the same user
        Filter reportFilter = new FilterPredicate(Report.PROPERTY_WORKER_RESPONSIBLE, FilterOperator.EQUAL, token.user);
        Query ctrQuery = new Query(Report.KIND).setFilter(reportFilter).addSort(Report.PROPERTY_POINTS, Query.SortDirection.DESCENDING);
        PreparedQuery preparedQuery = datastore.prepare(ctrQuery);
        FetchOptions fetchOptions = FetchOptions.Builder.withLimit(Utils.REPORTS_CURSOR_LIMIT);
        // Iterator<Entity> results = datastore.prepare(ctrQuery).asIterator();

        boolean hasCursor = jobject.has("cursor");
        String cursor = null;
        // If this servlet is passed a cursor parameter, let's use it
        if (hasCursor) {
            JsonElement jcursor = jobject.getAsJsonPrimitive("cursor");
            cursor = g.fromJson(jcursor.toString(), String.class);
            fetchOptions.startCursor(Cursor.fromWebSafeString(cursor));
        }

        QueryResultList<Entity> resultsList = preparedQuery.asQueryResultList(fetchOptions);
        cursor = resultsList.getCursor().toWebSafeString();

        if (resultsList.isEmpty()) {
            // If does not exist any report on the system, it returns HTTP 204
            // code
            LOG.warning(Utils.OUTPUT_MISSING_LIST_REPORTS);
            return Response.status(Status.NO_CONTENT).build();
        }

        // While exists reports, append them to the list of reports
        for (Entity report : resultsList) {

            String id = String.valueOf(report.getKey().getId());
            Filter filterByUser = new FilterPredicate(Follower.PROPERTY_EMAIL, FilterOperator.EQUAL, token.user);
            Filter filterByReport = new FilterPredicate(Follower.PROPERTY_REPORT, FilterOperator.EQUAL, id);
            Query query = new Query(Follower.KIND).setFilter(
                    Query.CompositeFilterOperator.and(filterByUser, filterByReport));
            Entity result = datastore.prepare(query).asSingleEntity();

            boolean isFollowing = false;

            if (result == null)
                isFollowing = false;
            else
                isFollowing = true;
            Report newReport = Utils.buildReportForOutput(report, isFollowing);
            listOfReports.add(newReport);
        }

        JsonParser parser = new JsonParser();
        JsonArray object = (JsonArray) parser.parse(g.toJson(listOfReports));

        JsonObject objectOutput = new JsonObject();
        objectOutput.addProperty("cursor", cursor);
        objectOutput.add("reports", object);
        return Response.ok(g.toJson(objectOutput)).build();
    }

    //
    // TODO: ../listAll?offset=50&limit=25
    // ou no json colocar o link em si
    //
    // @POST
    // @Path("/listAll")
    // @Consumes(MediaType.APPLICATION_JSON)
    // @Produces(MediaType.APPLICATION_JSON)
    // public Response getAllReports(AuthToken token) {
    //
    // // Logs the starting of the operation
    // LOG.info("Attempting to get all reports by backoffice user: " +
    // token.user);
    //
    // /* Security Validations */
    // Entity user = Utils.getUser(token.user, datastore, LOG);
    // if (user == null) {
    // LOG.warning(Utils.OUTPUT_MISSING_USER);
    // return Response.status(Status.UNAUTHORIZED).entity(new
    // StringUtil(Utils.OUTPUT_MISSING_USER)).build();
    // }
    // Response rsp = Utils.securityValidations(user, token,
    // ResourcesEnum.FRONTOFFICE_REGISTER_NEW_REPORT, datastore,
    // LOG);
    // if (rsp.getStatus() != Status.ACCEPTED.getStatusCode())
    // return rsp;
    // /* Security Validations */
    //
    // // Creation of list that will hold the reports of the logged on user
    // List<Report> listOfReports = new ArrayList<Report>();
    //
    // // Query that given the user as ancestor, retrieves all the reports from
    // // the same user
    // // Filter userFilter= new FilterPredicate(Report.PROPERTY_USER,
    // // FilterOperator.EQUAL, token.user);
    // Query ctrQuery = new Query(Report.KIND);
    // Iterator<Entity> results = datastore.prepare(ctrQuery).asIterator();
    // if (!results.hasNext()) {
    // // If does not exist any report on the system, it returns HTTP 204
    // // code
    // LOG.warning(Utils.OUTPUT_MISSING_LIST_REPORTS);
    // return Response.status(Status.NO_CONTENT).build();
    // }
    // // While exists reports, append them to the list of reports
    // while (results.hasNext()) {
    // Entity report = results.next();
    // String id = String.valueOf(report.getKey().getId());
    // String type = (String) report.getProperty(Report.PROPERTY_TYPE);
    // long priority = (long) report.getProperty(Report.PROPERTY_PRIORITY);
    // String statusDescription = (String)
    // report.getProperty(Report.PROPERTY_STATUS_DESCRIPTION);
    // String description = (String)
    // report.getProperty(Report.PROPERTY_DESCRIPTION);
    // String imageUrl = (String) report.getProperty(Report.PROPERTY_IMAGE);
    // String address = (String)
    // report.getProperty(Report.PROPERTY_ADDRESS_AS_STREET);
    // double lat = (double) report.getProperty(Report.PROPERTY_LATITUDE);
    // double lng = (double) report.getProperty(Report.PROPERTY_LONGITUDE);
    // long numOfFollowers = (long)
    // report.getProperty(Report.PROPERTY_FOLLOWERS);
    // String responsible = (String)
    // report.getProperty(Report.PROPERTY_WORKER_RESPONSIBLE);
    //
    // Report newReport = new Report(id, type, (int) priority,
    // statusDescription, description, imageUrl, address,
    // lat, lng, null, (int) numOfFollowers, responsible);
    // listOfReports.add(newReport);
    // }
    // // Returns all the reports as array
    // Report[] arr = new Report[listOfReports.size()];
    // listOfReports.toArray(arr);
    // return Response.ok(g.toJson(new StringUtil(arr)).build();
    // }

    @POST
    @Path("/get/logs/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatusLog(@PathParam("id") String reportId, AuthToken token) {
        // Logs the starting of the operation
        LOG.info("Attempting to get all status logs of specified report by backoffice user: " + token.user);

		  /* Security Validations */
        Entity user = Utils.getUser(token.user, datastore, LOG);
        if (user == null) {
            LOG.warning(Utils.OUTPUT_MISSING_USER);
            return Response.status(Status.UNAUTHORIZED).entity(g.toJson(new StringUtil(Utils.OUTPUT_MISSING_USER))).build();
        }
        Response rsp = Utils.securityValidations(user, token, ResourcesEnum.BACKOFFICE_GET_STATUS_LOG, datastore, LOG);
        if (rsp.getStatus() != Status.ACCEPTED.getStatusCode())
            return rsp;
		/* Security Validations */

        // Creation of list that will hold the reports of the logged on user
        List<StatusLog> listOfStatusLog = new ArrayList<>();

        // Query that given the user as ancestor, retrieves all the status logs
        // from
        // a specified report
        Filter reportFilter = new FilterPredicate(StatusLog.PROPERTY_REPORT_ID, FilterOperator.EQUAL, reportId);
        Query ctrQuery = new Query(StatusLog.KIND).setFilter(reportFilter);
        Iterator<Entity> results = datastore.prepare(ctrQuery).asIterator();
        if (!results.hasNext()) {
            // If does not exist any status log for the report on the system, it
            // returns HTTP 403
            // code
            LOG.warning(Utils.OUTPUT_MISSING_REPORT);
            return Response.status(Status.FORBIDDEN).entity(g.toJson(new StringUtil(Utils.OUTPUT_MISSING_REPORT)))
                    .build();
        }
        // While exists status logs for the report, append them to the list of
        // status logs
        while (results.hasNext()) {
            Entity statusLog = results.next();
            String id = (String) statusLog.getProperty(StatusLog.PROPERTY_REPORT_ID);
            String statusDescription = (String) statusLog.getProperty(StatusLog.PROPERTY_STATUS_DESCRIPTION);
            String description = (String) statusLog.getProperty(StatusLog.PROPERTY_DESCRIPTION);
            long modifiedTime = (long) statusLog.getProperty(StatusLog.PROPERTY_MODIFIED_TIME);
            String modifiedDate = (String) statusLog.getProperty(StatusLog.PROPERTY_MODIFIED_DATE);
            String modifiedBy = (String) statusLog.getProperty(StatusLog.PROPERTY_MODIFIED_BY);

            StatusLog newStatusLog = new StatusLog(id, statusDescription, description, modifiedTime, modifiedDate,
                    modifiedBy);
            listOfStatusLog.add(newStatusLog);
        }
        // Returns all the status logs as array
        //  StatusLog[] arr = new StatusLog[listOfStatusLog.size()];
        // listOfStatusLog.toArray(arr);
        return Response.ok(g.toJson(listOfStatusLog)).build();
    }

    // --- added ---

    @POST
    @Path("/details")
    public Response getWorkerDetails(AuthToken token) {
			  /* Security Validations */
        Entity user = Utils.getUser(token.user, datastore, LOG);
        if (user == null) {
            LOG.warning(Utils.OUTPUT_MISSING_USER);
            return Response.status(Status.UNAUTHORIZED).entity(g.toJson(new StringUtil(Utils.OUTPUT_MISSING_USER))).build();
        }
        Response rsp = Utils.securityValidations(user, token, ResourcesEnum.BACKOFFICE_WORKER_DETAILS, datastore, LOG);
        if (rsp.getStatus() != Status.ACCEPTED.getStatusCode())
            return rsp;
		/* Security Validations */

        String workerInfo = (String) user.getProperty(User_BackOffice.PROPERTY_WORKER_INFO);
        @SuppressWarnings("unchecked")
        ArrayList<String> workingArea = (ArrayList<String>) user.getProperty(User_BackOffice.PROPERTY_WORKING_AREA);

        JsonObject obj = new JsonObject();
        obj.addProperty("workerInfo", workerInfo);

        String data = g.toJson(workingArea);
        JsonArray jsonArray = new JsonParser().parse(data).getAsJsonArray();
        obj.add("workingArea", jsonArray);

        return Response.ok(g.toJson(obj)).build();
    }

    @POST
    @Path("/updateDetails")
    public Response updateWorkerInfo(String jsonString) {
        JsonElement jelement = new JsonParser().parse(jsonString);
        JsonObject jobject = jelement.getAsJsonObject();
        JsonObject jtoken = jobject.getAsJsonObject("token");
        JsonObject jdata = jobject.getAsJsonObject("data");
        JsonElement jworkerInfo = jdata.get("workerInfo");
        JsonArray countyArray = jdata.getAsJsonArray("workingArea");

        AuthToken token = g.fromJson(jtoken.toString(), AuthToken.class);

        String workerInfo = g.fromJson(jworkerInfo.toString(), String.class);
        ArrayList<String> list = new ArrayList<String>(countyArray.size());
        for (int i = 0; i < countyArray.size(); i++) {
            JsonElement element = countyArray.get(i);
            list.add(g.fromJson(element.toString(), String.class));
        }

		/* Security Validations */
        Entity user = Utils.getUser(token.user, datastore, LOG);
        if (user == null) {
            LOG.warning(Utils.OUTPUT_MISSING_USER);
            return Response.status(Status.UNAUTHORIZED).entity(new StringUtil(Utils.OUTPUT_MISSING_USER)).build();
        }
        Response rsp = Utils.securityValidations(user, token, ResourcesEnum.BACKOFFICE_WORKER_UPDATE_DETAILS, datastore, LOG);
        if (rsp.getStatus() != Status.ACCEPTED.getStatusCode())
            return rsp;
		/* Security Validations */


        // Calculate which counties are in the current list and not in the new List

        // Gets the list of counties that were removed
        @SuppressWarnings("unchecked")
        ArrayList<String> currentAreaList = (ArrayList<String>) user.getProperty(User_BackOffice.PROPERTY_WORKING_AREA);

        // Areas that were inexistent
        ArrayList<String> inexistentCounties = new ArrayList<String>(currentAreaList);
        inexistentCounties.removeAll(list);

        // New areas added
        ArrayList<String> newCounties = new ArrayList<String>(list);
        newCounties.removeAll(currentAreaList);

        System.out.println(inexistentCounties);
        System.out.println(newCounties);

        // Explore every removed county: check if there is any report in this county
        // associated to this user with the state "Em Resolução"


        // Transaction to deal with every database save
        TransactionOptions options = TransactionOptions.Builder.withXG(true);
        Transaction txn = datastore.beginTransaction(options);

        // Mapa de resultados, chave indica o concelho, valor indica se remocao foi feita
        // com sucesso
        Map<String, Boolean> finalRes = new HashMap<String, Boolean>();
        for (String county : inexistentCounties) {

            // Query to erase responsible from reports
            Filter filterByCounty = new FilterPredicate("address.county", FilterOperator.EQUAL, county);
            Filter filterByUser = new FilterPredicate(Report.PROPERTY_WORKER_RESPONSIBLE, FilterOperator.EQUAL, token.user);
            Query prevQuery = new Query(Report.KIND).setFilter(
                    CompositeFilterOperator.and(filterByCounty, filterByUser));
            Filter filterByState = new FilterPredicate(Report.PROPERTY_STATUS_DESCRIPTION, FilterOperator.EQUAL, "Em Resolução");


            // Filter to erase responsible from report types
            Filter filterByCountyToType = new FilterPredicate(ReportType.PROPERTY_COUNTY, FilterOperator.EQUAL, county);
            Filter filterByRespToType = new FilterPredicate(ReportType.PROPERTY_RESPONSIBLE, FilterOperator.EQUAL, token.user);

            Query reportTypeQuery = new Query(ReportType.KIND).setFilter(
                    CompositeFilterOperator.and(filterByCountyToType, filterByRespToType));

            Query query = new Query(Report.KIND).setFilter(
                    CompositeFilterOperator.and(filterByCounty, filterByUser, filterByState));
            Iterator<Entity> results = datastore.prepare(query).asIterator();
            if (results.hasNext()) {
                finalRes.put(county, false);
            } else {
                Iterator<Entity> reportIterator = datastore.prepare(prevQuery).asIterator();
                while (reportIterator.hasNext()) {
                    Entity report = reportIterator.next();
                    report.setProperty(Report.PROPERTY_WORKER_RESPONSIBLE, null);
                    datastore.put(txn, report);
                }

                // reportTypeQuery
                Iterator<Entity> typeIterator = datastore.prepare(reportTypeQuery).asIterator();
                while (typeIterator.hasNext()) {
                    Entity type = typeIterator.next();
                    type.setProperty(ReportType.PROPERTY_RESPONSIBLE, null);
                    datastore.put(txn, type);
                }
                finalRes.put(county, true);
            }
        }

        for (Map.Entry<String, Boolean> entry : finalRes.entrySet()) {
            String key = entry.getKey();
            boolean value = entry.getValue();
            if (value) {
                currentAreaList.remove(key);
            }

        }

        for (String newCounty : newCounties) {
            currentAreaList.add(newCounty);
        }

        user.setProperty(User_BackOffice.PROPERTY_WORKER_INFO, workerInfo);
        user.setProperty(User_BackOffice.PROPERTY_WORKING_AREA, currentAreaList);

        Cache cache;
        CacheManager manager = CacheManager.getInstance();
        try {

            cache = manager.getCache("userEntities");
            if (cache == null) {
                CacheFactory cacheFactory = manager.getCacheFactory();
                Map<Object, Object> properties = new HashMap<>();
                properties.put(GCacheFactory.EXPIRATION_DELTA, TimeUnit.MINUTES.toSeconds(30));
                cache = cacheFactory.createCache(properties);
                manager.registerCache("userEntities", cache);
            }

            cache.put(token.user, user);

        } catch (CacheException e) {

        }


        datastore.put(txn, user);
        txn.commit();

        JsonObject toReturn = new JsonObject();

        String data = g.toJson(newCounties);
        JsonArray jsonArray = new JsonParser().parse(data).getAsJsonArray();
        toReturn.add("addedCounties", jsonArray);

        JsonObject res = g.toJsonTree(finalRes).getAsJsonObject();
        toReturn.add("removedCounties", res);


        System.out.println(g.toJson(toReturn));


        LOG.info("Worker info was updated successfuly.");
        return Response.ok(g.toJson(toReturn)).build();
    }
}