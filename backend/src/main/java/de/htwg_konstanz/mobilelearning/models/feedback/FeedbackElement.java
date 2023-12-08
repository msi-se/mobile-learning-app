package de.htwg_konstanz.mobilelearning.models.feedback;

import java.util.List;

import org.bson.types.ObjectId;

import de.htwg_konstanz.mobilelearning.enums.FeedbackElementType;

public class FeedbackElement {
    public ObjectId id;
    public String name;
    public String description;
    public FeedbackElementType type;
    public List<FeedbackResult> results;

    public FeedbackElement() {
    }

    public FeedbackElement(String name, String description, FeedbackElementType type, List<FeedbackResult> results) {
        this.id = new ObjectId();
        this.name = name;
        this.description = description;
        this.type = type;
        this.results = results != null ? results : new java.util.ArrayList<FeedbackResult>();
    }

    public FeedbackElement(String name, String description, FeedbackElementType type) {
        this.id = new ObjectId();
        this.name = name;
        this.description = description;
        this.type = type;
        this.results = new java.util.ArrayList<FeedbackResult>();
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

    public FeedbackElementType getType() {
        return this.type;
    }

    public List<FeedbackResult> getResults() {
        return this.results;
    }

    public void addResult(FeedbackResult result) {
        this.results.add(result);
    }

    public void removeResult(FeedbackResult result) {
        this.results.remove(result);
    }

    public FeedbackResult getResultByUserId(ObjectId userId) {
        for (FeedbackResult result : this.results) {
            if (result.getUserId().equals(userId)) {
                return result;
            }
        }
        return null;
    }
}
