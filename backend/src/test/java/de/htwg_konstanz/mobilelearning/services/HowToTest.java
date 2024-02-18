package de.htwg_konstanz.mobilelearning.services;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.jose4j.jwt.JwtClaims;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.htwg_konstanz.mobilelearning.LiveFeedbackSocketClient;
import de.htwg_konstanz.mobilelearning.MockMongoTestProfile;
import de.htwg_konstanz.mobilelearning.models.Course;
import de.htwg_konstanz.mobilelearning.models.auth.UserRole;
import de.htwg_konstanz.mobilelearning.services.api.ApiService;
import de.htwg_konstanz.mobilelearning.services.api.models.ApiCourse;
import de.htwg_konstanz.mobilelearning.services.api.models.ApiFeedbackForm;
import de.htwg_konstanz.mobilelearning.services.api.models.ApiFeedbackForm.ApiFeedbackQuestion;
import de.htwg_konstanz.mobilelearning.services.api.models.ApiQuizForm.ApiQuizQuestion;
import de.htwg_konstanz.mobilelearning.services.auth.UserService;
import de.htwg_konstanz.mobilelearning.services.api.models.ApiQuizForm;
import de.htwg_konstanz.mobilelearning.test.SecureEndpoint;
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
public class HowToTest {

    @Inject
    private CourseService courseService;

    @Inject
    private ApiService apiService;

    @Inject
    private UserService userService;

    @Inject
    private SecureEndpoint secureEndpoint;

    private String profJwt = "";
    private String profId = "";

    @Test
    @TestSecurity(user = "TestUser", roles = { UserRole.PROF, UserRole.STUDENT })
    @JwtSecurity(claims = {})
    public void testGetAllCourses() {

        // delete all courses
        courseService.deleteAllCourses();

        // get all courses
        List<Course> courses = courseService.getCourses("");
        Assertions.assertTrue(courses.isEmpty());
    }

    @Test
    @TestSecurity(user = "TestUser", roles = { UserRole.PROF, UserRole.STUDENT })
    @JwtSecurity(claims = {
            @Claim(key = "email", value = "user@gmail.com"),
            @Claim(key = "thisIsATest", value = "true"),
    })
    public void testJWT() {
        String response = secureEndpoint.testJwt();
        Assertions.assertEquals(response, "TestUseruser@gmail.comtrue");
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

    @Test
    @TestSecurity(user = "Student", roles = {UserRole.STUDENT })
    @JwtSecurity(claims = { @Claim(key = "email", value = "student@htwg-konstanz.de") })
    public void createStudentUser() {
        try {
            Response response = userService.login("Basic U3R1ZGVudDo=");
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
            Assertions.assertEquals(defaultJWTCallerPrincipal.getClaim("full_name"), "Student");
            Assertions.assertTrue(defaultJWTCallerPrincipal.getClaim("sub").toString().length() > 0);
            this.profId = defaultJWTCallerPrincipal.getClaim("sub").toString(); // save id for later use
            Assertions.assertTrue(defaultJWTCallerPrincipal.getClaim("email").toString().length() > 0);
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }

    @Test
    @TestSecurity(user = "Prof", roles = { UserRole.PROF })
    @JwtSecurity(claims = { @Claim(key = "email", value = "prof@htwg-konstanz.de") })
    public void createACourse() {

        // create a course via the json api
        ApiCourse apiCourse1 = new ApiCourse(
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
                "AUME23",
                "1");
        apiService.updateCourses(List.of(apiCourse1));

        // get all courses
        List<Course> courses = courseService.getCourses("");
        Assertions.assertEquals(courses.size(), 1);
        Assertions.assertEquals(courses.get(0).getName(), "AUME 23/24");
        Assertions.assertEquals(courses.get(0).getDescription(), "Agile Vorgehensmodelle und Mobile Kommunikation");
        Assertions.assertEquals(courses.get(0).getFeedbackForms().size(), 1);
        Assertions.assertEquals(courses.get(0).getQuizForms().size(), 1);
        Assertions.assertEquals(courses.get(0).getFeedbackForms().get(0).getName(), "Erster Sprint");
        Assertions.assertEquals(courses.get(0).getQuizForms().get(0).getName(), "Rollenverständnis bei Scrum");
    }

    @Test
    @TestSecurity(user = "Prof", roles = { UserRole.PROF })
    @JwtSecurity(claims = { @Claim(key = "sub", value = "profId") })
    public void startFeedbackForm() {

        this.createProfUser();
        courseService.deleteAllCourses();
        this.createACourse();

        // get all courses
        List<Course> courses = courseService.getCourses("");
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

}
