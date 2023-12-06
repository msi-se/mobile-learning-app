package com.htwg.mobilelearning.models.feedback;

import org.bson.types.ObjectId;

import com.htwg.mobilelearning.enums.FeedbackElementType;

public class FeedbackElement {
    public ObjectId id;
    public String name;
    public String description;
    public FeedbackElementType type;
    public Object value;

    public FeedbackElement() {
    }

    public FeedbackElement(String name, String description, FeedbackElementType type, Object value) {
        this.id = new ObjectId();
        this.name = name;
        this.description = description;
        this.type = type;
        this.value = value;
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

    public Object getValue() {
        return this.value;
    }
}
