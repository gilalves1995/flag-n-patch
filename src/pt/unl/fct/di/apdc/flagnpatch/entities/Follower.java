package pt.unl.fct.di.apdc.flagnpatch.entities;

public class Follower {

    public static final String KIND = "Follower";

    public static final String PROPERTY_EMAIL = "email";
    public static final String PROPERTY_REPORT = "reportId";
    public static final String PROPERTY_PRIORITY = "priority";
    public static final String PROPERTY_FOLLOW_TIME = "followTime";
    public static final String PROPERTY_FOLLOW_DATE = "followDate";

    public String email;
    public String reportId;
    public int priority;
    public long followTime;
    public String followDate;

    public Follower() {

    }

    public Follower(String email, String reportId, int priority, long followTime, String followDate) {
        this.email = email;
        this.reportId = reportId;
        this.priority = priority;
        this.followTime = followTime;
        this.followDate = followDate;
    }
}
