package de.htwg_konstanz.mobilelearning.services.stats;

import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import de.htwg_konstanz.mobilelearning.SocketClient;
import de.htwg_konstanz.mobilelearning.MockMongoTestProfile;
import de.htwg_konstanz.mobilelearning.MockUser;
import de.htwg_konstanz.mobilelearning.models.Course;
import de.htwg_konstanz.mobilelearning.models.stats.Stats;
import de.htwg_konstanz.mobilelearning.services.CourseService;
import de.htwg_konstanz.mobilelearning.services.StatsService;
import de.htwg_konstanz.mobilelearning.services.auth.UserService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Session;
import de.htwg_konstanz.mobilelearning.Helper;

@QuarkusTest
@TestProfile(MockMongoTestProfile.class)
public class StatsServiceTest {

    @Inject
    private CourseService courseService;

    @Inject
    private UserService userService;

    @Inject
    private StatsService statsService;

    @BeforeEach
    void init(TestInfo testInfo) {
        System.out.println("------------------------------");
        System.out.println("Test: " + testInfo.getDisplayName());
        courseService.deleteAllCourses();
        userService.deleteAllUsers();
        statsService.resetGlobalStats();
    }

    @Test
    public void testUserAndGlobalStatsWithQuiz() {
        courseService.deleteAllCourses();
        userService.deleteAllUsers();

        // create a course with the prof user
        List<Course> courses = Helper.createCourse("Prof");
        String courseId = courses.get(0).getId().toString();
        String formId = courses.get(0).getQuizForms().get(0).getId().toString();
        MockUser prof = Helper.createMockUser("Prof");

        // create 2 students
        MockUser student1 = Helper.createMockUser("Student1");
        MockUser student2 = Helper.createMockUser("Student2");

        // fetch the students courses to sync the courses
        given().header("Authorization", "Bearer " + student1.getJwt()).get("/course").then().statusCode(200);
        given().header("Authorization", "Bearer " + student2.getJwt()).get("/course").then().statusCode(200);

        // participate in the quiz
        given()
                .header("Authorization", "Bearer " + student1.getJwt())
                .pathParam("courseId", courseId)
                .pathParam("formId", formId)
                .body("Student1")
                .when()
                .post("/course/{courseId}/quiz/form/{formId}/participate")
                .then()
                .statusCode(200)
                .body(is("Successfully added"));

        given()
                .header("Authorization", "Bearer " + student2.getJwt())
                .pathParam("courseId", courseId)
                .pathParam("formId", formId)
                .body("Student2")
                .when()
                .post("/course/{courseId}/quiz/form/{formId}/participate")
                .then()
                .statusCode(200)
                .body(is("Successfully added"));

        // create a websocket client for the prof and the students
        SocketClient profClient = new SocketClient();
        SocketClient student1Client = new SocketClient();
        SocketClient student2Client = new SocketClient();

        try {
            // connect to the websocket
            Session profSession = ContainerProvider.getWebSocketContainer().connectToServer(
                    profClient,
                    URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId
                            + "/subscribe/"
                            + prof.getId() + "/" + prof.getJwt()));
            Thread.sleep(100);
            Assertions.assertNotNull(profSession);
            Thread.sleep(100);
            Assertions.assertTrue(profSession.isOpen());
            Session student1Session = ContainerProvider.getWebSocketContainer().connectToServer(
                    student1Client,
                    URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId
                            + "/subscribe/"
                            + student1.getId() + "/" + student1.getJwt()));
            Thread.sleep(100);
            Assertions.assertNotNull(student1Session);
            Thread.sleep(100);
            Assertions.assertTrue(student1Session.isOpen());
            Session student2Session = ContainerProvider.getWebSocketContainer().connectToServer(
                    student2Client,
                    URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId
                            + "/subscribe/"
                            + student2.getId() + "/" + student2.getJwt()));
            Thread.sleep(100);
            Assertions.assertNotNull(student2Session);
            Thread.sleep(100);
            Assertions.assertTrue(student2Session.isOpen());
            Thread.sleep(100);

            // start the quiz
            profClient.sendMessage(String.format("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "STARTED",
                    "roles": ["PROF"]
                }
            """));

            Thread.sleep(100);

            Assertions.assertTrue(courseService.getCourse(courseId).getQuizForms().get(0).getStatus().toString().equals("STARTED"));
            Assertions.assertTrue(courseService.getCourse(courseId).getQuizForms().get(0).getParticipants().size() == 2);

            // add a result for student 1 (correct)
            student1Client.sendMessage(String.format("""
                {
                    "action": "ADD_RESULT",
                    "resultElementId": %s,
                    "resultValues": ["2"]
                }
            """, courses.get(0).getQuizForms().get(0).questions.get(0).getId().toString()));

            Thread.sleep(100);

            // add a result for student 2 (incorrect)
            student2Client.sendMessage(String.format("""
                {
                    "action": "ADD_RESULT",
                    "resultElementId": %s,
                    "resultValues": ["1"]
                }
            """, courses.get(0).getQuizForms().get(0).questions.get(0).getId().toString()));

            Thread.sleep(100);

            // complete the quiz
            profClient.sendMessage(String.format("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "FINISHED"
                }
            """));

            Thread.sleep(100);

            // get the user stats
            Response response = given()
                    .header("Authorization", "Bearer " + student1.getJwt())
                    .when()
                    .get("/stats");
            Stats student1Stats = response
                    .then()
                    .statusCode(200)
                    .extract()
                    .body()
                    .as(Stats.class);
            response = given()
                    .header("Authorization", "Bearer " + student2.getJwt())
                    .when()
                    .get("/stats");
            Stats student2Stats = response
                    .then()
                    .statusCode(200)
                    .extract()
                    .body()
                    .as(Stats.class);

            // check the user stats
            Assertions.assertEquals(1, student1Stats.userStats.getCompletedQuizForms());
            Assertions.assertEquals(1, student1Stats.userStats.getQainedQuizPoints());
            Assertions.assertEquals(1.0, student1Stats.userStats.getAvgQuizPosition());

            Assertions.assertEquals(1, student2Stats.userStats.getCompletedQuizForms());
            Assertions.assertEquals(0, student2Stats.userStats.getQainedQuizPoints());
            Assertions.assertEquals(2.0, student2Stats.userStats.getAvgQuizPosition());

            // reset the quiz
            profClient.sendMessage(String.format("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "NOT_STARTED",
                    "roles": ["PROF"]
                }
            """));

            Thread.sleep(100);

            // close the websocket
            profSession.close();
            student1Session.close();
            student2Session.close();
        } catch (Exception e) {
            System.out.println(e);
            Assertions.fail(e.getMessage());
        }

        // participate in the quiz
        given()
                .header("Authorization", "Bearer " + student1.getJwt())
                .pathParam("courseId", courseId)
                .pathParam("formId", formId)
                .body("Student1")
                .when()
                .post("/course/{courseId}/quiz/form/{formId}/participate")
                .then()
                .statusCode(200)
                .body(is("Successfully added"));

        given()
                .header("Authorization", "Bearer " + student2.getJwt())
                .pathParam("courseId", courseId)
                .pathParam("formId", formId)
                .body("Student2")
                .when()
                .post("/course/{courseId}/quiz/form/{formId}/participate")
                .then()
                .statusCode(200)
                .body(is("Successfully added"));

        try {
            // connect to the websocket
            Session profSession = ContainerProvider.getWebSocketContainer().connectToServer(
                    profClient,
                    URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId
                            + "/subscribe/"
                            + prof.getId() + "/" + prof.getJwt()));
            Assertions.assertNotNull(profSession);
            Thread.sleep(100);
            Assertions.assertTrue(profSession.isOpen());
            Thread.sleep(100);
            Session student1Session = ContainerProvider.getWebSocketContainer().connectToServer(
                    student1Client,
                    URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId
                            + "/subscribe/"
                            + student1.getId() + "/" + student1.getJwt()));
            Assertions.assertNotNull(student1Session);
            Thread.sleep(100);
            Assertions.assertTrue(student1Session.isOpen());
            Thread.sleep(100);
            Session student2Session = ContainerProvider.getWebSocketContainer().connectToServer(
                    student2Client,
                    URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId
                            + "/subscribe/"
                            + student2.getId() + "/" + student2.getJwt()));
            Assertions.assertNotNull(student2Session);
            Thread.sleep(100);
            Assertions.assertTrue(student2Session.isOpen());

            Thread.sleep(100);

            // start the quiz
            profClient.sendMessage(String.format("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "STARTED",
                    "roles": ["PROF"]
                }
            """));

            Thread.sleep(100);

            Assertions.assertTrue(courseService.getCourse(courseId).getQuizForms().get(0).getStatus().toString().equals("STARTED"));
            Assertions.assertTrue(courseService.getCourse(courseId).getQuizForms().get(0).getParticipants().size() == 2);

            // add a result for student 1 (correct)
            student1Client.sendMessage(String.format("""
                {
                    "action": "ADD_RESULT",
                    "resultElementId": %s,
                    "resultValues": ["1"]
                }
            """, courses.get(0).getQuizForms().get(0).questions.get(0).getId().toString()));

            Thread.sleep(100);

            // add a result for student 2 (incorrect)
            student2Client.sendMessage(String.format("""
                {
                    "action": "ADD_RESULT",
                    "resultElementId": %s,
                    "resultValues": ["2"]
                }
            """, courses.get(0).getQuizForms().get(0).questions.get(0).getId().toString()));

            Thread.sleep(100);

            // complete the quiz
            profClient.sendMessage(String.format("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "FINISHED"
                }
            """));

            Thread.sleep(100);

            // get the user stats
            Response response = given()
                    .header("Authorization", "Bearer " + student1.getJwt())
                    .when()
                    .get("/stats");
            Stats student1Stats = response
                    .then()
                    .statusCode(200)
                    .extract()
                    .body()
                    .as(Stats.class);
            response = given()
                    .header("Authorization", "Bearer " + student2.getJwt())
                    .when()
                    .get("/stats");
            Stats student2Stats = response
                    .then()
                    .statusCode(200)
                    .extract()
                    .body()
                    .as(Stats.class);

            // check the user stats
            Assertions.assertEquals(2, student1Stats.userStats.getCompletedQuizForms());
            Assertions.assertEquals(1, student1Stats.userStats.getQainedQuizPoints());
            Assertions.assertEquals(1.5, student1Stats.userStats.getAvgQuizPosition());

            Assertions.assertEquals(2, student2Stats.userStats.getCompletedQuizForms());
            Assertions.assertEquals(1, student2Stats.userStats.getQainedQuizPoints());
            Assertions.assertEquals(1.5, student2Stats.userStats.getAvgQuizPosition());

            // reset the quiz
            profClient.sendMessage(String.format("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "NOT_STARTED",
                    "roles": ["PROF"]
                }
            """));

            Thread.sleep(100);

            // close the websocket
            profSession.close();
            student1Session.close();
            student2Session.close();
        } catch (Exception e) {
            System.out.println(e);
            Assertions.fail(e.getMessage());
        }

        // participate in the quiz
        given()
                .header("Authorization", "Bearer " + student1.getJwt())
                .pathParam("courseId", courseId)
                .pathParam("formId", formId)
                .body("Student1")
                .when()
                .post("/course/{courseId}/quiz/form/{formId}/participate")
                .then()
                .statusCode(200)
                .body(is("Successfully added"));

        given()
                .header("Authorization", "Bearer " + student2.getJwt())
                .pathParam("courseId", courseId)
                .pathParam("formId", formId)
                .body("Student2")
                .when()
                .post("/course/{courseId}/quiz/form/{formId}/participate")
                .then()
                .statusCode(200)
                .body(is("Successfully added"));

        try {
            // connect to the websocket
            Session profSession = ContainerProvider.getWebSocketContainer().connectToServer(
                    profClient,
                    URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId
                            + "/subscribe/"
                            + prof.getId() + "/" + prof.getJwt()));
            Assertions.assertNotNull(profSession);
            Thread.sleep(100);
            Assertions.assertTrue(profSession.isOpen());
            Thread.sleep(100);
            Session student1Session = ContainerProvider.getWebSocketContainer().connectToServer(
                    student1Client,
                    URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId
                            + "/subscribe/"
                            + student1.getId() + "/" + student1.getJwt()));
            Assertions.assertNotNull(student1Session);
            Thread.sleep(100);
            Assertions.assertTrue(student1Session.isOpen());
            Thread.sleep(100);
            Session student2Session = ContainerProvider.getWebSocketContainer().connectToServer(
                    student2Client,
                    URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId
                            + "/subscribe/"
                            + student2.getId() + "/" + student2.getJwt()));
            Assertions.assertNotNull(student2Session);
            Thread.sleep(100);
            Assertions.assertTrue(student2Session.isOpen());
            Thread.sleep(100);

            // start the quiz
            profClient.sendMessage(String.format("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "STARTED",
                    "roles": ["PROF"]
                }
            """));

            Thread.sleep(100);

            Assertions.assertTrue(courseService.getCourse(courseId).getQuizForms().get(0).getStatus().toString().equals("STARTED"));
            Assertions.assertTrue(courseService.getCourse(courseId).getQuizForms().get(0).getParticipants().size() == 2);

            // add a result for student 1 (correct)
            student1Client.sendMessage(String.format("""
                {
                    "action": "ADD_RESULT",
                    "resultElementId": %s,
                    "resultValues": ["1"]
                }
            """, courses.get(0).getQuizForms().get(0).questions.get(0).getId().toString()));

            Thread.sleep(100);

            // add a result for student 2 (incorrect)
            student2Client.sendMessage(String.format("""
                {
                    "action": "ADD_RESULT",
                    "resultElementId": %s,
                    "resultValues": ["2"]
                }
            """, courses.get(0).getQuizForms().get(0).questions.get(0).getId().toString()));

            Thread.sleep(100);

            // complete the quiz
            profClient.sendMessage(String.format("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "FINISHED"
                }
            """));

            Thread.sleep(100);

            // get the user stats
            Response response = given()
                    .header("Authorization", "Bearer " + student1.getJwt())
                    .when()
                    .get("/stats");
            Stats student1Stats = response
                    .then()
                    .statusCode(200)
                    .extract()
                    .body()
                    .as(Stats.class);
            response = given()
                    .header("Authorization", "Bearer " + student2.getJwt())
                    .when()
                    .get("/stats");
            Stats student2Stats = response
                    .then()
                    .statusCode(200)
                    .extract()
                    .body()
                    .as(Stats.class);

            // check the user stats
            Assertions.assertEquals(3, student1Stats.userStats.getCompletedQuizForms());
            Assertions.assertEquals(1, student1Stats.userStats.getQainedQuizPoints());
            Assertions.assertEquals(5.0 / 3.0, student1Stats.userStats.getAvgQuizPosition());

            Assertions.assertEquals(3, student2Stats.userStats.getCompletedQuizForms());
            Assertions.assertEquals(2, student2Stats.userStats.getQainedQuizPoints());
            Assertions.assertEquals(4.0 / 3.0, student2Stats.userStats.getAvgQuizPosition());

            // reset the quiz
            profClient.sendMessage(String.format("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "NOT_STARTED",
                    "roles": ["PROF"]
                }
            """));

            Thread.sleep(100);

            // close the websocket
            profSession.close();
            student1Session.close();
            student2Session.close();

            // check the global stats
            Assertions.assertEquals(3, student1Stats.globalStats.getCompletedQuizForms());
            Assertions.assertEquals(0, student1Stats.globalStats.getCompletedFeedbackForms());

        } catch (Exception e) {
            System.out.println(e);
            Assertions.fail(e.getMessage());
        }

    }

    @Test
    public void testUserAndGlobalStatsWithFeedback() {
        courseService.deleteAllCourses();
        userService.deleteAllUsers();

        // create a course with the prof user
        List<Course> courses = Helper.createCourse("Prof");
        String courseId = courses.get(0).getId().toString();
        String formId = courses.get(0).getFeedbackForms().get(0).getId().toString();
        MockUser prof = Helper.createMockUser("Prof");

        // create 2 students
        MockUser student1 = Helper.createMockUser("Student1");
        MockUser student2 = Helper.createMockUser("Student2");

        // fetch the students courses to sync the courses
        given().header("Authorization", "Bearer " + student1.getJwt()).get("/course").then().statusCode(200);
        given().header("Authorization", "Bearer " + student2.getJwt()).get("/course").then().statusCode(200);

        // create a websocket client for the prof and the students
        SocketClient profClient = new SocketClient();
        SocketClient student1Client = new SocketClient();
        SocketClient student2Client = new SocketClient();

        try {
            // connect to the websocket
            Session profSession = ContainerProvider.getWebSocketContainer().connectToServer(
                    profClient,
                    URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId
                            + "/subscribe/"
                            + prof.getId() + "/" + prof.getJwt()));
            Assertions.assertNotNull(profSession);
            Thread.sleep(100);
            Assertions.assertTrue(profSession.isOpen());
            Thread.sleep(100);
            Session student1Session = ContainerProvider.getWebSocketContainer().connectToServer(
                    student1Client,
                    URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId
                            + "/subscribe/"
                            + student1.getId() + "/" + student1.getJwt()));
            Assertions.assertNotNull(student1Session);
            Thread.sleep(100);
            Assertions.assertTrue(student1Session.isOpen());
            Thread.sleep(100);
            Session student2Session = ContainerProvider.getWebSocketContainer().connectToServer(
                    student2Client,
                    URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId
                            + "/subscribe/"
                            + student2.getId() + "/" + student2.getJwt()));
            Assertions.assertNotNull(student2Session);
            Thread.sleep(100);
            Assertions.assertTrue(student2Session.isOpen());
            Thread.sleep(100);

            // start the feedback
            profClient.sendMessage(String.format("""
                        {
                            "action": "CHANGE_FORM_STATUS",
                            "formStatus": "STARTED"
                        }
                    """));

            Thread.sleep(100);

            Assertions.assertTrue(courseService.getCourse(courseId).getFeedbackForms().get(0).getStatus().toString().equals("STARTED"));
            Assertions.assertEquals(2, courseService.getCourse(courseId).getFeedbackForms().get(0).getParticipants().size());

            // add a result for student 1
            student1Client.sendMessage(String.format("""
                        {
                            "action": "ADD_RESULT",
                            "resultElementId": %s,
                            "resultValues": ["2"]
                        }
                    """, courses.get(0).getFeedbackForms().get(0).questions.get(0).getId().toString()));

            Thread.sleep(100);

            // add a result for student 2
            student2Client.sendMessage(String.format("""
                        {
                            "action": "ADD_RESULT",
                            "resultElementId": %s,
                            "resultValues": ["1"]
                        }
                    """, courses.get(0).getFeedbackForms().get(0).questions.get(0).getId().toString()));

            Thread.sleep(100);

            // complete the feedback
            profClient.sendMessage(String.format("""
                        {
                            "action": "CHANGE_FORM_STATUS",
                            "formStatus": "FINISHED"
                        }
                    """));

            Thread.sleep(100);

            // get the user stats
            Response response = given()
                    .header("Authorization", "Bearer " + student1.getJwt())
                    .when()
                    .get("/stats");
            Stats student1Stats = response
                    .then()
                    .statusCode(200)
                    .extract()
                    .body()
                    .as(Stats.class);
            response = given()
                    .header("Authorization", "Bearer " + student2.getJwt())
                    .when()
                    .get("/stats");
            Stats student2Stats = response
                    .then()
                    .statusCode(200)
                    .extract()
                    .body()
                    .as(Stats.class);

            // check the user stats
            Assertions.assertEquals(1, student1Stats.userStats.getCompletedFeedbackForms());
            Assertions.assertEquals(0, student1Stats.userStats.getCompletedQuizForms());
            Assertions.assertEquals(0, student1Stats.userStats.getQainedQuizPoints());
            Assertions.assertEquals(0.0, student1Stats.userStats.getAvgQuizPosition());

            Assertions.assertEquals(1, student2Stats.userStats.getCompletedFeedbackForms());
            Assertions.assertEquals(0, student2Stats.userStats.getCompletedQuizForms());
            Assertions.assertEquals(0, student2Stats.userStats.getQainedQuizPoints());
            Assertions.assertEquals(0.0, student2Stats.userStats.getAvgQuizPosition());

            // reset the feedback
            profClient.sendMessage(String.format("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "NOT_STARTED"
                }
            """));

            Thread.sleep(100);

            // close the websocket
            profSession.close();
            student1Session.close();
            student2Session.close();
        } catch (Exception e) {
            System.out.println(e);
            Assertions.fail(e.getMessage());
        }
    }

}
