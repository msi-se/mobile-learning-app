package de.htwg_konstanz.mobilelearning.repositories;



import org.bson.types.ObjectId;

import de.htwg_konstanz.mobilelearning.models.Course;
import de.htwg_konstanz.mobilelearning.models.Form;
import de.htwg_konstanz.mobilelearning.models.feedback.FeedbackForm;
import de.htwg_konstanz.mobilelearning.models.quiz.QuizForm;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CourseRepository implements PanacheMongoRepository<Course> {
    

    public Course findByFeedbackFormConnectCode(Integer connectCode) {
        return find("feedbackForms.connectCode", connectCode).firstResult();
    }

    public Course findByQuizFormConnectCode(Integer connectCode) {
        return find("quizForms.connectCode", connectCode).firstResult();
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

}
