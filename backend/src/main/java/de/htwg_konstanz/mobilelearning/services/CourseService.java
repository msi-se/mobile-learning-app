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
import jakarta.ws.rs.core.MediaType;

/**
 * Service used to manage courses.
 */
@Path("/course")
public class CourseService {

    @Inject
    private CourseRepository courseRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    JsonWebToken jwt;

    /**
     * Returns a single course.
     * 
     * @param courseId
     * @return Course
     */
    public Course getCourse(@RestPath String courseId) {
        ObjectId courseObjectId = new ObjectId(courseId);
        Course course = courseRepository.findById(courseObjectId);
        return course;
    }

    /**
     * Returns all courses of a user.
     * 
     * @param password
     * @return List of courses
     */
    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    public List<Course> getCourses() {
        
        // update the courses linked to the user
        User user = userRepository.findByUsername(jwt.getName());
        if (user == null) {
            throw new NotFoundException("User not found.");
        }

        // decrypt the password from the jwt
        String encrPassword = jwt.getClaim("encrPassword");
        String password = user.decryptPassword(encrPassword);

        updateCourseLinkedToUser(user, password);

        // don't return the results and participants (for security and data load reasons)
        List<Course> courses = courseRepository.listAllForOwnerAndStudent(user);
        courses.forEach(course -> {
            course.clearFormsContent();
        });
        return courses;
    }

    /**
     * Let's a student join a course which is not linked to a moodle course.
     * @param courseId
     * @return Course
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{courseId}/join")
    @RolesAllowed({ UserRole.STUDENT, UserRole.PROF })
    public Course joinCourse(@RestPath String courseId) {
        Course course = courseRepository.findById(new ObjectId(courseId));
        if (course == null) {
            throw new NotFoundException("Course not found");
        }

        // if the user is already a student, just return the course
        User user = userRepository.findByUsername(jwt.getName());
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        if (course.isStudent(user.getId())) {
            return course.clearFormsContent();
        }

        // if the course is linked to a moodle course, the user cannot join the course
        if (course.getIsLinkedToMoodle()) {
            throw new NotFoundException("Course is linked to a moodle course");
        }

        // add the user to the course and the course to the user
        course.addStudent(user.getId());
        courseRepository.update(course);
        user.addCourse(course.getId());
        userRepository.update(user);
        return course.clearFormsContent();
    }

    /**
     * Updates a course.
     * @note: This method is not used in the application yet.
     * @param courseId
     * @param course
     * @return Updated course
     */	
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

    /**
     * Creates a new course.
     * 
     * @param course
     * @return Created course
     */
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

    /**
     * Deletes all course (for testing purposes only).
     * 
     * @param courseId
     */
    public void deleteAllCourses() {
        courseRepository.deleteAll();
    }

    /**
     * Updates the courses linked to the user.
     * 
     * @param user
     * @param password
     */
    public void updateCourseLinkedToUser(User user, String password) {

        // use the moodle interface to get the courses linked to the user
        MoodleInterface moodle = new MoodleInterface(user.getUsername(), password);
        List<MoodleCourse> moodleCourses = moodle.getCourses();

        // update the courses linked to the user (only the courses which are linked to a moodle course)
        List<ObjectId> previousCourses = user.getCourses();
        
        // case 1: the user was added to a new course (-> add the course to the user and the student to the course)
        for (MoodleCourse moodleCourse : moodleCourses) {

            // find all courses with the moodle course id
            List<Course> courses = courseRepository.listAllByMoodleCourseId(moodleCourse.getId());
            for (Course course : courses) {
                if (!user.hasCourse(course.getId()) || !course.isStudent(user.getId())) {
                    user.addCourse(course.getId());
                    course.addStudent(user.getId());
                    courseRepository.update(course);
                }
            }
        };

        // case 2: the user has a course that is not in the moodle courses (-> remove the course from the user and the student from the course)
        List<String> moodleCourseIds = moodleCourses.stream().map(moodleCourse -> moodleCourse.getId()).toList();
        List<Course> coursesToRemove = new ArrayList<Course>();
        for (ObjectId courseId : previousCourses) {
            Course course = courseRepository.findById(courseId); // TODO: use a single query later
            if (course == null) {
                continue;
            }
            if (!moodleCourseIds.contains(course.getMoodleCourseId()) && course.getIsLinkedToMoodle()) {
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
