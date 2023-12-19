package de.htwg_konstanz.mobilelearning.services.feedback;

import java.util.List;

import org.bson.types.ObjectId;
import org.jboss.resteasy.reactive.RestPath;

import de.htwg_konstanz.mobilelearning.enums.FormStatus;
import de.htwg_konstanz.mobilelearning.models.Course;
import de.htwg_konstanz.mobilelearning.models.QuestionWrapper;
import de.htwg_konstanz.mobilelearning.models.auth.UserRole;
import de.htwg_konstanz.mobilelearning.models.feedback.FeedbackForm;
import de.htwg_konstanz.mobilelearning.repositories.CourseRepository;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/course/{courseId}/feedback/form")
public class FeedbackFormService {

    @Inject
    private CourseRepository feedbackChannelRepository;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    public List<FeedbackForm> getFeedbackForms(@RestPath String courseId) {
        ObjectId courseObjectId = new ObjectId(courseId);
        Course feedbackChannel = feedbackChannelRepository.findById(courseObjectId);
        return feedbackChannel.getFeedbackForms();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{formId}")
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    public FeedbackForm getFeedbackForm(@RestPath String courseId, @RestPath String formId) {

        ObjectId courseObjectId = new ObjectId(courseId);
        ObjectId formObjectId = new ObjectId(formId);

        // fill the questionContent with the linked question
        Course course = feedbackChannelRepository.findById(courseObjectId);
        FeedbackForm feedbackForm = course.getFeedbackFormById(formObjectId);
        feedbackForm.fillQuestionContents(course);

        return feedbackForm;
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{formId}")
    @RolesAllowed({ UserRole.PROF })
    public FeedbackForm updateFeedbackForm(@RestPath String courseId, @RestPath String formId, FeedbackForm feedbackForm) {
        ObjectId courseObjectId = new ObjectId(courseId);
        ObjectId formObjectId = new ObjectId(formId);
        Course feedbackChannel = feedbackChannelRepository.findById(courseObjectId);
        FeedbackForm feedbackFormToUpdate = feedbackChannel.getFeedbackFormById(formObjectId);
        
        if (feedbackFormToUpdate == null) {
            throw new NotFoundException("Feedbackcourse not found");
        }

        if (feedbackForm.description != null) {
            feedbackFormToUpdate.description = feedbackForm.description;
        }
        else if (feedbackForm.name != null) {
            feedbackFormToUpdate.name = feedbackForm.name;
        }
        else if (feedbackForm.questions != null) {
            feedbackFormToUpdate.questions = feedbackForm.questions;
        }
        else if (feedbackForm.connectCode != null) {
            feedbackFormToUpdate.connectCode = feedbackForm.connectCode;
        }
        else if (feedbackForm.status != null) {
            feedbackFormToUpdate.status = feedbackForm.status;
        }

        feedbackChannelRepository.update(feedbackChannel);
        return feedbackFormToUpdate;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("")
    @RolesAllowed({ UserRole.PROF })
    public FeedbackForm createFeedbackForm(@RestPath String courseId, FeedbackForm feedbackForm) {
        
        // TODO: add validation
        ObjectId courseObjectId = new ObjectId(courseId);
        Course feedbackChannel = feedbackChannelRepository.findById(courseObjectId);
        
        FeedbackForm newFeedbackForm = new FeedbackForm(
            feedbackChannel.getId(),
            feedbackForm.getName(),
            feedbackForm.getDescription(),
            feedbackForm.getQuestions(),
            FormStatus.NOT_STARTED
        );

        feedbackChannel.addFeedbackForm(newFeedbackForm);
        feedbackChannelRepository.update(feedbackChannel);

        return feedbackForm;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{formId}/clearresults")
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    public FeedbackForm clearFeedbackFormResults(@RestPath String courseId, @RestPath String formId) {
        ObjectId courseObjectId = new ObjectId(courseId);
        ObjectId formObjectId = new ObjectId(formId);
        Course feedbackChannel = feedbackChannelRepository.findById(courseObjectId);
        FeedbackForm feedbackForm = feedbackChannel.getFeedbackFormById(formObjectId);

        if (feedbackForm == null) {
            throw new NotFoundException("Feedbackcourse not found");
        }

        feedbackForm.clearResults();
        feedbackChannelRepository.update(feedbackChannel);
        return feedbackForm;
    }

}
