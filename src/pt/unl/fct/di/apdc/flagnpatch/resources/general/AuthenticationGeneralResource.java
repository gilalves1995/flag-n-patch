package pt.unl.fct.di.apdc.flagnpatch.resources.general;

import com.google.appengine.api.datastore.Entity;
import com.google.gson.Gson;
import pt.unl.fct.di.apdc.flagnpatch.entities.CredentialManager;
import pt.unl.fct.di.apdc.flagnpatch.entities.User_Global;
import pt.unl.fct.di.apdc.flagnpatch.utils.EmailUtil;
import pt.unl.fct.di.apdc.flagnpatch.utils.EmailsEnum;
import pt.unl.fct.di.apdc.flagnpatch.utils.StringUtil;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.logging.Logger;

/**
 * Created by michael on 27-06-2017.
 */
public class AuthenticationGeneralResource {
    // Converts objects to JSON
    private static final Gson g = new Gson();

    public static Response sendEmailWithConditions(String type, Entity user, String userEmail, String userKey, Entity credentialManagerEntity, Logger LOG) {
        String randomId = (String) credentialManagerEntity.getProperty(CredentialManager.PROPERTY_ID);
        long created = (long) credentialManagerEntity.getProperty(CredentialManager.PROPERTY_CREATION_DATE);
        long expiry = (long) credentialManagerEntity.getProperty(CredentialManager.PROPERTY_EXPIRY_DATE);
        long avg = ((expiry - created) / 2) + created;
        long actual = System.currentTimeMillis();
        if (actual > avg) {
            try {
                sendEmail(type, user, userEmail, userKey,  randomId, LOG);
            } catch (Exception e) {
                e.printStackTrace();
            }
            LOG.info(Utils.OUTPUT_EMAIL_SENT);
            return Response.ok().entity(g.toJson(new StringUtil(Utils.OUTPUT_EMAIL_SENT))).build();
        } else {
            LOG.warning(Utils.OUTPUT_EMAIL_NOT_EXPIRED);
            return Response.status(Status.FORBIDDEN).entity(g.toJson(new StringUtil(Utils.OUTPUT_EMAIL_NOT_EXPIRED))).build();
        }
    }

    public static void sendEmail(String type, Entity user, String userEmail, String userKey, String randomId, Logger LOG) {
        String userName = (String) user.getProperty(User_Global.PROPERTY_NAME);
// TODO: NÃO ESTÁ A ENVIAR EMAIL
String pro=Utils.PASSWORD_RECOVERY_SUBJECT;
String url=null;
        if (type.equals(CredentialManager.TYPE_PASSWORD)) {
           url= Utils.RECOVER_PASSWORD_LINK+ "/" + userKey + "/" + randomId;
            Utils.sendEmail(userEmail, userName, Utils.PASSWORD_RECOVERY_SUBJECT,
                    new EmailUtil(userKey,userEmail, userName, randomId, EmailsEnum.RECOVERY_PASSWORD_HTML).getHtml());
           /* Utils.sendNewEmail(userEmail, userName, Utils.PASSWORD_RECOVERY_SUBJECT,
                    new EmailUtil(userKey,userEmail, userName, randomId, EmailsEnum.RECOVERY_PASSWORD_HTML).getHtml());*/
        } else if (type.equals(CredentialManager.TYPE_EMAIL)) {
           url = Utils.CONFIRMATION_EMAIL_LINK + "/" + userKey + "/" + randomId;
           Utils.sendEmail(userEmail, userName, Utils.CONFIRMATION_EMAIL_SUBJECT,
                   new EmailUtil(userKey,userEmail, userName, randomId, EmailsEnum.CONFIRMATION_EMAIL_HTML).getHtml());
            /*Utils.sendNewEmail( userEmail, userName, Utils.CONFIRMATION_EMAIL_SUBJECT,
                    new EmailUtil(userKey,userEmail, userName, randomId, EmailsEnum.CONFIRMATION_EMAIL_HTML).getHtml());*/
        } else
            LOG.severe("did not send anything");


        LOG.warning("url: " + url);
        System.out.println(url);
    }

}
