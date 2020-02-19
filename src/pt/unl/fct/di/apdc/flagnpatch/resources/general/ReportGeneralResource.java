package pt.unl.fct.di.apdc.flagnpatch.resources.general;

import com.google.api.client.json.Json;
import com.google.appengine.api.datastore.*;
import com.google.gson.*;
import pt.unl.fct.di.apdc.flagnpatch.entities.Areas;
import pt.unl.fct.di.apdc.flagnpatch.entities.AuthToken;
import pt.unl.fct.di.apdc.flagnpatch.entities.Follower;
import pt.unl.fct.di.apdc.flagnpatch.entities.Report;
import pt.unl.fct.di.apdc.flagnpatch.inputData.ReportData;
import pt.unl.fct.di.apdc.flagnpatch.resources.frontOffice.ReportResource;
import pt.unl.fct.di.apdc.flagnpatch.utils.ResourcesEnum;
import pt.unl.fct.di.apdc.flagnpatch.utils.StringUtil;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;

@Path("/reports")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ReportGeneralResource {

    // Logger object
    private static final Logger LOG = Logger.getLogger(ReportResource.class.getName());
    // Datastore link
    private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    // Converts objects to JSON
    private final Gson g = new Gson();

    public ReportGeneralResource() {
    }

    @POST
    @Path("/getAll")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllReports(AuthToken token) {

        // TODO:confirmar que funciona
         /* Security Validations */
        Entity user = Utils.getUser(token.user, datastore, LOG);
        if (user == null) {
            LOG.warning(Utils.OUTPUT_MISSING_USER);
            return Response.status(Status.UNAUTHORIZED).entity(g.toJson(new StringUtil(Utils.OUTPUT_MISSING_USER))).build();
        }
        Response rsp = Utils.securityValidations(user, token, ResourcesEnum.GENERAL_GET_ALL_REPORTS, datastore,
                LOG);
        if (rsp.getStatus() != Status.ACCEPTED.getStatusCode())
            return rsp;
        /* Security Validations */

      /*  ArrayList<String> areasAvailable = new ArrayList<>();
        Filter filterByAreaAvailable = new FilterPredicate(Areas.PROPERTY_IS_AVAILABLE, FilterOperator.EQUAL, true);
        Query queryForAreas = new Query(Areas.KIND).setFilter(filterByAreaAvailable);
        Iterator<Entity> resultsOfArea = datastore.prepare(queryForAreas).asIterator();

        if (!resultsOfArea.hasNext()) {
            LOG.warning(" no reports for active areas");
            return Response.status(Status.NO_CONTENT).build();
        }
        while (resultsOfArea.hasNext()) {
            Entity entity = resultsOfArea.next();
            areasAvailable.add((String) entity.getProperty(Areas.PROPERTY_COUNTY));
        }
*/
        List<Report> allReports = new ArrayList<>();
        //Filter filterByCounty = new FilterPredicate("address.county", FilterOperator.IN, areasAvailable);

        Query queryForReports = new Query(Report.KIND);
        Iterator<Entity> results = datastore.prepare(queryForReports).asIterator();
        if (!results.hasNext()) {
            /* If no suggestions exist in database, it returns HTTP 204 code */
            LOG.info(Utils.OUTPUT_MISSING_LIST_REPORTS);
            return Response.status(Status.NO_CONTENT).build();
        }
        /* While exists reports, append them to the list of reports */
        while (results.hasNext()) {
            Entity report = results.next();
            // Transaction txn = datastore.beginTransaction();
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
            allReports.add(newReport);
        }
        return Response.ok(g.toJson(allReports)).build();
    }

    @POST
    @Path("/getA")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getA(String jsonString) {
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
        Response rsp = Utils.securityValidations(user, token, ResourcesEnum.GENERAL_GET_ALL_REPORTS, datastore,
                LOG);
        if (rsp.getStatus() != Status.ACCEPTED.getStatusCode())
            return rsp;
        /* Security Validations */


      /*  ArrayList<String> areasAvailable = new ArrayList<>();
        Filter filterByAreaAvailable = new FilterPredicate(Areas.PROPERTY_IS_AVAILABLE, FilterOperator.EQUAL, true);
        Query queryForAreas = new Query(Areas.KIND).setFilter(filterByAreaAvailable);
        Iterator<Entity> resultsOfArea = datastore.prepare(queryForAreas).asIterator();

        if (!resultsOfArea.hasNext()) {
            LOG.warning(" no reports for active areas");
            return Response.status(Status.NO_CONTENT).build();
        }
        while (resultsOfArea.hasNext()) {
            Entity entity = resultsOfArea.next();
            areasAvailable.add((String) entity.getProperty(Areas.PROPERTY_COUNTY));
        }
*/
        List<Report> allReports = new ArrayList<>();
       // Filter filterByCounty = new FilterPredicate("address.county", FilterOperator.IN, null);
        Query queryForReports = new Query(Report.KIND);
        PreparedQuery preparedQuery = datastore.prepare(queryForReports);

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
            /* If no suggestions exist in database, it returns HTTP 204 code */
            LOG.info(Utils.OUTPUT_MISSING_LIST_REPORTS);
            return Response.status(Status.NO_CONTENT).build();
        }

        for (Entity report : resultsList) {
            //Transaction txn = datastore.beginTransaction();
            String id = String.valueOf(report.getKey().getId());
            Filter filterByUser = new FilterPredicate(Follower.PROPERTY_EMAIL, FilterOperator.EQUAL, token.user);
            Filter filterByReport = new FilterPredicate(Follower.PROPERTY_REPORT, FilterOperator.EQUAL, id);
            Query query = new Query(Follower.KIND).setFilter(
                    Query.CompositeFilterOperator.and(filterByUser, filterByReport));
            Entity result = datastore.prepare(query).asSingleEntity();
            //   txn.rollback();
            boolean isFollowing = false;

            if (result == null)
                isFollowing = false;
            else
                isFollowing = true;

            Report newReport = Utils.buildReportForOutput(report, isFollowing);
            allReports.add(newReport);
        }
        JsonParser parser = new JsonParser();
        JsonArray object = (JsonArray) parser.parse(g.toJson(allReports));

        JsonObject objectOutput = new JsonObject();
        objectOutput.addProperty("cursor", cursor);
        objectOutput.add("reports", object);

        return Response.ok(g.toJson(objectOutput)).build();
    }

    @POST
    @Path("/getAreas")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAreas(AuthToken token) {
         /* Security Validations */
        Entity user = Utils.getUser(token.user, datastore, LOG);
        if (user == null) {
            LOG.warning(Utils.OUTPUT_MISSING_USER);
            return Response.status(Status.UNAUTHORIZED).entity(g.toJson(new StringUtil(Utils.OUTPUT_MISSING_USER))).build();
        }

        Filter filterByTrue = new FilterPredicate(Areas.PROPERTY_IS_AVAILABLE, FilterOperator.EQUAL, true);

        Query queryForAreas = new Query(Areas.KIND).setFilter(filterByTrue);
        Iterator<Entity> results = datastore.prepare(queryForAreas).asIterator();
        if (!results.hasNext()) {
            /* If no suggestions exist in database, it returns HTTP 204 code */
            LOG.info("No areas available");
            return Response.status(Status.NO_CONTENT).build();
        }
        ArrayList<String> availableAreas = new ArrayList<>();
        /* While exists reports, append them to the list of reports */
        while (results.hasNext()) {
            availableAreas.add((String) results.next().getProperty(Areas.PROPERTY_COUNTY));
        }
        JsonObject objectOutput = new JsonObject();
        JsonParser parser = new JsonParser();
        JsonArray object = (JsonArray) parser.parse(g.toJson(availableAreas));
        objectOutput.add("counties", object);

        return Response.ok(g.toJson(objectOutput)).build();
    }


}
