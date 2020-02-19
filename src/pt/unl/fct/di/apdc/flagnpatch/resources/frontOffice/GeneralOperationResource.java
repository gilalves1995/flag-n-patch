package pt.unl.fct.di.apdc.flagnpatch.resources.frontOffice;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.gson.*;
import pt.unl.fct.di.apdc.flagnpatch.entities.AuthToken;
import pt.unl.fct.di.apdc.flagnpatch.entities.Report;
import pt.unl.fct.di.apdc.flagnpatch.entities.ReportType;
import pt.unl.fct.di.apdc.flagnpatch.entities.StatusLog;
import pt.unl.fct.di.apdc.flagnpatch.entities.UserNotification;
import pt.unl.fct.di.apdc.flagnpatch.resources.general.Utils;
import pt.unl.fct.di.apdc.flagnpatch.utils.ResourcesEnum;
import pt.unl.fct.di.apdc.flagnpatch.utils.StringUtil;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

@Path("/operation")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class GeneralOperationResource {

    private static final Logger LOG = Logger.getLogger(ReportResource.class.getName());
    private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    private final Gson g = new Gson();

    public GeneralOperationResource() {

    }

    @GET
    @Path("/address")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response loadAddresses() {

        Query query = new Query(Utils.ENTITY_ADDRESSES);
        Iterator<Entity> results = datastore.prepare(query).asIterator();
        JsonObject object = new JsonObject();

        while (results.hasNext()) {
            Entity district = results.next();

            ArrayList<String> arrayList = (ArrayList<String>) district.getProperty("counties");
            JsonArray jsonArr = new JsonArray();
            for (String arrayListString : arrayList)
                jsonArr.add(arrayListString);
            object.add((String) district.getProperty("district"), jsonArr);

        }
        return Response.ok().entity(g.toJson(object)).build();
    }

    @POST
    @Path("/loadNotifications")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response loadNotifications(AuthToken token) {
          /* Security Validations */
        Entity user = Utils.getUser(token.user, datastore, LOG);
        if (user == null) {
            LOG.warning(Utils.OUTPUT_MISSING_USER);
            return Response.status(Status.UNAUTHORIZED).entity(g.toJson(new StringUtil(Utils.OUTPUT_MISSING_USER))).build();
        }
        Response rsp = Utils.securityValidations(user, token, ResourcesEnum.FRONTOFFICE_LOAD_NOTIFICATIONS, datastore,
                LOG);
        if (rsp.getStatus() != Status.ACCEPTED.getStatusCode())
            return rsp;
  /* Security Validations */
        Filter filterByUser = new FilterPredicate(UserNotification.PROPERTY_EMAIL, FilterOperator.EQUAL, token.user);
        Query query = new Query(UserNotification.KIND).setFilter(filterByUser).addSort(UserNotification.PROPERTY_EXACT_DATE);
        List<UserNotification> notificationList = new ArrayList<>();
        Iterator<Entity> it = datastore.prepare(query).asIterator();

        if (!it.hasNext()) {
            return Response.status(Status.NO_CONTENT).entity(g.toJson(new StringUtil("No notifications were found.")))
                    .build();
        }

        while (it.hasNext()) {
            Entity notification = it.next();
            String id = String.valueOf(notification.getKey().getId());
            String email = (String) notification.getProperty(UserNotification.PROPERTY_EMAIL);
            String reportId = (String) notification.getProperty(UserNotification.PROPERTY_REPORT_ID);
            boolean wasSeen = (boolean) notification.getProperty(UserNotification.PROPERTY_WAS_SEEN);
            String prevStatus = (String) notification.getProperty(UserNotification.PROPERTY_PREV_STATUS);
            String newStatus = (String) notification.getProperty(UserNotification.PROPERTY_NEW_STATUS);
            String modifiedDate = (String) notification.getProperty(UserNotification.PROPERTY_MODIFIED_DATE);
            String modifiedBy = (String) notification.getProperty(UserNotification.PROPERTY_MODIFIED_BY);
            long exactModifiedDate = (long) notification.getProperty(UserNotification.PROPERTY_EXACT_DATE);
            String description = (String) notification.getProperty(UserNotification.PROPERTY_DESCRIPTION);

            notificationList.add(new UserNotification(id, email, reportId, wasSeen, prevStatus, newStatus, modifiedDate,
                    modifiedBy, exactModifiedDate, description));
        }

        //UserNotification[] arr = new UserNotification[notificationList.size()];
        //notificationList.toArray(arr);
        return Response.ok(g.toJson(notificationList)).build();

    }

    @PUT
    @Path("/seeNotification/{id}")
    public Response seeNotification(@PathParam("id") String id, AuthToken token) {
	  /* Security Validations */
        Entity user = Utils.getUser(token.user, datastore, LOG);
        if (user == null) {
            LOG.warning(Utils.OUTPUT_MISSING_USER);
            return Response.status(Status.UNAUTHORIZED).entity(g.toJson(new StringUtil(Utils.OUTPUT_MISSING_USER))).build();
        }
        Response rsp = Utils.securityValidations(user, token, ResourcesEnum.FRONTOFFICE_SEE_NOTIFICATION, datastore, LOG);
        if (rsp.getStatus() != Status.ACCEPTED.getStatusCode())
            return rsp;

        TransactionOptions options = TransactionOptions.Builder.withXG(true);
        Transaction txn = datastore.beginTransaction(options);

        Key key = KeyFactory.createKey(UserNotification.KIND, Long.parseLong(id));
        try {
            Entity notification = datastore.get(key);

            // Testar se esta notificacao pertence de facto ao utilzador
            String email = (String) notification.getProperty(UserNotification.PROPERTY_EMAIL);
            if (!email.equals(token.user)) {
                txn.rollback();
                LOG.warning("User can't perform this operation.");
                return Response.status(Status.UNAUTHORIZED).entity(
                        g.toJson(new StringUtil("User can't perform this operation."))).build();
            }

            // Verificar se esta notificacao ja foi vista
            boolean wasSeen = (boolean) notification.getProperty(UserNotification.PROPERTY_WAS_SEEN);
            if (wasSeen) {
                txn.rollback();
                LOG.warning("Notification already seen.");
                return Response.status(Status.FORBIDDEN).entity(
                        g.toJson(new StringUtil("Notification already seen."))).build();
            }

            notification.setProperty(UserNotification.PROPERTY_WAS_SEEN, true);
            datastore.put(txn, notification);
            txn.commit();
            return Response.ok(g.toJson(
                    new StringUtil("Success! Notification was seen."))).build();

        } catch (EntityNotFoundException e) {
            txn.rollback();
            LOG.warning("Notification was not found.");
            return Response.status(Status.NOT_FOUND).entity(
                    g.toJson(new StringUtil("Notification was not found."))).build();
        } finally {
            // Checks if something went wrong and the transaction is still
            // active.
            if (txn.isActive()) {
                txn.rollback();
                LOG.severe(Utils.OUTPUT_TRANSACTION_FAILED);
                return Response.status(Status.INTERNAL_SERVER_ERROR).entity(g.toJson(new StringUtil(Utils.OUTPUT_TRANSACTION_FAILED))).build();
            }
        }
    }

//	@PUT
//	@Path("/loadNotifications")
//	@Consumes(MediaType.APPLICATION_JSON)
//	public Response loadNotifications(AuthToken token) {
//
//		/* Security Validations */
//		Entity user = Utils.getUser(token.user, datastore, LOG);
//		if (user == null) {
//			LOG.warning(Utils.OUTPUT_MISSING_USER);
//			return Response.status(Status.UNAUTHORIZED).entity(new
//					StringUtil(Utils.OUTPUT_MISSING_USER)).build();
//		}
//		Response rsp = Utils.securityValidations(user, token,
//				ResourcesEnum.FRONTOFFICE_REGISTER_NEW_REPORT, datastore,
//				LOG);
//		if (rsp.getStatus() != Status.ACCEPTED.getStatusCode())
//			return rsp;
//		/* Security Validations */
//
//		/* Creates a list which will contains the notifications which weren't yet
//	 loaded to server
//		 * and weren't seen yet by this user
//		 */
//		List<StatusLog> notificationList = new ArrayList<>();
//		Filter filter = new FilterPredicate(UserNotification.PROPERTY_EMAIL,
//				FilterOperator.EQUAL, token.user);
//		Query query = new Query(UserNotification.KIND).setFilter(filter);
//
//		Iterator<Entity> results = datastore.prepare(query).asIterator();
//
//		if (!results.hasNext()) {
//			// If does not exist any report from user, it returns HTTP 204 code
//			LOG.info("No notifications found: " + token.user);
//			return Response.status(Status.NO_CONTENT).build();
//		}
//
//		// Begin the transaction
//		//TransactionOptions options = TransactionOptions.Builder.withXG(true);
//		//Transaction txn = datastore.beginTransaction();
//
//
//		while (results.hasNext()) {
//			Entity notifyUser = results.next();
//
//			// Checks if the notification was already loaded to the client
//			// boolean loadedToClient =
//			// (boolean)
//			notifyUser.getProperty(UserNotification.PROPERTY_LOADED_TO_CLIENT);
//			// if (!loadedToClient) {
//
//			long reportId = Long.parseLong((String)
//					notifyUser.getProperty(UserNotification.PROPERTY_REPORT_ID));
//			long statusLogId = Long.parseLong((String)
//					notifyUser.getProperty(UserNotification.PROPERTY_STATUSLOG_ID));
//			System.out.println("reportId : " + reportId);
//			System.out.println("statusLogId: " + statusLogId);
//			// A key do StatusLog ja esta associado a um Report, logo aqui(no
//			notifiKey) precisamos de incluir
//// o id do report que esta associado
//
//Key reportKeyAncestor = KeyFactory.createKey(Report.KIND, reportId);
//			Key statusLogKey = KeyFactory.createKey(reportKeyAncestor,
//					StatusLog.KIND, statusLogId);
//			// KeyFactory.cre
//
//			//System.out.println(g.toJson(new StringUtil(notifiKey));
//
//			Entity reportEntity;
//			try {
//				//datastore.get(Key(StatusLog.KIND,Long.parseLong(id)));
//
//				Entity statusLogEntity = datastore.get(statusLogKey);
//				// System.out.println("Entity was found.");
//				try {
//					reportEntity = datastore.get(reportKeyAncestor);
//				} catch (EntityNotFoundException e) {
//					return Response.status(Status.FORBIDDEN).entity(g.toJson(new
//							StringUtil("Some notification was not found."))).build();
//				}
//
//				String statusDescription = (String)
//						statusLogEntity.getProperty(StatusLog.PROPERTY_STATUS_DESCRIPTION);
//				String description = (String)
//						statusLogEntity.getProperty(StatusLog.PROPERTY_DESCRIPTION);
//				String modifiedDate = (String)
//						statusLogEntity.getProperty(StatusLog.PROPERTY_MODIFIED_DATE);
//				String modifiedBy = (String)
//						statusLogEntity.getProperty(StatusLog.PROPERTY_MODIFIED_BY);
//				String wasSeen =
//						String.valueOf(notifyUser.getProperty(UserNotification.PROPERTY_WAS_SEEN));
//				String address = (String)
//						reportEntity.getProperty(Report.PROPERTY_ADDRESS_AS_STREET);
//				double lat = (double) reportEntity.getProperty(Report.PROPERTY_LATITUDE);
//				double lon = (double)
//						reportEntity.getProperty(Report.PROPERTY_LONGITUDE);
//
//				StatusLog notification = new StatusLog(String.valueOf(reportId),
//						String.valueOf(statusLogId), statusDescription, description,
//						modifiedDate,
//						modifiedBy, wasSeen, address, lat, lon);
//				notificationList.add(notification);
//				//
//				// This notification can be marked as loaded to client side
//				//notifyUser.setIndexedProperty(UserNotification.PROPERTY_LOADED_TO_CLIENT,
//				true);
//				//datastore.put(txn, notifyUser);
//				//txn.commit();
//
//			} catch (EntityNotFoundException e) {
//				return Response.status(Status.FORBIDDEN).entity(g.toJson(new
//						StringUtil("Some notification was not found."))).build();
//			}
//
//			//}
//		}
//		StatusLog[] arr = new StatusLog[notificationList.size()];
//		notificationList.toArray(arr);
//		return Response.ok(g.toJson(arr)).build();
//	}

    // @SuppressWarnings("unused")
    // @PUT
    // @Path("/seeNotification")
    // public Response seeNotification(String jsonString) {
    //
    // JsonElement jelement = new JsonParser().parse(jsonString);
    // JsonObject jobject = jelement.getAsJsonObject();
    //
    // JsonObject jtoken = jobject.getAsJsonObject("token");
    // JsonElement jstatusLogId = jobject.get("statusLogId");
    //
    // AuthToken token = g.fromJson(jtoken.toString(), AuthToken.class);
    // String statusLogId = g.fromJson(jstatusLogId.toString(), String.class);
    //
    // /* Security Validations */
    // Entity user = Utils.getUser(token.user, datastore, LOG);
    // if (user == null) {
    // LOG.warning(Utils.OUTPUT_MISSING_USER);
    // return Response.status(Status.UNAUTHORIZED).entity(new
    // StringUtil(Utils.OUTPUT_MISSING_USER)).build();
    // }
    // Response rsp = Utils.securityValidations(user, token,
    // ResourcesEnum.FRONTOFFICE_REGISTER_NEW_REPORT, datastore,
    // LOG);
    // if (rsp.getStatus() != Status.ACCEPTED.getStatusCode())
    // return rsp;
    // /* Security Validations */
    //
    // Filter filter = CompositeFilterOperator.and(
    // new FilterPredicate(UserNotification.PROPERTY_EMAIL,
    // FilterOperator.EQUAL, token.user),
    // new FilterPredicate(UserNotification.PROPERTY_STATUSLOG_ID,
    // FilterOperator.EQUAL, statusLogId));
    // Query query = new Query(UserNotification.KIND).setFilter(filter);
    // Entity entity = datastore.prepare(query).asSingleEntity();
    // if (entity == null) {
    //
    // return Response.status(Status.FORBIDDEN).entity(g.toJson(new
    // StringUtil("No notification was found."))).build();
    // }
    //
    // Transaction txn = datastore.beginTransaction();
    //
    // entity.setProperty(UserNotification.PROPERTY_WAS_SEEN,
    // String.valueOf(true));
    // datastore.put(txn, entity);
    // txn.commit();
    //
    // /*UserNotification jsonMessage = new UserNotification(
    // (String)entity.getProperty(UserNotification.PROPERTY_ID),
    // (String)entity.getProperty(UserNotification.PROPERTY_NOTIFICATION_ID),
    // (String)entity.getProperty(UserNotification.PROPERTY_EMAIL),
    // (boolean)entity.getProperty(UserNotification.PROPERTY_LOADED_TO_CLIENT),
    // (boolean)entity.getProperty(UserNotification.PROPERTY_WAS_SEEN)
    // );*/
    // return Response.ok().build();
    // }
}
