package de.htwg_konstanz.mobilelearning.services.quiz;

import java.util.List;
import java.util.ArrayList;

import org.bson.types.ObjectId;
import org.jboss.resteasy.reactive.RestPath;

import de.htwg_konstanz.mobilelearning.enums.FormStatus;
import de.htwg_konstanz.mobilelearning.models.Course;
import de.htwg_konstanz.mobilelearning.models.QuestionWrapper;
import de.htwg_konstanz.mobilelearning.models.auth.User;
import de.htwg_konstanz.mobilelearning.models.auth.UserRole;
import de.htwg_konstanz.mobilelearning.models.feedback.FeedbackForm;
import de.htwg_konstanz.mobilelearning.models.quiz.QuizForm;
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
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

@Path("/course/{courseId}/quiz/form")
public class QuizFormService {
    
    @Inject CourseRepository courseRepository;

    @Inject UserRepository userRepository;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    public List<QuizForm> getQuizForms(@RestPath String courseId) {
        ObjectId courseObjectId = new ObjectId(courseId);
        Course course = courseRepository.findById(courseObjectId);
        return course.getQuizForms();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{formId}")
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    public QuizForm getQuizForm(@RestPath String courseId, @RestPath String formId) {

        ObjectId courseObjectId = new ObjectId(courseId);
        ObjectId formObjectId = new ObjectId(formId);

        // fill the questionContent with the linked question
        Course course = courseRepository.findById(courseObjectId);
        QuizForm quizForm = course.getQuizFormById(formObjectId);
        QuizForm quizFormWithQuestionContents = quizForm.copyWithoutResultsButWithQuestionContents(course);

        return quizFormWithQuestionContents;
    }

    /*
     * Endpoint to participate in a quiz
     * The user has to be registered with the user id and he has to provide an alias (String)
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.STUDENT, UserRole.PROF })
    @Path("/{formId}/participate")
    public String participate(@Context SecurityContext ctx, String alias, @RestPath String courseId, @RestPath String formId) {

        // get the user
        String userName = ctx.getUserPrincipal().getName();
        User user = userRepository.findByUsername(userName);
        if (user == null) { throw new NotFoundException("User not found"); }
        ObjectId userId = user.getId();

        // get the course and the quizForm
        ObjectId courseObjectId = new ObjectId(courseId);
        ObjectId formObjectId = new ObjectId(formId);
        Course course = courseRepository.findById(courseObjectId);
        if (course == null) { throw new NotFoundException("Course not found"); }
        QuizForm quizForm = course.getQuizFormById(formObjectId);
        if (quizForm == null) { throw new NotFoundException("QuizForm not found"); }

        quizForm.addParticipant(userId, alias);
        courseRepository.update(course);

        return "OK";
    }

}
