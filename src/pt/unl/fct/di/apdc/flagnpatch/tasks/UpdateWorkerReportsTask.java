package pt.unl.fct.di.apdc.flagnpatch.tasks;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import pt.unl.fct.di.apdc.flagnpatch.entities.Report;
import pt.unl.fct.di.apdc.flagnpatch.utils.StatusEnum;
import pt.unl.fct.di.apdc.flagnpatch.utils.TypeEnum;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by michael on 09-07-2017.
 */
@Path("/task/reports/update")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class UpdateWorkerReportsTask {

    // Logger object
    private static final Logger LOG = Logger.getLogger(AuthTokenManagementTask.class.getName());

    // Datastore link
    private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    public UpdateWorkerReportsTask() {
    }

    @GET
    @Path("/points")
    public Response triggerReportsUpdatePoints() {
        Queue queue = QueueFactory.getDefaultQueue();
        queue.add(TaskOptions.Builder.withUrl("/task/reports/update/points"));
        return Response.ok().build();
    }

    @POST
    @Path("/points")
    public Response reportsUpdatePointsRoutine() {
        LOG.info("Starting the ReportsUpdatePoints routine...");
        Transaction txn = datastore.beginTransaction();
        Query.Filter filterByStatus1 = new Query.FilterPredicate(Report.PROPERTY_STATUS_DESCRIPTION,
                Query.FilterOperator.EQUAL, StatusEnum.SUBMITED.getStatusDescription());
        Query.Filter filterByStatus2 = new Query.FilterPredicate(Report.PROPERTY_STATUS_DESCRIPTION,
                Query.FilterOperator.EQUAL, StatusEnum.IN_RESOLUTION.getStatusDescription());
        Query query = new Query(Report.KIND).setFilter(Query.CompositeFilterOperator.or(filterByStatus1, filterByStatus2));
        List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
        txn.rollback();

        if (results.size() == 0) {
            return Response.ok().build();
        }

        for (Entity report : results) {
             txn = datastore.beginTransaction();
            long oldPoints = (long) report.getProperty(Report.PROPERTY_POINTS);
            report.setIndexedProperty(Report.PROPERTY_POINTS, (int) oldPoints + 50);
            datastore.put(txn, report);
            txn.commit();
        }
        return Response.ok().build();
    }
}


