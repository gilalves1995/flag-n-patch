package pt.unl.fct.di.apdc.flagnpatch.entities;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Created by michael on 04-07-2017.
 */
public class Graph {
    public static final String KIND = "GraphData";

    public static final String PROPERTY_COUNTY = "county";
    public static final String PROPERTY_ACTIVE = "active";
    public static final String PROPERTY_DATE_DAY = "day";
    public static final String PROPERTY_TOTAL_EVER = "isTotal";

    public String county;
    public boolean active;
    public String date;
    public boolean isTotal;

    public Graph() {

    }

    public Graph(String county, boolean active, long dateLong, boolean isTotal) {
        this.county = county;
        this.active = active;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Lisbon"));
        this.date = sdf.format(dateLong);
        this.isTotal=isTotal;
    }
}
