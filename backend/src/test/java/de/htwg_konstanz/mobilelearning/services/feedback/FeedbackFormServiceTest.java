package de.htwg_konstanz.mobilelearning.services.feedback;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.bson.types.ObjectId;
import org.jose4j.jwt.JwtClaims;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import de.htwg_konstanz.mobilelearning.Helper;
import de.htwg_konstanz.mobilelearning.LiveFeedbackSocketClient;
import de.htwg_konstanz.mobilelearning.MockMongoTestProfile;
import de.htwg_konstanz.mobilelearning.enums.FormStatus;
import de.htwg_konstanz.mobilelearning.models.Course;
import de.htwg_konstanz.mobilelearning.models.QuestionWrapper;
import de.htwg_konstanz.mobilelearning.models.auth.UserRole;
import de.htwg_konstanz.mobilelearning.models.feedback.FeedbackForm;
import de.htwg_konstanz.mobilelearning.repositories.CourseRepository;
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
    private CourseRepository courseRepository;

    @Inject
    private ApiService apiService;

    @Inject
    private UserService userService;

    @Inject
    private FeedbackFormService feedbackFormService;

    @BeforeEach
    void init(TestInfo testInfo){
        System.out.println("------------------------------");
        System.out.println("Test: " + testInfo.getDisplayName());
        courseService.deleteAllCourses();
        userService.deleteAllUsers();
    }

    @Test
    public void getFeedbackFormWithoutResult() {
        //create & get courses + ids
        List<Course> courses = Helper.createCourse();
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
    public void getFeedbackFormWithResult() {
        //create & get courses + ids
        List<Course> courses = Helper.createCourse();
        String courseId = courses.getFirst().getId().toString();
        String formId = courses.getFirst().getFeedbackForms().get(0).getId().toString();
        String questionId = courses.getFirst().feedbackForms.get(0).questions.get(0).getId().toString();
        // add a result & get feedback forms
        addResult(courseId, formId, questionId);
        List<FeedbackForm> feedbackForms = feedbackFormService.getFeedbackForms(courses.get(0).id.toString());
        
        // Assert get feedback form with results
        FeedbackForm feedbackFormFromService = feedbackFormService.getFeedbackForm(courses.get(0).id.toString(), feedbackForms.get(0).id.toString(), true);
        Assertions.assertEquals("Erster Sprint", feedbackFormFromService.name);
        Assertions.assertEquals("Hier wollen wir Ihr Feedback zum ersten Sprint einholen", feedbackFormFromService.description);
        Assertions.assertEquals(1, feedbackFormFromService.questions.size());
        Assertions.assertEquals(1, feedbackFormFromService.questions.get(0).results.size());
        Assertions.assertEquals("5", feedbackFormFromService.questions.get(0).results.get(0).values.get(0));
    }

    @Test
    public void clearResults() {
        //create & get courses + ids
        List<Course> courses = Helper.createCourse();
        String courseId = courses.getFirst().getId().toString();
        String formId = courses.getFirst().getFeedbackForms().get(0).getId().toString();
        String questionId = courses.getFirst().feedbackForms.get(0).questions.get(0).getId().toString();
        // add a result & get feedback forms
        addResult(courseId, formId, questionId);
        FeedbackForm feedbackForm = feedbackFormService.getFeedbackForm(courses.get(0).id.toString(), formId, true);

        // need to manually add owner because anntation sub claim needs to be static and profId is different
        Course course = courseService.getCourse(courseId);
        ObjectId ownerId = new ObjectId("111111111111111111111111");
        course.addOwner(ownerId);
        courseRepository.update(course);

        // Assert that results were cleared
        Assertions.assertEquals(1, feedbackForm.questions.get(0).results.size());
        feedbackFormService.clearFeedbackFormResults(courses.get(0).id.toString(), feedbackForm.id.toString());
        FeedbackForm feedbackFormCleared = feedbackFormService.getFeedbackForm(courses.get(0).id.toString(), formId, true);
        Assertions.assertEquals(0, feedbackFormCleared.questions.get(0).results.size());
    }    
    
    @Test
    public void clearResultsNotOwner() {
        //create & get courses + ids
        List<Course> courses = Helper.createCourse();
        String courseId = courses.getFirst().getId().toString();
        String formId = courses.getFirst().getFeedbackForms().get(0).getId().toString();
        String questionId = courses.getFirst().feedbackForms.get(0).questions.get(0).getId().toString();
        // add a result & get feedback forms
        addResult(courseId, formId, questionId);
        FeedbackForm feedbackForm = feedbackFormService.getFeedbackForm(courses.get(0).id.toString(), formId, true);

        // Assert that results were not cleared (not owner)
        Assertions.assertEquals(1, feedbackForm.questions.get(0).results.size());
        feedbackFormService.clearFeedbackFormResults(courses.get(0).id.toString(), feedbackForm.id.toString());
        FeedbackForm feedbackFormCleared = feedbackFormService.getFeedbackForm(courses.get(0).id.toString(), formId, true);
        Assertions.assertEquals(1, feedbackFormCleared.questions.get(0).results.size());
    }

    @Test
    public void clearResultsForbidden() {
        //create & get courses + ids
        List<Course> courses = Helper.createCourse();
        String formId = courses.getFirst().getFeedbackForms().get(0).getId().toString();
        FeedbackForm feedbackForm = feedbackFormService.getFeedbackForm(courses.get(0).id.toString(), formId, true);

        Exception exception = Assertions.assertThrows(ForbiddenException.class, () -> {
            feedbackFormService.clearFeedbackFormResults(courses.get(0).id.toString(), feedbackForm.id.toString());
        });

        // students should not be able to clear results
        Assertions.assertEquals("io.quarkus.security.ForbiddenException", exception.getClass().getName());
    }

    @Test
    public void updateFeedbackForm() {
        //create & get courses + ids
        List<Course> courses = Helper.createCourse();
        String courseId = courses.getFirst().getId().toString();
        String formId = courses.getFirst().getFeedbackForms().get(0).getId().toString();
      
        // need to manually add owner because anntation sub claim needs to be static profId is different
        Course course = courseService.getCourse(courseId);
        ObjectId ownerId = new ObjectId("111111111111111111111111");
        course.addOwner(ownerId);
        courseRepository.update(course);

        // update the feedback form name, description and questions
        FeedbackForm feedbackFormUpdate = new FeedbackForm(courses.get(0).id, "nameUpdate", "descriptionUpdate", new ArrayList<QuestionWrapper>(), FormStatus.NOT_STARTED);
        feedbackFormService.updateFeedbackForm(courseId, formId, feedbackFormUpdate);
        
        // check if the feedback form was updated
        List<FeedbackForm> updatedFeedbackForms = feedbackFormService.getFeedbackForms(courses.get(0).id.toString());
        Assertions.assertEquals("nameUpdate", updatedFeedbackForms.get(0).name);
        Assertions.assertEquals("descriptionUpdate", updatedFeedbackForms.get(0).description);
        Assertions.assertEquals(0, updatedFeedbackForms.get(0).questions.size());
    }

    @Test
    public void updateFeedbackFormNotOwner() {
        //create & get courses + ids
        List<Course> courses = Helper.createCourse();
        String courseId = courses.getFirst().getId().toString();
        String formId = courses.getFirst().getFeedbackForms().get(0).getId().toString();

        // update the feedback form name, description and questions
        FeedbackForm feedbackFormUpdate = new FeedbackForm(courses.get(0).id, "nameUpdate", "descriptionUpdate", new ArrayList<QuestionWrapper>(), FormStatus.NOT_STARTED);
        feedbackFormService.updateFeedbackForm(courseId, formId, feedbackFormUpdate);
        
        // Assert that results were not cleared (not owner)
        List<FeedbackForm> updatedFeedbackForms = feedbackFormService.getFeedbackForms(courses.get(0).id.toString());
        Assertions.assertEquals("Erster Sprint", updatedFeedbackForms.get(0).name);
        Assertions.assertEquals("Hier wollen wir Ihr Feedback zum ersten Sprint einholen", updatedFeedbackForms.get(0).description);
        Assertions.assertEquals(1, updatedFeedbackForms.get(0).questions.size());
    }
    
    @Test
    public void updateFeedbackFormForbidden() {
        //create & get courses + ids
        List<Course> courses = Helper.createCourse();
        String courseId = courses.getFirst().getId().toString();
        String formId = courses.getFirst().getFeedbackForms().get(0).getId().toString();
        
        // update the feedback form name, description and questions
        FeedbackForm feedbackFormUpdate = new FeedbackForm(courses.get(0).id, "nameUpdate", "descriptionUpdate", new ArrayList<QuestionWrapper>(), FormStatus.NOT_STARTED);
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
}
