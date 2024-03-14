package de.htwg_konstanz.mobilelearning.services;

import static io.restassured.RestAssured.given;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import de.htwg_konstanz.mobilelearning.Helper;
import de.htwg_konstanz.mobilelearning.MockMongoTestProfile;
import de.htwg_konstanz.mobilelearning.MockUser;
import de.htwg_konstanz.mobilelearning.models.Course;
import de.htwg_konstanz.mobilelearning.models.feedback.FeedbackForm;
import de.htwg_konstanz.mobilelearning.models.quiz.QuizForm;
import de.htwg_konstanz.mobilelearning.repositories.CourseRepository;
import de.htwg_konstanz.mobilelearning.repositories.UserRepository;
import de.htwg_konstanz.mobilelearning.services.auth.UserService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.response.Response;
import jakarta.inject.Inject;

@QuarkusTest
@TestProfile(MockMongoTestProfile.class)
public class ApiServiceTest {

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
    public void testAddCourseViaJsonApi() throws InterruptedException {

        // create mock users
        MockUser prof = Helper.createMockUser("Prof-1");
        Thread.sleep(100);

        // check that no courses exist
        List<Course> courses = given().header("Authorization", "Bearer " + prof.getJwt()).get("/course").then().statusCode(200).extract().body().jsonPath().getList(".", Course.class);
        Assertions.assertEquals(0, courses.size());

        // add a course via json api (/public)
        String json = """
            [
                {
                  "key": "AUME23",
                  "name": "AUME 23/24",
                  "description": "Agile Vorgehensmodelle und Mobile Kommunikation",
                  "moodleCourseId": "1",
                  "feedbackForms": [],
                  "quizForms": []
                }
              ]
            """;

        Response response = given()
            .header("Authorization", "Bearer " + prof.getJwt())
            .contentType("application/json")
            .body(json)
            .patch("/public/courses");
        response.then().statusCode(200);

        // check that the course was added
        courses = given().header("Authorization", "Bearer " + prof.getJwt()).get("/course").then().statusCode(200).extract().body().jsonPath().getList(".", Course.class);
        Assertions.assertEquals(1, courses.size());
        Course course = courses.get(0);
        Assertions.assertEquals("AUME 23/24", course.getName());
        Assertions.assertEquals("AUME23", course.getKey());
        Assertions.assertEquals("Agile Vorgehensmodelle und Mobile Kommunikation", course.getDescription());
        Assertions.assertEquals("1", course.getMoodleCourseId());
        Assertions.assertEquals(prof.getId(), course.getOwners().get(0).toString());
        Assertions.assertEquals(1, course.getOwners().size());
    }

    @Test
    public void testUpdateCourseViaJsonApi() throws InterruptedException {

        // create mock users
        MockUser prof = Helper.createMockUser("Prof-1");
        Thread.sleep(100);

        // add a course via json api (/public)
        this.testAddCourseViaJsonApi();

        // update the course via json api
        String json = """
            [
                {
                  "key": "AUME23",
                  "name": "AUME 23/24",
                  "description": "Edited description",
                  "moodleCourseId": "1",
                  "feedbackForms": [],
                  "quizForms": []
                }
              ]
            """;

        Response response = given()
            .header("Authorization", "Bearer " + prof.getJwt())
            .contentType("application/json")
            .body(json)
            .patch("/public/courses");
        response.then().statusCode(200);

        // check that the course was updated
        List<Course> courses = given().header("Authorization", "Bearer " + prof.getJwt()).get("/course").then().statusCode(200).extract().body().jsonPath().getList(".", Course.class);
        Assertions.assertEquals(1, courses.size());
        Course course = courses.get(0);
        Assertions.assertEquals("AUME 23/24", course.getName());
        Assertions.assertEquals("AUME23", course.getKey());
        Assertions.assertEquals("Edited description", course.getDescription());
        Assertions.assertEquals("1", course.getMoodleCourseId());
        Assertions.assertEquals(prof.getId(), course.getOwners().get(0).toString());
        Assertions.assertEquals(1, course.getOwners().size());
    }

    @Test
    public void testUpdateTheFormsViaJsonApi() throws InterruptedException {

        // create mock users
        MockUser prof = Helper.createMockUser("Prof-1");
        Thread.sleep(100);

        // add a course via json api (/public)
        this.testAddCourseViaJsonApi();

        // get the course
        List<Course> courses = given().header("Authorization", "Bearer " + prof.getJwt()).get("/course").then().statusCode(200).extract().body().jsonPath().getList(".", Course.class);
        Assertions.assertEquals(1, courses.size());
        Course course = courses.get(0);

        // update the forms via json api
        String json = """
            [
                {
                    "key": "AUME23",
                    "name": "AUME 23/24",
                    "description": "Agile Vorgehensmodelle und Mobile Kommunikation",
                    "moodleCourseId": "940",
                    "feedbackForms": [
                        {
                            "key": "F-ERSTERSPRINT",
                            "name": "Feedback zum ersten Sprint",
                            "description": "Hier wollen wir Ihr Feedback zum ersten Sprint einholen",
                            "questions": [
                                {
                                    "key": "F-Q-INHALTE",
                                    "name": "Inhalte",
                                    "description": "Wie zufrieden sind Sie mit den Inhalten der Vorlesung bisher?",
                                    "type": "SLIDER",
                                    "rangeLow": "Nicht zufrieden",
                                    "rangeHigh": "Sehr zufrieden"
                                },
                                {
                                    "key": "F-Q-INHALTE-3",
                                    "name": "Inhalte (gefehlt)",
                                    "description": "Welche Inhalte haben Ihnen gefehlt?",
                                    "type": "FULLTEXT"
                                }
                            ]
                        }
                    ],
                    "quizForms": [
                        {
                            "key": "Q-ROLES",
                            "name": "Rollenverständnis bei Scrum",
                            "description": "Ein Quiz zum Rollenverständnis und Teamaufbau bei Scrum",
                            "questions": [
                                {
                                    "key": "Q-Q-PDRODUCTOWNER",
                                    "name": "Product Owner",
                                    "description": "Welche der folgenden Aufgaben ist nicht Teil der Rolle des Product Owners?",
                                    "type": "SINGLE_CHOICE",
                                    "options": [
                                        "Erstellung des Product Backlogs",
                                        "Priorisierung des Product Backlogs",
                                        "Pizza bestellen für jedes Daily"
                                    ],
                                    "hasCorrectAnswers": true,
                                    "correctAnswers": [ "2" ]
                                }
                            ]
                        }
                    ]
                }
            ]
            """;

        Response response = given()
            .header("Authorization", "Bearer " + prof.getJwt())
            .contentType("application/json")
            .body(json)
            .patch("/public/courses");
        response.then().statusCode(200);

        // check that the course was updated
        courses = given().header("Authorization", "Bearer " + prof.getJwt()).get("/course").then().statusCode(200).extract().body().jsonPath().getList(".", Course.class);
        Assertions.assertEquals(1, courses.size());
        course = courses.get(0);
        Assertions.assertEquals("AUME 23/24", course.getName());
        Assertions.assertEquals("AUME23", course.getKey());
        Assertions.assertEquals("Agile Vorgehensmodelle und Mobile Kommunikation", course.getDescription());
        Assertions.assertEquals("940", course.getMoodleCourseId());
        Assertions.assertEquals(prof.getId(), course.getOwners().get(0).toString());
        Assertions.assertEquals(1, course.getOwners().size());
        Assertions.assertEquals(1, course.getFeedbackForms().size());
        Assertions.assertEquals(1, course.getQuizForms().size());
        Assertions.assertEquals("Feedback zum ersten Sprint", course.getFeedbackForms().get(0).getName());
        Assertions.assertEquals("Rollenverständnis bei Scrum", course.getQuizForms().get(0).getName());

        // check the feedback form
        FeedbackForm feedbackForm = course.getFeedbackForms().get(0);
        Assertions.assertEquals("F-ERSTERSPRINT", feedbackForm.getKey());
        Assertions.assertEquals("Feedback zum ersten Sprint", feedbackForm.getName());
        Assertions.assertEquals("Hier wollen wir Ihr Feedback zum ersten Sprint einholen", feedbackForm.getDescription());

        // for the questions we have to fetch the feedback form directly
        FeedbackForm feedbackFormFromGet = given().header("Authorization", "Bearer " + prof.getJwt()).get("/course/" + course.getId().toHexString() + "/feedback/form/" + feedbackForm.getId().toHexString()).then().statusCode(200).extract().body().as(FeedbackForm.class);
        Assertions.assertEquals(2, feedbackFormFromGet.getQuestions().size());
        Assertions.assertEquals("Inhalte", feedbackFormFromGet.getQuestions().get(0).questionContent.getName());
        Assertions.assertEquals("Inhalte (gefehlt)", feedbackFormFromGet.getQuestions().get(1).questionContent.getName());

        // check the quiz form
        QuizForm quizForm = course.getQuizForms().get(0);
        Assertions.assertEquals("Q-ROLES", quizForm.getKey());
        Assertions.assertEquals("Rollenverständnis bei Scrum", quizForm.getName());
        Assertions.assertEquals("Ein Quiz zum Rollenverständnis und Teamaufbau bei Scrum", quizForm.getDescription());

        // for the questions we have to fetch the quiz form directly
        QuizForm quizFormFromGet = given().header("Authorization", "Bearer " + prof.getJwt()).get("/course/" + course.getId().toHexString() + "/quiz/form/" + quizForm.getId().toHexString()).then().statusCode(200).extract().body().as(QuizForm.class);
        Assertions.assertEquals(1, quizFormFromGet.getQuestions().size());
        Assertions.assertEquals("Product Owner", quizFormFromGet.getQuestions().get(0).questionContent.getName());

    }



}
