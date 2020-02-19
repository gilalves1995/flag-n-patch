package pt.unl.fct.di.apdc.flagnpatch.entities;

public class CredentialManager {

    public static final String KIND = "CredentialManager";

    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_USER_EMAIL = "user";
    public static final String PROPERTY_CREATION_DATE = "creationDate";
    public static final String PROPERTY_EXPIRY_DATE = "expiryDate";
    public static final String PROPERTY_TYPE = "type";

    public static final String TYPE_EMAIL = "email";
    public static final String TYPE_PASSWORD = "password";
    public static final String TYPE_NEW_END_USER="newEnd";

    public String id;
    public String user;
    public String type;
    public long creationDate;
    public long expiryDate;

    public CredentialManager() {
    }

    public CredentialManager(String id, String user, String type, long EXPIRATION_TIME) {
        this.id = id;
        this.user = user;
        this.type = type;
        this.creationDate = System.currentTimeMillis();
        this.expiryDate = this.creationDate + EXPIRATION_TIME;
    }

}

