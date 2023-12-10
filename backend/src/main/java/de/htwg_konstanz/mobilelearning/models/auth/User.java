package de.htwg_konstanz.mobilelearning.models.auth;

import org.bson.types.ObjectId;

public class User {
    public ObjectId id;
    public String username;
    public String password;

    public User() {
    }

    public User(String username, String password) {
        this.id = new ObjectId();
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean authenticate(String password) {
        return this.password.equals(password);
    }


}
