package de.htwg_konstanz.mobilelearning.services.feedback.socket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.htwg_konstanz.mobilelearning.services.StatsService;
import de.htwg_konstanz.mobilelearning.services.auth.JwtService;
import de.htwg_konstanz.mobilelearning.services.auth.UserService;

import org.bson.types.ObjectId;

import de.htwg_konstanz.mobilelearning.enums.FormStatus;
import de.htwg_konstanz.mobilelearning.helper.Hasher;
import de.htwg_konstanz.mobilelearning.helper.SocketConnection;
import de.htwg_konstanz.mobilelearning.helper.SocketConnectionType;
import de.htwg_konstanz.mobilelearning.models.Course;
import de.htwg_konstanz.mobilelearning.models.QuestionWrapper;
import de.htwg_konstanz.mobilelearning.models.Result;
import de.htwg_konstanz.mobilelearning.models.auth.User;
import de.htwg_konstanz.mobilelearning.models.feedback.FeedbackForm;
import de.htwg_konstanz.mobilelearning.repositories.CourseRepository;
import de.htwg_konstanz.mobilelearning.repositories.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.websocket.Session;

/**
 * Socket run to live feedback sessions of a course.
 */
@ServerEndpoint("/course/{courseId}/feedback/form/{formId}/subscribe/{userId}/{jwt}")
@ApplicationScoped
public class LiveFeedbackSocket {
    Map<String, SocketConnection> connections = new ConcurrentHashMap<>();

    @Inject
    private CourseRepository courseRepository;

    @Inject
    private UserRepository userRepository;

    @Inject UserService userService;

    @Inject
    JwtService jwtService;

    @Inject
    StatsService statsService;

    /**
     * Method called when a new connection is opened.
     * User has to either student or owner of the course to connect.
     * 
     * @param session  Session that is created for the connection.
     * @param courseId
     * @param formId
     * @param userId
     * @param jwt
     * @throws Exception
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("courseId") String courseId, @PathParam("formId") String formId,
            @PathParam("userId") String userId, @PathParam("jwt") String jwt) throws Exception {
        // userId from Jwt has to match userId from path
        if (!jwtService.getJwtClaims(jwt).getSubject().equals(userId)) {
            connections.remove(session.getId());
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "User not authorized"));
            return;
        }

        // check if course, form and user exist
        Course course = courseRepository.findById(new ObjectId(courseId));
        if (course == null) {
            System.out.println("Course not found");
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Course not found"));
            return;
        }
        FeedbackForm form = course.getFeedbackFormById(new ObjectId(formId));
        if (form == null) {
            System.out.println("Form not found");
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Form not found"));
            return;
        }
        User user = userRepository.findById(new ObjectId(userId));
        if (user == null) {
            System.out.println("User not found");
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "User not found"));
            return;
        }

        // check if user is student or owner of the course
        if (!course.isStudent(userId) && !course.isOwner(userId)) {
            System.out.println("User is not a student of the course");
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "User is not a student of the course"));
            return;
        }

        // register the user (participate)
        Boolean isOwner = course.isOwner(userId);
        Boolean isParticipant = false;
        if (!isOwner) {
            isParticipant = form.addParticipant(new ObjectId(userId));
        }
        if (!isOwner && !isParticipant) {
            System.out.println("User could not be added as participant");
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "User could not be added as participant"));
            return;
        }

        // check if the user is a participant or a owner (by checking if the user is
        // owner of the course)
        SocketConnectionType type = isOwner ? SocketConnectionType.OWNER : SocketConnectionType.PARTICIPANT;

        // add the connection to the list
        SocketConnection socketMember = new SocketConnection(session, courseId, formId, userId, type);
        connections.put(session.getId(), socketMember);

        // send a message to the owner to notify that a new participant has joined
        if (isParticipant) {
            courseRepository.update(course);
            LiveFeedbackSocketMessage message = new LiveFeedbackSocketMessage("PARTICIPANT_JOINED", null, null, null,
                    form);
            this.broadcast(message, courseId, formId);

            this.tellUserIfAlreadySubmitted(form, user, session);
        }
    }

    private void tellUserIfAlreadySubmitted(FeedbackForm form, User user, Session session) {

        // check if the user already submitted a result
        Boolean userAlreadySubmitted = false;
        String hashedUserId = Hasher.hash(user.getId().toHexString());
        for (QuestionWrapper element : form.questions) {
            for (Result result : element.results) {
                if (result.hashedUserId.equals(hashedUserId)) {
                    userAlreadySubmitted = true;
                    break;
                }
            }
        }

        // if the user already submitted a result, send him a message
        if (userAlreadySubmitted) {
            LiveFeedbackSocketMessage message = new LiveFeedbackSocketMessage("ALREADY_SUBMITTED", null, null, null, null);
            this.sendMessageToUser(user, message);
        }
    }

    private void sendMessageToUser(User user, LiveFeedbackSocketMessage message) {

        // search the session of the user
        SocketConnection connection = null;
        for (SocketConnection c : connections.values()) {
            if (c.getUserId().equals(user.getId())) {
                connection = c;
                break;
            }
        }
        if (connection == null) {
            return;
        }

        // send the message
        String messageString = message.toJson();
        connection.session.getAsyncRemote().sendObject(messageString, result ->  {
            if (result.getException() != null) {
                System.out.println("Unable to send message: " + result.getException());
            }
        });
    }

    /**
     * Method called when a connection is closed.
     * 
     * @param session
     * @param courseId
     * @param formId
     * @param userId
     */
    @OnClose
    public void onClose(Session session, @PathParam("courseId") String courseId, @PathParam("formId") String formId,
            @PathParam("userId") String userId) {
        connections.remove(session.getId());
    }

    /**
     * Method called when an error occurs.
     * 
     * @param session
     * @param courseId
     * @param formId
     * @param userId
     * @param throwable
     */
    @OnError
    public void onError(Session session, @PathParam("courseId") String courseId, @PathParam("formId") String formId,
            @PathParam("userId") String userId, Throwable throwable) {
        throwable.printStackTrace();
        connections.remove(session.getId());
    }

    /**
     * Method called when a message is received via broadcast.
     * 
     * @param message
     * @param courseId
     * @param formId
     * @param userId
     */
    @OnMessage
    public void onMessage(String message, @PathParam("courseId") String courseId, @PathParam("formId") String formId,
            @PathParam("userId") String userId) {
        LiveFeedbackSocketMessage feedbackSocketMessage = new LiveFeedbackSocketMessage(message);
        this.evaluateMessage(feedbackSocketMessage, courseId, formId, userId);
    }

    private void broadcast(LiveFeedbackSocketMessage message, String courseId, String formId) {

        // copy the message to not change the original 
        LiveFeedbackSocketMessage messageToSend = new LiveFeedbackSocketMessage(message.action, message.formStatus,
                message.resultElementId, message.resultValues, null);

        connections.values().forEach(connection -> {

            // check if the course ID and form ID match
            if (!connection.getCourseId().equals(new ObjectId(courseId))) {
                return;
            }
            if (!connection.getFormId().equals(new ObjectId(formId))) {
                return;
            }

            // check what the user is allowed to see and if the user has to be notified
            // if the user doesn't have to be notified, return
            if ((messageToSend.action.equals("RESULT_ADDED") || messageToSend.action.equals("PARTICIPANT_JOINED"))

                    && connection.getType().equals(SocketConnectionType.PARTICIPANT)) {
                return;
            }
            
            // fill the form with the question contents
            messageToSend.form = new FeedbackForm(message.form.courseId, message.form.name, message.form.description, message.form.questions, message.form.status);
            messageToSend.form.setId(message.form.getId());
            messageToSend.form.fillQuestionContents(courseRepository.findById(new ObjectId(courseId)));
            if (connection.getType().equals(SocketConnectionType.PARTICIPANT)) {
                messageToSend.form.clearResults();
            } else {
                messageToSend.form.setParticipants(message.form.getParticipants());
            }


            // send the message
            String messageString = message.toJson();
            connection.session.getAsyncRemote().sendObject(messageString, result -> {
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

        // if the user is not an owner or participant, return
        User user = userRepository.findById(new ObjectId(userId));
        if (user == null) { return false; }
        Course course = courseRepository.findById(new ObjectId(courseId));
        if (course == null) { return false; }
        if (!course.isOwner(userId) && !course.isStudent(userId)) { return false; }

        if (feedbackSocketMessage.action.equals("CHANGE_FORM_STATUS")) {
            return this.changeFormStatus(feedbackSocketMessage, course, formId, user);
        }

        if (feedbackSocketMessage.action.equals("ADD_RESULT")) {
            return this.addResult(feedbackSocketMessage, course, formId, user);
        }

        return false;
    };

    private Boolean changeFormStatus(LiveFeedbackSocketMessage feedbackSocketMessage, Course course, String formId,
            User user) {

        // check if the user is an owner of the course
        if (!course.isOwner(user.getId())) {
            System.out.println("Not an owner of the course");
            return false;
        }

        System.out.println("Change form status");

        // evaluate formStatus
        if (feedbackSocketMessage.formStatus == null || feedbackSocketMessage.formStatus.equals("")
                || FormStatus.valueOf(feedbackSocketMessage.formStatus) == null) {
            System.out.println("Form status is invalid");
            return false;
        }

        // get the enum value of the formStatus
        FormStatus formStatusEnum = FormStatus.valueOf(feedbackSocketMessage.formStatus);
        System.out.println("Form status enum: " + formStatusEnum);

        // get the form
        FeedbackForm form = course.getFeedbackFormById(new ObjectId(formId));
        if (form == null) {
            System.out.println("Form not found");
            return false;
        }

        // change the form status
        form.setStatus(formStatusEnum);

        // if it is set to NOT_STARTED, remove all results
        if (formStatusEnum == FormStatus.NOT_STARTED) {
            form.clearResults();
            form.clearParticipants();
            // send the event to all receivers
            LiveFeedbackSocketMessage outgoingMessage = new LiveFeedbackSocketMessage("RESULT_ADDED",
                    form.status.toString(), null, null, form);
            this.broadcast(outgoingMessage, course.getId().toHexString(), formId);
        }

        // if it is set to STARTED set the timestamp
        if (formStatusEnum == FormStatus.STARTED) {
            form.setStartTimestamp();
        }

        // send the updated form to all receivers (stringify the form)
        LiveFeedbackSocketMessage outgoingMessage = new LiveFeedbackSocketMessage("FORM_STATUS_CHANGED",
                form.status.toString(), null, null, form);
        this.broadcast(outgoingMessage, course.getId().toHexString(), formId);

        // update the userstats of the participants and the global stats
        if (formStatusEnum == FormStatus.FINISHED) {
            this.userService.updateUserStatsByFeedbackForm(form);
            this.statsService.incrementCompletedFeedbackForms();
        }

        // update the form in the database
        form.clearQuestionContents();
        courseRepository.update(course);

        return true;
    };

    private Boolean addResult(LiveFeedbackSocketMessage feedbackSocketMessage, Course course, String formId,
            User user) {

        System.out.println("Add result");

        // evaluate resultElementId
        if (feedbackSocketMessage.resultElementId == null || feedbackSocketMessage.resultElementId.equals("")) {
            System.out.println("Result element ID is invalid");
            return false;
        }

        // evaluate resultValue
        if (feedbackSocketMessage.resultValues == null || feedbackSocketMessage.resultValues.size() < 1
                || feedbackSocketMessage.resultValues.get(0).equals("")) {
            System.out.println("Result value is invalid");
            return false;
        }

        // get the form
        FeedbackForm form = course.getFeedbackFormById(new ObjectId(formId));
        if (form == null) {
            System.out.println("Form not found");
            return false;
        }

        // get the element
        QuestionWrapper element = form.getQuestionById(new ObjectId(feedbackSocketMessage.resultElementId));
        if (element == null) {
            System.out.println("Element not found");
            return false;
        }

        // add the result
        String hashedUserId = Hasher.hash(user.getId().toHexString());
        Result result = new Result(hashedUserId, feedbackSocketMessage.resultValues);
        Boolean wasResultAdded = element.addResult(result);
        if (!wasResultAdded) {
            System.out.println("Result was not added (user probably already submitted a result)");
            return false;
        }

        // update the form in the database
        courseRepository.update(course);

        // send the updated form to all receivers (stringify the form)
        LiveFeedbackSocketMessage outgoingMessage = new LiveFeedbackSocketMessage("RESULT_ADDED", null,
                feedbackSocketMessage.resultElementId, feedbackSocketMessage.resultValues,
                form);
        this.broadcast(outgoingMessage, course.getId().toHexString(), formId);
        return true;
    };

}
