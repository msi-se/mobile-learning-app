package de.htwg_konstanz.mobilelearning.services;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.htwg_konstanz.mobilelearning.MockMongoTestProfile;
import de.htwg_konstanz.mobilelearning.models.Course;
import de.htwg_konstanz.mobilelearning.models.auth.UserRole;
import de.htwg_konstanz.mobilelearning.test.SecureEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;

import jakarta.inject.Inject;

@QuarkusTest
@TestProfile(MockMongoTestProfile.class)
public class CourseServiceTest {

    @Inject
    private CourseService courseService;

    // just for jwt testing
    @Inject
    private SecureEndpoint secureEndpoint;

    @Test
    @TestSecurity(user = "testUser", roles = { UserRole.PROF, UserRole.STUDENT })
    @JwtSecurity(claims = {})
    public void testGetAllCourses() {

        // delete all courses
        courseService.deleteAllCourses();

        // get all courses
        List<Course> courses = courseService.getCourses();
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

}
