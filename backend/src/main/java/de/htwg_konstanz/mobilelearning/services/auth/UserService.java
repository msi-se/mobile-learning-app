package de.htwg_konstanz.mobilelearning.services.auth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.SecretKey;

import org.jboss.resteasy.reactive.RestHeader;

import de.htwg_konstanz.mobilelearning.helper.Crypto;
import de.htwg_konstanz.mobilelearning.models.Course;
import de.htwg_konstanz.mobilelearning.models.auth.User;
import de.htwg_konstanz.mobilelearning.models.auth.UserRole;
import de.htwg_konstanz.mobilelearning.models.feedback.FeedbackForm;
import de.htwg_konstanz.mobilelearning.models.feedback.FeedbackParticipant;
import de.htwg_konstanz.mobilelearning.models.quiz.QuizForm;
import de.htwg_konstanz.mobilelearning.models.quiz.QuizParticipant;
import de.htwg_konstanz.mobilelearning.models.stats.UserStats;
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
    
    /**
     * Login user.
     * If user does not exist, create new user. 
     * 
     * @param authorization Auth header of the request
     * @return
     * @throws Exception
     */
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

        SecretKey passEncrKey = null;
        String encrPassword = "";

        // TEMP: bypass ldap (student, prof, admin as username)
        List<String> demoUsernames = Arrays.asList("Student", "Prof", "Prof2", "Admin", "Brande", "Tobi", "Marvin", "Leon", "Fabi", "Schimkat", "Landwehr" );
        if (demoUsernames.stream().anyMatch(username::startsWith)) {

            // check if user exists in db
            User existingUser = userRepository.findByUsername(username);
            if (existingUser != null) {
                encrPassword = existingUser.encryptPassword(password);
                // return jwt token
                String json = JwtService.getToken(existingUser, encrPassword);
                if (json == null) {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                }
                System.out.println("User exists: " + existingUser.getId().toHexString());
                return Response.ok(json).build();
            }

            passEncrKey = Crypto.generateKey();

            User newUser = new User(
                username + "@htwg-konstanz.de",
                "Testuser: " + username,
                username,
                Crypto.keyToString(passEncrKey)
            );

            encrPassword = newUser.encryptPassword(password);

            if (username.startsWith("Student")) {
                newUser.addRole(UserRole.STUDENT);
            } else if (username.startsWith("Prof")) {
                newUser.addRole(UserRole.PROF);
            } else if (username.startsWith("Admin")) {
                newUser.addRole(UserRole.ADMIN);
            } else {
                newUser.addRole(UserRole.STUDENT);
            }

            userRepository.persist(newUser);

            String json = JwtService.getToken(newUser, encrPassword);
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
            passEncrKey = Crypto.generateKey();
            User newUser = new User(
                userFromLdap.getEmail(),
                userFromLdap.getName(),
                userFromLdap.getUsername(),
                Crypto.keyToString(passEncrKey)
            );
            newUser.setRoles(userFromLdap.getRoles());
            userRepository.persist(newUser);
            user = newUser;
        } else {
            user = existingUser;
            passEncrKey = Crypto.stringToKey(user.getPassEncrKey());
            userRepository.update(user);
        }

        encrPassword = user.encryptPassword(password);

        // return jwt token
        String json = JwtService.getToken(user, encrPassword);
        if (json == null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.ok(json).build();
    }

    // for testing
    public UserRepository getUserRepository() {
        return userRepository;
    }

    // for testing
    public void deleteAllUsers() {
        userRepository.deleteAll();
    }

    public void updateUserStatsByFeedbackForm(FeedbackForm form) {
        for (FeedbackParticipant participant : form.getParticipants()) {
            User user = userRepository.findById(participant.getUserId());
            if (user == null) {
                continue;
            }
            UserStats stats = user.getStats();
            if (stats == null) { stats = new UserStats(); }
            stats.incrementCompletedFeedbackForms();
            user.setStats(stats);
            userRepository.update(user);
        }
    }

    public void updateUserStatsByQuizForm(QuizForm form) {

        // order participants by score
        List<QuizParticipant> participants = form.getParticipants();
        participants.sort((p1, p2) -> p2.getScore().compareTo(p1.getScore()));
        List<List<QuizParticipant>> groups = new ArrayList<>();
        List<QuizParticipant> group = new ArrayList<>();
        Integer lastScore = null;
        for (QuizParticipant participant : participants) {
            if (lastScore == null) {
                lastScore = participant.getScore();
            }
            if (lastScore.equals(participant.getScore())) {
                group.add(participant);
            } else {
                groups.add(group);
                group = new ArrayList<>();
                group.add(participant);
                lastScore = participant.getScore();
            }
        }

        // update stats for each participant
        for (QuizParticipant participant : participants) {
            User user = userRepository.findById(participant.getUserId());
            if (user == null) {
                continue;
            }
            UserStats stats = user.getStats();
            if (stats == null) { stats = new UserStats(); }

            stats.doneQuiz(participants.indexOf(participant) + 1, participant.getScore());

            user.setStats(stats);
            userRepository.update(user);
        }
    }
}