package de.htwg_konstanz.mobilelearning.services;

import de.htwg_konstanz.mobilelearning.helper.AliasGenerator;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

@Path("/funnyalias")
@Produces("text/plain")
public class AliasService {
    
    @GET
    @Path("/")
    public String getRandomAlias() {
        return AliasGenerator.generateAlias();
    }
}
