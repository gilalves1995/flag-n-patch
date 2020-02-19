package pt.unl.fct.di.apdc.flagnpatch.resources.general;


import com.google.appengine.api.datastore.*;
import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.memcache.stdimpl.GCacheFactory;
import com.google.gson.Gson;
import com.sendgrid.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import pt.unl.fct.di.apdc.flagnpatch.entities.*;

import pt.unl.fct.di.apdc.flagnpatch.inputData.*;
import pt.unl.fct.di.apdc.flagnpatch.utils.ResourcesEnum;
import pt.unl.fct.di.apdc.flagnpatch.utils.RolesEnum;

import pt.unl.fct.di.apdc.flagnpatch.utils.StringUtil;
import pt.unl.fct.di.apdc.flagnpatch.utils.TypeEnum;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.*;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;



public class Utils {

    public static final int REPORTS_CURSOR_LIMIT = 15;
    public static final String CACHE_ERROR = "Cache Error";
    public static final String DEFAULT_IMAGE = "\t\n" +
            "/img/missing.png";

    public static final int IMAGE_POINTS = 25;


    public static final String OUTPUT_WRONG_FIELDS = "Missing or wrong fields were introduced.";
    public static final String OUTPUT_MISSING_REPORT = "Report was not found.";
    public static final String OUTPUT_MISSING_LIST_REPORTS = "No reports were found in the database.";
    public static final String OUTPUT_MISSING_LIST_COMMENTS = "No comments were found in the database.";
    public static final String OUTPUT_STATUS_REGISTERED = "New status registered with success.";
    public static final String OUTPUT_NOT_FOLLOWING_REPORT = "Error: User is not following this report.";

    public static final String OUTPUT_NO_MORE_REPORTS_TRIAL = "Tem de verificar a conta para poder criar ocorrências.";

    public static final String OUTPUT_REPEATED_REPORT = "Multiple reports found.";
    public static final String OUTPUT_REPORT_REGISTERED = "Report registered with success.";
    public static final String OUTPUT_USER_ALREADY_EXISTS = "Email provided already in use";
    public static final String OUTPUT_USER_REGISTERED = "User registered with success.";
    public static final String OUTPUT_COMMENT_REGISTERED = "Comment registered with success.";
    public static final String OUTPUT_REPORT_ALREADY_FOLLOWED = "User already following report.";
    public static final String OUTPUT_EMAIL_ALREADY_VERIFIED = "User email already verified.";
    public static final String OUTPUT_ADDED_SUBSCRIBER = "New subscriber added to the report.";
    public static final String OUTPUT_TRANSACTION_STILL_ACTIVE = "The transaction is still active.";
    public static final String OUTPUT_TRANSACTION_FAILED = "The transaction failed. Rolling back.";
    public static final String DATABASE_USER_REPORT_LOG = "Report submited by user.";
    public static final String OUTPUT_STOPPED_FOLLOWING = "User stopped following this report.";
    public static final String OUTPUT_MISSING_USER = "User was not found.";
    public static final String OUTPUT_TYPE_MISSING = "Provided type does not exist.";

    public static final long EXPIRATION_TIME_EMAIL_CONFIRMATION = 1000 * 60 * 60 * 120; //5days
    public static final long EXPIRATION_TIME_PASSWORD_RECOVERY = 1000 * 60 * 60 * 4; //4h
    public static final String OUTPUT_PASSWORD_RECOVERY_OK = "We sent you an email, p";
    protected static final String OUTPUT_USER_LOGGED_IN = "User logged in with success.";
    protected static final String OUTPUT_USER_LOGGED_OUT = "User logged out with success.";
    protected static final String OUTPUT_TOKEN_INVALID = "Token is invalid, user required to login again.";
    public static final String RECOVER_PASSWORD_LINK = "https://flag-n-patch.appspot.com/newpassword";
    public static final String CONFIRMATION_EMAIL_LINK = "https://flag-n-patch.appspot.com/confirm";
    private static final String OUTPUT_TOKEN_EXPIRED = "Token has expired, user required to login again.";
    public static final String OUTPUT_NO_PERMISSIONS = "The user does not have the necessary permissions to the operation.";
    public static final String HELP_EMAIL = "no-reply@flag-n-patch.appspotmail.com";
    public static final String ENTITY_ADDRESSES = "Addresses";
    public static final String OUTPUT_EMAIL_NOT_EXPIRED = "You recieved an email a short time ago, please check your mailbox.";
    public static final String OUTPUT_EMAIL_SENT = "We send you an email, please check your mailbox.";
    public static final String OUTPUT_DEFAULT_TYPES = "Sucessfully added the default types as core creator";
    public static final String OUTPUT_NO_DATABASE = "No initial database setup";
    //public static final String AREA_NOT_AVAILABLE="Report on this area are unavailable.";
    public static final String OUTPUT_BAD_CREDENTIALS = "Invalid email or password.";
    public static final String OUTPUT_NEED_CONFIRM_EMAIL = "This user, because of its type, needs to confirm email before login.";
    public static final String OUTPUT_TOO_MANY_REPORT_TYPES_FOR_COUNTY = "Database error: Too many ReportTypes for the county.";
    public static final String OUTPUT_DATABASE_ERROR_CORE_USERS = "Database error: Already exists a user CORE associated to the county.";
    public static final String OUTPUT_SAME_REPORT_STATUS_AS_BEFORE = "You cannot set the same status.";
    public static final String CONFIRMATION_EMAIL_SUBJECT = "Confirmação de Email - Flag N' Patch";
    public static final String PASSWORD_RECOVERY_SUBJECT = "Recuperação de Password - Flag N' Patch";
    public static final String CHANGE_OF_WORK_TYPE_SUBJECT = "Alteração na Área de Trabalho - Flag N' Patch";
    public static final String OUTPUT_NO_DATA_FOR_GRAPH = "Não existem dados para o dia selecionado.";
    // 401 means unauthenticated you need valid credencials for me to
    // respond to you
    // 403 means unauthorized i understood your credencials but so sorry you are
    // not allowed

    /**
     * Validates the user token. If exists on database and if it does not
     * expired yet returns HTTP 202 code (ACCEPTED), else returns HTTP 401 code
     * (UNAUTHORIZED)
     * //TODO: change description  and vars
     *
     * @param token Token received by input
     * @param key   User key associated with token received by input
     * @return Response status, as HTTP 202 (ACCEPTED) or 401 code
     * (UNAUTHORIZED)
     */

    private static Cache cache;
    private static CacheManager manager;

    public static Response validateToken(AuthToken token, Key userKey, DatastoreService datastore, Logger LOG) {
        // Token validation
        final Gson g = new Gson();
        // Throws exception if entity does not exist
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
            if (cache.containsKey(token.id)) {
                if (token.expirationDate <= System.currentTimeMillis()) {
                    // If token has already expired, it returns HTTP 401 code
                    LOG.warning(OUTPUT_TOKEN_EXPIRED);
                    return Response.status(Status.UNAUTHORIZED).entity(g.toJson(new StringUtil(OUTPUT_TOKEN_EXPIRED))).build();
                }
                // Else, if everything is OK returns HTTP 202 code
                return Response.status(Status.ACCEPTED).build();
            } else {
                LOG.warning("Token not in cache");
                Key tokenKey = KeyFactory.createKey(userKey, AuthToken.KIND, token.id);

                try {
                    @SuppressWarnings("unused")
                    Entity tokenEntity = datastore.get(tokenKey);

                    // Checks if token has expired
                    if (token.expirationDate <= System.currentTimeMillis()) {
                        // If token has already expired, it returns HTTP 401 code
                        LOG.warning(OUTPUT_TOKEN_EXPIRED);
                        return Response.status(Status.UNAUTHORIZED).entity(g.toJson(new StringUtil(OUTPUT_TOKEN_EXPIRED))).build();

                    }
                    // Else, if everything is OK returns HTTP 202 code
                    return Response.status(Status.ACCEPTED).build();
                } catch (EntityNotFoundException e) {
                    // If user does not exist on database, it returns HTTP 401 code
                    LOG.warning(OUTPUT_TOKEN_INVALID);
                    return Response.status(Status.UNAUTHORIZED).entity(g.toJson(new StringUtil(OUTPUT_TOKEN_INVALID))).build();

                }
            }

        } catch (CacheException e) {
            LOG.severe(Utils.CACHE_ERROR);
            Key tokenKey = KeyFactory.createKey(userKey, AuthToken.KIND, token.id);

            try {
                @SuppressWarnings("unused")
                Entity tokenEntity = datastore.get(tokenKey);

                // Checks if token has expired
                if (token.expirationDate <= System.currentTimeMillis()) {
                    // If token has already expired, it returns HTTP 401 code
                    LOG.warning(OUTPUT_TOKEN_EXPIRED);
                    return Response.status(Status.UNAUTHORIZED).entity(g.toJson(new StringUtil(OUTPUT_TOKEN_EXPIRED))).build();

                }
                // Else, if everything is OK returns HTTP 202 code
                return Response.status(Status.ACCEPTED).build();
            } catch (EntityNotFoundException e1) {
                // If user does not exist on database, it returns HTTP 401 code
                LOG.warning(OUTPUT_TOKEN_INVALID);
                return Response.status(Status.UNAUTHORIZED).entity(g.toJson(new StringUtil(OUTPUT_TOKEN_INVALID))).build();

            }
        }
    }

    /**
     * Validates the user email. If exists on database it returns HTTP 202 code
     * (ACCEPTED), else returns HTTP 401 code (UNAUTHORIZED)
     * // TODO: change description  and vars
     *
     * @param user User key associated with token received by input
     * @return Response status, as HTTP 202 (ACCEPTED) or 401 code
     * (UNAUTHORIZED)
     */
    public static Response validateUser(Entity user, DatastoreService datastore, Logger LOG, ResourcesEnum resource) {
        // User validation
        final Gson g = new Gson();
        String userRole = (String) user.getProperty(User_Global.PROPERTY_ROLE);
        return Utils.checkPermissionsMemCache(datastore, LOG, userRole, resource);
    }

    // TODO: stupid thing
    public static Entity getUser(String userEmail, DatastoreService datastore, Logger LOG) {
        if (userEmail == null)
            return null;
        // User validation

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

            if (cache.containsKey(userEmail)) {
                return (Entity) cache.get(userEmail);
            } else {
                LOG.warning("User entity not in cache");
                Key key = KeyFactory.createKey(User_Global.KIND, userEmail);
                final Gson g = new Gson();
                // Throws EntityNotFoundException if the email does not exist
                try {
                    Entity entity = datastore.get(key);
                    cache.put(userEmail, entity);
                    return entity;
                } catch (EntityNotFoundException e) {
                    return null;
                }
            }
        } catch (CacheException e) {
            LOG.severe(CACHE_ERROR);
            Key key = KeyFactory.createKey(User_Global.KIND, userEmail);
            final Gson g = new Gson();
            // Throws EntityNotFoundException if the email does not exist
            try {
                return datastore.get(key);
            } catch (EntityNotFoundException e1) {
                return null;
            }
        }
    }

    public static int calcAveragePriority(int totalSumBefore, int numFollowers, int priority, boolean addValue) {
        if (numFollowers == 1)
            return priority;


        if (addValue)
            return (int) Math.ceil((totalSumBefore + priority) / (numFollowers + 1));
        else
            return (int) Math.ceil((totalSumBefore - priority) / (numFollowers - 1));
        //return oldPriority * (n - 1) / n + newPriority / n;
    }

   /* public static int countNewPointsForPriority(int oldPoints, int avgPriority, int) {

    }*/

    /**
     * Verifies if the user has permission to execute certain operation, with
     * the resources permitted
     * //TODO: change description  and vars
     *
     * @param userRole List with the resources of the user
     *                 //@param permissions List with the resources permissions admitted by certain operation
     * @return boolean with true, if user can execute the operation, or false,
     * if he cannot execute operation
     */
    /*private static Response checkPermissions(DatastoreService datastore, Logger LOG,
                                             String userRole, ResourcesEnum resource) {
        Gson g = new Gson();
        Key roleKey = KeyFactory.createKey(Role.KIND, userRole);
        try {
            Entity role = datastore.get(roleKey);

            ArrayList<String> resourcesFromRole = (ArrayList<String>) role.getProperty(Role.PROPERTY_RESOURCES);
            final Iterator<String> resourcesFromRoleIterator = resourcesFromRole.iterator();
            while (resourcesFromRoleIterator.hasNext()) {
                String resourceString = resourcesFromRoleIterator.next();
                if (resourceString.equalsIgnoreCase(resource.getResourceName()))
                    return Response.status(Status.ACCEPTED).build();
            }
            return Response.status(Status.FORBIDDEN).entity(g.toJson(new StringUtil(OUTPUT_NO_PERMISSIONS))).build();
            //  return Response.
        } catch (EntityNotFoundException e) {
            // TODO: change outputs
            // If user does not exist on database, it returns HTTP 401 code
            LOG.severe(OUTPUT_NO_DATABASE);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(g.toJson(new StringUtil(OUTPUT_NO_DATABASE))).build();
        }
        // return Response.status(Status.FORBIDDEN).entity(g.toJson(new StringUtil(OUTPUT_NO_PERMISSIONS)).build();

    }*/
    public static byte[] serializeObject(Object object) {
        try {
            // Serialize data object to a file
            ObjectOutputStream out;
            // Serialize data object to a byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            out = new ObjectOutputStream(bos);
            out.writeObject(object);
            out.close();

            // Get the bytes of the serialized object
            return bos.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }

    public static Object deSerializeObject(byte[] byteArray) {
        try {
            // Serialize data object to a file
            ObjectInputStream in;
            // Serialize data object to a byte array
            ByteArrayInputStream bos = new ByteArrayInputStream(byteArray);
            in = new ObjectInputStream(bos);

            Object object = in.readObject();
            in.close();
            // Get the bytes of the serialized object
            return object;
        } catch (IOException e) {
            return null;

        } catch (ClassNotFoundException c) {
            return null;
        }
    }


    private static Response checkPermissionsMemCache(DatastoreService datastore, Logger LOG,
                                                     String userRole, ResourcesEnum resource) {

        Gson g = new Gson();
        ArrayList<String> resourcesFromRole;
        Iterator<String> resourcesFromRoleIterator;

        try {
            manager = CacheManager.getInstance();
            cache = manager.getCache("permissions");
            if (cache == null) {
                CacheFactory cacheFactory = manager.getCacheFactory();
                cache = cacheFactory.createCache(Collections.emptyMap());
                manager.registerCache("permissions", cache);
            }

            if (cache.containsKey(userRole)) {
                resourcesFromRole = (ArrayList<String>) cache.get(userRole);
                resourcesFromRoleIterator = resourcesFromRole.iterator();
            } else {
                LOG.warning(userRole + " permissions not in cache");
                Key roleKey = KeyFactory.createKey(Role.KIND, userRole);
                try {
                    Entity role = datastore.get(roleKey);
                    resourcesFromRole = (ArrayList<String>) role.getProperty(Role.PROPERTY_RESOURCES);
                    cache.put(userRole, resourcesFromRole);
                    resourcesFromRoleIterator = resourcesFromRole.iterator();
                } catch (EntityNotFoundException e) {
                    // TODO: change outputs
                    // If user does not exist on database, it returns HTTP 401 code
                    LOG.severe(OUTPUT_NO_DATABASE);
                    return Response.status(Status.INTERNAL_SERVER_ERROR).entity(g.toJson(new StringUtil(OUTPUT_NO_DATABASE))).build();
                }
            }

        } catch (CacheException e) {
            LOG.severe(CACHE_ERROR);
            Key roleKey = KeyFactory.createKey(Role.KIND, userRole);
            try {
                Entity role = datastore.get(roleKey);
                resourcesFromRole = (ArrayList<String>) role.getProperty(Role.PROPERTY_RESOURCES);
                resourcesFromRoleIterator = resourcesFromRole.iterator();
            } catch (EntityNotFoundException e1) {
                // TODO: change outputs
                // If user does not exist on database, it returns HTTP 401 code
                LOG.severe(OUTPUT_NO_DATABASE);
                return Response.status(Status.INTERNAL_SERVER_ERROR).entity(g.toJson(new StringUtil(OUTPUT_NO_DATABASE))).build();
            }
        }

        while (resourcesFromRoleIterator.hasNext()) {
            String resourceString = resourcesFromRoleIterator.next();
            if (resourceString.equalsIgnoreCase(resource.getResourceName()))
                return Response.status(Status.ACCEPTED).build();
        }
        return Response.status(Status.FORBIDDEN).entity(g.toJson(new StringUtil(OUTPUT_NO_PERMISSIONS))).build();
/*

        Gson g = new Gson();
        Key roleKey = KeyFactory.createKey(Role.KIND, userRole);
        try {
            Entity role = datastore.get(roleKey);

            ArrayList<String> resourcesFromRole = (ArrayList<String>) role.getProperty(Role.PROPERTY_RESOURCES);
            final Iterator<String> resourcesFromRoleIterator = resourcesFromRole.iterator();
            while (resourcesFromRoleIterator.hasNext()) {
                String resourceString = resourcesFromRoleIterator.next();
                if (resourceString.equalsIgnoreCase(resource.getResourceName()))
                    return Response.status(Status.ACCEPTED).build();
            }
            return Response.status(Status.FORBIDDEN).entity(g.toJson(new StringUtil(OUTPUT_NO_PERMISSIONS))).build();
            //  return Response.
        } catch (EntityNotFoundException e) {
            // TODO: change outputs
            // If user does not exist on database, it returns HTTP 401 code
            LOG.severe(OUTPUT_NO_DATABASE);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(g.toJson(new StringUtil(OUTPUT_NO_DATABASE))).build();
        }
        // return Response.status(Status.FORBIDDEN).entity(g.toJson(new StringUtil(OUTPUT_NO_PERMISSIONS)).build();
*/
    }

    /**
     * Used for the creation of the StatusLog Entity to be introduce into
     * database (code refactoring)
     *
     * @param statusDescription The description of the new status of report
     * @param description       Description of the status change
     * @param reportKey         Report key associated with the report that will be created a
     *                          new log
     * @param user              User email associated with token received by input
     * @return Object that includes the StatusLogEntity entity and the StatusLog
     * object
     */
    public static Object[] newStatusLogObject(String statusDescription, String description, Key reportKey,
                                              String user) {
        // creates object StatusLog with data for the entity
        String date = generateCustomDate();
        StatusLog stLog = new StatusLog(String.valueOf(reportKey.getId()), statusDescription, description, System.currentTimeMillis(), date, user);

        // creates a new entity StatusLogEntity, to log all the changes, using
        // the previous object
        Entity statusLogEntity = new Entity(StatusLog.KIND, reportKey);
        statusLogEntity.setIndexedProperty(StatusLog.PROPERTY_REPORT_ID, stLog.reportId);
        statusLogEntity.setIndexedProperty(StatusLog.PROPERTY_STATUS_DESCRIPTION, stLog.statusDescription);
        statusLogEntity.setIndexedProperty(StatusLog.PROPERTY_DESCRIPTION, stLog.description);
        statusLogEntity.setIndexedProperty(StatusLog.PROPERTY_MODIFIED_TIME, stLog.modifiedTime);
        statusLogEntity.setIndexedProperty(StatusLog.PROPERTY_MODIFIED_DATE, stLog.modifiedDate);
        statusLogEntity.setIndexedProperty(StatusLog.PROPERTY_MODIFIED_BY, stLog.modifiedBy);
        return new Object[]{statusLogEntity, stLog};
    }

    /**
     * Used for retrieve current date with a custom format
     *
     * @return custom date as String
     */
    public static String generateCustomDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Lisbon"));
        return sdf.format(new Date());
    }

    /**
     * Used to send and email of password recovery or email confirmation
     *
     * @param userEmail The user email
     *                  <<<<<<< HEAD
     * @throws MessagingException           Exception
     * @throws UnsupportedEncodingException Exception
     */
    public static void sendNewEmail(String userEmail, String userName, String subject, String contentHtml) {

        //subject = "Sending with SendGrid is Fun";
        com.sendgrid.Email from = new com.sendgrid.Email(Utils.HELP_EMAIL, "Flag N' Patch");
        com.sendgrid.Email to = new com.sendgrid.Email(userEmail, userName);
        Content content = new Content("text/html", contentHtml);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid("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");

        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            //  request.
            request.setBody(mail.build());
            sg.api(request);
        } catch (IOException ex) {
        }

    }

    public static void sendEmail(String userEmail, String userName, String subject, String contentHtml) {
        Properties props = new Properties();
        Session mailSession = Session.getDefaultInstance(props, null);
        MimeMessage msg = new MimeMessage(mailSession);
        String email = Utils.HELP_EMAIL;
        try {
            msg.setFrom(new InternetAddress(email, "Flag n' Patch"));
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(userEmail));
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        try {
            msg.setSubject(subject, "UTF-8");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        try {
            msg.setContent(contentHtml, "text/html; charset=UTF-8");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
       /* msg.setText(msgText +
                        "\n\n"
                + "Relembramos que o seu email e: " + userEmail + "\n\n"
                + "Aceda a " + hyperlink +
                "?email=" + userEmail + "&" + "id=" + randomId +
                "\n\n"
                + "Flag n' Patch", "UTF-8");*/
        try {
            Transport.send(msg);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }



    //TODO:do javadoc
    public static String generateRandomCode() {
        return UUID.randomUUID().toString();
    }

    /**
     * Builds the addressAsStreet entity.
     *
     * @param addr - Address
     * @return Returns the EmbeddedEntity of Address
     */
    public static EmbeddedEntity buildAddress(AddressData addr) {
        EmbeddedEntity address = new EmbeddedEntity();
        address.setProperty(AddressData.PROPERTY_DISTRICT, addr.district);
        address.setProperty(AddressData.PROPERTY_COUNTY, addr.county);
        return address;
    }

    /**
     * Builds a new report object with an entity's data
     *
     * @param report
     * @return Object of type report
     */
    public static Report buildReport(Entity report) {
        String id = String.valueOf(report.getKey().getId());
        String type = (String) report.getProperty(Report.PROPERTY_TYPE);
        long priority = (long) report.getProperty(Report.PROPERTY_PRIORITY);
        String statusDescription = (String) report.getProperty(Report.PROPERTY_STATUS_DESCRIPTION);
        String description = (String) report.getProperty(Report.PROPERTY_DESCRIPTION);
        String imageUrl = (String) report.getProperty(Report.PROPERTY_IMAGE);

        EmbeddedEntity embeddedEntity = (EmbeddedEntity) report.getProperty(Report.PROPERTY_ADDRESS);
        AddressData address = new AddressData((String) embeddedEntity.getProperty("district"),
                (String) embeddedEntity.getProperty("county"));

        String addressAsStreet = (String) report.getProperty(Report.PROPERTY_ADDRESS_AS_STREET);
        double lat = (double) report.getProperty(Report.PROPERTY_LATITUDE);
        double lon = (double) report.getProperty(Report.PROPERTY_LONGITUDE);
        long numOfFollows = (long) report.getProperty(Report.PROPERTY_FOLLOWERS);
        String responsible = (String) report.getProperty(Report.PROPERTY_WORKER_RESPONSIBLE);
        long points = (long) report.getProperty(Report.PROPERTY_POINTS);
        return new Report(id, type, (int) priority, statusDescription, description, imageUrl, addressAsStreet, lat, lon,
                address, (int) numOfFollows, responsible, (int) points);
    }

    public static Report buildReportForOutput(Entity report, boolean isFollowing) {
        String id = String.valueOf(report.getKey().getId());
        String type = (String) report.getProperty(Report.PROPERTY_TYPE);
        long priority = (long) report.getProperty(Report.PROPERTY_PRIORITY);
        String statusDescription = (String) report.getProperty(Report.PROPERTY_STATUS_DESCRIPTION);
        String description = (String) report.getProperty(Report.PROPERTY_DESCRIPTION);
        String imageUrl = (String) report.getProperty(Report.PROPERTY_IMAGE);

        EmbeddedEntity embeddedEntity = (EmbeddedEntity) report.getProperty(Report.PROPERTY_ADDRESS);
        AddressData address = new AddressData((String) embeddedEntity.getProperty("district"),
                (String) embeddedEntity.getProperty("county"));

        String addressAsStreet = (String) report.getProperty(Report.PROPERTY_ADDRESS_AS_STREET);
        double lat = (double) report.getProperty(Report.PROPERTY_LATITUDE);
        double lon = (double) report.getProperty(Report.PROPERTY_LONGITUDE);
        long numOfFollows = (long) report.getProperty(Report.PROPERTY_FOLLOWERS);
        //String responsible = (String) report.getProperty(Report.PROPERTY_WORKER_RESPONSIBLE);
        String user = (String) report.getProperty(Report.PROPERTY_USER);

        long date = (long) report.getProperty(Report.PROPERTY_CREATION_DATE);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Lisbon"));
        String creationTime = sdf.format(date);

        long points = (long) report.getProperty(Report.PROPERTY_POINTS);

        return new Report(id, type, (int) priority, creationTime, user, statusDescription, description, imageUrl, addressAsStreet, lat, lon,
                address, (int) numOfFollows, isFollowing, (int) points);
        // return new Report(id, type, (int) priority, creationTime, user, statusDescription, description, imageUrl, addressAsStreet, lat, lon,
        //          address, (int) numOfFollows, responsible, isFollowing);
    }

    /**
     * @param data frontoffice data
     * @return entity for user
     */
    public static Entity buildEntityUser_Trial(RegisterData_FrontOffice data) {
        // Creates the User entity.

        Entity user = new Entity(User_Global.KIND, data.email);

        // Sets the entity's properties.

        user.setProperty(User_Global.PROPERTY_NAME, data.name);
        user.setProperty(User_Global.PROPERTY_PWD, DigestUtils.sha512Hex(data.password));
        user.setIndexedProperty(User_Global.PROPERTY_ADDR, Utils.buildAddress(data.address));

        user.setProperty(User_Global.PROPERTY_ROLE, RolesEnum.TRIAL_USER.getRoleDescription());
        user.setProperty(User_Global.PROPERTY_ACCOUNT_BLOCKED, false);
        // TODO: o random nao deveria existir
        Random rn = new Random();
        user.setProperty(User_FrontOffice.PROPERTY_IDENTIFIER, rn.nextInt());
        //TODO: CHANGE ACCOUNT VERIFIED
        user.setProperty(User_Global.PROPERTY_ACCOUNT_VERIFIED, false);
        user.setProperty(User_FrontOffice.PROPERTY_NUMBER_REPORTS, 0);
        return user;
    }

    public static String generateEmail(String domain, int length) {
        return RandomStringUtils.random(length, "abcdefghijklmnopqrstuvwxyz") + "@" + domain;
    }

    /**
     * @param data
     * @return entity fdfd
     */
    public static Entity buildEntityUser_Worker(RegisterData_BackOffice data) {
        // Creates the User entity.
        // String email = generateEmail("gmail.com", 6);

        Entity user = new Entity(User_Global.KIND, data.email);

        // Sets the entity's properties.

        user.setProperty(User_Global.PROPERTY_NAME, data.name);
        user.setProperty(User_Global.PROPERTY_PWD, DigestUtils.sha512Hex(data.password));
        user.setIndexedProperty(User_Global.PROPERTY_ADDR, Utils.buildAddress(data.address));
        user.setProperty(User_BackOffice.PROPERTY_NIF, data.nif);
        user.setProperty(User_BackOffice.PROPERTY_WORKING_AREA, data.workingArea);
        user.setProperty(User_Global.PROPERTY_ROLE, RolesEnum.WORKER_USER.getRoleDescription());


        user.setProperty(User_Global.PROPERTY_ACCOUNT_BLOCKED, false);
        //TODO: CHANGE ACCOUNT VERIFIED
        user.setProperty(User_Global.PROPERTY_ACCOUNT_VERIFIED, false);

        user.setProperty(User_BackOffice.PROPERTY_WORKER_INFO, "");
        return user;
    }

    /**
     * @param data
     * @return entity
     */
    @SuppressWarnings("incomplete-switch")
    public static Entity buildEntityUser_Administration(RegisterData_Administration data) {
        // Creates the User entity.
        Entity user = new Entity(User_Global.KIND, data.email);

        // Sets the entity's properties.

        user.setProperty(User_Global.PROPERTY_NAME, data.name);
        user.setProperty(User_Global.PROPERTY_PWD, DigestUtils.sha512Hex(data.password));
        user.setIndexedProperty(User_Global.PROPERTY_ADDR, Utils.buildAddress(data.address));
        user.setProperty(User_Administration.PROPERTY_INTERNAL_ID, data.internalId);

        user.setProperty(User_Global.PROPERTY_ROLE, RolesEnum.CORE_USER.getRoleDescription());
        user.setProperty(User_Global.PROPERTY_ACCOUNT_BLOCKED, false);


        //TODO: CHANGE ACCOUNT VERIFIED
        user.setProperty(User_Global.PROPERTY_ACCOUNT_VERIFIED, true);
        return user;
    }

    @SuppressWarnings("incomplete-switch")
    public static Entity buildEntityUser_End(RegisterData_Administration data) {
        // Creates the User entity.
        Entity user = new Entity(User_Global.KIND, data.email);

        // Sets the entity's properties.

        user.setProperty(User_Global.PROPERTY_NAME, data.name);
        user.setProperty(User_Global.PROPERTY_PWD, DigestUtils.sha512Hex(data.password));

        //user.setProperty(User_Global.PROPERTY_GENDER, data.gender);
        //user.setProperty(User_Global.PROPERTY_YEAR_OF_BIRTH, data.yearOfBirth);
        user.setIndexedProperty(User_Global.PROPERTY_ADDR, Utils.buildAddress(data.address));
        user.setProperty(User_Administration.PROPERTY_INTERNAL_ID, data.internalId);

        user.setProperty(User_Global.PROPERTY_ROLE, RolesEnum.END_USER.getRoleDescription());
        user.setProperty(User_Global.PROPERTY_ACCOUNT_BLOCKED, false);
        //TODO: CHANGE ACCOUNT VERIFIED
        user.setProperty(User_Global.PROPERTY_ACCOUNT_VERIFIED, true);
        return user;
    }


    public static Object[] buildEntityAuthToken(LoginData data, String role, Key userKey) {
        // Creates a new token object.
        AuthToken token = new AuthToken(data.email, role);

        // Creates the AuthToken entity.
        Entity tkn = new Entity(AuthToken.KIND, token.id, userKey);

        // Sets the entity's properties.
        tkn.setProperty(AuthToken.PROPERTY_USER, token.user);
        tkn.setProperty(AuthToken.PROPERTY_CREATION, token.creationDate);
        tkn.setProperty(AuthToken.PROPERTY_EXPIRATION, token.expirationDate);
        return new Object[]{token, tkn};
    }

    /**
     * Performs security validations that includes token validations and user role-based acess validations
     *
     * @param token    The token received by input
     * @param resource The resource of the requested operation
     * @return Returns Response status, as HTTP 202 (ACCEPTED) or 401 code
     * (UNAUTHORIZED)
     */
    public static Response securityValidations(Entity user, AuthToken token, ResourcesEnum resource,
                                               DatastoreService datastore, Logger LOG) {
        final Gson g = new Gson();
        if (!token.validToken()) {
            LOG.warning(Utils.OUTPUT_WRONG_FIELDS);
            return Response.status(Status.BAD_REQUEST).entity(g.toJson(new StringUtil(Utils.OUTPUT_WRONG_FIELDS))).build();
        }

        // Token validations, if fail, it returns the specified Response code
        // error
        Response rsp = Utils.validateToken(token, user.getKey(), datastore, LOG);
        if (rsp.getStatus() != Status.ACCEPTED.getStatusCode())
            return rsp;
        // User validations, with permissions verifier, if fail, it returns the specified Response code
        // error
        rsp = Utils.validateUser(user, datastore, LOG, resource);
        return rsp;
    }

    /**
     * Creates default types for the core user recently created
     */
    public static void buildDefaultTypesForCore(Entity user, Transaction txn, DatastoreService datastore, Gson g, Logger LOG) {

        EmbeddedEntity address = (EmbeddedEntity) user.getProperty(User_Global.PROPERTY_ADDR);
        String county = (String) address.getProperty(AddressData.PROPERTY_COUNTY);
        String email = String.valueOf(user.getKey().getName());

        Entity reportTypeEntity;

        // Sets the entity's properties.
        for (TypeEnum type : TypeEnum.values()) {
            reportTypeEntity = new Entity(ReportType.KIND);
            reportTypeEntity.setProperty(ReportType.PROPERTY_NAME, type.getTypeDescription());
            reportTypeEntity.setProperty(ReportType.PROPERTY_CREATOR, email);
            reportTypeEntity.setProperty(ReportType.PROPERTY_COUNTY, county);
            reportTypeEntity.setProperty(ReportType.PROPERTY_RESPONSIBLE, null);
            reportTypeEntity.setProperty(ReportType.PROPERTY_ACTIVE, true);

            datastore.put(txn, reportTypeEntity);
        }

        // Entity areas=new Entity()
        LOG.info(OUTPUT_DEFAULT_TYPES);
    }


    // Private methods

    // This method verifies if the given responsible for the soving of an
    // occurence exists

    // TODO: check worker's working area
    public static boolean validResponsible(String responsible, String county, DatastoreService datastore) {

        // Checks if the responsible exists in database and if works on the county
        if (responsible.isEmpty())
            return false;
        Key key = KeyFactory.createKey(User_Global.KIND, responsible);
        try {
            Entity user = datastore.get(key);
            String role = (String) user.getProperty(User_Global.PROPERTY_ROLE);
            if (role.equalsIgnoreCase(RolesEnum.WORKER_USER.getRoleDescription())) {
                ArrayList<String> workingArea = (ArrayList<String>) user.getProperty(User_BackOffice.PROPERTY_WORKING_AREA);
                if (workingArea.contains(county))
                    return true;
                else return false;
            } else
                return false;
        } catch (EntityNotFoundException e) {
            return false;
        }
    }
}
