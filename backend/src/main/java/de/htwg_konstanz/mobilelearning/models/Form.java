package de.htwg_konstanz.mobilelearning.models;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import de.htwg_konstanz.mobilelearning.enums.FormStatus;

public abstract class Form {
    public ObjectId id;
    public ObjectId courseId;
    public String name;
    public String description;
    public List<QuestionWrapper> questions;
    public FormStatus status;
    public Integer connectCode;
    public String key;

    public Form() {
    }

    public Form(ObjectId courseId, String name, String description, List<QuestionWrapper> questions, FormStatus status) {
        this.id = new ObjectId();
        this.courseId = courseId;
        this.name = name;
        this.description = description;
        this.questions = questions != null ? questions : new ArrayList<QuestionWrapper>();
        this.status = status;

        this.key = "";

        // generate 6-digit connect code (100000 - 999999)
        this.connectCode = (int) (Math.random() * 899999) + 100000;
    }

    public ObjectId getId() {
        return this.id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public ObjectId getCourseId() {
        return this.courseId;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public List<QuestionWrapper> getQuestions() {
        return this.questions;
    }

    public FormStatus getStatus() {
        return this.status;
    }

    public Integer getConnectCode() {
        return this.connectCode;
    }

    public void addQuestion(QuestionWrapper element) {
        this.questions.add(element);
    }

    public void removeQuestion(QuestionWrapper element) {
        this.questions.remove(element);
    }

    public QuestionWrapper getQuestionById(ObjectId elementId) {
        for (QuestionWrapper element : this.questions) {
            if (element.getId().equals(elementId)) {
                return element;
            }
        }
        return null;
    }

    public void setStatus(FormStatus status) {
        this.status = status;
    }

    public void clearResults() {
        for (QuestionWrapper element : this.questions) {
            element.clearResults();
        }
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }

    public void setQuestions(List<QuestionWrapper> questions) {
        this.questions = questions;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
