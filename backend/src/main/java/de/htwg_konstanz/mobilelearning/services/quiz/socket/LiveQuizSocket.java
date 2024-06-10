package de.htwg_konstanz.mobilelearning.services.quiz.socket;

import java.util.ArrayList;
import java.util.List;
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
import de.htwg_konstanz.mobilelearning.models.quiz.QuizForm;
import de.htwg_konstanz.mobilelearning.models.quiz.QuizQuestion;
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
 * Socket run to live quiz sessions of a course.
 */
@ServerEndpoint("/course/{courseId}/quiz/form/{formId}/subscribe/{userId}/{jwt}")
@ApplicationScoped
public class LiveQuizSocket {
    Map<String, SocketConnection> connections = new ConcurrentHashMap<>();

    @Inject
    private CourseRepository courseRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private UserService userService;

    @Inject
    JwtService jwtService;

    @Inject
    StatsService statsService;

    private void log(String message) {
        System.out.println("LiveQuizSocket: " + message);
    }

    /**
     * Method called when a new connection is opened.
     * User has to either (student & participant of the session) or owner of the
     * course to connect.
     * Before the connection user call participate method.
     * 
     * @param session
     * @param courseId
     * @param formId
     * @param userId
     * @param jwt
     * @throws Exception
     */
    @OnOpen
    public void onOpen(
            Session session,
            @PathParam("courseId") String courseId,
            @PathParam("formId") String formId,
            @PathParam("userId") String userId,
            @PathParam("jwt") String jwt) throws Exception {

        // userId from Jwt has to match userId from path
        if (!jwtService.getJwtClaims(jwt).getSubject().equals(userId)) {
            connections.remove(session.getId());
            log(String.format("User %s not authorized (jwt does not match userId)", userId));
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "User not authorized"));
            return;
        }

        // check if course, form and user exist
        Course course = courseRepository.findById(new ObjectId(courseId));
        if (course == null) {
            log(String.format("Course %s not found", courseId));
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Course not found"));
            return;
        }
        QuizForm form = course.getQuizFormById(new ObjectId(formId));
        if (form == null) {
            log(String.format("Form %s not found", formId));
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Form not found"));
            return;
        }
        User user = userRepository.findById(new ObjectId(userId));
        if (user == null) {
            log(String.format("User %s not found", userId));
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "User not found"));
            return;
        }

        // check if user is student of the course
        if (!course.isStudent(userId) && !course.isOwner(userId)) {
            log(String.format("User %s is not a student of the course %s", user.getUsername(), course.getName()));
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "User is not a student of the course"));
            return;
        }

        // check if the user is owner or a participant of the form (is registered)
        Boolean isParticipant = form.isParticipant(userId);
        Boolean isOwner = course.isOwner(userId);
        if (!isParticipant && !isOwner) {
            log(String.format("User %s is not a participant or owner of the form", user.getUsername()));
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, String.format("User %s is not a participant or owner of the form", user.getUsername())));
            return;
        }

        // check if the user is a participant or a owner (by checking if the user is owner of the course)
        SocketConnectionType type = isOwner ? SocketConnectionType.OWNER : SocketConnectionType.PARTICIPANT;

        // add the connection to the list
        SocketConnection socketMember = new SocketConnection(session, courseId, formId, userId, type);
        log(String.format("User %s connected as %s", user.getUsername(), type));
        connections.put(session.getId(), socketMember);

        // send a message to the owner to notify that a new participant has joined
        if (isParticipant) {
            LiveQuizSocketMessage message = new LiveQuizSocketMessage("PARTICIPANT_JOINED", form.status.toString(),
                    null, null, form);
            this.broadcast(message, courseId, formId, course);

            this.tellUserIfAlreadySubmitted(form, user, session);
        }
    }

    private void tellUserIfAlreadySubmitted(QuizForm form, User user, Session session) {

        // check if the user already submitted a result
        Boolean userAlreadySubmitted = false;
        String hashedUserId = Hasher.hash(user.getId().toHexString());
        List<String> userAnswers = new ArrayList<String>();
        QuestionWrapper currentQuestionWrapper = form.questions.get(form.currentQuestionIndex);
        for (Result result : currentQuestionWrapper.results) {
            if (result.hashedUserId.equals(hashedUserId)) {
                userAlreadySubmitted = true;
                userAnswers = result.values;
                break;
            }
        }

        // if the user already submitted a result, send him a message
        if (userAlreadySubmitted) {
            LiveQuizSocketMessage message = new LiveQuizSocketMessage("ALREADY_SUBMITTED", null, null, null, null, userAnswers);
            this.sendMessageToUser(user, message);
        }
    }

    private void sendMessageToUser(User user, LiveQuizSocketMessage message) {

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
        log(String.format("User %s disconnected", userId));
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
    public void onMessage(String message, @PathParam("courseId") String courseId, @PathParam("formId") String formId, @PathParam("userId") String userId) {
        LiveQuizSocketMessage quizSocketMessage = new LiveQuizSocketMessage(message);
        this.evaluateMessage(quizSocketMessage, courseId, formId, userId);
    }

    private void broadcast(LiveQuizSocketMessage message, String courseId, String formId, Course course) {

        // copy the message to not change the original 
        LiveQuizSocketMessage messageToSend = new LiveQuizSocketMessage(message.action, message.formStatus, message.resultElementId, message.resultValues, null);

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

            // if the action is "CLOSED_QUESTION" all participants should have filled the userHasAnsweredCorrectly field
            if (messageToSend.action.equals("CLOSED_QUESTION") && connection.getType().equals(SocketConnectionType.PARTICIPANT)) {
                
                // check if user has answered correctly
                Integer currentQuestionIndex = message.form.currentQuestionIndex;
                QuestionWrapper questionWrapper = message.form.questions.get(currentQuestionIndex);
                QuizQuestion question = course.getQuizQuestionById(questionWrapper.getQuestionId());
                Result userResult = questionWrapper.getResultByUserId(connection.getUserId());
                messageToSend.userHasAnsweredCorrectly = userResult != null && userResult.getGainedPoints() > 0;
                messageToSend.gainedPoints = userResult != null ? userResult.getGainedPoints() : 0;

                // also append the correct answers
                messageToSend.correctAnswers = question.getCorrectAnswers();
            }

            // fill the form with the question contents
            messageToSend.form = message.form.deepCopy();
            messageToSend.form.fillQuestionContents(course);
            if (connection.getType().equals(SocketConnectionType.PARTICIPANT)) {
                messageToSend.form.clearResults();
            }
            messageToSend.form.setParticipants(message.form.getParticipants());

            // send the message
            String messageString = messageToSend.toJson();
            log("SEND: " + messageString);
            connection.session.getAsyncRemote().sendObject(messageString, result ->  {
                if (result.getException() != null) {
                    System.out.println("Unable to send message: " + result.getException());
                }
            });
        });
    }

    private Boolean evaluateMessage(LiveQuizSocketMessage quizSocketMessage, String courseId, String formId, String userId) {

        // evaluate action
        if (quizSocketMessage.action == null || quizSocketMessage.action.equals("")) {
            log("Action is invalid");
            return false;
        }
        
        if (quizSocketMessage.action.equals("FUN")) {
            return this.fun(quizSocketMessage, formId);
        }
                
        log("RECEIVED: " + quizSocketMessage.toJson());

        // if the user is not an owner or participant, return
        User user = userRepository.findById(new ObjectId(userId));
        if (user == null) {
            log(String.format("User %s not found", userId));
            return false;
        }
        Course course = courseRepository.findById(new ObjectId(courseId));
        if (course == null) {
            log(String.format("Course %s not found", courseId));
            return false;
        }
        if (!course.isOwner(userId) && !course.isStudent(userId)) {
            log(String.format("User %s is not a student or owner of the course", user.getUsername()));
            return false;
        }

        if (quizSocketMessage.action.equals("CHANGE_FORM_STATUS")) {
            return this.changeFormStatus(quizSocketMessage, course, formId, user);
        }

        if (quizSocketMessage.action.equals("ADD_RESULT")) {
            return this.addResult(quizSocketMessage, course, formId, user);
        }

        if (quizSocketMessage.action.equals("NEXT")) {
            return this.next(quizSocketMessage, course, formId, user);
        }

        return false;
    };

    private Boolean changeFormStatus(LiveQuizSocketMessage quizSocketMessage, Course course, String formId, User user) {

        // user must be owner of the course
        if (!course.isOwner(user.getId())) {
            log(String.format("User %s is not owner of the course", user.getUsername()));
            return false;
        }

        // evaluate formStatus
        if (quizSocketMessage.formStatus == null || quizSocketMessage.formStatus.equals("")
                || FormStatus.valueOf(quizSocketMessage.formStatus) == null) {
            log(String.format("Form status %s is invalid", quizSocketMessage.formStatus));
            return false;
        }

        // get the enum value of the formStatus
        FormStatus formStatusEnum = FormStatus.valueOf(quizSocketMessage.formStatus);

        // get the form
        QuizForm form = course.getQuizFormById(new ObjectId(formId));
        if (form == null) {
            log(String.format("Form %s not found", formId));
            return false;
        }

        // change the form status
        form.setStatus(formStatusEnum);
        log(String.format("Form status of form %s changed to %s", formId, formStatusEnum.toString()));

        // if it is set to NOT_STARTED, remove all results
        if (formStatusEnum == FormStatus.NOT_STARTED) {
            log("Clear results and participants of quiz form");
            form.clearResults();
            form.clearParticipants();
            form.currentQuestionIndex = 0;
            form.currentQuestionFinished = false;
            // send the event to all receivers
            LiveQuizSocketMessage outgoingMessage = new LiveQuizSocketMessage("RESULT_ADDED", form.status.toString(), null, null, form);
            this.broadcast(outgoingMessage, course.getId().toHexString(), formId, course);
        }

        // if it is set to STARTED set the timestamp
        if (formStatusEnum == FormStatus.STARTED) {
            log("Set start timestamp of quiz form");
            form.setStartTimestamp();
        }

        // send the updated form to all receivers (stringify the form)
        LiveQuizSocketMessage outgoingMessage = new LiveQuizSocketMessage("FORM_STATUS_CHANGED", form.status.toString(), null, null, form);
        this.broadcast(outgoingMessage, course.getId().toHexString(), formId, course);

        // update the userstats of the participants and the global stats
        if (formStatusEnum == FormStatus.FINISHED) {
            log("Update user stats and increment completed quiz forms");
            this.userService.updateUserStatsByQuizForm(form);
            this.statsService.incrementCompletedQuizForms();
        }

        // update the form in the database
        form.clearQuestionContents();
        courseRepository.update(course);

        return true;
    };

    private Boolean addResult(LiveQuizSocketMessage quizSocketMessage, Course course, String formId, User user) {

        // evaluate resultElementId
        if (quizSocketMessage.resultElementId == null || quizSocketMessage.resultElementId.equals("")) {
            log(String.format("Result element ID %s is invalid", quizSocketMessage.resultElementId));
            return false;
        }

        // evaluate resultValue
        if (quizSocketMessage.resultValues == null || quizSocketMessage.resultValues.size() < 1
                || quizSocketMessage.resultValues.get(0).equals("")) {
            log("Result values are invalid");
            return false;
        }

        // get the form
        QuizForm form = course.getQuizFormById(new ObjectId(formId));
        if (form == null) {
            log(String.format("Form %s not found", formId));
            return false;
        }

        // get the questionwrapper
        System.out.println(quizSocketMessage.resultElementId);
        QuestionWrapper questionwrapper = form.getQuestionById(new ObjectId(quizSocketMessage.resultElementId));
        if (questionwrapper == null) {
            log(String.format("Questionwrapper %s not found", quizSocketMessage.resultElementId));
            return false;
        }

        // add the result
        String hashedUserId = Hasher.hash(user.getId().toHexString());
        Result result = new Result(hashedUserId, quizSocketMessage.resultValues);
        Boolean wasResultAdded = questionwrapper.addResult(result);
        if (!wasResultAdded) {
            log(String.format("Result for user %s was not added", user.getUsername()));
            return false;
        }

        // check if the score of the user has to be updated
        QuizQuestion question = course.getQuizQuestionById(questionwrapper.getQuestionId());
        if (question == null) {
            log(String.format("Question %s not found", questionwrapper.getQuestionId()));
            return false;
        }
        if (question.getHasCorrectAnswers()) {
            Integer gainedPoints = question.checkAnswer(quizSocketMessage.resultValues);

            // if the user was the first to answer correctly, increase the score by 5, second by 4, ...
            if (gainedPoints > 0) {
                Integer participantsAnsweredCorrectly = form.getParticipantsAnsweredCorrectly(questionwrapper.getId());
                if (participantsAnsweredCorrectly < 5) {
                    gainedPoints += 5 - participantsAnsweredCorrectly;
                }
            }

            // update the result with the gained points and increase the score of the user
            result.setGainedPoints(gainedPoints);
            form.increaseScoreOfParticipant(user.getId(), gainedPoints);
        }

        // update the form in the database
        courseRepository.update(course);

        // send the updated form to all receivers (stringify the form)
        LiveQuizSocketMessage outgoingMessage = new LiveQuizSocketMessage("RESULT_ADDED", null, quizSocketMessage.resultElementId, quizSocketMessage.resultValues, form);
        this.broadcast(outgoingMessage, course.getId().toHexString(), formId, course);
        return true;
    };

    private Boolean next(LiveQuizSocketMessage quizSocketMessage, Course course, String formId, User user) {

        // user must be owner of the course
        if (!course.isOwner(user.getId())) {
            log(String.format("User %s is not owner of the course", user.getUsername()));
            return false;
        }

        // get the form
        QuizForm form = course.getQuizFormById(new ObjectId(formId));
        if (form == null) {
            log(String.format("Form %s not found", formId));
            return false;
        }

        // next question / finish question / finish quiz
        List<String> events = form.next();
        courseRepository.update(course);

        log(String.format("NEXT produced events: %s", events));
        log(String.format("Current question index: %d; Current question finished: %b", form.currentQuestionIndex, form.currentQuestionFinished));

        // for all events, send a message
        events.forEach(event -> {
            LiveQuizSocketMessage outgoingMessage = new LiveQuizSocketMessage(event, form.status.toString(), null, null, form);
            this.broadcast(outgoingMessage, course.getId().toHexString(), formId, course);
        });

        return true;
    }

    private Boolean fun(LiveQuizSocketMessage quizSocketMessage, String formId) {
        
        quizSocketMessage = new LiveQuizSocketMessage(quizSocketMessage.action, quizSocketMessage.fun);

        // send the message
        String messageString = quizSocketMessage.toJson();

        connections.values().forEach(connection -> {
            // check if the form ID match
            if (!connection.getFormId().equals(new ObjectId(formId))) {
                return;
            }

            connection.session.getAsyncRemote().sendObject(messageString, result ->  {
                if (result.getException() != null) {
                    System.out.println("Unable to send message: " + result.getException());
                }
            });
        });

        return true;
    }
}
