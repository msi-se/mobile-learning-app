package de.htwg_konstanz.mobilelearning.services.api.models;

import java.util.List;

public class ApiCourse {
    public String name;
    public String description;
    public List<ApiFeedbackForm> feedbackForms;
    public List<ApiQuizForm> quizForms;
    public String key;
    
    public ApiCourse() {
    }

    public ApiCourse(String name, String description, String key) {
        this.name = name;
        this.description = description;
        this.feedbackForms = List.of();
        this.quizForms = List.of();
        this.key = key;
    }

    public ApiCourse(String name, String description, List<ApiFeedbackForm> feedbackForms, List<ApiQuizForm> quizForms, String key) {
        this.name = name;
        this.description = description;
        this.feedbackForms = feedbackForms == null ? List.of() : feedbackForms;
        this.quizForms = quizForms == null ? List.of() : quizForms;
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<ApiFeedbackForm> getFeedbackForms() {
        return feedbackForms == null ? List.of() : feedbackForms;
    }

    public List<ApiQuizForm> getQuizForms() {
        return quizForms == null ? List.of() : quizForms;
    }

    public String getKey() {
        return key;
    }
}
