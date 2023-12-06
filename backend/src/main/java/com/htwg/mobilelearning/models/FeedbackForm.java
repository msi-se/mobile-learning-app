package com.htwg.mobilelearning.models;

import java.util.ArrayList;
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

    public FeedbackForm(String name, String description, List<Element> elements, Boolean isStarted, Boolean isFinished) {
        this.id = new ObjectId();
        this.name = name;
        this.description = description;
        this.elements = elements != null ? elements : new ArrayList<Element>();
        this.isStarted = isStarted != null ? isStarted : false;
        this.isFinished = isFinished != null ? isFinished : false;
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

    public Boolean getIsStarted() {
        return this.isStarted;
    }

    public Boolean getIsFinished() {
        return this.isFinished;
    }

    public void setIsStarted(Boolean isStarted) {
        this.isStarted = isStarted;
    }

    public void setIsFinished(Boolean isFinished) {
        this.isFinished = isFinished;
    }

}

