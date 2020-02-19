package pt.unl.fct.di.apdc.flagnpatch.entities;

public class Role {

    public static final String KIND = "Role";

    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_DESCRIPTION = "description";
    public static final String PROPERTY_RESOURCES = "resources";

    // Role attributes
    public String name;
    public String description;

    // Empty constructor
    public Role() {
    }

    public Role(String name, String description) {
        this.name = name;
        this.description = description;
    }

}
