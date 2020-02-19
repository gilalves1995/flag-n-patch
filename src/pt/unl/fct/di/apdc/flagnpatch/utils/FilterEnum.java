package pt.unl.fct.di.apdc.flagnpatch.utils;

public enum FilterEnum {
    REPORT_TYPE("type"),
    REPORT_STATUS("status");

    private String filterDescription;

    FilterEnum(String filterDescription) {
        this.filterDescription = filterDescription;
    }

    public static boolean validTypeValue(String filter) {
        for (FilterEnum value : FilterEnum.values())
            if (value.getFilterDescription().equals(filter))
                return true;
        return false;
    }

    public String getFilterDescription() {
        return filterDescription;
    }


}
