package de.htwg_konstanz.mobilelearning.services.api.models;

import java.util.ArrayList;
import java.util.List;

public class ApiFeedbackForm {

    public static class ApiFeedbackQuestion {
        public String name;
        public String description;
        public String type; // SLIDER, STARS, FULLTEXT, YES_NO, SINGLE_CHOICE
        public List<String> options;
        public String key;
        public String rangeLow;
        public String rangeHigh;

        public ApiFeedbackQuestion() {
        }

        public ApiFeedbackQuestion(String name, String description, String type, List<String> options, String key, String rangeLow, String rangeHigh) {
            this.name = name;
            this.description = description;
            this.type = type;
            this.options = options;
            this.key = key;
            this.rangeLow = rangeLow;
            this.rangeHigh = rangeHigh;
        }

        public String getKey() { return this.key; }
        public String getName() { return this.name; }
        public String getDescription() { return this.description; }
        public String getType() { return this.type; }
        public List<String> getOptions() {
            return this.options != null ? this.options : new ArrayList<String>();
        }
        public String getRangeLow() { return this.rangeLow; }
        public String getRangeHigh() { return this.rangeHigh; }
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

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<ApiFeedbackQuestion> getQuestions() {
        return questions == null ? new ArrayList<ApiFeedbackQuestion>() : questions;
    }
}
