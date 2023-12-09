package de.htwg_konstanz.mobilelearning.services.feedback;

import java.util.List;

import org.bson.types.ObjectId;
import org.jboss.resteasy.reactive.RestPath;

import de.htwg_konstanz.mobilelearning.models.feedback.FeedbackChannel;
import de.htwg_konstanz.mobilelearning.repositories.FeedbackChannelRepository;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/feedback/channel")
public class FeedbackChannelService {

    @Inject
    private FeedbackChannelRepository feedbackChannelRepository;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{channelId}")
    public FeedbackChannel getFeedbackChannel(@RestPath String channelId) {
        ObjectId channelObjectId = new ObjectId(channelId);
        FeedbackChannel feedbackChannel = feedbackChannelRepository.findById(channelObjectId);
        return feedbackChannel;
    }

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public List<FeedbackChannel> getFeedbackChannels() {
        List<FeedbackChannel> feedbackChannels = feedbackChannelRepository.listAll();
        return feedbackChannels;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/demoendpoint/deleteall")
    public String deleteAllFeedbackChannels() {
        feedbackChannelRepository.deleteAll();
        return "All FeedbackChannels deleted";
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/demoendpoint/insertmockdata")
    public List<FeedbackChannel> insertMockData() {
        feedbackChannelRepository.deleteAll();
        FeedbackChannel feedbackChannel = new FeedbackChannel("FeedbackChannel1", "Description1", null);
        feedbackChannelRepository.persist(feedbackChannel);
        feedbackChannel = new FeedbackChannel("FeedbackChannel2", "Description2", null);
        feedbackChannelRepository.persist(feedbackChannel);
        feedbackChannel = new FeedbackChannel("FeedbackChannel3", "Description3", null);
        feedbackChannelRepository.persist(feedbackChannel);
        return feedbackChannelRepository.listAll();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{channelId}")
    public FeedbackChannel updateFeedbackChannel(@RestPath String channelId, FeedbackChannel feedbackChannel) {
        ObjectId channelObjectId = new ObjectId(channelId);
        FeedbackChannel feedbackChannelToUpdate = feedbackChannelRepository.findById(channelObjectId);
        
        if (feedbackChannelToUpdate == null) {
            throw new NotFoundException("Feedbackchannel not found");
        }

        if (feedbackChannel.description != null) {
            feedbackChannelToUpdate.description = feedbackChannel.description;
        }
        else if (feedbackChannel.name != null) {
            feedbackChannelToUpdate.name = feedbackChannel.name;
        }
        else if (feedbackChannel.feedbackForms != null) {
            feedbackChannelToUpdate.feedbackForms = feedbackChannel.feedbackForms;
        }
        feedbackChannelRepository.update(feedbackChannelToUpdate);
        return feedbackChannelToUpdate;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("")
    public FeedbackChannel createFeedbackChannel(FeedbackChannel feedbackChannel) {
        // TODO: add validation
        feedbackChannel.id = new ObjectId();
        feedbackChannelRepository.persist(feedbackChannel);
        return feedbackChannel;
    }
}
