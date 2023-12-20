package de.htwg_konstanz.mobilelearning.services.auth;

import java.nio.charset.StandardCharsets;
import java.util.*;

import io.smallrye.jwt.auth.principal.DefaultJWTCallerPrincipal;
import org.eclipse.microprofile.jwt.Claims;

import de.htwg_konstanz.mobilelearning.models.auth.User;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;

@ApplicationScoped
public class JwtService {

        public DefaultJWTCallerPrincipal getJwtClaims(String jwt) throws InvalidJwtException {
            String jwtJson = new String(Base64.getUrlDecoder().decode(jwt.split("\\.")[1]), StandardCharsets.UTF_8);
            DefaultJWTCallerPrincipal defaultJWTCallerPrincipal = new DefaultJWTCallerPrincipal(JwtClaims.parse(jwtJson));
            return defaultJWTCallerPrincipal;
        }

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
                    .expiresAt((System.currentTimeMillis() + 172800000L)/1000L)
                    .sign();
            return token;
    }    
}
