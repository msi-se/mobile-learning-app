package com.htwg.mobilelearning.models;
import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;


public class FeedbackChannel {
    public ObjectId id;
    public String name;
    public String description;
    public List<FeedbackForm> feedbackForms;

    public FeedbackChannel() {
    }

    public FeedbackChannel(String name, String description, List<FeedbackForm> feedbackForms) {
        this.id = new ObjectId();
        this.name = name;
        this.description = description;
        this.feedbackForms = feedbackForms != null ? feedbackForms : new ArrayList<FeedbackForm>();
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

    public List<FeedbackForm> getFeedbackForms() {
        return this.feedbackForms;
    }
}
