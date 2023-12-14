package de.htwg_konstanz.mobilelearning.services.feedback;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.jboss.resteasy.reactive.RestPath;

import de.htwg_konstanz.mobilelearning.models.Course;
import de.htwg_konstanz.mobilelearning.models.QuestionWrapper;
import de.htwg_konstanz.mobilelearning.repositories.CourseRepository;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/course")
public class CourseService {

    @Inject
    private CourseRepository courseRepository;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{courseId}")
    public Course getCourse(@RestPath String courseId) {
        ObjectId courseObjectId = new ObjectId(courseId);
        Course course = courseRepository.findById(courseObjectId);
        return course;
    }

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Course> getCourses() {
        List<Course> courses = courseRepository.listAll();
        courses.forEach(course -> {
            course.feedbackForms.forEach(form -> {
                form.questions = null;
            });
            course.quizForms.forEach(form -> {
                form.questions = null;
            });
        });
        return courses;
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{courseId}")
    public Course updateCourse(@RestPath String courseId, Course course) {
        ObjectId courseObjectId = new ObjectId(courseId);
        Course courseToUpdate = courseRepository.findById(courseObjectId);
        
        if (courseToUpdate == null) {
            throw new NotFoundException("Course not found");
        }

        if (course.description != null) {
            courseToUpdate.description = course.description;
        }
        else if (course.name != null) {
            courseToUpdate.name = course.name;
        }
        else if (course.feedbackForms != null) {
            courseToUpdate.feedbackForms = course.feedbackForms;
        }
        else if (course.quizForms != null) {
            courseToUpdate.quizForms = course.quizForms;
        }
        else if (course.feedbackQuestions != null) {
            courseToUpdate.feedbackQuestions = course.feedbackQuestions;
        }
        else if (course.quizQuestions != null) {
            courseToUpdate.quizQuestions = course.quizQuestions;
        }
        else if (course.owners != null) {
            courseToUpdate.owners = course.owners;
        }
        courseRepository.update(courseToUpdate);
        return courseToUpdate;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("")
    public Course createCourse(Course course) {
        // TODO: add validation
        course.id = new ObjectId();
        courseRepository.persist(course);
        return course;
    }
}
