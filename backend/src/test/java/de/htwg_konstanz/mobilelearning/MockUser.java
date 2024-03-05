package de.htwg_konstanz.mobilelearning;

public class MockUser {
    String id;
    String jwt;

    public MockUser(String id, String jwt) {
        this.id = id;
        this.jwt = jwt;
    }

    public String getId() {
        return id;
    }

    public String getJwt() {
        return jwt;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }
}
