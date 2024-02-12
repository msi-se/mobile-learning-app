package de.htwg_konstanz.mobilelearning.helper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Speiseplan {
    
    @JacksonXmlElementWrapper(useWrapping = false)
    @JsonProperty("tag")
    public Tag[] tag;
}

@JsonIgnoreProperties(ignoreUnknown = true)
class Tag {
    
    @JacksonXmlElementWrapper(useWrapping = false)
    @JsonProperty("item")
    public Item[] item;
    
    @JsonProperty("timestamp")
    public String timestamp;
}

@JsonIgnoreProperties(ignoreUnknown = true)
class Item {
    
    @JsonProperty("preis1")
    public String preis1;
    
    @JsonProperty("beilagen")
    public String beilagen;
    
    @JsonProperty("preis2")
    public String preis2;
    
    @JsonProperty("preis3")
    public String preis3;
    
    @JsonProperty("einheit")
    public String einheit;
    
    @JsonProperty("color")
    public String color;
    
    @JsonProperty("kennzeichnungen")
    public String kennzeichnungen;
    
    @JsonProperty("description")
    public String description;
    
    @JsonProperty("language")
    public String language;
    
    @JsonProperty("category")
    public String category;
    
    @JsonProperty("title")
    public String title;
    
    @JsonProperty("icons")
    public String icons;
}