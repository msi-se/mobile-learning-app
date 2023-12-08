package com.htwg.mobilelearning.models.feedback;

import org.bson.types.ObjectId;

public class FeedbackResult {
    public ObjectId resultId;
    public ObjectId userId;
    public String value;

    public FeedbackResult() {
    }

    public FeedbackResult(ObjectId userId, String value) {
        this.resultId = new ObjectId();
        this.userId = userId;
        this.value = value;
    }

    public ObjectId getResultId() {
        return this.resultId;
    }

    public ObjectId getUserId() {
        return this.userId;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
