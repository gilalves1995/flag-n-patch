package pt.unl.fct.di.apdc.flagnpatch.tasks;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import pt.unl.fct.di.apdc.flagnpatch.entities.AuthToken;
import pt.unl.fct.di.apdc.flagnpatch.entities.CredentialManager;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by michael on 16/06/17.
 */
@Path("/task/credential/email")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class EmailCredentialManagerTask {

    // Logger object
    private static final Logger LOG = Logger.getLogger(AuthTokenManagementTask.class.getName());

    // Datastore link
    private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    public EmailCredentialManagerTask() {
    }

    @GET
    @Path("/clean")
    public Response triggerCleanTokensRoutine() {
        Queue queue = QueueFactory.getDefaultQueue();
        queue.add(TaskOptions.Builder.withUrl("/rest/task/credential/email/clean"));
        return Response.ok().build();
    }

    @POST
    @Path("/clean")
    public Response cleanEmailCredentialsRoutine() {
        LOG.info("Starting the CredentialManager EMAIL cleanup routine...");
        Transaction txn = datastore.beginTransaction();
        Query.Filter filterByType = new Query.FilterPredicate(CredentialManager.PROPERTY_TYPE,
                Query.FilterOperator.EQUAL, CredentialManager.TYPE_EMAIL);
        Query query = new Query(CredentialManager.KIND).setFilter(filterByType);
        List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
        long currentTime = System.currentTimeMillis();
        if(results.size() == 0){
            txn.rollback();
            return Response.ok().build();
        }
        for (Entity credentialManager : results) {
            long expiration = (long) credentialManager.getProperty(CredentialManager.PROPERTY_EXPIRY_DATE);
            if (currentTime > expiration) {
                datastore.delete(credentialManager.getKey());
            }
        }
        txn.commit();
        return Response.ok().build();
    }
}
