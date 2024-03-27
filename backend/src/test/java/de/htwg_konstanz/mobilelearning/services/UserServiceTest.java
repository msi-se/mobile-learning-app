package de.htwg_konstanz.mobilelearning.services;

import static io.restassured.RestAssured.given;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.jose4j.jwt.JwtClaims;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.htwg_konstanz.mobilelearning.MockMongoTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.response.Response;
import io.smallrye.jwt.auth.principal.DefaultJWTCallerPrincipal;

@QuarkusTest
@TestProfile(MockMongoTestProfile.class)
public class UserServiceTest {

    String jwt = null;
    String id = null;

    @Test
    public void testLogin() {

        String username = "Prof-1";

        String jwt = null;
        String id = null;
        try {
            String basicAuth = "Basic " + Base64.getEncoder().encodeToString((username + ":").getBytes());

            Response response = given()
                    .header("Authorization", basicAuth)
                    .when()
                    .post("/user/login");

            jwt = response.getBody().asString();
            String jwtJson = new String(Base64.getUrlDecoder().decode(jwt.split("\\.")[1]),
                    StandardCharsets.UTF_8);
            DefaultJWTCallerPrincipal defaultJWTCallerPrincipal = new DefaultJWTCallerPrincipal(
                    JwtClaims.parse(jwtJson));
            Assertions.assertEquals(defaultJWTCallerPrincipal.getClaim("full_name"), username);
            Assertions.assertTrue(defaultJWTCallerPrincipal.getClaim("sub").toString().length() > 0);
            id = defaultJWTCallerPrincipal.getClaim("sub").toString();
        } catch (Exception e) {
            Assertions.fail(e);
        }
        Assertions.assertNotNull(jwt);
        Assertions.assertNotNull(id);

        this.jwt = jwt;
        this.id = id;
    }

    @Test
    public void testVerifySuccess() {
        this.testLogin();
        given()
            .when()
            .header("Authorization", "Bearer " + this.jwt)
            .get("/user/verify")
            .then()
            .statusCode(200);
    }

    @Test
    public void testVerifyFailWithRandomToken() {
        given()
            .when()
            .header("Authorization", "Bearer " + "bullshit")
            .get("/user/verify")
            .then()
            .statusCode(401);
    }

}
