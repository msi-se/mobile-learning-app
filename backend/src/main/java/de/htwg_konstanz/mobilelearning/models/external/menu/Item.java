package de.htwg_konstanz.mobilelearning.models.external.menu;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Structure of a menu item.
 * Represents structre of xml data provided by Seezeit. 
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Item implements Serializable {

    public Item() {
    }

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

    public String getPreis1() {
        return preis1;
    }

    public void setPreis1(String preis1) {
        this.preis1 = preis1;
    }

    public String getBeilagen() {
        return beilagen;
    }

    public void setBeilagen(String beilagen) {
        this.beilagen = beilagen;
    }

    public String getPreis2() {
        return preis2;
    }

    public void setPreis2(String preis2) {
        this.preis2 = preis2;
    }

    public String getPreis3() {
        return preis3;
    }

    public void setPreis3(String preis3) {
        this.preis3 = preis3;
    }

    public String getEinheit() {
        return einheit;
    }

    public void setEinheit(String einheit) {
        this.einheit = einheit;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getKennzeichnungen() {
        return kennzeichnungen;
    }

    public void setKennzeichnungen(String kennzeichnungen) {
        this.kennzeichnungen = kennzeichnungen;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIcons() {
        return icons;
    }

    public void setIcons(String icons) {
        this.icons = icons;
    }

}
