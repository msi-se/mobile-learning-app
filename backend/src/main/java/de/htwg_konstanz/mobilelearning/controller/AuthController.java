package de.htwg_konstanz.mobilelearning.controller;

import java.util.Base64;

import org.jboss.resteasy.reactive.RestHeader;

import de.htwg_konstanz.mobilelearning.models.auth.User;
import de.htwg_konstanz.mobilelearning.services.auth.LdapHtwg;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/auth")
public class AuthController {

    @GET
    public Response auth(@RestHeader("Authorization") String authorization) throws Exception {
        String encodedString = authorization.split(" ")[1];
        byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
        String decodedString = new String(decodedBytes);
        String username = decodedString.split(":")[0];
        String password = decodedString.split(":")[1];

        try {
            LdapHtwg ldapUser = new LdapHtwg();
            User user = ldapUser.doLogin(username, password);
            return Response.ok(user).build();
        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }
    
}
