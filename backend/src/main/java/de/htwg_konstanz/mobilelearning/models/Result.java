package de.htwg_konstanz.mobilelearning.models;

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

    public Result() {
    }

    public Result(ObjectId userId, List<String> values) {
        this.id = new ObjectId();
        this.userId = userId;
        this.values = values;
        this.hashedUserId = null;
    }

    public Result(String hashedUserId, List<String> values) {
        this.id = new ObjectId();
        this.hashedUserId = hashedUserId;
        this.values = values;
        this.userId = null;
    }

    public ObjectId getId() {
        return this.id;
    }

    public ObjectId getUserId() {
        return this.userId;
    }

    public List<String> getValues() {
        return this.values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }
}
