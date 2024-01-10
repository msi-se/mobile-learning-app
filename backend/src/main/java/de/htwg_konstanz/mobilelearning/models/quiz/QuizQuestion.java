package de.htwg_konstanz.mobilelearning.models.quiz;

import java.util.List;

import de.htwg_konstanz.mobilelearning.enums.QuizQuestionType;
import de.htwg_konstanz.mobilelearning.models.Question;

public class QuizQuestion extends Question {

    public Boolean hasCorrectAnswers;
    public List<String> correctAnswers;
    public QuizQuestionType type;
    
    public QuizQuestion() {
    }

    public QuizQuestion(String name, String description, QuizQuestionType type, List<String> options, Boolean hasCorrectAnswers, List<String> correctAnswers) {
        super(name, description, options);
        this.type = type;
        this.hasCorrectAnswers = hasCorrectAnswers;
        this.correctAnswers = correctAnswers;
    }

    public QuizQuestion copy() {
        return new QuizQuestion(this.name, this.description, this.type, this.options, this.hasCorrectAnswers, this.correctAnswers);
    }

    public Boolean getHasCorrectAnswers() {
        return this.hasCorrectAnswers;
    }

    public void setHasCorrectAnswers(Boolean hasCorrectAnswers) {
        this.hasCorrectAnswers = hasCorrectAnswers;
    }

    public List<String> getCorrectAnswers() {
        return this.correctAnswers;
    }

    public void setCorrectAnswers(List<String> correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public Integer checkAnswer(List<String> answer) {

        // add more complicated answer checking here later (fulltext comparison by AI, ...)
        if (this.correctAnswers.equals(answer)) {
            return 1;
        } else {
            return 0;
        }
    }

    public void setType(QuizQuestionType type) {
        this.type = type;
    }
}
