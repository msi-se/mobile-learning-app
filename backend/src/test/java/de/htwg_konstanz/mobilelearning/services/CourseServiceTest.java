package de.htwg_konstanz.mobilelearning.services;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import java.net.URI;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import de.htwg_konstanz.mobilelearning.Helper;
import de.htwg_konstanz.mobilelearning.SocketClient;
import de.htwg_konstanz.mobilelearning.MockMongoTestProfile;
import de.htwg_konstanz.mobilelearning.MockUser;
import de.htwg_konstanz.mobilelearning.models.Course;
import de.htwg_konstanz.mobilelearning.models.auth.User;
import de.htwg_konstanz.mobilelearning.models.quiz.QuizForm;
import de.htwg_konstanz.mobilelearning.repositories.CourseRepository;
import de.htwg_konstanz.mobilelearning.repositories.UserRepository;
import de.htwg_konstanz.mobilelearning.services.CourseService;
import de.htwg_konstanz.mobilelearning.services.auth.UserService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Session;

@QuarkusTest
@TestProfile(MockMongoTestProfile.class)
public class CourseServiceTest {

    @Inject
    private CourseService courseService;   

    @Inject
    private CourseRepository courseRepository;

    @Inject
    private UserService userService;

    @Inject
    private UserRepository userRepository;

    @BeforeEach
    void init(TestInfo testInfo) {
        System.out.println("------------------------------");
        System.out.println("Test: " + testInfo.getDisplayName());
        courseService.deleteAllCourses();
        userService.deleteAllUsers();
    }

    @Test
    public void testNoCourses() throws InterruptedException {

        MockUser student = Helper.createMockUser("Student-1");
        Thread.sleep(100);
        Response response = given()
            .header("Authorization", "Bearer " + student.getJwt())
            .get("/course");
        List<Course> courses = response
            .then()
            .statusCode(200)
            .extract()
            .body()
            .jsonPath()
            .getList(".", Course.class);
        Assertions.assertEquals(0, courses.size());
    }

    @Test
    public void testCourseWhenMoodleMatchs() throws InterruptedException {

        // create a prof and a student
        MockUser prof = Helper.createMockUser("Prof-1");
        MockUser student = Helper.createMockUser("Student-1");
        Thread.sleep(100);

        // create a course with a moodle course id which the student has (for mock users its 1, 2, 3, 4, 5)
        List<Course> courses = Helper.createCourse("Prof-1", "1");
        Thread.sleep(100);
        Assertions.assertEquals(1, courses.size());

        // get the course with the student
        Response response = given()
            .header("Authorization", "Bearer " + student.getJwt())
            .get("/course");
        courses = response
            .then()
            .statusCode(200)
            .extract()
            .body()
            .jsonPath()
            .getList(".", Course.class);
        Assertions.assertEquals(1, courses.size());

        // check via the courseRepository and the userRespository if the student is in the course now
        Course course = courseRepository.findById(courses.get(0).getId());
        Assertions.assertTrue(course.isStudent(student.getId()));
        User user = userRepository.findById(new ObjectId(student.getId()));
        Assertions.assertTrue(user.hasCourse(courses.get(0).getId()));

        // check if the coursename and the feedbackform / quizform name is correct
        Assertions.assertEquals("AUME 23/24", courses.get(0).getName());
        Assertions.assertEquals("Erster Sprint", courses.get(0).getFeedbackForms().get(0).getName());
        Assertions.assertEquals("Rollenverständnis bei Scrum", courses.get(0).getQuizForms().get(0).getName());
    }

    @Test
    public void testNoCourseWhenMoodleDoesNotMatch() throws InterruptedException {

        // create a prof and a student
        MockUser prof = Helper.createMockUser("Prof-1");
        MockUser student = Helper.createMockUser("Student-1");
        Thread.sleep(100);

        // create a course with a moodle course id which the student does not have
        List<Course> courses = Helper.createCourse("Prof-1", "100");
        Thread.sleep(100);
        Assertions.assertEquals(1, courses.size());

        // get the course with the student
        Response response = given()
            .header("Authorization", "Bearer " + student.getJwt())
            .get("/course");
        courses = response
            .then()
            .statusCode(200)
            .extract()
            .body()
            .jsonPath()
            .getList(".", Course.class);
        Assertions.assertEquals(0, courses.size());
    }

    @Test
    public void testNoCourseWhenCourseIsNotLinkedToMoodle() throws InterruptedException {

        // create a prof and a student
        MockUser prof = Helper.createMockUser("Prof-1");
        MockUser student = Helper.createMockUser("Student-1");
        Thread.sleep(100);

        // create a course with a moodle course id which the student does not have
        List<Course> courses = Helper.createCourse("Prof-1", "");
        Thread.sleep(100);

        Assertions.assertEquals(1, courses.size());

        // get the course with the student
        Response response = given()
            .header("Authorization", "Bearer " + student.getJwt())
            .get("/course");
        courses = response
            .then()
            .statusCode(200)
            .extract()
            .body()
            .jsonPath()
            .getList(".", Course.class);
        Assertions.assertEquals(0, courses.size());
    }

    @Test
    public void testCourseWhenCourseIsNotLinkedToMoodleButStudentJoinedTheCourseViaId() throws InterruptedException {

        // create a prof and a student
        MockUser prof = Helper.createMockUser("Prof-1");
        MockUser student = Helper.createMockUser("Student-1");
        Thread.sleep(100);

        // create a course with a moodle course id which the student does not have
        List<Course> courses = Helper.createCourse("Prof-1", "");
        Assertions.assertEquals(1, courses.size());
        Thread.sleep(100);

        // student joins the course with the course id which he gets from the prof
        Response joinResponse = given()
            .header("Authorization", "Bearer " + student.getJwt())
            .post("/course/" + courses.get(0).getId().toHexString() + "/join");
        joinResponse
            .then()
            .statusCode(200);
        Thread.sleep(100);
            
        // check via the courseRepository and the userRespository if the student is in the course now
        Course course = courseRepository.findById(courses.get(0).getId());
        Assertions.assertTrue(course.isStudent(student.getId()));
        User user = userRepository.findById(new ObjectId(student.getId()));
        Assertions.assertTrue(user.hasCourse(courses.get(0).getId()));
        
        // get the course with the student
        Response response = given()
            .header("Authorization", "Bearer " + student.getJwt())
            .get("/course");
        courses = response
            .then()
            .statusCode(200)
            .extract()
            .body()
            .jsonPath()
            .getList(".", Course.class);
        Thread.sleep(100);
        Assertions.assertEquals(1, courses.size());

        // check if the coursename and the feedbackform / quizform name is correct
        Assertions.assertEquals("AUME 23/24", courses.get(0).getName());
        Assertions.assertEquals("Erster Sprint", courses.get(0).getFeedbackForms().get(0).getName());
        Assertions.assertEquals("Rollenverständnis bei Scrum", courses.get(0).getQuizForms().get(0).getName());
        
    }


}
