package pt.unl.fct.di.apdc.flagnpatch.resources.administration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
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
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.users.User;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import pt.unl.fct.di.apdc.flagnpatch.entities.*;
import pt.unl.fct.di.apdc.flagnpatch.inputData.AddressData;
import pt.unl.fct.di.apdc.flagnpatch.inputData.ReportTypeData;
import pt.unl.fct.di.apdc.flagnpatch.resources.frontOffice.ReportResource;
import pt.unl.fct.di.apdc.flagnpatch.resources.general.Utils;
import pt.unl.fct.di.apdc.flagnpatch.utils.*;

@Path("/admin/reportTypeManagement")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class TypeManagementResources {

    private static final Logger LOG = Logger.getLogger(ReportResource.class.getName());
    private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    private final Gson g = new Gson();
    private final Queue queue = QueueFactory.getDefaultQueue();

    public TypeManagementResources() {

    }

    @POST
    @Path("/addReportType")
    public Response addReportType(String jsonString) {
        JsonElement jelement = new JsonParser().parse(jsonString);
        JsonObject jobject = jelement.getAsJsonObject();

        JsonObject jtoken = jobject.getAsJsonObject("token");
        JsonObject jtype = jobject.getAsJsonObject("data");

        AuthToken token = g.fromJson(jtoken.toString(), AuthToken.class);
        ReportTypeData data = g.fromJson(jtype.toString(), ReportTypeData.class);

        /* Security Validations */
        Entity user = Utils.getUser(token.user, datastore, LOG);
        if (user == null) {
            LOG.warning(Utils.OUTPUT_MISSING_USER);
            return Response.status(Status.UNAUTHORIZED).entity(g.toJson(new StringUtil(Utils.OUTPUT_MISSING_USER))).build();
        }
        Response rsp = Utils.securityValidations(user, token, ResourcesEnum.ADMINISTRATION_ADD_REPORT_TYPE, datastore,
                LOG);
        if (rsp.getStatus() != Status.ACCEPTED.getStatusCode())
            return rsp;
        /* Security Validations */

        // Verifies input data from the user to create a new Report type
        if (!data.validTypeData()) {
            return Response.status(Status.BAD_REQUEST).entity(g.toJson("Missing or wrong field.")).build();
        }
        TransactionOptions options = TransactionOptions.Builder.withXG(true);
        Transaction txn = datastore.beginTransaction(options);

        EmbeddedEntity address = (EmbeddedEntity) user.getProperty(User_Global.PROPERTY_ADDR);
        String county = (String) address.getProperty(AddressData.PROPERTY_COUNTY);

        Filter filterByCounty = new FilterPredicate(ReportType.PROPERTY_COUNTY, FilterOperator.EQUAL, county);
        Filter filterByType = new FilterPredicate(ReportType.PROPERTY_NAME, FilterOperator.EQUAL, data.name);

        Query query = new Query(ReportType.KIND).setFilter(CompositeFilterOperator.and(filterByCounty, filterByType));
        Entity result = datastore.prepare(query).asSingleEntity();
        // txn.rollback();
        if (result != null) {

            txn.rollback();
            LOG.warning("Report type already exists.");

            // Conflict - entity with this name in this location already exists
            return Response.status(Status.CONFLICT).entity(g.toJson("Report type already exists.")).build();
        }

        // Entity with provided name was not found, there is no conflict
        //txn = datastore.beginTransaction();

        String name = (String) data.name;
        String responsible = (String) data.responsible;
        String creatorId = (String) token.user;

        Entity entity = new Entity(ReportType.KIND);
        entity.setIndexedProperty(ReportType.PROPERTY_NAME, name);
        entity.setIndexedProperty(ReportType.PROPERTY_CREATOR, creatorId);
        entity.setIndexedProperty(ReportType.PROPERTY_COUNTY, county);
        entity.setIndexedProperty(ReportType.PROPERTY_ACTIVE, true);

        if (data.hasResponsible()) {
            // Checks if the responsible has the role "worker" and if it's
            // active
            if (Utils.validResponsible(responsible, county, datastore)) {
                entity.setIndexedProperty(ReportType.PROPERTY_RESPONSIBLE, responsible);
            } else {
                return Response.status(Status.UNAUTHORIZED).entity(g.toJson("Invalid worker.")).build();
            }
        } else {
            entity.setIndexedProperty(ReportType.PROPERTY_RESPONSIBLE, null);
        }

        datastore.put(txn, entity);

        // txn.commit();

        //TODO :NOVO
        // txn = datastore.beginTransaction();

        filterByCounty = new Query.FilterPredicate(Graph.PROPERTY_COUNTY, Query.FilterOperator.EQUAL, county);
        Query.Filter filterByActive = new Query.FilterPredicate(Graph.PROPERTY_ACTIVE, Query.FilterOperator.EQUAL, true);
        query = new Query(Graph.KIND).setFilter(
                Query.CompositeFilterOperator.and(filterByActive, filterByCounty));

        Entity entityOfGraph = datastore.prepare(query).asSingleEntity();

        entityOfGraph.setIndexedProperty(name, 0);

        datastore.put(txn, entityOfGraph);

        txn.commit();
        String id = String.valueOf(entity.getKey().getId());
        return Response.ok(g.toJson(new ReportType(id, name, responsible, creatorId, county, true))).build();
    }


    @POST
    @Path("/changeReportTypeStatus/{id}")
    public Response changeReportTypeStatus(@PathParam("id") String id, AuthToken token) {
        /* Security Validations */
        Entity user = Utils.getUser(token.user, datastore, LOG);
        if (user == null) {
            LOG.warning(Utils.OUTPUT_MISSING_USER);
            return Response.status(Status.UNAUTHORIZED).entity(new StringUtil(Utils.OUTPUT_MISSING_USER)).build();
        }
        Response rsp = Utils.securityValidations(user, token, ResourcesEnum.ADMINISTRATION_CHANGE_TYPE_STATUS, datastore,
                LOG);
        if (rsp.getStatus() != Status.ACCEPTED.getStatusCode())
            return rsp;
        /* Security Validations */

        TransactionOptions options = TransactionOptions.Builder.withXG(true);
        Transaction txn = datastore.beginTransaction(options);
        Key key = KeyFactory.createKey(ReportType.KIND, Long.parseLong(id));
        try {
            Entity reportType = datastore.get(key);

            // Verificar se o tipo de ocorrencia esta registado na mesma zona que o utilizador
            EmbeddedEntity addrEntity = (EmbeddedEntity) user.getProperty(User_Global.PROPERTY_ADDR);
            String county = (String) addrEntity.getProperty(AddressData.PROPERTY_COUNTY);

            if (!county.equalsIgnoreCase((String) reportType.getProperty(ReportType.PROPERTY_COUNTY))) {
                LOG.info("Report type location is not the same as user.");
                return Response.status(Status.FORBIDDEN).entity(
                        g.toJson(new StringUtil("Report type location is not the same as user."))).build();
            }

            boolean isActive = (boolean) reportType.getProperty(ReportType.PROPERTY_ACTIVE);
            if (isActive) {
                reportType.setProperty(ReportType.PROPERTY_ACTIVE, false);
            } else reportType.setProperty(ReportType.PROPERTY_ACTIVE, true);

            datastore.put(txn, reportType);
            txn.commit();
            return Response.ok(g.toJson(!isActive)).build();

        } catch (EntityNotFoundException e) {
            LOG.info(Utils.OUTPUT_MISSING_REPORT);
            return Response.status(Status.NOT_FOUND).entity(
                    g.toJson(new StringUtil(Utils.OUTPUT_MISSING_REPORT))).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }


    }


    @POST
    @Path("/listReportTypes")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response listReportTypes(String jsonString) {
        // Method accessible to all users
        JsonElement jelement = new JsonParser().parse(jsonString);
        JsonObject jobject = jelement.getAsJsonObject();
        boolean hasCounty = jobject.has("county");
        String county = null;
        if (hasCounty) {

            JsonElement jcounty = jobject.getAsJsonPrimitive("county");
            county = g.fromJson(jcounty.toString(), String.class);
        }
        JsonObject jtoken = jobject.getAsJsonObject("token");
        AuthToken token = g.fromJson(jtoken.toString(), AuthToken.class);

        /* Security Validations */
        Entity user = Utils.getUser(token.user, datastore, LOG);
        if (user == null) {
            LOG.warning(Utils.OUTPUT_MISSING_USER);
            return Response.status(Status.UNAUTHORIZED).entity(g.toJson(new StringUtil(Utils.OUTPUT_MISSING_USER))).build();
        }
        Response rsp = Utils.securityValidations(user, token, ResourcesEnum.GENERAL_LIST_REPORT_TYPES, datastore, LOG);
        if (rsp.getStatus() != Status.ACCEPTED.getStatusCode())
            return rsp;
        Filter filter = null;
        List<ReportType> typeList = new ArrayList<ReportType>();

        if (hasCounty) {
            filter = new FilterPredicate(ReportType.PROPERTY_COUNTY, FilterOperator.EQUAL, county);
        } else {
            EmbeddedEntity address = (EmbeddedEntity) user.getProperty(User_Global.PROPERTY_ADDR);
            county = (String) address.getProperty(AddressData.PROPERTY_COUNTY);
            filter = new FilterPredicate(ReportType.PROPERTY_COUNTY, FilterOperator.EQUAL, county);
        }

        Query query = new Query(ReportType.KIND).setFilter(filter);
        Iterator<Entity> results = datastore.prepare(query).asIterator();

        if (!results.hasNext()) {
            // If does not exist any report from user, it returns HTTP 204 code
            return Response.status(Status.NO_CONTENT).build();
        }

        while (results.hasNext()) {
            Entity type = results.next();
            ReportType newType = new ReportType((String) String.valueOf(type.getKey().getId()),
                    (String) type.getProperty(ReportType.PROPERTY_NAME),
                    (String) type.getProperty(ReportType.PROPERTY_RESPONSIBLE),
                    (String) type.getProperty(ReportType.PROPERTY_CREATOR),
                    (String) type.getProperty(ReportType.PROPERTY_COUNTY),
                    (boolean) type.getProperty(ReportType.PROPERTY_ACTIVE));
            typeList.add(newType);
        }
        // ReportType[] arr = new ReportType[typeList.size()];
        //  typeList.toArray(arr);
        return Response.ok(g.toJson(typeList)).build();

    }

//    @POST
//    @Path("/changeResponsible/{id}")
//    public Response changeResponsible(@PathParam("id") String id, String jsonString) {
//
//        System.out.println(jsonString);
//        JsonElement jelement = new JsonParser().parse(jsonString);
//        JsonObject jobject = jelement.getAsJsonObject();
//
//        JsonObject jtoken = jobject.getAsJsonObject("token");
//        JsonElement jworker = jobject.get("email");
//
//        AuthToken token = g.fromJson(jtoken.toString(), AuthToken.class);
//        //String worker = g.fromJson(jworker.toString(), String.class);
//
//        String worker;
//        if (jworker == null) {
//            worker = null;
//            System.out.println("worker is null");
//        } else worker = g.fromJson(jworker.toString(), String.class);
//
//
//        Entity user = Utils.getUser(token.user, datastore, LOG);
//        if (user == null) {
//            LOG.warning(Utils.OUTPUT_MISSING_USER);
//            return Response.status(Status.UNAUTHORIZED).entity(new StringUtil(Utils.OUTPUT_MISSING_USER)).build();
//        }
//        Response rsp = Utils.securityValidations(user, token, ResourcesEnum.ADMINISTRATION_CHANGE_RESPONSIBLE, datastore, LOG);
//        if (rsp.getStatus() != Status.ACCEPTED.getStatusCode())
//            return rsp;
//
//        TransactionOptions options = TransactionOptions.Builder.withXG(true);
//        Transaction txn = datastore.beginTransaction(options);
//
//        try {
//            Key key = KeyFactory.createKey(ReportType.KIND, Long.parseLong(id));
//            Entity reportType = datastore.get(key);
//
//            // Verify if type belongs to this user
//            String typeCreator = (String) reportType.getProperty(ReportType.PROPERTY_CREATOR);
//            if (!typeCreator.equals(token.user)) {
//                LOG.warning("Type does not belong to this account.");
//                return Response.status(Status.BAD_REQUEST).entity(g.toJson("Type does not belong to this account."))
//                        .build();
//            }
//            // Check if it's a valid worker
//            if (worker != null) {
//                if (!validResponsible(worker)) {
//                    LOG.warning("Responsible is not valid.");
//                    return Response.status(Status.BAD_REQUEST).entity(g.toJson("Responsible is not valid.")).build();
//                }
//            }
//
//            // ?
//            String currentResp = (String) reportType.getProperty(ReportType.PROPERTY_RESPONSIBLE);
//            if (currentResp != null) {
//                if (currentResp.equals(worker)) {
//                    LOG.warning("Responsible already in charge.");
//                    return Response.status(Status.CONFLICT).entity(g.toJson("Responsible already in charge.")).build();
//                }
//                // Check if the if the previous responsible is solving (right
//                // now) some
//                // report of the given type
//                Filter filterByResp = new FilterPredicate(Report.PROPERTY_WORKER_RESPONSIBLE, FilterOperator.EQUAL,
//                        currentResp);
//                Filter filterByStatus = new FilterPredicate(Report.PROPERTY_STATUS_DESCRIPTION, FilterOperator.EQUAL,
//                        StatusEnum.IN_RESOLUTION.getStatusDescription());
//
//                String county = (String) reportType.getProperty(ReportType.PROPERTY_COUNTY);
//
//
//                // TODO: change this PROPRTY_ADDRESS_AS_STREET
//                //Filter filterByCounty = new FilterPredicate(Report.PROPERTY_ADDRESS_AS_STREET, FilterOperator.EQUAL, county);
//                Filter filterByCounty = new FilterPredicate("address.county", FilterOperator.EQUAL, county);
//                Query q = new Query(Report.KIND)
//                        .setFilter(CompositeFilterOperator.and(filterByResp, filterByStatus, filterByCounty));
//
//                if (datastore.prepare(q).asIterator().hasNext()) {
//                    LOG.warning("There is a report with this type being solved right now.");
//                    return Response.status(Status.UNAUTHORIZED)
//                            .entity(g.toJson("There is a report with this type being solved right now.")).build();
//                }
//            }
//
//            // Change the responsible
//            reportType.setProperty(ReportType.PROPERTY_RESPONSIBLE, worker);
//            datastore.put(txn, reportType);
//
//            // Search reports with the provided type and change the responsible
//            String name = (String) reportType.getProperty(ReportType.PROPERTY_NAME);
//            String county = (String) reportType.getProperty(ReportType.PROPERTY_COUNTY);
//            Filter filterByType = new FilterPredicate(Report.PROPERTY_TYPE, FilterOperator.EQUAL, name);
//            Filter filterByCounty = new FilterPredicate("address.county", FilterOperator.EQUAL, county);
//
//            Query query = new Query(Report.KIND).setFilter(
//            		CompositeFilterOperator.and(filterByType, filterByCounty));
//            Iterator <Entity> it = datastore.prepare(query).asIterator();
//            while(it.hasNext()) {
//            	Entity report = it.next();
//            	report.setIndexedProperty(Report.PROPERTY_WORKER_RESPONSIBLE, worker);
//            	datastore.put(txn, report);
//            }
//
//            txn.commit();
//
//            return Response.ok().build();
//
//        } catch (EntityNotFoundException e) {
//            LOG.warning("Report type not found.");
//            return Response.status(Status.NOT_FOUND).entity(g.toJson("Report type not found.")).build();
//        } finally {
//            if (txn.isActive()) {
//                txn.rollback();
//            }
//        }
//    }

    @POST
    @Path("/changeResponsible/{id}")
    public Response changeResponsible(@PathParam("id") String id, String jsonString) {

        JsonElement jelement = new JsonParser().parse(jsonString);
        JsonObject jobject = jelement.getAsJsonObject();

        JsonObject jtoken = jobject.getAsJsonObject("token");
        AuthToken token = g.fromJson(jtoken.toString(), AuthToken.class);

        boolean hasEmail = jobject.has("email");
        String worker = null;
        if (hasEmail) {
            JsonElement jworker = jobject.getAsJsonPrimitive("email");
            worker = g.fromJson(jworker.toString(), String.class);
        } else
            worker = null;

        /* Security Validations */
        Entity user = Utils.getUser(token.user, datastore, LOG);
        if (user == null) {
            LOG.warning(Utils.OUTPUT_MISSING_USER);
            return Response.status(Status.UNAUTHORIZED).entity(g.toJson(new StringUtil(Utils.OUTPUT_MISSING_USER))).build();
        }
        Response rsp = Utils.securityValidations(user, token, ResourcesEnum.ADMINISTRATION_CHANGE_RESPONSIBLE, datastore, LOG);
        if (rsp.getStatus() != Status.ACCEPTED.getStatusCode())
            return rsp;
        /* Security Validations */

        TransactionOptions options = TransactionOptions.Builder.withXG(true);
        Transaction txn = datastore.beginTransaction(options);

        // Check if it's a valid worker
        if (worker != null) {
            EmbeddedEntity embeddedEntity = (EmbeddedEntity) user.getProperty(User_Global.PROPERTY_ADDR);
            String county = (String) embeddedEntity.getProperty(AddressData.PROPERTY_COUNTY);
            if (!Utils.validResponsible(worker, county, datastore)) {
                txn.rollback();
                LOG.warning("Responsible is not valid.");
                return Response.status(Status.BAD_REQUEST).entity(g.toJson(new StringUtil("Responsible is not valid."))).build();
            }
        }

        Entity reportType = null;
        try {
            Key key = KeyFactory.createKey(ReportType.KIND, Long.parseLong(id));
            reportType = datastore.get(key);

            // Verify if type belongs to this user
            String typeCreator = (String) reportType.getProperty(ReportType.PROPERTY_CREATOR);
            String nameOfType = (String) reportType.getProperty(ReportType.PROPERTY_NAME);
            if (!typeCreator.equalsIgnoreCase(token.user)) {
                txn.rollback();
                LOG.warning("Type does not belong to this account.");
                return Response.status(Status.BAD_REQUEST).entity(g.toJson(new StringUtil("Type does not belong to this account.")))
                        .build();
            }

            String currentResp = (String) reportType.getProperty(ReportType.PROPERTY_RESPONSIBLE);
            if ((currentResp == null && worker == null)) {
                txn.rollback();
                LOG.warning("Responsible already in charge.");
                return Response.status(Status.CONFLICT).entity(g.toJson(new StringUtil("Responsible already in charge."))).build();
            }

            LOG.severe("qual worker " + worker);

            if (currentResp != null) {

                if (currentResp.equals(worker)) {
                    txn.rollback();
                    LOG.warning("Responsible already in charge.");
                    return Response.status(Status.CONFLICT).entity(g.toJson(new StringUtil("Responsible already in charge."))).build();
                }

                Filter filterByResp = new FilterPredicate(Report.PROPERTY_WORKER_RESPONSIBLE, FilterOperator.EQUAL,
                        currentResp);
                Filter filterByStatus = new FilterPredicate(Report.PROPERTY_STATUS_DESCRIPTION, FilterOperator.EQUAL,
                        StatusEnum.SUBMITED.getStatusDescription());
                String countyOfReportType = (String) reportType.getProperty(ReportType.PROPERTY_COUNTY);
                Filter filterByCounty = new FilterPredicate("address.county", FilterOperator.EQUAL, countyOfReportType);
                Filter filterByType = new FilterPredicate(Report.PROPERTY_TYPE, FilterOperator.EQUAL, nameOfType);

                Query q = new Query(Report.KIND)
                        .setFilter(CompositeFilterOperator.and(filterByResp, filterByStatus, filterByCounty, filterByType));
                Iterator<Entity> it = datastore.prepare(q).asIterator();

                while (it.hasNext()) {
                    Entity report = it.next();
                    report.setIndexedProperty(Report.PROPERTY_WORKER_RESPONSIBLE, worker);
                    datastore.put(txn, report);
                }

                reportType.setIndexedProperty(ReportType.PROPERTY_RESPONSIBLE, worker);
                datastore.put(txn, reportType);

                queue.add(TaskOptions.Builder.withUrl("/rest/admin/reportTypeManagement/sendEmailExtra")
                        .param("userEmail", currentResp)
                        .param("reportType", nameOfType)
                        .param("typeOfEmailHtml", EmailsEnum.WORKER_DEACTIVATE_HTML.toString())
                        .param("county", countyOfReportType)
                        .header("Content-Type", "application/html; charset=utf8"));


                if (worker != null) {
                    queue.add(TaskOptions.Builder.withUrl("/rest/admin/reportTypeManagement/sendEmailExtra")
                            .param("userEmail", worker)
                            .param("reportType", nameOfType)
                            .param("typeOfEmailHtml", EmailsEnum.WORKER_ACTIVATED_HTML.toString())
                            .param("county", countyOfReportType)
                            .header("Content-Type", "application/html; charset=utf8"));
                }
                txn.commit();

                return Response.ok().build();
            } else {

                String county = (String) reportType.getProperty(ReportType.PROPERTY_COUNTY);
                Filter filterByType = new FilterPredicate(Report.PROPERTY_TYPE, FilterOperator.EQUAL, nameOfType);
                Filter filterByStatus = new FilterPredicate(Report.PROPERTY_STATUS_DESCRIPTION, FilterOperator.EQUAL,
                        StatusEnum.SUBMITED.getStatusDescription());
                Filter filterByCounty = new FilterPredicate("address.county", FilterOperator.EQUAL, county);
                Query query = new Query(Report.KIND).setFilter(
                        CompositeFilterOperator.and(filterByType, filterByCounty, filterByStatus));
                Iterator<Entity> it = datastore.prepare(query).asIterator();

                while (it.hasNext()) {
                    Entity report = it.next();
                    report.setIndexedProperty(Report.PROPERTY_WORKER_RESPONSIBLE, worker);
                    datastore.put(txn, report);
                }
                reportType.setIndexedProperty(ReportType.PROPERTY_RESPONSIBLE, worker);
                datastore.put(txn, reportType);

                if (worker != null) {
                    queue.add(TaskOptions.Builder.withUrl("/rest/admin/reportTypeManagement/sendEmailExtra")
                            .param("userEmail", worker)
                            .param("reportType", nameOfType)
                            .param("typeOfEmailHtml", EmailsEnum.WORKER_ACTIVATED_HTML.toString())
                            .param("county", county)
                            .header("Content-Type", "application/html; charset=utf8"));
                }
                txn.commit();

                return Response.ok().build();
            }

        } catch (EntityNotFoundException e) {
            txn.rollback();
            LOG.warning("Report type not found.");
            return Response.status(Status.NOT_FOUND).entity(g.toJson(new StringUtil("Report type not found."))).build();
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
    @Path("/sendEmailExtra")
    public void callSendConfirmationEmailForOld(@Context HttpServletRequest httpServletRequest) {

        String userEmail = httpServletRequest.getParameter("userEmail");
        String reportType = httpServletRequest.getParameter("reportType");
        String typeOfEmailHtml = httpServletRequest.getParameter("typeOfEmailHtml");
        String county = httpServletRequest.getParameter("county");

        if (userEmail == null)
            return;
        else {
            Entity user = Utils.getUser(userEmail, datastore, LOG);
            if (user == null) {
                LOG.severe(Utils.OUTPUT_MISSING_USER);
                return;
            }

            String userName = (String) user.getProperty(User_Global.PROPERTY_NAME);
            EmailsEnum emailsEnum = EmailsEnum.valueOf(typeOfEmailHtml);
            // TODO: EMAIL NAO È ENVIADO
            LOG.warning("email iria ser enviado, mas esta desativado");
            Utils.sendEmail(userEmail, userName, Utils.CHANGE_OF_WORK_TYPE_SUBJECT,
                    new EmailUtil(userName, emailsEnum, county, reportType).getHtml());
        }
    }

    @POST
    @Path("/loadFilteredReports")
    public Response loadFilteredReports(String jsonString) {
        JsonElement jelement = new JsonParser().parse(jsonString);
        JsonObject jobject = jelement.getAsJsonObject();
        System.out.println(jsonString);

        JsonObject jtoken = jobject.getAsJsonObject("token");
        JsonElement jtype = jobject.get("type");
        JsonElement jemail = jobject.get("email");

        AuthToken token = g.fromJson(jtoken.toString(), AuthToken.class);
        String type = g.fromJson(jtype.toString(), String.class);
        String email = g.fromJson(jemail.toString(), String.class);

        Entity user = Utils.getUser(token.user, datastore, LOG);
        if (user == null) {
            LOG.warning(Utils.OUTPUT_MISSING_USER);
            return Response.status(Status.UNAUTHORIZED).entity(g.toJson(new StringUtil(Utils.OUTPUT_MISSING_USER))).build();
        }
        Response rsp = Utils.securityValidations(user, token, ResourcesEnum.ADMINISTRATION_LOAD_REPORTS_BY_RESP, datastore, LOG);
        if (rsp.getStatus() != Status.ACCEPTED.getStatusCode())
            return rsp;

        EmbeddedEntity addrEntity = (EmbeddedEntity) user.getProperty(User_Global.PROPERTY_ADDR);
        String county = (String) addrEntity.getProperty(AddressData.PROPERTY_COUNTY);

        //Filter filterByCounty = new FilterPredicate(Report.PROPERTY_ADDRESS_AS_STREET, FilterOperator.EQUAL, county);
        Filter filterByCounty = new FilterPredicate("address.county", FilterOperator.EQUAL, county);
        Filter filterByType = new FilterPredicate(Report.PROPERTY_TYPE, FilterOperator.EQUAL, type);
        Filter filterByWorker = new FilterPredicate(Report.PROPERTY_WORKER_RESPONSIBLE, FilterOperator.EQUAL, email);
        Query query = new Query(Report.KIND).setFilter(CompositeFilterOperator.and(filterByCounty, filterByType, filterByWorker));

        List<Report> reportList = new ArrayList<Report>();

        Iterator<Entity> it = datastore.prepare(query).asIterator();
        if (!it.hasNext()) {
            return Response.status(Status.NO_CONTENT).entity(g.toJson(new StringUtil("No reports found."))).build();
        }
        while (it.hasNext()) {
            Entity reportEntity = it.next();
            Report report = Utils.buildReport(reportEntity);
            reportList.add(report);

        }

        // Report[] arr = new Report[reportList.size()];
        // reportList.toArray(arr);
        return Response.ok(g.toJson(reportList)).build();
    }
}