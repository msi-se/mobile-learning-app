package de.htwg_konstanz.mobilelearning.services;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.RestPath;

import de.htwg_konstanz.mobilelearning.helper.moodle.MoodleCourse;
import de.htwg_konstanz.mobilelearning.helper.moodle.MoodleInterface;
import de.htwg_konstanz.mobilelearning.models.Course;
import de.htwg_konstanz.mobilelearning.models.auth.User;
import de.htwg_konstanz.mobilelearning.models.auth.UserRole;
import de.htwg_konstanz.mobilelearning.repositories.CourseRepository;
import de.htwg_konstanz.mobilelearning.repositories.UserRepository;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/course")
public class CourseService {

    @Inject
    private CourseRepository courseRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    JsonWebToken jwt;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{courseId}")
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    public Course getCourse(@RestPath String courseId) {
        ObjectId courseObjectId = new ObjectId(courseId);
        Course course = courseRepository.findById(courseObjectId);
        return course;
    }

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    public List<Course> getCourses(@QueryParam("password") String password) {
        
        // update the courses linked to the user
        User user = userRepository.findByUsername(jwt.getName());
        if (user == null) {
            throw new NotFoundException("User not found.");
        }
        updateCourseLinkedToUser(user, password);

        List<Course> courses = courseRepository.listAllForStudent(user);
        courses.forEach(course -> {
            course.feedbackForms.forEach(form -> {
                form.questions = List.of();
                form.clearResults();
            });
            course.quizForms.forEach(form -> {
                form.questions = List.of();
                form.clearResults();
                form.clearParticipants();
            });
        });
        return courses;
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{courseId}")
    @RolesAllowed({ UserRole.PROF })
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
    @RolesAllowed({ UserRole.PROF })
    public Course createCourse(Course course) {
        // TODO: add validation
        course.id = new ObjectId();
        courseRepository.persist(course);
        return course;
    }

    public void deleteAllCourses() {
        courseRepository.deleteAll();
    }

    public void updateCourseLinkedToUser(User user, String password) {

        // TEMP: mock the special users (Prof, Student, Admin)
        if (user.getUsername().equals("Prof") || user.getUsername().equals("Student") || user.getUsername().equals("Admin")) {
            return;
        }

        // use the moodle interface to get the courses linked to the user
        MoodleInterface moodle = new MoodleInterface(user.getUsername(), password);
        List<MoodleCourse> moodleCourses = moodle.getCourses();

        // update the courses linked to the user
        List<ObjectId> previousCourses = user.getCourses();
        
        // case 1: the user was added to a new course (-> add the course to the user and the student to the course)
        for (MoodleCourse moodleCourse : moodleCourses) {
            Course course = courseRepository.findByMoodleCourseId(moodleCourse.getId());
            if (course == null) {
                continue;
            }
            if (!user.hasCourse(course.getId()) || !course.isStudent(user.getId())) {
                user.addCourse(course.getId());
                course.addStudent(user.getId());
                courseRepository.update(course);
            }
        };

        // case 2: the user has a course that is not in the moodle courses (-> remove the course from the user and the student from the course)
        List<String> moodleCourseIds = moodleCourses.stream().map(moodleCourse -> moodleCourse.getId()).toList();
        List<Course> coursesToRemove = new ArrayList<Course>();
        for (ObjectId courseId : previousCourses) {
            Course course = courseRepository.findById(courseId);
            if (course == null) {
                continue;
            }
            if (!moodleCourseIds.contains(course.getMoodleCourseId())) {
                coursesToRemove.add(course);
            }
        };
        for (Course course : coursesToRemove) {
            course.removeStudent(user.getId());
            courseRepository.update(course);
            user.removeCourse(course.getId());
        }

        // update the user
        userRepository.update(user);
    }

    // for testing
    public CourseRepository getCourseRepository() {
        return courseRepository;
    }
}
