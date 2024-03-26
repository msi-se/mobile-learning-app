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
import de.htwg_konstanz.mobilelearning.models.Result;
import de.htwg_konstanz.mobilelearning.models.feedback.FeedbackForm;
import de.htwg_konstanz.mobilelearning.services.CourseService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
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

        // create user and sync the course-user relation
        MockUser prof2 = Helper.createMockUser("Prof2");
        given().header("Authorization", "Bearer " + prof2.getJwt()).get("/course").then().statusCode(200);

        // create a websocket client
        try {
            SocketClient client = new SocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" + prof2.getId() + "/" + prof2.getJwt())
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
 
        // create user and sync the course-user relation
        MockUser student = Helper.createMockUser("Student");
        given().header("Authorization", "Bearer " + student.getJwt()).get("/course").then().statusCode(200);

        // create a websocket client
        try {
            SocketClient client = new SocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" + student.getId() + "/" + student.getJwt())
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

        // create user and sync the course-user relation
        MockUser prof = Helper.createMockUser("Prof");
        given().header("Authorization", "Bearer " + prof.getJwt()).get("/course").then().statusCode(200);
 
        // create a websocket client
        try {
            SocketClient client = new SocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" + prof.getId() + "/" + prof.getJwt())
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
            Assertions.assertTrue(courseService.getCourse(courseId).getFeedbackForms().get(0).getStatus().toString().equals("STARTED"));
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
        List<Course> courses = Helper.createCourse("Prof");
        String courseId = courses.get(0).getId().toString();
        String formId = courses.get(0).getFeedbackForms().get(0).getId().toString();
        String questionId = courses.getFirst().feedbackForms.get(0).questions.get(0).getId().toString();
 
        // create user and sync the course-user relation
        MockUser prof = Helper.createMockUser("Prof");
        given().header("Authorization", "Bearer " + prof.getJwt()).get("/course").then().statusCode(200);

        // create a websocket client
        try {
            SocketClient client = new SocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" + prof.getId() + "/" + prof.getJwt())
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
            Thread.sleep(100); 
            Response response = given()
                                    .header("Authorization", "Bearer " + prof.getJwt())
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
                                    .header("Authorization", "Bearer " + prof.getJwt())
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
        List<Course> courses = Helper.createCourse("Prof");
        String courseId = courses.get(0).getId().toString();
        String formId = courses.get(0).getFeedbackForms().get(0).getId().toString();

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
                URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" + prof.getId() + "/" + prof.getJwt())
            );
            Thread.sleep(100);
            Assertions.assertTrue(session.isOpen());
            Session session2 = ContainerProvider.getWebSocketContainer().connectToServer(
                client2,
                URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" + prof2.getId() + "/" + prof2.getJwt())
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
 
            // form status should not change because User is not owner of the course
            Assertions.assertTrue(courseService.getCourse(courseId).getFeedbackForms().get(0).getStatus().toString().equals("STARTED"));
        } catch (Exception e) {
            System.out.println(e);
            Assertions.fail(e.getMessage());
        }
        
    }

    private void addResult(String courseId, String formId, String questionId) {

        // create user and sync the course-user relation
        MockUser prof = Helper.createMockUser("Prof");
        given().header("Authorization", "Bearer " + prof.getJwt()).get("/course").then().statusCode(200);

        // create a websocket client
        try {
            SocketClient client = new SocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" + prof.getId() + "/" + prof.getJwt())
            );
            Thread.sleep(100);
            Assertions.assertTrue(session.isOpen());
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
            Assertions.assertEquals("WAITING", courseService.getCourse(courseId).getFeedbackForms().get(0).getStatus().toString());
            Assertions.assertEquals(0, courseService.getCourse(courseId).getFeedbackForms().get(0).getParticipants().size());

            // connect the students to the feedback form
            Session studentSession1 = ContainerProvider.getWebSocketContainer().connectToServer(
                studentClient,
                URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" + student1.getId() + "/" + student1.getJwt())
            );
            Thread.sleep(100);
            Assertions.assertTrue(studentSession1.isOpen());
            Assertions.assertEquals(1, courseService.getCourse(courseId).getFeedbackForms().get(0).getParticipants().size());

            Session studentSession2 = ContainerProvider.getWebSocketContainer().connectToServer(
                studentClient,
                URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" + student2.getId() + "/" + student2.getJwt())
            );
            Thread.sleep(100);
            Assertions.assertTrue(studentSession2.isOpen());
            Assertions.assertEquals(2, courseService.getCourse(courseId).getFeedbackForms().get(0).getParticipants().size());
            Session studentSession3 = ContainerProvider.getWebSocketContainer().connectToServer(
                studentClient,
                URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" + student3.getId() + "/" + student3.getJwt())
            );
            Thread.sleep(100);
            Assertions.assertTrue(studentSession3.isOpen());
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

            // check via a get request if the participants are still in the feedback form
            Assertions.assertEquals(3, courseService.getCourse(courseId).getFeedbackForms().get(0).getParticipants().size());
            FeedbackForm feedbackFormFromGet = given().header("Authorization", "Bearer " + prof.getJwt()).get("/course/" + courseId + "/feedback/form/" + formId).then().statusCode(200).extract().body().as(FeedbackForm.class);
            Assertions.assertEquals(3, feedbackFormFromGet.getParticipants().size());

        } catch (Exception e) {
            System.out.println(e);
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    public void testAnalytics() {

        // create & get courses
        List<Course> courses = Helper.createCourse("Prof-1");
        Course course = courses.get(0);

        // make 1 prof and 3 students
        MockUser prof = Helper.createMockUser("Prof-1");
        MockUser student1 = Helper.createMockUser("Student-1");
        MockUser student2 = Helper.createMockUser("Student-2");
        MockUser student3 = Helper.createMockUser("Student-3");
        MockUser student4 = Helper.createMockUser("Student-4");

        // call the get courses endpoint for each user to update the course-user relation
        given().header("Authorization", "Bearer " + prof.getJwt()).when().get("/course").then().statusCode(200);
        given().header("Authorization", "Bearer " + student1.getJwt()).when().get("/course").then().statusCode(200);
        given().header("Authorization", "Bearer " + student2.getJwt()).when().get("/course").then().statusCode(200);
        given().header("Authorization", "Bearer " + student3.getJwt()).when().get("/course").then().statusCode(200);
        given().header("Authorization", "Bearer " + student4.getJwt()).when().get("/course").then().statusCode(200);

        // get course and feedback form id
        String courseId = course.getId().toString();
        String formId = course.getFeedbackForms().get(0).getId().toString();

        // try catch block to handle exceptions of websocket connection
        try {

            // create websocket clients
            SocketClient profClient = new SocketClient();
            SocketClient studentClient1 = new SocketClient();
            SocketClient studentClient2 = new SocketClient();
            SocketClient studentClient3 = new SocketClient();
            SocketClient studentClient4 = new SocketClient();

            // connect the prof to the feedback form
            Session profSession = ContainerProvider.getWebSocketContainer().connectToServer(
                profClient,
                URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" + prof.getId() + "/" + prof.getJwt())
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
            Assertions.assertEquals("WAITING", courseService.getCourse(courseId).getFeedbackForms().get(0).getStatus().toString());
            Assertions.assertEquals(0, courseService.getCourse(courseId).getFeedbackForms().get(0).getParticipants().size());

            // connect the students to the feedback form
            Session studentSession1 = ContainerProvider.getWebSocketContainer().connectToServer(
                studentClient1,
                URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" + student1.getId() + "/" + student1.getJwt())
            );
            Thread.sleep(100);
            Assertions.assertTrue(studentSession1.isOpen());
            Assertions.assertEquals(1, courseService.getCourse(courseId).getFeedbackForms().get(0).getParticipants().size());

            Session studentSession2 = ContainerProvider.getWebSocketContainer().connectToServer(
                studentClient2,
                URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" + student2.getId() + "/" + student2.getJwt())
            );
            Thread.sleep(100);
            Assertions.assertTrue(studentSession2.isOpen());
            Assertions.assertEquals(2, courseService.getCourse(courseId).getFeedbackForms().get(0).getParticipants().size());
            Session studentSession3 = ContainerProvider.getWebSocketContainer().connectToServer(
                studentClient3,
                URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" + student3.getId() + "/" + student3.getJwt())
            );
            Thread.sleep(100);
            Assertions.assertTrue(studentSession3.isOpen());
            Assertions.assertEquals(3, courseService.getCourse(courseId).getFeedbackForms().get(0).getParticipants().size());   
            Thread.sleep(100);
            Session studentSession4 = ContainerProvider.getWebSocketContainer().connectToServer(
                studentClient4,
                URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" + student4.getId() + "/" + student4.getJwt())
            );
            Thread.sleep(100);
            Assertions.assertTrue(studentSession4.isOpen());
            Assertions.assertEquals(4, courseService.getCourse(courseId).getFeedbackForms().get(0).getParticipants().size());

            // change the form status to "STARTED" and check if it was set
            profClient.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "STARTED"
                }
            """);
            Thread.sleep(100);
            Assertions.assertEquals("STARTED", courseService.getCourse(courseId).getFeedbackForms().get(0).getStatus().toString());
                        
            // send results to the feedback form (1, 2, 3, 3)
            String questionId = course.getFeedbackForms().get(0).getQuestions().get(0).getId().toString();
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
                    "resultValues": [3]
                }
            """, questionId));
            Thread.sleep(100);

            // close the websocket connections
            profSession.close();
            studentSession1.close();
            studentSession2.close();
            studentSession3.close();

            // check the analytics (avg, min, max, median, count)
            Response response = given()
                .header("Authorization", "Bearer " + prof.getJwt())
                .pathParam("courseId", courseId)
                .pathParam("formId", formId)
                .queryParam("analytics", true)
                .when()
                .get("/course/{courseId}/feedback/form/{formId}");
            FeedbackForm feedbackForm = response
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(FeedbackForm.class);

            Assertions.assertEquals(1, feedbackForm.getQuestions().get(0).getAnalytics().getMin());
            Assertions.assertEquals(3, feedbackForm.getQuestions().get(0).getAnalytics().getMax());
            Assertions.assertEquals(2.5, feedbackForm.getQuestions().get(0).getAnalytics().getMedian());
            Assertions.assertEquals(4, feedbackForm.getQuestions().get(0).getAnalytics().getCount());
            Assertions.assertEquals(2.25, feedbackForm.getQuestions().get(0).getAnalytics().getAvg());


        } catch (Exception e) {
            System.out.println(e);
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    public void testResultDownload() {


        /**
            @Path("/{formId}/downloadresults")
            @GET
            @Produces(MediaType.APPLICATION_OCTET_STREAM)
            @RolesAllowed({ UserRole.STUDENT, UserRole.PROF })
            public Response downloadResults(@RestPath String courseId, @RestPath String formId) {
                Course course = courseRepository.findById(new ObjectId(courseId));
                FeedbackForm feedbackForm = course.getFeedbackFormById(new ObjectId(formId));
                return Response.ok(feedbackForm.getResultsAsCsv(course)).header("Content-Disposition", "attachment; filename=results_" + feedbackForm.name + ".csv").build();
            }
         */
        // create & get courses
        // List<Course> courses = Helper.createCourse("Prof-1");

        // make 1 prof and 3 students
        MockUser prof = Helper.createMockUser("Prof-1");
        MockUser student1 = Helper.createMockUser("Student-1");
        MockUser student2 = Helper.createMockUser("Student-2");
        MockUser student3 = Helper.createMockUser("Student-3");
        MockUser student4 = Helper.createMockUser("Student-4");
        
        // manually create a course to have more questions
        String json = "[{\"name\":\"AUME 23/24\",\"description\":\"Agile Vorgehensmodelle und Mobile Kommunikation\",\"feedbackForms\":[{\"name\":\"Erster Sprint\",\"description\":\"Hier wollen wir Ihr Feedback zum ersten Sprint einholen\",\"questions\":[{\"name\":\"Rolle\",\"description\":\"Wie gut hat Ihnen ihre Pizza gefallen?\",\"type\":\"SLIDER\",\"options\":[],\"key\":\"F-Q-ROLLE\",\"rangeLow\":\"gut\",\"rangeHigh\":\"schlecht\"},{\"name\":\"Test\",\"description\":\"Wie gut hat Ihnen ihre Pizza gefallen?\",\"type\":\"SLIDER\",\"options\":[],\"key\":\"F-Q-TEST\",\"rangeLow\":\"gut\",\"rangeHigh\":\"schlecht\"},{\"name\":\"Test2\",\"description\":\"Wie gut hat Ihnen ihre Tests gefallen?\",\"type\":\"SLIDER\",\"options\":[],\"key\":\"F-Q-TEST2\",\"rangeLow\":\"gut\",\"rangeHigh\":\"schlecht\"}],\"key\":\"F-ERSTERSPRINT\"}],\"quizForms\":[{\"name\":\"Rollenverständnis bei Scrum\",\"description\":\"Ein Quiz zum Rollenverständnis und Teamaufbau bei Scrum\",\"questions\":[{\"name\":\"Product Owner\",\"description\":\"Welche der folgenden Aufgaben ist nicht Teil der Rolle des Product Owners?\",\"type\":\"SINGLE_CHOICE\",\"options\":[\"Erstellung des Product Backlogs\",\"Priorisierung des Product Backlogs\",\"Pizza bestellen für jedes Daily\"],\"hasCorrectAnswers\":true,\"correctAnswers\":[\"2\"],\"key\":\"Q-Q-PRODUCTOWNER\"},{\"name\":\"Product Owner\",\"description\":\"Test Frage?\",\"type\":\"SINGLE_CHOICE\",\"options\":[\"Antwort 1\",\"Antwort 2\",\"Antwort 3\"],\"hasCorrectAnswers\":true,\"correctAnswers\":[\"2\"],\"key\":\"Q-Q-PRODUCTOWNER\"}],\"key\":\"Q-ROLES\"}],\"key\":\"AUME23\",\"moodleCourseId\":\"1\"}]";
        Response response = given().accept(ContentType.JSON).contentType(ContentType.JSON).header("Authorization", "Bearer " + prof.getJwt()).body(json).patch("/public/courses/");
        List<Course> courses = response.then().statusCode(200).extract().body().jsonPath().getList(".", Course.class);
        Course course = courses.get(0);

        // call the get courses endpoint for each user to update the course-user relation
        given().header("Authorization", "Bearer " + prof.getJwt()).when().get("/course").then().statusCode(200);
        given().header("Authorization", "Bearer " + student1.getJwt()).when().get("/course").then().statusCode(200);
        given().header("Authorization", "Bearer " + student2.getJwt()).when().get("/course").then().statusCode(200);
        given().header("Authorization", "Bearer " + student3.getJwt()).when().get("/course").then().statusCode(200);
        given().header("Authorization", "Bearer " + student4.getJwt()).when().get("/course").then().statusCode(200);

        // get course and feedback form id
        String courseId = course.getId().toString();
        String formId = course.getFeedbackForms().get(0).getId().toString();

        // try catch block to handle exceptions of websocket connection
        try {

            // create websocket clients
            SocketClient profClient = new SocketClient();
            SocketClient studentClient1 = new SocketClient();
            SocketClient studentClient2 = new SocketClient();
            SocketClient studentClient3 = new SocketClient();
            SocketClient studentClient4 = new SocketClient();

            // connect the prof to the feedback form
            Session profSession = ContainerProvider.getWebSocketContainer().connectToServer(
                profClient,
                URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" + prof.getId() + "/" + prof.getJwt())
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
            Assertions.assertEquals("WAITING", courseService.getCourse(courseId).getFeedbackForms().get(0).getStatus().toString());
            Assertions.assertEquals(0, courseService.getCourse(courseId).getFeedbackForms().get(0).getParticipants().size());

            // connect the students to the feedback form
            Session studentSession1 = ContainerProvider.getWebSocketContainer().connectToServer(
                studentClient1,
                URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" + student1.getId() + "/" + student1.getJwt())
            );
            Thread.sleep(100);
            Assertions.assertTrue(studentSession1.isOpen());
            Assertions.assertEquals(1, courseService.getCourse(courseId).getFeedbackForms().get(0).getParticipants().size());

            Session studentSession2 = ContainerProvider.getWebSocketContainer().connectToServer(
                studentClient2,
                URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" + student2.getId() + "/" + student2.getJwt())
            );
            Thread.sleep(100);
            Assertions.assertTrue(studentSession2.isOpen());
            Assertions.assertEquals(2, courseService.getCourse(courseId).getFeedbackForms().get(0).getParticipants().size());
            Session studentSession3 = ContainerProvider.getWebSocketContainer().connectToServer(
                studentClient3,
                URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" + student3.getId() + "/" + student3.getJwt())
            );
            Thread.sleep(100);
            Assertions.assertTrue(studentSession3.isOpen());
            Assertions.assertEquals(3, courseService.getCourse(courseId).getFeedbackForms().get(0).getParticipants().size());   
            Thread.sleep(100);
            Session studentSession4 = ContainerProvider.getWebSocketContainer().connectToServer(
                studentClient4,
                URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" + student4.getId() + "/" + student4.getJwt())
            );
            Thread.sleep(100);
            Assertions.assertTrue(studentSession4.isOpen());
            Assertions.assertEquals(4, courseService.getCourse(courseId).getFeedbackForms().get(0).getParticipants().size());

            // change the form status to "STARTED" and check if it was set
            profClient.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "STARTED"
                }
            """);
            Thread.sleep(100);
            Assertions.assertEquals("STARTED", courseService.getCourse(courseId).getFeedbackForms().get(0).getStatus().toString());
                        
            // send results to the feedback form (1, 2, 3, 3)
            String questionId = course.getFeedbackForms().get(0).getQuestions().get(0).getId().toString();
            String questionId3 = course.getFeedbackForms().get(0).getQuestions().get(2).getId().toString();
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

            // close the websocket connections
            profSession.close();
            studentSession1.close();
            studentSession2.close();
            studentSession3.close();

            // check manually if the results are correct
            Course updatedCourse = courseService.getCourse(courseId);
            List<Result> results = updatedCourse.getFeedbackForms().get(0).getQuestions().get(0).getResults();
            Assertions.assertEquals(4, results.size());
            Assertions.assertEquals("1", results.get(0).values.get(0));
            Assertions.assertEquals("2", results.get(1).values.get(0));
            Assertions.assertEquals("3", results.get(2).values.get(0));
            Assertions.assertEquals("3", results.get(3).values.get(0));

            // fetch the download link
            Response downloadResponse = given()
                .header("Authorization", "Bearer " + prof.getJwt())
                .pathParam("courseId", courseId)
                .pathParam("formId", formId)
                .when()
                .get("/course/{courseId}/feedback/form/{formId}/downloadresults");
            downloadResponse.then().statusCode(200);

            // check if the downloadResponse is a csv file
            Assertions.assertEquals("attachment; filename=results_Erster Sprint.csv", downloadResponse.getHeader("Content-Disposition"));
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

}
