package com.htwg.mobilelearning.services.feedback;

import java.util.List;

import org.bson.types.ObjectId;
import org.jboss.resteasy.reactive.RestPath;

import com.htwg.mobilelearning.models.feedback.FeedbackChannel;
import com.htwg.mobilelearning.models.feedback.FeedbackForm;
import com.htwg.mobilelearning.repositories.FeedbackChannelRepository;

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
    public List<FeedbackForm> getFeedbackChannel(@RestPath String channelId) {
        ObjectId channelObjectId = new ObjectId(channelId);
        FeedbackChannel feedbackChannel = feedbackChannelRepository.findById(channelObjectId);
        return feedbackChannel.getFeedbackForms();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{formId}")
    public FeedbackForm getFeedbackChannel(@RestPath String channelId, @RestPath String formId) {

        // formId not considered yet
        ObjectId channelObjectId = new ObjectId(channelId);
        ObjectId formObjectId = new ObjectId(formId);
        FeedbackChannel feedbackChannel = feedbackChannelRepository.findById(channelObjectId);
        List<FeedbackForm> feedbackForms = feedbackChannel.getFeedbackForms();

        FeedbackForm feedbackForm = null;
        for (FeedbackForm form : feedbackForms) {
            if (form.getId().equals(formObjectId)) {
                feedbackForm = form;
                break;
            }
        }
        return feedbackForm;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/demoendpoint/insertmockdata")
    public List<FeedbackForm> insertMockData() {
        FeedbackChannel feedbackChannel = new FeedbackChannel("FeedbackChannel1", "Description1", null);
        FeedbackForm feedbackForm1 = new FeedbackForm("FeedbackForm1", "Description1", null, false, false);
        FeedbackForm feedbackForm2 = new FeedbackForm("FeedbackForm2", "Description2", null, false, false);
        feedbackChannel.getFeedbackForms().add(feedbackForm1);
        feedbackChannel.getFeedbackForms().add(feedbackForm2);
        feedbackChannelRepository.persist(feedbackChannel);

        feedbackChannel = feedbackChannelRepository.findById(feedbackChannel.getId());
        return feedbackChannel.getFeedbackForms();
    }

}
