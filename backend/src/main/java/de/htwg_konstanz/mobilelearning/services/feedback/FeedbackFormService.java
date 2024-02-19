package de.htwg_konstanz.mobilelearning.services.feedback;

import java.util.List;

import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.RestPath;

import de.htwg_konstanz.mobilelearning.enums.FormStatus;
import de.htwg_konstanz.mobilelearning.models.Course;
import de.htwg_konstanz.mobilelearning.models.auth.UserRole;
import de.htwg_konstanz.mobilelearning.models.feedback.FeedbackForm;
import de.htwg_konstanz.mobilelearning.repositories.CourseRepository;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/course/{courseId}/feedback/form")
public class FeedbackFormService {

    @Inject
    private CourseRepository courseRepository;

    @Inject
    private JsonWebToken jwt;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    public List<FeedbackForm> getFeedbackForms(@RestPath String courseId) {
        ObjectId courseObjectId = new ObjectId(courseId);
        Course course = courseRepository.findById(courseObjectId);
        return course.getFeedbackForms();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{formId}")
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    public FeedbackForm getFeedbackForm(@RestPath String courseId, @RestPath String formId, @QueryParam("results") @DefaultValue("false") Boolean results) {

        ObjectId courseObjectId = new ObjectId(courseId);
        ObjectId formObjectId = new ObjectId(formId);

        // fill the questionContent with the linked question
        Course course = courseRepository.findById(courseObjectId);
        FeedbackForm feedbackForm = course.getFeedbackFormById(formObjectId);

        if (results) {
            FeedbackForm feedbackFormWithQuestionContents = feedbackForm.copyWithQuestionContents(course);
            return feedbackFormWithQuestionContents;
        }

        FeedbackForm feedbackFormWithQuestionContents = feedbackForm.copyWithoutResultsButWithQuestionContents(course);
        return feedbackFormWithQuestionContents;
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{formId}")
    @RolesAllowed({ UserRole.PROF })
    public FeedbackForm updateFeedbackForm(@RestPath String courseId, @RestPath String formId, FeedbackForm feedbackForm) {
        ObjectId courseObjectId = new ObjectId(courseId);
        ObjectId formObjectId = new ObjectId(formId);
        Course course = courseRepository.findById(courseObjectId);
        FeedbackForm feedbackFormToUpdate = course.getFeedbackFormById(formObjectId);
        
        if (feedbackFormToUpdate == null) {
            throw new NotFoundException("Feedbackcourse not found");
        }
        if(!course.isOwner(jwt.getSubject())){
            return null;
        }
        if (feedbackForm.description != null) {
            feedbackFormToUpdate.description = feedbackForm.description;
        }
        if (feedbackForm.name != null) {
            feedbackFormToUpdate.name = feedbackForm.name;
        }
        if (feedbackForm.questions != null) {
            feedbackFormToUpdate.questions = feedbackForm.questions;
        }
        if (feedbackForm.connectCode != null) {
            feedbackFormToUpdate.connectCode = feedbackForm.connectCode;
        }
        if (feedbackForm.status != null) {
            feedbackFormToUpdate.status = feedbackForm.status;
        }

        courseRepository.update(course);
        return feedbackFormToUpdate;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("")
    @RolesAllowed({ UserRole.PROF })
    public FeedbackForm createFeedbackForm(@RestPath String courseId, FeedbackForm feedbackForm) {
        
        // TODO: add validation
        ObjectId courseObjectId = new ObjectId(courseId);
        Course course = courseRepository.findById(courseObjectId);
        
        FeedbackForm newFeedbackForm = new FeedbackForm(
            course.getId(),
            feedbackForm.getName(),
            feedbackForm.getDescription(),
            feedbackForm.getQuestions(),
            FormStatus.NOT_STARTED
        );

        course.addFeedbackForm(newFeedbackForm);
        courseRepository.update(course);

        return feedbackForm;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{formId}/clearresults")
    @RolesAllowed({ UserRole.PROF })
    public FeedbackForm clearFeedbackFormResults(@RestPath String courseId, @RestPath String formId) {
        ObjectId courseObjectId = new ObjectId(courseId);
        ObjectId formObjectId = new ObjectId(formId);
        Course course = courseRepository.findById(courseObjectId);
        FeedbackForm feedbackForm = course.getFeedbackFormById(formObjectId);

        if (feedbackForm == null) {
            throw new NotFoundException("Feedbackcourse not found");
        }
        if (!course.isOwner(jwt.getSubject())) {
            System.out.println("User is not owner of course");
            return null;
        }
        feedbackForm.clearResults();
        courseRepository.update(course);
        return feedbackForm;
    }

}
