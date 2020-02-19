package pt.unl.fct.di.apdc.flagnpatch.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class Comment {

    public static final String KIND = "Comment";

    public static final String PROPERTY_CONTENT = "content";

    public static final String PROPERTY_AUTHOR_EMAIL = "authorEmail";
    public static final String PROPERTY_AUTHOR_NAME = "authorName";
    public static final String PROPERTY_AUTHOR_IDENTIFIER = "authorIdentifier";
    public static final String PROPERTY_DATE = "registerDate";
    public static final String PROPERTY_TIME = "registerTime";

    public String content;
    public String authorEmail;
    public String authorName;
    public int authorIdentifier;

    public Long registerTime;
    public String registerDate;

    public Comment() {

    }


    public Comment(String content, String authorEmail, long registerTime) {
        this.content = content;
        this.authorEmail = authorEmail;
        this.registerTime = registerTime;
    }

    public Comment(String content, String authorEmail, int authorIdentifier, long registerTime) {
        this.content = content;
        this.authorIdentifier = authorIdentifier;
        this.authorEmail = authorEmail;
        this.registerTime = registerTime;
    }

    public Comment(String content, String authorEmail,
                   String authorName, int authorIdentifier,
                   long registerTime) {
        this.content = content;
        this.authorEmail = authorEmail;
        this.authorName = authorName;
        this.authorIdentifier = authorIdentifier;
        this.registerTime=registerTime;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Lisbon"));
        this.registerDate=sdf.format(registerTime);
    }
}
