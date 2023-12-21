package de.htwg_konstanz.mobilelearning.helper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
class MoodleCourse {
    public Integer id;
    public String fullname;
    public String shortname;
    public String summary;
    public Integer lastaccess;
    public Boolean isfavourite;
    public Boolean hidden;
    public Integer startdate;
    public Integer enddate;

    public MoodleCourse(
        @JsonProperty("id") Integer id,
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
