package de.htwg_konstanz.mobilelearning.models.quiz;

import java.util.List;

import de.htwg_konstanz.mobilelearning.enums.QuestionType;
import de.htwg_konstanz.mobilelearning.models.Question;

public class QuizQuestion extends Question {

    public String correctAnswer;
    
    public QuizQuestion() {
    }

    public QuizQuestion(String name, String description, QuestionType type, List<String> options, String correctAnswer) {
        super(name, description, type, options);
        this.correctAnswer = correctAnswer;
    }

    public QuizQuestion copy() {
        return new QuizQuestion(this.name, this.description, this.type, this.options, this.correctAnswer);
    }
}
