package de.htwg_konstanz.mobilelearning.services;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;

import de.htwg_konstanz.mobilelearning.enums.FormStatus;
import de.htwg_konstanz.mobilelearning.models.Course;
import de.htwg_konstanz.mobilelearning.models.Form;
import de.htwg_konstanz.mobilelearning.models.auth.User;
import de.htwg_konstanz.mobilelearning.models.auth.UserRole;
import de.htwg_konstanz.mobilelearning.repositories.CourseRepository;
import de.htwg_konstanz.mobilelearning.repositories.UserRepository;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/live")
public class LiveService {

    @Inject
    private CourseRepository courseRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private CourseService courseService;

    @Inject
    JsonWebToken jwt;

    /**
     * Returns all live forms of a user.
     * 
     * @return List of live forms
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("")
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    public List<Form> getLiveForms(@QueryParam("password") String password) {
        
        // update the courses linked to the user
        User user = userRepository.findByUsername(jwt.getName());
        if (user == null) {
            throw new NotFoundException("User not found.");
        }
        courseService.updateCourseLinkedToUser(user, password);

        // collect all forms which are started
        List<Form> forms = new ArrayList<Form>();
        List<Course> courses = courseRepository.listAllForOwnerAndStudent(user);
        for (Course course : courses) {
            for (Form form : course.getFeedbackForms()) {
                if (form.getStatus() == FormStatus.STARTED) {
                    forms.add(form);
                }
            }
            for (Form form : course.getQuizForms()) {
                if (form.getStatus() == FormStatus.STARTED) {
                    forms.add(form);
                }
            }
        }
        return forms;
    }

    /**
     * Returns all live feedback forms of a user.
     * 
     * @return List of live feedback forms
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("feedback")
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    public List<Form> getLiveFeedbackForms(@QueryParam("password") String password) {
        
        // update the courses linked to the user
        User user = userRepository.findByUsername(jwt.getName());
        if (user == null) {
            throw new NotFoundException("User not found.");
        }
        courseService.updateCourseLinkedToUser(user, password);

        // collect all forms which are started
        List<Form> forms = new ArrayList<Form>();
        List<Course> courses = courseRepository.listAllForOwnerAndStudent(user);
        for (Course course : courses) {
            for (Form form : course.getFeedbackForms()) {
                if (form.getStatus() == FormStatus.STARTED) {
                    forms.add(form);
                }
            }
        }
        return forms;
    }

    /**
     * Returns all live quiz forms of a user.
     * 
     * @return List of live quiz forms
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("quiz")
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    public List<Form> getLiveQuizForms(@QueryParam("password") String password) {
        
        // update the courses linked to the user
        User user = userRepository.findByUsername(jwt.getName());
        if (user == null) {
            throw new NotFoundException("User not found.");
        }
        courseService.updateCourseLinkedToUser(user, password);

        // collect all forms which are started
        List<Form> forms = new ArrayList<Form>();
        List<Course> courses = courseRepository.listAllForOwnerAndStudent(user);
        for (Course course : courses) {
            for (Form form : course.getQuizForms()) {
                if (form.getStatus() == FormStatus.STARTED) {
                    forms.add(form);
                }
            }
        }
        return forms;
    }

    
}
