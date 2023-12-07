package com.htwg.mobilelearning.repositories;

import com.htwg.mobilelearning.models.feedback.FeedbackForm;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FeedbackFormRepository implements PanacheMongoRepository<FeedbackForm> {
    
    public FeedbackForm findById(String id) {
        return find("id", id).firstResult();
    }
    
}
