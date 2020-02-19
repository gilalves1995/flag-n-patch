package pt.unl.fct.di.apdc.flagnpatch.entities;


import java.util.UUID;

public class AuthToken {

    public static final long EXPIRATION_TIME = 1000 * 60 * 60 * 4;    // 4h

    public static final String KIND = "AuthToken";

    public static final String PROPERTY_USER = "user";
    public static final String PROPERTY_CREATION = "creationDate";
    public static final String PROPERTY_EXPIRATION = "expirationDate";

    public String user;
    public String id;
    public long creationDate;
    public long expirationDate;
    public String role;

    // Empty constructor
    public AuthToken() {
    }

    public AuthToken(String user, String role) {
        this.user = user;
        this.id = UUID.randomUUID().toString();
        this.creationDate = System.currentTimeMillis();
        this.expirationDate = this.creationDate + AuthToken.EXPIRATION_TIME;
        this.role = role;
    }

    public AuthToken(String user, String id, long creationData, long expirationData, String role) {
        this.user = user;
        this.id = id;
        this.creationDate = creationData;
        this.expirationDate = expirationData;
        this.role = role;
    }

    public boolean validToken() {
        return validField(user) &&
                validField(id) &&
                validEmail() &&
                validDates();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AuthToken))
            return false;
        AuthToken t = (AuthToken) o;
        if (!t.user.equals(this.user))
            return false;
        if (!t.id.equals(this.id))
            return false;
        if (t.creationDate != this.creationDate)
            return false;
        if (t.expirationDate != this.expirationDate)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "AuthToken [user=" + user + ", id=" + id + ", creationDate=" + creationDate + ", expirationDate="
                + expirationDate + "]";
    }

    private boolean validField(String value) {
        return value != null && !value.equals("");
    }

    private boolean validEmail() {
        return this.user.matches("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
    }

    private boolean validDates() {
        return this.creationDate < this.expirationDate;
    }

}
