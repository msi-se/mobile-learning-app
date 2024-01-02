package de.htwg_konstanz.mobilelearning.models.quiz;

import java.util.List;

import de.htwg_konstanz.mobilelearning.enums.FeedbackQuestionType;
import de.htwg_konstanz.mobilelearning.models.Question;

public class QuizQuestion extends Question {

    public Boolean hasCorrectAnswer;
    public String correctAnswer;
    
    public QuizQuestion() {
    }

    public QuizQuestion(String name, String description, FeedbackQuestionType type, List<String> options, String correctAnswer) {
        super(name, description, type, options);
        this.correctAnswer = correctAnswer;
    }

    public QuizQuestion copy() {
        return new QuizQuestion(this.name, this.description, this.type, this.options, this.correctAnswer);
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
