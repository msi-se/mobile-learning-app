package de.htwg_konstanz.mobilelearning.repositories;

import de.htwg_konstanz.mobilelearning.models.auth.User;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserRepository implements PanacheMongoRepository<User> {
    
    public User findById(String id) {
        return find("id", id).firstResult();
    }

    public User findByUsername(String username) {
        return find("username", username).firstResult();
    }
}
