package de.htwg_konstanz.mobilelearning.models.feedback;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import de.htwg_konstanz.mobilelearning.enums.FeedbackChannelStatus;

public class FeedbackForm implements Serializable {
    public ObjectId id;
    public ObjectId feedbackChannelId;
    public String name;
    public String description;
    public List<FeedbackElement> elements;
    public FeedbackChannelStatus status;
    public Integer connectCode;

    public FeedbackForm() {
    }

    public FeedbackForm(ObjectId feedbackChannelId, String name, String description, List<FeedbackElement> elements, FeedbackChannelStatus status) {
        this.id = new ObjectId();
        this.feedbackChannelId = feedbackChannelId;
        this.name = name;
        this.description = description;
        this.elements = elements != null ? elements : new ArrayList<FeedbackElement>();
        this.status = status;

        // generate 6-digit connect code (100000 - 999999)
        this.connectCode = (int) (Math.random() * 899999) + 100000;
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

    public FeedbackChannelStatus getStatus() {
        return this.status;
    }

    public Integer getConnectCode() {
        return this.connectCode;
    }

    public void addElement(FeedbackElement element) {
        this.elements.add(element);
    }

    public void removeElement(FeedbackElement element) {
        this.elements.remove(element);
    }

    public FeedbackElement getElementById(ObjectId elementId) {
        for (FeedbackElement element : this.elements) {
            if (element.getId().equals(elementId)) {
                return element;
            }
        }
        return null;
    }

    public void setStatus(FeedbackChannelStatus status) {
        this.status = status;
    }



}

