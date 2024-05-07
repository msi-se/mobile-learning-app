package de.htwg_konstanz.mobilelearning.services.quiz;

import java.util.List;

import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestResponse;
import org.jose4j.jwt.consumer.InvalidJwtException;

import de.htwg_konstanz.mobilelearning.models.Course;
import de.htwg_konstanz.mobilelearning.models.auth.User;
import de.htwg_konstanz.mobilelearning.models.auth.UserRole;
import de.htwg_konstanz.mobilelearning.models.quiz.QuizForm;
import de.htwg_konstanz.mobilelearning.repositories.CourseRepository;
import de.htwg_konstanz.mobilelearning.repositories.UserRepository;
import de.htwg_konstanz.mobilelearning.services.auth.JwtService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Service used to manage quiz forms of a course.
 */
@Path("/course/{courseId}/quiz/form")
public class QuizFormService {
    
    @Inject CourseRepository courseRepository;

    @Inject
    JsonWebToken jwt;

    @Inject
    UserRepository userRepository;

    @Inject
    JwtService jwtService;

    /**
     * Returns all quiz forms of a course.
     * 
     * @param courseId
     * @return List of quiz forms
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    public List<QuizForm> getQuizForms(@RestPath String courseId) {
        ObjectId courseObjectId = new ObjectId(courseId);
        Course course = courseRepository.findById(courseObjectId);
        return course.getQuizForms();
    }

    /**
     * Returns a single quiz form of a course.
     * 
     * @param courseId
     * @param formId
     * @param results (optional query param - default: false)
     * @return Quiz form
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{formId}")
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    public QuizForm getQuizForm(@RestPath String courseId, @RestPath String formId, @QueryParam("results") @DefaultValue("false") Boolean results) {

        ObjectId courseObjectId = new ObjectId(courseId);
        ObjectId formObjectId = new ObjectId(formId);

        // fill the questionContent with the linked question
        Course course = courseRepository.findById(courseObjectId);
        QuizForm quizForm = course.getQuizFormById(formObjectId);
        if (results) {
            QuizForm quizFormWithQuestionContents = quizForm.copyWithQuestionContents(course);
            return quizFormWithQuestionContents;
        }
        
        QuizForm quizFormWithQuestionContents = quizForm.copyWithoutResultsButWithQuestionContents(course);
        return quizFormWithQuestionContents;
    }

    /*
     * Endpoint to participate in a quiz.
     * The user has to be registered with the user id and he has to provide an alias (String).
     * 
     * @param alias
     * @param courseId
     * @param formId
     * @return RestResponse
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.STUDENT, UserRole.PROF })
    @Path("/{formId}/participate")
    public RestResponse<String> participate(String alias, @RestPath String courseId, @RestPath String formId) {

        // get the user
        String userId = jwt.getSubject();

        // get the course and the quizForm
        ObjectId courseObjectId = new ObjectId(courseId);
        ObjectId formObjectId = new ObjectId(formId);
        Course course = courseRepository.findById(courseObjectId);
        if (course == null) { throw new NotFoundException("Course not found"); }
        QuizForm quizForm = course.getQuizFormById(formObjectId);
        if (quizForm == null) { throw new NotFoundException("QuizForm not found"); }

        // check if user is student of the course
        if (!course.isStudent(userId)) {
            return RestResponse.status(Response.Status.FORBIDDEN, "User is not student of the course");
        }

        // add the participant and check if the alias is already taken
        Boolean successfullyAdded = quizForm.addParticipant(new ObjectId(userId), alias);
        if (!successfullyAdded) {
            return RestResponse.status(Response.Status.CONFLICT, "Alias already taken");
        }
        courseRepository.update(course);

        return RestResponse.ok("Successfully added");
    }

    @Path("/{formId}/downloadresults")
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadResultsWithToken(@RestPath String courseId, @RestPath String formId, @QueryParam("token") String token) {

        User user = userRepository.findByUsername(jwt.getName());
        if (user == null) {
            try {
                user = userRepository.findById(new ObjectId(jwtService.getJwtClaims(token).getSubject()));
            } catch (InvalidJwtException e) {
            }
        }

        if (user == null) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        
        Course course = courseRepository.findById(new ObjectId(courseId));

        if (!course.isOwner(user.getId())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        QuizForm quizForm = course.getQuizFormById(new ObjectId(formId));
        return Response.ok(quizForm.getResultsAsCsv(course)).header("Content-Disposition", "attachment; filename=results_" + quizForm.name + ".csv").build();
    }


}
