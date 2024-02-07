package de.htwg_konstanz.mobilelearning;

import java.util.Collections;
import java.util.Map;

import io.quarkus.test.junit.QuarkusTestProfile;

public class MockMongoTestProfile implements QuarkusTestProfile { 

    @Override
    public Map<String, String> getConfigOverrides() {

        // override the mongo port
        return Collections.singletonMap("quarkus.mongodb.connection-string", "mongodb://localhost:27018");
    }
}