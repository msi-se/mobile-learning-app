package de.htwg_konstanz.mobilelearning.models.feedback;

import org.bson.types.ObjectId;

public class FeedbackParticipant {

    public ObjectId userId;

    public FeedbackParticipant() {
    }

    public FeedbackParticipant(ObjectId userId) {
        this.userId = userId;
    }

    public ObjectId getUserId() {
        return this.userId;
    }

}
