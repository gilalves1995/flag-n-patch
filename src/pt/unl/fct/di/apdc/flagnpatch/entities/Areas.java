package pt.unl.fct.di.apdc.flagnpatch.entities;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by michael on 28-06-2017.
 */
public class Areas {

    public static final String KIND="Areas";

    public static final String PROPERTY_COUNTY="county";
    public static final String PROPERTY_RESPONSIBLE="responsible";
    public static final String PROPERTY_CREATION_DATE="creationDate";
    public static final String PROPERTY_IS_AVAILABLE="isAvailable";
    //public static final String PROPERTY_COUNTY22="county";

    public String county;
    public String responsible;
    public String creationDate;
    public boolean isAvailable;

    public Areas() {

    }

    public Areas(String responsible, String county) {
        this.responsible = responsible;
        this.county = county;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Lisbon"));
        this.creationDate = sdf.format(new Date());
        this.isAvailable = true;
    }


}
