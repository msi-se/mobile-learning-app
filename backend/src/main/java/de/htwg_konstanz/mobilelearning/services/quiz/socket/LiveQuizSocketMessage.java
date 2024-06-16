package de.htwg_konstanz.mobilelearning.services.quiz.socket;

import java.util.List;

import org.bson.types.ObjectId;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.htwg_konstanz.mobilelearning.helper.ObjectIdTypeAdapter;
import de.htwg_konstanz.mobilelearning.models.quiz.QuizForm;

/**
 * Strucure of a LiveFeedbackSocketMessage
 */
public class LiveQuizSocketMessage {

    // general
    public String action; // CHANGE_FORM_STATUS (client), ADD_RESULT (client), FORM_STATUS_CHANGED (server), RESULT_ADDED (server), NEXT (client), CLOSED_QUESTION (server), OPENED_NEXT_QUESTION (server), PARTICIPANT_JOINED (server)
    public String formStatus; // NOT_STARTED, STARTED, FINISHED
    
    // incoming message
    public String resultElementId;
    public List<String> resultValues;

    // outgoing message
    public QuizForm form;

    // fun stuff (throw paper plane)
    public Fun fun;

    public Boolean userHasAnsweredCorrectly;
    public List<String> correctAnswers;
    public List<String> userAnswers;
    public Integer gainedPoints;

    public LiveQuizSocketMessage(String message) {


        Gson gson = new GsonBuilder().registerTypeAdapter(ObjectId.class, new ObjectIdTypeAdapter()).create();
        LiveQuizSocketMessage quizSocketMessage = gson.fromJson(message, LiveQuizSocketMessage.class);
        this.action = quizSocketMessage.action;
        this.formStatus = quizSocketMessage.formStatus;
        this.resultElementId = quizSocketMessage.resultElementId;
        this.resultValues = quizSocketMessage.resultValues;
        this.fun = quizSocketMessage.fun;
        this.form = null;

        // System.out.println("Action: " + this.action);
        // System.out.println("Form status: " + this.formStatus);
        // System.out.println("Result element ID: " + this.resultElementId);
        // System.out.println("Result value: " + this.resultValues);
    }

    public LiveQuizSocketMessage(String action, Fun fun) {
        this.action = action;
        this.formStatus = null;
        this.resultElementId = null;
        this.resultValues = null;
        this.form = null;
        this.userHasAnsweredCorrectly = null;
        this.correctAnswers = null;
        this.fun = fun;
        this.userAnswers = null;
    }

    public LiveQuizSocketMessage(String action, String formStatus, String resultElementId, List<String> resultValues, QuizForm form) {
        this.action = action;
        this.formStatus = formStatus;
        this.resultElementId = resultElementId;
        this.resultValues = resultValues;
        this.form = form;
        this.userHasAnsweredCorrectly = null;
        this.correctAnswers = null;
        this.fun = null;
        this.userAnswers = null;
    }

    public LiveQuizSocketMessage(String action, String formStatus, String resultElementId, List<String> resultValues, QuizForm form, List<String> userAnswers) {
        this.action = action;
        this.formStatus = formStatus;
        this.resultElementId = resultElementId;
        this.resultValues = resultValues;
        this.form = form;
        this.userHasAnsweredCorrectly = null;
        this.correctAnswers = null;
        this.fun = null;
        this.userAnswers = userAnswers;
    }

    public LiveQuizSocketMessage(String action, String formStatus, String resultElementId, List<String> resultValues, QuizForm form, Boolean userHasAnsweredCorrectly) {
        this.action = action;
        this.formStatus = formStatus;
        this.resultElementId = resultElementId;
        this.resultValues = resultValues;
        this.form = form;
        this.userHasAnsweredCorrectly = userHasAnsweredCorrectly;
        this.correctAnswers = null;
        this.fun = null;
        this.userAnswers = null;
    }

    public LiveQuizSocketMessage(String action, String formStatus, String resultElementId, List<String> resultValues, QuizForm form, Boolean userHasAnsweredCorrectly, List<String> correctAnswers) {
        this.action = action;
        this.formStatus = formStatus;
        this.resultElementId = resultElementId;
        this.resultValues = resultValues;
        this.form = form;
        this.userHasAnsweredCorrectly = userHasAnsweredCorrectly;
        this.correctAnswers = correctAnswers;
        this.fun = null;
        this.userAnswers = null;
    }

    public LiveQuizSocketMessage(String action, String formStatus, String resultElementId, List<String> resultValues, QuizForm form, Boolean userHasAnsweredCorrectly, List<String> correctAnswers, Fun fun) {
        this.action = action;
        this.formStatus = formStatus;
        this.resultElementId = resultElementId;
        this.resultValues = resultValues;
        this.form = form;
        this.userHasAnsweredCorrectly = userHasAnsweredCorrectly;
        this.correctAnswers = correctAnswers;
        this.fun = fun;
        this.userAnswers = null;
    }

    public String toJson() {
        Gson gson = new GsonBuilder().registerTypeAdapter(ObjectId.class, new ObjectIdTypeAdapter()).create();
        return gson.toJson(this);
    }

    public LiveQuizSocketMessage copy() {
        return new LiveQuizSocketMessage(this.action, this.formStatus, this.resultElementId, this.resultValues, this.form, this.userHasAnsweredCorrectly, this.correctAnswers, this.fun);
    }

    public static LiveQuizSocketMessage getByJsonWithForm(String message) {
        Gson gson = new GsonBuilder().registerTypeAdapter(ObjectId.class, new ObjectIdTypeAdapter()).create();
        return gson.fromJson(message, LiveQuizSocketMessage.class);
    }
}
