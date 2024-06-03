package de.htwg_konstanz.mobilelearning.models;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

/**
 * Result of feedback/quiz form.
 * User id is hashed for anonymity.
 */
public class Result {
    public ObjectId id;
    public ObjectId userId;
    public List<String> values;
    public String hashedUserId;
    public Integer gainedPoints;

    public Result() {
    }

    public Result(ObjectId userId, List<String> values) {
        this.id = new ObjectId();
        this.userId = userId;
        this.values = values;
        this.hashedUserId = null;
        this.gainedPoints = 0;
    }

    public Result(String hashedUserId, List<String> values) {
        this.id = new ObjectId();
        this.hashedUserId = hashedUserId;
        this.values = values;
        this.userId = null;
        this.gainedPoints = 0;
    }

    public ObjectId getId() {
        return this.id;
    }

    public ObjectId getUserId() {
        return this.userId;
    }

    public String getHashedUserId() {
        return this.hashedUserId;
    }

    public List<String> getValues() {
        return this.values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public void setGainedPoints(Integer gainedPoints) {
        this.gainedPoints = gainedPoints;
    }

    public Integer getGainedPoints() {
        return this.gainedPoints;
    }

    public Result deepCopy() {
        Result copy = new Result();
        copy.id = this.id;
        copy.userId = this.userId;
        copy.values = new ArrayList<String>(this.values);
        copy.hashedUserId = this.hashedUserId;
        copy.gainedPoints = this.gainedPoints;
        return copy;
    }
}
