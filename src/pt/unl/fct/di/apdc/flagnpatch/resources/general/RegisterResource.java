package pt.unl.fct.di.apdc.flagnpatch.resources.general;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.memcache.stdimpl.GCacheFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.users.User;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import pt.unl.fct.di.apdc.flagnpatch.entities.*;
import pt.unl.fct.di.apdc.flagnpatch.inputData.*;
import pt.unl.fct.di.apdc.flagnpatch.utils.*;
import org.apache.commons.validator.routines.EmailValidator;


import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


@Path("/register")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RegisterResource {

    // Logger object
    private static final Logger LOG = Logger.getLogger(RegisterResource.class.getName());
    // Datastore link
    private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    // Converts objects to JSON

    static Gson g = new Gson();

    static Queue queue = QueueFactory.getDefaultQueue();

    private Cache cache;
    private CacheManager manager;

    // Empty constructor for code 'correctness'
    public RegisterResource() {
    }

    @POST
    @Path("/trial")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerTrialUser(RegisterData_FrontOffice data) {
        LOG.info("Attempting to register a TRIAL user: " + data.email);
        Response rsp = registerUser(data, RolesEnum.TRIAL_USER);
        return rsp;
    }

    @POST
    @Path("/worker")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerWorkUser(RegisterData_BackOffice data) {
        LOG.info("Attempting to register a WORK user: " + data.email);
        return registerUser(data, RolesEnum.WORKER_USER);
    }

    @POST
    @Path("/core")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerCoreUser(RegisterData_Administration data) {
        LOG.info("Attempting to register a CORE user: " + data.email);

        // TODO:Use this ->securityValidations(); or not
        if (!data.validRegistration()) {
            LOG.warning(Utils.OUTPUT_WRONG_FIELDS);
            return Response.status(Status.BAD_REQUEST).entity(g.toJson(new StringUtil(Utils.OUTPUT_WRONG_FIELDS))).build();
        }

        Key userKey = KeyFactory.createKey(User_Global.KIND, data.email);

        Filter filterByEmail = new FilterPredicate(
                Entity.KEY_RESERVED_PROPERTY, FilterOperator.EQUAL, userKey);
        Filter filterByCounty = new FilterPredicate(
                "address.county", FilterOperator.EQUAL, data.address.county);
        Filter filterByRole = new FilterPredicate(
                User_Global.PROPERTY_ROLE, FilterOperator.EQUAL, RolesEnum.CORE_USER.getRoleDescription());
        Query query = new Query(User_Global.KIND).setFilter(
                CompositeFilterOperator.or(filterByEmail, CompositeFilterOperator.and(filterByCounty, filterByRole)));
        Entity entity = null;
        try {
            entity = datastore.prepare(query).asSingleEntity();
            if (entity != null) {
                LOG.severe(Utils.OUTPUT_DATABASE_ERROR_CORE_USERS);
                return Response.status(Status.CONFLICT).entity(
                        g.toJson(new StringUtil(Utils.OUTPUT_DATABASE_ERROR_CORE_USERS))).build();
            }
        } catch (PreparedQuery.TooManyResultsException e) {
            LOG.severe(Utils.OUTPUT_DATABASE_ERROR_CORE_USERS);
            return Response.status(Status.CONFLICT).entity(
                    g.toJson(new StringUtil(Utils.OUTPUT_DATABASE_ERROR_CORE_USERS))).build();
        }
        // TODO:Use this ->securityValidations(); or not
        return registerUser(data, RolesEnum.CORE_USER);

    }

    // Added recently
    @POST
    @Path("/end")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerEndUser(String jsonString) {
        //RegisterData_Administration data

        JsonElement jelement = new JsonParser().parse(jsonString);
        JsonObject jobject = jelement.getAsJsonObject();

        JsonObject jtoken = jobject.getAsJsonObject("token");
        JsonObject jdata = jobject.getAsJsonObject("data");

        AuthToken token = g.fromJson(jtoken.toString(), AuthToken.class);

        Entity user = Utils.getUser(token.user, datastore, LOG);
        if (user == null) {
            LOG.warning(Utils.OUTPUT_MISSING_USER);
            return Response.status(Status.UNAUTHORIZED).entity(g.toJson(new StringUtil(Utils.OUTPUT_MISSING_USER))).build();
        }
        Response rsp = Utils.securityValidations(user, token, ResourcesEnum.ADMINISTRATION_REGISTER_END, datastore, LOG);
        if (rsp.getStatus() != Status.ACCEPTED.getStatusCode())
            return rsp;

        RegisterData_Administration data = g.fromJson(jdata.toString(), RegisterData_Administration.class);
        LOG.info("Attempting to register an END user: " + data.email);

        EmbeddedEntity addrEntity = (EmbeddedEntity) user.getProperty(User_Global.PROPERTY_ADDR);
        String district = (String) addrEntity.getProperty(AddressData.PROPERTY_DISTRICT);
        String county = (String) addrEntity.getProperty(AddressData.PROPERTY_COUNTY);
        data.address.district = district;
        data.address.county = county;

        return registerUser(data, RolesEnum.END_USER);

    }

    @SuppressWarnings("incomplete-switch")
    public static final Response registerUser(Object dataIn, RolesEnum role) {


        System.out.println("general register user was called.");

        String email = ((RegisterData_Global) dataIn).email;
        RegisterData_FrontOffice frontOfficeObject = null;
        RegisterData_BackOffice backOfficeObject = null;
        RegisterData_Administration administrationObject = null;

        // Cast the object to the correct type &&
        // Checks whether the registration data is valid.
        // If not, terminates execution and replies to the client with
        // BAD_REQUEST.
        boolean failed = false;

        if (dataIn instanceof RegisterData_FrontOffice) {
            frontOfficeObject = (RegisterData_FrontOffice) dataIn;
            if (!frontOfficeObject.validRegistrationForFrontOffice())
                failed = true;

        } else if (dataIn instanceof RegisterData_BackOffice) {
            backOfficeObject = (RegisterData_BackOffice) dataIn;
            if (!backOfficeObject.validRegistration())
                failed = true;

        } else if (dataIn instanceof RegisterData_Administration) {
            administrationObject = (RegisterData_Administration) dataIn;
            if (!administrationObject.validRegistration())
                failed = true;
        }

        if (failed) {
            // TODO: change message
            LOG.warning("Did not register user " + email + " because the submitted data was invalid.");
            return Response.status(Status.BAD_REQUEST).entity(g.toJson(new StringUtil(Utils.OUTPUT_WRONG_FIELDS))).build();
        }


        // Begins a transaction.
        TransactionOptions options = TransactionOptions.Builder.withXG(true);
        Transaction txn = datastore.beginTransaction(options);

        try {
            // Builds the key using the provided email addressAsStreet.
            // Key userKey = KeyFactory.createKey(User.KIND, data.email);
            Key userKey = KeyFactory.createKey(User_Global.KIND, email);
            // Throws EntityNotFoundException if the provided email addressAsStreet is
            // not in use.
            @SuppressWarnings("unused")
            Entity user = datastore.get(userKey);

            // If the provided email addressAsStreet is in use, terminates the
            // transaction.
            txn.rollback();
            LOG.warning(Utils.OUTPUT_USER_ALREADY_EXISTS + " " + email);
            return Response.status(Status.CONFLICT).entity(g.toJson(new StringUtil(Utils.OUTPUT_USER_ALREADY_EXISTS))).build();

        } catch (EntityNotFoundException e) {

            Entity user = null;
            switch (role) {
                case TRIAL_USER: {
                    user = Utils.buildEntityUser_Trial(frontOfficeObject);
                }
                break;
                case WORKER_USER: {
                    user = Utils.buildEntityUser_Worker(backOfficeObject);
                }
                break;
                case CORE_USER: {
                    user = Utils.buildEntityUser_Administration(administrationObject);
                    Utils.buildDefaultTypesForCore(user, txn, datastore, g, LOG);

                    Areas area = new Areas(administrationObject.email, administrationObject.address.county);
                    Entity areaEntity = new Entity(Areas.KIND, area.county);
                    areaEntity.setIndexedProperty(Areas.PROPERTY_RESPONSIBLE, area.responsible);
                    areaEntity.setIndexedProperty(Areas.PROPERTY_COUNTY, area.county);
                    areaEntity.setIndexedProperty(Areas.PROPERTY_CREATION_DATE, area.creationDate);
                    areaEntity.setIndexedProperty(Areas.PROPERTY_IS_AVAILABLE, area.isAvailable);
                    datastore.put(txn, areaEntity);

                    Graph graph = new Graph(area.county, true, System.currentTimeMillis(), false);
                    Entity graphEntity = new Entity(Graph.KIND);
                    graphEntity.setIndexedProperty(Graph.PROPERTY_ACTIVE, graph.active);
                    graphEntity.setIndexedProperty(Graph.PROPERTY_COUNTY, graph.county);
                    graphEntity.setIndexedProperty(Graph.PROPERTY_DATE_DAY, graph.date);
                    graphEntity.setIndexedProperty(Graph.PROPERTY_TOTAL_EVER, graph.isTotal);

                    // Sets the entity's properties.
                    for (TypeEnum typeEnum : TypeEnum.values()) {
                        graphEntity.setIndexedProperty(typeEnum.getTypeDescription(), 0);

                    }
                    for (StatusEnum statusEnum : StatusEnum.values()) {
                        graphEntity.setIndexedProperty(statusEnum.getStatusDescription(), 0);
                    }

                    datastore.put(txn, graphEntity);

                    graph = new Graph(area.county, false, System.currentTimeMillis(), true);
                    graphEntity = new Entity(Graph.KIND);
                    graphEntity.setIndexedProperty(Graph.PROPERTY_ACTIVE, graph.active);
                    graphEntity.setIndexedProperty(Graph.PROPERTY_COUNTY, graph.county);
                    graphEntity.setIndexedProperty(Graph.PROPERTY_DATE_DAY, graph.date);
                    graphEntity.setIndexedProperty(Graph.PROPERTY_TOTAL_EVER, graph.isTotal);
                    // Sets the entity's properties.
                    for (TypeEnum typeEnum : TypeEnum.values()) {
                        graphEntity.setIndexedProperty(typeEnum.getTypeDescription(), 0);

                    }
                    for (StatusEnum statusEnum : StatusEnum.values()) {
                        graphEntity.setIndexedProperty(statusEnum.getStatusDescription(), 0);
                    }

                    datastore.put(txn, graphEntity);
                }
                break;
                case END_USER: {
                    administrationObject.password = administrationObject.password_confirm = "63gfe8fb7b43bf78w";
                    user = Utils.buildEntityUser_End(administrationObject);
                }
                break;
            }
            datastore.put(txn, user);


            // added recently
              if (!role.equals(RolesEnum.END_USER)) {
                  queue.add(TaskOptions.Builder.withUrl("/rest/register/sendEmailExtra").param("user", "null")
                          .param("userEmail", email).header("Content-Type", "application/html; charset=utf8"));
              }
            if (role.equals(RolesEnum.END_USER) || role.equals(RolesEnum.WORKER_USER)) {
                queue.add(TaskOptions.Builder.withUrl("/rest/register/resetPasswordExtra").etaMillis(System.currentTimeMillis() + 15000)
                        .param("userEmail", email).header("Content-Type", "application/html; charset=utf8"));
            }
            //   } else {
            //       queue.add(TaskOptions.Builder.withUrl("/rest/register/endCredential").param("user", "null")
            //               .param("userEmail", email).header("Content-Type", "application/html; charset=utf8"));
            //   }

            txn.commit();
            //sendConfirmationEmail(user, email);
            // Informs the client that the operation finished successfully.
            LOG.info(Utils.OUTPUT_USER_REGISTERED + " " + email);
            return Response.ok(g.toJson(new StringUtil(Utils.OUTPUT_USER_REGISTERED))).build();
        } finally {
            // Checks if something went wrong and the transaction is still
            // active.
            if (txn.isActive()) {
                txn.rollback();
                LOG.severe(Utils.OUTPUT_TRANSACTION_STILL_ACTIVE);
                return Response.status(Status.INTERNAL_SERVER_ERROR)
                        .entity(g.toJson(new StringUtil(Utils.OUTPUT_TRANSACTION_STILL_ACTIVE))).build();
            }
        }
    }

    @POST
    @Path("/endCredential")
    public void sendEndUserCredentials(@Context HttpServletRequest httpServletRequest) {
        String user = httpServletRequest.getParameter("user");
        String userEmail = httpServletRequest.getParameter("userEmail");
        this.sendConfirmationEmail(null, userEmail);
        //AuthenticationGeneralResource.sendEndCredentials(userEmail, "gil");
    }

    @POST
    @Path("/sendEmailExtra")
    public void callSendConfirmationEmail(@Context HttpServletRequest httpServletRequest) {
        String user = httpServletRequest.getParameter("user");
        String userEmail = httpServletRequest.getParameter("userEmail");
        if (user.equalsIgnoreCase("null"))
            this.sendConfirmationEmail(null, userEmail);
    }

    @POST
    @Path("/resetPasswordExtra")
    public Response resetPasswordExtra(@Context HttpServletRequest httpServletRequest) {

        String userEmail = httpServletRequest.getParameter("userEmail");

        Entity user = Utils.getUser(userEmail, datastore, LOG);
        if (user == null) {
            LOG.warning(Utils.OUTPUT_MISSING_USER);
            return Response.status(Status.UNAUTHORIZED).entity(Utils.OUTPUT_MISSING_USER).build();
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
            return Response.ok().entity(g.toJson(new StringUtil(Utils.OUTPUT_EMAIL_SENT))).build();
        }
    }

    @POST
    @Path("/confirmation")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response retrieveConfirmationEmail(String jsonString) {
        JsonElement jelement = new JsonParser().parse(jsonString);
        JsonObject jobject = jelement.getAsJsonObject();
        JsonObject jtoken = jobject.getAsJsonObject("token");

        AuthToken token = g.fromJson(jtoken.toString(), AuthToken.class);

        /* Security Validations */
        Entity user = Utils.getUser(token.user, datastore, LOG);

        if (user == null) {
            LOG.warning(Utils.OUTPUT_MISSING_USER);
            return Response.status(Status.UNAUTHORIZED).entity(g.toJson(new StringUtil(Utils.OUTPUT_MISSING_USER))).build();
        }

        boolean isAccountVerified = (boolean) user.getProperty(User_Global.PROPERTY_ACCOUNT_VERIFIED);
        if (isAccountVerified)
            return Response.status(Status.NO_CONTENT).entity(g.toJson(new StringUtil(Utils.OUTPUT_EMAIL_ALREADY_VERIFIED))).build();

        // TODO: RESOURCES ENUM
        Response rsp = Utils.securityValidations(user, token, ResourcesEnum.GENERAL_RETRIEVE_CONFIRMATION_EMAIL, datastore,
                LOG);
        if (rsp.getStatus() != Status.ACCEPTED.getStatusCode())
            return rsp;
        /* Security Validations */


        return sendConfirmationEmail(user, token.user);
    }


    /*
    public Response sendEndUserEmail (Entity userIn, String userEmail){
        TransactionOptions options = TransactionOptions.Builder.withXG(true);
        Transaction txn = datastore.beginTransaction(options);

        boolean isAccountVerified = (boolean) userIn.getProperty(User_Global.PROPERTY_ACCOUNT_VERIFIED);

        if (!isAccountVerified) {

            // Builds the key using the provided email addressAsStreet.
            Filter filterByEmail = new FilterPredicate(CredentialManager.PROPERTY_USER_EMAIL,
                    FilterOperator.EQUAL, userEmail);
            Filter filterByType = new FilterPredicate(CredentialManager.PROPERTY_TYPE,
                    FilterOperator.EQUAL, CredentialManager.TYPE_EMAIL);
            Filter filter = CompositeFilterOperator.and(filterByEmail, filterByType);
            Query query = new Query(CredentialManager.KIND).setFilter(filter);

            Entity tokenEmailEntity = datastore.prepare(query).asSingleEntity();

            if (tokenEmailEntity != null) {
                txn.rollback();
                return AuthenticationGeneralResource.sendEmailWithConditions(user, userEmail, tokenEmailEntity, LOG);
            } else {
                String randomId = Utils.generateRandomCode();
                Entity newTokenEmailEntity = new Entity(CredentialManager.KIND);
                newTokenEmailEntity.setIndexedProperty(CredentialManager.PROPERTY_ID, randomId);
                newTokenEmailEntity.setIndexedProperty(CredentialManager.PROPERTY_USER_EMAIL, userEmail);
                newTokenEmailEntity.setIndexedProperty(CredentialManager.PROPERTY_TYPE, CredentialManager.TYPE_EMAIL);
                long currentTime = System.currentTimeMillis();
                newTokenEmailEntity.setIndexedProperty(CredentialManager.PROPERTY_CREATION_DATE, currentTime);
                long expiryDate = System.currentTimeMillis() + Utils.EXPIRATION_TIME_EMAIL_CONFIRMATION;
                newTokenEmailEntity.setIndexedProperty(CredentialManager.PROPERTY_EXPIRY_DATE, expiryDate);
                datastore.put(txn, newTokenEmailEntity);
                txn.commit();
                AuthenticationGeneralResource.sendEmail(user, userEmail, randomId);
                // TODO:OUTPUT a new email has been sent
                return Response.ok().build();
            }

        } else {
            // active.
            if (txn.isActive()) {
                txn.rollback();
            }
            LOG.warning(Utils.OUTPUT_EMAIL_ALREADY_VERIFIED);
            return Response.status(Status.NOT_MODIFIED).entity(g.toJson(new StringUtil(Utils.OUTPUT_EMAIL_ALREADY_VERIFIED))).build();
        }


    }*/


    // @POST
    //  @Path("/confirmation")
    // @Consumes(MediaType.APPLICATION_JSON)
    public Response sendConfirmationEmail(Entity userIn, String userEmail) {
        /*JsonElement jelement = new JsonParser().parse(jsonString);
        JsonObject jobject = jelement.getAsJsonObject();
        JsonElement jemail = jobject.get("email");
        String userEmail = g.fromJson(jemail.toString(), String.class);*/

        TransactionOptions options = TransactionOptions.Builder.withXG(true);
        Transaction txn = datastore.beginTransaction(options);
        Entity user;
        if (userIn == null) {
            LOG.info("USER is NULL");
            user = Utils.getUser(userEmail, datastore, LOG);
            if (user == null) {
                // FALTA ROLLBACK?
                LOG.warning(Utils.OUTPUT_MISSING_USER);
                return Response.status(Status.UNAUTHORIZED).entity(g.toJson(new StringUtil(Utils.OUTPUT_MISSING_USER))).build();
            }
        } else {
            user = userIn;
        }
        boolean isAccountVerified = (boolean) user.getProperty(User_Global.PROPERTY_ACCOUNT_VERIFIED);

        if (!isAccountVerified) {

            // Builds the key using the provided email addressAsStreet.
            Filter filterByEmail = new FilterPredicate(CredentialManager.PROPERTY_USER_EMAIL,
                    FilterOperator.EQUAL, userEmail);
            Filter filterByType = new FilterPredicate(CredentialManager.PROPERTY_TYPE,
                    FilterOperator.EQUAL, CredentialManager.TYPE_EMAIL);
            Filter filter = CompositeFilterOperator.and(filterByEmail, filterByType);
            Query query = new Query(CredentialManager.KIND).setFilter(filter);

            Entity tokenEmailEntity = datastore.prepare(query).asSingleEntity();
            String userKey = KeyFactory.keyToString(user.getKey());
            if (tokenEmailEntity != null) {
                txn.rollback();
                return AuthenticationGeneralResource.sendEmailWithConditions(CredentialManager.TYPE_EMAIL, user, userEmail, userKey, tokenEmailEntity, LOG);
            } else {
                String randomId = Utils.generateRandomCode();
                Entity newTokenEmailEntity = new Entity(CredentialManager.KIND);
                newTokenEmailEntity.setIndexedProperty(CredentialManager.PROPERTY_ID, randomId);
                newTokenEmailEntity.setIndexedProperty(CredentialManager.PROPERTY_USER_EMAIL, userEmail);
                newTokenEmailEntity.setIndexedProperty(CredentialManager.PROPERTY_TYPE, CredentialManager.TYPE_EMAIL);
                long currentTime = System.currentTimeMillis();
                newTokenEmailEntity.setIndexedProperty(CredentialManager.PROPERTY_CREATION_DATE, currentTime);
                long expiryDate = System.currentTimeMillis() + Utils.EXPIRATION_TIME_EMAIL_CONFIRMATION;
                newTokenEmailEntity.setIndexedProperty(CredentialManager.PROPERTY_EXPIRY_DATE, expiryDate);
                datastore.put(txn, newTokenEmailEntity);
                txn.commit();
                AuthenticationGeneralResource.sendEmail(CredentialManager.TYPE_EMAIL, user, userEmail, userKey, randomId, LOG);
                // TODO:OUTPUT a new email has been sent
                return Response.ok().build();
            }

        } else {
            // active.
            if (txn.isActive()) {
                txn.rollback();
            }
            LOG.warning(Utils.OUTPUT_EMAIL_ALREADY_VERIFIED);
            return Response.status(Status.NOT_MODIFIED).entity(g.toJson(new StringUtil(Utils.OUTPUT_EMAIL_ALREADY_VERIFIED))).build();
        }
    }

   /* private void sendEmail(Entity user, String userEmail, String randomId) throws Exception {
        String userName = (String) user.getProperty(User_Global.PROPERTY_NAME);
        // TODO: NÃO ESTÁ A ENVIAR EMAIL
        // Utils.sendNewEmail(userEmail, userName, Utils.CONFIRMATION_EMAIL_SUBJECT,
        //    new EmailUtil(userEmail, userName, gender, randomId, EmailsEnum.CONFIRMATION_EMAIL_HTML).getHtml());
        String url = Utils.CONFIRMATION_EMAIL_LINK + "/" + userEmail + "/" + randomId;
        System.out.println(url);
    }*/

    @GET
    @Path("/confirm/{id}/{code}")
    public Response confirmEmail(@PathParam("id") String id, @PathParam("code") String code) {

        Key key = KeyFactory.stringToKey(id);
        LOG.warning(key.getName());
        TransactionOptions options = TransactionOptions.Builder.withXG(true);
        Transaction txn = datastore.beginTransaction(options);
        // Builds the key using the provided email addressAsStreet.
        Filter filterByEmail = new FilterPredicate(CredentialManager.PROPERTY_USER_EMAIL,
                FilterOperator.EQUAL, key.getName());
        Filter filterByType = new FilterPredicate(CredentialManager.PROPERTY_TYPE,
                FilterOperator.EQUAL, CredentialManager.TYPE_EMAIL);
        Filter filterByRandomId = new FilterPredicate(CredentialManager.PROPERTY_ID,
                FilterOperator.EQUAL, code);
        Filter filter = CompositeFilterOperator.and(filterByEmail, filterByType, filterByRandomId);
        Query query = new Query(CredentialManager.KIND).setFilter(filter);

        Entity tokenEmailEntity = datastore.prepare(query).asSingleEntity();
        Entity user = Utils.getUser(key.getName(), datastore, LOG);
        if (user == null) {
            LOG.warning(Utils.OUTPUT_MISSING_USER);
            return Response.status(Status.UNAUTHORIZED).entity(g.toJson(new StringUtil(Utils.OUTPUT_MISSING_USER))).build();
        }

        if (tokenEmailEntity != null) {
            user.setProperty(User_Global.PROPERTY_ACCOUNT_VERIFIED, true);

            String role = (String) user.getProperty(User_Global.PROPERTY_ROLE);
            Entity utilsEntity;
            if (role.equalsIgnoreCase(RolesEnum.TRIAL_USER.getRoleDescription())) {
                user.setProperty(User_Global.PROPERTY_ROLE, RolesEnum.BASIC_USER.getRoleDescription());
                utilsEntity = getEntityUtilsEntity_Frontoffice();
                if (utilsEntity != null) {
                    long counterString = ((long) utilsEntity.getProperty(UtilsEntity.PROPERTY_NUMBER_USER));
                    int counter = new BigDecimal(counterString).intValueExact() + 1;
                    utilsEntity.setProperty(UtilsEntity.PROPERTY_NUMBER_USER, counter);
                    user.setProperty(User_FrontOffice.PROPERTY_IDENTIFIER, counter);
                    datastore.put(txn, utilsEntity);
                }
            }

            datastore.put(txn, user);
            datastore.delete(tokenEmailEntity.getKey());
            String email=user.getKey().getName();
            LOG.severe("oh gil "+key.getName());
            LOG.severe("dsdsds "+email);

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
                if(cache.containsKey(key.getName()))
                    cache.remove(key.getName());

              //  cache.put(key.getName(), user);
            } catch (CacheException e) {
                LOG.severe(Utils.CACHE_ERROR);
            }
            txn.commit();
            // TODO: "ok"
            return Response.status(Status.OK).build();
        } else {
            txn.rollback();
            boolean isVerified = (boolean) user.getProperty(User_Global.PROPERTY_ACCOUNT_VERIFIED);
            if (isVerified) {
                // TODO: Response status
                return Response.status(Status.UNAUTHORIZED).entity(g.toJson(new StringUtil("ja tava verificado"))).build();
                //return Response.status(Status.METHOD_NOT_ALLOWED).entity("send confirm").build();
            } else {
                //sendConfirmationEmail(user, userEmail);
                return Response.status(Status.METHOD_NOT_ALLOWED).entity(g.toJson(new StringUtil("dados incorretos"))).build();
            }
        }
    }

    private Entity getEntityUtilsEntity_Frontoffice() {
        Key frontOfficeKey = KeyFactory.createKey(UtilsEntity.KIND, UtilsEntity.KEY_FRONTOFFICE);
        try {
            return datastore.get(frontOfficeKey);
        } catch (EntityNotFoundException e) {
            LOG.severe("Default int generator");
            return null;
        }
    }
}

