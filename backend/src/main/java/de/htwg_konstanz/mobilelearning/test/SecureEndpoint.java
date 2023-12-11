package de.htwg_konstanz.mobilelearning.test;

import org.eclipse.microprofile.jwt.JsonWebToken;

import io.smallrye.jwt.auth.principal.ParseException;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

@Path("/secure")
@RequestScoped
public class SecureEndpoint {

    @Inject
    JsonWebToken jwt;

    @GET
    @Path("permit-all")
    @PermitAll
    @Produces(MediaType.TEXT_PLAIN)
    public String hello(@Context SecurityContext ctx) {
        return getResponseString(ctx);
    }

    @GET
    @Path("roles-allowed")
    @RolesAllowed({ "Student", "Teacher" })
    @Produces(MediaType.TEXT_PLAIN)
    public String helloRolesAllowed(@Context SecurityContext ctx) throws ParseException {
        return getResponseString(ctx);
    }

    @GET
    @Path("roles-allowed-Teacher")
    @RolesAllowed("Teacher")
    @Produces(MediaType.TEXT_PLAIN)
    public String helloRolesAllowedTeacher(@Context SecurityContext ctx) {
        return getResponseString(ctx);
    }

    @GET
    @Path("deny-all")
    @DenyAll
    @Produces(MediaType.TEXT_PLAIN)
    public String helloShouldDeny(@Context SecurityContext ctx) {
        throw new InternalServerErrorException("This method must not be invoked");
    }

    private String getResponseString(SecurityContext ctx) {
        String name;
        if (ctx.getUserPrincipal() == null) {
            name = "anonymous";
        } else if (!ctx.getUserPrincipal().getName().equals(jwt.getName())) {
            throw new InternalServerErrorException("Principal and JsonWebToken names do not match");
        } else {
            name = ctx.getUserPrincipal().getName();
        }
        return String.format("hello + %s,"
                        + " isHttps: %s,"
                        + " authScheme: %s,"
                        + " hasJWT: %s",
                name, ctx.isSecure(), ctx.getAuthenticationScheme(), hasJwt());
    }

    private boolean hasJwt() {
        return jwt.getClaimNames() != null;
    }
}
