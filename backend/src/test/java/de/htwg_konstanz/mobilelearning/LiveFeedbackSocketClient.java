package de.htwg_konstanz.mobilelearning;

import java.util.ArrayList;
import java.util.List;

import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;

@ClientEndpoint
public class LiveFeedbackSocketClient {
    
    private List<String> messageQueue;
    private Session session;

    public LiveFeedbackSocketClient() {
        this.messageQueue = new ArrayList<String>();
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        this.session = session;
        System.out.println("Connected to server");
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("Disconnected from server");
    }

    @OnMessage
    public void onMessage(String message) {
        messageQueue.add(message);
    }

    public void sendMessage(String message) {
        session.getAsyncRemote().sendText(message);
    }

    public List<String> getMessageQueue() {
        return messageQueue;
    }
}
