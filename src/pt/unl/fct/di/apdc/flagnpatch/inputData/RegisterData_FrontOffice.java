package pt.unl.fct.di.apdc.flagnpatch.inputData;


import org.apache.commons.validator.routines.EmailValidator;
import pt.unl.fct.di.apdc.flagnpatch.resources.general.Utils;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public class RegisterData_FrontOffice extends RegisterData_Global {

    public RegisterData_FrontOffice() {
        super();
    }

    public RegisterData_FrontOffice(String name, String email, String email_confirm, String password,

                                    String password_confirm, AddressData address) {
        super(name, email, email_confirm, password, password_confirm, address);

    }

    public boolean validRegistrationForFrontOffice() {
        return validField(this.name) && validField(this.email) && validField(this.password)
                && validField(this.password_confirm) && super.emailsMatch() && super.passwordsMatch() && super.validEmail()
                && address != null && this.address.validAddress() ;
    }

}
