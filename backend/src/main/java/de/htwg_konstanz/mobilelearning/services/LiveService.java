package de.htwg_konstanz.mobilelearning.services;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;

import de.htwg_konstanz.mobilelearning.enums.FormStatus;
import de.htwg_konstanz.mobilelearning.models.Course;
import de.htwg_konstanz.mobilelearning.models.Form;
import de.htwg_konstanz.mobilelearning.models.auth.UserRole;
import de.htwg_konstanz.mobilelearning.repositories.CourseRepository;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/live")
public class LiveService {

    @Inject
    private CourseRepository courseRepository;

    @Inject
    JsonWebToken jwt;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{courseId}")
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    public List<Form> getLiveForms() {

        // get the user
        String userId = jwt.getName();
        if (userId == null) {
            return new ArrayList<Form>();
        }

        List<Form> forms = new ArrayList<Form>();
        List<Course> courses = courseRepository.listAllForOwnerAndStudent(new ObjectId(userId));
        courses.forEach(course -> {
            course.feedbackForms.forEach(form -> {

                if (form.status == FormStatus.STARTED) {
                    forms.add(form);
                }

                forms.add(form.copyWithoutResults());
            });
            course.quizForms.forEach(form -> {

                if (form.status == FormStatus.STARTED) {
                    forms.add(form);
                }

                forms.add(form.copyWithoutResults());
            });
        });

        return forms;
    }
}
