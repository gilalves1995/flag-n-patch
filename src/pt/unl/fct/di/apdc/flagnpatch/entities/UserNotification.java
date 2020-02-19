package pt.unl.fct.di.apdc.flagnpatch.entities;

public class UserNotification {

	public static final String KIND = "UserNotification";

	// public static final String PROPERTY_ID = "id";
	// public static final String PROPERTY_EMAIL = "email";
	// public static final String PROPERTY_STATUSLOG_ID = "statusLogId";
	// public static final String PROPERTY_REPORT_ID = "reportId";
	// public static final String PROPERTY_WAS_SEEN = "wasSeen";
	//
	// public String id;
	// public String email;
	// public String statusLogId;
	// public String reportId;
	// public boolean wasSeen;

	public static final String PROPERTY_ID = "id";
	public static final String PROPERTY_EMAIL = "email";
	public static final String PROPERTY_REPORT_ID = "reportId";
	public static final String PROPERTY_WAS_SEEN = "wasSeen";
	public static final String PROPERTY_PREV_STATUS = "prevStatus";
	public static final String PROPERTY_NEW_STATUS = "newStatus";
	public static final String PROPERTY_MODIFIED_DATE = "modifiedDate";
	public static final String PROPERTY_MODIFIED_BY = "modifiedBy";
	public static final String PROPERTY_EXACT_DATE = "exactModifiedDate"; 
	public static final String PROPERTY_DESCRIPTION = "description";
	
	
	public String id;
	public String email;
	public String reportId;
	public boolean wasSeen;
	public String prevStatus;
	public String newStatus;
	public String modifiedDate;
	public String modifiedBy; // id do worker
	public long exactModifiedDate;
	public String description;
	

	public UserNotification() {

    }
	
	public UserNotification(String id, String email, String reportId, boolean wasSeen,
    		String prevStatus, String newStatus, String modifiedDate, String modifiedBy,
    			long exactModifiedDate, String description) {
    	this.id = id;
    	this.email = email;
    	this.reportId = reportId;
    	this.wasSeen = wasSeen;
    	this.prevStatus = prevStatus;
    	this.newStatus = newStatus;
    	this.modifiedDate = modifiedDate;
    	this.modifiedBy = modifiedBy;	
    	this.exactModifiedDate = exactModifiedDate;
    	this.description = description;
    }
    
   

}
