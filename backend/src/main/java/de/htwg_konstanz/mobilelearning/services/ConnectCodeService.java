package de.htwg_konstanz.mobilelearning.services;

import org.jboss.resteasy.reactive.RestPath;
import org.json.JSONObject;

import de.htwg_konstanz.mobilelearning.models.Course;
import de.htwg_konstanz.mobilelearning.models.auth.UserRole;
import de.htwg_konstanz.mobilelearning.models.feedback.FeedbackForm;
import de.htwg_konstanz.mobilelearning.models.quiz.QuizForm;
import de.htwg_konstanz.mobilelearning.repositories.CourseRepository;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Service used to get the course and form id for a given connect code.
 * Each form has a unique connect code which is used to identify the form.
 * Professors share this code with their students in a live session.
 */
@Path("/connectto")
public class ConnectCodeService {
    
    @Inject
    private CourseRepository courseRepository;

    /**
     * Returns the course and feedback form id for a given connect code.
     * 
     * @param connectCode random 6-digit connect code created in Form.java
     * @return Course and feedback form id
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/feedback/{connectCode}")
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    public String getFeedbackFormConnectCode(@RestPath Integer connectCode) {
        Course course = courseRepository.findByFeedbackFormConnectCode(connectCode);
        if (course == null) {
            throw new NotFoundException("Course with feedback form connect code " + connectCode + " not found.");
        }

        FeedbackForm feedbackForm = null;
        for (FeedbackForm form : course.getFeedbackForms()) {
            if (form.getConnectCode().equals(connectCode)) {
                feedbackForm = form;
            }
        };

        if (feedbackForm == null) {
            throw new NotFoundException("Feedback form with connect code " + connectCode + " not found.");
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("courseId", course.getId().toHexString());
        jsonObject.put("formId", feedbackForm.getId().toHexString());
        return jsonObject.toString();
    }

    /**
     * Returns the course and quiz form id for a given connect code.
     * 
     * @param connectCode random 6-digit connect code
     * @return Course and quiz form id
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/quiz/{connectCode}")
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    public String getQuizFormConnectCode(@RestPath Integer connectCode) {
        Course course = courseRepository.findByQuizFormConnectCode(connectCode);
        if (course == null) {
            throw new NotFoundException("Course with quiz form connect code " + connectCode + " not found.");
        }

        QuizForm quizForm = null;
        for (QuizForm form : course.getQuizForms()) {
            if (form.getConnectCode().equals(connectCode)) {
                quizForm = form;
            }
        };

        if (quizForm == null) {
            throw new NotFoundException("Quiz form with connect code " + connectCode + " not found.");
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("courseId", course.getId().toHexString());
        jsonObject.put("formId", quizForm.getId().toHexString());
        return jsonObject.toString();
    }
}