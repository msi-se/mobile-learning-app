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
import io.quarkus.security.ForbiddenException;
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
    public void createFeedbackForm() {
        //create & get courses
        List<Course> courses = createCourse();
        FeedbackForm feedbackForm = new FeedbackForm(courses.get(0).id, "name", "description", List.of(), FormStatus.NOT_STARTED);

        // create a feedback form
        feedbackFormService.createFeedbackForm(courses.getFirst().id.toString(), feedbackForm);

        //check if the feedback form was created
        List<FeedbackForm> feedbackForms = feedbackFormService.getFeedbackForms(courses.get(0).id.toString());
        Assertions.assertEquals(2, feedbackForms.size());
        Assertions.assertEquals("name", feedbackForms.get(1).name);
        Assertions.assertEquals("description", feedbackForms.get(1).description);
    }

    @Test
    @TestSecurity(user = "Student", roles = { UserRole.STUDENT})
    @JwtSecurity(claims = { @Claim(key = "email", value = "student@htwg-konstanz.de") })
    public void createFeedbackFormForbidden() {
        //create & get courses
        List<Course> courses = createCourse();
        FeedbackForm feedbackForm = new FeedbackForm(courses.get(0).id, "name", "description", List.of(), FormStatus.NOT_STARTED);
        
        Exception exception = Assertions.assertThrows(ForbiddenException.class, () -> {
            feedbackFormService.createFeedbackForm(courses.getFirst().id.toString(), feedbackForm);
        });
        // student should not be allowed to create feedback forms
        Assertions.assertEquals("io.quarkus.security.ForbiddenException", exception.getClass().getName());
    }

    @Test
    @TestSecurity(user = "Prof", roles = { UserRole.PROF})
    @JwtSecurity(claims = { @Claim(key = "email", value = "prof@htwg-konstanz.de") })
    public void getFeedbackForm() {
        //create & get courses + ids
        List<Course> courses = createCourse();
        String courseId = courses.getFirst().getId().toString();
        String formId = courses.getFirst().getFeedbackForms().get(0).getId().toString();
        String questionId = courses.getFirst().feedbackForms.get(0).questions.get(0).getId().toString();
        
        // add a result & get feedback forms
        addResult(courseId, formId, questionId);
        List<FeedbackForm> feedbackForms = feedbackFormService.getFeedbackForms(courses.get(0).id.toString());
        
        // Assert get feedback form without results
        FeedbackForm feedbackFormFromService = feedbackFormService.getFeedbackForm(courses.get(0).id.toString(), feedbackForms.get(0).id.toString(), false);
        Assertions.assertEquals("Erster Sprint", feedbackFormFromService.name);
        Assertions.assertEquals("Hier wollen wir Ihr Feedback zum ersten Sprint einholen", feedbackFormFromService.description);
        Assertions.assertEquals(1, feedbackFormFromService.questions.size());
        Assertions.assertEquals(0, feedbackFormFromService.questions.get(0).results.size());
    }
    
    @Test
    @TestSecurity(user = "Prof", roles = { UserRole.PROF})
    @JwtSecurity(claims = { @Claim(key = "email", value = "prof@htwg-konstanz.de") })
    public void getFeedbackFormWithResult() {
        //create & get courses + ids
        List<Course> courses = createCourse();
        String courseId = courses.getFirst().getId().toString();
        String formId = courses.getFirst().getFeedbackForms().get(0).getId().toString();
        String questionId = courses.getFirst().feedbackForms.get(0).questions.get(0).getId().toString();
        // add a result & get feedback forms
        addResult(courseId, formId, questionId);
        List<FeedbackForm> feedbackForms = feedbackFormService.getFeedbackForms(courses.get(0).id.toString());
        
        // Assert get feedback form without results
        FeedbackForm feedbackFormFromService = feedbackFormService.getFeedbackForm(courses.get(0).id.toString(), feedbackForms.get(0).id.toString(), true);
        Assertions.assertEquals("Erster Sprint", feedbackFormFromService.name);
        Assertions.assertEquals("Hier wollen wir Ihr Feedback zum ersten Sprint einholen", feedbackFormFromService.description);
        Assertions.assertEquals(1, feedbackFormFromService.questions.size());
        Assertions.assertEquals(1, feedbackFormFromService.questions.get(0).results.size());
        Assertions.assertEquals("5", feedbackFormFromService.questions.get(0).results.get(0).values.get(0));
    }

    @Test
    @TestSecurity(user = "Prof", roles = { UserRole.PROF})
    @JwtSecurity(claims = { @Claim(key = "email", value = "prof@htwg-konstanz.de") })
    public void clearResults() {
        //create & get courses + ids
        List<Course> courses = createCourse();
        String courseId = courses.getFirst().getId().toString();
        String formId = courses.getFirst().getFeedbackForms().get(0).getId().toString();
        String questionId = courses.getFirst().feedbackForms.get(0).questions.get(0).getId().toString();
        // add a result & get feedback forms
        addResult(courseId, formId, questionId);
        FeedbackForm feedbackForm = feedbackFormService.getFeedbackForm(courses.get(0).id.toString(), formId, true);

        // Assert that results were cleared
        Assertions.assertEquals(1, feedbackForm.questions.get(0).results.size());
        feedbackFormService.clearFeedbackFormResults(courses.get(0).id.toString(), feedbackForm.id.toString());
        FeedbackForm feedbackFormCleared = feedbackFormService.getFeedbackForm(courses.get(0).id.toString(), formId, true);
        Assertions.assertEquals(0, feedbackFormCleared.questions.get(0).results.size());
    }

    @Test
    @TestSecurity(user = "Student", roles = { UserRole.STUDENT})
    @JwtSecurity(claims = { @Claim(key = "email", value = "student@htwg-konstanz.de") })
    public void clearResultsForbidden() {
        //create & get courses + ids
        List<Course> courses = createCourse();
        String formId = courses.getFirst().getFeedbackForms().get(0).getId().toString();
        FeedbackForm feedbackForm = feedbackFormService.getFeedbackForm(courses.get(0).id.toString(), formId, true);

        Exception exception = Assertions.assertThrows(ForbiddenException.class, () -> {
            feedbackFormService.clearFeedbackFormResults(courses.get(0).id.toString(), feedbackForm.id.toString());
        });

        // students should not be able to clear results
        Assertions.assertEquals("io.quarkus.security.ForbiddenException", exception.getClass().getName());
    }

    @Test
    @TestSecurity(user = "Prof", roles = { UserRole.PROF})
    @JwtSecurity(claims = { @Claim(key = "email", value = "prof@htwg-konstanz.de") })
    public void updateFeedbackForm() {
        //create & get courses + ids
        List<Course> courses = createCourse();
        String courseId = courses.getFirst().getId().toString();
        String formId = courses.getFirst().getFeedbackForms().get(0).getId().toString();
      
        // update the feedback form name, description and questions
        FeedbackForm feedbackFormUpdate = new FeedbackForm(courses.get(0).id, "nameUpdate", "descriptionUpdate", List.of(), FormStatus.NOT_STARTED);
        feedbackFormService.updateFeedbackForm(courseId, formId, feedbackFormUpdate);
        
        // check if the feedback form was updated
        List<FeedbackForm> updatedFeedbackForms = feedbackFormService.getFeedbackForms(courses.get(0).id.toString());
        Assertions.assertEquals("nameUpdate", updatedFeedbackForms.get(0).name);
        Assertions.assertEquals("descriptionUpdate", updatedFeedbackForms.get(0).description);
        Assertions.assertEquals(0, updatedFeedbackForms.get(0).questions.size());
    }

    
    @Test
    @TestSecurity(user = "Student", roles = { UserRole.STUDENT})
    @JwtSecurity(claims = { @Claim(key = "email", value = "student@htwg-konstanz.de") })
    public void updateFeedbackFormForbidden() {
        //create & get courses + ids
        List<Course> courses = createCourse();
        String courseId = courses.getFirst().getId().toString();
        String formId = courses.getFirst().getFeedbackForms().get(0).getId().toString();
        
        // update the feedback form name, description and questions
        FeedbackForm feedbackFormUpdate = new FeedbackForm(courses.get(0).id, "nameUpdate", "descriptionUpdate", List.of(), FormStatus.NOT_STARTED);
        Exception exception = Assertions.assertThrows(ForbiddenException.class, () -> {
            feedbackFormService.updateFeedbackForm(courseId, formId, feedbackFormUpdate);
        });

        // students should not be able to update feedback forms
        Assertions.assertEquals("io.quarkus.security.ForbiddenException", exception.getClass().getName());
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
            // starts feedbacksession
            client.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "STARTED",
                    "roles": []
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
