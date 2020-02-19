package pt.unl.fct.di.apdc.flagnpatch.resources.administration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import pt.unl.fct.di.apdc.flagnpatch.entities.AuthToken;
import pt.unl.fct.di.apdc.flagnpatch.entities.Report;
import pt.unl.fct.di.apdc.flagnpatch.entities.ReportType;
import pt.unl.fct.di.apdc.flagnpatch.entities.User_BackOffice;
import pt.unl.fct.di.apdc.flagnpatch.entities.User_Global;
import pt.unl.fct.di.apdc.flagnpatch.inputData.AddressData;
import pt.unl.fct.di.apdc.flagnpatch.resources.frontOffice.ReportResource;
import pt.unl.fct.di.apdc.flagnpatch.resources.general.Utils;
import pt.unl.fct.di.apdc.flagnpatch.utils.ResourcesEnum;
import pt.unl.fct.di.apdc.flagnpatch.utils.RolesEnum;
import pt.unl.fct.di.apdc.flagnpatch.utils.StringUtil;

@Path("/admin/workerManagement")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class WorkerManagementResources {

    private static final Logger LOG = Logger.getLogger(ReportResource.class.getName());
    private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    private final Gson g = new Gson();

    public WorkerManagementResources() {

    }

    // Method returns users with role Worker that are active in this area

    @POST
    @Path("/listWorkers")
    public Response listWorkers(AuthToken token) {
      /* Security Validations */
        Entity user = Utils.getUser(token.user, datastore, LOG);
        if (user == null) {
            LOG.warning(Utils.OUTPUT_MISSING_USER);
            return Response.status(Status.UNAUTHORIZED).entity(g.toJson(new StringUtil(Utils.OUTPUT_MISSING_USER))).build();
        }
        Response rsp = Utils.securityValidations(user, token, ResourcesEnum.ADMINISTRATION_LIST_WORKERS, datastore,
                LOG);
        if (rsp.getStatus() != Status.ACCEPTED.getStatusCode())
            return rsp;
			  /* Security Validations */

        EmbeddedEntity address = (EmbeddedEntity) user.getProperty(User_Global.PROPERTY_ADDR);
        String county = (String) address.getProperty(AddressData.PROPERTY_COUNTY);

        // Filters users by roles and working areas
        Filter filterByRole = new FilterPredicate(User_Global.PROPERTY_ROLE, FilterOperator.EQUAL, RolesEnum.WORKER_USER.getRoleDescription());

        // Filter filterByWorkArea = new
        // FilterPredicate(User_BackOffice.PROPERTY_WORKING_AREA,
        // FilterOperator.IN, new ArrayList<String>(Arrays.asList(county)));
        Filter filterByWorkArea = new FilterPredicate(User_BackOffice.PROPERTY_WORKING_AREA, FilterOperator.EQUAL,
                county);

        Query query = new Query(User_Global.KIND)
                .setFilter(CompositeFilterOperator.and(filterByRole, filterByWorkArea));

        Iterator<Entity> results = datastore.prepare(query).asIterator();
        if (!results.hasNext()) {
            // If the no results are found, message of "No content" is sent
            return Response.status(Status.NO_CONTENT).build();
        }


        List<User_BackOffice> workerList = new ArrayList<User_BackOffice>();
        while (results.hasNext()) {
            Entity workerEntity = results.next();

            String name = (String) workerEntity.getProperty(User_Global.PROPERTY_NAME);
            String email = workerEntity.getKey().getName();


            // Gathers information about user's living area
            EmbeddedEntity livingAreaEntity = (EmbeddedEntity) workerEntity.getProperty(User_Global.PROPERTY_ADDR);
            String userCounty = (String) livingAreaEntity.getProperty(AddressData.PROPERTY_COUNTY);
            String userDistrict = (String) livingAreaEntity.getProperty(AddressData.PROPERTY_DISTRICT);

            AddressData livingArea = new AddressData(userDistrict, userCounty);

            String nif = (String) workerEntity.getProperty(User_BackOffice.PROPERTY_NIF);

            @SuppressWarnings("unchecked")
            ArrayList<String> workArea = (ArrayList<String>) workerEntity
                    .getProperty(User_BackOffice.PROPERTY_WORKING_AREA);
            String workerInfo = (String) workerEntity.getProperty(User_BackOffice.PROPERTY_WORKER_INFO);

            
            User_BackOffice worker = new User_BackOffice(name, email, livingArea, RolesEnum.WORKER_USER.getRoleDescription(), nif, workArea, workerInfo);
            workerList.add(worker);
        }

     //   User_BackOffice[] arr = new User_BackOffice[workerList.size()];
     //   workerList.toArray(arr);

        return Response.ok(g.toJson(workerList)).build();
    }

    @POST
    @Path("/loadAdminInfo")
    public Response loadAdminInfo(AuthToken token) {
         /* Security Validations */
        Entity user = Utils.getUser(token.user, datastore, LOG);
        if (user == null) {
            LOG.warning(Utils.OUTPUT_MISSING_USER);
            return Response.status(Status.UNAUTHORIZED).entity(g.toJson(new StringUtil(Utils.OUTPUT_MISSING_USER))).build();
        }
        Response rsp = Utils.securityValidations(user, token, ResourcesEnum.ADMINISTRATION_GEN_INFO, datastore, LOG);
        if (rsp.getStatus() != Status.ACCEPTED.getStatusCode())
            return rsp;
 /* Security Validations */

        // Get user's county
        EmbeddedEntity addrEntity = (EmbeddedEntity) user.getProperty(User_Global.PROPERTY_ADDR);
        String county = (String) addrEntity.getProperty(AddressData.PROPERTY_COUNTY);

        // Gets workers in this county
        Filter filter = new FilterPredicate(User_BackOffice.PROPERTY_WORKING_AREA, FilterOperator.EQUAL, county);
        Query query = new Query(User_BackOffice.KIND).setFilter(filter);
        Iterator<Entity> it = datastore.prepare(query).asIterator();

        if (!it.hasNext()) {
            // No workers were found
            return Response.status(Status.NO_CONTENT).entity(g.toJson("No workers were found.")).build();
        }

        JsonArray array = new JsonArray();
        while (it.hasNext()) {
            Entity worker = it.next();

            // General data
            String email = worker.getKey().getName();
            String name = (String) worker.getProperty(User_Global.PROPERTY_NAME);

            @SuppressWarnings("unchecked")
            ArrayList<String> workingArea = (ArrayList<String>) worker
                    .getProperty(User_BackOffice.PROPERTY_WORKING_AREA);

            JsonObject obj = new JsonObject();
            obj.addProperty("email", email);
            obj.addProperty("name", name);

            String data = g.toJson(workingArea);
            JsonArray jsonArray = new JsonParser().parse(data).getAsJsonArray();
            obj.add("workingArea", jsonArray);


//			String typesData = g.toJson(this.getReportTypes(token.user, email));
//			System.out.println("typesData: " + typesData);
//			jsonArray = new JsonParser().parse(typesData).getAsJsonArray();
//			obj.add("services", jsonArray);

            String typesData = g.toJson(this.getReportTypes(county, email));
           // System.out.println("typesData: " + typesData);
            jsonArray = new JsonParser().parse(typesData).getAsJsonArray();
            obj.add("services", jsonArray);

//			String reportsData = g.toJson(this.getReports(email, county));
//			jsonArray = new JsonParser().parse(reportsData).getAsJsonArray();
//			obj.add("reports", jsonArray);

            JsonArray reportArray = new JsonArray();
            Map<String, Integer> results = this.reportNumberByType(email, county);
            for (Map.Entry<String, Integer> entry : results.entrySet()) {
                JsonObject o = new JsonObject();
                o.addProperty("type", entry.getKey());
                o.addProperty("numberOf", entry.getValue());
                reportArray.add(o);
            }
            obj.add("reports", reportArray);
            array.add(obj);
        }

        return Response.ok(g.toJson(array)).build();

    }

    private List<String> getReportTypes(String council, String resp) {
        List<String> l = new ArrayList<>();
        Filter filterByResp = new FilterPredicate(ReportType.PROPERTY_RESPONSIBLE, FilterOperator.EQUAL, resp);
        Filter filterByCounty = new FilterPredicate(ReportType.PROPERTY_COUNTY, FilterOperator.EQUAL, council);
        Query query = new Query(ReportType.KIND)
                .setFilter(CompositeFilterOperator.and(filterByResp, filterByCounty));

        Iterator<Entity> it = datastore.prepare(query).asIterator();
        while (it.hasNext()) {
            Entity reportType = it.next();
            String name = (String) reportType.getProperty(ReportType.PROPERTY_NAME);
            l.add(name);
        }
        return l;
    }
	
	
	
	/*
	private List<Report> getReports(String resp, String county) {
		List<Report> l = new ArrayList<Report>();
		Filter filterByResp = new FilterPredicate(Report.PROPERTY_WORKER_RESPONSIBLE, FilterOperator.EQUAL, resp);
		Filter filterByCounty = new FilterPredicate("address.county", FilterOperator.EQUAL, county);
		Query query = new Query(Report.KIND)
				.setFilter(CompositeFilterOperator.and(filterByResp, filterByCounty));
		
		Iterator <Entity> it = datastore.prepare(query).asIterator();
		while(it.hasNext()) {
			Entity reportEntity = it.next();
			l.add(Utils.buildReport(reportEntity));
		}
		return l;
	}
	*/


    // ADDRESS_BY_STREET
    private Map<String, Integer> reportNumberByType(String resp, String county) {

        Map<String, Integer> results = new HashMap<String, Integer>();

        Filter filterByResp = new FilterPredicate(Report.PROPERTY_WORKER_RESPONSIBLE, FilterOperator.EQUAL, resp);
        Filter filterByCounty = new Query.FilterPredicate("address.county", FilterOperator.EQUAL, county);
        Query query = new Query(Report.KIND)
                .setFilter(CompositeFilterOperator.and(filterByResp, filterByCounty));

        Iterator<Entity> it = datastore.prepare(query).asIterator();
        while (it.hasNext()) {
            Entity report = it.next();
            String type = (String) report.getProperty(Report.PROPERTY_TYPE);
            if (!results.containsKey(type)) {
                results.put(type, 1);
            } else {
                int reportNumber = results.get(type);
                results.put(type, reportNumber + 1);
            }

        }
        return results;
    }

}



/*
@GET
@Path("/test")
@Produces(MediaType.APPLICATION_JSON)
public Response testegil() {

    Query.Filter filterByCounty = new Query.FilterPredicate("address.county",
            Query.FilterOperator.EQUAL, "Barreiro");
    Query query = new Query(Report.KIND).setFilter(filterByCounty);

    Iterator<Entity> it=datastore.prepare(query).asIterator();
    //List<String> allUsers = new ArrayList<>();
    while(it.hasNext()){
        Entity report=it.next();
        System.out.println(report.getProperty(Report.PROPERTY_ADDRESS));

    }
    return
            Response.ok().build();
}
*/

/*
 * worker : { email, name, area, ocorrenciasAssociadas: { Saneamento: {
 * 47538490583049584 (long report id) } }
 * 
 * }
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 */
