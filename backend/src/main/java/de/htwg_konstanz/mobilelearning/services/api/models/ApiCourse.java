package de.htwg_konstanz.mobilelearning.services.api.models;

public class ApiCourse {
    public String name;
    public String description;
    
    public ApiCourse() {
    }

    public ApiCourse(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
