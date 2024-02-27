package de.htwg_konstanz.mobilelearning.services.feedback.socket;

import static io.restassured.RestAssured.given;

import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import de.htwg_konstanz.mobilelearning.Helper;
import de.htwg_konstanz.mobilelearning.LiveFeedbackSocketClient;
import de.htwg_konstanz.mobilelearning.MockMongoTestProfile;
import de.htwg_konstanz.mobilelearning.models.Course;
import de.htwg_konstanz.mobilelearning.models.feedback.FeedbackForm;
import de.htwg_konstanz.mobilelearning.services.CourseService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Session;

@QuarkusTest
@TestProfile(MockMongoTestProfile.class)
public class LiveFeedbackSocketTest {

    @Inject
    private CourseService courseService;

    @BeforeEach
    void init(TestInfo testInfo){
        System.out.println("------------------------------");
        System.out.println("Test: " + testInfo.getDisplayName());
        courseService.deleteAllCourses();
    }

    @Test
    public void startFeedbackForm() {
        // create & get courses
        List<Course> courses = Helper.createCourse();
        Assertions.assertEquals(courses.size(), 1);
        Course course = courses.get(0);
        Assertions.assertEquals(course.getFeedbackForms().size(), 1);

        // get course and feedback form id
        String courseId = course.getId().toString();
        String formId = course.getFeedbackForms().get(0).getId().toString();

        // create a websocket client
        // (@ServerEndpoint("/course/{courseId}/feedback/form/{formId}/subscribe/{userId}/{jwt}")
        try {
            LiveFeedbackSocketClient client = new LiveFeedbackSocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" + Helper.createMockUser("Prof").getId() + "/" + Helper.createMockUser("Prof").getJwt())
            );
            client.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "STARTED",
                    "roles": [Prof]
                }
            """);
            Thread.sleep(1000);
            session.close();

            // check if the form status has changed
            Assertions.assertTrue(courseService.getCourse(courseId).getFeedbackForms().get(0).getStatus().toString().equals("STARTED"));
        } catch (Exception e) {
            System.out.println(e);
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    public void feedbackAcceptedOnce() {
        //create & get courses + ids 
        List<Course> courses = Helper.createCourse();
        String courseId = courses.getFirst().getId().toString();
        String formId = courses.getFirst().getFeedbackForms().get(0).getId().toString();
        String questionId = courses.getFirst().feedbackForms.get(0).questions.get(0).getId().toString();
        // add a result to the feedback form
        addResult(courseId, formId, questionId);
        addResult(courseId, formId, questionId);
        // get feedback forms from course
        Response response = given()
                                .header("Authorization", "Bearer " + Helper.createMockUser("Prof").getJwt())
                                .pathParam("courseId", courseId)
                                .pathParam("formId", formId)
                                .queryParam("results", true)
                                .when()
                                .get("/course/{courseId}/feedback/form/{formId}");
        FeedbackForm feedbackFormFromService = response
                                                    .then()
                                                    .statusCode(200)
                                                    .extract()
                                                    .body()
                                                    .as(FeedbackForm.class);
        
        // Assert getFeedbackForm()
         Assertions.assertEquals("Erster Sprint", feedbackFormFromService.name);
        Assertions.assertEquals("Hier wollen wir Ihr Feedback zum ersten Sprint einholen", feedbackFormFromService.description);
        Assertions.assertEquals(1, feedbackFormFromService.questions.size());
        Assertions.assertEquals(1, feedbackFormFromService.questions.get(0).results.size());
    }

    @Test
    public void startFeedbackuserNotOwner() {
        //create & get courses + ids
        List<Course> courses = Helper.createCourse();
        String courseId = courses.get(0).getId().toString();
        String formId = courses.get(0).getFeedbackForms().get(0).getId().toString();
 
         // create a websocket client
         // (@ServerEndpoint("/course/{courseId}/feedback/form/{formId}/subscribe/{userId}/{jwt}")
         try {
             LiveFeedbackSocketClient client = new LiveFeedbackSocketClient();
             Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                 client,
                 URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" + Helper.createMockUser("Prof2").getId() + "/" + Helper.createMockUser("Prof2").getJwt())
             );
             client.sendMessage("""
                 {
                     "action": "CHANGE_FORM_STATUS",
                     "formStatus": "STARTED",
                     "roles": [Prof]
                 }
             """);
             Thread.sleep(1000);
             session.close();
 
             // form status should not change because user is not owner
             Assertions.assertTrue(courseService.getCourse(courseId).getFeedbackForms().get(0).getStatus().toString().equals("NOT_STARTED"));
         } catch (Exception e) {
             System.out.println(e);
             Assertions.fail(e.getMessage());
         }
        
    }

    //TODO Fix test not started
    @Test
    public void startFeedbackStudent() {
        //create & get courses + ids
        List<Course> courses = Helper.createCourse();
        String courseId = courses.get(0).getId().toString();
        String formId = courses.get(0).getFeedbackForms().get(0).getId().toString();
 
         // create a websocket client
         // (@ServerEndpoint("/course/{courseId}/feedback/form/{formId}/subscribe/{userId}/{jwt}")
         try {
             LiveFeedbackSocketClient client = new LiveFeedbackSocketClient();
             Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                 client,
                 URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" + Helper.createMockUser("Prof").getId() + "/" + Helper.createMockUser("Prof").getJwt())
             );
             client.sendMessage("""
                 {
                     "action": "CHANGE_FORM_STATUS",
                     "formStatus": "STARTED",
                     "roles": [Student]
                 }
             """);
             Thread.sleep(1000);
             session.close();
 
             // form status should not change because user student
             Assertions.assertTrue(courseService.getCourse(courseId).getFeedbackForms().get(0).getStatus().toString().equals("STARTED"));
         } catch (Exception e) {
             System.out.println(e);
             Assertions.fail(e.getMessage());
         }
        
    }

    @Test
    public void stopFeedback() {
        //create & get courses + ids
        List<Course> courses = Helper.createCourse();
        String courseId = courses.get(0).getId().toString();
        String formId = courses.get(0).getFeedbackForms().get(0).getId().toString();
 
         // create a websocket client
         // (@ServerEndpoint("/course/{courseId}/feedback/form/{formId}/subscribe/{userId}/{jwt}")
         try {
             LiveFeedbackSocketClient client = new LiveFeedbackSocketClient();
             Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                 client,
                 URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" + Helper.createMockUser("Prof").getId() + "/" + Helper.createMockUser("Prof").getJwt())
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
 
             // form status should not change because user student
             Assertions.assertTrue(courseService.getCourse(courseId).getFeedbackForms().get(0).getStatus().toString().equals("FINISHED"));
         } catch (Exception e) {
             System.out.println(e);
             Assertions.fail(e.getMessage());
         }
        
    }    
    
    @Test
    public void clearFeedback() {
        //create & get courses + ids
        List<Course> courses = Helper.createCourse();
        String courseId = courses.get(0).getId().toString();
        String formId = courses.get(0).getFeedbackForms().get(0).getId().toString();
        String questionId = courses.getFirst().feedbackForms.get(0).questions.get(0).getId().toString();
 
         // create a websocket client
         // (@ServerEndpoint("/course/{courseId}/feedback/form/{formId}/subscribe/{userId}/{jwt}")
         try {
             LiveFeedbackSocketClient client = new LiveFeedbackSocketClient();
             Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                 client,
                 URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" + Helper.createMockUser("Prof").getId() + "/" + Helper.createMockUser("Prof").getJwt())
             );
             client.sendMessage("""
                 {
                     "action": "CHANGE_FORM_STATUS",
                     "formStatus": "STARTED",
                     "roles": [Prof]
                 }
             """);
            // adds result to feedbackform
            client.sendMessage(String.format("""
                {
                    "action": "ADD_RESULT",
                    "resultElementId": %s,
                    "resultValues": [5],
                    "role": "STUDENT"
                }
            """, questionId));
             Thread.sleep(100);
             client.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "FINISHED",
                    "roles": [Prof]
                }
            """); 
            Response response = given()
                                    .header("Authorization", "Bearer " + Helper.createMockUser("Prof").getJwt())
                                    .pathParam("courseId", courseId)
                                    .pathParam("formId", formId)
                                    .queryParam("results", true)
                                    .when()
                                    .get("/course/{courseId}/feedback/form/{formId}");
            FeedbackForm feedbackForm = response
                                                        .then()
                                                        .statusCode(200)
                                                        .extract()
                                                        .body()
                                                        .as(FeedbackForm.class);    
            Assertions.assertEquals(1, feedbackForm.getQuestions().get(0).results.size());       
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
                                    .get("/course/{courseId}/feedback/form/{formId}");
            FeedbackForm feedbackFormCleared = responseCleared
                                                        .then()
                                                        .statusCode(200)
                                                        .extract()
                                                        .body()
                                                        .as(FeedbackForm.class);    
 
             // form should be cleared after status is set to NOT_STARTED
            Assertions.assertTrue(feedbackFormCleared.getStatus().toString().equals("NOT_STARTED"));
            Assertions.assertEquals(0, feedbackFormCleared.getQuestions().get(0).results.size()); 
        } catch (Exception e) {
             System.out.println(e);
             Assertions.fail(e.getMessage());
         }
        
    }

    @Test
    public void stopFeedbackNotOwner() {
        //create & get courses + ids
        List<Course> courses = Helper.createCourse();
        String courseId = courses.get(0).getId().toString();
        String formId = courses.get(0).getFeedbackForms().get(0).getId().toString();
 
         // create a websocket client
         // (@ServerEndpoint("/course/{courseId}/feedback/form/{formId}/subscribe/{userId}/{jwt}")
         try {
             LiveFeedbackSocketClient client = new LiveFeedbackSocketClient();
             LiveFeedbackSocketClient client2 = new LiveFeedbackSocketClient();
             Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                 client,
                 URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" + Helper.createMockUser("Prof").getId() + "/" + Helper.createMockUser("Prof").getJwt())
             );
             Session session2 = ContainerProvider.getWebSocketContainer().connectToServer(
                 client2,
                 URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" + Helper.createMockUser("Prof2").getId() + "/" + Helper.createMockUser("Prof2").getJwt())
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
 
            // form status should not change because User is not owner of the course
            Assertions.assertTrue(courseService.getCourse(courseId).getFeedbackForms().get(0).getStatus().toString().equals("STARTED"));
            } catch (Exception e) {
             System.out.println(e);
             Assertions.fail(e.getMessage());
         }
        
    }

    private void addResult(String courseId, String formId, String questionId) {
        // create a websocket client
        // (@ServerEndpoint("/course/{courseId}/feedback/form/{formId}/subscribe/{userId}/{jwt}")
        try {
            LiveFeedbackSocketClient client = new LiveFeedbackSocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" + Helper.createMockUser("Prof").getId() + "/" + Helper.createMockUser("Prof").getJwt())
            );
            // starts feedbacksession
            client.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "STARTED",
                    "roles": [Prof]
                }
            """);
            // adds result to feedbackform
            client.sendMessage(String.format("""
                {
                    "action": "ADD_RESULT",
                    "resultElementId": %s,
                    "resultValues": [5],
                    "role": "STUDENT"
                }
            """, questionId));
            Thread.sleep(1000);
            session.close();

            // check if the form status has changed
            Assertions.assertTrue(courseService.getCourse(courseId).getFeedbackForms().get(0).getStatus().toString().equals("STARTED"));
        } catch (Exception e) {
            System.out.println(e);
            Assertions.fail(e.getMessage());
        }
    }  
}
