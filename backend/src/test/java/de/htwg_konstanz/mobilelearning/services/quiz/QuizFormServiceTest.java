package de.htwg_konstanz.mobilelearning.services.quiz;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.jose4j.jwt.JwtClaims;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import com.google.gson.Gson;

import de.htwg_konstanz.mobilelearning.LiveFeedbackSocketClient;
import de.htwg_konstanz.mobilelearning.MockMongoTestProfile;
import de.htwg_konstanz.mobilelearning.MockUser;
import de.htwg_konstanz.mobilelearning.models.Course;
import de.htwg_konstanz.mobilelearning.models.auth.UserRole;
import de.htwg_konstanz.mobilelearning.models.quiz.QuizForm;
import de.htwg_konstanz.mobilelearning.services.CourseService;
import de.htwg_konstanz.mobilelearning.services.api.ApiService;
import de.htwg_konstanz.mobilelearning.services.api.models.ApiCourse;
import de.htwg_konstanz.mobilelearning.services.api.models.ApiFeedbackForm;
import de.htwg_konstanz.mobilelearning.services.api.models.ApiFeedbackForm.ApiFeedbackQuestion;
import de.htwg_konstanz.mobilelearning.services.api.models.ApiQuizForm;
import de.htwg_konstanz.mobilelearning.services.api.models.ApiQuizForm.ApiQuizQuestion;
import de.htwg_konstanz.mobilelearning.services.auth.UserService;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import io.restassured.http.ContentType;
import io.smallrye.jwt.auth.principal.DefaultJWTCallerPrincipal;
import jakarta.inject.Inject;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Session;
import jakarta.ws.rs.core.Response;

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
        given()
                .header("Authorization", "Bearer " + Helper.createMockUser("Student1").getJwt())
                .pathParam("courseId", courseId)
                .pathParam("formId", formId)
                .body("alias")
                .when()
                .post("/course/{courseId}/quiz/form/{formId}/participate")
                .then()
                .statusCode(200)
                .body(is("Successfully added"));

        // 200 if alias already taken by the same user
        given()
                .header("Authorization", "Bearer " + Helper.createMockUser("Student1").getJwt())
                .pathParam("courseId", courseId)
                .pathParam("formId", formId)
                .body("alias")
                .when()
                .post("/course/{courseId}/quiz/form/{formId}/participate")
                .then()
                .statusCode(200)
                .body(is("Successfully added"));

        // 409 if alias already taken by another user
        given()
                .header("Authorization", "Bearer " + Helper.createMockUser("Student2").getJwt())
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
}
