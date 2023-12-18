package de.htwg_konstanz.mobilelearning.models;

import org.bson.types.ObjectId;

public class Result {
    public ObjectId id;
    public ObjectId userId;
    public String value;
    public String hashedUserId;

    public Result() {
    }

    public Result(ObjectId userId, String value) {
        this.id = new ObjectId();
        this.userId = userId;
        this.value = value;
        this.hashedUserId = null;
    }

    public Result(String hashedUserId, String value) {
        this.id = new ObjectId();
        this.hashedUserId = hashedUserId;
        this.value = value;
        this.userId = null;
    }

    public ObjectId getId() {
        return this.id;
    }

    public ObjectId getUserId() {
        return this.userId;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
