package de.htwg_konstanz.mobilelearning.services.api.models;

import java.util.List;

public class ApiQuizForm {

    public static class ApiQuizQuestion {
        public String name;
        public String description;
        public String type; // YES_NO, SINGLE_CHOICE, MULTIPLE_CHOICE, WORD_CLOUD, FULLTEXT
        public List<String> options;
        public Boolean hasCorrectAnswer;
        public String correctAnswer;

        public ApiQuizQuestion() {
        }

        public ApiQuizQuestion(String name, String description, String type, List<String> options,
                Boolean hasCorrectAnswer, String correctAnswer) {
            this.name = name;
            this.description = description;
            this.type = type;
            this.options = options;
            this.hasCorrectAnswer = hasCorrectAnswer;
            this.correctAnswer = correctAnswer;
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

}
