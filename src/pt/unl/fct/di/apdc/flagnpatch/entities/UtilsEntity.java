package pt.unl.fct.di.apdc.flagnpatch.entities;

/**
 * Created by michael on 22-06-2017.
 */
public class UtilsEntity {
        public static final String KIND = "Utils";
    public static final String KEY_FRONTOFFICE = "Frontoffice";

        public static final String PROPERTY_NUMBER_USER = "numUser";
    public static final String PROPERTY_TYPE = "type";

        //public static final String PROPERTY_DESCRIPTION = "description";
        //public static final String PROPERTY_RESOURCES = "resources";

        // Role attributes
        public int numUser;
        public String type;
        //public String description;

        // Empty constructor
        public UtilsEntity() {
        }

        public UtilsEntity(int numUser, String type) {
            this.numUser= numUser;
            this.type=type;
        }

    }

