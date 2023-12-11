package de.htwg_konstanz.mobilelearning.services.auth;

import org.bson.types.ObjectId;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.htwg_konstanz.mobilelearning.helper.ObjectIdTypeAdapter;
import de.htwg_konstanz.mobilelearning.models.auth.User;
import de.htwg_konstanz.mobilelearning.repositories.UserRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

// POST: /auth/login ( body: { username: string, password: string } )
    // wenn nicht vorhanden: neuen User anlegen und zurückgeben (mit id)
    // wenn vorhanden: User zurückgeben (um id zu erhalten)

@Path("/auth")
public class UserService {

    @Inject UserRepository userRepository;
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/login")
    public String login(User user) {

        // check if user exists
        User existingUser = userRepository.findByUsername(user.getUsername());

        if (existingUser == null) {
            user = new User(user.getEmail(), user.getName(), user.getUsername(), user.getPassword());
            userRepository.persist(user);
        } else {

            // check if password is correct
            if (!existingUser.authenticate(user.getPassword())) {
                return null;
            }
            user = existingUser;
        }

        Gson gson = new GsonBuilder().registerTypeAdapter(ObjectId.class, new ObjectIdTypeAdapter()).create();
        user.setPassword(null);
        return gson.toJson(user);
    }

}