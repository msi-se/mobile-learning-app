package com.htwg.mobilelearning.helper;

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
        this.formId = formId;
        this.userId = userId;
        this.type = type;
    }
    
    public SocketConnection(Session session, String channelId, String formId, String userId, SocketConnectionType type) {
        this.id = new ObjectId();
        this.session = session;
        this.channelId = new ObjectId(channelId);
        this.formId = new ObjectId(formId);
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

    public ObjectId getFormId() {
        return formId;
    }

    public ObjectId getUserId() {
        return userId;
    }

    public SocketConnectionType getType() {
        return type;
    }

}
