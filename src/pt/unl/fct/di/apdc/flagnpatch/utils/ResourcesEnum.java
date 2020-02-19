package pt.unl.fct.di.apdc.flagnpatch.utils;

public enum ResourcesEnum  {

    GENERAL_GET_ALL_REPORTS("getAllReports"),
    GENERAL_REGISTER_END_USER("registerEndUser"),
    FRONTOFFICE_LOAD_NOTIFICATIONS("loadNotifications"),
    FRONTOFFICE_SEE_NOTIFICATION("seeNotification"),
    FRONTOFFICE_REGISTER_NEW_REPORT("registerNewReport"),
    FRONTOFFICE_GET_PERSONAL_REPORT("getPersonalReports"),
    FRONTOFFICE_GET_FOLLOWED_REPORT("getFollowedReports"),
    FRONTOFFICE_ADD_COMMENT_REPORT("addCommentReport"),
    FRONTOFFICE_GET_COMMENT_REPORT("getCommentsReport"),
    FRONTOFFICE_GET_SUGGESTION_LIST_REPORT("getSuggestionList"),
    FRONTOFFICE_FOLLOW_SPECIFIED_REPORT("followSpecifiedReport"),
    FRONTOFFICE_UNFOLLOW_SPECIFIED_REPORT("unfollowSpecifiedReport"),
    
    BACKOFFICE_CHANGE_REPORT_STATUS("changeReportStatus"),
    BACKOFFICE_GET_WORKER_REPORTS ("getWorkerReports"),
    BACKOFFICE_GET_STATUS_LOG("getStatusLog"),
    BACKOFFICE_WORKER_DETAILS("getWorkerDetails"),
    BACKOFFICE_WORKER_UPDATE_DETAILS("updateWorkerInfo"),
    GENERAL_LIST_REPORT_TYPES("listReportTypes"),
    GENERAL_CHANGE_PASSWORD("changePassword"),
    GENERAL_RETRIEVE_CONFIRMATION_EMAIL("retrieveConfirmationEmail"),
    GENERAL_GET_REPORT_DETAILS("getReportDetails"),

    ADMINISTRATION_ADD_REPORT_TYPE("addReportType"),
    ADMINISTRATION_CHANGE_TYPE_STATUS("changeTypeStatus"),
    ADMINISTRATION_LIST_WORKERS("listWorkers"),
    ADMINISTRATION_GEN_INFO("loadAdminInfo"),
    ADMINISTRATION_CHANGE_RESPONSIBLE("changeResponsible"),
    ADMINISTRATION_LOAD_REPORTS_BY_RESP("loadFilteredReports"),
    ADMINISTRATION_REGISTER_END("registerEndAccount"),
    ADMINISTRATION_LOAD_END_ACCOUNTS("loadEndAccounts"),
    ADMINISTRATION_UPDATE_BLOCK_STATUS("updateBlockStatus"),
    ADMINISTRATION_CHANGE_AREA_STATUS("changeAreaStatus"),
    ADMINISTRATION_GET_AREA_STATUS("getAreaStatus"),
    ADMINISTRATION_GET_GRAPH_DATA("getReportsGraph"),
    ADMINISTRATION_REGISTER_WORKER_USER("workerRegisterByCore")
    
    // TODO:pensar nisto
    
    ;
    private String resourceName;

    private String resourceNamePT;
    private String type;

    ResourcesEnum(){}

    ResourcesEnum(String resourceName, String resourceNamePT) {
        this.resourceName = resourceName;
        this.resourceNamePT = resourceNamePT;
        this.type="public";
    }
    ResourcesEnum(String resourceName) {
        this.resourceName = resourceName;
        this.resourceNamePT = "";
        this.type="private";
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getResourceNamePT() {
        return resourceNamePT;
    }

    public String getType() {
        return type;
    }
}
