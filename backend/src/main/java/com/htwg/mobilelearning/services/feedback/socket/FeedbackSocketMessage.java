package com.htwg.mobilelearning.services.feedback.socket;

import org.bson.types.ObjectId;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.htwg.mobilelearning.helper.ObjectIdTypeAdapter;
import com.htwg.mobilelearning.models.feedback.FeedbackForm;

public class FeedbackSocketMessage {

    // general
    public String action; // CHANGE_FORM_STATUS, ADD_RESULT, FORM_STATUS_CHANGED, RESULT_ADDED
    public String formStatus; // NOT_STARTED, STARTED, FINISHED
    public String role; // STUDENT, PROF, SERVER // not really used yet
    
    // incoming message
    public String resultElementId;
    public String resultValue;

    // outgoing message
    public FeedbackForm form;

    public FeedbackSocketMessage(String message) {
        Gson gson = new GsonBuilder().create();
        FeedbackSocketMessage feedbackSocketMessage = gson.fromJson(message, FeedbackSocketMessage.class);
        this.action = feedbackSocketMessage.action;
        this.formStatus = feedbackSocketMessage.formStatus;
        this.resultElementId = feedbackSocketMessage.resultElementId;
        this.resultValue = feedbackSocketMessage.resultValue;
        this.role = feedbackSocketMessage.role;
        this.form = null;

        System.out.println("Action: " + this.action);
        System.out.println("Form status: " + this.formStatus);
        System.out.println("Result element ID: " + this.resultElementId);
        System.out.println("Result value: " + this.resultValue);
        System.out.println("Role: " + this.role);
    }

    public FeedbackSocketMessage(String action, String formStatus, String resultElementId, String resultValue, String role, FeedbackForm form) {
        this.action = action;
        this.formStatus = formStatus;
        this.resultElementId = resultElementId;
        this.resultValue = resultValue;
        this.role = role;
        this.form = form;
    }

    public String toJson() {
        Gson gson = new GsonBuilder().registerTypeAdapter(ObjectId.class, new ObjectIdTypeAdapter()).create();
        return gson.toJson(this);
    }
}
