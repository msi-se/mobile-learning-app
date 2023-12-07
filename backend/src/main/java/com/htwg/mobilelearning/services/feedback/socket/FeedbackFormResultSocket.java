package com.htwg.mobilelearning.services.feedback.socket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bson.types.ObjectId;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.htwg.mobilelearning.enums.FeedbackChannelStatus;
import com.htwg.mobilelearning.helper.ObjectIdTypeAdapter;
import com.htwg.mobilelearning.helper.SocketConnection;
import com.htwg.mobilelearning.helper.SocketConnectionType;
import com.htwg.mobilelearning.models.feedback.FeedbackChannel;
import com.htwg.mobilelearning.models.feedback.FeedbackElement;
import com.htwg.mobilelearning.models.feedback.FeedbackForm;
import com.htwg.mobilelearning.models.feedback.FeedbackResult;
import com.htwg.mobilelearning.repositories.FeedbackChannelRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.websocket.Session;


@ServerEndpoint("/feedback/channel/{channelId}/form/{formId}/subscribe/{userId}")
@ApplicationScoped
public class FeedbackFormResultSocket {
    Map<String, SocketConnection> connections = new ConcurrentHashMap<>();

    @Inject
    private FeedbackChannelRepository feedbackChannelRepository;

    @OnOpen
    public void onOpen(Session session, @PathParam("channelId") String channelId, @PathParam("formId") String formId, @PathParam("userId") String userId) {
        System.out.println("New connection with session ID: " + session.getId());
        System.out.println("Channel ID: " + channelId);
        System.out.println("Form ID: " + formId);
        System.out.println("User ID: " + userId);
        SocketConnection socketMember = new SocketConnection(session, channelId, formId, userId, SocketConnectionType.RECEIVER);
        connections.put(session.getId(), socketMember);
    }

    @OnClose
    public void onClose(Session session, @PathParam("channelId") String channelId, @PathParam("formId") String formId, @PathParam("userId") String userId) {
        connections.remove(session.getId());
    }

    @OnError
    public void onError(Session session, @PathParam("channelId") String channelId, @PathParam("formId") String formId, @PathParam("userId") String userId, Throwable throwable) {
        System.out.println("Error: " + throwable.getMessage());
        connections.remove(session.getId());
    }

    @OnMessage
    public void onMessage(String message, @PathParam("channelId") String channelId, @PathParam("formId") String formId, @PathParam("userId") String userId) {

        // type feedbackSocketMessage = {
        //     "action": "CHANGE_FORM_STATUS" | "ADD_RESULT"
        //     "formStatus": null | "NOT_STARTED" | "STARTED" | "FINISHED"
        //     "resultElementId": null | string,
        //     "resultValue": null | string | boolean,
        //     "role": "STUDENT" | "PROF" | "SERVER"
        // }

        FeedbackSocketMessage feedbackSocketMessage = new FeedbackSocketMessage(message);
        this.evaluateMessage(feedbackSocketMessage, channelId, formId, userId);

    }

    private void broadcast(String message, String channelId, String formId) {
        connections.values().forEach(connection -> {

            // check if the channel ID and form ID match
            if (!connection.getChannelId().equals(new ObjectId(channelId))) {
                return;
            }
            if (!connection.getFormId().equals(new ObjectId(formId))) {
                return;
            }

            connection.session.getAsyncRemote().sendObject(message, result ->  {
                if (result.getException() != null) {
                    System.out.println("Unable to send message: " + result.getException());
                }
            });
        });
    }

    private Boolean evaluateMessage(FeedbackSocketMessage feedbackSocketMessage, String channelId, String formId, String userId) {
        
        // evaluate action
        if (feedbackSocketMessage.action == null || feedbackSocketMessage.action.equals("")) { 
            System.out.println("Action is null");
            return false;
        }

        if (feedbackSocketMessage.action.equals("CHANGE_FORM_STATUS")) {

            System.out.println("Change form status");

            // evaluate formStatus
            if (feedbackSocketMessage.formStatus == null || feedbackSocketMessage.formStatus.equals("") || FeedbackChannelStatus.valueOf(feedbackSocketMessage.formStatus) == null) {
                System.out.println("Form status is invalid");
                return false;
            }

            // get the enum value of the formStatus
            FeedbackChannelStatus formStatusEnum = FeedbackChannelStatus.valueOf(feedbackSocketMessage.formStatus);
            System.out.println("Form status enum: " + formStatusEnum);

            // get the form
            FeedbackChannel channel = feedbackChannelRepository.findById(new ObjectId(channelId));
            if (channel == null) {
                System.out.println("Channel not found");
                return false;
            }
            FeedbackForm form = channel.getFeedbackFormById(new ObjectId(formId));
            if (form == null) {
                System.out.println("Form not found");
                return false;
            }

            // change the form status
            form.setStatus(formStatusEnum);

            // update the form in the database
            feedbackChannelRepository.update(channel);

            // send the updated form to all receivers (stringify the form)
            Gson gson = new GsonBuilder().registerTypeAdapter(ObjectId.class, new ObjectIdTypeAdapter()).create();
            String formString = gson.toJson(form);
            this.broadcast(formString, channelId, formId);
            return true;
        }

        if (feedbackSocketMessage.action.equals("ADD_RESULT")) {

            System.out.println("Add result");

            // evaluate resultElementId
            if (feedbackSocketMessage.resultElementId == null || feedbackSocketMessage.resultElementId.equals("")) {
                System.out.println("Result element ID is invalid");
                return false;
            }

            // evaluate resultValue
            if (feedbackSocketMessage.resultValue == null || feedbackSocketMessage.resultValue.equals("")) {
                System.out.println("Result value is invalid");
                return false;
            }

            // get the form
            FeedbackChannel channel = feedbackChannelRepository.findById(new ObjectId(channelId));
            if (channel == null) {
                System.out.println("Channel not found");
                return false;
            }
            FeedbackForm form = channel.getFeedbackFormById(new ObjectId(formId));
            if (form == null) {
                System.out.println("Form not found");
                return false;
            }

            // get the element
            FeedbackElement element = form.getElementById(new ObjectId(feedbackSocketMessage.resultElementId));
            if (element == null) {
                System.out.println("Element not found");
                return false;
            }

            // add the result
            FeedbackResult result = new FeedbackResult(new ObjectId(userId), feedbackSocketMessage.resultValue);
            element.addResult(result);

            // update the form in the database
            feedbackChannelRepository.update(channel);

            // send the updated form to all receivers (stringify the form)
            Gson gson = new GsonBuilder().registerTypeAdapter(ObjectId.class, new ObjectIdTypeAdapter()).create();
            String formString = gson.toJson(form);
            this.broadcast(formString, channelId, formId);
            return true;
        }

        return false;
    }
    
}
