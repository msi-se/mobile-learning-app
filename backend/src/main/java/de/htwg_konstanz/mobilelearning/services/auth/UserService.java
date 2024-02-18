package de.htwg_konstanz.mobilelearning.services.auth;

import java.util.Arrays;
import java.util.List;

import org.jboss.resteasy.reactive.RestHeader;

import de.htwg_konstanz.mobilelearning.models.Course;
import de.htwg_konstanz.mobilelearning.models.auth.User;
import de.htwg_konstanz.mobilelearning.models.auth.UserRole;
import de.htwg_konstanz.mobilelearning.repositories.CourseRepository;
import de.htwg_konstanz.mobilelearning.repositories.UserRepository;
import jakarta.annotation.security.PermitAll;
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
    CourseRepository courseRepository; // only for testing (TODO: remove)

    @Inject
    JwtService JwtService;
    
    @POST
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/login")
    public Response login(@RestHeader("Authorization") String authorization) throws Exception {

        // decode username and password from basic auth header
        String encodedString = authorization.split(" ")[1];
        byte[] decodedBytes = java.util.Base64.getDecoder().decode(encodedString);
        String decodedString = new String(decodedBytes);
        String username = "";
        String password = "";
        try { username = decodedString.split(":")[0]; } catch (Exception e) {}
        try { password = decodedString.split(":")[1]; } catch (Exception e) {}
        User userFromLdap = null;

        // TEMP: bypass ldap (student, prof, admin as username)
        List<String> demoUsernames = Arrays.asList("Student", "Prof", "Prof2", "Admin", "Brande", "Tobi", "Marvin", "Leon", "Fabi", "Schimkat", "Landwehr" );
        if (demoUsernames.contains(username)) {

            // check if user exists in db
            User existingUser = userRepository.findByUsername(username);
            if (existingUser != null) {
                // return jwt token
                String json = JwtService.getToken(existingUser);
                if (json == null) {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                }
                System.out.println("User exists: " + existingUser.getId().toHexString());
                return Response.ok(json).build();
            }

            User newUser = new User(
                username + "@htwg-konstanz.de",
                "Testuser: " + username,
                username,
                password
            );

            if (username.equals("Student")) {
                newUser.addRole(UserRole.STUDENT);
            } else if (username.equals("Prof")) {
                newUser.addRole(UserRole.PROF);
            } else if (username.equals("Admin")) {
                newUser.addRole(UserRole.ADMIN);
            } else {
                newUser.addRole(UserRole.STUDENT);
            }

            userRepository.persist(newUser);

            // add course "Diskrete Mathematik" to prof
            if (username.equals("Prof")) {
                Course course = courseRepository.findByName("Diskrete Mathematik");
                if (course != null) {
                    course.addOwner(newUser.getId());
                    courseRepository.update(course);
                }
            }

            String json = JwtService.getToken(newUser);
            if (json == null) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
            return Response.ok(json).build();
        }

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
        String usernameFromLdap = userFromLdap.getUsername();
        User existingUser = userRepository.findByUsername(usernameFromLdap);
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

    // for testing
    public UserRepository getUserRepository() {
        return userRepository;
    }
}