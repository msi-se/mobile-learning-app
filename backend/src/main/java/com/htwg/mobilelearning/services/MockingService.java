package com.htwg.mobilelearning.services;

import java.util.List;

import org.bson.types.ObjectId;
import org.jboss.resteasy.reactive.RestPath;

import com.htwg.mobilelearning.enums.FeedbackChannelStatus;
import com.htwg.mobilelearning.enums.FeedbackElementType;
import com.htwg.mobilelearning.models.auth.User;
import com.htwg.mobilelearning.models.feedback.FeedbackChannel;
import com.htwg.mobilelearning.models.feedback.FeedbackElement;
import com.htwg.mobilelearning.models.feedback.FeedbackForm;
import com.htwg.mobilelearning.repositories.FeedbackChannelRepository;
import com.htwg.mobilelearning.repositories.UserRepository;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.PUT;
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

        // generate a FeedbackForm
        FeedbackForm feedbackForm1 = new FeedbackForm("Letzte Vorlesung", "Dies ist das Feedback zur letzten Vorlesung.", List.of(feedbackElement1, feedbackElement2), FeedbackChannelStatus.NOT_STARTED);

        // generate a FeedbackChannel
        FeedbackChannel feedbackChannel1 = new FeedbackChannel("Diskrete Mathematik", "Feedback-Kanal für DiMa", List.of(feedbackForm1));

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
