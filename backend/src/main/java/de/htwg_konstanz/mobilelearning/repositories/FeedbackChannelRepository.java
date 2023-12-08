package de.htwg_konstanz.mobilelearning.repositories;



import de.htwg_konstanz.mobilelearning.models.feedback.FeedbackChannel;

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

}
