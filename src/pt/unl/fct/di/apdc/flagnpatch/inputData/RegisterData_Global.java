package pt.unl.fct.di.apdc.flagnpatch.inputData;

import org.apache.commons.validator.routines.EmailValidator;

public class RegisterData_Global {

    // Authentication attributes
    public String name;
    public String email;
    public String email_confirm;
    public String password;
    public String password_confirm;

    // Personal info attributes
    public AddressData address;

    // Empty constructor

    RegisterData_Global() {
    	
    }

    RegisterData_Global(String name, String email, String email_confirm, String password,
                        String password_confirm, AddressData address) {

        this.name = name;
        this.email = email.toLowerCase();
        this.email_confirm = email_confirm.toLowerCase();
        this.password = password;
        this.password_confirm = password_confirm;
        this.address = address;
    }

    public boolean validRegistration() {
        return validField(this.name) && validField(this.email) && validField(this.password)
                && validField(this.password_confirm) && emailsMatch() && passwordsMatch() && validEmail()
                && address!=null && this.address.validAddress() ;
    }


    boolean validField(String value) {
        return value != null && !value.equals("");
    }

    protected boolean emailsMatch() {
        return this.email.equals(this.email_confirm);
    }

    protected boolean passwordsMatch() {
        return this.password.equals(this.password_confirm);
    }

    protected boolean validEmail() {
        return this.email.matches("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
    }
}
