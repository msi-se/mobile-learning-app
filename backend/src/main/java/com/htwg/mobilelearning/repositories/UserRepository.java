package com.htwg.mobilelearning.repositories;

import com.htwg.mobilelearning.models.auth.User;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserRepository implements PanacheMongoRepository<User> {
    
    public User findById(String id) {
        return find("id", id).firstResult();
    }
}
