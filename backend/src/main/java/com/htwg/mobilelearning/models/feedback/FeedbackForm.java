package com.htwg.mobilelearning.models.feedback;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import com.htwg.mobilelearning.enums.FeedbackChannelStatus;

public class FeedbackForm {
    public ObjectId id;
    public String name;
    public String description;
    public List<FeedbackElement> elements;
    public FeedbackChannelStatus status;

    public FeedbackForm() {
    }

    public FeedbackForm(String name, String description, List<FeedbackElement> elements, Boolean isStarted, Boolean isFinished) {
        this.id = new ObjectId();
        this.name = name;
        this.description = description;
        this.elements = elements != null ? elements : new ArrayList<FeedbackElement>();
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

    public List<FeedbackElement> getElements() {
        return this.elements;
    }

}

