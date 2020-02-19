package pt.unl.fct.di.apdc.flagnpatch.resources.general;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.memcache.stdimpl.GCacheFactory;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.codec.digest.DigestUtils;
import org.glassfish.jersey.server.ResourceConfig;
import pt.unl.fct.di.apdc.flagnpatch.entities.AuthToken;
import pt.unl.fct.di.apdc.flagnpatch.entities.CredentialManager;
import pt.unl.fct.di.apdc.flagnpatch.entities.ReportType;
import pt.unl.fct.di.apdc.flagnpatch.entities.User_Global;
import pt.unl.fct.di.apdc.flagnpatch.inputData.LoginData;
import pt.unl.fct.di.apdc.flagnpatch.utils.ResourcesEnum;
import pt.unl.fct.di.apdc.flagnpatch.utils.RolesEnum;
import pt.unl.fct.di.apdc.flagnpatch.utils.StringUtil;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;
import javax.ws.rs.*;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource extends ResourceConfig {

    // Logger object
    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
    // Datastore link
    private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    // Converts objects to JSON
    private final Gson g = new Gson();


    // Empty constructor for code 'correctness'
    public LoginResource() {

    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response doLogin(LoginData data) {
        LOG.info("Attempting to login user: " + data.email);

        // Checks whether the login data is valid.
        // If not, terminates execution and replies to the client with
        // BAD_REQUEST.
        if (!data.validLoginData()) {
            LOG.warning(Utils.OUTPUT_WRONG_FIELDS);
            return Response.status(Status.BAD_REQUEST).entity(g.toJson(new StringUtil(Utils.OUTPUT_WRONG_FIELDS))).build();
        }

        // Begins a transaction.
        TransactionOptions options = TransactionOptions.Builder.withXG(true);
        Transaction txn = datastore.beginTransaction(options);

        try {
            // Builds the key using the provided email addressAsStreet.
            Key userKey = KeyFactory.createKey(User_Global.KIND, data.email);

            // Throws EntityNotFoundException if the provided email addressAsStreet is
            // not in use.
            Entity user = datastore.get(userKey);

            boolean isVerified = (boolean) user.getProperty(User_Global.PROPERTY_ACCOUNT_VERIFIED);
            String role = (String) user.getProperty(User_Global.PROPERTY_ROLE);
            if ((role.equalsIgnoreCase(RolesEnum.CORE_USER.getRoleDescription()) ||
                    role.equalsIgnoreCase(RolesEnum.END_USER.getRoleDescription()) || role.equalsIgnoreCase(RolesEnum.WORKER_USER.getRoleDescription())) && !isVerified) {
                txn.rollback();
                LOG.warning(Utils.OUTPUT_NEED_CONFIRM_EMAIL + data.email);
                return Response.status(Status.UNAUTHORIZED).entity(g.toJson(new StringUtil(Utils.OUTPUT_NEED_CONFIRM_EMAIL))).build();
            }

            // Obtains the hashed password from the database.
            String hashedPWD = (String) user.getProperty(User_Global.PROPERTY_PWD);

            // If the provided password doesn't match the one on the system,
            // terminates execution and replies to the client with FORBIDDEN.
            if (!hashedPWD.equals(DigestUtils.sha512Hex(data.password))) {
                txn.rollback();
                LOG.warning(Utils.OUTPUT_BAD_CREDENTIALS);
                return Response.status(Status.UNAUTHORIZED).entity(g.toJson(new StringUtil(Utils.OUTPUT_BAD_CREDENTIALS))).build();
            }

            Query.Filter filterByEmail = new Query.FilterPredicate(
                    AuthToken.PROPERTY_USER, Query.FilterOperator.EQUAL, data.email);
            Query query = new Query(AuthToken.KIND).setFilter(filterByEmail);
            Entity authTokenEntity;
            //  try {
            authTokenEntity = datastore.prepare(query).asSingleEntity();
            //} catch (PreparedQuery.TooManyResultsException e) {
            //       txn.rollback();
            //       LOG.severe("Too many results in AuthToken for user");
            //   }

            AuthToken token = null;
            long expirationData = 0;

            Cache cache = null;
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
            } catch (CacheException e) {
                LOG.severe(Utils.CACHE_ERROR);
            }

            if (authTokenEntity != null) {
                expirationData = (long) authTokenEntity.getProperty(AuthToken.PROPERTY_EXPIRATION);

                if ((System.currentTimeMillis() > expirationData)) {
                    if (cache.containsKey(authTokenEntity.getKey().getName())) {
                        cache.remove(authTokenEntity.getKey().getName());
                    }
                    datastore.delete(txn, authTokenEntity.getKey());
                    Object[] objects = Utils.buildEntityAuthToken(data, role, userKey);
                    token = (AuthToken) objects[0];
                    Entity tkn = (Entity) objects[1];
                    datastore.put(txn, tkn);
                    cache.put(tkn.getKey().getName(), tkn);
                    txn.commit();
                } else if ((expirationData - System.currentTimeMillis() <= (1000 * 60 * 30))) {
                    String id = authTokenEntity.getKey().getName();
                    long creationData = (long) authTokenEntity.getProperty(AuthToken.PROPERTY_CREATION);
                    token = new AuthToken(data.email, id, creationData, expirationData + AuthToken.EXPIRATION_TIME, role);
                    authTokenEntity.setProperty(AuthToken.PROPERTY_EXPIRATION, expirationData + AuthToken.EXPIRATION_TIME);
                    datastore.put(txn, authTokenEntity);
                    cache.put(authTokenEntity.getKey().getName(), authTokenEntity);
                    txn.commit();

                } else {
                    String id = authTokenEntity.getKey().getName();
                    long creationData = (long) authTokenEntity.getProperty(AuthToken.PROPERTY_CREATION);
                    token = new AuthToken(data.email, id, creationData, expirationData, role);
                    cache.put(authTokenEntity.getKey().getName(), authTokenEntity);
                    txn.rollback();
                }
            } else {
                Object[] objects = Utils.buildEntityAuthToken(data, role, userKey);
                token = (AuthToken) objects[0];
                Entity tkn = (Entity) objects[1];
                datastore.put(txn, tkn);
                cache.put(tkn.getKey().getName(), tkn);
                // Stores the changes into the transactions and commits.
                txn.commit();
            }
            // If the operation finished successfully, the built token is
            // returned to the client.
            LOG.info(Utils.OUTPUT_USER_LOGGED_IN + " " + data.email);
            return Response.ok(g.toJson(token)).build();
        } catch (EntityNotFoundException e) {
            // If the provided email addressAsStreet isn't in use, terminates the
            // transaction.
            txn.rollback();
            LOG.warning(Utils.OUTPUT_MISSING_USER + ": " + data.email);
            return Response.status(Status.UNAUTHORIZED).entity(g.toJson(new StringUtil(Utils.OUTPUT_BAD_CREDENTIALS))).build();
        } finally {
            // Checks if something went wrong and the transaction is still
            // active.
            if (txn.isActive()) {
                txn.rollback();
                LOG.severe(Utils.OUTPUT_TRANSACTION_STILL_ACTIVE);
                return Response.status(Status.INTERNAL_SERVER_ERROR).entity(g.toJson(new StringUtil(Utils.OUTPUT_TRANSACTION_STILL_ACTIVE))).build();
            }
        }
    }

    @POST
    @Path("/resetPassword")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response resetPassword(String jsonString) {
        // Extracts data from custom JSON input string and creates objects from
        // it
        JsonElement jelement = new JsonParser().parse(jsonString);
        JsonObject jobject = jelement.getAsJsonObject();
        JsonElement jemail = jobject.get("email");
        String userEmail = g.fromJson(jemail.toString(), String.class);

        Entity user = Utils.getUser(userEmail, datastore, LOG);
        if (user == null) {
            LOG.warning(Utils.OUTPUT_MISSING_USER);
            return Response.status(Status.FORBIDDEN.getStatusCode()).entity(g.toJson(new StringUtil(Utils.OUTPUT_MISSING_USER))).build();
        }

        // Begins a transaction.
        TransactionOptions options = TransactionOptions.Builder.withXG(true);
        Transaction txn = datastore.beginTransaction(options);

        // Builds the key using the provided email addressAsStreet.
        Query.Filter filterByEmail = new Query.FilterPredicate(CredentialManager.PROPERTY_USER_EMAIL,
                Query.FilterOperator.EQUAL, userEmail);
        Query.Filter filterByType = new Query.FilterPredicate(CredentialManager.PROPERTY_TYPE,
                Query.FilterOperator.EQUAL, CredentialManager.TYPE_PASSWORD);
        Query.Filter filter = Query.CompositeFilterOperator.and(filterByEmail, filterByType);
        Query query = new Query(CredentialManager.KIND).setFilter(filter);

        String userKey = KeyFactory.keyToString(user.getKey());

        Entity tokenPasswordEntity = datastore.prepare(query).asSingleEntity();
        if (tokenPasswordEntity != null) {
            txn.rollback();
            return AuthenticationGeneralResource.sendEmailWithConditions(CredentialManager.TYPE_PASSWORD, user, userEmail, userKey, tokenPasswordEntity, LOG);
        } else {
            String randomId = Utils.generateRandomCode();
            Entity passwordRecoveryEntity = new Entity(CredentialManager.KIND);
            passwordRecoveryEntity.setIndexedProperty(CredentialManager.PROPERTY_ID, randomId);
            passwordRecoveryEntity.setIndexedProperty(CredentialManager.PROPERTY_USER_EMAIL, userEmail);
            passwordRecoveryEntity.setIndexedProperty(CredentialManager.PROPERTY_TYPE, CredentialManager.TYPE_PASSWORD);
            long currentTime = System.currentTimeMillis();
            passwordRecoveryEntity.setIndexedProperty(CredentialManager.PROPERTY_CREATION_DATE, currentTime);
            long expiryDate = System.currentTimeMillis() + Utils.EXPIRATION_TIME_PASSWORD_RECOVERY;
            passwordRecoveryEntity.setIndexedProperty(CredentialManager.PROPERTY_EXPIRY_DATE, expiryDate);

            datastore.put(txn, passwordRecoveryEntity);
            txn.commit();
            AuthenticationGeneralResource.sendEmail(CredentialManager.TYPE_PASSWORD, user, userEmail, userKey, randomId, LOG);
            return Response.ok().entity(g.toJson(Utils.OUTPUT_EMAIL_SENT)).build();
        }
    }


    //TODO: definir permissioes
    @POST
    @Path("/resetPassword/{id}/{code}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response resetPassword(@PathParam("id") String id, @PathParam("code") String code, String jsonString) {
        Key key = KeyFactory.stringToKey(id);
        LOG.warning(key.getName());
        Entity user = Utils.getUser(key.getName(), datastore, LOG);
        if (user == null) {
            LOG.warning(Utils.OUTPUT_MISSING_USER);
            return Response.status(Status.UNAUTHORIZED).entity(g.toJson(new StringUtil(Utils.OUTPUT_MISSING_USER))).build();
        }

        TransactionOptions options = TransactionOptions.Builder.withXG(true);
        Transaction txn = datastore.beginTransaction(options);

        // Builds the key using the provided email addressAsStreet.
        Query.Filter filterByEmail = new Query.FilterPredicate(CredentialManager.PROPERTY_USER_EMAIL,
                Query.FilterOperator.EQUAL, key.getName());
        Query.Filter filterByType = new Query.FilterPredicate(CredentialManager.PROPERTY_TYPE,
                Query.FilterOperator.EQUAL, CredentialManager.TYPE_PASSWORD);
        Query.Filter filter = Query.CompositeFilterOperator.and(filterByEmail, filterByType);
        Query query = new Query(CredentialManager.KIND).setFilter(filter);

        Entity tokenPasswordEntity = datastore.prepare(query).asSingleEntity();

        if (tokenPasswordEntity != null) {
            JsonElement jelement = new JsonParser().parse(jsonString);
            JsonObject jobject = jelement.getAsJsonObject();

            JsonElement jpassword = jobject.get("password");
            JsonElement jpassword_confirm = jobject.get("password_confirm");

            String password = g.fromJson(jpassword.toString(), String.class);
            String password_confirm = g.fromJson(jpassword_confirm.toString(), String.class);

            if (!password.equals(password_confirm)) {
                LOG.warning(Utils.OUTPUT_WRONG_FIELDS);
                return Response.status(Status.BAD_REQUEST).entity(g.toJson(new StringUtil(Utils.OUTPUT_WRONG_FIELDS))).build();
            }
            user.setProperty(User_Global.PROPERTY_PWD, DigestUtils.sha512Hex(password));

            datastore.put(txn, user);
            datastore.delete(tokenPasswordEntity.getKey());
            txn.commit();

            Cache cache;
            CacheManager manager;
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
                cache.put(key.getName(), user);
            } catch (CacheException e) {
                LOG.severe(Utils.CACHE_ERROR);
            }

            return Response.status(Status.OK).entity(g.toJson(new StringUtil("password alterada com sucesso"))).build();

        } else {
            txn.rollback();
            //TODO: LOG
            return Response.status(Status.METHOD_NOT_ALLOWED).entity(g.toJson(new StringUtil("expirou o passwordToken"))).build();
        }


    }

    //TODO: definir permissioes e TESTAR
    @POST
    @Path("/changePassword")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changePassword(String jsonString) {

        JsonElement jelement = new JsonParser().parse(jsonString);
        JsonObject jobject = jelement.getAsJsonObject();

        JsonObject jtoken = jobject.getAsJsonObject("token");
        JsonElement jpassword = jobject.get("password");
        JsonElement jpassword_confirm = jobject.get("password_confirm");

        AuthToken token = g.fromJson(jtoken.toString(), AuthToken.class);
        String password = g.fromJson(jpassword.toString(), String.class);
        String password_confirm = g.fromJson(jpassword_confirm.toString(), String.class);


        /* Security Validations */
        Entity user = Utils.getUser(token.user, datastore, LOG);
        if (user == null) {
            LOG.warning(Utils.OUTPUT_MISSING_USER);
            return Response.status(Status.UNAUTHORIZED).entity(g.toJson(new StringUtil(Utils.OUTPUT_MISSING_USER))).build();
        }
        Response rsp = Utils.securityValidations(user, token, ResourcesEnum.GENERAL_CHANGE_PASSWORD, datastore,
                LOG);
        if (rsp.getStatus() != Status.ACCEPTED.getStatusCode())
            return rsp;
        /* Security Validations */

        if (!password.equals(password_confirm)) {
            LOG.warning(Utils.OUTPUT_WRONG_FIELDS);
            return Response.status(Status.BAD_REQUEST).entity(g.toJson(new StringUtil(Utils.OUTPUT_WRONG_FIELDS))).build();
        }

        Transaction txn = datastore.beginTransaction();
        user.setProperty(User_Global.PROPERTY_PWD, DigestUtils.sha512Hex(password));
        datastore.put(txn, user);
        txn.commit();
        Cache cache;
        CacheManager manager;
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
            cache.put(token.user, user);

        } catch (CacheException e) {
            LOG.severe(Utils.CACHE_ERROR);
        }


        LOG.info("Password changed");
        return Response.ok().build();
    }

}