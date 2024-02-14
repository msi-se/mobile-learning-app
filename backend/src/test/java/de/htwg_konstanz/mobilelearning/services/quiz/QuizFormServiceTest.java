package de.htwg_konstanz.mobilelearning.services.quiz;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import org.jose4j.jwt.JwtClaims;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.htwg_konstanz.mobilelearning.LiveFeedbackSocketClient;
import de.htwg_konstanz.mobilelearning.MockMongoTestProfile;
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
import io.smallrye.jwt.auth.principal.DefaultJWTCallerPrincipal;
import jakarta.inject.Inject;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Session;
import jakarta.ws.rs.core.Response;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@TestProfile(MockMongoTestProfile.class)
@TestHTTPEndpoint(QuizFormService.class)
public class QuizFormServiceTest {

    @Inject
    private CourseService courseService;

    @Inject
    private ApiService apiService;

    @Inject
    private UserService userService;

    @Inject
    private QuizFormService quizFormService;

    private String profJwt = "";
    private String profId = "";
    private String studentJwt = "";

    @BeforeEach
    void init(){
        courseService.deleteAllCourses();
        createProfUser();
        createStudentUser();
    }

    @Test
    @TestSecurity(user = "Prof", roles = { UserRole.PROF})
    @JwtSecurity(claims = { @Claim(key = "email", value = "prof@htwg-konstanz.de") })
    public void getQuizFormWithoutResult() {
        //create & get courses + ids
        List<Course> courses = createCourse();
        String courseId = courses.getFirst().getId().toString();
        String formId = courses.getFirst().getQuizForms().get(0).getId().toString();
        String questionId = courses.getFirst().quizForms.get(0).questions.get(0).getId().toString();
        
        // add a result & get quiz forms
        addResult(courseId, formId, questionId, "Prof");
        List<QuizForm> quizForms = quizFormService.getQuizForms(courses.get(0).id.toString());
        
        // Assert get quiz form without results
        QuizForm quizFormFromService = quizFormService.getQuizForm(courses.get(0).id.toString(), quizForms.get(0).id.toString(), false);
        Assertions.assertEquals("Rollenverständnis bei Scrum", quizFormFromService.name);
        Assertions.assertEquals("Ein Quiz zum Rollenverständnis und Teamaufbau bei Scrum", quizFormFromService.description);
        Assertions.assertEquals(1, quizFormFromService.questions.size());
        Assertions.assertEquals(0, quizFormFromService.questions.get(0).results.size());
    }

    @Test
    @TestSecurity(user = "Prof", roles = { UserRole.PROF})
    @JwtSecurity(claims = { @Claim(key = "email", value = "prof@htwg-konstanz.de") })
    public void getQuizFormWithResult() {
        //create & get courses + ids
        List<Course> courses = createCourse();
        String courseId = courses.getFirst().getId().toString();
        String formId = courses.getFirst().getQuizForms().get(0).getId().toString();
        String questionId = courses.getFirst().quizForms.get(0).questions.get(0).getId().toString();
        
        // add a result & get quiz forms
        addResult(courseId, formId, questionId, "Prof");
        List<QuizForm> quizForms = quizFormService.getQuizForms(courses.get(0).id.toString());
        
        // Assert get quiz form without results
        QuizForm quizFormFromService = quizFormService.getQuizForm(courses.get(0).id.toString(), quizForms.get(0).id.toString(), true);
        Assertions.assertEquals("Rollenverständnis bei Scrum", quizFormFromService.name);
        Assertions.assertEquals("Ein Quiz zum Rollenverständnis und Teamaufbau bei Scrum", quizFormFromService.description);
        Assertions.assertEquals(1, quizFormFromService.questions.size());
        Assertions.assertEquals(1, quizFormFromService.questions.get(0).results.size());
        Assertions.assertEquals("1", quizFormFromService.questions.get(0).results.get(0).values.get(0));	
    }

    @Test
    @TestSecurity(user = "Prof", roles = { UserRole.PROF})
    @JwtSecurity(claims = { @Claim(key = "email", value = "prof@htwg-konstanz.de") })
    public void participateUniqueAlias() {
        //create & get courses + ids
        List<Course> courses = createCourse();
        String courseId = courses.getFirst().getId().toString();
        String formId = courses.getFirst().getQuizForms().get(0).getId().toString();
        createProfUser();
        createStudentUser();

        given()
            .header("Authorization", "Bearer " + studentJwt)
            .pathParam("courseId", courseId)
            .pathParam("formId", formId)
            .body("alias")
            .when()
            .post("/{formId}/participate")  
            .then()
                .statusCode(200)
                .body(is("Successfully added"));

        given()
            .header("Authorization", "Bearer " + studentJwt)
            .pathParam("courseId", courseId)
            .pathParam("formId", formId)
            .body("alias")
            .when()
            .post("/{formId}/participate")  
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
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + this.profId + "/" + this.profJwt)
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

    private List<Course> createCourse() {
        // create a course via the json api
        ApiCourse apiCourse = new ApiCourse(
            "AUME 23/24",
            "Agile Vorgehensmodelle und Mobile Kommunikation",
            List.of(
                    new ApiFeedbackForm(
                            "Erster Sprint",
                            "Hier wollen wir Ihr Feedback zum ersten Sprint einholen",
                            List.of(
                                    new ApiFeedbackQuestion(
                                            "Rolle",
                                            "Wie gut hat Ihnen ihre Pizza gefallen?",
                                            "SLIDER",
                                            List.of(),
                                            "F-Q-ROLLE")),
                            "F-ERSTERSPRINT")),
            List.of(
                    new ApiQuizForm(
                            "Rollenverständnis bei Scrum",
                            "Ein Quiz zum Rollenverständnis und Teamaufbau bei Scrum",
                            List.of(
                                    new ApiQuizQuestion(
                                            "Product Owner",
                                            "Welche der folgenden Aufgaben ist nicht Teil der Rolle des Product Owners?",
                                            "SINGLE_CHOICE",
                                            List.of(
                                                    "Erstellung des Product Backlogs",
                                                    "Priorisierung des Product Backlogs",
                                                    "Pizza bestellen für jedes Daily"),
                                            true,
                                            List.of("2"),
                                            "Q-Q-PDRODUCTOWNER")),
                            "Q-ROLES")),
            "AUME23");
        return apiService.updateCourses(List.of(apiCourse));
    } 

    public void createProfUser() {
        try {
            Response response = userService.login("Basic UHJvZjo=");
            profJwt = response.getEntity().toString(); // save jwt for later use
            String jwtJson = new String(Base64.getUrlDecoder().decode(profJwt.split("\\.")[1]), StandardCharsets.UTF_8);
            DefaultJWTCallerPrincipal defaultJWTCallerPrincipal = new DefaultJWTCallerPrincipal(
                    JwtClaims.parse(jwtJson));
            Assertions.assertEquals(defaultJWTCallerPrincipal.getClaim("full_name"), "Prof");
            Assertions.assertTrue(defaultJWTCallerPrincipal.getClaim("sub").toString().length() > 0);
            profId = defaultJWTCallerPrincipal.getClaim("sub").toString(); // save id for later use
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }

    public void createStudentUser() {
        try {
            Response response = userService.login("Basic U3R1ZGVudDo=");
            studentJwt = response.getEntity().toString();
            String jwtJson = new String(Base64.getUrlDecoder().decode(studentJwt.split("\\.")[1]), StandardCharsets.UTF_8);
            DefaultJWTCallerPrincipal defaultJWTCallerPrincipal = new DefaultJWTCallerPrincipal(
                    JwtClaims.parse(jwtJson));
            Assertions.assertEquals(defaultJWTCallerPrincipal.getClaim("full_name"), "Student");
            Assertions.assertTrue(defaultJWTCallerPrincipal.getClaim("sub").toString().length() > 0);
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }
    
}
