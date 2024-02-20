package de.htwg_konstanz.mobilelearning.models.external.menu;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

/**
 * Structure of a menu day.
 * Constists of a list of items and a timestamp. 
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Day implements Serializable {

    @JacksonXmlElementWrapper(useWrapping = false)
    @JsonProperty("item")
    public List<Item> items;

    @JsonProperty("timestamp")
    public String timestamp;

    public Day() {
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> item) {
        this.items = item;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
