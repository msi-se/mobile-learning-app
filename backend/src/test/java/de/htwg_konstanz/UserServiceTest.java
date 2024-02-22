package de.htwg_konstanz;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class UserServiceTest {

    @Test
    void testUnauthenticated(){
        assertEquals("", "");
    }

    
}
