package pt.unl.fct.di.apdc.flagnpatch.entities;

public class ReportType {

    public static final String KIND = "ReportType";

    public static final String PROPERTY_ID = "id";

    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_RESPONSIBLE = "responsible";
    public static final String PROPERTY_CREATOR = "creatorId";
    public static final String PROPERTY_COUNTY = "belongsToCounty";
    public static final String PROPERTY_ACTIVE = "isActive";

    public String id;
    public String name;
    public String responsible;
    public String creatorId;
    public String county;
    public boolean isActive;

    public ReportType() {

    }

    public ReportType(String id, String name, String responsible, String creatorId, String county,
    		boolean isActive) {
    	this.id = id;
        this.name = name;
        this.responsible = responsible;
        this.creatorId = creatorId;
        this.county = county;
        this.isActive = isActive;
    }

}
