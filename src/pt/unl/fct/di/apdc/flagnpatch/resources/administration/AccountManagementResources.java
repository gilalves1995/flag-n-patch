package pt.unl.fct.di.apdc.flagnpatch.resources.administration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import pt.unl.fct.di.apdc.flagnpatch.entities.AuthToken;
import pt.unl.fct.di.apdc.flagnpatch.entities.User_Administration;
import pt.unl.fct.di.apdc.flagnpatch.entities.User_Global;
import pt.unl.fct.di.apdc.flagnpatch.inputData.AddressData;
import pt.unl.fct.di.apdc.flagnpatch.inputData.RegisterData_BackOffice;
import pt.unl.fct.di.apdc.flagnpatch.resources.frontOffice.ReportResource;
import pt.unl.fct.di.apdc.flagnpatch.resources.general.RegisterResource;
import pt.unl.fct.di.apdc.flagnpatch.resources.general.Utils;
import pt.unl.fct.di.apdc.flagnpatch.utils.ResourcesEnum;
import pt.unl.fct.di.apdc.flagnpatch.utils.RolesEnum;
import pt.unl.fct.di.apdc.flagnpatch.utils.StringUtil;

@Path("/admin/endAccountManagement")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class AccountManagementResources {

    private static final Logger LOG = Logger.getLogger(ReportResource.class.getName());
    private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    private final Gson g = new Gson();

    public AccountManagementResources() {

    }

    @POST
    @Path("/loadEndAccounts")
    public Response loadEndAccounts(AuthToken token) {

        /* Security Validations */
        Entity user = Utils.getUser(token.user, datastore, LOG);
        if (user == null) {
            LOG.warning(Utils.OUTPUT_MISSING_USER);
            return Response.status(Status.UNAUTHORIZED).entity(g.toJson(Utils.OUTPUT_MISSING_USER)).build();
        }
        Response rsp = Utils.securityValidations(user, token,
                ResourcesEnum.ADMINISTRATION_LOAD_END_ACCOUNTS, datastore, LOG);
        if (rsp.getStatus() != Status.ACCEPTED.getStatusCode())
            return rsp;
        /* Security Validations */

        EmbeddedEntity addrEntity = (EmbeddedEntity) user.getProperty(User_Global.PROPERTY_ADDR);
        String county = (String) addrEntity.getProperty(AddressData.PROPERTY_COUNTY);

        Filter filterByCounty = new FilterPredicate("address.county", FilterOperator.EQUAL, county);
        Filter filterByRole = new FilterPredicate(User_Global.PROPERTY_ROLE, FilterOperator.EQUAL, RolesEnum.END_USER.getRoleDescription());
        Query query = new Query(User_Global.KIND).setFilter(
                CompositeFilterOperator.and(filterByCounty, filterByRole));

        List<User_Administration> endList = new ArrayList<>();
        Iterator<Entity> it = datastore.prepare(query).asIterator();
        if (!it.hasNext()) {
            return Response.status(Status.NO_CONTENT).entity(
                    g.toJson(new StringUtil("No end users were found for this county."))).build();
        }

        while (it.hasNext()) {
            Entity entity = it.next();
            String name = (String) entity.getProperty(User_Global.PROPERTY_NAME);
            String email = String.valueOf(entity.getKey().getName());

            EmbeddedEntity en = (EmbeddedEntity) entity.getProperty(User_Global.PROPERTY_ADDR);
            String district = (String) en.getProperty(AddressData.PROPERTY_DISTRICT);
            String county2 = (String) en.getProperty(AddressData.PROPERTY_COUNTY);
            String internalId = (String) entity.getProperty(User_Administration.PROPERTY_INTERNAL_ID);
            boolean isAccountBlocked = (boolean) entity.getProperty(User_Global.PROPERTY_ACCOUNT_BLOCKED);

            User_Administration endUser = new User_Administration(name, email,
                    new AddressData(district, county2), RolesEnum.END_USER.getRoleDescription(), internalId, isAccountBlocked);
            endList.add(endUser);
        }

        return Response.ok(g.toJson(endList)).build();
    }

    @POST
    @Path("/updateBlockStatus/{id}")
    public Response updateBlockStatus(@PathParam("id") String id, AuthToken token) {

        /* Security Validations */
        Entity user = Utils.getUser(token.user, datastore, LOG);
        if (user == null) {
            LOG.warning(Utils.OUTPUT_MISSING_USER);
            return Response.status(Status.UNAUTHORIZED).entity(g.toJson(Utils.OUTPUT_MISSING_USER)).build();
        }
        Response rsp = Utils.securityValidations(user, token,
                ResourcesEnum.ADMINISTRATION_UPDATE_BLOCK_STATUS, datastore, LOG);
        if (rsp.getStatus() != Status.ACCEPTED.getStatusCode())
            return rsp;
        /* Security Validations */

        Key key = KeyFactory.createKey(User_Global.KIND, id);

        TransactionOptions options = TransactionOptions.Builder.withXG(true);
        Transaction txn = datastore.beginTransaction(options);
        try {
            // Verificar se utilizador existe
            Entity entity = datastore.get(key);

            // Verificar se se trata de um utilizador pertence à autarquia
            EmbeddedEntity addrEntity1 = (EmbeddedEntity) user.getProperty(User_Global.PROPERTY_ADDR);
            String county1 = (String) addrEntity1.getProperty(AddressData.PROPERTY_COUNTY);

            EmbeddedEntity addrEntity2 = (EmbeddedEntity) user.getProperty(User_Global.PROPERTY_ADDR);
            String county2 = (String) addrEntity2.getProperty(AddressData.PROPERTY_COUNTY);
            if (!county1.equals(county2)) {
                txn.rollback();
                LOG.warning("User doesn't belong to county.");
                return Response.status(Status.FORBIDDEN).entity(
                        g.toJson(new StringUtil("User doesn't belong to county."))).build();
            }

            // Verificar se o utilizador tem o role end
            String role = (String) entity.getProperty(User_Global.PROPERTY_ROLE);

            if (!role.equals(RolesEnum.END_USER.getRoleDescription())) {
                txn.rollback();
                LOG.severe("User doesn't have the role end");
                return Response.status(Status.FORBIDDEN).entity(
                        g.toJson(new StringUtil("User doesn't have the role end"))).build();
            }

            // Perform operation
            boolean isAccountBlocked = (boolean) entity.getProperty(User_Global.PROPERTY_ACCOUNT_BLOCKED);
            if (isAccountBlocked) {
                isAccountBlocked = false;
            } else isAccountBlocked = true;

            entity.setProperty(User_Global.PROPERTY_ACCOUNT_BLOCKED, isAccountBlocked);
            datastore.put(txn, entity);
            txn.commit();

            return Response.ok(g.toJson(isAccountBlocked)).build();

        } catch (EntityNotFoundException e) {
            txn.rollback();
            return Response.status(Status.NOT_FOUND).entity(
                    g.toJson(new StringUtil("User not found."))).build();
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
    
    @POST
    @Path("/workerRegisterByCore")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerWorkerByCore(String jsonString) {
    	JsonElement jelement = new JsonParser().parse(jsonString);
        JsonObject jobject = jelement.getAsJsonObject();
        JsonObject jtoken = jobject.getAsJsonObject("token");
        JsonObject jdata = jobject.getAsJsonObject("data");
        
        AuthToken token = g.fromJson(jtoken.toString(), AuthToken.class);
        RegisterData_BackOffice data = g.fromJson(jdata.toString(), RegisterData_BackOffice.class);
        
    	  /* Security Validations */
        Entity user = Utils.getUser(token.user, datastore, LOG);
        if (user == null) {
            LOG.warning(Utils.OUTPUT_MISSING_USER);
            return Response.status(Status.UNAUTHORIZED).entity(g.toJson(new StringUtil(Utils.OUTPUT_MISSING_USER))).build();
        }
        Response rsp = Utils.securityValidations(user, token, ResourcesEnum.ADMINISTRATION_REGISTER_WORKER_USER, datastore,
                LOG);
        if (rsp.getStatus() != Status.ACCEPTED.getStatusCode())
            return rsp;
		/* Security Validations */
        
        // completar data
        
        EmbeddedEntity addrEntity = (EmbeddedEntity) user.getProperty(User_Global.PROPERTY_ADDR);
        String county = (String) addrEntity.getProperty(AddressData.PROPERTY_COUNTY);
        data.workingArea.add(county);
        System.out.println(g.toJson(data));
       
        return RegisterResource.registerUser(data, RolesEnum.WORKER_USER);
        
    }


}
