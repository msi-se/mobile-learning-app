package de.htwg_konstanz.mobilelearning.repositories;



import org.bson.types.ObjectId;

import de.htwg_konstanz.mobilelearning.models.feedback.FeedbackChannel;
import de.htwg_konstanz.mobilelearning.models.feedback.FeedbackForm;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FeedbackChannelRepository implements PanacheMongoRepository<FeedbackChannel> {
    
    // public FeedbackChannel findById(ObjectId id) {
    //     return find("id", id).firstResult();
    // }

    // public FeedbackChannel findByName(String name) {
    //     return find("name", name).firstResult();
    // }

    public FeedbackChannel findByFormConnectCode(Integer connectCode) {
        return find("feedbackForms.connectCode", connectCode).firstResult();
    }

    public FeedbackForm findFeedbackFormById(ObjectId feedbackChannelId, ObjectId feedbackFormId) {
        FeedbackChannel feedbackChannel = findById(feedbackChannelId);
        if (feedbackChannel == null) {
            return null;
        }

        return feedbackChannel.getFeedbackFormById(feedbackFormId);
    }

    public FeedbackForm findFeedbackFormByConnectCode(Integer connectionCode) {
        FeedbackChannel feedbackChannel = findByFormConnectCode(connectionCode);
        if (feedbackChannel == null) {
            return null;
        }

        return feedbackChannel.getFeedbackFormByConnectCode(connectionCode);
    }

}
