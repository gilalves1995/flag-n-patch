package pt.unl.fct.di.apdc.flagnpatch.inputData;


public class ReportData {

    public static final String KIND = "Report";

    private static final int MAX_PRIORITY_VALUE = 5;


    public String type;
    public int priority;
    public String description;
    public String imageUrl;
    public String addressAsStreet;

    public Double lat, lng;
    public AddressData address;

    public ReportData() {}

    public ReportData(String type, int priority, String description, String imageUrl,
                      String addressAsStreet, double lat, double lng, AddressData address) {
        this.type = type;

        this.priority = priority;
        this.description = description;
        this.imageUrl = imageUrl;
        this.addressAsStreet = addressAsStreet;
        this.lat = lat;
        this.lng = lng;
        this.address = address;
    }

    // Methods left as private because they might be used individually in the future

    private boolean validPriorityValue() {

        return this.priority > 0 && this.priority <= MAX_PRIORITY_VALUE;
    }

    public boolean validReport() {

        return validPriorityValue() && validateField(this.type) && validateField(this.description) && validateField(this.imageUrl)
                && validateField(this.addressAsStreet) && this.lat!=null && this.lng!=null && isValidLatLng(this.lat, this.lng) && address!=null && validateAddress(this.address);
    }

    private boolean isValidLatLng(double lat, double lng) {
        if(lat==Double.NaN || lng==Double.NaN)
            return false;
        if (lat < -90 || lat > 90) {
            return false;
        } else if (lng < -180 || lng > 180) {
            return false;
        }
        return true;
    }

    private boolean validateField(String value) {
        return value != null && !value.equals("");
    }

    private boolean validateAddress(AddressData address) {
        return validateField(address.county) && validateField(address.district);
    }

}
