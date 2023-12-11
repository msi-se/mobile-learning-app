package de.htwg_konstanz.mobilelearning.services.auth;

import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.microprofile.jwt.Claims;

import de.htwg_konstanz.mobilelearning.models.auth.User;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JwtService {

        public String getToken(User user) {
            String role;
            if(user.getIsTeacher()){
                role = "Teacher";
            }else{
                role = "Student";
            }
            String token =
                Jwt.claim(Claims.full_name.name(), user.getName())
                        .claim(Claims.email.name(), user.getEmail())
                        .claim(Claims.preferred_username.name(), user.getUsername())
                        .groups(new HashSet<>(Arrays.asList(role)))
                        .expiresAt(System.currentTimeMillis() + 172800000L)
                        .sign();
            return token;
    }    
}
