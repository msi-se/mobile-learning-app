package de.htwg_konstanz.mobilelearning.models.external.menu;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Menu implements Serializable {

    @JacksonXmlElementWrapper(useWrapping = false)
    @JsonProperty("tag")
    public List<Day> days;

    public Menu() {
    }

    public List<Day> getDays() {
        return days;
    }

    public void setDays(List<Day> tag) {
        this.days = tag;
    }
}

