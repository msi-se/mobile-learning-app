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
    public String action; // CHANGE_FORM_STATUS (client), ADD_RESULT (client), FORM_STATUS_CHANGED (server), RESULT_ADDED (server), PARTICIPANT_JOINED (server)
    public String formStatus; // NOT_STARTED, STARTED, FINISHED
    
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
        this.form = null;

        System.out.println("Action: " + this.action);
        System.out.println("Form status: " + this.formStatus);
        System.out.println("Result element ID: " + this.resultElementId);
        System.out.println("Result value: " + this.resultValues);
    }

    public LiveFeedbackSocketMessage(String action, String formStatus, String resultElementId, List<String> resultValues, FeedbackForm form) {
        this.action = action;
        this.formStatus = formStatus;
        this.resultElementId = resultElementId;
        this.resultValues = resultValues;
        this.form = form;
    }

    public String toJson() {
        Gson gson = new GsonBuilder().registerTypeAdapter(ObjectId.class, new ObjectIdTypeAdapter()).create();
        return gson.toJson(this);
    }

    public static LiveFeedbackSocketMessage getByJsonWithForm(String message) {
        Gson gson = new GsonBuilder().registerTypeAdapter(ObjectId.class, new ObjectIdTypeAdapter()).create();
        return gson.fromJson(message, LiveFeedbackSocketMessage.class);
    }

}
