package de.htwg_konstanz.mobilelearning.models.feedback;

import java.util.List;

import de.htwg_konstanz.mobilelearning.enums.FeedbackQuestionType;
import de.htwg_konstanz.mobilelearning.models.Question;

public class FeedbackQuestion extends Question {

    public FeedbackQuestionType type;
    public String rangeLow;
    public String rangeHigh;

    public FeedbackQuestion() {
    }

    public FeedbackQuestion(String name, String description, FeedbackQuestionType type, List<String> options, String key, String rangeLow, String rangeHigh) {
        super(name, description, options, key);
        this.type = type;
        this.rangeLow = rangeLow;
        this.rangeHigh = rangeHigh;
    }

    public FeedbackQuestion copy() {
        FeedbackQuestion copy = new FeedbackQuestion(this.name, this.description, this.type, this.options, this.key, this.rangeLow, this.rangeHigh);
        copy.id = this.id;
        return copy;
    }

    public void setType(FeedbackQuestionType type) {
        this.type = type;
    }
}
