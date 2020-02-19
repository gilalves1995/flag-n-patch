package pt.unl.fct.di.apdc.flagnpatch.entities;

import pt.unl.fct.di.apdc.flagnpatch.inputData.AddressData;

public class Report {

    public static final String KIND = "Report";
    public static final String PROPERTY_STATUS_DESCRIPTION = "statusDescription";

    public static final String PROPERTY_USER = "user";
    public static final String PROPERTY_TYPE = "type";
    public static final String PROPERTY_PRIORITY = "priority";
    public static final String PROPERTY_DESCRIPTION = "description";
    public static final String PROPERTY_IMAGE = "imageUrl";
    public static final String PROPERTY_ADDRESS_AS_STREET = "addressAsStreet";
    public static final String PROPERTY_ADDRESS = "address";
    public static final String PROPERTY_LATITUDE = "lat";
    public static final String PROPERTY_LONGITUDE = "lng";
    public static final String PROPERTY_FOLLOWERS = "numOfFollows";
    public static final String PROPERTY_CREATION_DATE = "creationDate";

   public static final String PROPERTY_WORKER_RESPONSIBLE = "responsible";
    public static final String PROPERTY_POINTS = "points";
    public static final String PROPERTY_SUM_PRIORITY = "sumPriority";


    public String id;
    public String type;
    public int priority;
    public String statusDescription;
    public String description;
    public String creationDate;
    public String imageUrl;

    // addressAsStreet as String or as Address??
    public String addressAsStreet;
    public double lat, lng;
    public Integer numOfFollows;
    public AddressData address;
    public String user;

    public String responsible;
    public boolean isFollowing;
    public Integer points;
    public Integer sumPriority;

    public Report() {
    }

    public Report(String id, String type, int priority, String statusDescription,
                  String description, String imageUrl, String addressAsStreet, double lat, double lng,
                  AddressData address, int numOfFollows, String responsible, int points) {
        this.id = id;
        this.type = type;
        this.priority = priority;
        this.statusDescription = statusDescription;
        this.description = description;
        this.imageUrl = imageUrl;
        this.addressAsStreet = addressAsStreet;
        this.lat = lat;
        this.lng = lng;
        this.address=address;
        this.numOfFollows = numOfFollows;
        this.responsible=responsible;
        //this.points=points;
    }
    public Report(String id, String type, int priority, String creationDate, String user, String statusDescription,
                  String description, String imageUrl, String addressAsStreet, double lat, double lng,
                  AddressData address, int numOfFollows, boolean isFollowing, int points) {
        this.id = id;
        this.type = type;
        this.priority = priority;
        this.creationDate=creationDate;
        this.user=user;
        this.statusDescription = statusDescription;
        this.description = description;
        this.imageUrl = imageUrl;
        this.addressAsStreet = addressAsStreet;
        this.lat = lat;
        this.lng = lng;
        this.address=address;
        this.numOfFollows = numOfFollows;
       // this.responsible=responsible;
        this.isFollowing=isFollowing;
        this.points=points;
    }
}
