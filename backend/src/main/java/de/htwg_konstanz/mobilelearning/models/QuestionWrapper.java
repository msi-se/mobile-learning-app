package de.htwg_konstanz.mobilelearning.models;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import de.htwg_konstanz.mobilelearning.helper.Analytics;

/**
 * Saves the results of a questions and references question with id.
 */
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

    public Boolean addResult(Result result) {

        if (result == null) {
            return false;
        }

        // check if the user already submitted a result
        for (Result r : this.results) {
            if (r.hashedUserId != null && r.hashedUserId.equals(result.hashedUserId)) {
                return false;
            }
            if (r.userId != null && r.userId.equals(result.userId)) {
                return false;
            }
        }

        this.results.add(result);

        // update analytics
        List<String> values = new ArrayList<String>();
        this.results.forEach(r -> { if (r.values != null) { values.addAll(r.values); } });
        this.analytics.update(values);

        return true;
    }

    public Analytics getAnalytics() {
        return analytics;
    }

    public void setQuestionContent(Question question) {
        if (question == null) {
            this.questionContent = null;
            return;
        }
        this.questionContent = question.copy();
        this.questionContent.id = this.id;
    }

    public ObjectId getQuestionId() {
        return questionId;
    }

    public List<Result> getResults() {
        return results;
    }
}
