package pt.unl.fct.di.apdc.flagnpatch.inputData;

import pt.unl.fct.di.apdc.flagnpatch.utils.StatusEnum;

public class StatusData {

    public String statusDescription;
    public String description;

    public StatusData() {
    }

    public StatusData(String statusDescription, String description) {
        this.statusDescription = statusDescription;
        this.description = description;
    }

    public boolean validStatusData() {
        return validField(this.statusDescription) &&
                validField(this.description) &&
                validStatusDescription();
    }

    private boolean validField(String value) {
        return value != null && !value.equals("");
    }


    private boolean validStatusDescription() {

        for (StatusEnum value : StatusEnum.values())
            if (value.getStatusDescription().equals(this.statusDescription))
                return true;
        return false;
    }
}
