package de.htwg_konstanz.mobilelearning.services.quiz.socket;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import de.htwg_konstanz.mobilelearning.Helper;
import de.htwg_konstanz.mobilelearning.MockMongoTestProfile;
import de.htwg_konstanz.mobilelearning.MockUser;
import de.htwg_konstanz.mobilelearning.SocketClient;
import de.htwg_konstanz.mobilelearning.models.Course;
import de.htwg_konstanz.mobilelearning.models.Result;
import de.htwg_konstanz.mobilelearning.models.quiz.QuizForm;
import de.htwg_konstanz.mobilelearning.services.CourseService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.Session;

@QuarkusTest
@TestProfile(MockMongoTestProfile.class)
public class LiveQuizSocketTest {

    @Inject
    private CourseService courseService;
    
    @BeforeEach
    void init(TestInfo testInfo){
        System.out.println("------------------------------");
        System.out.println("Test: " + testInfo.getDisplayName());
        courseService.deleteAllCourses();
    
    }

    @Test
    public void startQuizForm() {
        // create & get courses
        List<Course> courses = Helper.createCourse();
        Course course = courses.get(0);

        // get course and quiz form id
        String courseId = course.getId().toString();
        String formId = course.getQuizForms().get(0).getId().toString();

        // create user and sync the course-user relation
        MockUser prof = Helper.createMockUser("Prof");
        given().header("Authorization", "Bearer " + prof.getJwt()).get("/course").then().statusCode(200);

        // store a timestamp to check if the form's startTimestamp has been set
        Date startTimestamp = new Date();

        // create a websocket client
        try {
            SocketClient client = new SocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + prof.getId() + "/" + prof.getJwt())
            );
            Thread.sleep(100);
            Assertions.assertTrue(session.isOpen());
            client.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "STARTED"
                }
            """);
            Thread.sleep(100);
            session.close();

            // check if the form status has changed
            Assertions.assertTrue(courseService.getCourse(courseId).getQuizForms().get(0).getStatus().toString().equals("STARTED"));
        } catch (Exception e) {
            System.out.println(e);
            Assertions.fail(e.getMessage());
        }

        // check if the startTimestamp has been set and is less than 1 minute ago
        Date formStartTimestamp = courseService.getCourse(courseId).getQuizForms().get(0).getStartTimestamp();
        Assertions.assertNotNull(formStartTimestamp);
        Assertions.assertTrue(formStartTimestamp.after(startTimestamp));
    }

    @Test
    public void startQuizFormNotOwner() {
        // create & get courses
        List<Course> courses = Helper.createCourse("Prof");
        Course course = courses.get(0);

        // get course and quiz form id
        String courseId = course.getId().toString();
        String formId = course.getQuizForms().get(0).getId().toString();

        // create user and sync the course-user relation
        MockUser prof2 = Helper.createMockUser("Prof2");
        given().header("Authorization", "Bearer " + prof2.getJwt()).get("/course").then().statusCode(200);

        // create a websocket client
        try {
            SocketClient client = new SocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + prof2.getId() + "/" + prof2.getJwt())
            );
            Thread.sleep(100);
            Assertions.assertFalse(session.isOpen());
            client.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "STARTED"
                }
            """);
            Thread.sleep(100);
            session.close();

            // check if the form status has not changed
            Assertions.assertTrue(courseService.getCourse(courseId).getQuizForms().get(0).getStatus().toString().equals("NOT_STARTED"));
        } catch (Exception e) {
            System.out.println(e);
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    public void startQuizFormStudent() {
        // create & get courses
        List<Course> courses = Helper.createCourse();
        Assertions.assertEquals(courses.size(), 1);
        Course course = courses.get(0);
        Assertions.assertEquals(course.getQuizForms().size(), 1);

        // get course and quiz form id
        String courseId = course.getId().toString();
        String formId = course.getQuizForms().get(0).getId().toString();

        // create user and sync the course-user relation
        MockUser student = Helper.createMockUser("Student");
        given().header("Authorization", "Bearer " + student.getJwt()).get("/course").then().statusCode(200);

        // participate in the quiz form
        given()
            .header("Authorization", "Bearer " + student.getJwt())
            .pathParam("courseId", courseId)
            .pathParam("formId", formId)
            .body("alias-student")
            .when()
            .post("/course/{courseId}/quiz/form/{formId}/participate")
            .then()
            .statusCode(200);

        // create a websocket client
        try {
            SocketClient client = new SocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + student.getId() + "/" + student.getJwt())
            );
            Thread.sleep(100);
            Assertions.assertTrue(session.isOpen());
            client.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "STARTED"
                }
            """);
            Thread.sleep(100);
            session.close();

            // check if the form status has not changed
            Assertions.assertTrue(courseService.getCourse(courseId).getQuizForms().get(0).getStatus().toString().equals("NOT_STARTED"));
        } catch (Exception e) {
            System.out.println(e);
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    public void addResult() {
        //create & get courses + ids
        List<Course> courses = Helper.createCourse();
        String courseId = courses.getFirst().getId().toString();
        String formId = courses.getFirst().getQuizForms().get(0).getId().toString();
        String questionId = courses.getFirst().quizForms.get(0).questions.get(0).getId().toString();
        
        // add a result & get quiz forms
        addResult(courseId, formId, questionId);
        Response response = given()
                            .header("Authorization", "Bearer " + Helper.createMockUser("Prof").getJwt())
                            .pathParam("courseId", courseId)
                            .pathParam("formId", formId)
                            .queryParam("results", true)
                            .when()
                            .get("/course/{courseId}/quiz/form/{formId}");
        QuizForm quizFormFromService = response
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .jsonPath()
                        .getObject(".", QuizForm.class);
        
        // check that the result was added
        Assertions.assertEquals("Rollenverständnis bei Scrum", quizFormFromService.name);
        Assertions.assertEquals("Ein Quiz zum Rollenverständnis und Teamaufbau bei Scrum", quizFormFromService.description);
        Assertions.assertEquals(2, quizFormFromService.questions.size());
        Assertions.assertEquals(1, quizFormFromService.questions.get(0).results.size());
        Assertions.assertEquals("1", quizFormFromService.questions.get(0).results.get(0).values.get(0));	
    }

    @Test
    public void submitResultOnlyAcceptedOnce() {
        //create & get courses + ids
        List<Course> courses = Helper.createCourse();
        String courseId = courses.getFirst().getId().toString();
        String formId = courses.getFirst().getQuizForms().get(0).getId().toString();
        String questionId = courses.getFirst().quizForms.get(0).questions.get(0).getId().toString();

        // create user and sync the course-user relation
        MockUser prof = Helper.createMockUser("Prof");
        given().header("Authorization", "Bearer " + prof.getJwt()).get("/course").then().statusCode(200);
        
        // add a result & get quiz forms
        addResult(courseId, formId, questionId);
        addResult(courseId, formId, questionId);
        Response response = given()
                                .header("Authorization", "Bearer " + prof.getJwt())
                                .pathParam("courseId", courseId)
                                .pathParam("formId", formId)
                                .queryParam("results", true)
                                .when()
                                .get("/course/{courseId}/quiz/form/{formId}");
        QuizForm quizFormFromService = response
                                            .then()
                                            .statusCode(200)
                                            .extract()
                                            .body()
                                            .jsonPath()
                                            .getObject(".", QuizForm.class);
        
        // check that only one result was added
        Assertions.assertEquals("Rollenverständnis bei Scrum", quizFormFromService.name);
        Assertions.assertEquals("Ein Quiz zum Rollenverständnis und Teamaufbau bei Scrum", quizFormFromService.description);
        Assertions.assertEquals(2, quizFormFromService.questions.size());
        Assertions.assertEquals(1, quizFormFromService.questions.get(0).results.size());
        Assertions.assertEquals("1", quizFormFromService.questions.get(0).results.get(0).values.get(0));	
    }

    @Test
    public void stopQuizForm() {
        // create & get courses
        List<Course> courses = Helper.createCourse();
        Course course = courses.get(0);

        // get course and quiz form id
        String courseId = course.getId().toString();
        String formId = course.getQuizForms().get(0).getId().toString();

        // create user and sync the course-user relation
        MockUser prof = Helper.createMockUser("Prof");
        given().header("Authorization", "Bearer " + prof.getJwt()).get("/course").then().statusCode(200);

        // create a websocket client
        try {
            SocketClient client = new SocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + prof.getId() + "/" + prof.getJwt())
            );
            Thread.sleep(100);
            Assertions.assertTrue(session.isOpen());
            client.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "STARTED"
                }
            """);
            Thread.sleep(100);
            client.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "FINISHED"
                }
            """);
            Thread.sleep(100);
            session.close();

            // check if the form status has changed
            Assertions.assertTrue(courseService.getCourse(courseId).getQuizForms().get(0).getStatus().toString().equals("FINISHED"));
        } catch (Exception e) {
            System.out.println(e);
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    public void stopQuizFormNotOwner() {
        // create & get courses
        List<Course> courses = Helper.createCourse();
        Assertions.assertEquals(courses.size(), 1);
        Course course = courses.get(0);
        Assertions.assertEquals(course.getQuizForms().size(), 1);

        // get course and quiz form id
        String courseId = course.getId().toString();
        String formId = course.getQuizForms().get(0).getId().toString();

        // create user and sync the course-user relation
        MockUser prof = Helper.createMockUser("Prof");
        given().header("Authorization", "Bearer " + prof.getJwt()).get("/course").then().statusCode(200);
        MockUser prof2 = Helper.createMockUser("Prof2");
        given().header("Authorization", "Bearer " + prof2.getJwt()).get("/course").then().statusCode(200);

        // create a websocket client
        try {
            SocketClient client = new SocketClient();
            SocketClient client2 = new SocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + prof.getId() + "/" + prof.getJwt())
            );
            Thread.sleep(100);
            Assertions.assertTrue(session.isOpen());
            Session session2 = ContainerProvider.getWebSocketContainer().connectToServer(
                client2,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + prof2.getId() + "/" + prof2.getJwt())
            );
            Thread.sleep(100);
            Assertions.assertFalse(session2.isOpen());
            client.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "STARTED"
                }
            """);
            Thread.sleep(100);
            client2.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "FINISHED"
                }
            """);
            Thread.sleep(100);
            session.close();
            session2.close();

            // check if the form was started & status has not changed
            Assertions.assertTrue(courseService.getCourse(courseId).getQuizForms().get(0).getStatus().toString().equals("STARTED"));
        } catch (Exception e) {
            System.out.println(e);
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    public void stopQuizFormStudent() {
        // create & get courses
        List<Course> courses = Helper.createCourse();
        Assertions.assertEquals(courses.size(), 1);
        Course course = courses.get(0);
        Assertions.assertEquals(course.getQuizForms().size(), 1);

        // get course and quiz form id
        String courseId = course.getId().toString();
        String formId = course.getQuizForms().get(0).getId().toString();

        // create user and sync the course-user relation
        MockUser prof = Helper.createMockUser("Prof");
        given().header("Authorization", "Bearer " + prof.getJwt()).get("/course").then().statusCode(200);
        MockUser student = Helper.createMockUser("Student");
        given().header("Authorization", "Bearer " + student.getJwt()).get("/course").then().statusCode(200);

        // participate in the quiz form
        given()
            .header("Authorization", "Bearer " + student.getJwt())
            .pathParam("courseId", courseId)
            .pathParam("formId", formId)
            .body("alias-student")
            .when()
            .post("/course/{courseId}/quiz/form/{formId}/participate")
            .then()
            .statusCode(200);

        // create a websocket client
        try {
            SocketClient client = new SocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + prof.getId() + "/" + prof.getJwt())
            );
            Thread.sleep(100);
            Assertions.assertTrue(session.isOpen());
            SocketClient client2 = new SocketClient();
            Session session2 = ContainerProvider.getWebSocketContainer().connectToServer(
                client2,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + student.getId() + "/" + student.getJwt())
            );
            Thread.sleep(100);
            Assertions.assertTrue(session2.isOpen());
            client.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "STARTED"
                }
            """);
            Thread.sleep(100);
            client2.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "FINISHED"
                }
            """);
            Thread.sleep(100);
            session.close();
            session2.close();

            // check if the form was started & status has not changed
            Assertions.assertTrue(courseService.getCourse(courseId).getQuizForms().get(0).getStatus().toString().equals("STARTED"));
        } catch (Exception e) {
            System.out.println(e);
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    public void clearResults() {
        // create & get courses
        List<Course> courses = Helper.createCourse();
        Assertions.assertEquals(courses.size(), 1);
        Course course = courses.get(0);
        Assertions.assertEquals(course.getQuizForms().size(), 1);

        // get course and quiz form id
        String courseId = course.getId().toString();
        String formId = course.getQuizForms().get(0).getId().toString();
        String questionId = courses.getFirst().quizForms.get(0).questions.get(0).getId().toString();

        // create user and sync the course-user relation
        MockUser prof = Helper.createMockUser("Prof");
        given().header("Authorization", "Bearer " + prof.getJwt()).get("/course").then().statusCode(200);

        // create a websocket client
        try {
            SocketClient client = new SocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + prof.getId() + "/" + prof.getJwt())
            );
            Thread.sleep(100);
            Assertions.assertTrue(session.isOpen());
            client.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "STARTED"
                }
            """);
            Thread.sleep(100);
            // adds result to quiz form
            client.sendMessage(String.format("""
                {
                    "action": "ADD_RESULT",
                    "resultElementId": %s,
                    "resultValues": ["1"]
                }
            """, questionId));
            Thread.sleep(100);
            Response response = given()
                                    .header("Authorization", "Bearer " + prof.getJwt())
                                    .pathParam("courseId", courseId)
                                    .pathParam("formId", formId)
                                    .queryParam("results", true)
                                    .when()
                                    .get("/course/{courseId}/quiz/form/{formId}");
            QuizForm quizForm = response
                                    .then()
                                    .statusCode(200)
                                    .extract()
                                    .body()
                                    .jsonPath()
                                    .getObject(".", QuizForm.class);
            Assertions.assertEquals(1, quizForm.questions.get(0).results.size());
            Assertions.assertEquals("1", quizForm.questions.get(0).results.get(0).values.get(0));	
            client.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "FINISHED"
                }
            """);
            Thread.sleep(100);
            client.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "NOT_STARTED"
                }
            """);
            Thread.sleep(100);
            session.close();
            Response responseCleared = given()
                                            .header("Authorization", "Bearer " + prof.getJwt())
                                            .pathParam("courseId", courseId)
                                            .pathParam("formId", formId)
                                            .queryParam("results", true)
                                            .when()
                                            .get("/course/{courseId}/quiz/form/{formId}");
            quizForm = responseCleared.then()
                            .statusCode(200)
                            .extract()
                            .body()
                            .jsonPath()
                            .getObject(".", QuizForm.class);
            // form should be cleared after status is set to NOT_STARTED
            Assertions.assertEquals(0, quizForm.questions.get(0).results.size());	
        } catch (Exception e) {
            System.out.println(e);
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    public void nextQuestion() {
        // create & get courses
        List<Course> courses = Helper.createCourse();
        Assertions.assertEquals(courses.size(), 1);
        Course course = courses.get(0);
        Assertions.assertEquals(course.getQuizForms().size(), 1);

        // get course and quiz form id
        String courseId = course.getId().toString();
        String formId = course.getQuizForms().get(0).getId().toString();

        // create user and sync the course-user relation
        MockUser prof = Helper.createMockUser("Prof");
        given().header("Authorization", "Bearer " + prof.getJwt()).get("/course").then().statusCode(200);

        // create a websocket client
        try {
            SocketClient client = new SocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + prof.getId() + "/" + prof.getJwt())
            );
            Thread.sleep(100);
            Assertions.assertTrue(session.isOpen());
            client.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "STARTED"
                }
            """);
            Thread.sleep(500);
            client.sendMessage("""
                {
                    "action": "NEXT"
                }
            """); 
            Thread.sleep(500);
            // newest messagqueue item after first next should be CLOSED_QUESTION
            String messageString = client.getMessageQueue().get(1);
            LiveQuizSocketMessage message = LiveQuizSocketMessage.getByJsonWithForm(messageString);
            Assertions.assertEquals("CLOSED_QUESTION", message.action);
            Assertions.assertEquals(true, message.form.currentQuestionFinished);
            Assertions.assertEquals(0, message.form.currentQuestionIndex);

            client.sendMessage("""
                {
                    "action": "NEXT"
                }
            """);
            Thread.sleep(500);
            // newest messagqueue item after second next should be OPENED_NEXT_QUESTION
            messageString = client.getMessageQueue().get(2);
            message = LiveQuizSocketMessage.getByJsonWithForm(messageString);
            Assertions.assertEquals("OPENED_NEXT_QUESTION", message.action);
            client.sendMessage("""
                {
                    "action": "NEXT"
                }
            """);
            Thread.sleep(500);
            // newest messagqueue item after third next should be CLOSED_QUESTION
            messageString = client.getMessageQueue().get(3);
            message = LiveQuizSocketMessage.getByJsonWithForm(messageString);
            Assertions.assertEquals("CLOSED_QUESTION", message.action);
            Assertions.assertEquals(true, message.form.currentQuestionFinished);
            Assertions.assertEquals(1, message.form.currentQuestionIndex);
            client.sendMessage("""
                {
                    "action": "NEXT"
                }
            """);
            Thread.sleep(500);
            // newest messagqueue items after fourth next should be CLOSED_QUESTION & FINISHED
            messageString = client.getMessageQueue().get(4);
            message = LiveQuizSocketMessage.getByJsonWithForm(messageString);
            Assertions.assertEquals("CLOSED_QUESTION", message.action);
            messageString = client.getMessageQueue().get(5);
            message = LiveQuizSocketMessage.getByJsonWithForm(messageString);
            Assertions.assertEquals("FORM_STATUS_CHANGED", message.action);
            Assertions.assertEquals("FINISHED", message.formStatus);

            Thread.sleep(100);
            session.close();

            // check if the form status has changed
            Assertions.assertTrue(courseService.getCourse(courseId).getQuizForms().get(0).getStatus().toString().equals("FINISHED"));
        } catch (Exception e) {
            System.out.println(e);
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    public void nextQuestionNotOnwer() {
        // create & get courses
        List<Course> courses = Helper.createCourse();
        Assertions.assertEquals(courses.size(), 1);
        Course course = courses.get(0);
        Assertions.assertEquals(course.getQuizForms().size(), 1);

        // get course and quiz form id
        String courseId = course.getId().toString();
        String formId = course.getQuizForms().get(0).getId().toString();

        // create user and sync the course-user relation
        MockUser prof = Helper.createMockUser("Prof");
        given().header("Authorization", "Bearer " + prof.getJwt()).get("/course").then().statusCode(200);
        MockUser prof2 = Helper.createMockUser("Prof2");
        given().header("Authorization", "Bearer " + prof2.getJwt()).get("/course").then().statusCode(200);

        // create a websocket client
        try {
            // Owner stats quiz & 2nd prof (not owner) tries to change question
            SocketClient client = new SocketClient();
            SocketClient client2 = new SocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + prof.getId() + "/" + prof.getJwt())
            );      
            Thread.sleep(100);
            Assertions.assertTrue(session.isOpen());      
            Session session2 = ContainerProvider.getWebSocketContainer().connectToServer(
                client2,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + prof2.getId() + "/" + prof2.getJwt())
            );
            Thread.sleep(100);
            Assertions.assertFalse(session2.isOpen());
            client.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "STARTED"
                }
            """);
            Thread.sleep(500);
            client2.sendMessage("""
                {
                    "action": "NEXT"
                }
            """); 
            Thread.sleep(500);
            Assertions.assertEquals(0 , client2.getMessageQueue().size());
            session.close();    
            session2.close();    

            // check that the form status has not changed
            Assertions.assertTrue(courseService.getCourse(courseId).getQuizForms().get(0).getStatus().toString().equals("STARTED"));
        } catch (Exception e) {
            System.out.println(e);
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    public void nextQuestionStudent() {
        // create & get courses
        List<Course> courses = Helper.createCourse();
        Assertions.assertEquals(courses.size(), 1);
        Course course = courses.get(0);
        Assertions.assertEquals(course.getQuizForms().size(), 1);

        // get course and quiz form id
        String courseId = course.getId().toString();
        String formId = course.getQuizForms().get(0).getId().toString();

        // create user and sync the course-user relation
        MockUser prof = Helper.createMockUser("Prof");
        given().header("Authorization", "Bearer " + prof.getJwt()).get("/course").then().statusCode(200);
        MockUser student = Helper.createMockUser("Student");
        given().header("Authorization", "Bearer " + student.getJwt()).get("/course").then().statusCode(200);

        // participate in the quiz form
        given()
            .header("Authorization", "Bearer " + student.getJwt())
            .pathParam("courseId", courseId)
            .pathParam("formId", formId)
            .body("alias-student")
            .when()
            .post("/course/{courseId}/quiz/form/{formId}/participate")
            .then()
            .statusCode(200);

        // create a websocket client
        try {
            // Owner stats quiz & 2nd prof (with student role) tries to change question
            SocketClient client = new SocketClient();
            SocketClient client2 = new SocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + prof.getId() + "/" + prof.getJwt())
            );
            Thread.sleep(100);
            Assertions.assertTrue(session.isOpen());       
            Session session2 = ContainerProvider.getWebSocketContainer().connectToServer(
                client2,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + student.getId() + "/" + student.getJwt())
            );
            Thread.sleep(100);
            Assertions.assertTrue(session2.isOpen());
            client.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "STARTED"
                }
            """);
            Thread.sleep(500);
            client2.sendMessage("""
                {
                    "action": "NEXT"
                }
            """); 
            Thread.sleep(500);
            Assertions.assertEquals(1, client2.getMessageQueue().size());
            session.close();    
            session2.close();    

            // check that the form status has not changed
            Assertions.assertTrue(courseService.getCourse(courseId).getQuizForms().get(0).getStatus().toString().equals("STARTED"));
        } catch (Exception e) {
            System.out.println(e);
            Assertions.fail(e.getMessage());
        }
    }

    private void addResult(String courseId, String formId, String questionId) {
        // create a websocket client

        // create user and sync the course-user relation
        MockUser prof = Helper.createMockUser("Prof");
        given().header("Authorization", "Bearer " + prof.getJwt()).get("/course").then().statusCode(200);

        try {
            SocketClient client = new SocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + prof.getId() + "/" + prof.getJwt())
            );
            Thread.sleep(100);
            Assertions.assertTrue(session.isOpen());
            // starts quiz session
            client.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "STARTED"
                }
            """);
            // adds result to quiz form
            client.sendMessage(String.format("""
                {
                    "action": "ADD_RESULT",
                    "resultElementId": %s,
                    "resultValues": ["1"]
                }
            """, questionId));
            Thread.sleep(100);
            session.close();

            // check if the form status has changed
            Assertions.assertTrue(courseService.getCourse(courseId).getQuizForms().get(0).getStatus().toString().equals("STARTED"));
        } catch (Exception e) {
            System.out.println(e);
            Assertions.fail(e.getMessage());
        }
    }


    // test the participate function in combination with the new "WAITING" status
    // a student should be able to participate in a quiz form
    // the prof should get a message every time a student joins the quiz form ("PARTICIPANT_JOINED")
    @Test
    public void participateInQuizForm() {

        // create & get courses
        List<Course> courses = Helper.createCourse("Prof-1");
        Course course = courses.get(0);

        // make 1 prof and 3 students
        MockUser prof = Helper.createMockUser("Prof-1");
        MockUser student1 = Helper.createMockUser("Student-1");
        MockUser student2 = Helper.createMockUser("Student-2");
        MockUser student3 = Helper.createMockUser("Student-3");

        // call the get courses endpoint for each user to update the course-user relation
        given().header("Authorization", "Bearer " + prof.getJwt()).when().get("/course").then().statusCode(200);
        given().header("Authorization", "Bearer " + student1.getJwt()).when().get("/course").then().statusCode(200);
        given().header("Authorization", "Bearer " + student2.getJwt()).when().get("/course").then().statusCode(200);
        given().header("Authorization", "Bearer " + student3.getJwt()).when().get("/course").then().statusCode(200);

        // get course and quiz form id
        String courseId = course.getId().toString();
        String formId = course.getQuizForms().get(0).getId().toString();

        // try catch block to handle exceptions of websocket connection
        try {

            // create websocket clients
            SocketClient profClient = new SocketClient();
            SocketClient studentClient = new SocketClient();

            // connect the prof to the quiz form
            Session profSession = ContainerProvider.getWebSocketContainer().connectToServer(
                profClient,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + prof.getId() + "/" + prof.getJwt())
            );
            Thread.sleep(100);
            Assertions.assertTrue(profSession.isOpen());

            // set the form status to "WAITING" and check if it was set
            profClient.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "WAITING"
                }
            """);
            Thread.sleep(100);
            Assertions.assertEquals("WAITING", courseService.getCourse(courseId).getQuizForms().get(0).getStatus().toString());

            // make the participate requests for the students and connect them to the quiz form
            Response response1 = given()
                                    .header("Authorization", "Bearer " + student1.getJwt())
                                    .pathParam("courseId", courseId)
                                    .pathParam("formId", formId)
                                    .body("alias-student-1")
                                    .when()
                                    .post("/course/{courseId}/quiz/form/{formId}/participate");
            Thread.sleep(100);
            Assertions.assertEquals(200, response1.getStatusCode());
            Session studentSession1 = ContainerProvider.getWebSocketContainer().connectToServer(
                studentClient,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + student1.getId() + "/" + student1.getJwt())
            );
            Thread.sleep(100);
            Assertions.assertTrue(studentSession1.isOpen());

            Response response2 = given()
                                    .header("Authorization", "Bearer " + student2.getJwt())
                                    .pathParam("courseId", courseId)
                                    .pathParam("formId", formId)
                                    .body("alias-student-2")
                                    .when()
                                    .post("/course/{courseId}/quiz/form/{formId}/participate"); 
            Thread.sleep(100);
            Assertions.assertEquals(200, response2.getStatusCode());
            Session studentSession2 = ContainerProvider.getWebSocketContainer().connectToServer(
                studentClient,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + student2.getId() + "/" + student2.getJwt())
            );
            Thread.sleep(100);
            Assertions.assertTrue(studentSession2.isOpen());

            Response response3 = given()
                                    .header("Authorization", "Bearer " + student3.getJwt())
                                    .pathParam("courseId", courseId)
                                    .pathParam("formId", formId)
                                    .body("alias-student-3")
                                    .when()
                                    .post("/course/{courseId}/quiz/form/{formId}/participate");
            Thread.sleep(100);
            Assertions.assertEquals(200, response3.getStatusCode());
            Session studentSession3 = ContainerProvider.getWebSocketContainer().connectToServer(
                studentClient,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + student3.getId() + "/" + student3.getJwt())
            );
            Thread.sleep(100);
            Assertions.assertTrue(studentSession3.isOpen());

            // check if the quiz has now participants
            Assertions.assertEquals(3, courseService.getCourse(courseId).getQuizForms().get(0).getParticipants().size());

            // check if the prof received the "PARTICIPANT_JOINED" messages and got the right amount of participants
            Assertions.assertEquals("PARTICIPANT_JOINED", LiveQuizSocketMessage.getByJsonWithForm(profClient.getMessageQueue().get(profClient.getMessageQueue().size() - 1)).action);
            Assertions.assertEquals(3, LiveQuizSocketMessage.getByJsonWithForm(profClient.getMessageQueue().get(profClient.getMessageQueue().size() - 1)).form.participants.size());
            Assertions.assertEquals("PARTICIPANT_JOINED", LiveQuizSocketMessage.getByJsonWithForm(profClient.getMessageQueue().get(profClient.getMessageQueue().size() - 2)).action);
            Assertions.assertEquals(2, LiveQuizSocketMessage.getByJsonWithForm(profClient.getMessageQueue().get(profClient.getMessageQueue().size() - 2)).form.participants.size());
            Assertions.assertEquals("PARTICIPANT_JOINED", LiveQuizSocketMessage.getByJsonWithForm(profClient.getMessageQueue().get(profClient.getMessageQueue().size() - 3)).action);
            Assertions.assertEquals(1, LiveQuizSocketMessage.getByJsonWithForm(profClient.getMessageQueue().get(profClient.getMessageQueue().size() - 3)).form.participants.size());
            Assertions.assertEquals("FORM_STATUS_CHANGED", LiveQuizSocketMessage.getByJsonWithForm(profClient.getMessageQueue().get(profClient.getMessageQueue().size() - 4)).action);
            Assertions.assertEquals(0, LiveQuizSocketMessage.getByJsonWithForm(profClient.getMessageQueue().get(profClient.getMessageQueue().size() - 4)).form.participants.size());
            
            // change the form status to "STARTED" and check if it was set
            profClient.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "STARTED"
                }
            """);
            Thread.sleep(100);
            Assertions.assertEquals("STARTED", courseService.getCourse(courseId).getQuizForms().get(0).getStatus().toString());

            // check if the students received the "FORM_STATUS_CHANGED" message
            Assertions.assertEquals("FORM_STATUS_CHANGED", LiveQuizSocketMessage.getByJsonWithForm(studentClient.getMessageQueue().get(studentClient.getMessageQueue().size() - 1)).action);
            Assertions.assertEquals("FORM_STATUS_CHANGED", LiveQuizSocketMessage.getByJsonWithForm(studentClient.getMessageQueue().get(studentClient.getMessageQueue().size() - 1)).action);
            Assertions.assertEquals("FORM_STATUS_CHANGED", LiveQuizSocketMessage.getByJsonWithForm(studentClient.getMessageQueue().get(studentClient.getMessageQueue().size() - 1)).action);

            // close the websocket connections
            profSession.close();
            studentSession1.close();
            studentSession2.close();
            studentSession3.close();


        } catch (Exception e) {
            System.out.println(e);
            Assertions.fail(e.getMessage());
        }
    }


    @Test
    public void testScoreAndResult() throws DeploymentException, IOException, InterruptedException {

        // create & get courses
        List<Course> courses = Helper.createCourse("Prof-1");
        Course course = courses.get(0);

        // make 1 prof and 3 students
        MockUser prof = Helper.createMockUser("Prof-1");
        MockUser student1 = Helper.createMockUser("Student-1");
        MockUser student2 = Helper.createMockUser("Student-2");
        MockUser student3 = Helper.createMockUser("Student-3");

        // call the get courses endpoint for each user to update the course-user relation
        given().header("Authorization", "Bearer " + prof.getJwt()).when().get("/course").then().statusCode(200);
        given().header("Authorization", "Bearer " + student1.getJwt()).when().get("/course").then().statusCode(200);
        given().header("Authorization", "Bearer " + student2.getJwt()).when().get("/course").then().statusCode(200);
        given().header("Authorization", "Bearer " + student3.getJwt()).when().get("/course").then().statusCode(200);
        
        // get course and quiz form id
        String courseId = course.getId().toString();
        String formId = course.getQuizForms().get(0).getId().toString();

        // create websocket clients
        SocketClient profClient = new SocketClient();
        SocketClient studentClient1 = new SocketClient();
        SocketClient studentClient2 = new SocketClient();
        SocketClient studentClient3 = new SocketClient();

        // connect the prof to the quiz form
        Session profSession = ContainerProvider.getWebSocketContainer().connectToServer(
            profClient,
            URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + prof.getId() + "/" + prof.getJwt())
        );
        Thread.sleep(100);
        Assertions.assertTrue(profSession.isOpen());

        // set the form status to "WAITING" and check if it was set
        profClient.sendMessage("""
            {
                "action": "CHANGE_FORM_STATUS",
                "formStatus": "WAITING"
            }
        """);
        Thread.sleep(100);
        Assertions.assertEquals("WAITING", courseService.getCourse(courseId).getQuizForms().get(0).getStatus().toString());

        // make the participate requests for the students and connect them to the quiz form
        Response response1 = given()
                                .header("Authorization", "Bearer " + student1.getJwt())
                                .pathParam("courseId", courseId)
                                .pathParam("formId", formId)
                                .body("alias-student-1")
                                .when()
                                .post("/course/{courseId}/quiz/form/{formId}/participate");
        Thread.sleep(100);
        Assertions.assertEquals(200, response1.getStatusCode());
        Session studentSession1 = ContainerProvider.getWebSocketContainer().connectToServer(
            studentClient1,
            URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + student1.getId() + "/" + student1.getJwt())
        );
        Thread.sleep(100);
        Assertions.assertTrue(studentSession1.isOpen());

        Response response2 = given()
                                .header("Authorization", "Bearer " + student2.getJwt())
                                .pathParam("courseId", courseId)
                                .pathParam("formId", formId)
                                .body("alias-student-2")
                                .when()
                                .post("/course/{courseId}/quiz/form/{formId}/participate");
        Thread.sleep(100);
        Assertions.assertEquals(200, response2.getStatusCode());
        Session studentSession2 = ContainerProvider.getWebSocketContainer().connectToServer(
            studentClient2,
            URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + student2.getId() + "/" + student2.getJwt())
        );
        Thread.sleep(100);
        Assertions.assertTrue(studentSession2.isOpen());

        Response response3 = given()
                                .header("Authorization", "Bearer " + student3.getJwt())
                                .pathParam("courseId", courseId)
                                .pathParam("formId", formId)
                                .body("alias-student-3")
                                .when()
                                .post("/course/{courseId}/quiz/form/{formId}/participate");
        Thread.sleep(100);
        Assertions.assertEquals(200, response3.getStatusCode());
        Session studentSession3 = ContainerProvider.getWebSocketContainer().connectToServer(
            studentClient3,
            URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + student3.getId() + "/" + student3.getJwt())
        );
        Thread.sleep(100);
        Assertions.assertTrue(studentSession3.isOpen());

        // check if the quiz has now participants
        Assertions.assertEquals(3, courseService.getCourse(courseId).getQuizForms().get(0).getParticipants().size());

        // check if the prof received the "PARTICIPANT_JOINED" messages and got the right amount of participants
        Assertions.assertEquals("PARTICIPANT_JOINED", LiveQuizSocketMessage.getByJsonWithForm(profClient.getMessageQueue().get(profClient.getMessageQueue().size() - 1)).action);
        Assertions.assertEquals(3, LiveQuizSocketMessage.getByJsonWithForm(profClient.getMessageQueue().get(profClient.getMessageQueue().size() - 1)).form.participants.size());
        Assertions.assertEquals("PARTICIPANT_JOINED", LiveQuizSocketMessage.getByJsonWithForm(profClient.getMessageQueue().get(profClient.getMessageQueue().size() - 2)).action);
        Assertions.assertEquals(2, LiveQuizSocketMessage.getByJsonWithForm(profClient.getMessageQueue().get(profClient.getMessageQueue().size() - 2)).form.participants.size());
        Assertions.assertEquals("PARTICIPANT_JOINED", LiveQuizSocketMessage.getByJsonWithForm(profClient.getMessageQueue().get(profClient.getMessageQueue().size() - 3)).action);
        Assertions.assertEquals(1, LiveQuizSocketMessage.getByJsonWithForm(profClient.getMessageQueue().get(profClient.getMessageQueue().size() - 3)).form.participants.size());
        Assertions.assertEquals("FORM_STATUS_CHANGED", LiveQuizSocketMessage.getByJsonWithForm(profClient.getMessageQueue().get(profClient.getMessageQueue().size() - 4)).action);
        Assertions.assertEquals(0, LiveQuizSocketMessage.getByJsonWithForm(profClient.getMessageQueue().get(profClient.getMessageQueue().size() - 4)).form.participants.size());
        
        // change the form status to "STARTED" and check if it was set
        profClient.sendMessage("""
            {
                "action": "CHANGE_FORM_STATUS",
                "formStatus": "STARTED"
            }
        """);
        Thread.sleep(100);
        Assertions.assertEquals("STARTED", courseService.getCourse(courseId).getQuizForms().get(0).getStatus().toString());

        // check if the student received the "FORM_STATUS_CHANGED" message
        Assertions.assertEquals("FORM_STATUS_CHANGED", LiveQuizSocketMessage.getByJsonWithForm(studentClient1.getMessageQueue().get(studentClient1.getMessageQueue().size() - 1)).action);

        // add a correct result to the quiz form and then a wrong one
        studentClient1.sendMessage("""
            {
                "action": "ADD_RESULT",
                "resultElementId": %s,
                "resultValues": ["2"]
            }
        """.formatted(course.getQuizForms().get(0).questions.get(0).getId().toString()));
        Thread.sleep(100);
        studentClient2.sendMessage("""
            {
                "action": "ADD_RESULT",
                "resultElementId": %s,
                "resultValues": ["2"]
            }
        """.formatted(course.getQuizForms().get(0).questions.get(0).getId().toString()));
        Thread.sleep(100);
        studentClient3.sendMessage("""
            {
                "action": "ADD_RESULT",
                "resultElementId": %s,
                "resultValues": ["2"]
            }
        """.formatted(course.getQuizForms().get(0).questions.get(0).getId().toString()));
        Thread.sleep(100);

        // check that the result was added
        Assertions.assertEquals(3, courseService.getCourse(courseId).getQuizForms().get(0).questions.get(0).results.size());
        Assertions.assertEquals("2", courseService.getCourse(courseId).getQuizForms().get(0).questions.get(0).results.get(0).values.get(0));
        Assertions.assertEquals("2", courseService.getCourse(courseId).getQuizForms().get(0).questions.get(0).results.get(1).values.get(0));
        Assertions.assertEquals("2", courseService.getCourse(courseId).getQuizForms().get(0).questions.get(0).results.get(2).values.get(0));


        // check that the score was updated
        Assertions.assertEquals(15, courseService.getCourse(courseId).getQuizForms().get(0).getParticipants().get(0).getScore());
        Assertions.assertEquals(14, courseService.getCourse(courseId).getQuizForms().get(0).getParticipants().get(1).getScore());
        Assertions.assertEquals(13, courseService.getCourse(courseId).getQuizForms().get(0).getParticipants().get(2).getScore());

        // let the prof stop the question
        profClient.sendMessage("""
            {
                "action": "NEXT"
            }
        """);
        Thread.sleep(100);

        // check that the currentQuestionIndex is 0 and the currentQuestionFinished is true
        Assertions.assertEquals(0, courseService.getCourse(courseId).getQuizForms().get(0).getCurrentQuestionIndex());
        Assertions.assertTrue(courseService.getCourse(courseId).getQuizForms().get(0).getCurrentQuestionFinished());

        // check that the user got the "CLOSED_QUESTION" message and that it contains the correct result
        Assertions.assertEquals("CLOSED_QUESTION", LiveQuizSocketMessage.getByJsonWithForm(studentClient1.getMessageQueue().get(studentClient1.getMessageQueue().size() - 1)).action);
        Assertions.assertTrue(LiveQuizSocketMessage.getByJsonWithForm(studentClient1.getMessageQueue().get(studentClient1.getMessageQueue().size() - 1)).userHasAnsweredCorrectly);
        Assertions.assertEquals("CLOSED_QUESTION", LiveQuizSocketMessage.getByJsonWithForm(studentClient2.getMessageQueue().get(studentClient2.getMessageQueue().size() - 1)).action);
        Assertions.assertTrue(LiveQuizSocketMessage.getByJsonWithForm(studentClient2.getMessageQueue().get(studentClient2.getMessageQueue().size() - 1)).userHasAnsweredCorrectly);
        Assertions.assertEquals("CLOSED_QUESTION", LiveQuizSocketMessage.getByJsonWithForm(studentClient3.getMessageQueue().get(studentClient3.getMessageQueue().size() - 1)).action);
        Assertions.assertTrue(LiveQuizSocketMessage.getByJsonWithForm(studentClient3.getMessageQueue().get(studentClient3.getMessageQueue().size() - 1)).userHasAnsweredCorrectly);

        // let the prof start the next question
        profClient.sendMessage("""
            {
                "action": "NEXT"
            }
        """);
        Thread.sleep(100);

        // add a wrong result to the quiz form
        studentClient1.sendMessage("""
            {
                "action": "ADD_RESULT",
                "resultElementId": %s,
                "resultValues": ["1"]
            }
        """.formatted(course.getQuizForms().get(0).questions.get(1).getId().toString()));
        Thread.sleep(100);

        // check that the result was added
        Assertions.assertEquals(1, courseService.getCourse(courseId).getQuizForms().get(0).questions.get(1).results.size());
        Assertions.assertEquals("1", courseService.getCourse(courseId).getQuizForms().get(0).questions.get(1).results.get(0).values.get(0));
        
        // check that the score was updated
        Assertions.assertEquals(15, courseService.getCourse(courseId).getQuizForms().get(0).getParticipants().get(0).getScore());

        // let the prof stop the question
        profClient.sendMessage("""
            {
                "action": "NEXT"
            }
        """);
        Thread.sleep(100);

        // check that the user got the opened and closed question message and that it contains the correct result
        Assertions.assertEquals("OPENED_NEXT_QUESTION", LiveQuizSocketMessage.getByJsonWithForm(studentClient1.getMessageQueue().get(studentClient1.getMessageQueue().size() - 2)).action);
        Assertions.assertEquals("CLOSED_QUESTION", LiveQuizSocketMessage.getByJsonWithForm(studentClient1.getMessageQueue().get(studentClient1.getMessageQueue().size() - 1)).action);
        Assertions.assertFalse(LiveQuizSocketMessage.getByJsonWithForm(studentClient1.getMessageQueue().get(studentClient1.getMessageQueue().size() - 1)).userHasAnsweredCorrectly);

        // check that the correctAnswers are set
        Assertions.assertEquals(List.of("2"), LiveQuizSocketMessage.getByJsonWithForm(studentClient1.getMessageQueue().get(studentClient1.getMessageQueue().size() - 1)).correctAnswers);

        // close the websocket connections
        profSession.close();
        studentSession1.close();
    }

    @Test
    public void testAlreadySubmitted() throws DeploymentException, IOException, InterruptedException {

        // create & get courses
        List<Course> courses = Helper.createCourse("Prof-1");
        Course course = courses.get(0);

        // make 1 prof and 1 student
        MockUser prof = Helper.createMockUser("Prof-1");
        MockUser student1 = Helper.createMockUser("Student-1");

        // call the get courses endpoint for each user to update the course-user relation
        given().header("Authorization", "Bearer " + prof.getJwt()).when().get("/course").then().statusCode(200);
        given().header("Authorization", "Bearer " + student1.getJwt()).when().get("/course").then().statusCode(200);
        
        // get course and quiz form id
        String courseId = course.getId().toString();
        String formId = course.getQuizForms().get(0).getId().toString();

        // create websocket clients
        SocketClient profClient = new SocketClient();
        SocketClient studentClient = new SocketClient();

        // connect the prof to the quiz form
        Session profSession = ContainerProvider.getWebSocketContainer().connectToServer(
            profClient,
            URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + prof.getId() + "/" + prof.getJwt())
        );
        Thread.sleep(100);
        Assertions.assertTrue(profSession.isOpen());

        // set the form status to "WAITING" and check if it was set
        profClient.sendMessage("""
            {
                "action": "CHANGE_FORM_STATUS",
                "formStatus": "WAITING"
            }
        """);
        Thread.sleep(100);
        Assertions.assertEquals("WAITING", courseService.getCourse(courseId).getQuizForms().get(0).getStatus().toString());

        // make the participate requests for the students and connect them to the quiz form
        Response response1 = given()
                                .header("Authorization", "Bearer " + student1.getJwt())
                                .pathParam("courseId", courseId)
                                .pathParam("formId", formId)
                                .body("alias-student-1")
                                .when()
                                .post("/course/{courseId}/quiz/form/{formId}/participate");
        Thread.sleep(100);
        Assertions.assertEquals(200, response1.getStatusCode());
        Session studentSession1 = ContainerProvider.getWebSocketContainer().connectToServer(
            studentClient,
            URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + student1.getId() + "/" + student1.getJwt())
        );
        Thread.sleep(100);
        Assertions.assertTrue(studentSession1.isOpen());

        // check if the quiz has now participants
        Assertions.assertEquals(1, courseService.getCourse(courseId).getQuizForms().get(0).getParticipants().size());

        // check if the prof received the "PARTICIPANT_JOINED" messages and got the right amount of participants
        Assertions.assertEquals("PARTICIPANT_JOINED", LiveQuizSocketMessage.getByJsonWithForm(profClient.getMessageQueue().get(profClient.getMessageQueue().size() - 1)).action);
        Assertions.assertEquals(1, LiveQuizSocketMessage.getByJsonWithForm(profClient.getMessageQueue().get(profClient.getMessageQueue().size() - 1)).form.participants.size());
        Assertions.assertEquals("FORM_STATUS_CHANGED", LiveQuizSocketMessage.getByJsonWithForm(profClient.getMessageQueue().get(profClient.getMessageQueue().size() - 2)).action);
        Assertions.assertEquals(0, LiveQuizSocketMessage.getByJsonWithForm(profClient.getMessageQueue().get(profClient.getMessageQueue().size() - 2)).form.participants.size());
        
        // change the form status to "STARTED" and check if it was set
        profClient.sendMessage("""
            {
                "action": "CHANGE_FORM_STATUS",
                "formStatus": "STARTED"
            }
        """);
        Thread.sleep(100);
        Assertions.assertEquals("STARTED", courseService.getCourse(courseId).getQuizForms().get(0).getStatus().toString());

        // check if the student received the "FORM_STATUS_CHANGED" message
        Assertions.assertEquals("FORM_STATUS_CHANGED", LiveQuizSocketMessage.getByJsonWithForm(studentClient.getMessageQueue().get(studentClient.getMessageQueue().size() - 1)).action);

        // add a correct result to the quiz form and then a wrong one
        studentClient.sendMessage("""
            {
                "action": "ADD_RESULT",
                "resultElementId": %s,
                "resultValues": ["2"]
            }
        """.formatted(course.getQuizForms().get(0).questions.get(0).getId().toString()));
        Thread.sleep(100);

        // check that the result was added
        Assertions.assertEquals(1, courseService.getCourse(courseId).getQuizForms().get(0).questions.get(0).results.size());
        Assertions.assertEquals("2", courseService.getCourse(courseId).getQuizForms().get(0).questions.get(0).results.get(0).values.get(0));

        // check that the score was updated
        Assertions.assertEquals(15, courseService.getCourse(courseId).getQuizForms().get(0).getParticipants().get(0).getScore());

        // disconnect the student and reconnect it
        studentSession1.close();

        Session studentSession1_2 = ContainerProvider.getWebSocketContainer().connectToServer(
            studentClient,
            URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + student1.getId() + "/" + student1.getJwt())
        );
        Thread.sleep(100);

        // check that the user got the "ALREADY_SUBMITTED" message
        Assertions.assertEquals("ALREADY_SUBMITTED", LiveQuizSocketMessage.getByJsonWithForm(studentClient.getMessageQueue().get(studentClient.getMessageQueue().size() - 1)).action);
        Assertions.assertEquals("2", LiveQuizSocketMessage.getByJsonWithForm(studentClient.getMessageQueue().get(studentClient.getMessageQueue().size() - 1)).userAnswers.get(0));

        // close the websocket connections
        profSession.close();
        studentSession1_2.close();

    }

    @Test
    public void testResultDownload() {

        // create & get courses
        // List<Course> courses = Helper.createCourse("Prof-1");

        // make 1 prof and 3 students
        MockUser prof = Helper.createMockUser("Prof-1");
        MockUser student1 = Helper.createMockUser("Student-1");
        MockUser student2 = Helper.createMockUser("Student-2");
        MockUser student3 = Helper.createMockUser("Student-3");
        MockUser student4 = Helper.createMockUser("Student-4");
        
        // manually create a course to have more questions
        String json = "[\r\n  {\r\n    \"name\": \"AUME 23/24\",\r\n    \"description\": \"Agile Vorgehensmodelle und Mobile Kommunikation\",\r\n    \"feedbackForms\": [],\r\n    \"quizForms\": [\r\n      {\r\n        \"name\": \"Rollenverst\u00E4ndnis bei Scrum\",\r\n        \"description\": \"Ein Quiz zum Rollenverst\u00E4ndnis und Teamaufbau bei Scrum\",\r\n        \"questions\": [\r\n          {\r\n            \"name\": \"Product Owner\",\r\n            \"description\": \"Welche der folgenden Aufgaben ist nicht Teil der Rolle des Product Owners?\",\r\n            \"type\": \"MULTIPLE_CHOICE\",\r\n            \"options\": [\r\n              \"Erstellung des Product Backlogs\",\r\n              \"Priorisierung des Product Backlogs\",\r\n              \"Pizza bestellen f\u00FCr jedes Daily\"\r\n            ],\r\n            \"hasCorrectAnswers\": true,\r\n            \"correctAnswers\": [\"2\"],\r\n            \"key\": \"Q-Q-PRODUCTOWNER-1\"\r\n          },\r\n          {\r\n            \"name\": \"Product Owner\",\r\n            \"description\": \"Test Frage?\",\r\n            \"type\": \"MULTIPLE_CHOICE\",\r\n            \"options\": [\"Antwort 1\", \"Antwort 2\", \"Antwort 3\"],\r\n            \"hasCorrectAnswers\": true,\r\n            \"correctAnswers\": [\"2\"],\r\n            \"key\": \"Q-Q-PRODUCTOWNER-2\"\r\n          },\r\n          {\r\n            \"name\": \"Product Owner\",\r\n            \"description\": \"Test Frage?\",\r\n            \"type\": \"MULTIPLE_CHOICE\",\r\n            \"options\": [\"Antwort 1\", \"Antwort 2\", \"Antwort 3\"],\r\n            \"hasCorrectAnswers\": true,\r\n            \"correctAnswers\": [\"2\"],\r\n            \"key\": \"Q-Q-PRODUCTOWNER-3\"\r\n          },\r\n          {\r\n            \"name\": \"Product Owner\",\r\n            \"description\": \"Test Frage?\",\r\n            \"type\": \"FULLTEXT\",\r\n            \"options\": [\"\", \"\", \"\"],\r\n            \"hasCorrectAnswers\": true,\r\n            \"correctAnswers\": [\"2\"],\r\n            \"key\": \"Q-Q-PRODUCTOWNER-4\"\r\n          }\r\n        ],\r\n        \"key\": \"Q-ROLES\"\r\n      }\r\n    ],\r\n    \"key\": \"AUME23\",\r\n    \"moodleCourseId\": \"1\"\r\n  }\r\n]";
        Response response = given().accept(ContentType.JSON).contentType(ContentType.JSON).header("Authorization", "Bearer " + prof.getJwt()).body(json).patch("/public/courses/");
        List<Course> courses = response.then().statusCode(200).extract().body().jsonPath().getList(".", Course.class);
        Course course = courses.get(0);

        // call the get courses endpoint for each user to update the course-user relation
        given().header("Authorization", "Bearer " + prof.getJwt()).when().get("/course").then().statusCode(200);
        given().header("Authorization", "Bearer " + student1.getJwt()).when().get("/course").then().statusCode(200);
        given().header("Authorization", "Bearer " + student2.getJwt()).when().get("/course").then().statusCode(200);
        given().header("Authorization", "Bearer " + student3.getJwt()).when().get("/course").then().statusCode(200);
        given().header("Authorization", "Bearer " + student4.getJwt()).when().get("/course").then().statusCode(200);
        
        // get course and quiz form id
        String courseId = course.getId().toString();
        String formId = course.getQuizForms().get(0).getId().toString();

        // make the participate requests for the students and connect them to the quiz form
        given()
            .header("Authorization", "Bearer " + student1.getJwt())
            .pathParam("courseId", courseId)
            .pathParam("formId", formId)
            .body("alias-student-1")
            .when()
            .post("/course/{courseId}/quiz/form/{formId}/participate")
            .then()
            .statusCode(200);
        given()
            .header("Authorization", "Bearer " + student2.getJwt())
            .pathParam("courseId", courseId)
            .pathParam("formId", formId)
            .body("alias-student-2")
            .when()
            .post("/course/{courseId}/quiz/form/{formId}/participate")
            .then()
            .statusCode(200);
        given()
            .header("Authorization", "Bearer " + student3.getJwt())
            .pathParam("courseId", courseId)
            .pathParam("formId", formId)
            .body("alias-student-3")
            .when()
            .post("/course/{courseId}/quiz/form/{formId}/participate")
            .then()
            .statusCode(200);
        given()
            .header("Authorization", "Bearer " + student4.getJwt())
            .pathParam("courseId", courseId)
            .pathParam("formId", formId)
            .body("alias-student-4")
            .when()
            .post("/course/{courseId}/quiz/form/{formId}/participate")
            .then()
            .statusCode(200);
            
        // try catch block to handle exceptions of websocket connection
        try {
            Thread.sleep(100);

            // create websocket clients
            SocketClient profClient = new SocketClient();
            SocketClient studentClient1 = new SocketClient();
            SocketClient studentClient2 = new SocketClient();
            SocketClient studentClient3 = new SocketClient();
            SocketClient studentClient4 = new SocketClient();

            // connect the prof to the quiz form
            Session profSession = ContainerProvider.getWebSocketContainer().connectToServer(
                profClient,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + prof.getId() + "/" + prof.getJwt())
            );
            Thread.sleep(100);
            Assertions.assertTrue(profSession.isOpen());

            // set the form status to "WAITING" and check if it was set
            profClient.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "WAITING"
                }
            """);
            Thread.sleep(100);
            Assertions.assertEquals("WAITING", courseService.getCourse(courseId).getQuizForms().get(0).getStatus().toString());
            Assertions.assertEquals(4, courseService.getCourse(courseId).getQuizForms().get(0).getParticipants().size());

            // connect the students to the quiz form
            Session studentSession1 = ContainerProvider.getWebSocketContainer().connectToServer(
                studentClient1,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + student1.getId() + "/" + student1.getJwt())
            );
            Thread.sleep(100);
            Assertions.assertTrue(studentSession1.isOpen());

            Session studentSession2 = ContainerProvider.getWebSocketContainer().connectToServer(
                studentClient2,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + student2.getId() + "/" + student2.getJwt())
            );
            Thread.sleep(100);
            Assertions.assertTrue(studentSession2.isOpen());

            Session studentSession3 = ContainerProvider.getWebSocketContainer().connectToServer(
                studentClient3,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + student3.getId() + "/" + student3.getJwt())
            );
            Thread.sleep(100);
            Assertions.assertTrue(studentSession3.isOpen());

            Thread.sleep(100);
            Session studentSession4 = ContainerProvider.getWebSocketContainer().connectToServer(
                studentClient4,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + student4.getId() + "/" + student4.getJwt())
            );
            Thread.sleep(100);
            Assertions.assertTrue(studentSession4.isOpen());

            // change the form status to "STARTED" and check if it was set
            profClient.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "STARTED"
                }
            """);
            Thread.sleep(100);
            Assertions.assertEquals("STARTED", courseService.getCourse(courseId).getQuizForms().get(0).getStatus().toString());
                        
            // send results to the quiz form (1, 2, 3, 3)
            String questionId = course.getQuizForms().get(0).getQuestions().get(0).getId().toString();
            String questionId3 = course.getQuizForms().get(0).getQuestions().get(2).getId().toString();
            String questionIdFulltext = course.getQuizForms().get(0).getQuestions().get(3).getId().toString();
            studentClient1.sendMessage(String.format("""
                {
                    "action": "ADD_RESULT",
                    "resultElementId": %s,
                    "resultValues": [1]
                }
            """, questionId));
            Thread.sleep(100);
            studentClient2.sendMessage(String.format("""
                {
                    "action": "ADD_RESULT",
                    "resultElementId": %s,
                    "resultValues": [2]
                }
            """, questionId));
            Thread.sleep(100);
            studentClient3.sendMessage(String.format("""
                {
                    "action": "ADD_RESULT",
                    "resultElementId": %s,
                    "resultValues": [3]
                }
            """, questionId));
            Thread.sleep(100);
            studentClient4.sendMessage(String.format("""
                {
                    "action": "ADD_RESULT",
                    "resultElementId": %s,
                    "resultValues": [3, 5]
                }
            """, questionId));
            Thread.sleep(100);
            studentClient4.sendMessage(String.format("""
                {
                    "action": "ADD_RESULT",
                    "resultElementId": %s,
                    "resultValues": [3, 5]
                }
            """, questionId3));
            Thread.sleep(100);
            studentClient3.sendMessage(String.format("""
                {
                    "action": "ADD_RESULT",
                    "resultElementId": %s,
                    "resultValues": ["This is a fulltext response"]
                }
            """, questionIdFulltext));
            Thread.sleep(100);
            studentClient4.sendMessage(String.format("""
                {
                    "action": "ADD_RESULT",
                    "resultElementId": %s,
                    "resultValues": ["This is a 2", "part fulltext response"]
                }
            """, questionIdFulltext));
            Thread.sleep(100);

            // close the websocket connections
            profSession.close();
            studentSession1.close();
            studentSession2.close();
            studentSession3.close();

            // check manually if the results are correct
            Course updatedCourse = courseService.getCourse(courseId);
            List<Result> results = updatedCourse.getQuizForms().get(0).getQuestions().get(0).getResults();
            List<Result> results3 = updatedCourse.getQuizForms().get(0).getQuestions().get(2).getResults();
            List<Result> resultsFulltext = updatedCourse.getQuizForms().get(0).getQuestions().get(3).getResults();
            Assertions.assertEquals(4, results.size());
            Assertions.assertEquals("1", results.get(0).values.get(0));
            Assertions.assertEquals("2", results.get(1).values.get(0));
            Assertions.assertEquals("3", results.get(2).values.get(0));
            Assertions.assertEquals("3", results.get(3).values.get(0));
            Assertions.assertEquals("This is a fulltext response", resultsFulltext.get(0).values.get(0));
            Assertions.assertEquals("This is a 2", resultsFulltext.get(1).values.get(0));
            Assertions.assertEquals("part fulltext response", resultsFulltext.get(1).values.get(1));

            
            // fetch the download link
            Response downloadResponse = given()
                .header("Authorization", "Bearer " + prof.getJwt())
                .pathParam("courseId", courseId)
                .pathParam("formId", formId)
                .when()
                .get("/course/{courseId}/quiz/form/{formId}/downloadresults");
            downloadResponse.then().statusCode(200);

            // check if the downloadResponse is a csv file
            Assertions.assertEquals("attachment; filename=results_Rollenverständnis bei Scrum.csv", downloadResponse.getHeader("Content-Disposition"));
            Assertions.assertEquals("application/octet-stream", downloadResponse.getHeader("Content-Type"));

            // check if the csv file contains the right data
            String csv = downloadResponse.getBody().asString();
            String[] lines = csv.split("\n");
            Assertions.assertEquals(6, lines.length);
            Assertions.assertEquals("1", lines[1].split(",")[1].trim());
            Assertions.assertEquals("2", lines[2].split(",")[1].trim());
            Assertions.assertEquals("3", lines[3].split(",")[1].trim());
            Assertions.assertEquals("3", lines[4].split(",")[1].trim());
            Assertions.assertEquals("5", lines[5].split(",")[1].trim());
            Assertions.assertEquals("", lines[5].split(",")[2].trim());
            Assertions.assertEquals("5", lines[5].split(",")[3].trim());
            Assertions.assertEquals("participant-3", lines[4].split(",")[0]);
            Assertions.assertEquals("participant-3", lines[5].split(",")[0]);
        } catch (Exception e) {
            System.out.println(e);
            Assertions.fail(e.getMessage());
        }
    }


    @Test
    public void stressTest() throws InterruptedException, DeploymentException, IOException {

        Integer AMOUNT_OF_STUDENTS = 50;

        // create & get course
        List<Course> courses = Helper.createCourse();
        Course course = courses.get(0);

        // get course and quiz form id
        String courseId = course.getId().toString();
        String formId = course.getQuizForms().get(0).getId().toString();

        // create 1 prof and 500 students
        MockUser prof = Helper.createMockUser("Prof");
        List<MockUser> students = new ArrayList<>();
        given().header("Authorization", "Bearer " + prof.getJwt()).when().get("/course").then().statusCode(200);
        
        for (int i = 0; i < AMOUNT_OF_STUDENTS; i++) {
            MockUser student = Helper.createMockUser("Student" + i);
            students.add(student);

            // call the get courses endpoint for each user to update the course-user relation
            Thread.sleep(100);
            given().header("Authorization", "Bearer " + student.getJwt()).when().get("/course").then().statusCode(200);
        }

        // create a websocket client for the prof and the students
        SocketClient profClient = new SocketClient();
        List<SocketClient> studentClients = new ArrayList<>();
        for (MockUser student : students) {
            SocketClient studentClient = new SocketClient();
            studentClients.add(studentClient);
            Thread.sleep(100);
        }

        // connect the prof to the quiz form
        Session profSession = ContainerProvider.getWebSocketContainer().connectToServer(
            profClient,
            URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + prof.getId() + "/" + prof.getJwt())
        );
        Thread.sleep(100);
        Assertions.assertTrue(profSession.isOpen());

        // set the form status to "WAITING" and check if it was set
        profClient.sendMessage("""
            {
                "action": "CHANGE_FORM_STATUS",
                "formStatus": "WAITING"
            }
        """);
        Thread.sleep(100);
        Assertions.assertEquals("WAITING", courseService.getCourse(courseId).getQuizForms().get(0).getStatus().toString());

        Thread.sleep(1000);

        // make the participate requests for the students and connect them to the quiz form
        for (int i = 0; i < AMOUNT_OF_STUDENTS; i++) {
            given().header("Authorization", "Bearer " + students.get(i).getJwt()).pathParam("courseId", courseId).pathParam("formId", formId).body("alias-student-" + i).when().post("/course/{courseId}/quiz/form/{formId}/participate").then().statusCode(200);
            Thread.sleep(100);
        }

        Thread.sleep(5000);

        Assertions.assertEquals(AMOUNT_OF_STUDENTS, courseService.getCourse(courseId).getQuizForms().get(0).getParticipants().size());

        // connect the students to the quiz form
        List<Session> studentSessions = new ArrayList<>();
        for (int i = 0; i < AMOUNT_OF_STUDENTS; i++) {
            Session studentSession = ContainerProvider.getWebSocketContainer().connectToServer(
                studentClients.get(i),
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + students.get(i).getId() + "/" + students.get(i).getJwt())
            );
            studentSessions.add(studentSession);
            Thread.sleep(100);
            Assertions.assertTrue(studentSession.isOpen());
        }

        // change the form status to "STARTED" and check if it was set
        profClient.sendMessage("""
            {
                "action": "CHANGE_FORM_STATUS",
                "formStatus": "STARTED"
            }
        """);
        Thread.sleep(5000);
        Assertions.assertEquals("STARTED", courseService.getCourse(courseId).getQuizForms().get(0).getStatus().toString());

        // make the students submit their answers
        for (int i = 0; i < AMOUNT_OF_STUDENTS; i++) {
            studentClients.get(i).sendMessage("""
                {
                    "action": "ADD_RESULT",
                    "resultElementId": %s,
                    "resultValues": ["2"]
                }
            """.formatted(course.getQuizForms().get(0).questions.get(0).getId().toString()));
            Thread.sleep(100);
        }

        Thread.sleep(15_000);

        // check that the results were added
        Assertions.assertEquals(AMOUNT_OF_STUDENTS, courseService.getCourse(courseId).getQuizForms().get(0).getQuestions().get(0).getResults().size());

        // check that the score was updated
        for (int i = 0; i < AMOUNT_OF_STUDENTS; i++) {
            Assertions.assertTrue(courseService.getCourse(courseId).getQuizForms().get(0).getParticipants().get(i).getScore() > 9);
        }

        // let the prof stop the question
        profClient.sendMessage("""
            {
                "action": "NEXT"
            }
        """);
        Thread.sleep(5000);

        // check that the currentQuestionIndex is 0 and the currentQuestionFinished is true
        Assertions.assertEquals(0, courseService.getCourse(courseId).getQuizForms().get(0).getCurrentQuestionIndex());
        Assertions.assertTrue(courseService.getCourse(courseId).getQuizForms().get(0).getCurrentQuestionFinished());

        // check that all students got the "CLOSED_QUESTION" message
        for (int i = 0; i < AMOUNT_OF_STUDENTS; i++) {
            Assertions.assertEquals("CLOSED_QUESTION", LiveQuizSocketMessage.getByJsonWithForm(studentClients.get(i).getMessageQueue().get(studentClients.get(i).getMessageQueue().size() - 1)).action);
            Assertions.assertTrue(LiveQuizSocketMessage.getByJsonWithForm(studentClients.get(i).getMessageQueue().get(studentClients.get(i).getMessageQueue().size() - 1)).userHasAnsweredCorrectly);
        }

        // let the prof start the next question
        profClient.sendMessage("""
            {
                "action": "NEXT"
            }
        """);
        Thread.sleep(1000);

        // check that the students got the opened and closed question message and that it contains the correct result
        for (int i = 0; i < AMOUNT_OF_STUDENTS; i++) {
            // Assertions.assertEquals("CLOSED_QUESTION", LiveQuizSocketMessage.getByJsonWithForm(studentClients.get(i).getMessageQueue().get(studentClients.get(i).getMessageQueue().size() - 2)).action);
            // Assertions.assertFalse(LiveQuizSocketMessage.getByJsonWithForm(studentClients.get(i).getMessageQueue().get(studentClients.get(i).getMessageQueue().size() - 2)).userHasAnsweredCorrectly);
            Assertions.assertEquals("OPENED_NEXT_QUESTION", LiveQuizSocketMessage.getByJsonWithForm(studentClients.get(i).getMessageQueue().get(studentClients.get(i).getMessageQueue().size() - 1)).action);
        }

        // add a wrong result to the quiz form
        for (int i = 0; i < AMOUNT_OF_STUDENTS; i++) {
            studentClients.get(i).sendMessage("""
                {
                    "action": "ADD_RESULT",
                    "resultElementId": %s,
                    "resultValues": ["1"]
                }
            """.formatted(course.getQuizForms().get(0).questions.get(1).getId().toString()));
            Thread.sleep(100);
        }

        Thread.sleep(5000);

        // check that the results were added
        Assertions.assertEquals(AMOUNT_OF_STUDENTS, courseService.getCourse(courseId).getQuizForms().get(0).questions.get(1).results.size());
        Assertions.assertEquals("1", courseService.getCourse(courseId).getQuizForms().get(0).questions.get(1).results.get(0).values.get(0));

        // check that the score was updated
        for (int i = 0; i < AMOUNT_OF_STUDENTS; i++) {
            Assertions.assertTrue(courseService.getCourse(courseId).getQuizForms().get(0).getParticipants().get(i).getScore() < 16);
        }

        // close the websocket connections
        profSession.close();
        for (int i = 0; i < AMOUNT_OF_STUDENTS; i++) {
            studentSessions.get(i).close();
        }

    }


}