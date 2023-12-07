package com.htwg.mobilelearning.helperclasses;

import org.bson.types.ObjectId;

import jakarta.websocket.Session;

public class SocketConnection {
    public ObjectId id;
    public Session session;
    public ObjectId channelId;
    public ObjectId formId;
    public ObjectId userId;
    public SocketConnectionType type;

    public SocketConnection(Session session, ObjectId channelId, ObjectId formId, ObjectId userId, SocketConnectionType type) {
        this.id = new ObjectId();
        this.session = session;
        this.channelId = channelId;
        this.userId = userId;
        this.type = type;
    }
    
    public SocketConnection(Session session, String channelId, String formId, String userId, SocketConnectionType type) {
        this.id = new ObjectId();
        this.session = session;
        this.channelId = new ObjectId(channelId);
        this.userId = new ObjectId(userId);
        this.type = type;
    }

    public ObjectId getId() {
        return id;
    }

    public Session getSession() {
        return session;
    }

    public ObjectId getChannelId() {
        return channelId;
    }

    public ObjectId getUserId() {
        return userId;
    }

    public SocketConnectionType getType() {
        return type;
    }

}
