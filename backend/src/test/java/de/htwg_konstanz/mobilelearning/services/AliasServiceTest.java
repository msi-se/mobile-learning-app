
package de.htwg_konstanz.mobilelearning.services;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import de.htwg_konstanz.mobilelearning.MockMongoTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.response.Response;

@QuarkusTest
@TestProfile(MockMongoTestProfile.class)
public class AliasServiceTest {

    @BeforeEach
    void init(TestInfo testInfo) {
        System.out.println("------------------------------");
        System.out.println("Test: " + testInfo.getDisplayName());
    }

    @Test
    public void testGetRandomAlias() {
        Response response = given().when().get("/funnyalias");
        response.then().statusCode(200);
        String alias = response.getBody().asString();
        
        // check that alias is not null and at least 3 characters long
        Assertions.assertNotNull(alias);
        Assertions.assertTrue(alias.length() >= 3);
    }
}