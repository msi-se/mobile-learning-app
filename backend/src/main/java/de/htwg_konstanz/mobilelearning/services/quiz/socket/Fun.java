package de.htwg_konstanz.mobilelearning.services.quiz.socket;

import org.bson.types.ObjectId;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.htwg_konstanz.mobilelearning.helper.ObjectIdTypeAdapter;

public class Fun {

    public String action; // THROW_PAPER_PLANE
    public double percentageX;
    public double percentageY;
    
    public Fun(String message) {


        Gson gson = new GsonBuilder().registerTypeAdapter(ObjectId.class, new ObjectIdTypeAdapter()).create();
        Fun funString = gson.fromJson(message, Fun.class);
        this.action = funString.action;
        this.percentageX = funString.percentageX;
        this.percentageY = funString.percentageY;

        System.out.println("Fun Action: " + this.action);
        System.out.println("Fun percentageX: " + this.percentageX);
        System.out.println("Fun percentageY: " + this.percentageY);
    }

    public Fun(String action, double percentageX, double percentageY) {
        this.action = action;
        this.percentageX = percentageX;
        this.percentageY = percentageY;
    }

    public String toJson() {
        Gson gson = new GsonBuilder().registerTypeAdapter(ObjectId.class, new ObjectIdTypeAdapter()).create();
        return gson.toJson(this);
    }

    public Fun copy() {
        return new Fun(this.action, this.percentageX, this.percentageY);
    }

    public static Fun getByJsonWithForm(String message) {
        Gson gson = new GsonBuilder().registerTypeAdapter(ObjectId.class, new ObjectIdTypeAdapter()).create();
        return gson.fromJson(message, Fun.class);
    }

}
