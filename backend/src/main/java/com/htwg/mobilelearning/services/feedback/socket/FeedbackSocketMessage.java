package com.htwg.mobilelearning.services.feedback.socket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class FeedbackSocketMessage {

    public String action; // CHANGE_FORM_STATUS, CHANGE_RESULT_VALUE
    public String formStatus; // NOT_STARTED, STARTED, FINISHED
    public String resultElementId;
    public String resultValue;
    public String role; // STUDENT, PROF, SERVER

    public FeedbackSocketMessage(String message) {
        Gson gson = new GsonBuilder().create();
        FeedbackSocketMessage feedbackSocketMessage = gson.fromJson(message, FeedbackSocketMessage.class);
        this.action = feedbackSocketMessage.action;
        this.formStatus = feedbackSocketMessage.formStatus;
        this.resultElementId = feedbackSocketMessage.resultElementId;
        this.resultValue = feedbackSocketMessage.resultValue;
        this.role = feedbackSocketMessage.role;

        System.out.println("Action: " + this.action);
        System.out.println("Form status: " + this.formStatus);
        System.out.println("Result element ID: " + this.resultElementId);
        System.out.println("Result value: " + this.resultValue);
        System.out.println("Role: " + this.role);

    }
}
