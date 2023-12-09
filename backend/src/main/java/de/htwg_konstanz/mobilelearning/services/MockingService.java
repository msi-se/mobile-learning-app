package de.htwg_konstanz.mobilelearning.services;

import java.util.List;

import org.bson.types.ObjectId;

import de.htwg_konstanz.mobilelearning.enums.FeedbackChannelStatus;
import de.htwg_konstanz.mobilelearning.enums.FeedbackElementType;
import de.htwg_konstanz.mobilelearning.models.auth.User;
import de.htwg_konstanz.mobilelearning.models.feedback.FeedbackChannel;
import de.htwg_konstanz.mobilelearning.models.feedback.FeedbackElement;
import de.htwg_konstanz.mobilelearning.models.feedback.FeedbackForm;
import de.htwg_konstanz.mobilelearning.repositories.FeedbackChannelRepository;
import de.htwg_konstanz.mobilelearning.repositories.UserRepository;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/mock")
public class MockingService {

    @Inject
    private FeedbackChannelRepository feedbackChannelRepository;

    @Inject
    private UserRepository userRepository;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/mock")
    public List<Object> addData() {
        feedbackChannelRepository.deleteAll();
        userRepository.deleteAll();

        // generate some FeedbackElements
        FeedbackElement feedbackElement1 = new FeedbackElement("Frage 1", "Wie fandest du die letzte Vorlesung auf einer Skala von 1 bis 5?", FeedbackElementType.STARS, null);
        FeedbackElement feedbackElement2 = new FeedbackElement("Frage 2", "Würdest du die Vorlesung weiterempfehlen?", FeedbackElementType.YES_NO, null);

        // generate a FeedbackChannel
        FeedbackChannel feedbackChannel1 = new FeedbackChannel("Diskrete Mathematik", "Feedback-Kanal für DiMa", null);

        // generate a FeedbackForm
        ObjectId feedbackChannel1Id = feedbackChannel1.getId();
        FeedbackForm feedbackForm1 = new FeedbackForm(feedbackChannel1Id, "Letzte Vorlesung", "Dies ist das Feedback zur letzten Vorlesung.", List.of(feedbackElement1, feedbackElement2), FeedbackChannelStatus.NOT_STARTED);
        feedbackChannel1.addFeedbackForm(feedbackForm1);

        // save the FeedbackChannel
        feedbackChannelRepository.persist(feedbackChannel1);

        // create dummy user
        User user1 = new User("Johannes", "password1");
        User user2 = new User("Fabi", "password2");

        // save the user
        userRepository.persist(user1);
        userRepository.persist(user2);

        // return all FeedbackChannels and Users
        return List.of(feedbackChannelRepository.listAll(), userRepository.listAll());
    }
}
