package pt.unl.fct.di.apdc.flagnpatch.resources.frontOffice;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.gson.*;
import com.sun.org.apache.regexp.internal.RE;
import org.apache.commons.lang3.SerializationUtils;
import pt.unl.fct.di.apdc.flagnpatch.entities.*;
import pt.unl.fct.di.apdc.flagnpatch.inputData.CommentData;
import pt.unl.fct.di.apdc.flagnpatch.inputData.ReportData;
import pt.unl.fct.di.apdc.flagnpatch.resources.general.Utils;
import pt.unl.fct.di.apdc.flagnpatch.utils.ResourcesEnum;
import pt.unl.fct.di.apdc.flagnpatch.utils.RolesEnum;
import pt.unl.fct.di.apdc.flagnpatch.utils.StatusEnum;
import pt.unl.fct.di.apdc.flagnpatch.utils.StringUtil;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

@Path("/report")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ReportResource {

    // Logger object
    private static final Logger LOG = Logger.getLogger(ReportResource.class.getName());
    // Datastore link
    private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    // Converts objects to JSON
    private final Gson g = new Gson();

    private final Queue queue = QueueFactory.getDefaultQueue();

    public ReportResource() {

    }

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerNewReport(String jsonString) {
/*Extracts data from custom JSON input string and creates objects from it*/
        JsonElement jelement = new JsonParser().parse(jsonString);
        JsonObject jobject = jelement.getAsJsonObject();
        JsonObject jreport = jobject.getAsJsonObject("report");
        JsonObject jtoken = jobject.getAsJsonObject("token");
        ReportData data = g.fromJson(jreport.toString(), ReportData.class);
        AuthToken token = g.fromJson(jtoken.toString(), AuthToken.class);
        LOG.info("Attempting to register a new report from user: " + token.user);

        //TODO: zona activada ou não
        Key areasKey = KeyFactory.createKey(Areas.KIND, data.address.county);
        try {
            Entity area = datastore.get(areasKey);
            if (!(boolean) area.getProperty(Areas.PROPERTY_IS_AVAILABLE)) {
                LOG.severe("area indisponivel");
                return Response.status(Status.FORBIDDEN).entity(
                        g.toJson(new StringUtil("area indisponivel"))).build();
            }
        } catch (EntityNotFoundException e) {
            LOG.severe("area indisponivel");
            return Response.status(Status.FORBIDDEN).entity(
                    g.toJson(new StringUtil("area indisponivel"))).build();
        }
        //TODO: zona activada ou não

        /* Security Validations */
        Entity user = Utils.getUser(token.user, datastore, LOG);
        if (user == null) {
            LOG.warning(Utils.OUTPUT_MISSING_USER);
            return Response.status(Status.UNAUTHORIZED).entity(g.toJson(new StringUtil(Utils.OUTPUT_MISSING_USER))).build();
        }
        Response rsp = Utils.securityValidations(user, token, ResourcesEnum.FRONTOFFICE_REGISTER_NEW_REPORT, datastore,
                LOG);
        if (rsp.getStatus() != Status.ACCEPTED.getStatusCode())
            return rsp;
        /* Security Validations */
        long numberReports = (long) user.getProperty(User_FrontOffice.PROPERTY_NUMBER_REPORTS);
           String role = (String) user.getProperty(User_Global.PROPERTY_ROLE);
            if (role.equalsIgnoreCase(RolesEnum.TRIAL_USER.getRoleDescription())) {

                LOG.warning("numberReport " + numberReports);
                boolean isVerified = (boolean) user.getProperty(User_FrontOffice.PROPERTY_ACCOUNT_VERIFIED);
                if ((int)numberReports > 0 && !isVerified) {
                    LOG.warning(Utils.OUTPUT_NO_MORE_REPORTS_TRIAL);
                    return Response.status(Status.FORBIDDEN).entity(g.toJson(new StringUtil(Utils.OUTPUT_NO_MORE_REPORTS_TRIAL))).build();
                }
            }
        // Validates the report fields
        if (!data.validReport()) {
            // If missing or wrong fields were introduced, it returns HTTP 400
            // code
            LOG.warning(Utils.OUTPUT_WRONG_FIELDS);
            return Response.status(Status.BAD_REQUEST).entity(g.toJson(new StringUtil(Utils.OUTPUT_WRONG_FIELDS))).build();
        }

        // Report type validation - check if there is a type with the provided name
        // in the provided county in the database
        Filter filterByTypeName = new FilterPredicate(
                ReportType.PROPERTY_NAME, FilterOperator.EQUAL, data.type);
        Filter filterByCounty = new FilterPredicate(
                ReportType.PROPERTY_COUNTY, FilterOperator.EQUAL, data.address.county);
        Query query = new Query(ReportType.KIND).setFilter(
                CompositeFilterOperator.and(filterByTypeName, filterByCounty));
        Entity typeEntity = null;
        try {
            typeEntity = datastore.prepare(query).asSingleEntity();
        } catch (PreparedQuery.TooManyResultsException e) {
            LOG.severe(Utils.OUTPUT_TOO_MANY_REPORT_TYPES_FOR_COUNTY);
            return Response.status(Status.CONFLICT).entity(
                    g.toJson(new StringUtil(Utils.OUTPUT_TOO_MANY_REPORT_TYPES_FOR_COUNTY))).build();
        }

        if (typeEntity == null) {
            // Specidied type does not exist
            return Response.status(Status.NOT_FOUND).entity(
                    g.toJson(new StringUtil(Utils.OUTPUT_TYPE_MISSING))).build();
        }

        String responsible = (String) typeEntity.getProperty(ReportType.PROPERTY_RESPONSIBLE);

        // In case token is valid and report fields are valid, begin transaction
        TransactionOptions options = TransactionOptions.Builder.withXG(true);
        Transaction txn = datastore.beginTransaction(options);

        // Creates a new entity for the report and saves it on database

        Entity reportEntity = new Entity(Report.KIND);

        long registerTime = System.currentTimeMillis();
        int points = 0;
        if (!data.imageUrl.trim().equalsIgnoreCase(Utils.DEFAULT_IMAGE.trim())) {
            points += Utils.IMAGE_POINTS;
        }
        points += data.priority * 1;

        reportEntity.setIndexedProperty(Report.PROPERTY_USER, token.user);
        reportEntity.setIndexedProperty(Report.PROPERTY_TYPE, data.type);
        reportEntity.setIndexedProperty(Report.PROPERTY_PRIORITY, data.priority);
        reportEntity.setIndexedProperty(Report.PROPERTY_STATUS_DESCRIPTION, StatusEnum.SUBMITED.getStatusDescription());
        reportEntity.setProperty(Report.PROPERTY_DESCRIPTION, data.description);
        reportEntity.setProperty(Report.PROPERTY_IMAGE, data.imageUrl);
        reportEntity.setIndexedProperty(Report.PROPERTY_ADDRESS_AS_STREET, data.addressAsStreet);
        reportEntity.setIndexedProperty(Report.PROPERTY_ADDRESS, Utils.buildAddress(data.address));
        reportEntity.setIndexedProperty(Report.PROPERTY_LATITUDE, data.lat);
        reportEntity.setIndexedProperty(Report.PROPERTY_LONGITUDE, data.lng);
        reportEntity.setProperty(Report.PROPERTY_FOLLOWERS, 1);
        reportEntity.setProperty(Report.PROPERTY_CREATION_DATE, registerTime);
        reportEntity.setIndexedProperty(Report.PROPERTY_WORKER_RESPONSIBLE, responsible);
        reportEntity.setIndexedProperty(Report.PROPERTY_POINTS, points);
        reportEntity.setIndexedProperty(Report.PROPERTY_SUM_PRIORITY, data.priority);
        System.out.println(data.imageUrl.trim());
        System.out.println(Utils.DEFAULT_IMAGE.trim());


        Key reportKey = datastore.put(txn, reportEntity);
        String id = String.valueOf(reportKey.getId());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Lisbon"));
        String creationTime = sdf.format(registerTime);

        Report report = new Report(id, data.type, data.priority, creationTime, token.user, StatusEnum.SUBMITED.getStatusDescription(),
                data.description, data.imageUrl, data.addressAsStreet, data.lat, data.lng, data.address, 1,
                true, points);

        // TODO: verificar se é útil
        user.setProperty(User_FrontOffice.PROPERTY_NUMBER_REPORTS, numberReports + 1);
        datastore.put(txn, user);

        queue.add(TaskOptions.Builder.withUrl("/rest/report/registerDerivative")
                .param("reportKeyId", String.valueOf(reportKey.getId()))
                .param("registerTime", String.valueOf(registerTime))
                .param("priority", String.valueOf(data.priority))
                .param("email", token.user)
                .header("Content-Type", "application/html; charset=utf8"));

        queue.add(TaskOptions.Builder.withUrl("/rest/report/changeGraphInfoOfCounty").etaMillis(System.currentTimeMillis() + 5000)
                .param("county", data.address.county)
                .param("typeReport", data.type)
                .header("Content-Type", "application/html; charset=utf8"));

        txn.commit();
        LOG.info(Utils.OUTPUT_REPORT_REGISTERED + " " + id);
        return Response.ok(g.toJson(report)).build();
    }

    @POST
    @Path("/changeGraphInfoOfCounty")
    public void callChangeGraphInfoOfCounty(@Context HttpServletRequest httpServletRequest) {


        String county = httpServletRequest.getParameter("county");
        String typeReport = httpServletRequest.getParameter("typeReport");

        TransactionOptions options = TransactionOptions.Builder.withXG(true);
        Transaction txn = datastore.beginTransaction(options);

        Filter filterByCounty = new FilterPredicate(Graph.PROPERTY_COUNTY, FilterOperator.EQUAL, county);
        Filter filterByActive = new FilterPredicate(Graph.PROPERTY_ACTIVE, FilterOperator.EQUAL, true);
        Query query = new Query(Graph.KIND).setFilter(
                CompositeFilterOperator.and(filterByActive, filterByCounty));
        Entity entity = datastore.prepare(query).asSingleEntity();

        if (entity == null)
            LOG.severe("nao existe a entidade graph com county x e active y");

        if (entity.hasProperty(typeReport)) {
            int typeReportOldCounter = Integer.parseInt(String.valueOf(entity.getProperty(typeReport)));
            int oldStatusCounter = Integer.parseInt(String.valueOf(entity.getProperty(StatusEnum.SUBMITED.getStatusDescription())));
            entity.setProperty(typeReport, typeReportOldCounter + 1);
            entity.setProperty(StatusEnum.SUBMITED.getStatusDescription(), oldStatusCounter + 1);
            datastore.put(txn, entity);
        } else {
            entity.setProperty(typeReport, 1);
            entity.setProperty(StatusEnum.SUBMITED.getStatusDescription(), 1);
            datastore.put(txn, entity);
        }
        txn.commit();

    }

    @POST
    @Path("/registerDerivative")
    public void callNewReportDerivatives(@Context HttpServletRequest httpServletRequest) {
/*         Creates a new object StatusLogObject that includes
         statusLogEntity entity and StatusLog Object
         gets statusLogEntity entity from the object that will be put on
         datastore*/
        Key reportKey = KeyFactory.createKey(Report.KIND, Long.valueOf(httpServletRequest.getParameter("reportKeyId")));
        long registerTime = Long.valueOf(httpServletRequest.getParameter("registerTime"));
        int priority = Integer.valueOf(httpServletRequest.getParameter("priority"));
        String email = httpServletRequest.getParameter("email");

        TransactionOptions options = TransactionOptions.Builder.withXG(true);
        Transaction txn = datastore.beginTransaction(options);

        Entity statusLogEntity = (Entity) (Utils.newStatusLogObject(StatusEnum.SUBMITED.getStatusDescription(),
                Utils.DATABASE_USER_REPORT_LOG, reportKey, email))[0];
        datastore.put(txn, statusLogEntity);

        Entity followerEntity = new Entity(Follower.KIND);
        followerEntity.setProperty(Follower.PROPERTY_EMAIL, email);
        followerEntity.setProperty(Follower.PROPERTY_REPORT, String.valueOf(reportKey.getId()));
        followerEntity.setProperty(Follower.PROPERTY_PRIORITY, priority);
        followerEntity.setProperty(Follower.PROPERTY_FOLLOW_TIME, registerTime);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Lisbon"));
        String creationDate = sdf.format(registerTime);
        followerEntity.setProperty(Follower.PROPERTY_FOLLOW_DATE, creationDate);
        // User update
        datastore.put(txn, followerEntity);
        txn.commit();
    }

    @POST
    @Path("/myReports")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPersonalReports(AuthToken token) {

        /* Security Validations */
        Entity user = Utils.getUser(token.user, datastore, LOG);
        if (user == null) {
            LOG.warning(Utils.OUTPUT_MISSING_USER);
            return Response.status(Status.UNAUTHORIZED).entity(g.toJson(new StringUtil(Utils.OUTPUT_MISSING_USER))).build();
        }
        Response rsp = Utils.securityValidations(user, token, ResourcesEnum.FRONTOFFICE_GET_PERSONAL_REPORT, datastore, LOG);
        if (rsp.getStatus() != Status.ACCEPTED.getStatusCode())
            return rsp;
        /* Security Validations */

        /* Creation of list that will hold the reports of the logged on user*/
        List<Report> listOfReports = new ArrayList<>();

/* Query that given the user as ancestor, retrieves all the reports from the same user*/
        Filter userFilter = new FilterPredicate(Report.PROPERTY_USER, FilterOperator.EQUAL, token.user);
        Query ctrQuery = new Query(Report.KIND).setFilter(userFilter);
        Iterator<Entity> results = datastore.prepare(ctrQuery).asIterator();
        if (!results.hasNext()) {
            // If does not exist any report from user, it returns HTTP 204 code
            LOG.warning(Utils.OUTPUT_MISSING_LIST_REPORTS);
            return Response.status(Status.NO_CONTENT).build();
        }

        /*   While exists reports, append them to the list of reports*/
        while (results.hasNext()) {
            Entity report = results.next();

            String id = String.valueOf(report.getKey().getId());
            Filter filterByUser = new FilterPredicate(Follower.PROPERTY_EMAIL, FilterOperator.EQUAL, token.user);
            Filter filterByReport = new FilterPredicate(Follower.PROPERTY_REPORT, FilterOperator.EQUAL, id);
            Query query = new Query(Follower.KIND).setFilter(
                    Query.CompositeFilterOperator.and(filterByUser, filterByReport));
            Entity result = datastore.prepare(query).asSingleEntity();
            // txn.rollback();
            boolean isFollowing = false;

            if (result == null)
                isFollowing = false;
            else
                isFollowing = true;

            Report newReport = Utils.buildReportForOutput(report, isFollowing);
            listOfReports.add(newReport);
        }
        /* Returns all the reports as array*/
        //  Report[] arr = new Report[listOfReports.size()];
        // listOfReports.toArray(arr);
        return Response.ok(g.toJson(listOfReports)).build();
    }


    @POST
    @Path("/followedReports")
    public Response getFollowedReports(AuthToken token) {

         /* Security Validations */
        Entity user = Utils.getUser(token.user, datastore, LOG);
        if (user == null) {
            LOG.warning(Utils.OUTPUT_MISSING_USER);
            return Response.status(Status.UNAUTHORIZED).entity(g.toJson(new StringUtil(Utils.OUTPUT_MISSING_USER))).build();
        }
        Response rsp = Utils.securityValidations(user, token, ResourcesEnum.FRONTOFFICE_GET_FOLLOWED_REPORT, datastore, LOG);
        if (rsp.getStatus() != Status.ACCEPTED.getStatusCode())
            return rsp;
        /* Security Validations */

        // Creates a list which will hold reports followed by the user
        List<Report> reports = new ArrayList<>();

        // Gets all the reports followed by this user
        Filter filter = new FilterPredicate(Follower.PROPERTY_EMAIL, FilterOperator.EQUAL, token.user);
        Query query = new Query(Follower.KIND).setFilter(filter);
        Iterator<Entity> results = datastore.prepare(query).asIterator();
        if (!results.hasNext()) {
            // If does not exist any report from user, it returns HTTP 204 code
            LOG.warning(Utils.OUTPUT_MISSING_LIST_REPORTS);
            return Response.status(Status.NO_CONTENT).build();
        }

        while (results.hasNext()) {
            Entity entity = results.next();

            // Gets the id of the report
            String reportId = (String) entity.getProperty(Follower.PROPERTY_REPORT);
            Key reportKey = KeyFactory.createKey(Report.KIND, Long.parseLong(reportId));

            try {
                Entity report = datastore.get(reportKey);
                Report newReport = Utils.buildReportForOutput(report, true);
                reports.add(newReport);

            } catch (EntityNotFoundException e) {
                //continue;
            }
        }
        //  Report[] arr = new Report[reports.size()];
        //  reports.toArray(arr);
        return Response.ok(g.toJson(reports)).build();
    }


    // TODO: deveria ser put pois ja se sabe o id


    @POST
    @Path("/addComment/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addCommentReport(@PathParam("id") String reportId, String jsonString) {
        // Extracts data from custom JSON input string and creates objects from
        // it
        JsonElement jelement = new JsonParser().parse(jsonString);
        JsonObject jobject = jelement.getAsJsonObject();
        JsonObject jcomment = jobject.getAsJsonObject("comment");
        JsonObject jtoken = jobject.getAsJsonObject("token");

        CommentData comment = g.fromJson(jcomment.toString(), CommentData.class);
        AuthToken token = g.fromJson(jtoken.toString(), AuthToken.class);

        /* Security Validations */
        Entity user = Utils.getUser(token.user, datastore, LOG);
        if (user == null) {
            LOG.warning(Utils.OUTPUT_MISSING_USER);
            return Response.status(Status.UNAUTHORIZED).entity(g.toJson(new StringUtil(Utils.OUTPUT_MISSING_USER))).build();
        }
        Response rsp = Utils.securityValidations(user, token, ResourcesEnum.FRONTOFFICE_ADD_COMMENT_REPORT, datastore, LOG);
        if (rsp.getStatus() != Status.ACCEPTED.getStatusCode())
            return rsp;
        /* Security Validations */


        // Field validation
        if (!comment.valid()) {
            LOG.warning(Utils.OUTPUT_WRONG_FIELDS);
            return Response.status(Status.BAD_REQUEST).entity(g.toJson(new StringUtil(Utils.OUTPUT_WRONG_FIELDS))).build();
        }

        Key newKey = KeyFactory.createKey(Report.KIND, Long.parseLong(reportId));
        Transaction txn = datastore.beginTransaction();
        try {
            // Exception is thrown if this report does not exist
            Entity report = datastore.get(newKey);

            Comment outputComment = new Comment(comment.content, token.user, (String) user.getProperty(User_Global.PROPERTY_NAME),
                    Integer.parseInt(String.valueOf((long) user.getProperty(User_FrontOffice.PROPERTY_IDENTIFIER))), System.currentTimeMillis());
            // Adds a new comment to the found report
            Entity commentEntity = new Entity(Comment.KIND, newKey);
            commentEntity.setIndexedProperty(Comment.PROPERTY_AUTHOR_EMAIL, outputComment.authorEmail);
            commentEntity.setIndexedProperty(Comment.PROPERTY_AUTHOR_NAME, outputComment.authorName);
            commentEntity.setIndexedProperty(Comment.PROPERTY_AUTHOR_IDENTIFIER, outputComment.authorIdentifier);
            commentEntity.setIndexedProperty(Comment.PROPERTY_CONTENT, outputComment.content);
            commentEntity.setIndexedProperty(Comment.PROPERTY_TIME, outputComment.registerTime);
            commentEntity.setProperty(Comment.PROPERTY_DATE, outputComment.registerDate);

            long points = (long) report.getProperty(Report.PROPERTY_POINTS);
            report.setIndexedProperty(Report.PROPERTY_POINTS, points+5);

            datastore.put(txn, report);
            datastore.put(txn, commentEntity);
            txn.commit();

            LOG.fine(Utils.OUTPUT_COMMENT_REGISTERED);
            return Response.ok(g.toJson(outputComment)).build();

        } catch (EntityNotFoundException e) {
            txn.rollback();
            LOG.warning(Utils.OUTPUT_MISSING_REPORT);
            return Response.status(Status.FORBIDDEN).entity(g.toJson(new StringUtil(Utils.OUTPUT_MISSING_REPORT))).build();
        } finally {
            // Checks if something went wrong and the transaction is still
            // active.
            if (txn.isActive()) {
                txn.rollback();
                LOG.severe(Utils.OUTPUT_TRANSACTION_FAILED);
                return Response.status(Status.INTERNAL_SERVER_ERROR).entity(g.toJson(new StringUtil(Utils.OUTPUT_TRANSACTION_FAILED))).build();
            }
        }
    }


    // TODO: deveria ser put pois ja se sabe o id

    @POST
    @Path("/getComments/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getCommentsReport(@PathParam("id") String reportId, AuthToken token) {

         /* Security Validations */
        Entity user = Utils.getUser(token.user, datastore, LOG);
        if (user == null) {
            LOG.warning(Utils.OUTPUT_MISSING_USER);
            return Response.status(Status.UNAUTHORIZED).entity(g.toJson(new StringUtil(Utils.OUTPUT_MISSING_USER))).build();
        }
        Response rsp = Utils.securityValidations(user, token, ResourcesEnum.FRONTOFFICE_GET_COMMENT_REPORT, datastore, LOG);
        if (rsp.getStatus() != Status.ACCEPTED.getStatusCode())
            return rsp;
        /* Security Validations */
        //   Transaction txn = datastore.beginTransaction();
        Transaction txn = datastore.beginTransaction();
        Key newKey = KeyFactory.createKey(Report.KIND, Long.parseLong(reportId));
        try {
            // EntityNotFoundException is thrown if report does not exist
            datastore.get(newKey);

            // Returns the list of reports
            List<Comment> listOfComments = new ArrayList<>();

            Query query = new Query(Comment.KIND).addSort(Comment.PROPERTY_TIME, Query.SortDirection.ASCENDING).setAncestor(newKey);
            Iterator<Entity> results = datastore.prepare(query).asIterator();
            txn.rollback();
            if (!results.hasNext()) {

                // If does not exist any report from user, it returns HTTP 204
                // code
                LOG.warning(Utils.OUTPUT_MISSING_LIST_COMMENTS);
                return Response.status(Status.NO_CONTENT).build();
            }
            // While exists reports, append them to the list of reports
            while (results.hasNext()) {
                Entity comment = results.next();
                String content = (String) comment.getProperty(Comment.PROPERTY_CONTENT);

                String authorEmail = (String) comment.getProperty(Comment.PROPERTY_AUTHOR_EMAIL);
                long authorIdentifierLong = (long) comment.getProperty(Comment.PROPERTY_AUTHOR_IDENTIFIER);
//                long authorIdentifierLong=0;
//                if(identifier!=null)
//                authorIdentifierLong =Long.valueOf(identifier);
                String authorName = (String) comment.getProperty(Comment.PROPERTY_AUTHOR_NAME);
                int authorIdentifier = new BigDecimal(authorIdentifierLong).intValueExact();
                long registerTime = (long) comment.getProperty(Comment.PROPERTY_TIME);
                // String registerDate = (long) comment.getProperty(Comment.PROPERTY_DATE);
                Comment newComment = new Comment(content, authorEmail, authorName, authorIdentifier, registerTime);
                listOfComments.add(newComment);
            }
            // Returns all the reports as array
            //       Comment[] arr = new Comment[listOfComments.size()];
            //       listOfComments.toArray(arr);
            //txn.rollback();
            return Response.ok(g.toJson(listOfComments)).build();
        } catch (EntityNotFoundException e) {
            txn.rollback();
            LOG.warning(Utils.OUTPUT_MISSING_REPORT);
            return Response.status(Status.FORBIDDEN).entity(g.toJson(new StringUtil(Utils.OUTPUT_MISSING_REPORT))).build();

        } finally {
            // Checks if something went wrong and the transaction is still
            // active.
            if (txn.isActive()) {
                txn.rollback();
                LOG.severe(Utils.OUTPUT_TRANSACTION_FAILED);
                return Response.status(Status.INTERNAL_SERVER_ERROR).entity(g.toJson(new StringUtil(Utils.OUTPUT_TRANSACTION_FAILED))).build();
            }
        }
    }

    @POST
    @Path("/getC/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getC(@PathParam("id") String reportId, String jsonString) {
        JsonElement jelement = new JsonParser().parse(jsonString);
        JsonObject jobject = jelement.getAsJsonObject();
        JsonObject jtoken = jobject.getAsJsonObject("token");
        AuthToken token = g.fromJson(jtoken.toString(), AuthToken.class);

         /* Security Validations */
        Entity user = Utils.getUser(token.user, datastore, LOG);
        if (user == null) {
            LOG.warning(Utils.OUTPUT_MISSING_USER);
            return Response.status(Status.UNAUTHORIZED).entity(g.toJson(new StringUtil(Utils.OUTPUT_MISSING_USER))).build();
        }
        Response rsp = Utils.securityValidations(user, token, ResourcesEnum.FRONTOFFICE_GET_COMMENT_REPORT, datastore, LOG);
        if (rsp.getStatus() != Status.ACCEPTED.getStatusCode())
            return rsp;
        /* Security Validations */

        Key newKey = KeyFactory.createKey(Report.KIND, Long.parseLong(reportId));
        try {
            // EntityNotFoundException is thrown if report does not exist
            datastore.get(newKey);

            // Returns the list of reports
            List<Comment> listOfComments = new ArrayList<>();
            Query query = new Query(Comment.KIND).setAncestor(newKey).addSort(Comment.PROPERTY_TIME, Query.SortDirection.ASCENDING);

            PreparedQuery preparedQuery = datastore.prepare(query);
            FetchOptions fetchOptions = FetchOptions.Builder.withLimit(Utils.REPORTS_CURSOR_LIMIT);

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
                // If does not exist any report from user, it returns HTTP 204
                // code
                LOG.warning(Utils.OUTPUT_MISSING_LIST_COMMENTS);
                return Response.status(Status.NO_CONTENT).build();
            }

            // While exists reports, append them to the list of reports
            for (Entity comment : resultsList) {
                String content = (String) comment.getProperty(Comment.PROPERTY_CONTENT);
                String authorEmail = (String) comment.getProperty(Comment.PROPERTY_AUTHOR_EMAIL);
                long authorIdentifierLong = (long) comment.getProperty(Comment.PROPERTY_AUTHOR_IDENTIFIER);
                String authorName = (String) comment.getProperty(Comment.PROPERTY_AUTHOR_NAME);
                int authorIdentifier = new BigDecimal(authorIdentifierLong).intValueExact();
                long registerTime = (long) comment.getProperty(Comment.PROPERTY_TIME);
                Comment newComment = new Comment(content, authorEmail, authorName, authorIdentifier, registerTime);
                listOfComments.add(newComment);
            }

            JsonParser parser = new JsonParser();
            JsonArray object = (JsonArray) parser.parse(g.toJson(listOfComments));

            JsonObject objectOutput = new JsonObject();
            objectOutput.addProperty("cursor", cursor);
            objectOutput.add("comments", object);

            return Response.ok(g.toJson(objectOutput)).build();
        } catch (EntityNotFoundException e) {
            LOG.warning(Utils.OUTPUT_MISSING_REPORT);
            return Response.status(Status.FORBIDDEN).entity(g.toJson(new StringUtil(Utils.OUTPUT_MISSING_REPORT))).build();
        }
    }

    @POST
    @Path("/suggestions")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getSuggestionList(String jsonString) {

        JsonElement jelement = new JsonParser().parse(jsonString);
        JsonObject jobject = jelement.getAsJsonObject();

        JsonObject jtoken = jobject.getAsJsonObject("token");
        JsonElement jtype = jobject.get(Report.PROPERTY_TYPE);
        JsonElement jlat = jobject.get(Report.PROPERTY_LATITUDE);
        JsonElement jlon = jobject.get(Report.PROPERTY_LONGITUDE);

        AuthToken token = g.fromJson(jtoken.toString(), AuthToken.class);

        String providedType = g.fromJson(jtype.toString(), String.class);

        double lat = g.fromJson(jlat, double.class);
        double lon = g.fromJson(jlon, double.class);

        /* Security Validations */
        Entity user = Utils.getUser(token.user, datastore, LOG);
        if (user == null) {
            LOG.warning(Utils.OUTPUT_MISSING_USER);
            return Response.status(Status.UNAUTHORIZED).entity(g.toJson(new StringUtil(Utils.OUTPUT_MISSING_USER))).build();
        }
        Response rsp = Utils.securityValidations(user, token, ResourcesEnum.FRONTOFFICE_GET_SUGGESTION_LIST_REPORT, datastore,
                LOG);
        if (rsp.getStatus() != Status.ACCEPTED.getStatusCode())
            return rsp;
        /* Security Validations */

        // Sets range of search
        final double radium = 0.002;

        // Sets a filter by provided type
        Filter filterByType = new FilterPredicate(Report.PROPERTY_TYPE, FilterOperator.EQUAL, providedType);

        // Query by provided type
        Query query = new Query(Report.KIND).setFilter(filterByType);
        List<Report> suggestionList = new ArrayList<>();

        Iterator<Entity> results = datastore.prepare(query).asIterator();
        if (!results.hasNext()) {
            // If no suggestions exist in database, it returns HTTP 204 code
            LOG.warning(Utils.OUTPUT_MISSING_LIST_REPORTS);
            return Response.status(Status.NO_CONTENT).build();
        }

        // While exists reports, append them to the list of reports
        while (results.hasNext()) {
            Entity report = results.next();
            double latitude = (double) report.getProperty(Report.PROPERTY_LATITUDE);
            double longitude = (double) report.getProperty(Report.PROPERTY_LONGITUDE);
            if (latitude <= lat + radium && latitude >= lat - radium && longitude <= lon + radium
                    && longitude >= lon - radium) {

                String id = String.valueOf(report.getKey().getId());
                Filter filterByUser = new FilterPredicate(Follower.PROPERTY_EMAIL, FilterOperator.EQUAL, token.user);
                Filter filterByReport = new FilterPredicate(Follower.PROPERTY_REPORT, FilterOperator.EQUAL, id);
                Query query2 = new Query(Follower.KIND).setFilter(
                        Query.CompositeFilterOperator.and(filterByUser, filterByReport));
                Entity result = datastore.prepare(query2).asSingleEntity();
                //   txn.rollback();
                boolean isFollowing = false;

                if (result == null)
                    isFollowing = false;
                else
                    isFollowing = true;

                Report newReport = Utils.buildReportForOutput(report, isFollowing);
                suggestionList.add(newReport);
            }
        }
        if (suggestionList.isEmpty()) {
            //********************************************************
            LOG.warning("Request reported no entities were found in the close range.");
            return Response.status(Status.NO_CONTENT).build();
        }

        // Returns the suggestion list (based on coordinates and type)
        //  Report[] arr = new Report[suggestionList.size()];
        //  suggestionList.toArray(arr);
        //***************************************
        //LOG.fine("At least one suggestion was found in database");
        return Response.ok(g.toJson(suggestionList)).build();
    }


    // TODO: deveria ser put pois ja se sabe o id


    @POST
    @Path("/follow/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response followSpecifiedReport(@PathParam("id") String reportId, String jsonString) {

        JsonElement jelement = new JsonParser().parse(jsonString);
        JsonObject jobject = jelement.getAsJsonObject();

        JsonObject jtoken = jobject.getAsJsonObject("token");
        JsonElement jpriority = jobject.get(Report.PROPERTY_PRIORITY);

        AuthToken token = g.fromJson(jtoken.toString(), AuthToken.class);
        int priority = g.fromJson(jpriority, Integer.class);

         /* Security Validations */
        Entity user = Utils.getUser(token.user, datastore, LOG);
        if (user == null) {
            LOG.warning(Utils.OUTPUT_MISSING_USER);
            return Response.status(Status.UNAUTHORIZED).entity(g.toJson(new StringUtil(Utils.OUTPUT_MISSING_USER))).build();
        }
        Response rsp = Utils.securityValidations(user, token, ResourcesEnum.FRONTOFFICE_FOLLOW_SPECIFIED_REPORT, datastore,
                LOG);
        if (rsp.getStatus() != Status.ACCEPTED.getStatusCode())
            return rsp;
        /* Security Validations */


        // Checks if this user is already following this report
        Filter filterByEmail = new FilterPredicate(Follower.PROPERTY_EMAIL, FilterOperator.EQUAL, token.user);
        Filter filterByReport = new FilterPredicate(Follower.PROPERTY_REPORT, FilterOperator.EQUAL, reportId);
        Query query = new Query(Follower.KIND).setFilter(
                CompositeFilterOperator.and(filterByEmail, filterByReport));

        Iterator<Entity> results = datastore.prepare(query).asIterator();
        if (results.hasNext()) {
            // If no suggestions exist in database, it returns HTTP 204 code
            LOG.warning(Utils.OUTPUT_REPORT_ALREADY_FOLLOWED);
            return Response.status(Status.CONFLICT).entity(g.toJson(new StringUtil(Utils.OUTPUT_REPORT_ALREADY_FOLLOWED))).build();
        }
        // Creates key to report passed in url
        Key reportKey = KeyFactory.createKey(Report.KIND, Long.parseLong(reportId));

        TransactionOptions options = TransactionOptions.Builder.withXG(true);
        Transaction txn = datastore.beginTransaction(options);
        try {

            // EntityNotFoundException will be thrown if the report does not
            // exist
            Entity reportEntity = datastore.get(reportKey);

            long currentTime = System.currentTimeMillis();
            // Creates a new subscriber to this report
            Entity follower = new Entity(Follower.KIND);
            follower.setIndexedProperty(Follower.PROPERTY_EMAIL, token.user);
            follower.setIndexedProperty(Follower.PROPERTY_REPORT, reportId);
            follower.setProperty(Follower.PROPERTY_PRIORITY, priority);
            follower.setProperty(Follower.PROPERTY_FOLLOW_TIME, currentTime);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            sdf.setTimeZone(TimeZone.getTimeZone("Europe/Lisbon"));
            String creationDate = sdf.format(currentTime);
            follower.setProperty(Follower.PROPERTY_FOLLOW_DATE, creationDate);

            long nrOfFollows = (long) reportEntity.getProperty(Report.PROPERTY_FOLLOWERS);
            long totalSumBefore = (long) reportEntity.getProperty(Report.PROPERTY_SUM_PRIORITY);

            int newPriority = Utils.calcAveragePriority((int) totalSumBefore, (int) nrOfFollows, priority, true);
            long points = (long) reportEntity.getProperty(Report.PROPERTY_POINTS);
            points += priority * (2);

            reportEntity.setIndexedProperty(Report.PROPERTY_FOLLOWERS, nrOfFollows + 1);
            reportEntity.setIndexedProperty(Report.PROPERTY_SUM_PRIORITY, totalSumBefore + priority);
            reportEntity.setIndexedProperty(Report.PROPERTY_PRIORITY, newPriority);
            reportEntity.setIndexedProperty(Report.PROPERTY_POINTS, (int) points);
            // TODO:
            datastore.put(txn, reportEntity);
            datastore.put(txn, follower);
            txn.commit();

            LOG.info("Everything went fine. New subscriber added.");
            return Response.ok(g.toJson(new StringUtil(Utils.OUTPUT_ADDED_SUBSCRIBER))).build();
        } catch (EntityNotFoundException e) {
            txn.rollback();
            LOG.warning(Utils.OUTPUT_MISSING_REPORT);
            return Response.status(Status.FORBIDDEN).entity(g.toJson(new StringUtil(Utils.OUTPUT_MISSING_REPORT))).build();
        } finally {
            // Checks if something went wrong and the transaction is still
            // active.
            if (txn.isActive()) {
                txn.rollback();
                LOG.severe(Utils.OUTPUT_TRANSACTION_FAILED);
                return Response.status(Status.INTERNAL_SERVER_ERROR).entity(g.toJson(new StringUtil(Utils.OUTPUT_TRANSACTION_FAILED))).build();
            }
        }
    }

    @POST
    @Path("/unfollow/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response unfollowSpecifiedReport(@PathParam("id") String reportId, AuthToken token) {

           /* Security Validations */
        Entity user = Utils.getUser(token.user, datastore, LOG);
        if (user == null) {
            LOG.warning(Utils.OUTPUT_MISSING_USER);
            return Response.status(Status.UNAUTHORIZED).entity(g.toJson(new StringUtil(Utils.OUTPUT_MISSING_USER))).build();
        }
        Response rsp = Utils.securityValidations(user, token, ResourcesEnum.FRONTOFFICE_UNFOLLOW_SPECIFIED_REPORT, datastore,
                LOG);
        if (rsp.getStatus() != Status.ACCEPTED.getStatusCode())
            return rsp;
        /* Security Validations */

        // Begins a new transaction
        TransactionOptions options = TransactionOptions.Builder.withXG(true);
        Transaction txn = datastore.beginTransaction(options);

        Key reportKey = KeyFactory.createKey(Report.KIND, Long.parseLong(reportId));
        try {

            Entity report = datastore.get(reportKey);

            Filter filter = CompositeFilterOperator.and(
                    new FilterPredicate(Follower.PROPERTY_EMAIL, FilterOperator.EQUAL, token.user),
                    new FilterPredicate(Follower.PROPERTY_REPORT, FilterOperator.EQUAL, reportId));

            Query query = new Query(Follower.KIND).setFilter(filter);
            Entity entity = datastore.prepare(query).asSingleEntity();

            if (entity == null) {
                txn.rollback();
                LOG.warning("Error: user is not following this report.");
                return Response.status(Status.FORBIDDEN).entity(g.toJson(new StringUtil(Utils.OUTPUT_NOT_FOLLOWING_REPORT))).build();

            }
            datastore.delete(txn, entity.getKey());

            // Decrement number of followers
            long numOfFollows = (long) report.getProperty(Report.PROPERTY_FOLLOWERS);
            long totalSumBefore = (long) report.getProperty(Report.PROPERTY_SUM_PRIORITY);
            long priority = (long) entity.getProperty(Follower.PROPERTY_PRIORITY);
            long points = (long) report.getProperty(Report.PROPERTY_POINTS);

            int newPriority = Utils.calcAveragePriority((int) totalSumBefore, (int) numOfFollows, (int) priority, false);

            // report.setIndexedProperty(Report.PROPERTY_FOLLOWERS, numOfFollows - 1);
            report.setIndexedProperty(Report.PROPERTY_SUM_PRIORITY, totalSumBefore - priority);
            report.setIndexedProperty(Report.PROPERTY_PRIORITY, newPriority);

            points -= priority * 2;

            report.setIndexedProperty(Report.PROPERTY_POINTS, (int) points);

            numOfFollows -= 1;
            if (numOfFollows > 0) {
                report.setIndexedProperty(Report.PROPERTY_FOLLOWERS, numOfFollows);
                datastore.put(txn, report);
            } else if (numOfFollows == 0) {
                //datastore.delete(reportKey);
                report.setIndexedProperty(Report.PROPERTY_FOLLOWERS, numOfFollows);
                datastore.put(txn, report);
            } else {

            }
            txn.commit();
            return Response.ok(g.toJson(new StringUtil(Utils.OUTPUT_STOPPED_FOLLOWING))).build();

        } catch (EntityNotFoundException e) {
            txn.rollback();
            LOG.warning("No report with provided id was found in database.");
            return Response.status(Status.FORBIDDEN).entity(g.toJson(new StringUtil(Utils.OUTPUT_MISSING_REPORT))).build();

        } finally {
            // Checks if something went wrong and the transaction is still
            // active.
            if (txn.isActive()) {
                txn.rollback();
                LOG.severe(Utils.OUTPUT_TRANSACTION_FAILED);
                return Response.status(Status.INTERNAL_SERVER_ERROR).entity(g.toJson(new StringUtil(Utils.OUTPUT_TRANSACTION_FAILED))).build();
            }
        }
    }

    @POST
    @Path("/reportById/{id}")
    public Response getReportById(@PathParam("id") String id, AuthToken token) {

        /* Security Validations */
        Entity user = Utils.getUser(token.user, datastore, LOG);
        if (user == null) {
            LOG.warning(Utils.OUTPUT_MISSING_USER);
            return Response.status(Status.UNAUTHORIZED).entity(new StringUtil(Utils.OUTPUT_MISSING_USER)).build();
        }
        Response rsp = Utils.securityValidations(user, token, ResourcesEnum.GENERAL_GET_REPORT_DETAILS, datastore, LOG);
        if (rsp.getStatus() != Status.ACCEPTED.getStatusCode())
            return rsp;
        /* Security Validations */


        Key key = KeyFactory.createKey(Report.KIND, Long.parseLong(id));
        try {
            Entity entity = datastore.get(key);

            String idExtra = String.valueOf(entity.getKey().getId());
            Filter filterByUser = new FilterPredicate(Follower.PROPERTY_EMAIL, FilterOperator.EQUAL, token.user);
            Filter filterByReport = new FilterPredicate(Follower.PROPERTY_REPORT, FilterOperator.EQUAL, idExtra);
            Query query = new Query(Follower.KIND).setFilter(
                    Query.CompositeFilterOperator.and(filterByUser, filterByReport));
            Entity result = datastore.prepare(query).asSingleEntity();
            // txn.rollback();
            boolean isFollowing = false;

            if (result == null)
                isFollowing = false;
            else
                isFollowing = true;

            Report newReport = Utils.buildReportForOutput(entity, isFollowing);
            return Response.ok(g.toJson(newReport)).build();
        } catch (EntityNotFoundException e) {
            LOG.info(Utils.OUTPUT_MISSING_REPORT);
            return Response.status(Status.NOT_FOUND).entity(
                    g.toJson(new StringUtil(Utils.OUTPUT_MISSING_REPORT))).build();
        }
    }

}

