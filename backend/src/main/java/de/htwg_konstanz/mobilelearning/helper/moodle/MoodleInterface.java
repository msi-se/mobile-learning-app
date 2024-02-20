package de.htwg_konstanz.mobilelearning.helper.moodle;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Moodle service that returns moodle courses of a user.
 */
public class MoodleInterface {
    public List<MoodleCourse> courses;
    private String username;
    private String password;
    private String token;
    private Integer userId;

    public MoodleInterface(String username,
            String password) {
        this.username = username;
        this.password = password;
    }

    public Boolean login() {

        ObjectMapper mapper = new ObjectMapper();

        try {

            // get the token from
            // https://moodle.htwg-konstanz.de/moodle/login/token.php?username=USERNAME&password=PASSWORD&service=SERVICESHORTNAME
            CloseableHttpClient client = HttpClients.createDefault();
            HttpGet request = new HttpGet("https://moodle.htwg-konstanz.de/moodle/login/token.php?username="
                    + this.username + "&password=" + this.password + "&service=moodle_mobile_app");
            MoodleTokenResponse tokenResponse = mapper.readValue(client.execute(request).getEntity().getContent(),
                    MoodleTokenResponse.class);
            this.token = tokenResponse.token;
            // System.out.println("Successfully logged in as " + this.username + " with
            // token " + this.token.substring(0, 5) + "...");

            // get user id
            String wsFunction = "core_webservice_get_site_info";
            request = new HttpGet("https://moodle.htwg-konstanz.de/moodle/webservice/rest/server.php?wstoken="
                    + this.token + "&wsfunction=" + wsFunction + "&moodlewsrestformat=json");
            String response = EntityUtils.toString(client.execute(request).getEntity());
            MoodleUserIdResponse userIdResponse = mapper.readValue(response, MoodleUserIdResponse.class);
            Integer userId = userIdResponse.userid;
            this.userId = userId;
            // System.out.println("Got user id: " + userId);

            // get all courses
            wsFunction = "core_enrol_get_users_courses";
            request = new HttpGet("https://moodle.htwg-konstanz.de/moodle/webservice/rest/server.php?wstoken="
                    + this.token + "&wsfunction=" + wsFunction + "&userid=" + userId + "&moodlewsrestformat=json");
            response = EntityUtils.toString(client.execute(request).getEntity());
            MoodleCourse[] courses = mapper.readValue(response, MoodleCourse[].class);
            this.courses = List.of(courses);
            // System.out.println("Got courses: ");
            // System.out.println(this.courses);

        } catch (Exception e) {
            System.out.println("Error while logging into moodle: " + e.getMessage());
            return false;
        }

        return true;

    };

    /**
     * Returns moodle courses of the user.
     * Courses are fetched from moodle api with username & password.
     * 
     * @return List of moodle courses
     */
    public List<MoodleCourse> getCourses() {

        // TEMP: mock the special users (Prof, Student, Admin)
        if (this.username.equals("Prof") || this.username.equals("Student") || this.username.equals("Admin")) {
            return List.of(new MoodleCourse("1"), new MoodleCourse("2"), new MoodleCourse("3"));
        }

        // if courses are already set return them if not fetch them 
        if (this.courses != null) {
            return this.courses;
        }

        // if token is not set login first
        if (this.token == null) {
            if (!this.login()) {
                return new ArrayList<MoodleCourse>();
            }
        }

        ObjectMapper mapper = new ObjectMapper();

        try {

            // get all courses
            CloseableHttpClient client = HttpClients.createDefault();
            String wsFunction = "core_enrol_get_users_courses";
            HttpGet request = new HttpGet("https://moodle.htwg-konstanz.de/moodle/webservice/rest/server.php?wstoken=" + this.token + "&wsfunction=" + wsFunction + "&userid=" + userId + "&moodlewsrestformat=json");
            String response = EntityUtils.toString(client.execute(request).getEntity());
            MoodleCourse[] courses = mapper.readValue(response, MoodleCourse[].class);
            this.courses = List.of(courses);
            // System.out.println("Got courses: ");
            // System.out.println(this.courses);

            return this.courses;

        } catch (Exception e) {
            System.out.println("Error while getting courses from moodle: " + e.getMessage());
            return new ArrayList<MoodleCourse>();
        }

    }

    // main method for testing
    public static void main(String[] args) {
        MoodleInterface moodle = new MoodleInterface("xxx", "xxx");
        moodle.login();
        moodle.getCourses();
    }
}
