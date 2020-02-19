package pt.unl.fct.di.apdc.flagnpatch.resources.administration;

import com.google.appengine.api.datastore.*;
import com.google.gson.Gson;

import pt.unl.fct.di.apdc.flagnpatch.entities.Areas;
import pt.unl.fct.di.apdc.flagnpatch.entities.AuthToken;
import pt.unl.fct.di.apdc.flagnpatch.entities.User_Global;
import pt.unl.fct.di.apdc.flagnpatch.inputData.AddressData;
import pt.unl.fct.di.apdc.flagnpatch.resources.frontOffice.ReportResource;
import pt.unl.fct.di.apdc.flagnpatch.resources.general.Utils;
import pt.unl.fct.di.apdc.flagnpatch.utils.ResourcesEnum;
import pt.unl.fct.di.apdc.flagnpatch.utils.StringUtil;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.util.logging.Logger;

/**
 * Created by michael on 01-07-2017.
 */
@Path("/admin/areaManagement")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class GeneralOperationResources {

    private static final Logger LOG = Logger.getLogger(ReportResource.class.getName());
    private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    private final Gson g = new Gson();


    @POST
    @Path("/changeStatus")
    public Response changeAreaStatus(AuthToken token) {

        /* Security Validations */
        Entity user = Utils.getUser(token.user, datastore, LOG);
        if (user == null) {
            LOG.warning(Utils.OUTPUT_MISSING_USER);
            return Response.status(Status.UNAUTHORIZED).entity(g.toJson(new StringUtil(Utils.OUTPUT_MISSING_USER))).build();
        }
        Response rsp = Utils.securityValidations(user, token,
                ResourcesEnum.ADMINISTRATION_CHANGE_AREA_STATUS, datastore, LOG);
        if (rsp.getStatus() != Response.Status.ACCEPTED.getStatusCode())
            return rsp;
        /* Security Validations */

        TransactionOptions options = TransactionOptions.Builder.withXG(true);
        Transaction txn = datastore.beginTransaction(options);

        EmbeddedEntity embeddedEntity = (EmbeddedEntity) user.getProperty(User_Global.PROPERTY_ADDR);
        String county = (String) embeddedEntity.getProperty(AddressData.PROPERTY_COUNTY);

        Key areasKey = KeyFactory.createKey(Areas.KIND, county);
        try {
            Entity area = datastore.get(areasKey);
            boolean isAvailable = !(boolean) area.getProperty(Areas.PROPERTY_IS_AVAILABLE);
            area.setProperty(Areas.PROPERTY_IS_AVAILABLE, isAvailable);
            datastore.put(txn, area);
            txn.commit();
            return Response.ok(g.toJson(isAvailable)).build();
        } catch (EntityNotFoundException e) {
            txn.rollback();
            LOG.severe("area not found");
            return Response.status(Response.Status.FORBIDDEN).entity(
                    g.toJson(new StringUtil("area not found"))).build();
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
    @Path("/getAreaStatus")
    public Response getAreaStatus(AuthToken token) {
       /* Security Validations */
        Entity user = Utils.getUser(token.user, datastore, LOG);
        if (user == null) {
            LOG.warning(Utils.OUTPUT_MISSING_USER);
            return Response.status(Status.UNAUTHORIZED).entity(g.toJson(new StringUtil(Utils.OUTPUT_MISSING_USER))).build();
        }
        Response rsp = Utils.securityValidations(user, token,
                ResourcesEnum.ADMINISTRATION_GET_AREA_STATUS, datastore, LOG);
        if (rsp.getStatus() != Response.Status.ACCEPTED.getStatusCode())
            return rsp;
        /* Security Validations */

        EmbeddedEntity addrEntity = (EmbeddedEntity) user.getProperty(User_Global.PROPERTY_ADDR);
        String county = (String) addrEntity.getProperty(AddressData.PROPERTY_COUNTY);

        Key areasKey = KeyFactory.createKey(Areas.KIND, county);
        try {
            Entity area = datastore.get(areasKey);
            boolean isActive = (boolean) area.getProperty(Areas.PROPERTY_IS_AVAILABLE);
            return Response.ok(g.toJson(isActive)).build();
        } catch (EntityNotFoundException e) {
            LOG.info("Area was not found.");
            return Response.status(Status.NOT_FOUND).entity(
                    g.toJson(new StringUtil("Area was not found"))).build();
        }
    }
}
