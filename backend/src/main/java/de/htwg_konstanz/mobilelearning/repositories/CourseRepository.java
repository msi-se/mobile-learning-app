package de.htwg_konstanz.mobilelearning.repositories;

import java.io.Serializable;
import java.util.List;
import org.bson.types.ObjectId;

import de.htwg_konstanz.mobilelearning.models.Course;
import de.htwg_konstanz.mobilelearning.models.Form;
import de.htwg_konstanz.mobilelearning.models.auth.User;
import de.htwg_konstanz.mobilelearning.models.feedback.FeedbackForm;
import de.htwg_konstanz.mobilelearning.models.quiz.QuizForm;
import de.htwg_konstanz.mobilelearning.repositories.projectionclasses.CourseWithoutForms;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import io.quarkus.mongodb.panache.common.ProjectionFor;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CourseRepository implements PanacheMongoRepository<Course> {

    public Course findByName(String name) {
        return find("name", name).firstResult();
    }

    public Course findByFeedbackFormConnectCode(Integer connectCode) {
        return find("feedbackForms.connectCode", connectCode).firstResult();
    }

    public Course findByQuizFormConnectCode(Integer connectCode) {
        return find("quizForms.connectCode", connectCode).firstResult();
    }

    public Course findByMoodleCourseId(String moodleCourseId) {
        return find("moodleCourseId", moodleCourseId).firstResult();
    }

    public Course findByFormConnectCode(Integer connectCode) {
        // TODO: make this more efficient
        Course course = findByFeedbackFormConnectCode(connectCode);
        if (course == null) {
            course = findByQuizFormConnectCode(connectCode);
        }
        return course;
    }

    public Form findFormByIds(ObjectId courseId, ObjectId formId) {
        Course course = findById(courseId);
        if (course == null) {
            return null;
        }
        return course.getFormById(formId);
    }

    public FeedbackForm findFeedbackFormByIds(ObjectId courseId, ObjectId formId) {
        Course course = findById(courseId);
        if (course == null) {
            return null;
        }
        return course.getFeedbackFormById(formId);
    }

    public FeedbackForm findFeedbackFormByConnectCode(Integer connectionCode) {
        Course course = findByFormConnectCode(connectionCode);
        if (course == null) {
            return null;
        }

        return course.getFeedbackFormByConnectCode(connectionCode);
    }

    public QuizForm findQuizFormByConnectCode(Integer connectionCode) {
        Course course = findByFormConnectCode(connectionCode);
        if (course == null) {
            return null;
        }

        return course.getQuizFormByConnectCode(connectionCode);
    }

    public Course findByKey(String key) {
        return find("key", key).firstResult();
    }

    public List<Course> listAllForStudent(User user) {
        return find("students", user.getId()).list();
    }

    public List<Course> listAllForStudent(ObjectId userId) {
        return find("students", userId).list();
    }

    public List<Course> listAllForOwner(User user) {
        return find("owners", user.getId()).list();
    }

    public List<Course> listAllForOwner(ObjectId userId) {
        return find("owners", userId).list();
    }

    public List<Course> listAllForOwnerAndStudent(User user) {
        List<Course> coursesForOwner = listAllForOwner(user);
        List<Course> coursesForStudent = listAllForStudent(user);

        for (Course course : coursesForStudent) {
            if (!coursesForOwner.contains(course)) {
                coursesForOwner.add(course);
            }
        }

        return coursesForOwner;
    }

    public List<Course> listAllForOwnerAndStudent(ObjectId userId) {
        List<Course> coursesForOwner = listAllForOwner(userId);
        List<Course> coursesForStudent = listAllForStudent(userId);
        for (Course course : coursesForStudent) {
            if (!coursesForOwner.contains(course)) {
                coursesForOwner.add(course);
            }
        }
        return coursesForOwner;
    }

    public Course findByIdWithoutForms(ObjectId id) {

        CourseWithoutForms course = find("_id", id).project(CourseWithoutForms.class).firstResult();
        if (course == null) {
            return null;
        }
        return new Course(course.id(), course.name(), course.description(), course.owners(), course.students(), course.key(),
                course.moodleCourseId());
    }

    public Course findByIdWithSpecificFeedbackForm(ObjectId id, ObjectId formId) {
        
        // fist query the course without the forms and then project the specific form
        Course course = findByIdWithoutForms(id);
        FeedbackForm form = find("feedbackForms._id", formId).project(FeedbackForm.class).firstResult();
        if (form != null && form instanceof FeedbackForm) {
            course.addFeedbackForm(form);
        }

        return course;
    }



}
