package com.htwg.mobilelearning.models.feedback;

import org.bson.types.ObjectId;

public class FeedbackResult {
    public ObjectId resultId;
    public ObjectId feedbackElementId;
    public ObjectId userId;
    public Object value;
}
