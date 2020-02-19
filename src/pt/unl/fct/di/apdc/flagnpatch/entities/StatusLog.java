package pt.unl.fct.di.apdc.flagnpatch.entities;

public class StatusLog {

    public static final String KIND = "StatusLog";

    public static final String PROPERTY_REPORT_ID = "reportId";
    public static final String PROPERTY_STATUS_DESCRIPTION = "statusDescription";
    public static final String PROPERTY_DESCRIPTION = "description";
    public static final String PROPERTY_MODIFIED_TIME = "modifiedTime";
    public static final String PROPERTY_MODIFIED_DATE = "modifiedDate";
    public static final String PROPERTY_MODIFIED_BY = "modifiedBy";

    public String reportId;
    public String statusLogId;
    public String statusDescription;
    public String description;
    public long modifiedTime;
    public String modifiedDate;
    public String modifiedBy;
    public String wasSeen;

    public StatusLog() {
    }

    public StatusLog(String reportId, String statusDescription, String description, long modifiedTime, String modifiedDate,
                     String modifiedBy) {
        this.reportId = reportId;
        this.statusDescription = statusDescription;
        this.description = description;
        this.modifiedTime=modifiedTime;
        this.modifiedDate = modifiedDate;
        this.modifiedBy = modifiedBy;
    }

    public StatusLog(String reportId, String statusLogId, String statusDescription, String description, long modifiedTime, String modifiedDate,
                     String modifiedBy, String wasSeen) {
        this.reportId = reportId;
        this.statusLogId = statusLogId;
        this.statusDescription = statusDescription;
        this.description = description;
        this.modifiedTime=modifiedTime;
        this.modifiedDate = modifiedDate;
        this.modifiedBy = modifiedBy;
        this.wasSeen = wasSeen;

    }

}