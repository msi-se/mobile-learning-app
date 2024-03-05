package de.htwg_konstanz.mobilelearning.models;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;

import de.htwg_konstanz.mobilelearning.models.feedback.FeedbackQuestion;
import de.htwg_konstanz.mobilelearning.models.quiz.QuizQuestion;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Contains name & description of a question.
 * Options are the possible answers to the question.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({ 
    @JsonSubTypes.Type(QuizQuestion.class),
    @JsonSubTypes.Type(FeedbackQuestion.class)
})
public class Question {
    
    public ObjectId id;
    public String name;
    public String description;
    public List<String> options;
    public String key;

    public Question() {
    }

    public Question(String name, String description, List<String> options, String key) {
        this.id = new ObjectId();
        this.name = name;
        this.description = description;
        this.options = options != null ? options : new ArrayList<String>();
        this.key = key;
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

    public void setName(String title) {
        this.name = title;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String text) {
        this.description = text;
    }

    public List<String> getOptions() {
        return this.options;
    }

    public void setOptions(List<String> options) {
        this.options = options != null ? options : new ArrayList<String>();
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Question copy() {
        return new Question(this.name, this.description, this.options, this.key);
    }

}
