package de.htwg_konstanz.mobilelearning.models.auth;

import org.bson.types.ObjectId;

public class User {
    public ObjectId id;
    public String username;
    public String hashedPassword;

    public User() {
    }

    public User(String username, String hashedPassword) {
        this.id = new ObjectId();
        this.username = username;
        this.hashedPassword = hashedPassword;
    }

    public String getUsername() {
        return this.username;
    }

    public String getHashedPassword() {
        return this.hashedPassword;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public boolean authenticate(String password) {
        return this.hashedPassword.equals(password);
    }
}
