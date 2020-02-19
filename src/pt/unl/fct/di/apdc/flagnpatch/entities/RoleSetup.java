package pt.unl.fct.di.apdc.flagnpatch.entities;

import pt.unl.fct.di.apdc.flagnpatch.utils.ResourcesEnum;
import java.util.ArrayList;

/**
 * Created by michael on 31/05/17.
 */
public class RoleSetup {


    public static final String KIND = "Role";

    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_DESCRIPTION = "description";
    public static final String PROPERTY_RESOURCES = "resources";

    // Role attributes
    public String name;
    public String description;
    public ArrayList<String> resources;

    // Empty constructor
    public RoleSetup() {
    }

    public RoleSetup(String name, String description, ArrayList<String> resources) {
        this.name = name;
        this.description = description;
        System.out.println("sao" +resources);
        this.resources = resources;
    }

}
