package de.htwg_konstanz.mobilelearning.services.api.models;

import java.util.ArrayList;
import java.util.List;

public class ApiQuizForm {

    public static class ApiQuizQuestion {
        public String name;
        public String description;
        public String type; // YES_NO, SINGLE_CHOICE, MULTIPLE_CHOICE, WORD_CLOUD, FULLTEXT
        public List<String> options;
        public Boolean hasCorrectAnswers;
        public List<String> correctAnswers;
        public String key;

        public ApiQuizQuestion() {
        }

        public ApiQuizQuestion(String name, String description, String type, List<String> options,
                Boolean hasCorrectAnswers, List<String> correctAnswers, String key) {
            this.name = name;
            this.description = description;
            this.type = type;
            this.options = options;
            this.hasCorrectAnswers = hasCorrectAnswers;
            this.correctAnswers = correctAnswers;
            this.key = key;
        }

        public String getKey() { return this.key; }
        public String getName() { return this.name; }
        public String getDescription() { return this.description; }
        public String getType() { return this.type; }
        public Boolean getHasCorrectAnswers() { return this.hasCorrectAnswers; }
        public List<String> getOptions() {
            return this.options != null ? this.options : new ArrayList<String>();
        }
        public List<String> getCorrectAnswers() {
            return this.correctAnswers != null ? this.correctAnswers : new ArrayList<String>();
        }
    }

    public String name;
    public String description;
    public List<ApiQuizQuestion> questions;
    public String key;

    public ApiQuizForm() {
    }

    public ApiQuizForm(String name, String description, List<ApiQuizQuestion> questions, String key) {
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

    public List<ApiQuizQuestion> getQuestions() {
        return questions != null ? questions : new ArrayList<ApiQuizQuestion>();
    }

}
