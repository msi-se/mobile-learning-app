package de.htwg_konstanz.mobilelearning;

import static io.restassured.RestAssured.given;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.jose4j.jwt.JwtClaims;
import org.junit.jupiter.api.Assertions;

import com.google.gson.Gson;

import de.htwg_konstanz.mobilelearning.models.Course;
import de.htwg_konstanz.mobilelearning.services.api.models.ApiCourse;
import de.htwg_konstanz.mobilelearning.services.api.models.ApiFeedbackForm;
import de.htwg_konstanz.mobilelearning.services.api.models.ApiFeedbackForm.ApiFeedbackQuestion;
import de.htwg_konstanz.mobilelearning.services.api.models.ApiQuizForm;
import de.htwg_konstanz.mobilelearning.services.api.models.ApiQuizForm.ApiQuizQuestion;
import io.restassured.http.ContentType;
import io.smallrye.jwt.auth.principal.DefaultJWTCallerPrincipal;
import io.restassured.response.Response;

public class Helper {

    public static List<Course> createCourse() {
        return createCourse("Prof");
    }

    public static List<Course> createCourse(String username) {
        return createCourse(username, "1");
    }

    public static List<Course> createCourse(String username, String moodleCourseId) {
        // create a course via the json api
        ApiCourse apiCourse = new ApiCourse(
                "AUME 23/24",
                "Agile Vorgehensmodelle und Mobile Kommunikation",
                List.of(
                        new ApiFeedbackForm(
                                "Erster Sprint",
                                "Hier wollen wir Ihr Feedback zum ersten Sprint einholen",
                                List.of(
                                        new ApiFeedbackQuestion(
                                                "Rolle",
                                                "Wie gut hat Ihnen ihre Pizza gefallen?",
                                                "SLIDER",
                                                new ArrayList<String>(),
                                                "F-Q-ROLLE",
                                                "gut",
                                                "schlecht")),
                                "F-ERSTERSPRINT")),
                List.of(
                        new ApiQuizForm(
                                "Rollenverständnis bei Scrum",
                                "Ein Quiz zum Rollenverständnis und Teamaufbau bei Scrum",
                                List.of(
                                        new ApiQuizQuestion(
                                                "Product Owner",
                                                "Welche der folgenden Aufgaben ist nicht Teil der Rolle des Product Owners?",
                                                "SINGLE_CHOICE",
                                                List.of(
                                                        "Erstellung des Product Backlogs",
                                                        "Priorisierung des Product Backlogs",
                                                        "Pizza bestellen für jedes Daily"),
                                                true,
                                                List.of("2"),
                                                "Q-Q-PDRODUCTOWNER"),
                                                
                                        new ApiQuizQuestion(
                                                "Product Owner",
                                                "Test Frage?",
                                                "SINGLE_CHOICE",
                                                List.of(
                                                        "Antwort 1",
                                                        "Antwort 2",
                                                        "Antwort 3"),
                                                true,
                                                List.of("2"),
                                                "Q-Q-PDRODUCTOWNER")),                                        
                                "Q-ROLES")),
                "AUME23",
                moodleCourseId);

        Gson gson = new Gson();
        String json = gson.toJson(List.of(apiCourse));

        Response response = null;
        try {

            String jwt = createMockUser(username).getJwt();
            response = given()
                    .accept(ContentType.JSON)
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + jwt)
                    .body(json)
                    .patch("/public/courses/");
        } catch (Exception e) {
            System.out.println(e);
            Assertions.fail(e.getMessage());
        }

        // print the response
        String bodyString = response.getBody().asString();
        System.out.println(bodyString);

        List<Course> courses = response
                .then()
                .statusCode(200)
                .extract()
                .body()
                .jsonPath()
                .getList(".", Course.class);

        return courses;
    }

    public static MockUser createMockUser(String username) {
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
        return new MockUser(id, jwt);
    }
}
