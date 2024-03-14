package de.htwg_konstanz.mobilelearning.services;

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
import de.htwg_konstanz.mobilelearning.models.FormShell;
import de.htwg_konstanz.mobilelearning.services.auth.UserService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Session;

@QuarkusTest
@TestProfile(MockMongoTestProfile.class)
public class LiveServiceTest {
    

    @Inject
    private CourseService courseService;

    @Inject UserService userService;

    @BeforeEach
    void init(TestInfo testInfo) {
        System.out.println("------------------------------");
        System.out.println("Test: " + testInfo.getDisplayName());
        courseService.deleteAllCourses();
        userService.deleteAllUsers();
    }

    @Test
    public void testNoLiveForms() {

        // create mock users and courses
        MockUser mockProf = Helper.createMockUser("Prof1");
        List<Course> courses = Helper.createCourse("Prof1");
        Assertions.assertEquals(1, courses.size());
        MockUser mockStudent = Helper.createMockUser("Student1");

        // fetch the live service -> no live forms
        Response response = given().auth().oauth2(mockStudent.getJwt()).when().get("/live");
        response.then().statusCode(200);
        Assertions.assertEquals("[]", response.getBody().asString());
    }

    // helper function to convert the live response to a array of FormShell objects
    private List<FormShell> convertToFormShellArray(Response response) {

        List<FormShell> forms = response
        .then()
        .statusCode(200)
        .extract()
        .body()
        .jsonPath()
        .getList(".", FormShell.class);

        return forms;
    }

    @Test
    public void testLiveForms() {

        // create mock users and courses
        MockUser mockProf = Helper.createMockUser("Prof1");
        List<Course> courses = Helper.createCourse("Prof1");
        String courseId = courses.get(0).getId().toString();
        String feedbackFormId = courses.get(0).getFeedbackForms().get(0).getId().toString();
        String quizFormId = courses.get(0).getQuizForms().get(0).getId().toString();
        Assertions.assertEquals(1, courses.size());
        MockUser mockStudent = Helper.createMockUser("Student1");


        // try catch block to handle exceptions of websocket connection
        try {

            // create websocket client
            SocketClient profClient = new SocketClient();

            // connect the prof to the feedback form
            Session profSession = ContainerProvider.getWebSocketContainer().connectToServer(
                profClient,
                URI.create("ws://localhost:8081/course/" + courseId + "/feedback/form/" + feedbackFormId + "/subscribe/" + mockProf.getId() + "/" + mockProf.getJwt())
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        // fetch the live service -> live forms
        Response response = given().auth().oauth2(mockStudent.getJwt()).when().get("/live");
        List<FormShell> forms = convertToFormShellArray(response);
        Assertions.assertEquals(1, forms.size());
        Assertions.assertEquals("WAITING", forms.get(0).getStatus().toString());
        Assertions.assertEquals("Erster Sprint", forms.get(0).getName());
        Assertions.assertEquals("feedback", forms.get(0).getType());
        Assertions.assertEquals("AUME 23/24", forms.get(0).getCourse());

        // try catch block to handle exceptions of websocket connection
        try {

            // create websocket client
            SocketClient profClient = new SocketClient();

            // connect the prof to the quiz form
            Session profSession = ContainerProvider.getWebSocketContainer().connectToServer(
                profClient,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + quizFormId + "/subscribe/" + mockProf.getId() + "/" + mockProf.getJwt())
            );
            Thread.sleep(100);
            Assertions.assertTrue(profSession.isOpen());

            // set the form status to "WAITING" and check if it was set
            profClient.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "STARTED"
                }
            """);
            Thread.sleep(100);
            Assertions.assertEquals("STARTED", courseService.getCourse(courseId).getQuizForms().get(0).getStatus().toString());
            Assertions.assertEquals(0, courseService.getCourse(courseId).getQuizForms().get(0).getParticipants().size());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // fetch the live service -> live forms
        response = given().auth().oauth2(mockStudent.getJwt()).when().get("/live");
        forms = convertToFormShellArray(response);
        Assertions.assertEquals(2, forms.size());
        Assertions.assertEquals("WAITING", forms.get(0).getStatus().toString());
        Assertions.assertEquals("Erster Sprint", forms.get(0).getName());
        Assertions.assertEquals("feedback", forms.get(0).getType());
        Assertions.assertEquals("AUME 23/24", forms.get(0).getCourse());
        Assertions.assertEquals("STARTED", forms.get(1).getStatus().toString());
        Assertions.assertEquals("Rollenverständnis bei Scrum", forms.get(1).getName());
        Assertions.assertEquals("quiz", forms.get(1).getType());
        Assertions.assertEquals("AUME 23/24", forms.get(1).getCourse());

        // check that the connect code is not empty
        Assertions.assertNotNull(forms.get(0).getConnectCode());
        Assertions.assertNotNull(forms.get(1).getConnectCode());
    }
}
