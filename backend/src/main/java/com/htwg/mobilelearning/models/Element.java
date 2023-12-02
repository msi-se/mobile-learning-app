package com.htwg.mobilelearning.models;

import org.bson.types.ObjectId;

public class Element {
    public ObjectId id;
    public String name;
    public String description;
    public String type;
    public String value;

    public Element() {
    }

    public Element(String name, String description, String type, String value) {
        this.id = new ObjectId();
        this.name = name;
        this.description = description;
        this.type = type;
        this.value = value;
    }

    public ObjectId getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String getType() {
        return this.type;
    }

    public String getValue() {
        return this.value;
    }
}
