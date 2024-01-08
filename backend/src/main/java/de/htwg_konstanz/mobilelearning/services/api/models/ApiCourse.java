package de.htwg_konstanz.mobilelearning.services.api.models;

import java.util.List;

public class ApiCourse {
    public String name;
    public String description;
    public List<ApiFeedbackForm> feedbackForms;
    public List<ApiQuizForm> quizForms;
    
    public ApiCourse() {
    }

    public ApiCourse(String name, String description) {
        this.name = name;
        this.description = description;
        this.feedbackForms = List.of();
        this.quizForms = List.of();
    }

    public ApiCourse(String name, String description, List<ApiFeedbackForm> feedbackForms, List<ApiQuizForm> quizForms) {
        this.name = name;
        this.description = description;
        this.feedbackForms = feedbackForms;
        this.quizForms = quizForms;
    }
}
