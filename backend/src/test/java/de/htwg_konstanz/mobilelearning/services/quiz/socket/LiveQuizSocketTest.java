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
import de.htwg_konstanz.mobilelearning.LiveFeedbackSocketClient;
import de.htwg_konstanz.mobilelearning.MockMongoTestProfile;
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
            LiveFeedbackSocketClient client = new LiveFeedbackSocketClient();
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
            LiveFeedbackSocketClient client = new LiveFeedbackSocketClient();
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
            LiveFeedbackSocketClient client = new LiveFeedbackSocketClient();
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
        addResult(courseId, formId, questionId, "Prof");
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
        addResult(courseId, formId, questionId, "Prof");
        addResult(courseId, formId, questionId, "Prof");
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
            LiveFeedbackSocketClient client = new LiveFeedbackSocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + Helper.createMockUser("Prof").getId() + "/" + Helper.createMockUser("Prof").getJwt())
            );
            client.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "STARTED",
                    "roles": [Prof]
                }
            """);
            Thread.sleep(100);
            client.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "FINISHED",
                    "roles": [Prof]
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
            LiveFeedbackSocketClient client = new LiveFeedbackSocketClient();
            LiveFeedbackSocketClient client2 = new LiveFeedbackSocketClient();
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
                    "formStatus": "STARTED",
                    "roles": [Prof]
                }
            """);
            Thread.sleep(100);
            client2.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "FINISHED",
                    "roles": [Prof]
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

    //TODO Fix this test
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
            LiveFeedbackSocketClient client = new LiveFeedbackSocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + Helper.createMockUser("Prof").getId() + "/" + Helper.createMockUser("Prof").getJwt())
            );
            client.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "STARTED",
                    "roles": [Prof]
                }
            """);
            Thread.sleep(100);
            client.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "FINISHED",
                    "roles": [Student]
                }
            """);
            Thread.sleep(1000);
            session.close();

            // check if the form was started & status has not changed
            Assertions.assertTrue(courseService.getCourse(courseId).getQuizForms().get(0).getStatus().toString().equals("FINISHED"));
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
            LiveFeedbackSocketClient client = new LiveFeedbackSocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + Helper.createMockUser("Prof").getId() + "/" + Helper.createMockUser("Prof").getJwt())
            );
            client.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "STARTED",
                    "roles": [Prof]
                }
            """);
            Thread.sleep(100);
            // adds result to quiz form
            client.sendMessage(String.format("""
                {
                    "action": "ADD_RESULT",
                    "resultElementId": %s,
                    "resultValues": ["1"],
                    "role": "STUDENT"
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
                    "formStatus": "FINISHED",
                    "roles": [Prof]
                }
            """);
            Thread.sleep(100);
            client.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "NOT_STARTED",
                    "roles": [Prof]
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
            LiveFeedbackSocketClient client = new LiveFeedbackSocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + Helper.createMockUser("Prof").getId() + "/" + Helper.createMockUser("Prof").getJwt())
            );
            client.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "STARTED",
                    "roles": [Prof]
                }
            """);
            Thread.sleep(500);
            client.sendMessage("""
                {
                    "action": "NEXT",
                    "roles": [Prof]
                }
            """); 
            Thread.sleep(500);
            // newest messagqueue item after first next should be CLOSED_QUESTION
            Map<String, String> next1 = mapper.readerFor(Map.class).readValue(client.getMessageQueue().get(1));
            Assertions.assertEquals("CLOSED_QUESTION", next1.get("action"));
            Object obj1 = next1.get("form");  // replace with your object
            LinkedHashMap<String, Object> form1 = (LinkedHashMap<String, Object>) obj1;
            Assertions.assertEquals(true, form1.get("currentQuestionFinished"));
            Assertions.assertEquals(0, form1.get("currentQuestionIndex"));

            client.sendMessage("""
                {
                    "action": "NEXT",
                    "roles": [Prof]
                }
            """);
            Thread.sleep(500);
            // newest messagqueue item after second next should be OPENED_NEXT_QUESTION
            Map<String, String> next2 = mapper.readerFor(Map.class).readValue(client.getMessageQueue().get(2));
            Assertions.assertEquals("OPENED_NEXT_QUESTION", next2.get("action"));
            client.sendMessage("""
                {
                    "action": "NEXT",
                    "roles": [Prof]
                }
            """);
            Thread.sleep(500);
            // newest messagqueue item after third next should be CLOSED_QUESTION
            Map<String, String> next3 = mapper.readerFor(Map.class).readValue(client.getMessageQueue().get(3));
            Assertions.assertEquals("CLOSED_QUESTION", next3.get("action"));
            client.sendMessage("""
                {
                    "action": "NEXT",
                    "roles": [Prof]
                }
            """);
            Thread.sleep(500);
            // newest messagqueue items after fourth next should be CLOSED_QUESTION & FINISHED
            Map<String, String> next4 = mapper.readerFor(Map.class).readValue(client.getMessageQueue().get(4));
            Assertions.assertEquals("CLOSED_QUESTION", next4.get("action"));
            Map<String, String> next5 = mapper.readerFor(Map.class).readValue(client.getMessageQueue().get(5));
            Assertions.assertEquals("FORM_STATUS_CHANGED", next5.get("action"));
            Assertions.assertEquals("FINISHED", next5.get("formStatus"));

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
            LiveFeedbackSocketClient client = new LiveFeedbackSocketClient();
            LiveFeedbackSocketClient client2 = new LiveFeedbackSocketClient();
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
                    "formStatus": "STARTED",
                    "roles": [Prof]
                }
            """);
            Thread.sleep(500);
            client2.sendMessage("""
                {
                    "action": "NEXT",
                    "roles": [Prof]
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
            LiveFeedbackSocketClient client = new LiveFeedbackSocketClient();
            LiveFeedbackSocketClient client2 = new LiveFeedbackSocketClient();
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
                    "formStatus": "STARTED",
                    "roles": [Prof]
                }
            """);
            Thread.sleep(500);
            client2.sendMessage("""
                {
                    "action": "NEXT",
                    "roles": [Student]
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

    private void addResult(String courseId, String formId, String questionId, String role) {
        // create a websocket client
        // (@ServerEndpoint("/course/{courseId}/quiz/form/{formId}/subscribe/{userId}/{jwt}")
        try {
            LiveFeedbackSocketClient client = new LiveFeedbackSocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + Helper.createMockUser("Prof").getId() + "/" + Helper.createMockUser("Prof").getJwt())
            );
            // starts quiz session
            client.sendMessage(String.format("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "STARTED",
                    "roles": [%s]
                }
            """, role));
            // adds result to quiz form
            client.sendMessage(String.format("""
                {
                    "action": "ADD_RESULT",
                    "resultElementId": %s,
                    "resultValues": ["1"],
                    "role": "STUDENT"
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
}
