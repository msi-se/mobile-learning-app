package de.htwg_konstanz.mobilelearning.helper.moodle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MoodleCourse {
    public String id;
    public String fullname;
    public String shortname;
    public String summary;
    public Integer lastaccess;
    public Boolean isfavourite;
    public Boolean hidden;
    public Integer startdate;
    public Integer enddate;

    public MoodleCourse(
        @JsonProperty("id") String id,
        @JsonProperty("fullname") String fullname,
        @JsonProperty("shortname") String shortname,
        @JsonProperty("summary") String summary,
        @JsonProperty("lastaccess") Integer lastaccess,
        @JsonProperty("isfavourite") Boolean isfavourite,
        @JsonProperty("hidden") Boolean hidden,
        @JsonProperty("startdate") Integer startdate,
        @JsonProperty("enddate") Integer enddate
    ) {
        this.id = id;
        this.fullname = fullname;
        this.shortname = shortname;
        this.summary = summary;
        this.lastaccess = lastaccess;
        this.isfavourite = isfavourite;
        this.hidden = hidden;
        this.startdate = startdate;
        this.enddate = enddate;
    }

    public MoodleCourse(String id) {
        this.id = id;
        this.fullname = "";
        this.shortname = "";
        this.summary = "";
        this.lastaccess = 0;
        this.isfavourite = false;
        this.hidden = false;
        this.startdate = 0;
        this.enddate = 0;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "MoodleCourse{" +
            "id=" + id +
            ", fullname='" + fullname + '\'' +
            ", shortname='" + shortname + '\'' +
            ", summary='" + summary + '\'' +
            ", lastaccess=" + lastaccess +
            ", isfavourite=" + isfavourite +
            ", hidden=" + hidden +
            ", startdate=" + startdate +
            ", enddate=" + enddate +
            '}';
    }
}
