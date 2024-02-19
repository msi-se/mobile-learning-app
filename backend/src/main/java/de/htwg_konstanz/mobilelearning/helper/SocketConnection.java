package de.htwg_konstanz.mobilelearning.helper;

import org.bson.types.ObjectId;

import jakarta.websocket.Session;

/**
 * Socket connection that is added to current connections of a socket.
 */
public class SocketConnection {
    public ObjectId id;
    public Session session;
    public ObjectId courseId;
    public ObjectId formId;
    public ObjectId userId;
    public SocketConnectionType type;

    public SocketConnection(Session session, ObjectId courseId, ObjectId formId, ObjectId userId, SocketConnectionType type) {
        this.id = new ObjectId();
        this.session = session;
        this.courseId = courseId;
        this.formId = formId;
        this.userId = userId;
        this.type = type;
    }
    
    public SocketConnection(Session session, String courseId, String formId, String userId, SocketConnectionType type) {
        this.id = new ObjectId();
        this.session = session;
        this.courseId = new ObjectId(courseId);
        this.formId = new ObjectId(formId);
        this.userId = new ObjectId(userId);
        this.type = type;
    }

    public ObjectId getId() {
        return this.id;
    }

    public Session getSession() {
        return this.session;
    }

    public ObjectId getCourseId() {
        return this.courseId;
    }

    public ObjectId getFormId() {
        return this.formId;
    }

    public ObjectId getUserId() {
        return this.userId;
    }

    public SocketConnectionType getType() {
        return this.type;
    }
}
