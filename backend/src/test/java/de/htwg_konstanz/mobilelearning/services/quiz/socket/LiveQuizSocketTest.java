package de.htwg_konstanz.mobilelearning.services.quiz.socket;

import static io.restassured.RestAssured.given;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.htwg_konstanz.mobilelearning.Helper;
import de.htwg_konstanz.mobilelearning.SocketClient;
import de.htwg_konstanz.mobilelearning.MockMongoTestProfile;
import de.htwg_konstanz.mobilelearning.MockUser;
import de.htwg_konstanz.mobilelearning.models.Course;
import de.htwg_konstanz.mobilelearning.models.quiz.QuizForm;
import de.htwg_konstanz.mobilelearning.services.CourseService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import jakarta.websocket.ContainerProvider;
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

        // get course and feedback form id
        String courseId = course.getId().toString();
        String formId = course.getQuizForms().get(0).getId().toString();

        // create a websocket client
        // (@ServerEndpoint("/course/{courseId}/quiz/form/{formId}/subscribe/{userId}/{jwt}")
        try {
            SocketClient client = new SocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + Helper.createMockUser("Prof").getId() + "/" + Helper.createMockUser("Prof").getJwt())
            );
            client.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "STARTED"
                }
            """);
            Thread.sleep(1000);
            session.close();

            // check if the form status has changed
            Assertions.assertTrue(courseService.getCourse(courseId).getQuizForms().get(0).getStatus().toString().equals("STARTED"));
        } catch (Exception e) {
            System.out.println(e);
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    public void startQuizFormNotOwner() {
        // create & get courses
        List<Course> courses = Helper.createCourse();
        Course course = courses.get(0);

        // get course and feedback form id
        String courseId = course.getId().toString();
        String formId = course.getQuizForms().get(0).getId().toString();

        // create a websocket client
        // (@ServerEndpoint("/course/{courseId}/quiz/form/{formId}/subscribe/{userId}/{jwt}")
        try {
            SocketClient client = new SocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + Helper.createMockUser("Prof2").getId() + "/" + Helper.createMockUser("Prof2").getJwt())
            );
            client.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "STARTED",
                }
            """);
            Thread.sleep(1000);
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

        // get course and feedback form id
        String courseId = course.getId().toString();
        String formId = course.getQuizForms().get(0).getId().toString();

        // create a websocket client
        // (@ServerEndpoint("/course/{courseId}/quiz/form/{formId}/subscribe/{userId}/{jwt}")
        try {
            SocketClient client = new SocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + Helper.createMockUser("Student").getId() + "/" + Helper.createMockUser("Student").getJwt())
            );
            client.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "STARTED",
                }
            """);
            Thread.sleep(1000);
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
        
        // add a result & get quiz forms
        addResult(courseId, formId, questionId);
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

        // get course and feedback form id
        String courseId = course.getId().toString();
        String formId = course.getQuizForms().get(0).getId().toString();

        // create a websocket client
        // (@ServerEndpoint("/course/{courseId}/quiz/form/{formId}/subscribe/{userId}/{jwt}")
        try {
            SocketClient client = new SocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + Helper.createMockUser("Prof").getId() + "/" + Helper.createMockUser("Prof").getJwt())
            );
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
            Thread.sleep(1000);
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

        // get course and feedback form id
        String courseId = course.getId().toString();
        String formId = course.getQuizForms().get(0).getId().toString();

        // create a websocket client
        // (@ServerEndpoint("/course/{courseId}/quiz/form/{formId}/subscribe/{userId}/{jwt}")
        try {
            SocketClient client = new SocketClient();
            SocketClient client2 = new SocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + Helper.createMockUser("Prof").getId() + "/" + Helper.createMockUser("Prof").getJwt())
            );Session session2 = ContainerProvider.getWebSocketContainer().connectToServer(
                client2,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + Helper.createMockUser("Prof2").getId() + "/" + Helper.createMockUser("Prof2").getJwt())
            );
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
            Thread.sleep(1000);
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

        // get course and feedback form id
        String courseId = course.getId().toString();
        String formId = course.getQuizForms().get(0).getId().toString();

        // create a websocket client
        // (@ServerEndpoint("/course/{courseId}/quiz/form/{formId}/subscribe/{userId}/{jwt}")
        try {
            SocketClient client = new SocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + Helper.createMockUser("Prof").getId() + "/" + Helper.createMockUser("Prof").getJwt())
            );
            SocketClient client2 = new SocketClient();
            Session session2 = ContainerProvider.getWebSocketContainer().connectToServer(
                client2,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + Helper.createMockUser("Student").getId() + "/" + Helper.createMockUser("Student").getJwt())
            );
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
            Thread.sleep(1000);
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

        // get course and feedback form id
        String courseId = course.getId().toString();
        String formId = course.getQuizForms().get(0).getId().toString();
        String questionId = courses.getFirst().quizForms.get(0).questions.get(0).getId().toString();

        // create a websocket client
        // (@ServerEndpoint("/course/{courseId}/quiz/form/{formId}/subscribe/{userId}/{jwt}")
        try {
            SocketClient client = new SocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + Helper.createMockUser("Prof").getId() + "/" + Helper.createMockUser("Prof").getJwt())
            );
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
                                    .header("Authorization", "Bearer " + Helper.createMockUser("Prof").getJwt())
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
            Thread.sleep(1000);
            session.close();
            Response responseCleared = given()
                                            .header("Authorization", "Bearer " + Helper.createMockUser("Prof").getJwt())
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
        ObjectMapper mapper = new ObjectMapper();
        Assertions.assertEquals(courses.size(), 1);
        Course course = courses.get(0);
        Assertions.assertEquals(course.getQuizForms().size(), 1);

        // get course and feedback form id
        String courseId = course.getId().toString();
        String formId = course.getQuizForms().get(0).getId().toString();

        // create a websocket client
        // (@ServerEndpoint("/course/{courseId}/quiz/form/{formId}/subscribe/{userId}/{jwt}")
        try {
            SocketClient client = new SocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + Helper.createMockUser("Prof").getId() + "/" + Helper.createMockUser("Prof").getJwt())
            );
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

            Thread.sleep(1000);
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

        // get course and feedback form id
        String courseId = course.getId().toString();
        String formId = course.getQuizForms().get(0).getId().toString();

        // create a websocket client
        // (@ServerEndpoint("/course/{courseId}/quiz/form/{formId}/subscribe/{userId}/{jwt}")
        try {
            // Owner stats feedback & 2nd prof (not owner) tries to change question
            SocketClient client = new SocketClient();
            SocketClient client2 = new SocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + Helper.createMockUser("Prof").getId() + "/" + Helper.createMockUser("Prof").getJwt())
            );            
            Session session2 = ContainerProvider.getWebSocketContainer().connectToServer(
                client2,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + Helper.createMockUser("Prof2").getId() + "/" + Helper.createMockUser("Prof2").getJwt())
            );
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

        // get course and feedback form id
        String courseId = course.getId().toString();
        String formId = course.getQuizForms().get(0).getId().toString();

        // create a websocket client
        // (@ServerEndpoint("/course/{courseId}/quiz/form/{formId}/subscribe/{userId}/{jwt}")
        try {
            // Owner stats feedback & 2nd prof (with student role) tries to change question
            SocketClient client = new SocketClient();
            SocketClient client2 = new SocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + Helper.createMockUser("Prof").getId() + "/" + Helper.createMockUser("Prof").getJwt())
            );            
            Session session2 = ContainerProvider.getWebSocketContainer().connectToServer(
                client2,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + Helper.createMockUser("Student").getId() + "/" + Helper.createMockUser("Student").getJwt())
            );
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

    private void addResult(String courseId, String formId, String questionId) {
        // create a websocket client
        // (@ServerEndpoint("/course/{courseId}/quiz/form/{formId}/subscribe/{userId}/{jwt}")
        try {
            SocketClient client = new SocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + Helper.createMockUser("Prof").getId() + "/" + Helper.createMockUser("Prof").getJwt())
            );
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
            Thread.sleep(1000);
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

        // get course and feedback form id
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

            // set the form status to "WAITING" and check if it was set
            profClient.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "WAITING"
                }
            """);
            Thread.sleep(100);
            Assertions.assertEquals("WAITING", courseService.getCourse(courseId).getQuizForms().get(0).getStatus().toString());

            // make the participate requests for the students
            Response response1 = given()
                                    .header("Authorization", "Bearer " + student1.getJwt())
                                    .pathParam("courseId", courseId)
                                    .pathParam("formId", formId)
                                    .body("alias-student-1")
                                    .when()
                                    .post("/course/{courseId}/quiz/form/{formId}/participate");
            Response response2 = given()
                                    .header("Authorization", "Bearer " + student2.getJwt())
                                    .pathParam("courseId", courseId)
                                    .pathParam("formId", formId)
                                    .body("alias-student-2")
                                    .when()
                                    .post("/course/{courseId}/quiz/form/{formId}/participate"); 
            Response response3 = given()
                                    .header("Authorization", "Bearer " + student3.getJwt())
                                    .pathParam("courseId", courseId)
                                    .pathParam("formId", formId)
                                    .body("alias-student-3")
                                    .when()
                                    .post("/course/{courseId}/quiz/form/{formId}/participate");

            // check if the participate requests were successful and if the quiz has now participants
            Thread.sleep(100);
            Assertions.assertEquals(200, response1.getStatusCode());
            Assertions.assertEquals(200, response2.getStatusCode());
            Assertions.assertEquals(200, response3.getStatusCode());
            Assertions.assertEquals(3, courseService.getCourse(courseId).getQuizForms().get(0).getParticipants().size());

            // connect the students to the quiz form
            Session studentSession1 = ContainerProvider.getWebSocketContainer().connectToServer(
                studentClient,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + student1.getId() + "/" + student1.getJwt())
            );
            Session studentSession2 = ContainerProvider.getWebSocketContainer().connectToServer(
                studentClient,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + student2.getId() + "/" + student2.getJwt())
            );
            Session studentSession3 = ContainerProvider.getWebSocketContainer().connectToServer(
                studentClient,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + student3.getId() + "/" + student3.getJwt())
            );
            Thread.sleep(1000);

            // check if the prof received the "PARTICIPANT_JOINED" messages
            Assertions.assertEquals("PARTICIPANT_JOINED", LiveQuizSocketMessage.getByJsonWithForm(profClient.getMessageQueue().get(profClient.getMessageQueue().size() - 1)).action);
            Assertions.assertEquals("PARTICIPANT_JOINED", LiveQuizSocketMessage.getByJsonWithForm(profClient.getMessageQueue().get(profClient.getMessageQueue().size() - 2)).action);
            Assertions.assertEquals("PARTICIPANT_JOINED", LiveQuizSocketMessage.getByJsonWithForm(profClient.getMessageQueue().get(profClient.getMessageQueue().size() - 3)).action);
            Assertions.assertEquals("FORM_STATUS_CHANGED", LiveQuizSocketMessage.getByJsonWithForm(profClient.getMessageQueue().get(profClient.getMessageQueue().size() - 4)).action);
            
            // check if the prof gets the right amount of participants in the last message
            Assertions.assertEquals(3, LiveQuizSocketMessage.getByJsonWithForm(profClient.getMessageQueue().get(profClient.getMessageQueue().size() - 1)).form.participants.size());

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

}
