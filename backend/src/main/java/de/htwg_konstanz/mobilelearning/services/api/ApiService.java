package de.htwg_konstanz.mobilelearning.services.api;

import java.util.List;

import org.eclipse.microprofile.jwt.JsonWebToken;

import de.htwg_konstanz.mobilelearning.models.Course;
import de.htwg_konstanz.mobilelearning.models.auth.User;
import de.htwg_konstanz.mobilelearning.repositories.CourseRepository;
import de.htwg_konstanz.mobilelearning.repositories.UserRepository;
import de.htwg_konstanz.mobilelearning.services.api.models.ApiCourse;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;

@Path("/public")
public class ApiService {

    @Inject
    private CourseRepository courseRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    JsonWebToken jwt;

    @PATCH
    @Path("/courses/")
    public List<Course> updateCourses(List<ApiCourse> courses) {

        User user = userRepository.findByUsername(jwt.getName());
        if (user == null) {
            throw new NotFoundException("User not found.");
        }

        // validate input
        if (courses == null || courses.isEmpty()) {
            throw new IllegalArgumentException("Courses must not be empty.");
        }

        // create / update courses (based on key)
        for (ApiCourse course : courses) {
            if (course.getKey() == null || course.getKey().isEmpty()) {
                throw new IllegalArgumentException("Course key must not be empty.");
            }

            // check if course already exists
            Course existingCourse = courseRepository.findByKey(course.getKey());
            if (existingCourse != null) {

                // check if user is owner of the course
                if (!existingCourse.isOwner(user)) {
                    throw new IllegalArgumentException("User is not owner of the course.");
                }

                existingCourse.updateFromApiCourse(course);
                courseRepository.update(existingCourse);
            } else {
                Course newCourse = Course.fromApiCourse(course);
                newCourse.addOwner(user.getId());
                courseRepository.persist(newCourse);
            }
        }

        return courseRepository.findAll().list();
    }

}
