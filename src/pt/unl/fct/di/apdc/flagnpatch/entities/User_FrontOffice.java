package pt.unl.fct.di.apdc.flagnpatch.entities;

import pt.unl.fct.di.apdc.flagnpatch.inputData.AddressData;

import java.io.Serializable;

public class User_FrontOffice extends User_Global implements Serializable {

    public static final String PROPERTY_NUMBER_REPORTS = "numberReports";
    public static final String PROPERTY_IDENTIFIER = "identifier";
    public int numberReports;
    public int identifier;


    public User_FrontOffice() {
        super();
    }


    public User_FrontOffice(String name, String email, AddressData address, String role,
                            int numberReports, int identifier) {
        super(name, email,  address, role);
        this.numberReports = numberReports;
        this.identifier = identifier;
    }

    public User_FrontOffice(String name, String email, AddressData address, String role,
                            int numberReports) {
        super(name, email, address, role);
        this.numberReports = numberReports;
    }
}
