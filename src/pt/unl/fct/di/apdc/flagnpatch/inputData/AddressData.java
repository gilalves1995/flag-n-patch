package pt.unl.fct.di.apdc.flagnpatch.inputData;

public class AddressData {


    public static final String KIND = "address";

    public static final String PROPERTY_DISTRICT = "district";
    public static final String PROPERTY_COUNTY = "county";

    public String district;
    public String county;

    // Empty constructor
    public AddressData() {

    }

    public AddressData(String district, String county) {
        this.district = district;
        this.county = county;
    }

    public boolean validAddress() {
        return validField(this.district) &&
                validField(this.county);
    }

    private boolean validField(String value) {
        return value != null && !value.equals("");
    }
}
