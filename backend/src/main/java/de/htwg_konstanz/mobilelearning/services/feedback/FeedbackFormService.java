package de.htwg_konstanz.mobilelearning.services.feedback;

import java.util.List;

import org.bson.types.ObjectId;
import org.jboss.resteasy.reactive.RestPath;

import de.htwg_konstanz.mobilelearning.enums.FeedbackChannelStatus;
import de.htwg_konstanz.mobilelearning.models.feedback.FeedbackChannel;
import de.htwg_konstanz.mobilelearning.models.feedback.FeedbackForm;
import de.htwg_konstanz.mobilelearning.repositories.FeedbackChannelRepository;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/feedback/channel/{channelId}/form")
public class FeedbackFormService {

    @Inject
    private FeedbackChannelRepository feedbackChannelRepository;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<FeedbackForm> getFeedbackForms(@RestPath String channelId) {
        ObjectId channelObjectId = new ObjectId(channelId);
        FeedbackChannel feedbackChannel = feedbackChannelRepository.findById(channelObjectId);
        return feedbackChannel.getFeedbackForms();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{formId}")
    public FeedbackForm getFeedbackForm(@RestPath String channelId, @RestPath String formId) {

        ObjectId channelObjectId = new ObjectId(channelId);
        ObjectId formObjectId = new ObjectId(formId);

        return feedbackChannelRepository.findFeedbackFormById(channelObjectId, formObjectId);
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{formId}")
    public FeedbackForm updateFeedbackForm(@RestPath String channelId, @RestPath String formId, FeedbackForm feedbackForm) {
        ObjectId channelObjectId = new ObjectId(channelId);
        ObjectId formObjectId = new ObjectId(formId);
        FeedbackChannel feedbackChannel = feedbackChannelRepository.findById(channelObjectId);
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
        else if (feedbackForm.elements != null) {
            feedbackFormToUpdate.elements = feedbackForm.elements;
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
    public FeedbackForm createFeedbackForm(@RestPath String channelId, FeedbackForm feedbackForm) {
        // TODO: add validation
        ObjectId channelObjectId = new ObjectId(channelId);
        FeedbackChannel feedbackChannel = feedbackChannelRepository.findById(channelObjectId);
        
        FeedbackForm newFeedbackForm = new FeedbackForm(
            feedbackChannel.getId(),
            feedbackForm.name,
            feedbackForm.description,
            feedbackForm.elements,
            feedbackForm.status != null ? feedbackForm.status : FeedbackChannelStatus.NOT_STARTED
        );

        feedbackChannel.addFeedbackForm(newFeedbackForm);
        feedbackChannelRepository.update(feedbackChannel);

        return feedbackForm;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{formId}/clearresults")
    public FeedbackForm clearFeedbackFormResults(@RestPath String channelId, @RestPath String formId) {
        ObjectId channelObjectId = new ObjectId(channelId);
        ObjectId formObjectId = new ObjectId(formId);
        FeedbackChannel feedbackChannel = feedbackChannelRepository.findById(channelObjectId);
        FeedbackForm feedbackForm = feedbackChannel.getFeedbackFormById(formObjectId);

        if (feedbackForm == null) {
            throw new NotFoundException("Feedbackchannel not found");
        }

        feedbackForm.clearResults();
        feedbackChannelRepository.update(feedbackChannel);
        return feedbackForm;
    }

}
