package de.htwg_konstanz.mobilelearning.services;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.htwg_konstanz.mobilelearning.models.Course;
import de.htwg_konstanz.mobilelearning.models.auth.UserRole;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;

import jakarta.inject.Inject;

@QuarkusTest
public class CourseServiceTest {

    @Inject
    private CourseService courseService;

    @Test
    @TestSecurity(user = "testUser", roles = { UserRole.PROF, UserRole.STUDENT })
    @JwtSecurity(claims = {
            @Claim(key = "email", value = "user@gmail.com")
    })
    public void testGetAllCourses() {

        // get all courses
        List<Course> courses = courseService.getCourses();

        // check if the list is not empty
        Assertions.assertFalse(courses.isEmpty());
    }

}
