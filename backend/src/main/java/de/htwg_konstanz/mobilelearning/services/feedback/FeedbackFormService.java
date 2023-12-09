package de.htwg_konstanz.mobilelearning.services.feedback;

import java.util.List;

import org.bson.types.ObjectId;
import org.jboss.resteasy.reactive.RestPath;

import de.htwg_konstanz.mobilelearning.models.feedback.FeedbackChannel;
import de.htwg_konstanz.mobilelearning.models.feedback.FeedbackForm;
import de.htwg_konstanz.mobilelearning.repositories.FeedbackChannelRepository;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/feedback/channel/{channelId}/form")
public class FeedbackFormService {

    @Inject
    private FeedbackChannelRepository feedbackChannelRepository;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<FeedbackForm> getFeedbackChannels(@RestPath String channelId) {
        ObjectId channelObjectId = new ObjectId(channelId);
        FeedbackChannel feedbackChannel = feedbackChannelRepository.findById(channelObjectId);
        return feedbackChannel.getFeedbackForms();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{formId}")
    public FeedbackForm getFeedbackChannel(@RestPath String channelId, @RestPath String formId) {

        ObjectId channelObjectId = new ObjectId(channelId);
        ObjectId formObjectId = new ObjectId(formId);

        return feedbackChannelRepository.findFeedbackFormById(channelObjectId, formObjectId);
    }

}
