package pt.unl.fct.di.apdc.flagnpatch.inputData;

import java.util.ArrayList;

public class RegisterData_BackOffice extends RegisterData_Global {

    // Authentication attributes
    public String nif;
    public ArrayList<String> workingArea;

    public RegisterData_BackOffice() {
        super();
    }

    public RegisterData_BackOffice(String name, String email, String email_confirm, String password,

                                   String password_confirm, AddressData address, String nif,
                                   ArrayList<String> workingArea) {
        super(name, email, email_confirm, password, password_confirm, address);

        this.nif = nif;
        this.workingArea = workingArea;
    }

    public boolean validRegistration() {
        return super.validRegistration() &&
                super.validField(this.nif) &&
                this.workingArea!=null &&
                this.validateWorkingArea(this.workingArea);
    }


    private boolean validateWorkingArea(ArrayList<String> workingArea) {
        return !workingArea.isEmpty();
    }

}


