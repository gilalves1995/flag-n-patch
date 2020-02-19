package pt.unl.fct.di.apdc.flagnpatch.inputData;

import pt.unl.fct.di.apdc.flagnpatch.utils.StatusEnum;
import pt.unl.fct.di.apdc.flagnpatch.utils.TypeEnum;

// TODO: VERIFICAR USO DA CLASSE

public class ReportFilterData {

    public static final String PROPERTY_TYPE = "type";
    public static final String PROPERTY_STATUS = "status";
    public static final String PROPERTY_START_DATE = "startDate";
    public static final String PROPERTY_END_DATE = "endDate";

    public String type;
    public String status;

    // Start date: if it's the only active field, than we want every report after this date
    public String startDate;
    public String endDate;


    public ReportFilterData() {

    }

    public ReportFilterData(String type, String status, String startDate, String endDate) {
        this.type = type;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // TODO: Verification of both date's string formation, verification of type and status

    // Checks if a field is empty
    public boolean emptyField(String field) {
        return field.equals("");
    }

    // Verification of type value
    public boolean validTypeValue() {
        for (TypeEnum value : TypeEnum.values())
            if (value.getTypeDescription().equals(this.type))
                return true;
        return false;
    }

    // Verification of status value
    public boolean validStatusValue() {
        for (StatusEnum value : StatusEnum.values())
            if (value.getStatusDescription().equals(this.status))
                return true;
        return false;
    }


}
