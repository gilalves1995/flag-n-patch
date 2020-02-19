package pt.unl.fct.di.apdc.flagnpatch.entities;

import pt.unl.fct.di.apdc.flagnpatch.inputData.AddressData;

import java.io.Serializable;

public class User_Global implements Serializable{

    public static final String KIND = "User";

    public static final String PROPERTY_NAME = "name";

    public static final String PROPERTY_EMAIL = "email";

    public static final String PROPERTY_PWD = "password";
    public static final String PROPERTY_ADDR = "address";
    public static final String PROPERTY_ROLE = "role";
    public static final String PROPERTY_ACCOUNT_BLOCKED = "isAccountBlocked";
    public static final String PROPERTY_ACCOUNT_VERIFIED = "isAccountVerified";
    // Common attributes
    public String name;

    // Authentication attributes
    public String email;
    public String password;

    // Personal info attributes
    public AddressData address;

    //Roles
    public String role;

    //
    public boolean isAccountBlocked;

    public boolean isAccountVerified;

    // Empty constructor
    public User_Global() {
    }

     public User_Global(String name,String email, AddressData address, String role) {
        this.name = name;
        this.email = email;
        this.address = address;
        this.role = role;
        this.isAccountBlocked = false;
        this.isAccountVerified = false;
    }
}
