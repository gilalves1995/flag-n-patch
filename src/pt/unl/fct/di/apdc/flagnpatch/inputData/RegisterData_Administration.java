package pt.unl.fct.di.apdc.flagnpatch.inputData;

public class RegisterData_Administration extends RegisterData_Global {

	public static final String PROPERTY_INTERNAL_ID = "internalId";
	
    // Authentication attributes
    public String internalId;

    public RegisterData_Administration() {
        super();
    }

    public RegisterData_Administration(String name, String email, String email_confirm, String password,
                                       String password_confirm,  AddressData address, String internalId) {
        super(name, email, email_confirm, password, password_confirm, address);

        this.internalId = internalId;
    }

    public boolean validRegistration() {
        return super.validRegistration() &&
                super.validField(this.internalId);
    }

}
