package de.htwg_konstanz.mobilelearning.services.auth;

import java.util.HashSet;

import org.eclipse.microprofile.jwt.Claims;

import de.htwg_konstanz.mobilelearning.models.auth.User;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JwtService {

        public String getToken(User user) {


            // evaluate the needed fields for the token
            if (user == null) {
                return null;
            }
            if (user.getName() == null || user.getName().isEmpty()) {
                return null;
            }
            if (user.getId() == null) {
                return null;
            }
            if (user.getEmail() == null || user.getEmail().isEmpty()) {
                return null;
            }
            if (user.getUsername() == null || user.getUsername().isEmpty()) {
                return null;
            }

            String token =
                Jwt
                    .claim(Claims.full_name.name(), user.getName())
                    .claim(Claims.sub.name(), user.getId().toString())
                    .claim(Claims.email.name(), user.getEmail())
                    .claim(Claims.preferred_username.name(), user.getUsername())
                    .groups(new HashSet<String>(user.getRoles()))
                    .expiresAt(System.currentTimeMillis() + 172800000L)
                    .sign();
            return token;
    }    
}
