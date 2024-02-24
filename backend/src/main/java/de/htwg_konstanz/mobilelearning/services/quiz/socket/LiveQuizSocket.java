package de.htwg_konstanz.mobilelearning.services.quiz.socket;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.htwg_konstanz.mobilelearning.services.auth.JwtService;

import org.bson.types.ObjectId;

import de.htwg_konstanz.mobilelearning.enums.FormStatus;
import de.htwg_konstanz.mobilelearning.helper.Hasher;
import de.htwg_konstanz.mobilelearning.helper.SocketConnection;
import de.htwg_konstanz.mobilelearning.helper.SocketConnectionType;
import de.htwg_konstanz.mobilelearning.models.Course;
import de.htwg_konstanz.mobilelearning.models.QuestionWrapper;
import de.htwg_konstanz.mobilelearning.models.Result;
import de.htwg_konstanz.mobilelearning.models.auth.User;
import de.htwg_konstanz.mobilelearning.models.auth.UserRole;
import de.htwg_konstanz.mobilelearning.models.quiz.QuizForm;
import de.htwg_konstanz.mobilelearning.models.quiz.QuizQuestion;
import de.htwg_konstanz.mobilelearning.repositories.CourseRepository;
import de.htwg_konstanz.mobilelearning.repositories.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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
    JwtService jwtService;

    /**
     * Method called when a new connection is opened.
     * User has to either (student & participant of the session) or owner of the course to connect.
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
        @PathParam("jwt") String jwt
        ) throws Exception {
        // userId from Jwt has to match userId from path
        if (jwtService.getJwtClaims(jwt).getSubject().equals(userId)){

            // check if course, form and user exist
            Course course = courseRepository.findById(new ObjectId(courseId));
            if (course == null) {
                System.out.println("Course not found");
                return;
            }
            QuizForm form = course.getQuizFormById(new ObjectId(formId));
            if (form == null) {
                System.out.println("Form not found");
                return;
            }
            User user = userRepository.findById(new ObjectId(userId));
            if (user == null) {
                System.out.println("User not found");
                return;
            }

            // check if the user is owner or a participant of the form (is registered)
            Boolean isParticipant = form.isParticipant(userId);
            Boolean isOwner = course.isOwner(userId);
            if (!isParticipant && !isOwner) {
                System.out.println("User is not a participant of the course. Please register first.");
                return;
            }

            // check if user is student of the course
            if (!course.isStudent(userId) && !course.isOwner(userId)) {
                System.out.println("User is not a student of the course");
                return;
            }

            // check if the user is a participant or a owner (by checking if the user is owner of the course)
            SocketConnectionType type = isOwner ? SocketConnectionType.OWNER : SocketConnectionType.PARTICIPANT;

            // add the connection to the list
            SocketConnection socketMember = new SocketConnection(session, courseId, formId, userId, type);
            connections.put(session.getId(), socketMember);
        } else {
            connections.remove(session.getId());
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
    public void onClose(Session session, @PathParam("courseId") String courseId, @PathParam("formId") String formId, @PathParam("userId") String userId) {
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
    public void onError(Session session, @PathParam("courseId") String courseId, @PathParam("formId") String formId, @PathParam("userId") String userId, Throwable throwable) {
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
            if (message.action.equals("RESULT_ADDED") && connection.getType().equals(SocketConnectionType.PARTICIPANT)) {
                return;
            }
            if (connection.getType().equals(SocketConnectionType.PARTICIPANT)) {
                // not show the results
                message.form = message.form.copyWithoutResultsAndParticipantsButWithQuestionContents(courseRepository.findById(new ObjectId(courseId)));
            } else {
                // show the results
                message.form = message.form.copyWithQuestionContents(courseRepository.findById(new ObjectId(courseId)));
            }

            // send the message
            String messageString = message.toJson();
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

        if (quizSocketMessage.action.equals("CHANGE_FORM_STATUS")) {
            return this.changeFormStatus(quizSocketMessage, courseId, formId, userId);
        }

        if (quizSocketMessage.action.equals("ADD_RESULT")) {
            return this.addResult(quizSocketMessage, courseId, formId, userId);
        }

        if (quizSocketMessage.action.equals("NEXT")) {
            return this.next(quizSocketMessage, courseId, formId, userId);
        }

        return false;
    };

    private Boolean changeFormStatus(LiveQuizSocketMessage quizSocketMessage, String courseId, String formId, String userId) {

        // check if the user has the role Prof
        if(!quizSocketMessage.roles.contains(UserRole.PROF)){
            System.out.println("You need the role Prof to change the form status");
            return false;
        }

        // check if the user is an owner of the course
        Course course = courseRepository.findById(new ObjectId(courseId));
        if (course == null) {
            System.out.println("Course not found");
            return false;
        }
        if (!course.isOwner(userId)) {
            System.out.println("Not an owner of the course");
            return false;
        }
        

        System.out.println("Change form status");

        // evaluate formStatus
        if (quizSocketMessage.formStatus == null || quizSocketMessage.formStatus.equals("") || FormStatus.valueOf(quizSocketMessage.formStatus) == null) {
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
            LiveQuizSocketMessage outgoingMessage = new LiveQuizSocketMessage("RESULT_ADDED", form.status.toString(), null, null, null, form);
            this.broadcast(outgoingMessage, courseId, formId);
        }
        
        // send the updated form to all receivers (stringify the form)
        LiveQuizSocketMessage outgoingMessage = new LiveQuizSocketMessage("FORM_STATUS_CHANGED", form.status.toString(), null, null, null, form);
        this.broadcast(outgoingMessage, courseId, formId);

        // update the form in the database
        form.clearQuestionContents();
        courseRepository.update(course);

        return true;
    };

    private Boolean addResult(LiveQuizSocketMessage quizSocketMessage, String courseId, String formId, String userId) {

        System.out.println("Add result");

        // evaluate resultElementId
        if (quizSocketMessage.resultElementId == null || quizSocketMessage.resultElementId.equals("")) {
            System.out.println("Result questionwrapper ID is invalid");
            return false;
        }

        // evaluate resultValue
        if (quizSocketMessage.resultValues == null || quizSocketMessage.resultValues.size() < 1 || quizSocketMessage.resultValues.get(0).equals("")) {
            System.out.println("Result value is invalid");
            return false;
        }

        // get the form
        Course course = courseRepository.findById(new ObjectId(courseId));
        if (course == null) {
            System.out.println("Course not found");
            return false;
        }
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
        String hashedUserId = Hasher.hash(userId);
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
            form.increaseScoreOfParticipant(new ObjectId(userId), gainedPoints);
        }

        // update the form in the database
        courseRepository.update(course);

        // send the updated form to all receivers (stringify the form)
        LiveQuizSocketMessage outgoingMessage = new LiveQuizSocketMessage("RESULT_ADDED", null, quizSocketMessage.resultElementId, quizSocketMessage.resultValues, quizSocketMessage.roles, form);
        this.broadcast(outgoingMessage, courseId, formId);
        return true;
    };

    private Boolean next(LiveQuizSocketMessage quizSocketMessage, String courseId, String formId, String userId) {

        // check if the user has the role Prof
        if(!quizSocketMessage.roles.contains(UserRole.PROF)){
            System.out.println("You need the role Prof to get the next question");
            return false;
        }

        // check if the user is an owner of the course
        Course course = courseRepository.findById(new ObjectId(courseId));
        if (course == null) {
            System.out.println("Course not found");
            return false;
        }
        if (!course.isOwner(userId)) {
            System.out.println("Not an owner of the course");
            return false;
        }

        System.out.println("Next");

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
            LiveQuizSocketMessage outgoingMessage = new LiveQuizSocketMessage(event, form.status.toString(), null, null, null, form);
            this.broadcast(outgoingMessage, courseId, formId);
        });

        return true;
    }
    
}
