package de.htwg_konstanz.mobilelearning.models;

import org.bson.types.ObjectId;

import de.htwg_konstanz.mobilelearning.enums.FormStatus;

public class FormShell extends Form {

    public String type;
    public String course;

    public FormShell() { }

    public FormShell(ObjectId id, ObjectId courseId, String name, String description, FormStatus status, Integer connectCode, String key, String type, String course) {
        this.id = id;
        this.courseId = courseId;
        this.name = name;
        this.description = description;
        this.status = status;
        this.connectCode = connectCode;
        this.key = key;
        this.type = type;
        this.course = course;
    }
}