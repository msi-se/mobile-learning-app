package de.htwg_konstanz.mobilelearning.models.feedback;

import java.util.List;

import de.htwg_konstanz.mobilelearning.enums.FeedbackQuestionType;
import de.htwg_konstanz.mobilelearning.models.Question;

public class FeedbackQuestion extends Question {

    public FeedbackQuestionType type;

    public FeedbackQuestion() {
    }

    public FeedbackQuestion(String name, String description, FeedbackQuestionType type, List<String> options) {
        super(name, description, options);
        this.type = type;
    }

    public FeedbackQuestion copy() {
        FeedbackQuestion copy = new FeedbackQuestion(this.name, this.description, this.type, this.options);
        copy.id = this.id;
        return copy;
    }

    public void setType(FeedbackQuestionType type) {
        this.type = type;
    }
}
