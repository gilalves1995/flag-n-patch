package pt.unl.fct.di.apdc.flagnpatch.tasks;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import pt.unl.fct.di.apdc.flagnpatch.entities.Areas;
import pt.unl.fct.di.apdc.flagnpatch.entities.CredentialManager;
import pt.unl.fct.di.apdc.flagnpatch.entities.Graph;
import pt.unl.fct.di.apdc.flagnpatch.entities.ReportType;
import pt.unl.fct.di.apdc.flagnpatch.utils.StatusEnum;
import pt.unl.fct.di.apdc.flagnpatch.utils.TypeEnum;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by michael on 04-07-2017.
 */
@Path("/task/graph/day")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class GraphNewDayTask {
    // Logger object
    private static final Logger LOG = Logger.getLogger(GraphNewDayTask.class.getName());

    // Datastore link
    private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    public GraphNewDayTask() {
    }


    @GET
    @Path("/new")
    public Response triggerGraphRoutineNew() {
        Queue queue = QueueFactory.getDefaultQueue();
        queue.add(TaskOptions.Builder.withUrl("/rest/task/graph/day/new"));
        return Response.ok().build();
    }

    @GET
    @Path("/update")
    public Response triggerGraphRoutineUpdate() {
        Queue queue = QueueFactory.getDefaultQueue();
        queue.add(TaskOptions.Builder.withUrl("/rest/task/graph/day/update"));
        return Response.ok().build();
    }

    @POST
    @Path("/new")
    public Response GraphRoutineNew() {
        LOG.info("Starting the GraphNewDay routine...");

        Transaction txn = datastore.beginTransaction();

        Query query = new Query(Areas.KIND);
        Iterator<Entity> areaEntityIterator = datastore.prepare(query).asIterator();

        txn.rollback();

        while (areaEntityIterator.hasNext()) {
            Entity areaEntity = areaEntityIterator.next();
            String county = (String) areaEntity.getProperty(Areas.PROPERTY_COUNTY);
            ArrayList<String> typesOfArea = new ArrayList<>();

            txn = datastore.beginTransaction();

            Query.Filter filter = new Query.FilterPredicate(ReportType.PROPERTY_COUNTY, Query.FilterOperator.EQUAL, county);
            query = new Query(ReportType.KIND).setFilter(filter);
            Iterator<Entity> results = datastore.prepare(query).asIterator();

            txn.rollback();

            while (results.hasNext()) {
                Entity type = results.next();
                typesOfArea.add((String) type.getProperty(ReportType.PROPERTY_NAME));
            }

            Calendar cal = Calendar.getInstance(); //current date and time
            //cal.add(Calendar.DAY_OF_MONTH, 1);
            cal.setTimeZone(TimeZone.getTimeZone("Europe/Lisbon"));
            long nextDayMillis = cal.getTimeInMillis();

            txn = datastore.beginTransaction();

            Query.Filter filterByCounty = new Query.FilterPredicate(Graph.PROPERTY_COUNTY, Query.FilterOperator.EQUAL, county);
            Query.Filter filterByActive = new Query.FilterPredicate(Graph.PROPERTY_ACTIVE, Query.FilterOperator.EQUAL, true);
            query = new Query(Graph.KIND).setFilter(
                    Query.CompositeFilterOperator.and(filterByActive, filterByCounty));

            Entity entityOfGraph = datastore.prepare(query).asSingleEntity();

            entityOfGraph.setIndexedProperty(Graph.PROPERTY_ACTIVE, false);
            datastore.put(txn, entityOfGraph);

            txn.commit();
            txn = datastore.beginTransaction();

            Graph graph = new Graph(county, true, nextDayMillis, false);
            Entity graphEntity = new Entity(Graph.KIND);
            graphEntity.setIndexedProperty(Graph.PROPERTY_ACTIVE, graph.active);
            graphEntity.setIndexedProperty(Graph.PROPERTY_COUNTY, graph.county);
            graphEntity.setIndexedProperty(Graph.PROPERTY_DATE_DAY, graph.date);
            graphEntity.setIndexedProperty(Graph.PROPERTY_TOTAL_EVER, graph.isTotal);

            // Sets the entity's properties.
            for (String type : typesOfArea) {
                graphEntity.setIndexedProperty(type, 0);

            }
            for (StatusEnum statusEnum : StatusEnum.values()) {
                graphEntity.setIndexedProperty(statusEnum.getStatusDescription(), 0);
            }

            datastore.put(txn, graphEntity);
            txn.commit();
        }
        return Response.ok().build();
    }


    @POST
    @Path("/update")
    public Response GraphRoutineUpdate() {
        LOG.info("Starting the GraphNewDay - UPDATE routine...");

        Transaction txn = datastore.beginTransaction();

        Query query = new Query(Areas.KIND);
        Iterator<Entity> entityIterator = datastore.prepare(query).asIterator();
        txn.rollback();

        while (entityIterator.hasNext()) {
            Entity entity = entityIterator.next();
            String county = (String) entity.getProperty(Areas.PROPERTY_COUNTY);
            Calendar cal = Calendar.getInstance(); //current date and time
            cal.setTimeZone(TimeZone.getTimeZone("Europe/Lisbon"));
            cal.add(Calendar.DAY_OF_MONTH, -1);
            long nextDayMillis = cal.getTimeInMillis();

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            sdf.setTimeZone(TimeZone.getTimeZone("Europe/Lisbon"));
            String creationTime = sdf.format(nextDayMillis);
            System.out.println(creationTime);


            txn = datastore.beginTransaction();
            Query.Filter filterByCounty = new Query.FilterPredicate(Graph.PROPERTY_COUNTY, Query.FilterOperator.EQUAL, county);
            Query.Filter filterByActive = new Query.FilterPredicate(Graph.PROPERTY_ACTIVE, Query.FilterOperator.EQUAL, false);
            Query.Filter filterByDate = new Query.FilterPredicate(Graph.PROPERTY_DATE_DAY, Query.FilterOperator.EQUAL, creationTime);
            Query.Filter filterByIsTotal = new Query.FilterPredicate(Graph.PROPERTY_TOTAL_EVER, Query.FilterOperator.EQUAL, false);

            query = new Query(Graph.KIND).setFilter(
                    Query.CompositeFilterOperator.and(filterByActive, filterByCounty, filterByDate, filterByIsTotal));

            Entity entityOfGraph = datastore.prepare(query).asSingleEntity();

            txn.rollback();

            if (entityOfGraph == null) {
                System.out.println("espera pelo proximo dia");
                return Response.status(Response.Status.CONFLICT).build();
            }
            System.out.println("dia antigo fixe");

            ArrayList<String> typesOfArea = new ArrayList<>();
            txn = datastore.beginTransaction();

            Query.Filter filter = new Query.FilterPredicate(ReportType.PROPERTY_COUNTY, Query.FilterOperator.EQUAL, county);
            query = new Query(ReportType.KIND).setFilter(filter);
            Iterator<Entity> results = datastore.prepare(query).asIterator();

            txn.rollback();

            while (results.hasNext()) {
                Entity type = results.next();
                typesOfArea.add((String) type.getProperty(ReportType.PROPERTY_NAME));
            }

            txn = datastore.beginTransaction();

            filterByCounty = new Query.FilterPredicate(Graph.PROPERTY_COUNTY, Query.FilterOperator.EQUAL, county);
            filterByActive = new Query.FilterPredicate(Graph.PROPERTY_ACTIVE, Query.FilterOperator.EQUAL, false);
            filterByIsTotal = new Query.FilterPredicate(Graph.PROPERTY_TOTAL_EVER, Query.FilterOperator.EQUAL, true);
            query = new Query(Graph.KIND).setFilter(
                    Query.CompositeFilterOperator.and(filterByActive, filterByCounty, filterByIsTotal));

            Entity entityOfGraphTotal = datastore.prepare(query).asSingleEntity();

            for (String type : typesOfArea) {

                int newValue = (int) ((long) entityOfGraph.getProperty(type));
                int before = (int) ((long) entityOfGraphTotal.getProperty(type));
                entityOfGraphTotal.setIndexedProperty(type, before + newValue);
                datastore.put(txn, entityOfGraphTotal);
            }
            for (StatusEnum statusEnum : StatusEnum.values()) {


                int newValue = (int) ((long) entityOfGraph.getProperty(statusEnum.getStatusDescription()));
                int before = (int) ((long) entityOfGraphTotal.getProperty(statusEnum.getStatusDescription()));
                entityOfGraphTotal.setIndexedProperty(statusEnum.getStatusDescription(), before + newValue);
                datastore.put(txn, entityOfGraphTotal);
            }

            txn.commit();
        }
        return Response.ok().build();
    }

}
