package pt.unl.fct.di.apdc.flagnpatch.entities;

import pt.unl.fct.di.apdc.flagnpatch.inputData.AddressData;

import java.io.Serializable;
import java.util.ArrayList;

public class User_BackOffice extends User_Global implements Serializable {


    public static final String PROPERTY_NIF = "nif";
    public static final String PROPERTY_WORKING_AREA = "workingArea";
    public static final String PROPERTY_WORKER_INFO = "workerInfo";

    public String nif;
    public ArrayList<String> workingArea;
    public String workerInfo;

    public User_BackOffice() {
        super();
    }

    public User_BackOffice(String name, String email, AddressData address, String role,
                           String nif, ArrayList<String> workingArea, String workerInfo) {
        super(name, email,  address, role);
        this.nif = nif;
        this.workingArea = workingArea;
        this.workerInfo = workerInfo;
    }
}
