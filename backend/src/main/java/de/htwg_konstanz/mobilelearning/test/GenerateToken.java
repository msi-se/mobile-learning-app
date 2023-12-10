package de.htwg_konstanz.mobilelearning.test;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.jwt.Claims;

import java.util.Arrays;
import java.util.HashSet;

@Path("/token")
@RequestScoped
public class GenerateToken {
    /**
     * Generate JWT token
     */
    @GET
    public String getToken() {
        String token =
                Jwt.issuer("https://example.com/issuer")
                        .upn("jdoe@quarkus.io")
                        .groups(new HashSet<>(Arrays.asList("User", "Admin")))
                        .claim(Claims.birthdate.name(), "2001-07-13")
                        .sign();
        return token;
    }
}
