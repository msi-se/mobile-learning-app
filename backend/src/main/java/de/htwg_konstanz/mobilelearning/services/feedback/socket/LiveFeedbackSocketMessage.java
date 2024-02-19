package de.htwg_konstanz.mobilelearning.services.feedback.socket;

import java.util.List;

import org.bson.types.ObjectId;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.htwg_konstanz.mobilelearning.helper.ObjectIdTypeAdapter;
import de.htwg_konstanz.mobilelearning.models.feedback.FeedbackForm;

/**
 * Strucure of a LiveFeedbackSocketMessage
 */
public class LiveFeedbackSocketMessage {

    // general
    public String action; // CHANGE_FORM_STATUS, ADD_RESULT, FORM_STATUS_CHANGED, RESULT_ADDED
    public String formStatus; // NOT_STARTED, STARTED, FINISHED
    public List<String> roles; // STUDENT, PROF, SERVER // not really used yet
    
    // incoming message
    public String resultElementId;
    public List<String> resultValues;

    // outgoing message
    public FeedbackForm form;

    public LiveFeedbackSocketMessage(String message) {
        Gson gson = new GsonBuilder().create();
        LiveFeedbackSocketMessage feedbackSocketMessage = gson.fromJson(message, LiveFeedbackSocketMessage.class);
        this.action = feedbackSocketMessage.action;
        this.formStatus = feedbackSocketMessage.formStatus;
        this.resultElementId = feedbackSocketMessage.resultElementId;
        this.resultValues = feedbackSocketMessage.resultValues;
        this.roles = feedbackSocketMessage.roles;
        this.form = null;

        System.out.println("Action: " + this.action);
        System.out.println("Form status: " + this.formStatus);
        System.out.println("Result element ID: " + this.resultElementId);
        System.out.println("Result value: " + this.resultValues);
        System.out.println("Roles: " + this.roles);
    }

    public LiveFeedbackSocketMessage(String action, String formStatus, String resultElementId, List<String> resultValues, List<String> roles, FeedbackForm form) {
        this.action = action;
        this.formStatus = formStatus;
        this.resultElementId = resultElementId;
        this.resultValues = resultValues;
        this.roles = roles;
        this.form = form;
    }

    public String toJson() {
        Gson gson = new GsonBuilder().registerTypeAdapter(ObjectId.class, new ObjectIdTypeAdapter()).create();
        return gson.toJson(this);
    }
}
