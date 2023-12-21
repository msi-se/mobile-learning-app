package de.htwg_konstanz.mobilelearning.services.feedback.socket;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.htwg_konstanz.mobilelearning.services.auth.JwtService;
import io.smallrye.jwt.auth.principal.DefaultJWTCallerPrincipal;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import org.bson.types.ObjectId;

import de.htwg_konstanz.mobilelearning.enums.FormStatus;
import de.htwg_konstanz.mobilelearning.helper.Hasher;
import de.htwg_konstanz.mobilelearning.helper.SocketConnection;
import de.htwg_konstanz.mobilelearning.helper.SocketConnectionType;
import de.htwg_konstanz.mobilelearning.models.Course;
import de.htwg_konstanz.mobilelearning.models.QuestionWrapper;
import de.htwg_konstanz.mobilelearning.models.Result;
import de.htwg_konstanz.mobilelearning.models.auth.UserRole;
import de.htwg_konstanz.mobilelearning.models.feedback.FeedbackForm;
import de.htwg_konstanz.mobilelearning.repositories.CourseRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.websocket.Session;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;


@ServerEndpoint("/course/{courseId}/feedback/form/{formId}/subscribe/{userId}/{jwt}")
@ApplicationScoped
public class LiveFeedbackSocket {
    Map<String, SocketConnection> connections = new ConcurrentHashMap<>();

    @Inject
    private CourseRepository feedbackChannelRepository;
    @Inject
    JwtService jwtService;

    @OnOpen
    public void onOpen(Session session, @PathParam("courseId") String courseId, @PathParam("formId") String formId, @PathParam("userId") String userId, @PathParam("jwt") String jwt) throws Exception {
        //UserId from Jwt has to match userId from path
        if(jwtService.getJwtClaims(jwt).getSubject().equals(userId)){
        System.out.println("New connection with session ID: " + session.getId());
        System.out.println("Channel ID: " + courseId);
        System.out.println("Form ID: " + formId);
        System.out.println("User ID: " + userId);
        SocketConnection socketMember = new SocketConnection(session, courseId, formId, userId, SocketConnectionType.RECEIVER);
        connections.put(session.getId(), socketMember);
        }else{
            connections.remove(session.getId());
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("courseId") String courseId, @PathParam("formId") String formId, @PathParam("userId") String userId) {
        connections.remove(session.getId());
    }

    @OnError
    public void onError(Session session, @PathParam("courseId") String courseId, @PathParam("formId") String formId, @PathParam("userId") String userId, Throwable throwable) {
        System.out.println("Error: " + throwable.getMessage());
        connections.remove(session.getId());
    }

    @OnMessage
    public void onMessage(String message, @PathParam("courseId") String courseId, @PathParam("formId") String formId, @PathParam("userId") String userId) {
        LiveFeedbackSocketMessage feedbackSocketMessage = new LiveFeedbackSocketMessage(message);
        this.evaluateMessage(feedbackSocketMessage, courseId, formId, userId);
    }

    private void broadcast(String message, String courseId, String formId) {
        connections.values().forEach(connection -> {

            // check if the course ID and form ID match
            if (!connection.getCourseId().equals(new ObjectId(courseId))) {
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

    private Boolean evaluateMessage(LiveFeedbackSocketMessage feedbackSocketMessage, String courseId, String formId, String userId) {
        
        // evaluate action
        if (feedbackSocketMessage.action == null || feedbackSocketMessage.action.equals("")) { 
            System.out.println("Action is null");
            return false;
        }

        if (feedbackSocketMessage.action.equals("CHANGE_FORM_STATUS")) {
            return this.changeFormStatus(feedbackSocketMessage, courseId, formId, userId);
        }

        if (feedbackSocketMessage.action.equals("ADD_RESULT")) {
            return this.addResult(feedbackSocketMessage, courseId, formId, userId);
        }

        return false;
    };

    private Boolean changeFormStatus(LiveFeedbackSocketMessage feedbackSocketMessage, String courseId, String formId, String userId) {

        if(!feedbackSocketMessage.roles.contains(UserRole.PROF)){
            System.out.println("You need the role Prof to change the form status");
            return false;
        }

        System.out.println("Change form status");

        // evaluate formStatus
        if (feedbackSocketMessage.formStatus == null || feedbackSocketMessage.formStatus.equals("") || FormStatus.valueOf(feedbackSocketMessage.formStatus) == null) {
            System.out.println("Form status is invalid");
            return false;
        }

        // get the enum value of the formStatus
        FormStatus formStatusEnum = FormStatus.valueOf(feedbackSocketMessage.formStatus);
        System.out.println("Form status enum: " + formStatusEnum);

        // get the form
        Course course = feedbackChannelRepository.findById(new ObjectId(courseId));
        if (course == null) {
            System.out.println("Channel not found");
            return false;
        }
        FeedbackForm form = course.getFeedbackFormById(new ObjectId(formId));
        if (form == null) {
            System.out.println("Form not found");
            return false;
        }

        // change the form status
        form.setStatus(formStatusEnum);
        form.fillQuestionContents(course);

        // if it is set to NOT_STARTED, remove all results
        if (formStatusEnum == FormStatus.NOT_STARTED) {
            form.clearResults();
            // send the event to all receivers
            LiveFeedbackSocketMessage outgoingMessage = new LiveFeedbackSocketMessage("RESULT_ADDED", form.status.toString(), null, null, null, form);
            this.broadcast(outgoingMessage.toJson(), courseId, formId);
        }
        
        // send the updated form to all receivers (stringify the form)
        LiveFeedbackSocketMessage outgoingMessage = new LiveFeedbackSocketMessage("FORM_STATUS_CHANGED", form.status.toString(), null, null, null, form);
        this.broadcast(outgoingMessage.toJson(), courseId, formId);

        // update the form in the database
        form.clearQuestionContents();
        feedbackChannelRepository.update(course);

        return true;
    };

    private Boolean addResult(LiveFeedbackSocketMessage feedbackSocketMessage, String courseId, String formId, String userId) {

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
        Course course = feedbackChannelRepository.findById(new ObjectId(courseId));
        if (course == null) {
            System.out.println("Channel not found");
            return false;
        }
        FeedbackForm form = course.getFeedbackFormById(new ObjectId(formId));
        if (form == null) {
            System.out.println("Form not found");
            return false;
        }
        form.fillQuestionContents(course);

        // get the element
        QuestionWrapper element = form.getQuestionById(new ObjectId(feedbackSocketMessage.resultElementId));
        if (element == null) {
            System.out.println("Element not found");
            return false;
        }

        // add the result
        String hashedUserId = Hasher.hash(userId);
        Result result = new Result(hashedUserId, feedbackSocketMessage.resultValue);
        Boolean wasResultAdded = element.addResult(result);
        if (!wasResultAdded) {
            System.out.println("Result was not added (user probably already submitted a result)");
            return false;
        }

        // send the updated form to all receivers (stringify the form)
        LiveFeedbackSocketMessage outgoingMessage = new LiveFeedbackSocketMessage("RESULT_ADDED", null, feedbackSocketMessage.resultElementId, feedbackSocketMessage.resultValue, feedbackSocketMessage.roles, form);
        this.broadcast(outgoingMessage.toJson(), courseId, formId);

        // update the form in the database
        form.clearQuestionContents();
        feedbackChannelRepository.update(course);
        return true;
    };
    
}
