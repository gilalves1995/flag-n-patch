package pt.unl.fct.di.apdc.flagnpatch.tasks;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.gson.Gson;
import pt.unl.fct.di.apdc.flagnpatch.entities.Follower;
import pt.unl.fct.di.apdc.flagnpatch.entities.Report;
import pt.unl.fct.di.apdc.flagnpatch.resources.frontOffice.ReportResource;
import pt.unl.fct.di.apdc.flagnpatch.utils.StatusEnum;
import pt.unl.fct.di.apdc.flagnpatch.utils.StringUtil;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Iterator;
import java.util.logging.Logger;

@Path("/task/reports")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ReportManagementTask {

    // Logger object
    private static final Logger LOG = Logger.getLogger(ReportResource.class.getName());
    // Datastore link
    private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    // Converts objects to JSON
    private final Gson g = new Gson();

    public ReportManagementTask() {

    }

    @GET
    @Path("/removeFollows")
    public Response triggerCleanTokensRoutine() {
        Queue queue = QueueFactory.getDefaultQueue();
        queue.add(TaskOptions.Builder.withUrl("/rest/task/reports/removeFollows"));
        return Response.ok().build();
    }

    @POST
    @Path("/removeFollows")
    public Response manageExpiredFollows() {

        // Begins a new transaction

        Transaction txn = datastore.beginTransaction();
        Query query = new Query(Follower.KIND);
        Iterator<Entity> results = datastore.prepare(query).asIterator();
        txn.rollback();
        if (!results.hasNext()) {
            LOG.warning("No entities found in follower table.");
            return Response.status(Status.FORBIDDEN).entity(g.toJson(new StringUtil("No entities were found."))).build();
        }


        // Counts the number of removed entities
        int count = 0;
        while (results.hasNext()) {
            Entity follower = results.next();

            String reportId = (String) follower.getProperty(Follower.PROPERTY_REPORT);
            Key reportKey = KeyFactory.createKey(Report.KIND, Long.parseLong(reportId));
            txn = datastore.beginTransaction();
            try {
                Entity report = datastore.get(reportKey);

                // Checks if the report is unused
                String status = (String) report.getProperty(Report.PROPERTY_STATUS_DESCRIPTION);
                if (status.equals(StatusEnum.CLOSED) || status.equals(StatusEnum.REJECTED.getStatusDescription())) {

                    // If the report is unused, then it will be removed from database
                    datastore.delete(txn, follower.getKey());
                    count++;
                    txn.commit();
                } else {
                    txn.rollback();
                }
            } catch (EntityNotFoundException e) {
                txn.rollback();
                continue;
            }
        }
        return Response.ok(g.toJson(count)).build();
    }
}
