package de.htwg_konstanz.mobilelearning.services.feedback.socket;

import static io.restassured.RestAssured.given;

import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import de.htwg_konstanz.mobilelearning.Helper;
import de.htwg_konstanz.mobilelearning.SocketClient;
import de.htwg_konstanz.mobilelearning.MockMongoTestProfile;
import de.htwg_konstanz.mobilelearning.MockUser;
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
            SocketClient client = new SocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" + Helper.createMockUser("Prof").getId() + "/" + Helper.createMockUser("Prof").getJwt())
            );
            client.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "STARTED"
                    
                }
            """);
            Thread.sleep(100);
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
             SocketClient client = new SocketClient();
             Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                 client,
                 URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" + Helper.createMockUser("Prof2").getId() + "/" + Helper.createMockUser("Prof2").getJwt())
             );
             client.sendMessage("""
                 {
                     "action": "CHANGE_FORM_STATUS",
                     "formStatus": "STARTED"
                     
                 }
             """);
             Thread.sleep(100);
             session.close();
 
             // form status should not change because user is not owner
             Assertions.assertTrue(courseService.getCourse(courseId).getFeedbackForms().get(0).getStatus().toString().equals("NOT_STARTED"));
         } catch (Exception e) {
             System.out.println(e);
             Assertions.fail(e.getMessage());
         }
        
    }

    @Test
    public void startFeedbackStudent() {
        //create & get courses + ids
        List<Course> courses = Helper.createCourse();
        String courseId = courses.get(0).getId().toString();
        String formId = courses.get(0).getFeedbackForms().get(0).getId().toString();
 
         // create a websocket client
         // (@ServerEndpoint("/course/{courseId}/feedback/form/{formId}/subscribe/{userId}/{jwt}")
         try {
             SocketClient client = new SocketClient();
             Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                 client,
                 URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" + Helper.createMockUser("Student").getId() + "/" + Helper.createMockUser("Student").getJwt())
             );
             client.sendMessage("""
                 {
                     "action": "CHANGE_FORM_STATUS",
                     "formStatus": "STARTED"
                 }
             """);
             Thread.sleep(100);
             session.close();
 
             // form status should not change because user student
             Assertions.assertTrue(courseService.getCourse(courseId).getFeedbackForms().get(0).getStatus().toString().equals("NOT_STARTED"));
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
             SocketClient client = new SocketClient();
             Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                 client,
                 URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" + Helper.createMockUser("Prof").getId() + "/" + Helper.createMockUser("Prof").getJwt())
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
            Thread.sleep(100);
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
             SocketClient client = new SocketClient();
             Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                 client,
                 URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" + Helper.createMockUser("Prof").getId() + "/" + Helper.createMockUser("Prof").getJwt())
             );
             client.sendMessage("""
                 {
                     "action": "CHANGE_FORM_STATUS",
                     "formStatus": "STARTED"
                     
                 }
             """);
            // adds result to feedbackform
            client.sendMessage(String.format("""
                {
                    "action": "ADD_RESULT",
                    "resultElementId": %s,
                    "resultValues": [5]
                    
                }
            """, questionId));
             Thread.sleep(100);
             client.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "FINISHED"
                    
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
                    "formStatus": "NOT_STARTED"
                    
                }
            """);
            Thread.sleep(100);
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
             SocketClient client = new SocketClient();
             SocketClient client2 = new SocketClient();
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
            SocketClient client = new SocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" + Helper.createMockUser("Prof").getId() + "/" + Helper.createMockUser("Prof").getJwt())
            );
            // starts feedbacksession
            client.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "STARTED"
                    
                }
            """);
            // adds result to feedbackform
            client.sendMessage(String.format("""
                {
                    "action": "ADD_RESULT",
                    "resultElementId": %s,
                    "resultValues": [5]
                    
                }
            """, questionId));
            Thread.sleep(100);
            session.close();

            // check if the form status has changed
            Assertions.assertTrue(courseService.getCourse(courseId).getFeedbackForms().get(0).getStatus().toString().equals("STARTED"));
        } catch (Exception e) {
            System.out.println(e);
            Assertions.fail(e.getMessage());
        }
    }  

        // test the participate function in combination with the new "WAITING" status
    // a student should be able to participate in a feedback form
    // the prof should get a message every time a student joins the feedback form ("PARTICIPANT_JOINED")
    @Test
    public void participateInFeedbackForm() {

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
        String formId = course.getFeedbackForms().get(0).getId().toString();

        // try catch block to handle exceptions of websocket connection
        try {

            // create websocket clients
            SocketClient profClient = new SocketClient();
            SocketClient studentClient = new SocketClient();

            // connect the prof to the feedback form
            Session profSession = ContainerProvider.getWebSocketContainer().connectToServer(
                profClient,
                URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" + prof.getId() + "/" + prof.getJwt())
            );

            // set the form status to "WAITING" and check if it was set
            profClient.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "WAITING"
                }
            """);
            Thread.sleep(100);
            Assertions.assertEquals("WAITING", courseService.getCourse(courseId).getFeedbackForms().get(0).getStatus().toString());
            Assertions.assertEquals(0, courseService.getCourse(courseId).getFeedbackForms().get(0).getParticipants().size());

            // connect the students to the feedback form
            Session studentSession1 = ContainerProvider.getWebSocketContainer().connectToServer(
                studentClient,
                URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" + student1.getId() + "/" + student1.getJwt())
            );
            Thread.sleep(100);
            Assertions.assertEquals(1, courseService.getCourse(courseId).getFeedbackForms().get(0).getParticipants().size());

            Session studentSession2 = ContainerProvider.getWebSocketContainer().connectToServer(
                studentClient,
                URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" + student2.getId() + "/" + student2.getJwt())
            );
            Thread.sleep(100);
            Assertions.assertEquals(2, courseService.getCourse(courseId).getFeedbackForms().get(0).getParticipants().size());
            Session studentSession3 = ContainerProvider.getWebSocketContainer().connectToServer(
                studentClient,
                URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" + student3.getId() + "/" + student3.getJwt())
            );
            Thread.sleep(100);
            Assertions.assertEquals(3, courseService.getCourse(courseId).getFeedbackForms().get(0).getParticipants().size());   

            // check if the prof received the "PARTICIPANT_JOINED" messages
            Assertions.assertEquals("PARTICIPANT_JOINED", LiveFeedbackSocketMessage.getByJsonWithForm(profClient.getMessageQueue().get(profClient.getMessageQueue().size() - 1)).action);
            Assertions.assertEquals("PARTICIPANT_JOINED", LiveFeedbackSocketMessage.getByJsonWithForm(profClient.getMessageQueue().get(profClient.getMessageQueue().size() - 2)).action);
            Assertions.assertEquals("PARTICIPANT_JOINED", LiveFeedbackSocketMessage.getByJsonWithForm(profClient.getMessageQueue().get(profClient.getMessageQueue().size() - 3)).action);
            Assertions.assertEquals("FORM_STATUS_CHANGED", LiveFeedbackSocketMessage.getByJsonWithForm(profClient.getMessageQueue().get(profClient.getMessageQueue().size() - 4)).action);
            
            // check if the prof gets the right amount of participants in the last message
            Assertions.assertEquals(3, LiveFeedbackSocketMessage.getByJsonWithForm(profClient.getMessageQueue().get(profClient.getMessageQueue().size() - 1)).form.participants.size());

            // change the form status to "STARTED" and check if it was set
            profClient.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "STARTED"
                }
            """);
            Thread.sleep(100);
            Assertions.assertEquals("STARTED", courseService.getCourse(courseId).getFeedbackForms().get(0).getStatus().toString());

            // check if the students received the "FORM_STATUS_CHANGED" message
            Assertions.assertEquals("FORM_STATUS_CHANGED", LiveFeedbackSocketMessage.getByJsonWithForm(studentClient.getMessageQueue().get(studentClient.getMessageQueue().size() - 1)).action);
            Assertions.assertEquals("FORM_STATUS_CHANGED", LiveFeedbackSocketMessage.getByJsonWithForm(studentClient.getMessageQueue().get(studentClient.getMessageQueue().size() - 1)).action);
            Assertions.assertEquals("FORM_STATUS_CHANGED", LiveFeedbackSocketMessage.getByJsonWithForm(studentClient.getMessageQueue().get(studentClient.getMessageQueue().size() - 1)).action);

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
