package de.htwg_konstanz.mobilelearning.models;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import de.htwg_konstanz.mobilelearning.helper.Analytics;

public class QuestionWrapper {
    
    public ObjectId id;
    public ObjectId questionId;
    public List<Result> results;
    public Analytics analytics;

    public Question questionContent;

    public QuestionWrapper() {
    }

    public QuestionWrapper(ObjectId questionId, List<Result> results) {
        this.id = new ObjectId();
        this.questionId = questionId;
        this.results = results != null ? results : new ArrayList<Result>();
        this.analytics = new Analytics();
        this.questionContent = null;
    }

    public ObjectId getId() {
        return id;
    }

    public void clearResults() {
        this.results.clear();
    }

    public void addResult(Result result) {
        this.results.add(result);
    }

    public void setQuestionContent(Question question) {
        if (question == null) {
            return;
        }
        this.questionContent = question.copy();
        this.questionContent.id = this.id;
    }

    public ObjectId getQuestionId() {
        return questionId;
    }
}
