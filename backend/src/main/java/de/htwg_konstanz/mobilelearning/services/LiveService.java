package de.htwg_konstanz.mobilelearning.services;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.jwt.JsonWebToken;

import de.htwg_konstanz.mobilelearning.enums.FormStatus;
import de.htwg_konstanz.mobilelearning.models.Course;
import de.htwg_konstanz.mobilelearning.models.Form;
import de.htwg_konstanz.mobilelearning.models.FormShell;
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
    public List<FormShell> getLiveForms(@QueryParam("password") String password) {
        
        // update the courses linked to the user
        User user = userRepository.findByUsername(jwt.getName());
        if (user == null) {
            throw new NotFoundException("User not found.");
        }
        courseService.updateCourseLinkedToUser(user, password);

        // collect all forms which are started
        List<FormShell> forms = new ArrayList<FormShell>();
        List<Course> courses = courseRepository.listAllForOwnerAndStudent(user);
        for (Course course : courses) {
            for (Form form : course.getFeedbackForms()) {
                if (form.getStatus() == FormStatus.STARTED || form.getStatus() == FormStatus.WAITING) {
                    // convert the forms to form shells to not have all the data in them (e.g. the questions)
                    forms.add(new FormShell(form.getId(), form.getCourseId(), form.getName(), form.getDescription(), form.getStatus(), form.getConnectCode(), form.getKey(), "feedback", course.getName()));
                }
            }
            for (Form form : course.getQuizForms()) {
                if (form.getStatus() == FormStatus.STARTED || form.getStatus() == FormStatus.WAITING) {
                    forms.add(new FormShell(form.getId(), form.getCourseId(), form.getName(), form.getDescription(), form.getStatus(), form.getConnectCode(), form.getKey(), "quiz", course.getName()));
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
