package de.htwg_konstanz.mobilelearning.services.quiz.socket;

import java.util.List;

import org.bson.types.ObjectId;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.htwg_konstanz.mobilelearning.helper.ObjectIdTypeAdapter;
import de.htwg_konstanz.mobilelearning.models.quiz.QuizForm;

public class LiveQuizSocketMessage {

    // general
    public String action; // CHANGE_FORM_STATUS (client), ADD_RESULT (client), FORM_STATUS_CHANGED (server), RESULT_ADDED (server), NEXT (client), CLOSED_QUESTION (server), OPENED_NEXT_QUESTION (server)
    public String formStatus; // NOT_STARTED, STARTED, FINISHED
    public List<String> roles; // STUDENT, PROF, SERVER // not really used yet
    
    // incoming message
    public String resultElementId;
    public List<String> resultValues;

    // outgoing message
    public QuizForm form;

    public LiveQuizSocketMessage(String message) {
        Gson gson = new GsonBuilder().create();
        LiveQuizSocketMessage quizSocketMessage = gson.fromJson(message, LiveQuizSocketMessage.class);
        this.action = quizSocketMessage.action;
        this.formStatus = quizSocketMessage.formStatus;
        this.resultElementId = quizSocketMessage.resultElementId;
        this.resultValues = quizSocketMessage.resultValues;
        this.roles = quizSocketMessage.roles;
        this.form = null;

        System.out.println("Action: " + this.action);
        System.out.println("Form status: " + this.formStatus);
        System.out.println("Result element ID: " + this.resultElementId);
        System.out.println("Result value: " + this.resultValues);
        System.out.println("Roles: " + this.roles);
    }

    public LiveQuizSocketMessage(String action, String formStatus, String resultElementId, List<String> resultValues, List<String> roles, QuizForm form) {
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
