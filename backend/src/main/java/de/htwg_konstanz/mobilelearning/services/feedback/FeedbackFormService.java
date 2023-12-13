package de.htwg_konstanz.mobilelearning.services.feedback;

import java.util.List;

import org.bson.types.ObjectId;
import org.jboss.resteasy.reactive.RestPath;

import de.htwg_konstanz.mobilelearning.enums.FormStatus;
import de.htwg_konstanz.mobilelearning.models.Course;
import de.htwg_konstanz.mobilelearning.models.feedback.FeedbackForm;
import de.htwg_konstanz.mobilelearning.repositories.CourseRepository;

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
    public List<FeedbackForm> getFeedbackForms(@RestPath String courseId) {
        ObjectId channelObjectId = new ObjectId(courseId);
        Course feedbackChannel = feedbackChannelRepository.findById(channelObjectId);
        return feedbackChannel.getFeedbackForms();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{formId}")
    public FeedbackForm getFeedbackForm(@RestPath String courseId, @RestPath String formId) {

        ObjectId channelObjectId = new ObjectId(courseId);
        ObjectId formObjectId = new ObjectId(formId);

        return feedbackChannelRepository.findFeedbackFormByIds(channelObjectId, formObjectId);
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{formId}")
    public FeedbackForm updateFeedbackForm(@RestPath String courseId, @RestPath String formId, FeedbackForm feedbackForm) {
        ObjectId channelObjectId = new ObjectId(courseId);
        ObjectId formObjectId = new ObjectId(formId);
        Course feedbackChannel = feedbackChannelRepository.findById(channelObjectId);
        FeedbackForm feedbackFormToUpdate = feedbackChannel.getFeedbackFormById(formObjectId);
        
        if (feedbackFormToUpdate == null) {
            throw new NotFoundException("Feedbackchannel not found");
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
    public FeedbackForm createFeedbackForm(@RestPath String courseId, FeedbackForm feedbackForm) {
        
        // TODO: add validation
        ObjectId channelObjectId = new ObjectId(courseId);
        Course feedbackChannel = feedbackChannelRepository.findById(channelObjectId);
        
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
    public FeedbackForm clearFeedbackFormResults(@RestPath String courseId, @RestPath String formId) {
        ObjectId channelObjectId = new ObjectId(courseId);
        ObjectId formObjectId = new ObjectId(formId);
        Course feedbackChannel = feedbackChannelRepository.findById(channelObjectId);
        FeedbackForm feedbackForm = feedbackChannel.getFeedbackFormById(formObjectId);

        if (feedbackForm == null) {
            throw new NotFoundException("Feedbackchannel not found");
        }

        feedbackForm.clearResults();
        feedbackChannelRepository.update(feedbackChannel);
        return feedbackForm;
    }

}
