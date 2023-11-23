package com.htwg.mobilelearning.models;

import java.util.List;

import org.bson.types.ObjectId;

public class FeedbackForm {
    public ObjectId id;
    public String name;
    public String description;
    public List<Element> elements;
    public Boolean isStarted;
    public Boolean isFinished;

    public FeedbackForm() {
    }

    public FeedbackForm(String name, String description, List<Element> elements) {
        this.id = new ObjectId();
        this.name = name;
        this.description = description;
        this.elements = elements;
    }

    public ObjectId getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public List<Element> getElements() {
        return this.elements;
    }
}
