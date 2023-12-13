package de.htwg_konstanz.mobilelearning.models.feedback;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import de.htwg_konstanz.mobilelearning.enums.QuestionType;
import de.htwg_konstanz.mobilelearning.models.Question;

public class FeedbackQuestion extends Question {
    
    public FeedbackQuestion() {
    }

    public FeedbackQuestion(String name, String description, QuestionType type, List<String> options) {
        this.id = new ObjectId();
        this.name = name;
        this.description = description;
        this.type = type;
        this.options = options != null ? options : new ArrayList<String>();
    }

}
