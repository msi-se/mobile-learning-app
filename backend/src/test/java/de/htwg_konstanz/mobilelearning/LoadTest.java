package de.htwg_konstanz.mobilelearning;

import static io.restassured.RestAssured.given;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import de.htwg_konstanz.mobilelearning.models.Course;
import de.htwg_konstanz.mobilelearning.models.auth.UserRole;
import de.htwg_konstanz.mobilelearning.services.CourseService;
import de.htwg_konstanz.mobilelearning.services.api.ApiService;
import de.htwg_konstanz.mobilelearning.services.api.models.ApiCourse;
import de.htwg_konstanz.mobilelearning.services.api.models.ApiFeedbackForm;
import de.htwg_konstanz.mobilelearning.services.api.models.ApiFeedbackForm.ApiFeedbackQuestion;
import de.htwg_konstanz.mobilelearning.services.api.models.ApiQuizForm;
import de.htwg_konstanz.mobilelearning.services.api.models.ApiQuizForm.ApiQuizQuestion;
import de.htwg_konstanz.mobilelearning.services.auth.UserService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import jakarta.inject.Inject;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Session;

@QuarkusTest
@TestProfile(MockMongoTestProfile.class)
public class LoadTest {

        @Inject
    private CourseService courseService;

    @Inject
    private ApiService apiService;

    @Inject
    private UserService userService;

    //private String profJwt = "";
    //private String profId = "";

        @BeforeEach
    void init(TestInfo testInfo){
        System.out.println("\n\n========================================");
        System.out.println("               LOAD TEST");
        System.out.println("========================================");
        System.out.println("Test: " + testInfo.getDisplayName());
        //courseService.deleteAllCourses();
        //createProfUser();
    }

    @Test
    @TestSecurity(user = "Prof", roles = { UserRole.PROF })
    @JwtSecurity(claims = { @Claim(key = "sub", value = "profId") })
    public void LoadTestingFeedback() throws ProtocolException, IOException, URISyntaxException {
        // create & get courses
        String url= "loco.in.htwg-konstanz.de/api";
        URL uri = new URI("http://"+url+"/user/login").toURL();
        String profToken = login(uri, "UHJvZjo=");
        String courseId = "65d77a0c274547320b93698c";
        String formId = "65d77a0c274547320b936999";
        String questionId = "65d77a0c274547320b936993";
        int numberOfStudents = 60;

        // create a websocket client
        // (@ServerEndpoint("/course/{courseId}/feedback/form/{formId}/subscribe/{userId}/{jwt}")
        try {
            LiveFeedbackSocketClient profClient = new LiveFeedbackSocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                profClient,
                URI.create("ws://"+url+"/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" + "65d77a0c274547320b93698b" + "/" + profToken)
            );
            Thread.sleep(100); 
            profClient.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "STARTED",
                    "roles": [Prof]
                }
            """);
            Thread.sleep(100); 
            List<LiveFeedbackSocketClient> clients = new ArrayList<>();   

            List<Session> sessions = new ArrayList<>();   
            for (int i = 0; i < numberOfStudents; i++) {
                /*JwtService jwtService = Mockito.mock(JwtService.class);
                Mockito.when(jwtService.getToken(any())).thenReturn(token);
                userService.login("Basic "+ Base64.getEncoder().encodeToString((i + ":").getBytes(StandardCharsets.UTF_8)));*/
                //String response = given().header("Authorization", "Basic " +Base64.getEncoder().encodeToString((i + ":").getBytes(StandardCharsets.UTF_8))).when().post("http://"+url+"/user/login").then().statusCode(200).extract().asString();
                String response = login(uri, Base64.getEncoder().encodeToString((i + ":").getBytes(StandardCharsets.UTF_8)));
                //UserRepository userRepository = new UserRepository();
                //User user = userRepository.findByUsername(""+i);
                //Course courseUpdate = courseService.getCourse(courseId);
                //courseUpdate.addStudent(user.getId());
                //CourseRepository courseRepository = new CourseRepository();
                //courseRepository.update(courseUpdate);
                clients.add(new LiveFeedbackSocketClient());
                URL uriUser = new URI("http://"+url+"/user/load/"+i).toURL();
                String userId = getUserId(uriUser, Base64.getEncoder().encodeToString((i + ":").getBytes(StandardCharsets.UTF_8)));
                System.out.println("userId: " + userId);
                sessions.add(ContainerProvider.getWebSocketContainer().connectToServer(clients.get(i), URI.create("ws://"+url+"/course/" + courseId + "/feedback/form/" + formId + "/subscribe/" +userId.substring(1, userId.length() - 1) + "/" + response)));
                Thread.sleep(100);                
            }

            for (int i = 0; i < numberOfStudents; i++) {
                LiveFeedbackSocketClient client = clients.get(i);
                client.sendMessage(String.format("""
                    {
                        "action": "ADD_RESULT",
                        "resultElementId": %s,
                        "resultValues": [5],
                        "role": "STUDENT"
                    }
                """, questionId));   
                Thread.sleep(100);      
            }
            profClient.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "FINISHED",
                    "roles": [Prof]
                }
            """);
            
            for (int i = 0; i < numberOfStudents; i++) {
                Session sesssion = sessions.get(i);
                sesssion.close();         
            }
            
            session.close();

            // check if the form status has changed
        } catch (Exception e) {
            System.out.println(e);
            Assertions.fail(e.getMessage());
        }
    
    }

    @Test
    @TestSecurity(user = "Prof", roles = { UserRole.PROF })
    @JwtSecurity(claims = { @Claim(key = "sub", value = "profId") })
    public void LoadTestingQuiz() throws ProtocolException, IOException, URISyntaxException {
        // create & get courses
        String url= "loco.in.htwg-konstanz.de/api";
        URL uri = new URI("http://"+url+"/user/login").toURL();
        String profToken = login(uri, "UHJvZjo=");
        String courseId = "65d77a0c274547320b93698c";
        String formId = "65d77a0c274547320b9369a2";
        String questionId1 = "65d77a0c274547320b93699e";
        String questionId2 = "65d77a0c274547320b93699f";
        String questionId3 = "65d77a0c274547320b9369a0";
        String questionId4 = "65d77a0c274547320b9369a1";
        int numberOfStudents = 60;

        // create a websocket client
        // (@ServerEndpoint("/course/{courseId}/feedback/form/{formId}/subscribe/{userId}/{jwt}")
        try {
            LiveFeedbackSocketClient profClient = new LiveFeedbackSocketClient();
            Session session = ContainerProvider.getWebSocketContainer().connectToServer(
                profClient,
                URI.create("ws://"+url+"/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" + "65d77360274547320b935fd9" + "/" + profToken)
            );
            Thread.sleep(100); 
            profClient.sendMessage("""
                {
                    "action": "CHANGE_FORM_STATUS",
                    "formStatus": "STARTED",
                    "roles": [Prof]
                }
            """);
            Thread.sleep(100); 
            List<LiveFeedbackSocketClient> clients = new ArrayList<>();   

            List<Session> sessions = new ArrayList<>();   
            for (int i = 0; i < numberOfStudents; i++) {
                /*JwtService jwtService = Mockito.mock(JwtService.class);
                Mockito.when(jwtService.getToken(any())).thenReturn(token);
                userService.login("Basic "+ Base64.getEncoder().encodeToString((i + ":").getBytes(StandardCharsets.UTF_8)));*/
                //String response = given().header("Authorization", "Basic " +Base64.getEncoder().encodeToString((i + ":").getBytes(StandardCharsets.UTF_8))).when().post("http://"+url+"/user/login").then().statusCode(200).extract().asString();
                String response = login(uri, Base64.getEncoder().encodeToString((i + ":").getBytes(StandardCharsets.UTF_8)));
                //UserRepository userRepository = new UserRepository();
                //User user = userRepository.findByUsername(""+i);
                //Course courseUpdate = courseService.getCourse(courseId);
                //courseUpdate.addStudent(user.getId());
                //CourseRepository courseRepository = new CourseRepository();
                //courseRepository.update(courseUpdate);
                clients.add(new LiveFeedbackSocketClient());
                URL uriUser = new URI("http://"+url+"/user/load/"+i).toURL();
                String userId = getUserId(uriUser, Base64.getEncoder().encodeToString((i + ":").getBytes(StandardCharsets.UTF_8)));
                System.out.println("userId: " + userId);
                sessions.add(ContainerProvider.getWebSocketContainer().connectToServer(clients.get(i), URI.create("ws://"+url+"/course/" + courseId + "/quiz/form/" + formId + "/subscribe/" +userId.substring(1, userId.length() - 1) + "/" + response)));
                Thread.sleep(100);                
            }
            profClient.sendMessage("""
                {
                    "action": "NEXT",
                    "roles": [Prof]
                }
            """); 

            for (int i = 0; i < numberOfStudents; i++) {
                LiveFeedbackSocketClient client = clients.get(i);
                client.sendMessage(String.format("""
                    {
                        "action": "ADD_RESULT",
                        "resultElementId": %s,
                        "resultValues": [1],
                        "role": "STUDENT"
                    }
                """, questionId1));   
                Thread.sleep(100);      
            }
            profClient.sendMessage("""
                {
                    "action": "NEXT",
                    "roles": [Prof]
                }
            """);
            for (int i = 0; i < numberOfStudents; i++) {
                LiveFeedbackSocketClient client = clients.get(i);
                client.sendMessage(String.format("""
                    {
                        "action": "ADD_RESULT",
                        "resultElementId": %s,
                        "resultValues": [1],
                        "role": "STUDENT"
                    }
                """, questionId2));   
                Thread.sleep(100);      
            }
            profClient.sendMessage("""
                {
                    "action": "NEXT",
                    "roles": [Prof]
                }
            """); 
            for (int i = 0; i < numberOfStudents; i++) {
                LiveFeedbackSocketClient client = clients.get(i);
                client.sendMessage(String.format("""
                    {
                        "action": "ADD_RESULT",
                        "resultElementId": %s,
                        "resultValues": [1],
                        "role": "STUDENT"
                    }
                """, questionId3));   
                Thread.sleep(100);      
            }
            profClient.sendMessage("""
                {
                    "action": "NEXT",
                    "roles": [Prof]
                }
            """); 
            for (int i = 0; i < numberOfStudents; i++) {
                LiveFeedbackSocketClient client = clients.get(i);
                client.sendMessage(String.format("""
                    {
                        "action": "ADD_RESULT",
                        "resultElementId": %s,
                        "resultValues": [1],
                        "role": "STUDENT"
                    }
                """, questionId4));   
                Thread.sleep(100);      
            }
            profClient.sendMessage("""
                {
                    "action": "NEXT",
                    "roles": [Prof]
                }
            """); 
            
            
            for (int i = 0; i < numberOfStudents; i++) {
                Session sesssion = sessions.get(i);
                sesssion.close();         
            }
            
            session.close();

            // check if the form status has changed
        } catch (Exception e) {
            System.out.println(e);
            Assertions.fail(e.getMessage());
        }
    
    }

    private String getUserId(URL url, String user) throws ProtocolException, IOException {
        HttpURLConnection conn = (HttpURLConnection) 
        url.openConnection();
        String basicAuth = "Basic " + user;
        conn.setRequestProperty("Authorization", basicAuth);
        
        // Set the request method to POST
        conn.setRequestMethod("GET");
        
        // Get the response code
        int responseCode = conn.getResponseCode();
        
        // Read the response
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        
        conn.disconnect();
        return content.toString();
    }

    private String login(URL url, String user) throws ProtocolException, IOException {
        HttpURLConnection conn = (HttpURLConnection) 
        url.openConnection();
        String basicAuth = "Basic " + user;
        conn.setRequestProperty("Authorization", basicAuth);
        
        // Set the request method to POST
        conn.setRequestMethod("POST");
        
        // Get the response code
        int responseCode = conn.getResponseCode();
        
        // Read the response
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        
        conn.disconnect();
        return content.toString();
    }

    @Test
    @TestSecurity(user = "Prof", roles = { UserRole.PROF})
    @JwtSecurity(claims = { @Claim(key = "email", value = "prof@htwg-konstanz.de") })
    private List<Course> createCourseServer(String url, String profToken) {
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
                                            "Q-Q-PDRODUCTOWNER")),
                            "Q-ROLES")),
            "AUME23",
            "1");

            /*HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", "Bearer " + profToken);
            
            // Set the request method to POST
            conn.setRequestMethod("PATCH");
            
            // Get the response code
            int responseCode = conn.getResponseCode();
            
            // Read the response
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            
            conn.disconnect();*/

            List<Course> response = given()
            .header("Authorization", "Bearer " + profToken)
            .body(List.of(apiCourse).toString())
            .when()
            .patch("http://"+url+"/public/courses")
            .then().statusCode(200).extract().body().jsonPath().getList("$");
        return response;
    } 

    @Test
    @TestSecurity(user = "Prof", roles = { UserRole.PROF})
    @JwtSecurity(claims = { @Claim(key = "email", value = "prof@htwg-konstanz.de") })
    private List<Course> createCourse() {
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
                                            "Q-Q-PDRODUCTOWNER")),
                            "Q-ROLES")),
            "AUME23",
            "1");
        return apiService.updateCourses(List.of(apiCourse));
    } 

        /*public void createProfUser() {
        // creates Prof User
        try {
            Response response = userService.login("Basic UHJvZjo=");
            profJwt = response.getEntity().toString(); // save jwt for later use
            String jwtJson = new String(Base64.getUrlDecoder().decode(profJwt.split("\\.")[1]), StandardCharsets.UTF_8);
            DefaultJWTCallerPrincipal defaultJWTCallerPrincipal = new DefaultJWTCallerPrincipal(
                    JwtClaims.parse(jwtJson));
            Assertions.assertEquals(defaultJWTCallerPrincipal.getClaim("full_name"), "Prof");
            Assertions.assertTrue(defaultJWTCallerPrincipal.getClaim("sub").toString().length() > 0);
            profId = defaultJWTCallerPrincipal.getClaim("sub").toString(); // save id for later use
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }*/
}
