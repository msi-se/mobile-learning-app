package de.htwg_konstanz.mobilelearning.services.api.models;

import java.util.List;

public class ApiFeedbackForm {

    public static class ApiFeedbackQuestion {
        public String name;
        public String description;
        public String type; // SLIDER, STARS, FULLTEXT, YES_NO, SINGLE_CHOICE
        public List<String> options;

        public ApiFeedbackQuestion() {
        }

        public ApiFeedbackQuestion(String name, String description, String type, List<String> options, String key) {
            this.name = name;
            this.description = description;
            this.type = type;
            this.options = options;
        }
    }

    public String name;
    public String description;
    public List<ApiFeedbackQuestion> questions;
    public String key;
    
    public ApiFeedbackForm() {
    }

    public ApiFeedbackForm(String name, String description, List<ApiFeedbackQuestion> questions, String key) {
        this.name = name;
        this.description = description;
        this.questions = questions;
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
