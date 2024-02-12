package de.htwg_konstanz.mobilelearning.services.feedback;

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
import de.htwg_konstanz.mobilelearning.enums.FormStatus;
import de.htwg_konstanz.mobilelearning.models.Course;
import de.htwg_konstanz.mobilelearning.models.QuestionWrapper;
import de.htwg_konstanz.mobilelearning.models.auth.UserRole;
import de.htwg_konstanz.mobilelearning.models.feedback.FeedbackForm;
import de.htwg_konstanz.mobilelearning.services.CourseService;
import de.htwg_konstanz.mobilelearning.services.api.ApiService;
import de.htwg_konstanz.mobilelearning.services.api.models.ApiCourse;
import de.htwg_konstanz.mobilelearning.services.api.models.ApiFeedbackForm;
import de.htwg_konstanz.mobilelearning.services.api.models.ApiFeedbackForm.ApiFeedbackQuestion;
import de.htwg_konstanz.mobilelearning.services.api.models.ApiQuizForm;
import de.htwg_konstanz.mobilelearning.services.api.models.ApiQuizForm.ApiQuizQuestion;
import de.htwg_konstanz.mobilelearning.services.auth.UserService;
import de.htwg_konstanz.mobilelearning.test.SecureEndpoint;
import io.quarkus.security.ForbiddenException;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import io.smallrye.common.constraint.Assert;
import io.smallrye.jwt.auth.principal.DefaultJWTCallerPrincipal;
import jakarta.inject.Inject;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Session;
import jakarta.ws.rs.core.Response;

@QuarkusTest
@TestProfile(MockMongoTestProfile.class)
public class FeedbackFormServiceTest {

    @Inject
    private CourseService courseService;

    @Inject
    private ApiService apiService;

    @Inject
    private UserService userService;

    @Inject
    private FeedbackFormService feedbackFormService;

    private String profJwt = "";
    private String profId = "";

    @BeforeEach
    void init(){
        courseService.deleteAllCourses();
    }

    @Test
    @TestSecurity(user = "Prof", roles = { UserRole.PROF})
    @JwtSecurity(claims = { @Claim(key = "email", value = "prof@htwg-konstanz.de") })
    public void createFeedbackForm() {
        List<Course> courses = createCourse();
        FeedbackForm feedbackForm = new FeedbackForm(courses.get(0).id, "name", "description", List.of(), FormStatus.NOT_STARTED);

        feedbackFormService.createFeedbackForm(courses.getFirst().id.toString(), feedbackForm);

        List<FeedbackForm> feedbackForms = feedbackFormService.getFeedbackForms(courses.get(0).id.toString());
        Assertions.assertEquals(feedbackForms.size(), 2);
        Assertions.assertEquals(feedbackForms.get(1).name, "name");
        Assertions.assertEquals(feedbackForms.get(1).description, "description");
    }

    @Test
    @TestSecurity(user = "Student", roles = { UserRole.STUDENT})
    @JwtSecurity(claims = { @Claim(key = "email", value = "student@htwg-konstanz.de") })
    public void createFeedbackFormNotAllowed() {
        List<Course> courses = createCourse();
        FeedbackForm feedbackForm = new FeedbackForm(courses.get(0).id, "name", "description", List.of(), FormStatus.NOT_STARTED);

        Exception exception = Assertions.assertThrows(ForbiddenException.class, () -> {
            feedbackFormService.createFeedbackForm(courses.getFirst().id.toString(), feedbackForm);
        });

        Assertions.assertEquals(exception.getClass().getName(), "io.quarkus.security.ForbiddenException");
    }

    @Test
    @TestSecurity(user = "Prof", roles = { UserRole.PROF})
    @JwtSecurity(claims = { @Claim(key = "email", value = "prof@htwg-konstanz.de") })
    public void getFeedbackForm() {
        List<Course> courses = createCourse();
        List<FeedbackForm> feedbackForms = feedbackFormService.getFeedbackForms(courses.get(0).id.toString());
        
        FeedbackForm feedbackFormFromService = feedbackFormService.getFeedbackForm(courses.get(0).id.toString(), feedbackForms.get(0).id.toString(), false);
        Assertions.assertEquals(feedbackFormFromService.name, "Erster Sprint");
        Assertions.assertEquals(feedbackFormFromService.description, "Hier wollen wir Ihr Feedback zum ersten Sprint einholen");
        Assertions.assertEquals(feedbackFormFromService.questions.size(), 1);
        Assertions.assertEquals(feedbackFormFromService.questions.get(0).results.size(), 0);
    }

    
    @Test
    @TestSecurity(user = "Prof", roles = { UserRole.PROF})
    @JwtSecurity(claims = { @Claim(key = "email", value = "prof@htwg-konstanz.de") })
    public void getFeedbackFormWithResult() {
        List<Course> courses = createCourse();
        String courseId = courses.getFirst().getId().toString();
        String formId = courses.getFirst().getFeedbackForms().get(0).getId().toString();
        String questionId = courses.getFirst().feedbackForms.get(0).questions.get(0).getId().toString();
        this.createProfUser();
        addResult(courseId, formId, questionId);
        List<FeedbackForm> feedbackForms = feedbackFormService.getFeedbackForms(courses.get(0).id.toString());
        
        FeedbackForm feedbackFormFromService = feedbackFormService.getFeedbackForm(courses.get(0).id.toString(), feedbackForms.get(0).id.toString(), true);
        Assertions.assertEquals(feedbackFormFromService.name, "Erster Sprint");
        Assertions.assertEquals(feedbackFormFromService.description, "Hier wollen wir Ihr Feedback zum ersten Sprint einholen");
        Assertions.assertEquals(feedbackFormFromService.questions.size(), 1);
        Assertions.assertEquals(feedbackFormFromService.questions.get(0).results.size(), 1);
    }

    private void addResult(String courseId, String formId, String questionId) {
        // create a websocket client
        // (@ServerEndpoint("/course/{courseId}/feedback/form/{formId}/subscribe/{userId}/{jwt}")
        try {
            LiveFeedbackSocketClient client = new LiveFeedbackSocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" + this.profId + "/" + this.profJwt)
            );
            client.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "STARTED",
                    "roles": []
                }
            """);
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

    @Test
    @TestSecurity(user = "Prof", roles = { UserRole.PROF, UserRole.STUDENT })
    @JwtSecurity(claims = { @Claim(key = "email", value = "prof@htwg-konstanz.de") })
    public void createProfUser() {
        try {
            Response response = userService.login("Basic UHJvZjo=");
            Assertions.assertNotNull(response);
            Assertions.assertEquals(response.getStatus(), 200);
            String jwt = response.getEntity().toString();
            Assertions.assertNotNull(jwt);
            Assertions.assertTrue(jwt.length() > 0);
            Assertions.assertTrue(jwt.contains("ey"));
            this.profJwt = jwt; // save jwt for later use

            String jwtJson = new String(Base64.getUrlDecoder().decode(jwt.split("\\.")[1]), StandardCharsets.UTF_8);
            DefaultJWTCallerPrincipal defaultJWTCallerPrincipal = new DefaultJWTCallerPrincipal(
                    JwtClaims.parse(jwtJson));
            Assertions.assertNotNull(defaultJWTCallerPrincipal);
            Assertions.assertEquals(defaultJWTCallerPrincipal.getClaim("full_name"), "Prof");
            Assertions.assertTrue(defaultJWTCallerPrincipal.getClaim("sub").toString().length() > 0);
            this.profId = defaultJWTCallerPrincipal.getClaim("sub").toString(); // save id for later use
            Assertions.assertTrue(defaultJWTCallerPrincipal.getClaim("email").toString().length() > 0);
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }

}
