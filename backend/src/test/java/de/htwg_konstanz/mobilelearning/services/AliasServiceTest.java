
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
public class AliasServiceTest {

    @BeforeEach
    void init(TestInfo testInfo) {
        System.out.println("------------------------------");
        System.out.println("Test: " + testInfo.getDisplayName());
    }

    @Test
    public void testGetRandomAlias() {
        Response response = given().when().get("/funnyalias");
        response.then().statusCode(200);
        String alias = response.getBody().asString();
        
        // check that alias is not null and at least 3 characters long
        Assertions.assertNotNull(alias);
        Assertions.assertTrue(alias.length() >= 3);
    }
}