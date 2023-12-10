package de.htwg_konstanz.mobilelearning.controller;

import de.htwg_konstanz.mobilelearning.models.auth.User;
import de.htwg_konstanz.mobilelearning.services.auth.LdapHtwg;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/auth")
public class AuthController {

    @GET
    public User auth() throws Exception {
        LdapHtwg ldapUser = new LdapHtwg();
        return ldapUser.doLogin("", "");
    }
    
}
