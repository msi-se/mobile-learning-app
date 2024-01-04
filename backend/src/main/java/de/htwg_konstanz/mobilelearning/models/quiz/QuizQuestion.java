package de.htwg_konstanz.mobilelearning.models.quiz;

import java.util.List;

import de.htwg_konstanz.mobilelearning.enums.QuizQuestionType;
import de.htwg_konstanz.mobilelearning.models.Question;

public class QuizQuestion extends Question {

    public Boolean hasCorrectAnswer;
    public String correctAnswer;
    public QuizQuestionType type;
    
    public QuizQuestion() {
    }

    public QuizQuestion(String name, String description, QuizQuestionType type, List<String> options, Boolean hasCorrectAnswer, String correctAnswer) {
        super(name, description, options);
        this.type = type;
        this.hasCorrectAnswer = hasCorrectAnswer;
        this.correctAnswer = correctAnswer;
    }

    public QuizQuestion copy() {
        return new QuizQuestion(this.name, this.description, this.type, this.options, this.hasCorrectAnswer, this.correctAnswer);
    }

    public Boolean getHasCorrectAnswer() {
        return this.hasCorrectAnswer;
    }

    public void setHasCorrectAnswer(Boolean hasCorrectAnswer) {
        this.hasCorrectAnswer = hasCorrectAnswer;
    }

    public String getCorrectAnswer() {
        return this.correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public Integer checkAnswer(String answer) {

        // add more complicated answer checking here later (fulltext comparison by AI, ...)
        if (this.correctAnswer.equals(answer)) {
            return 1;
        } else {
            return 0;
        }
    }
}
