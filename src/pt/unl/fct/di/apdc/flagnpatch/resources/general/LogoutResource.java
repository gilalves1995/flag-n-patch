package pt.unl.fct.di.apdc.flagnpatch.resources.general;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.memcache.stdimpl.GCacheFactory;
import com.google.gson.Gson;
import pt.unl.fct.di.apdc.flagnpatch.entities.AuthToken;
import pt.unl.fct.di.apdc.flagnpatch.entities.User_Global;
import pt.unl.fct.di.apdc.flagnpatch.utils.StringUtil;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Path("/logout")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LogoutResource {

    // Logger object
    private static final Logger LOG = Logger.getLogger(LogoutResource.class.getName());
    // Datastore link
    private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    // Converts objects to JSON
    private final Gson g = new Gson();

    // Empty constructor for code 'correctness'
    public LogoutResource() {
    }

    @POST
    @Path("/")
    public Response doLogout(AuthToken data) {
        LOG.info("Attempting to logout user: " + data.user);

        // Checks whether the parsed data is a valid token.
        // If not, terminates execution and replies to the client with
        // BAD_REQUEST.
        if (!data.validToken()) {
            LOG.warning("User " + data.user + " tried to logout by providing a wrong token");
            return Response.status(Status.FORBIDDEN).entity(g.toJson(new StringUtil("Bad token or wrong parameter."))).build();

        }

        // Begins a transaction.
        Transaction txn = datastore.beginTransaction();

        try {
            // Builds the user key using the user email addressAsStreet provived with
            // the parsed token.
            Key parent = KeyFactory.createKey(User_Global.KIND, data.user);

            // Builds the token key using the token id provived with the parsed
            // token.
            Key tokenKey = KeyFactory.createKey(parent, AuthToken.KIND, data.id);

            // Tries to fetch the AuthToken entity from the database.
            // Throws EntityNotFoundException if not succeeded.
            Entity e = datastore.get(tokenKey);

            Entity user = datastore.get(parent);
            @SuppressWarnings("unchecked")
            String role = (String) user.getProperty(User_Global.PROPERTY_ROLE);

            // Builds an AuthToken object out of the fetched entity.
            AuthToken token = new AuthToken((String) e.getProperty(AuthToken.PROPERTY_USER),
                    e.getKey().getName(), (long) e.getProperty(AuthToken.PROPERTY_CREATION),
                    (long) e.getProperty(AuthToken.PROPERTY_EXPIRATION), role);


            // Compares if the provided token fields match those of the fetched
            // one.
            // If not, terminates execution and replies to the client with
            // BAD_REQUEST.
            if (!token.equals(data)) {
                txn.rollback();
                LOG.warning(Utils.OUTPUT_TOKEN_INVALID + " " + data.user + ", tokenId " + data.id);
                return Response.status(Status.UNAUTHORIZED).entity(g.toJson(new StringUtil(Utils.OUTPUT_TOKEN_INVALID))).build();
            }

            // Deletes the token from the database and commits.
            datastore.delete(txn, tokenKey);
            txn.commit();


           /* Cache cache = null;
            CacheManager manager;
            try {
                manager = CacheManager.getInstance();
                cache = manager.getCache("token");
                if (cache == null) {
                    CacheFactory cacheFactory = manager.getCacheFactory();
                    Map<Object, Object> properties = new HashMap<>();
                    properties.put(GCacheFactory.EXPIRATION_DELTA, TimeUnit.HOURS.toSeconds(1));
                    cache = cacheFactory.createCache(properties);
                    manager.registerCache("token", cache);
                }
                if(cache.containsKey(data.id)){
                        cache.remove(data.id);
                }
            } catch (CacheException e1) {
                LOG.severe(Utils.CACHE_ERROR);
            }

             cache = null;
            try {
                manager = CacheManager.getInstance();
                cache = manager.getCache("userEntities");
                if (cache == null) {
                    CacheFactory cacheFactory = manager.getCacheFactory();
                    Map<Object, Object> properties = new HashMap<>();
                    properties.put(GCacheFactory.EXPIRATION_DELTA, TimeUnit.MINUTES.toSeconds(30));
                    cache = cacheFactory.createCache(properties);
                    manager.registerCache("userEntities", cache);
                }
                if(cache.containsKey(data.user)){
                    cache.remove(data.user);
                }
            } catch (CacheException e1) {
                LOG.severe(Utils.CACHE_ERROR);
            }
*/
            // Informs the client that the operation finished successfully.
            LOG.info(Utils.OUTPUT_USER_LOGGED_OUT + " " + data.user);
            return Response.ok(g.toJson(new StringUtil(Utils.OUTPUT_USER_LOGGED_OUT))).build();

        } catch (EntityNotFoundException e) {
            // The provived AuthToken was not found in the database.
            txn.rollback();
            LOG.warning(Utils.OUTPUT_TOKEN_INVALID + " " + data.user + ", tokenId " + data.id);
            return Response.status(Status.UNAUTHORIZED).entity(g.toJson(new StringUtil(Utils.OUTPUT_TOKEN_INVALID))).build();

        } finally {
            // Checks if something went wrong and the transaction is still
            // active.
            if (txn.isActive()) {
                txn.rollback();
                LOG.severe(Utils.OUTPUT_TRANSACTION_STILL_ACTIVE);
                return Response.status(Status.INTERNAL_SERVER_ERROR).entity(g.toJson(new StringUtil(Utils.OUTPUT_TRANSACTION_STILL_ACTIVE)))
                        .build();
            }
        }
    }

}
