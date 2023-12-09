package de.htwg_konstanz.mobilelearning.services.feedback;

import org.jboss.resteasy.reactive.RestPath;
import org.json.JSONObject;

import de.htwg_konstanz.mobilelearning.models.feedback.FeedbackChannel;
import de.htwg_konstanz.mobilelearning.models.feedback.FeedbackForm;
import de.htwg_konstanz.mobilelearning.repositories.FeedbackChannelRepository;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/feedback/connectto")
public class ConnectCodeService {
    
    @Inject
    private FeedbackChannelRepository feedbackChannelRepository;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{connectCode}")
    public String getFeedbackChannelByConnectCode(@RestPath Integer connectCode) {
        System.out.println("Connect code: " + connectCode);
        FeedbackChannel feedbackChannel = feedbackChannelRepository.findByFormConnectCode(connectCode);
        if (feedbackChannel == null) {
            throw new NotFoundException("Feedback channel with connect code " + connectCode + " not found.");
        }

        FeedbackForm feedbackForm = null;
        for (FeedbackForm form : feedbackChannel.getFeedbackForms()) {
            if (form.getConnectCode().equals(connectCode)) {
                feedbackForm = form;
            }
        };

        if (feedbackForm == null) {
            throw new NotFoundException("Feedback form with connect code " + connectCode + " not found.");
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("channelId", feedbackChannel.getId().toHexString());
        jsonObject.put("formId", feedbackForm.getId().toHexString());
        return jsonObject.toString();
    }
}