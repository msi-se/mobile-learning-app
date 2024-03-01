package de.htwg_konstanz.mobilelearning.services.feedback;

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
import de.htwg_konstanz.mobilelearning.models.Course;
import de.htwg_konstanz.mobilelearning.models.feedback.FeedbackForm;
import de.htwg_konstanz.mobilelearning.services.CourseService;
import de.htwg_konstanz.mobilelearning.services.auth.UserService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Session;

@QuarkusTest
@TestProfile(MockMongoTestProfile.class)
public class FeedbackFormServiceTest {

    @Inject
    private CourseService courseService;    

    @Inject
    private UserService userService;

    @BeforeEach
    void init(TestInfo testInfo){
        System.out.println("------------------------------");
        System.out.println("Test: " + testInfo.getDisplayName());
        courseService.deleteAllCourses();
        userService.deleteAllUsers();
    }

    @Test
    public void getFeedbackFormWithoutResult() {
        //create & get courses + ids
        List<Course> courses = Helper.createCourse();
        String courseId = courses.getFirst().getId().toString();
        String formId = courses.getFirst().getFeedbackForms().get(0).getId().toString();
        String questionId = courses.getFirst().feedbackForms.get(0).questions.get(0).getId().toString();
        
        // add a result & get feedback forms
        addResult(courseId, formId, questionId);
        
        // Assert get feedback form without results
        Response response = given()
                            .header("Authorization", "Bearer " + Helper.createMockUser("Prof").getJwt())
                            .pathParam("courseId", courseId)
                            .pathParam("formId", formId)
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
        Assertions.assertEquals(0, feedbackFormFromService.questions.get(0).results.size());
    }
    
    @Test
    public void getFeedbackFormWithResult() {
        //create & get courses + ids
        List<Course> courses = Helper.createCourse();
        String courseId = courses.getFirst().getId().toString();
        String formId = courses.getFirst().getFeedbackForms().get(0).getId().toString();
        String questionId = courses.getFirst().feedbackForms.get(0).questions.get(0).getId().toString();
        // add a result & get feedback forms
        addResult(courseId, formId, questionId);
                // Assert get feedback form without results
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
        Assertions.assertEquals("5", feedbackFormFromService.questions.get(0).results.get(0).values.get(0));
    }

    @Test
    public void clearResults() {
        //create & get courses + ids
        List<Course> courses = Helper.createCourse();
        String courseId = courses.getFirst().getId().toString();
        String formId = courses.getFirst().getFeedbackForms().get(0).getId().toString();
        String questionId = courses.getFirst().feedbackForms.get(0).questions.get(0).getId().toString();
        // add a result & get feedback forms
        addResult(courseId, formId, questionId);
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

        // Assert that results were cleared
        Assertions.assertEquals(1, feedbackForm.questions.get(0).results.size());
        response = given()
                    .header("Authorization", "Bearer " + Helper.createMockUser("Prof").getJwt())
                    .pathParam("courseId", courseId)
                    .pathParam("formId", formId)
                    .queryParam("results", true)
                    .when()
                    .get("/course/{courseId}/feedback/form/{formId}/clearresults");
        FeedbackForm feedbackFormCleared = response
                                       .then()
                                       .statusCode(200)
                                       .extract()
                                       .body()
                                       .as(FeedbackForm.class);
        Assertions.assertEquals(0, feedbackFormCleared.questions.get(0).results.size());
    }    
    
    @Test
    public void clearResultsNotOwner() {
        //create & get courses + ids
        List<Course> courses = Helper.createCourse();
        String courseId = courses.getFirst().getId().toString();
        String formId = courses.getFirst().getFeedbackForms().get(0).getId().toString();
        String questionId = courses.getFirst().feedbackForms.get(0).questions.get(0).getId().toString();
        // add a result & get feedback forms
        addResult(courseId, formId, questionId);
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

        // Assert that results were not cleared (not owner)
        Assertions.assertEquals(1, feedbackForm.questions.get(0).results.size());
        Response responseProf2 = given()
                                    .header("Authorization", "Bearer " + Helper.createMockUser("Prof2").getJwt())
                                    .pathParam("courseId", courseId)
                                    .pathParam("formId", formId)
                                    .queryParam("results", true)
                                    .when()
                                    .get("/course/{courseId}/feedback/form/{formId}/clearresults");

        responseProf2.then()
                    .statusCode(204);

        feedbackForm = response.then()
                                .statusCode(200)
                                .extract()
                                .body()
                                .as(FeedbackForm.class);

        Assertions.assertEquals(1, feedbackForm.questions.get(0).results.size());
    }

    @Test
    public void clearResultsForbidden() {
        //create & get courses + ids
        List<Course> courses = Helper.createCourse();
        String courseId = courses.getFirst().getId().toString();
        String formId = courses.getFirst().getFeedbackForms().get(0).getId().toString();
       
            Response responseStudent = given()
                                        .header("Authorization", "Bearer " + Helper.createMockUser("Student").getJwt())
                                        .pathParam("courseId", courseId)
                                        .pathParam("formId", formId)
                                        .queryParam("results", true)
                                        .when()
                                        .get("/course/{courseId}/feedback/form/{formId}/clearresults");
            responseStudent.then()
                            .statusCode(403);
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
}
