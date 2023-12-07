package com.htwg.mobilelearning.services.feedback;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bson.BSONObject;
import org.bson.types.ObjectId;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.htwg.mobilelearning.enums.FeedbackChannelStatus;
import com.htwg.mobilelearning.helperclasses.SocketConnection;
import com.htwg.mobilelearning.helperclasses.SocketConnectionType;
import com.htwg.mobilelearning.models.feedback.FeedbackChannel;
import com.htwg.mobilelearning.models.feedback.FeedbackForm;
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
        //     "resultValue": null | string | number | boolean,
        //     "role": "STUDENT" | "PROF" | "SERVER"
        // }

        // convert message to json and extract values
        JSONObject messageObject = (JSONObject) JSONValue.parse(message);
        System.out.println("Message object: " + messageObject.toJSONString());
        String action = (String) messageObject.get("action");
        String formStatus = (String) messageObject.get("formStatus");
        String resultElementId = (String) messageObject.get("resultElementId");
        String resultValue = (String) messageObject.get("resultValue");
        String role = (String) messageObject.get("role");

        System.out.println("Action: " + action);
        System.out.println("Form status: " + formStatus);
        System.out.println("Result element ID: " + resultElementId);
        System.out.println("Result value: " + resultValue);
        System.out.println("Role: " + role);

        this.evaluateMessage(channelId, formId, userId, action, formStatus, resultElementId, resultValue, role);

    }

    private void broadcast(String message) {
        connections.values().forEach(connection -> {
            connection.session.getAsyncRemote().sendObject(message, result ->  {
                if (result.getException() != null) {
                    System.out.println("Unable to send message: " + result.getException());
                }
            });
        });
    }

    private Boolean evaluateMessage(String channelId, String formId, String userId, String action, String formStatus, String resultElementId, String resultValue, String role) {
        
        // evaluate action
        if (action == null || action.equals("")) { 
            System.out.println("Action is null");
            return false;
        }

        if (action.equals("CHANGE_FORM_STATUS")) {

            System.out.println("Change form status");

            // evaluate formStatus
            // if (formStatus == null || formStatus == "" || (formStatus != "NOT_STARTED" && formStatus != "STARTED" && formStatus != "FINISHED")) {
            //     System.out.println("Form status is invalid");
            //     return false;
            // }

            // get the enum value of the formStatus
            FeedbackChannelStatus formStatusEnum = FeedbackChannelStatus.valueOf(formStatus);
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

            // send the new form status to all receivers
            JSONObject messageObject = new JSONObject();
            messageObject.put("action", "CHANGE_FORM_STATUS");
            messageObject.put("formStatus", formStatus);
            messageObject.put("role", "SERVER");

            System.out.println("Broadcasting message: " + messageObject.toJSONString());

            this.broadcast(messageObject.toJSONString());
            return true;
        }

        return false;
    }
    
}
