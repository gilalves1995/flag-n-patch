package pt.unl.fct.di.apdc.flagnpatch.resources.administration;

import com.google.appengine.api.datastore.*;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import pt.unl.fct.di.apdc.flagnpatch.entities.AuthToken;
import pt.unl.fct.di.apdc.flagnpatch.entities.Graph;
import pt.unl.fct.di.apdc.flagnpatch.entities.Report;
import pt.unl.fct.di.apdc.flagnpatch.entities.User_Global;
import pt.unl.fct.di.apdc.flagnpatch.inputData.AddressData;
import pt.unl.fct.di.apdc.flagnpatch.inputData.CommentData;
import pt.unl.fct.di.apdc.flagnpatch.inputData.LoginData;
import pt.unl.fct.di.apdc.flagnpatch.resources.general.LoginResource;
import pt.unl.fct.di.apdc.flagnpatch.resources.general.Utils;
import pt.unl.fct.di.apdc.flagnpatch.utils.ResourcesEnum;
import pt.unl.fct.di.apdc.flagnpatch.utils.StatusEnum;
import pt.unl.fct.di.apdc.flagnpatch.utils.StringUtil;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

@Path("/admin/graph")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class GraphResources {

    // Logger object
    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
    // Datastore link
    private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    // Converts objects to JSON
    private final Gson g = new Gson();

    // Empty constructor for code 'correctness'
    public GraphResources() {
    }


    @POST
    @Path("/reports")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReportsGraph(String jsonString) {

        JsonElement jelement = new JsonParser().parse(jsonString);
        JsonObject jobject = jelement.getAsJsonObject();
        JsonObject jtoken = jobject.getAsJsonObject("token");
        AuthToken token = g.fromJson(jtoken.toString(), AuthToken.class);

        /* Security Validations */
        Entity user = Utils.getUser(token.user, datastore, LOG);
        if (user == null) {
            LOG.warning(Utils.OUTPUT_MISSING_USER);
            return Response.status(Response.Status.UNAUTHORIZED).entity(g.toJson(new StringUtil(Utils.OUTPUT_MISSING_USER))).build();
        }
        Response rsp = Utils.securityValidations(user, token, ResourcesEnum.ADMINISTRATION_GET_GRAPH_DATA, datastore,
                LOG);
        if (rsp.getStatus() != Response.Status.ACCEPTED.getStatusCode())
            return rsp;
        /* Security Validations */

        EmbeddedEntity embeddedEntity = (EmbeddedEntity) user.getProperty(User_Global.PROPERTY_ADDR);
        String county = (String) embeddedEntity.getProperty(AddressData.PROPERTY_COUNTY);

        boolean hasDate = jobject.has("date");
        String date = null;

        Transaction txn = datastore.beginTransaction();

        Query.Filter filterByCounty = new Query.FilterPredicate(Graph.PROPERTY_COUNTY, Query.FilterOperator.EQUAL, county);
        Query query = null;
        if (hasDate) {
            JsonElement jdate = jobject.getAsJsonPrimitive("date");
            date = g.fromJson(jdate.toString(), String.class);

          //  Query.Filter filterByActive = new Query.FilterPredicate(Graph.PROPERTY_ACTIVE, Query.FilterOperator.EQUAL, false);
            Query.Filter filterByDate = new Query.FilterPredicate(Graph.PROPERTY_DATE_DAY, Query.FilterOperator.EQUAL, date);
            Query.Filter filterByIsTotal = new Query.FilterPredicate(Graph.PROPERTY_TOTAL_EVER, Query.FilterOperator.EQUAL, false);

            query = new Query(Graph.KIND).setFilter(
                    Query.CompositeFilterOperator.and(filterByCounty, filterByDate, filterByIsTotal));

        } else {
            filterByCounty = new Query.FilterPredicate(Graph.PROPERTY_COUNTY, Query.FilterOperator.EQUAL, county);
            Query.Filter filterByActive = new Query.FilterPredicate(Graph.PROPERTY_ACTIVE, Query.FilterOperator.EQUAL, false);
            Query.Filter filterByIsTotal = new Query.FilterPredicate(Graph.PROPERTY_TOTAL_EVER, Query.FilterOperator.EQUAL, true);

            query = new Query(Graph.KIND).setFilter(
                    Query.CompositeFilterOperator.and(filterByActive, filterByCounty, filterByIsTotal));
        }

        Entity entityOfGraphTotal = datastore.prepare(query).asSingleEntity();
        txn.rollback();
        if (entityOfGraphTotal == null) {
            LOG.severe(Utils.OUTPUT_NO_DATA_FOR_GRAPH);
            return Response.status(Response.Status.BAD_REQUEST).entity(g.toJson(new StringUtil(Utils.OUTPUT_NO_DATA_FOR_GRAPH))).build();
        }
        java.util.Map<String, Object> mapFromDatabase = entityOfGraphTotal.getProperties();

        Map<String, Object> map = new HashMap<>();
        map.putAll(mapFromDatabase);

        map.remove(Graph.PROPERTY_ACTIVE);
        map.remove(Graph.PROPERTY_COUNTY);

        boolean isTotal = (boolean) map.get(Graph.PROPERTY_TOTAL_EVER);
        String dateForOutput = (String) map.get(Graph.PROPERTY_DATE_DAY);

        map.remove(Graph.PROPERTY_TOTAL_EVER);
        map.remove(Graph.PROPERTY_DATE_DAY);

        JsonObject graph2Object = new JsonObject();

        for (StatusEnum statusEnum : StatusEnum.values()) {
            long value = (long) map.get(statusEnum.getStatusDescription());
            graph2Object.addProperty(statusEnum.getStatusDescription(), value);
            map.remove(statusEnum.getStatusDescription());
        }

        Set<Map.Entry<String, Object>> set = map.entrySet();
        Iterator<Map.Entry<String, Object>> it = set.iterator();

        JsonObject graph1Object = new JsonObject();

        while (it.hasNext()) {
            Map.Entry<String, Object> mapEntry = it.next();
            String key = mapEntry.getKey();
            long value = (long) mapEntry.getValue();
            graph1Object.addProperty(key, value);
        }

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(Graph.PROPERTY_TOTAL_EVER, isTotal);
        jsonObject.addProperty(Graph.PROPERTY_DATE_DAY, dateForOutput);
        jsonObject.add("graph1", graph1Object);
        jsonObject.add("graph2", graph2Object);

        return Response.ok(g.toJson(jsonObject)).build();
    }
}
