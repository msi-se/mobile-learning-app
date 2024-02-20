package de.htwg_konstanz.mobilelearning.services.quiz.socket;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jose4j.jwt.JwtClaims;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import com.fasterxml.jackson.databind.ObjectMapper;

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
import de.htwg_konstanz.mobilelearning.services.quiz.QuizFormService;
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
public class LiveQuizSocketTest {

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
    public void startQuizForm() {
        // create & get courses
        List<Course> courses = createCourse();
        Assertions.assertEquals(courses.size(), 1);
        Course course = courses.get(0);
        Assertions.assertEquals(course.getQuizForms().size(), 1);

        // get course and feedback form id
        String courseId = course.getId().toString();
        String formId = course.getQuizForms().get(0).getId().toString();

        // create a websocket client
        // (@ServerEndpoint("/course/{courseId}/quiz/form/{formId}/subscribe/{userId}/{jwt}")
        try {
            LiveFeedbackSocketClient client = new LiveFeedbackSocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + this.profId + "/" + profJwt)
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
            Assertions.assertTrue(courseService.getCourse(courseId).getQuizForms().get(0).getStatus().toString().equals("STARTED"));
        } catch (Exception e) {
            System.out.println(e);
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    @TestSecurity(user = "Prof", roles = { UserRole.PROF })
    @JwtSecurity(claims = { @Claim(key = "sub", value = "profId") })
    public void startQuizFormNotOwner() {
        // create & get courses
        List<Course> courses = createCourse();
        Assertions.assertEquals(courses.size(), 1);
        Course course = courses.get(0);
        Assertions.assertEquals(course.getQuizForms().size(), 1);

        // get course and feedback form id
        String courseId = course.getId().toString();
        String formId = course.getQuizForms().get(0).getId().toString();

        // create a websocket client
        // (@ServerEndpoint("/course/{courseId}/quiz/form/{formId}/subscribe/{userId}/{jwt}")
        try {
            LiveFeedbackSocketClient client = new LiveFeedbackSocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + this.profId2 + "/" + profJwt2)
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

            // check if the form status has not changed
            Assertions.assertTrue(courseService.getCourse(courseId).getQuizForms().get(0).getStatus().toString().equals("NOT_STARTED"));
        } catch (Exception e) {
            System.out.println(e);
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    @TestSecurity(user = "Prof", roles = { UserRole.PROF })
    @JwtSecurity(claims = { @Claim(key = "sub", value = "profId") })
    public void startQuizFormStudent() {
        // create & get courses
        List<Course> courses = createCourse();
        Assertions.assertEquals(courses.size(), 1);
        Course course = courses.get(0);
        Assertions.assertEquals(course.getQuizForms().size(), 1);

        // get course and feedback form id
        String courseId = course.getId().toString();
        String formId = course.getQuizForms().get(0).getId().toString();

        // create a websocket client
        // (@ServerEndpoint("/course/{courseId}/quiz/form/{formId}/subscribe/{userId}/{jwt}")
        try {
            LiveFeedbackSocketClient client = new LiveFeedbackSocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + this.profId + "/" + profJwt)
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

            // check if the form status has not changed
            Assertions.assertTrue(courseService.getCourse(courseId).getQuizForms().get(0).getStatus().toString().equals("NOT_STARTED"));
        } catch (Exception e) {
            System.out.println(e);
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    @TestSecurity(user = "Prof", roles = { UserRole.PROF})
    @JwtSecurity(claims = { @Claim(key = "email", value = "prof@htwg-konstanz.de") })
    public void addResult() {
        //create & get courses + ids
        List<Course> courses = createCourse();
        String courseId = courses.getFirst().getId().toString();
        String formId = courses.getFirst().getQuizForms().get(0).getId().toString();
        String questionId = courses.getFirst().quizForms.get(0).questions.get(0).getId().toString();
        
        // add a result & get quiz forms
        addResult(courseId, formId, questionId, "Prof");
        List<QuizForm> quizForms = quizFormService.getQuizForms(courses.get(0).id.toString());
        
        // check that the result was added
        QuizForm quizFormFromService = quizFormService.getQuizForm(courses.get(0).id.toString(), quizForms.get(0).id.toString(), true);
        Assertions.assertEquals("Rollenverständnis bei Scrum", quizFormFromService.name);
        Assertions.assertEquals("Ein Quiz zum Rollenverständnis und Teamaufbau bei Scrum", quizFormFromService.description);
        Assertions.assertEquals(2, quizFormFromService.questions.size());
        Assertions.assertEquals(1, quizFormFromService.questions.get(0).results.size());
        Assertions.assertEquals("1", quizFormFromService.questions.get(0).results.get(0).values.get(0));	
    }

    @Test
    @TestSecurity(user = "Prof", roles = { UserRole.PROF})
    @JwtSecurity(claims = { @Claim(key = "email", value = "prof@htwg-konstanz.de") })
    public void submitResultOnlyAcceptedOnce() {
        //create & get courses + ids
        List<Course> courses = createCourse();
        String courseId = courses.getFirst().getId().toString();
        String formId = courses.getFirst().getQuizForms().get(0).getId().toString();
        String questionId = courses.getFirst().quizForms.get(0).questions.get(0).getId().toString();
        
        // add a result & get quiz forms
        addResult(courseId, formId, questionId, "Prof");
        addResult(courseId, formId, questionId, "Prof");
        List<QuizForm> quizForms = quizFormService.getQuizForms(courses.get(0).id.toString());
        
        // check that only one result was added
        QuizForm quizFormFromService = quizFormService.getQuizForm(courses.get(0).id.toString(), quizForms.get(0).id.toString(), true);
        Assertions.assertEquals("Rollenverständnis bei Scrum", quizFormFromService.name);
        Assertions.assertEquals("Ein Quiz zum Rollenverständnis und Teamaufbau bei Scrum", quizFormFromService.description);
        Assertions.assertEquals(2, quizFormFromService.questions.size());
        Assertions.assertEquals(1, quizFormFromService.questions.get(0).results.size());
        Assertions.assertEquals("1", quizFormFromService.questions.get(0).results.get(0).values.get(0));	
    }

    @Test
    @TestSecurity(user = "Prof", roles = { UserRole.PROF })
    @JwtSecurity(claims = { @Claim(key = "sub", value = "profId") })
    public void stopQuizForm() {
        // create & get courses
        List<Course> courses = createCourse();
        Assertions.assertEquals(courses.size(), 1);
        Course course = courses.get(0);
        Assertions.assertEquals(course.getQuizForms().size(), 1);

        // get course and feedback form id
        String courseId = course.getId().toString();
        String formId = course.getQuizForms().get(0).getId().toString();

        // create a websocket client
        // (@ServerEndpoint("/course/{courseId}/quiz/form/{formId}/subscribe/{userId}/{jwt}")
        try {
            LiveFeedbackSocketClient client = new LiveFeedbackSocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + this.profId + "/" + profJwt)
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

            // check if the form status has changed
            Assertions.assertTrue(courseService.getCourse(courseId).getQuizForms().get(0).getStatus().toString().equals("FINISHED"));
        } catch (Exception e) {
            System.out.println(e);
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    @TestSecurity(user = "Prof", roles = { UserRole.PROF })
    @JwtSecurity(claims = { @Claim(key = "sub", value = "profId") })
    public void stopQuizFormNotOwner() {
        // create & get courses
        List<Course> courses = createCourse();
        Assertions.assertEquals(courses.size(), 1);
        Course course = courses.get(0);
        Assertions.assertEquals(course.getQuizForms().size(), 1);

        // get course and feedback form id
        String courseId = course.getId().toString();
        String formId = course.getQuizForms().get(0).getId().toString();

        // create a websocket client
        // (@ServerEndpoint("/course/{courseId}/quiz/form/{formId}/subscribe/{userId}/{jwt}")
        try {
            LiveFeedbackSocketClient client = new LiveFeedbackSocketClient();
            LiveFeedbackSocketClient client2 = new LiveFeedbackSocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + this.profId + "/" + profJwt)
            );Session session2 = ContainerProvider.getWebSocketContainer().connectToServer(
                client2,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + this.profId2 + "/" + profJwt2)
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

            // check if the form was started & status has not changed
            Assertions.assertTrue(courseService.getCourse(courseId).getQuizForms().get(0).getStatus().toString().equals("STARTED"));
        } catch (Exception e) {
            System.out.println(e);
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    @TestSecurity(user = "Prof", roles = { UserRole.PROF })
    @JwtSecurity(claims = { @Claim(key = "sub", value = "profId") })
    public void stopQuizFormStudent() {
        // create & get courses
        List<Course> courses = createCourse();
        Assertions.assertEquals(courses.size(), 1);
        Course course = courses.get(0);
        Assertions.assertEquals(course.getQuizForms().size(), 1);

        // get course and feedback form id
        String courseId = course.getId().toString();
        String formId = course.getQuizForms().get(0).getId().toString();

        // create a websocket client
        // (@ServerEndpoint("/course/{courseId}/quiz/form/{formId}/subscribe/{userId}/{jwt}")
        try {
            LiveFeedbackSocketClient client = new LiveFeedbackSocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + this.profId + "/" + profJwt)
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
                    "roles": [Student]
                }
            """);
            Thread.sleep(1000);
            session.close();

            // check if the form was started & status has not changed
            Assertions.assertTrue(courseService.getCourse(courseId).getQuizForms().get(0).getStatus().toString().equals("STARTED"));
        } catch (Exception e) {
            System.out.println(e);
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    @TestSecurity(user = "Prof", roles = { UserRole.PROF })
    @JwtSecurity(claims = { @Claim(key = "sub", value = "profId") })
    public void clearResults() {
        // create & get courses
        List<Course> courses = createCourse();
        Assertions.assertEquals(courses.size(), 1);
        Course course = courses.get(0);
        Assertions.assertEquals(course.getQuizForms().size(), 1);

        // get course and feedback form id
        String courseId = course.getId().toString();
        String formId = course.getQuizForms().get(0).getId().toString();
        String questionId = courses.getFirst().quizForms.get(0).questions.get(0).getId().toString();

        // create a websocket client
        // (@ServerEndpoint("/course/{courseId}/quiz/form/{formId}/subscribe/{userId}/{jwt}")
        try {
            LiveFeedbackSocketClient client = new LiveFeedbackSocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + this.profId + "/" + profJwt)
            );
            client.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "STARTED",
                    "roles": [Prof]
                }
            """);
            Thread.sleep(100);
            // adds result to quiz form
            client.sendMessage(String.format("""
                {
                    "action": "ADD_RESULT",
                    "resultElementId": %s,
                    "resultValues": ["1"],
                    "role": "STUDENT"
                }
            """, questionId));
            Thread.sleep(100);
            QuizForm quizForm = quizFormService.getQuizForms(courses.get(0).id.toString()).get(0);
            Assertions.assertEquals(1, quizForm.questions.get(0).results.size());
            Assertions.assertEquals("1", quizForm.questions.get(0).results.get(0).values.get(0));	
            client.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "FINISHED",
                    "roles": [Prof]
                }
            """);
            Thread.sleep(100);
            client.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "NOT_STARTED",
                    "roles": [Prof]
                }
            """);
            Thread.sleep(1000);
            session.close();

            quizForm = quizFormService.getQuizForms(courses.get(0).id.toString()).get(0);
            // form should be cleared after status is set to NOT_STARTED
            Assertions.assertEquals(0, quizForm.questions.get(0).results.size());	
        } catch (Exception e) {
            System.out.println(e);
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    @TestSecurity(user = "Prof", roles = { UserRole.PROF })
    @JwtSecurity(claims = { @Claim(key = "sub", value = "profId") })
    public void nextQuestion() {
        // create & get courses
        List<Course> courses = createCourse();
        ObjectMapper mapper = new ObjectMapper();
        Assertions.assertEquals(courses.size(), 1);
        Course course = courses.get(0);
        Assertions.assertEquals(course.getQuizForms().size(), 1);

        // get course and feedback form id
        String courseId = course.getId().toString();
        String formId = course.getQuizForms().get(0).getId().toString();

        // create a websocket client
        // (@ServerEndpoint("/course/{courseId}/quiz/form/{formId}/subscribe/{userId}/{jwt}")
        try {
            LiveFeedbackSocketClient client = new LiveFeedbackSocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + this.profId + "/" + profJwt)
            );
            client.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "STARTED",
                    "roles": [Prof]
                }
            """);
            Thread.sleep(500);
            client.sendMessage("""
                {
                    "action": "NEXT",
                    "roles": [Prof]
                }
            """); 
            Thread.sleep(500);
            // newest messagqueue item after first next should be CLOSED_QUESTION
            Map<String, String> next1 = mapper.readerFor(Map.class).readValue(client.getMessageQueue().get(1));
            Assertions.assertEquals("CLOSED_QUESTION", next1.get("action"));
            Object obj1 = next1.get("form");  // replace with your object
            LinkedHashMap<String, Object> form1 = (LinkedHashMap<String, Object>) obj1;
            Assertions.assertEquals(true, form1.get("currentQuestionFinished"));
            Assertions.assertEquals(0, form1.get("currentQuestionIndex"));

            client.sendMessage("""
                {
                    "action": "NEXT",
                    "roles": [Prof]
                }
            """);
            Thread.sleep(500);
            // newest messagqueue item after second next should be OPENED_NEXT_QUESTION
            Map<String, String> next2 = mapper.readerFor(Map.class).readValue(client.getMessageQueue().get(2));
            Assertions.assertEquals("OPENED_NEXT_QUESTION", next2.get("action"));
            client.sendMessage("""
                {
                    "action": "NEXT",
                    "roles": [Prof]
                }
            """);
            Thread.sleep(500);
            // newest messagqueue item after third next should be CLOSED_QUESTION
            Map<String, String> next3 = mapper.readerFor(Map.class).readValue(client.getMessageQueue().get(3));
            Assertions.assertEquals("CLOSED_QUESTION", next3.get("action"));
            client.sendMessage("""
                {
                    "action": "NEXT",
                    "roles": [Prof]
                }
            """);
            Thread.sleep(500);
            // newest messagqueue items after fourth next should be CLOSED_QUESTION & FINISHED
            Map<String, String> next4 = mapper.readerFor(Map.class).readValue(client.getMessageQueue().get(4));
            Assertions.assertEquals("CLOSED_QUESTION", next4.get("action"));
            Map<String, String> next5 = mapper.readerFor(Map.class).readValue(client.getMessageQueue().get(5));
            Assertions.assertEquals("FORM_STATUS_CHANGED", next5.get("action"));
            Assertions.assertEquals("FINISHED", next5.get("formStatus"));

            Thread.sleep(1000);
            session.close();

            // check if the form status has changed
            Assertions.assertTrue(courseService.getCourse(courseId).getQuizForms().get(0).getStatus().toString().equals("FINISHED"));
        } catch (Exception e) {
            System.out.println(e);
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    @TestSecurity(user = "Prof", roles = { UserRole.PROF })
    @JwtSecurity(claims = { @Claim(key = "sub", value = "profId") })
    public void nextQuestionNotOnwer() {
        // create & get courses
        List<Course> courses = createCourse();
        Assertions.assertEquals(courses.size(), 1);
        Course course = courses.get(0);
        Assertions.assertEquals(course.getQuizForms().size(), 1);

        // get course and feedback form id
        String courseId = course.getId().toString();
        String formId = course.getQuizForms().get(0).getId().toString();

        // create a websocket client
        // (@ServerEndpoint("/course/{courseId}/quiz/form/{formId}/subscribe/{userId}/{jwt}")
        try {
            // Owner stats feedback & 2nd prof (not owner) tries to change question
            LiveFeedbackSocketClient client = new LiveFeedbackSocketClient();
            LiveFeedbackSocketClient client2 = new LiveFeedbackSocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + this.profId + "/" + profJwt)
            );            
            Session session2 = ContainerProvider.getWebSocketContainer().connectToServer(
                client2,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + this.profId2 + "/" + profJwt2)
            );
            client.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "STARTED",
                    "roles": [Prof]
                }
            """);
            Thread.sleep(500);
            client2.sendMessage("""
                {
                    "action": "NEXT",
                    "roles": [Prof]
                }
            """); 
            Thread.sleep(500);
            Assertions.assertEquals(0 , client2.getMessageQueue().size());
            session.close();    
            session2.close();    

            // check that the form status has not changed
            Assertions.assertTrue(courseService.getCourse(courseId).getQuizForms().get(0).getStatus().toString().equals("STARTED"));
        } catch (Exception e) {
            System.out.println(e);
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    @TestSecurity(user = "Prof", roles = { UserRole.PROF })
    @JwtSecurity(claims = { @Claim(key = "sub", value = "profId") })
    public void nextQuestionStudent() {
        // create & get courses
        List<Course> courses = createCourse();
        Assertions.assertEquals(courses.size(), 1);
        Course course = courses.get(0);
        Assertions.assertEquals(course.getQuizForms().size(), 1);

        // get course and feedback form id
        String courseId = course.getId().toString();
        String formId = course.getQuizForms().get(0).getId().toString();

        // create a websocket client
        // (@ServerEndpoint("/course/{courseId}/quiz/form/{formId}/subscribe/{userId}/{jwt}")
        try {
            // Owner stats feedback & 2nd prof (with student role) tries to change question
            LiveFeedbackSocketClient client = new LiveFeedbackSocketClient();
            LiveFeedbackSocketClient client2 = new LiveFeedbackSocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                client,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + this.profId + "/" + profJwt)
            );            
            Session session2 = ContainerProvider.getWebSocketContainer().connectToServer(
                client2,
                URI.create("ws://localhost:8081/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + this.profId2 + "/" + profJwt2)
            );
            client.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "STARTED",
                    "roles": [Prof]
                }
            """);
            Thread.sleep(500);
            client2.sendMessage("""
                {
                    "action": "NEXT",
                    "roles": [Student]
                }
            """); 
            Thread.sleep(500);
            Assertions.assertEquals(0 , client2.getMessageQueue().size());
            session.close();    
            session2.close();    

            // check that the form status has not changed
            Assertions.assertTrue(courseService.getCourse(courseId).getQuizForms().get(0).getStatus().toString().equals("STARTED"));
        } catch (Exception e) {
            System.out.println(e);
            Assertions.fail(e.getMessage());
        }
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
                                        new ArrayList<String>(),
                                        "F-Q-ROLLE",
                                        "gut",
                                        "schlecht")),
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
                                        "Q-Q-PDRODUCTOWNER"),
                                        new ApiQuizQuestion(
                                        "Product Owner",
                                        "Test Frage?",
                                        "SINGLE_CHOICE",
                                        List.of(
                                                "Antwort 1",
                                                "Antwort 2",
                                                "Antwort 3"),
                                        true,
                                        List.of("2"),
                                        "Q-Q-PDRODUCTOWNER")),
                        "Q-ROLES")),
        "AUME23",
        "1");
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
