package com.htwg.mobilelearning.services.feedback;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.htwg.mobilelearning.helperclasses.SocketConnection;
import com.htwg.mobilelearning.helperclasses.SocketConnectionType;

import jakarta.enterprise.context.ApplicationScoped;
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
    public void onMessage(String message, @PathParam("channelId") String channelId) {
        broadcast(message);
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
    
}
