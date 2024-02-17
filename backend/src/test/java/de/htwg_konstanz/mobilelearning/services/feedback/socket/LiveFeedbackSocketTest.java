package de.htwg_konstanz.mobilelearning.services.feedback.socket;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import org.jose4j.jwt.JwtClaims;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import de.htwg_konstanz.mobilelearning.LiveFeedbackSocketClient;
import de.htwg_konstanz.mobilelearning.MockMongoTestProfile;
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
import de.htwg_konstanz.mobilelearning.services.feedback.FeedbackFormService;
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
public class LiveFeedbackSocketTest {

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
    private String profJwt2 = "";
    private String profId2 = "";

    @BeforeEach
    void init(TestInfo testInfo){
        System.out.println("------------------------------");
        System.out.println("Test: " + testInfo.getDisplayName());
        courseService.deleteAllCourses();
        createProfUser();
        createProf2User();
    }

    @Test
    @TestSecurity(user = "Prof", roles = { UserRole.PROF })
    @JwtSecurity(claims = { @Claim(key = "sub", value = "profId") })
    public void startFeedbackForm() {
        // create & get courses
        List<Course> courses = createCourse();
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
                URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" + this.profId + "/" + profJwt)
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
    @TestSecurity(user = "Prof", roles = { UserRole.PROF})
    @JwtSecurity(claims = { @Claim(key = "email", value = "prof@htwg-konstanz.de") })
    public void feedbackAcceptedOnce() {
        //create & get courses + ids 
        List<Course> courses = createCourse();
        String courseId = courses.getFirst().getId().toString();
        String formId = courses.getFirst().getFeedbackForms().get(0).getId().toString();
        String questionId = courses.getFirst().feedbackForms.get(0).questions.get(0).getId().toString();
        // add a result to the feedback form
        addResult(courseId, formId, questionId);
        addResult(courseId, formId, questionId);
        // get feedback forms from course
        List<FeedbackForm> feedbackForms = feedbackFormService.getFeedbackForms(courses.get(0).id.toString());
        
        // Assert getFeedbackForm()
        FeedbackForm feedbackFormFromService = feedbackFormService.getFeedbackForm(courses.get(0).id.toString(), feedbackForms.get(0).id.toString(), true);
        Assertions.assertEquals("Erster Sprint", feedbackFormFromService.name);
        Assertions.assertEquals("Hier wollen wir Ihr Feedback zum ersten Sprint einholen", feedbackFormFromService.description);
        Assertions.assertEquals(1, feedbackFormFromService.questions.size());
        Assertions.assertEquals(1, feedbackFormFromService.questions.get(0).results.size());
    }

    @Test
    @TestSecurity(user = "Prof", roles = { UserRole.PROF})
    @JwtSecurity(claims = { @Claim(key = "email", value = "prof@htwg-konstanz.de") })
    public void startFeedbackuserNotOwner() {
        //create & get courses + ids
        List<Course> courses = createCourse();
        String courseId = courses.get(0).getId().toString();
        String formId = courses.get(0).getFeedbackForms().get(0).getId().toString();
 
         // create a websocket client
         // (@ServerEndpoint("/course/{courseId}/feedback/form/{formId}/subscribe/{userId}/{jwt}")
         try {
             LiveFeedbackSocketClient client = new LiveFeedbackSocketClient();
             Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                 client,
                 URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" + this.profId2 + "/" + profJwt2)
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

    @Test
    @TestSecurity(user = "Prof", roles = { UserRole.PROF})
    @JwtSecurity(claims = { @Claim(key = "email", value = "prof@htwg-konstanz.de") })
    public void startFeedbackStudent() {
        //create & get courses + ids
        List<Course> courses = createCourse();
        String courseId = courses.get(0).getId().toString();
        String formId = courses.get(0).getFeedbackForms().get(0).getId().toString();
 
         // create a websocket client
         // (@ServerEndpoint("/course/{courseId}/feedback/form/{formId}/subscribe/{userId}/{jwt}")
         try {
             LiveFeedbackSocketClient client = new LiveFeedbackSocketClient();
             Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                 client,
                 URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" + this.profId + "/" + profJwt)
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
             Assertions.assertTrue(courseService.getCourse(courseId).getFeedbackForms().get(0).getStatus().toString().equals("NOT_STARTED"));
         } catch (Exception e) {
             System.out.println(e);
             Assertions.fail(e.getMessage());
         }
        
    }

    @Test
    @TestSecurity(user = "Prof", roles = { UserRole.PROF})
    @JwtSecurity(claims = { @Claim(key = "email", value = "prof@htwg-konstanz.de") })
    public void stopFeedback() {
        //create & get courses + ids
        List<Course> courses = createCourse();
        String courseId = courses.get(0).getId().toString();
        String formId = courses.get(0).getFeedbackForms().get(0).getId().toString();
 
         // create a websocket client
         // (@ServerEndpoint("/course/{courseId}/feedback/form/{formId}/subscribe/{userId}/{jwt}")
         try {
             LiveFeedbackSocketClient client = new LiveFeedbackSocketClient();
             Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                 client,
                 URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" + profId + "/" + profJwt)
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
    @TestSecurity(user = "Prof", roles = { UserRole.PROF})
    @JwtSecurity(claims = { @Claim(key = "email", value = "prof@htwg-konstanz.de") })
    public void stopFeedbackNotOwner() {
        //create & get courses + ids
        List<Course> courses = createCourse();
        String courseId = courses.get(0).getId().toString();
        String formId = courses.get(0).getFeedbackForms().get(0).getId().toString();
 
         // create a websocket client
         // (@ServerEndpoint("/course/{courseId}/feedback/form/{formId}/subscribe/{userId}/{jwt}")
         try {
             LiveFeedbackSocketClient client = new LiveFeedbackSocketClient();
             LiveFeedbackSocketClient client2 = new LiveFeedbackSocketClient();
             Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                 client,
                 URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" + profId + "/" + profJwt)
             );
             Session session2 = ContainerProvider.getWebSocketContainer().connectToServer(
                 client2,
                 URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" + profId2 + "/" + profJwt2)
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
 
             // form status should not change because user student
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

    public void createProfUser() {
        // creates Prof User
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

    public void createProf2User() {
        // creates Prof2 User
        try {
            Response response = userService.login("Basic UHJvZjI=");
            profJwt2 = response.getEntity().toString(); // save jwt for later use
            String jwtJson = new String(Base64.getUrlDecoder().decode(profJwt2.split("\\.")[1]), StandardCharsets.UTF_8);
            DefaultJWTCallerPrincipal defaultJWTCallerPrincipal = new DefaultJWTCallerPrincipal(
                    JwtClaims.parse(jwtJson));
            Assertions.assertEquals(defaultJWTCallerPrincipal.getClaim("full_name"), "Prof2");
            Assertions.assertTrue(defaultJWTCallerPrincipal.getClaim("sub").toString().length() > 0);
            profId2 = defaultJWTCallerPrincipal.getClaim("sub").toString(); // save id for later use
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }    
}
