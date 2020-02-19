package pt.unl.fct.di.apdc.flagnpatch.tasks;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.memcache.stdimpl.GCacheFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import pt.unl.fct.di.apdc.flagnpatch.entities.AuthToken;
import pt.unl.fct.di.apdc.flagnpatch.resources.general.Utils;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Path("/task/token")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class AuthTokenManagementTask {

    // Logger object
    private static final Logger LOG = Logger.getLogger(AuthTokenManagementTask.class.getName());

    // Datastore link
    private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    public AuthTokenManagementTask() {
    }    // Empty constructor for code 'correctness'

    @GET
    @Path("/clean")
    public Response triggerCleanTokensRoutine() {
        Queue queue = QueueFactory.getDefaultQueue();
        queue.add(TaskOptions.Builder.withUrl("/rest/task/token/clean"));
        return Response.ok().build();
    }

    @POST
    @Path("/clean")
    /**
     * Devia de se fazer a query e as remocoes dentro de uma transacao
     * No entanto nao o estou a fazer por ter sido incapaz de contornar
     * os erros da minha implementacao.
     * @return
     */
    public Response cleanTokensRoutine() {
        LOG.info("Starting the AuthToken cleanup routine...");

        Cache cache;
        CacheManager manager;
        manager = CacheManager.getInstance();
        cache = manager.getCache("token");
        try {
            if (cache == null) {
                CacheFactory cacheFactory = manager.getCacheFactory();
                Map<Object, Object> properties = new HashMap<>();
                properties.put(GCacheFactory.EXPIRATION_DELTA, TimeUnit.HOURS.toSeconds(1));
                cache = cacheFactory.createCache(properties);
                manager.registerCache("token", cache);
            }
        } catch (CacheException e) {
            LOG.severe(Utils.CACHE_ERROR);
        }

        Transaction txn = datastore.beginTransaction();
        Query q = new Query(AuthToken.KIND);
        List<Entity> results = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
        long currentTime = System.currentTimeMillis();
        if (results.size() == 0) {
            txn.rollback();
            return Response.ok().build();
        }
        for (Entity token : results) {
            long expiration = (long) token.getProperty(AuthToken.PROPERTY_EXPIRATION);
            //LOG.info("Expiration: " + expiration);
            if (currentTime > expiration) {
                datastore.delete(token.getKey());
                if (cache.containsKey(token.getKey().getName())) {
                    cache.remove(token.getKey().getName());
                }
            }
        }
        txn.commit();
        return Response.ok().build();
    }
}