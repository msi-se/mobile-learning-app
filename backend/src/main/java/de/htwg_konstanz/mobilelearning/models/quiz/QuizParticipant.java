package de.htwg_konstanz.mobilelearning.models.quiz;

import org.bson.types.ObjectId;

public class QuizParticipant {

    public ObjectId userId;
    public String userAlias;
    public Integer score;

    public QuizParticipant() {
    }

    public QuizParticipant(ObjectId userId, String userAlias) {
        this.userId = userId;
        this.userAlias = userAlias;
        this.score = 0;
    }

    public String getUserAlias() {
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

    public QuizParticipant deepCopy() {
        QuizParticipant copy = new QuizParticipant();
        copy.userId = this.userId;
        copy.userAlias = this.userAlias;
        copy.score = this.score;
        return copy;
    }

}
