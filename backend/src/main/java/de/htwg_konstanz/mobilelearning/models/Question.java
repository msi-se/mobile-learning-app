package de.htwg_konstanz.mobilelearning.models;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import de.htwg_konstanz.mobilelearning.enums.FeedbackQuestionType;

public abstract class Question {
    
    public ObjectId id;
    public String name;
    public String description;
    public FeedbackQuestionType type;
    public List<String> options;

    public Question() {
    }

    public Question(String name, String description, FeedbackQuestionType type, List<String> options) {
        this.id = new ObjectId();
        this.name = name;
        this.description = description;
        this.type = type;
        this.options = options != null ? options : new ArrayList<String>();
    }

    public ObjectId getId() {
        return this.id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public FeedbackQuestionType getType() {
        return this.type;
    }

    public List<String> getOptions() {
        return this.options;
    }

    public abstract Question copy();
}
