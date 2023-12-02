package com.htwg.mobilelearning;

import java.util.List;

import org.jboss.resteasy.reactive.RestPath;

import com.htwg.mobilelearning.models.FeedbackChannel;
import com.htwg.mobilelearning.repositories.FeedbackChannelRepository;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/feedbackchannel")
public class FeedbackChannelResource {

    @Inject
    private FeedbackChannelRepository feedbackChannelRepository;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public FeedbackChannel getFeedbackChannel(@RestPath String id) {
        FeedbackChannel feedbackChannel = feedbackChannelRepository.findById(id);
        return feedbackChannel;
    }

    @GET
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

    @PATCH
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public FeedbackChannel updateFeedbackChannel(@RestPath String id, FeedbackChannel feedbackChannel) {
        FeedbackChannel feedbackChannelToUpdate = feedbackChannelRepository.findById(id);
        
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
}
