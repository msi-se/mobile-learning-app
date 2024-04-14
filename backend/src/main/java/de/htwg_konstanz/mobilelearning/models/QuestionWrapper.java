package de.htwg_konstanz.mobilelearning.models;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import de.htwg_konstanz.mobilelearning.helper.Analytics;
import de.htwg_konstanz.mobilelearning.helper.Hasher;

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

    public List<String> getResultsByUserId(ObjectId userId) {
        List<String> userResults = new ArrayList<String>();
        for (Result result : this.results) {
            String hashedUserId = Hasher.hash(userId.toString());
            if (result.userId != null && result.userId.equals(userId)) {
                userResults.addAll(result.values);
            }
            else if (result.hashedUserId != null && result.hashedUserId.equals(hashedUserId)) {
                userResults.addAll(result.values);
            }
        }
        return userResults;
    }

    public QuestionWrapper deepCopy() {
        QuestionWrapper copy = new QuestionWrapper();
        copy.id = this.id;
        copy.questionId = this.questionId;
        copy.results = new ArrayList<Result>();
        this.results.forEach(r -> copy.results.add(r.deepCopy()));
        copy.analytics = this.analytics.deepCopy();
        copy.questionContent = this.questionContent != null ? this.questionContent.copy() : null;
        return copy;
    }
}
