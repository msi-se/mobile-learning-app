package de.htwg_konstanz.mobilelearning.services.quiz.socket;

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
        QuizForm form = course.getQuizFormById(new ObjectId(formId));
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

        // check if user is student of the course
        if (!course.isStudent(userId) && !course.isOwner(userId)) {
            System.out.println("User is not a student of the course");
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "User is not a student of the course"));
            return;
        }

        // check if the user is owner or a participant of the form (is registered)
        Boolean isParticipant = form.isParticipant(userId);
        Boolean isOwner = course.isOwner(userId);
        if (!isParticipant && !isOwner) {
            System.out.println(String.format("User %s is not a participant or owner of the form", user.getUsername()));
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, String.format("User %s is not a participant or owner of the form", user.getUsername())));
            return;
        }

        // check if the user is a participant or a owner (by checking if the user is owner of the course)
        SocketConnectionType type = isOwner ? SocketConnectionType.OWNER : SocketConnectionType.PARTICIPANT;

        // add the connection to the list
        SocketConnection socketMember = new SocketConnection(session, courseId, formId, userId, type);
        connections.put(session.getId(), socketMember);

        // send a message to the owner to notify that a new participant has joined
        if (isParticipant) {
            LiveQuizSocketMessage message = new LiveQuizSocketMessage("PARTICIPANT_JOINED", form.status.toString(),
                    null, null, form);
            this.broadcast(message, courseId, formId);
        }
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
    public void onMessage(String message, @PathParam("courseId") String courseId, @PathParam("formId") String formId, @PathParam("userId") String userId) {
        LiveQuizSocketMessage quizSocketMessage = new LiveQuizSocketMessage(message);
        this.evaluateMessage(quizSocketMessage, courseId, formId, userId);
    }

    private void broadcast(LiveQuizSocketMessage message, String courseId, String formId) {

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

            // fill the form with the question contents
            messageToSend.form = new QuizForm(message.form.courseId, message.form.name, message.form.description, message.form.questions, message.form.status, message.form.currentQuestionIndex, message.form.currentQuestionFinished);
            messageToSend.form.setId(message.form.getId());
            messageToSend.form.fillQuestionContents(courseRepository.findById(new ObjectId(courseId)));
            if (connection.getType().equals(SocketConnectionType.PARTICIPANT)) {
                messageToSend.form.clearResults();
            } else {
                messageToSend.form.setParticipants(message.form.getParticipants());
            }

            // send the message
            String messageString = messageToSend.toJson();
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
            System.out.println("Action is null");
            return false;
        }

        // if the user is not an owner or participant, return
        User user = userRepository.findById(new ObjectId(userId));
        if (user == null) { return false; }
        Course course = courseRepository.findById(new ObjectId(courseId));
        if (course == null) { return false; }
        if (!course.isOwner(userId) && !course.isStudent(userId)) { return false; }

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

        System.out.println("Change form status");

        // user must be owner of the course
        if (!course.isOwner(user.getId())) {
            System.out.println("User is not owner of the course");
            return false;
        }

        // evaluate formStatus
        if (quizSocketMessage.formStatus == null || quizSocketMessage.formStatus.equals("")
                || FormStatus.valueOf(quizSocketMessage.formStatus) == null) {
            System.out.println("Form status is invalid");
            return false;
        }

        // get the enum value of the formStatus
        FormStatus formStatusEnum = FormStatus.valueOf(quizSocketMessage.formStatus);
        System.out.println("Form status enum: " + formStatusEnum);

        // get the form
        QuizForm form = course.getQuizFormById(new ObjectId(formId));
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
            form.currentQuestionIndex = 0;
            form.currentQuestionFinished = false;
            // send the event to all receivers
            LiveQuizSocketMessage outgoingMessage = new LiveQuizSocketMessage("RESULT_ADDED", form.status.toString(), null, null, form);
            this.broadcast(outgoingMessage, course.getId().toHexString(), formId);
        }

        // send the updated form to all receivers (stringify the form)
        LiveQuizSocketMessage outgoingMessage = new LiveQuizSocketMessage("FORM_STATUS_CHANGED", form.status.toString(), null, null, form);
        this.broadcast(outgoingMessage, course.getId().toHexString(), formId);

        // update the userstats of the participants and the global stats
        if (formStatusEnum == FormStatus.FINISHED) {
            System.out.println("Update user stats for quiz form");
            System.out.println(form.getParticipants().size());
            this.userService.updateUserStatsByQuizForm(form);
            this.statsService.incrementCompletedQuizForms();
        }

        // update the form in the database
        form.clearQuestionContents();
        courseRepository.update(course);

        return true;
    };

    private Boolean addResult(LiveQuizSocketMessage quizSocketMessage, Course course, String formId, User user) {

        System.out.println("Add result");

        // evaluate resultElementId
        if (quizSocketMessage.resultElementId == null || quizSocketMessage.resultElementId.equals("")) {
            System.out.println("Result questionwrapper ID is invalid");
            return false;
        }

        // evaluate resultValue
        if (quizSocketMessage.resultValues == null || quizSocketMessage.resultValues.size() < 1
                || quizSocketMessage.resultValues.get(0).equals("")) {
            System.out.println("Result value is invalid");
            return false;
        }

        // get the form
        QuizForm form = course.getQuizFormById(new ObjectId(formId));
        if (form == null) {
            System.out.println("Form not found");
            return false;
        }

        // get the questionwrapper
        System.out.println(quizSocketMessage.resultElementId);
        QuestionWrapper questionwrapper = form.getQuestionById(new ObjectId(quizSocketMessage.resultElementId));
        if (questionwrapper == null) {
            System.out.println("Element not found");
            return false;
        }

        // add the result
        String hashedUserId = Hasher.hash(user.getId().toHexString());
        Result result = new Result(hashedUserId, quizSocketMessage.resultValues);
        Boolean wasResultAdded = questionwrapper.addResult(result);
        if (!wasResultAdded) {
            System.out.println("Result was not added (user probably already submitted a result)");
            return false;
        }

        // check if the score of the user has to be updated
        QuizQuestion question = course.getQuizQuestionById(questionwrapper.getQuestionId());
        if (question == null) {
            System.out.println("Question not found");
            return false;
        }
        if (question.getHasCorrectAnswers()) {
            Integer gainedPoints = question.checkAnswer(quizSocketMessage.resultValues);
            form.increaseScoreOfParticipant(user.getId(), gainedPoints);
        }

        // update the form in the database
        courseRepository.update(course);

        // send the updated form to all receivers (stringify the form)
        LiveQuizSocketMessage outgoingMessage = new LiveQuizSocketMessage("RESULT_ADDED", null, quizSocketMessage.resultElementId, quizSocketMessage.resultValues, form);
        this.broadcast(outgoingMessage, course.getId().toHexString(), formId);
        return true;
    };

    private Boolean next(LiveQuizSocketMessage quizSocketMessage, Course course, String formId, User user) {

        System.out.println("Next");

        // user must be owner of the course
        if (!course.isOwner(user.getId())) {
            System.out.println("User is not owner of the course");
            return false;
        }

        // get the form
        QuizForm form = course.getQuizFormById(new ObjectId(formId));
        if (form == null) {
            System.out.println("Form not found");
            return false;
        }

        // next question / finish question / finish quiz
        List<String> events = form.next();
        courseRepository.update(course);

        // for all events, send a message
        events.forEach(event -> {
            LiveQuizSocketMessage outgoingMessage = new LiveQuizSocketMessage(event, form.status.toString(), null, null, form);
            this.broadcast(outgoingMessage, course.getId().toHexString(), formId);
        });

        return true;
    }

}
