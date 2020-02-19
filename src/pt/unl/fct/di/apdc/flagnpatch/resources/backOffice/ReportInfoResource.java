package pt.unl.fct.di.apdc.flagnpatch.resources.backOffice;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import pt.unl.fct.di.apdc.flagnpatch.entities.AuthToken;
import pt.unl.fct.di.apdc.flagnpatch.entities.Report;
import pt.unl.fct.di.apdc.flagnpatch.entities.User_Global;
import pt.unl.fct.di.apdc.flagnpatch.inputData.AddressData;
import pt.unl.fct.di.apdc.flagnpatch.inputData.ReportFilterData;
import pt.unl.fct.di.apdc.flagnpatch.resources.frontOffice.ReportResource;
import pt.unl.fct.di.apdc.flagnpatch.resources.general.Utils;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

@Path("/backoffice/report/info")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ReportInfoResource {

    // Logger object
    private static final Logger LOG = Logger.getLogger(ReportResource.class.getName());
    // Datastore link
    private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    // Converts objects to JSON
    private final Gson g = new Gson();

    public ReportInfoResource() {

    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getFilteredReports(String jsonString) {
        JsonElement jelement = new JsonParser().parse(jsonString);
        JsonObject jobject = jelement.getAsJsonObject();

        JsonObject jtoken = jobject.getAsJsonObject("token");
        AuthToken token = g.fromJson(jtoken.toString(), AuthToken.class);

        JsonElement jfilters = jobject.getAsJsonObject("filters");
        ReportFilterData data = g.fromJson(jfilters.toString(), ReportFilterData.class);

        // Contains key of the user who is logged in and performs operations
        Key parentKey = KeyFactory.createKey(User_Global.KIND, token.user);

        // Contains key of the token given the current user
        Key key = KeyFactory.createKey(parentKey, AuthToken.KIND, token.id);

        // Token validation
        Response rsp = Utils.validateToken(token, key, datastore, LOG);
        if (rsp.getStatus() != Status.ACCEPTED.getStatusCode())
            return rsp;

        // Creates an empty list of filters
        List<Filter> filters = new ArrayList<Filter>();

		/*
         * Method to test if both dates passed by the user are valid If they're
		 * valid, then adds it to filter list
		 */
        this.testDates(data.startDate, data.endDate, filters);

        // If the type value is valid, than it's added to filter list
        if (data.validTypeValue()) {
            filters.add(new FilterPredicate(Report.PROPERTY_TYPE, FilterOperator.EQUAL, data.type));
        }

        // If the status value is valid, than it's added to filter list
        if (data.validStatusValue()) {
            filters.add(new FilterPredicate(Report.PROPERTY_STATUS_DESCRIPTION, FilterOperator.EQUAL, data.status));
        }

        Query query = new Query(Report.KIND);
        if (filters.size() > 0) {
            Filter filter;
            if (filters.size() == 1) {
                // Only one filter was passed by the user
                filter = filters.get(0);
            } else {
                // Multiple filters were passed by the user
                filter = CompositeFilterOperator.and(filters);
            }
            query.setFilter(filter);
        }

        List<Report> tmp = new ArrayList<Report>();

        Iterator<Entity> results = datastore.prepare(query).asIterator();
        if (!results.hasNext()) {
            // If no suggestions exist in database, it returns HTTP 204 code
            LOG.warning(Utils.OUTPUT_MISSING_LIST_REPORTS);
            return Response.status(Status.NO_CONTENT).build();
        }
        // While exists reports, append them to the list of reports
        while (results.hasNext()) {
            Entity report = results.next();

            String id = String.valueOf(report.getKey().getId());
            String type = (String) report.getProperty(Report.PROPERTY_TYPE);
            long priority = (long) report.getProperty(Report.PROPERTY_PRIORITY);
            String statusDescription = (String) report.getProperty(Report.PROPERTY_STATUS_DESCRIPTION);
            String description = (String) report.getProperty(Report.PROPERTY_DESCRIPTION);
            String imageUrl = (String) report.getProperty(Report.PROPERTY_IMAGE);
            String addressAsStreet = (String) report.getProperty(Report.PROPERTY_ADDRESS_AS_STREET);
            AddressData address = (AddressData) report.getProperty(Report.PROPERTY_ADDRESS);
            double lat = (double) report.getProperty(Report.PROPERTY_LATITUDE);
            double lon = (double) report.getProperty(Report.PROPERTY_LONGITUDE);
            long numOfFollowers = (long) report.getProperty(Report.PROPERTY_FOLLOWERS);
            String responsible = (String) report.getProperty(Report.PROPERTY_WORKER_RESPONSIBLE);

            Report newReport = new Report(id, type, (int) priority, statusDescription, description, imageUrl, addressAsStreet,
                    lat, lon, address, (int) numOfFollowers, responsible, -1);
            tmp.add(newReport);
        }
        //Report[] arr = new Report[tmp.size()];
       // tmp.toArray(arr);
        return Response.ok(g.toJson(tmp)).build();
    }

    // Checks if a date is valid
    private boolean validDate(String date) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        formatter.setLenient(false);
        try {
            formatter.parse(date);
        } catch (ParseException e) {
            // If input date is in different format or invalid.
            return false;
        }
        return true;

    }

    // Makes the verification of dates passed by the user and adds it to filter
    // list
    private void testDates(String startDate, String endDate, List<Filter> filters) {
        if (validDate(startDate)) {
            String[] fields = startDate.split("/");
            long d1 = new GregorianCalendar(Integer.parseInt(fields[0]), Integer.parseInt(fields[1]),
                    Integer.parseInt(fields[2])).getTimeInMillis();
            Filter f1 = new FilterPredicate(Report.PROPERTY_CREATION_DATE, FilterOperator.GREATER_THAN_OR_EQUAL, d1);
            if (validDate(endDate)) {
                // Testing reports in a range of dates
                fields = endDate.split("/");
                long d2 = new GregorianCalendar(Integer.parseInt(fields[0]), Integer.parseInt(fields[1]),
                        Integer.parseInt(fields[2])).getTimeInMillis();
                Filter f2 = new FilterPredicate(Report.PROPERTY_CREATION_DATE, FilterOperator.LESS_THAN_OR_EQUAL, d2);
                filters.add(CompositeFilterOperator.and(f1, f2));
            } else {
                // Testing reports after the startDate
                filters.add(f1);
            }
        }
    }

}
