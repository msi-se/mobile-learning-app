package de.htwg_konstanz.mobilelearning.services.auth;

import org.bson.types.ObjectId;
import org.jboss.resteasy.reactive.RestHeader;

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
import jakarta.ws.rs.core.Response;

// POST: /auth/login ( body: { username: string, password: string } )
    // wenn nicht vorhanden: neuen User anlegen und zurückgeben (mit id)
    // wenn vorhanden: User zurückgeben (um id zu erhalten)

@Path("/user")
public class UserService {

    @Inject
    UserRepository userRepository;

    @Inject
    JwtService JwtService;
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/login")
    public Response login(@RestHeader("Authorization") String authorization) throws Exception {

        // decode username and password from basic auth header
        String encodedString = authorization.split(" ")[1];
        byte[] decodedBytes = java.util.Base64.getDecoder().decode(encodedString);
        String decodedString = new String(decodedBytes);
        String username = decodedString.split(":")[0];
        String password = decodedString.split(":")[1];
        User userFromLdap = null;

        try {
            // fetch user from ldap
            LdapHtwg ldapHtwg = new LdapHtwg();         
            userFromLdap = ldapHtwg.doLogin(username, password);
            System.out.println("User from LDAP: " + userFromLdap);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        // should not happen (just to be sure)
        if (userFromLdap == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        // check if user exists in db
        User existingUser = userRepository.findByUsername(userFromLdap.getUsername());
        System.out.println("Existing user: " + existingUser);
        User user = null;

        // if not, create new user
        if (existingUser == null) {
            User newUser = new User(
                userFromLdap.getEmail(),
                userFromLdap.getName(),
                userFromLdap.getUsername(),
                userFromLdap.getPassword()
            );
            newUser.setRoles(userFromLdap.getRoles());
            userRepository.persist(newUser);
            user = newUser;
        } else {
            user = existingUser;
        }

        // return jwt token
        String json = JwtService.getToken(user);
        if (json == null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.ok(json).build();
    }
}