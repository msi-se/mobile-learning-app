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
        FeedbackElement feedbackElement1 = new FeedbackElement(
            "Verständlichkeit",
            "Wie verständlich war das Thema Kombinatorik?",
            FeedbackElementType.STARS,
            null
        );
        FeedbackElement feedbackElement2 = new FeedbackElement(
            "Kurzweiligkeit",
            "Wie kurzweilig war die Vorlesung? (links = langweilig, rechts = kurzweilig)",
            FeedbackElementType.SLIDER,
            null
        );
        FeedbackElement feedbackElement3 = new FeedbackElement(
            "Praxisbezug",
            "Wie bewerten Sie den Praxisbezug der Vorlesung? (links = wenig Praxisbezug, rechts = viel Praxisbezug)",
            FeedbackElementType.SLIDER,
            null
        );
        FeedbackElement feedbackElement4 = new FeedbackElement(
            "Sprachbarriere",
            "Die Vorlesung wurde auf Englisch gehalten. Wie fanden Sie die Verständlichkeit?",
            FeedbackElementType.SLIDER,
            null
        );
        FeedbackElement feedbackElement5 = new FeedbackElement(
            "Prüfungsvorbereitung",
            "Wenn jetzt direkt die Prüfung wäre, wie gut fühlen Sie sich vorbereitet?",
            FeedbackElementType.STARS,
            null
        );
        FeedbackElement feedbackElement6 = new FeedbackElement(
            "Technische Mittel",
            "Wie bewerten Sie die technischen Mittel, die in der Vorlesung verwendet wurden?",
            FeedbackElementType.STARS,
            null
        );

        // generate a FeedbackChannel
        FeedbackChannel feedbackChannelDima = new FeedbackChannel("Diskrete Mathematik", "Feedback-Kanal für DiMa", null);
        FeedbackChannel feedbackChannelAUME = new FeedbackChannel("AUME", "Feedback-Kanal für Agile Vorgehensmodelle und Mobile Kommunikation", null);
        FeedbackChannel feedbackChannelCloud = new FeedbackChannel("Cloud Application Development", "Feedback-Kanal für Cloud Application Development", null);

        // generate a FeedbackForm
        ObjectId feedbackChannelDimaId = feedbackChannelDima.getId();
        ObjectId feedbackChannelAUMEId = feedbackChannelAUME.getId();
        ObjectId feedbackChannelCloudId = feedbackChannelCloud.getId();
        FeedbackForm feedbackForm1 = new FeedbackForm(
            feedbackChannelDimaId,
            "1. Monat im neuen Semester",
            "Dies ist das Feedback für den 1. Monat im neuen Semester",
            List.of(feedbackElement2, feedbackElement4, feedbackElement5),
            FeedbackChannelStatus.NOT_STARTED
        );
        feedbackChannelDima.addFeedbackForm(feedbackForm1);
        FeedbackForm feedbackForm2 = new FeedbackForm(
            feedbackChannelDimaId,
            "Kombinatorik",
            "Dies ist das Feedback zum Thema Kombinatorik",
            List.of(feedbackElement1, feedbackElement2, feedbackElement3, feedbackElement4, feedbackElement5, feedbackElement6),
            FeedbackChannelStatus.NOT_STARTED
        );
        feedbackChannelDima.addFeedbackForm(feedbackForm2);
        FeedbackForm feedbackForm3 = new FeedbackForm(
            feedbackChannelAUMEId,
            "1. Monat im neuen Semester",
            "Dies ist das Feedback für den 1. Monat im neuen Semester",
            List.of(feedbackElement2, feedbackElement4, feedbackElement5),
            FeedbackChannelStatus.NOT_STARTED
        );
        feedbackChannelAUME.addFeedbackForm(feedbackForm3);
        FeedbackForm feedbackForm4 = new FeedbackForm(
            feedbackChannelCloudId,
            "1. Monat im neuen Semester",
            "Dies ist das Feedback für den 1. Monat im neuen Semester",
            List.of(feedbackElement2, feedbackElement4, feedbackElement5),
            FeedbackChannelStatus.NOT_STARTED
        );
        feedbackChannelCloud.addFeedbackForm(feedbackForm4);

        // save the FeedbackChannel
        feedbackChannelRepository.persist(feedbackChannelDima);
        feedbackChannelRepository.persist(feedbackChannelAUME);
        feedbackChannelRepository.persist(feedbackChannelCloud);

        // create dummy user
        User user1 = new User("Johannes@example.com","Johannes", "jo123joe");
        User user2 = new User("Fabi@example.com","Fabi", "fa123fae");

        // save the user
        userRepository.persist(user1);
        userRepository.persist(user2);

        // return all FeedbackChannels and Users
        return List.of(feedbackChannelRepository.listAll(), userRepository.listAll());
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/deleteallchannels")
    public String deleteAllFeedbackChannels() {
        feedbackChannelRepository.deleteAll();
        return "All FeedbackChannels deleted";
    }
}
