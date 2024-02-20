package de.htwg_konstanz.mobilelearning.services.api.models;

import java.util.ArrayList;
import java.util.List;

public class ApiCourse {
    public String name;
    public String description;
    public List<ApiFeedbackForm> feedbackForms;
    public List<ApiQuizForm> quizForms;
    public String key;
    public String moodleCourseId;
    
    public ApiCourse() {
    }

    public ApiCourse(String name, String description, String key) {
        this.name = name;
        this.description = description;
        this.feedbackForms = new ArrayList<ApiFeedbackForm>();
        this.quizForms = new ArrayList<ApiQuizForm>();
        this.key = key;
    }

    public ApiCourse(String name, String description, List<ApiFeedbackForm> feedbackForms, List<ApiQuizForm> quizForms, String key, String moodleCourseId) {
        this.name = name;
        this.description = description;
        this.feedbackForms = feedbackForms == null ? new ArrayList<ApiFeedbackForm>() : feedbackForms;
        this.quizForms = quizForms == null ? new ArrayList<ApiQuizForm>() : quizForms;
        this.key = key;
        this.moodleCourseId = moodleCourseId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<ApiFeedbackForm> getFeedbackForms() {
        return feedbackForms == null ? new ArrayList<ApiFeedbackForm>() : feedbackForms;
    }

    public List<ApiQuizForm> getQuizForms() {
        return quizForms == null ? new ArrayList<ApiQuizForm>() : quizForms;
    }

    public String getKey() {
        return key;
    }

    public String getMoodleCourseId() {
        return moodleCourseId;
    }
}
