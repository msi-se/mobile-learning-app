package de.htwg_konstanz.mobilelearning.models.feedback;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;


public class FeedbackChannel implements Serializable {
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

    public void addFeedbackForm(FeedbackForm feedbackForm) {
        this.feedbackForms.add(feedbackForm);
    }

    public void removeFeedbackForm(FeedbackForm feedbackForm) {
        this.feedbackForms.remove(feedbackForm);
    }

    public FeedbackForm getFeedbackFormById(ObjectId feedbackFormId) {
        for (FeedbackForm feedbackForm : this.feedbackForms) {
            if (feedbackForm.getId().equals(feedbackFormId)) {
                return feedbackForm;
            }
        }
        return null;
    }
}
