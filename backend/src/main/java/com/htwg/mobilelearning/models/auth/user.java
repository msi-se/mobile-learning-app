package com.htwg.mobilelearning.models.auth;

public class user {
    public String username;
    public String hashedPassword;

    public user() {
    }

    public user(String username, String hashedPassword) {
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
