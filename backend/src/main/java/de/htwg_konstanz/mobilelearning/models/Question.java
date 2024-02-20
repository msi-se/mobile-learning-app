package de.htwg_konstanz.mobilelearning.models;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

/**
 * Contains name & description of a question.
 * Options are the possible answers to the question.
 */
public abstract class Question {
    
    public ObjectId id;
    public String name;
    public String description;
    public List<String> options;
    public String key;

    public Question() {
    }

    public Question(String name, String description, List<String> options, String key) {
        this.id = new ObjectId();
        this.name = name;
        this.description = description;
        this.options = options != null ? options : new ArrayList<String>();
        this.key = key;
    }

    public ObjectId getId() {
        return this.id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String title) {
        this.name = title;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String text) {
        this.description = text;
    }

    public List<String> getOptions() {
        return this.options;
    }

    public void setOptions(List<String> options) {
        this.options = options != null ? options : new ArrayList<String>();
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public abstract Question copy();

}
