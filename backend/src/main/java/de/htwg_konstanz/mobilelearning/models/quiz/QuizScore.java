package de.htwg_konstanz.mobilelearning.models.quiz;

import org.bson.types.ObjectId;

public class QuizScore {

    public ObjectId userId;
    public String userAlias;
    public Integer score;

    public QuizScore() {
    }

    public QuizScore(ObjectId userId, String userAlias, Integer score) {
        this.userId = userId;
        this.userAlias = userAlias;
        this.score = score;
    }

    public String getUseralias() {
        return this.userAlias;
    }

    public ObjectId getUserId() {
        return this.userId;
    }

    public Integer getScore() {
        return this.score;
    }

    public Integer increaseScore(Integer by) {
        this.score += by;
        return this.score;
    }

}
