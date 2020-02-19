package pt.unl.fct.di.apdc.flagnpatch.inputData;

public class LoginData {

    // Authentication attributes
    public String email;
    public String password;

    // Empty constructor
    public LoginData() {
    }

    public LoginData(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public boolean validLoginData() {
        return validField(this.email) &&
                validField(this.password) &&
                validEmail();
    }

    private boolean validField(String value) {
        return value != null && !value.equals("");
    }

    private boolean validEmail() {
        return this.email.matches("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");

    }

}
