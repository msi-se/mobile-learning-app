package de.htwg_konstanz.mobilelearning.services.quiz;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import de.htwg_konstanz.mobilelearning.LiveFeedbackSocketClient;
import de.htwg_konstanz.mobilelearning.MockMongoTestProfile;
import de.htwg_konstanz.mobilelearning.MockUser;
import de.htwg_konstanz.mobilelearning.models.Course;
import de.htwg_konstanz.mobilelearning.models.auth.UserRole;
import de.htwg_konstanz.mobilelearning.models.quiz.QuizForm;
import de.htwg_konstanz.mobilelearning.models.stats.Stats;
import de.htwg_konstanz.mobilelearning.services.CourseService;
import de.htwg_konstanz.mobilelearning.services.api.ApiService;
import de.htwg_konstanz.mobilelearning.services.auth.UserService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Session;
import de.htwg_konstanz.mobilelearning.Helper;

@QuarkusTest
@TestProfile(MockMongoTestProfile.class)
public class QuizFormServiceTest {

    @Inject
    private CourseService courseService;

    @Inject
    private ApiService apiService;

    @Inject
    private UserService userService;

    @Inject
    private QuizFormService quizFormService;

    @BeforeEach
    void init(TestInfo testInfo) {
        // System.out.println("------------------------------");
        // System.out.println("Test: " + testInfo.getDisplayName());
        // courseService.deleteAllCourses();
    }

    @Test
    public void testMockUser() {
        MockUser mockUser = Helper.createMockUser("Prof1");
        Assertions.assertTrue(mockUser.getJwt().length() > 0);
        Assertions.assertTrue(mockUser.getId().length() > 0);
    }

    @Test
    public void testCreateCourse() {
        List<Course> courses = Helper.createCourse();
        Assertions.assertEquals(1, courses.size());
        Assertions.assertEquals("AUME 23/24", courses.get(0).getName());
        Assertions.assertEquals("Agile Vorgehensmodelle und Mobile Kommunikation", courses.get(0).getDescription());
        Assertions.assertEquals(1, courses.get(0).getFeedbackForms().size());
        Assertions.assertEquals(1, courses.get(0).getQuizForms().size());
    }

    @Test
    @TestSecurity(user = "Prof", roles = { UserRole.PROF })
    @JwtSecurity(claims = { @Claim(key = "email", value = "prof@htwg-konstanz.de") })
    public void getQuizFormWithoutResult() {
        // create & get courses + ids
        List<Course> courses = Helper.createCourse();
        String courseId = courses.getFirst().getId().toString();
        String formId = courses.getFirst().getQuizForms().get(0).getId().toString();
        String questionId = courses.getFirst().quizForms.get(0).questions.get(0).getId().toString();

        // add a result & get quiz forms
        addResult(courseId, formId, questionId, "Prof");
        List<QuizForm> quizForms = quizFormService.getQuizForms(courses.get(0).id.toString());

        // Assert get quiz form without results
        QuizForm quizFormFromService = quizFormService.getQuizForm(courses.get(0).id.toString(),
                quizForms.get(0).id.toString(), false);
        Assertions.assertEquals("Rollenverständnis bei Scrum",
                quizFormFromService.name);
        Assertions.assertEquals("Ein Quiz zum Rollenverständnis und Teamaufbau bei Scrum",
                quizFormFromService.description);
        Assertions.assertEquals(1, quizFormFromService.questions.size());
        Assertions.assertEquals(0,
                quizFormFromService.questions.get(0).results.size());
    }

    @Test
    @TestSecurity(user = "Prof", roles = { UserRole.PROF })
    @JwtSecurity(claims = { @Claim(key = "email", value = "prof@htwg-konstanz.de") })
    public void getQuizFormWithResult() {
        // create & get courses + ids
        List<Course> courses = Helper.createCourse();
        String courseId = courses.getFirst().getId().toString();
        String formId = courses.getFirst().getQuizForms().get(0).getId().toString();
        String questionId = courses.getFirst().quizForms.get(0).questions.get(0).getId().toString();

        // add a result & get quiz forms
        addResult(courseId, formId, questionId, "Prof");
        List<QuizForm> quizForms = quizFormService.getQuizForms(courses.get(0).id.toString());

        // Assert get quiz form without results
        QuizForm quizFormFromService = quizFormService.getQuizForm(courses.get(0).id.toString(),
                quizForms.get(0).id.toString(), true);
        Assertions.assertEquals("Rollenverständnis bei Scrum",
                quizFormFromService.name);
        Assertions.assertEquals("Ein Quiz zum Rollenverständnis und Teamaufbau bei Scrum",
                quizFormFromService.description);
        Assertions.assertEquals(1, quizFormFromService.questions.size());
        Assertions.assertEquals(1,
                quizFormFromService.questions.get(0).results.size());
        Assertions.assertEquals("1",
                quizFormFromService.questions.get(0).results.get(0).values.get(0));
    }

    @Test
    public void participateUniqueAlias() {
        // create & get courses + ids
        List<Course> courses = Helper.createCourse();
        String courseId = courses.getFirst().getId().toString();
        String formId = courses.getFirst().getQuizForms().get(0).getId().toString();

        // Check successful RestResponse
        given().header("Authorization", "Bearer " + Helper.createMockUser("Student1").getJwt())
                .pathParam("courseId", courseId)
                .pathParam("formId", formId)
                .body("alias")
                .when()
                .post("/course/{courseId}/quiz/form/{formId}/participate")
                .then()
                .statusCode(200)
                .body(is("Successfully added"));

        // 200 if alias already taken by the same user
        given().header("Authorization", "Bearer " + Helper.createMockUser("Student1").getJwt())
                .pathParam("courseId", courseId)
                .pathParam("formId", formId)
                .body("alias")
                .when()
                .post("/course/{courseId}/quiz/form/{formId}/participate")
                .then()
                .statusCode(200)
                .body(is("Successfully added"));

        // 409 if alias already taken by another user
        given().header("Authorization", "Bearer " + Helper.createMockUser("Student2").getJwt())
                .pathParam("courseId", courseId)
                .pathParam("formId", formId)
                .body("alias")
                .when()
                .post("/course/{courseId}/quiz/form/{formId}/participate")
                .then()
                .statusCode(409)
                .body(is("Alias already taken"));
    }

    private void addResult(String courseId, String formId, String questionId, String role) {
        // create a websocket client
        // (@ServerEndpoint("/course/{courseId}/quiz/form/{formId}/subscribe/{userId}/{jwt}")
        try {
            LiveFeedbackSocketClient client = new LiveFeedbackSocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                    client,
                    URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId
                            + "/subscribe/"
                            + Helper.createMockUser("Prof").getId() + "/" + Helper.createMockUser("Prof").getJwt()));
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
            Assertions.assertTrue(
                    courseService.getCourse(courseId).getQuizForms().get(0).getStatus().toString().equals("STARTED"));
        } catch (Exception e) {
            System.out.println(e);
            Assertions.fail(e.getMessage());
        }
    }

    // TODO: move to different test class (socket)
    @Test
    public void testUserStats() {
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

        // participate in the quiz
        given().header("Authorization", "Bearer " + student1.getJwt())
                .pathParam("courseId", courseId)
                .pathParam("formId", formId)
                .body("Student1")
                .when()
                .post("/course/{courseId}/quiz/form/{formId}/participate")
                .then()
                .statusCode(200)
                .body(is("Successfully added"));

        given().header("Authorization", "Bearer " + student2.getJwt())
                .pathParam("courseId", courseId)
                .pathParam("formId", formId)
                .body("Student2")
                .when()
                .post("/course/{courseId}/quiz/form/{formId}/participate")
                .then()
                .statusCode(200)
                .body(is("Successfully added"));

        // create a websocket client for the prof and the students
        LiveFeedbackSocketClient profClient = new LiveFeedbackSocketClient();
        LiveFeedbackSocketClient student1Client = new LiveFeedbackSocketClient();
        LiveFeedbackSocketClient student2Client = new LiveFeedbackSocketClient();

        try {
            // connect to the websocket
            Session profSession = ContainerProvider.getWebSocketContainer().connectToServer(
                    profClient,
                    URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId
                            + "/subscribe/"
                            + prof.getId() + "/" + prof.getJwt()));
            Session student1Session = ContainerProvider.getWebSocketContainer().connectToServer(
                    student1Client,
                    URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId
                            + "/subscribe/"
                            + student1.getId() + "/" + student1.getJwt()));
            Session student2Session = ContainerProvider.getWebSocketContainer().connectToServer(
                    student2Client,
                    URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId
                            + "/subscribe/"
                            + student2.getId() + "/" + student2.getJwt()));

            Thread.sleep(1000);

            // start the quiz
            profClient.sendMessage(String.format("""
                        {
                            "action": "CHANGE_FORM_STATUS",
                            "formStatus": "STARTED",
                            "roles": ["PROF"]
                        }
                    """));

            Thread.sleep(1000);

            Assertions.assertTrue(
                    courseService.getCourse(courseId).getQuizForms().get(0).getStatus().toString().equals("STARTED"));
            Assertions
                    .assertTrue(courseService.getCourse(courseId).getQuizForms().get(0).getParticipants().size() == 2);

            // add a result for student 1 (correct)
            student1Client.sendMessage(String.format("""
                        {
                            "action": "ADD_RESULT",
                            "resultElementId": %s,
                            "resultValues": ["2"]
                        }
                    """, courses.get(0).getQuizForms().get(0).questions.get(0).getId().toString()));

            Thread.sleep(1000);

            // add a result for student 2 (incorrect)
            student2Client.sendMessage(String.format("""
                        {
                            "action": "ADD_RESULT",
                            "resultElementId": %s,
                            "resultValues": ["1"]
                        }
                    """, courses.get(0).getQuizForms().get(0).questions.get(0).getId().toString()));

            Thread.sleep(1000);

            // complete the quiz
            profClient.sendMessage(String.format("""
                        {
                            "action": "CHANGE_FORM_STATUS",
                            "formStatus": "FINISHED"
                        }
                    """));

            Thread.sleep(1000);

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

            Thread.sleep(1000);

            // close the websocket
            profSession.close();
            student1Session.close();
            student2Session.close();
        } catch (Exception e) {
            System.out.println(e);
            Assertions.fail(e.getMessage());
        }

        // participate in the quiz
        given().header("Authorization", "Bearer " + student1.getJwt())
                .pathParam("courseId", courseId)
                .pathParam("formId", formId)
                .body("Student1")
                .when()
                .post("/course/{courseId}/quiz/form/{formId}/participate")
                .then()
                .statusCode(200)
                .body(is("Successfully added"));

        given().header("Authorization", "Bearer " + student2.getJwt())
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
            Session student1Session = ContainerProvider.getWebSocketContainer().connectToServer(
                    student1Client,
                    URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId
                            + "/subscribe/"
                            + student1.getId() + "/" + student1.getJwt()));
            Session student2Session = ContainerProvider.getWebSocketContainer().connectToServer(
                    student2Client,
                    URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId
                            + "/subscribe/"
                            + student2.getId() + "/" + student2.getJwt()));

            Thread.sleep(1000);

            // start the quiz
            profClient.sendMessage(String.format("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "STARTED",
                    "roles": ["PROF"]
                }
            """));

            Thread.sleep(1000);

            Assertions.assertTrue(
                    courseService.getCourse(courseId).getQuizForms().get(0).getStatus().toString().equals("STARTED"));
            Assertions
                    .assertTrue(courseService.getCourse(courseId).getQuizForms().get(0).getParticipants().size() == 2);

            // add a result for student 1 (correct)
            student1Client.sendMessage(String.format("""
                        {
                            "action": "ADD_RESULT",
                            "resultElementId": %s,
                            "resultValues": ["1"]
                        }
                    """, courses.get(0).getQuizForms().get(0).questions.get(0).getId().toString()));

            Thread.sleep(1000);

            // add a result for student 2 (incorrect)
            student2Client.sendMessage(String.format("""
                        {
                            "action": "ADD_RESULT",
                            "resultElementId": %s,
                            "resultValues": ["2"]
                        }
                    """, courses.get(0).getQuizForms().get(0).questions.get(0).getId().toString()));

            Thread.sleep(1000);

            // complete the quiz
            profClient.sendMessage(String.format("""
                        {
                            "action": "CHANGE_FORM_STATUS",
                            "formStatus": "FINISHED"
                        }
                    """));

            Thread.sleep(1000);

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

            Thread.sleep(1000);

            // close the websocket
            profSession.close();
            student1Session.close();
            student2Session.close();
        } catch (Exception e) {
            System.out.println(e);
            Assertions.fail(e.getMessage());
        }

        // participate in the quiz
        given().header("Authorization", "Bearer " + student1.getJwt())
                .pathParam("courseId", courseId)
                .pathParam("formId", formId)
                .body("Student1")
                .when()
                .post("/course/{courseId}/quiz/form/{formId}/participate")
                .then()
                .statusCode(200)
                .body(is("Successfully added"));

        given().header("Authorization", "Bearer " + student2.getJwt())
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
            Session student1Session = ContainerProvider.getWebSocketContainer().connectToServer(
                    student1Client,
                    URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId
                            + "/subscribe/"
                            + student1.getId() + "/" + student1.getJwt()));
            Session student2Session = ContainerProvider.getWebSocketContainer().connectToServer(
                    student2Client,
                    URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId
                            + "/subscribe/"
                            + student2.getId() + "/" + student2.getJwt()));

            Thread.sleep(1000);

            // start the quiz
            profClient.sendMessage(String.format("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "STARTED",
                    "roles": ["PROF"]
                }
            """));

            Thread.sleep(1000);

            Assertions.assertTrue(
                    courseService.getCourse(courseId).getQuizForms().get(0).getStatus().toString().equals("STARTED"));
            Assertions
                    .assertTrue(courseService.getCourse(courseId).getQuizForms().get(0).getParticipants().size() == 2);

            // add a result for student 1 (correct)
            student1Client.sendMessage(String.format("""
                        {
                            "action": "ADD_RESULT",
                            "resultElementId": %s,
                            "resultValues": ["1"]
                        }
                    """, courses.get(0).getQuizForms().get(0).questions.get(0).getId().toString()));

            Thread.sleep(1000);

            // add a result for student 2 (incorrect)
            student2Client.sendMessage(String.format("""
                        {
                            "action": "ADD_RESULT",
                            "resultElementId": %s,
                            "resultValues": ["2"]
                        }
                    """, courses.get(0).getQuizForms().get(0).questions.get(0).getId().toString()));

            Thread.sleep(1000);

            // complete the quiz
            profClient.sendMessage(String.format("""
                        {
                            "action": "CHANGE_FORM_STATUS",
                            "formStatus": "FINISHED"
                        }
                    """));

            Thread.sleep(1000);

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

            Thread.sleep(1000);

            // close the websocket
            profSession.close();
            student1Session.close();
            student2Session.close();
        } catch (Exception e) {
            System.out.println(e);
            Assertions.fail(e.getMessage());
        }

    }


    // TODO: test Status WAITING (lobby)
}
