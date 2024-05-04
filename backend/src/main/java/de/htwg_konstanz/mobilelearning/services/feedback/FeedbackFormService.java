package de.htwg_konstanz.mobilelearning.services.feedback;

import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestResponse;
import org.jose4j.jwt.consumer.InvalidJwtException;

import de.htwg_konstanz.mobilelearning.enums.FormStatus;
import de.htwg_konstanz.mobilelearning.models.Course;
import de.htwg_konstanz.mobilelearning.models.QuestionWrapper;
import de.htwg_konstanz.mobilelearning.models.auth.User;
import de.htwg_konstanz.mobilelearning.models.auth.UserRole;
import de.htwg_konstanz.mobilelearning.models.feedback.FeedbackForm;
import de.htwg_konstanz.mobilelearning.models.feedback.FeedbackQuestion;
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
 * Service used to manage feedback forms of a course.
 */
@Path("/course/{courseId}/feedback/form")
public class FeedbackFormService {

    @Inject
    private CourseRepository courseRepository;

    @Inject
    private JsonWebToken jwt;

    @Inject
    private JwtService jwtService;

    @Inject
    private UserRepository userRepository;

    /**
     * Returns a single feedback form of a course.
     * 
     * @param courseId
     * @param formId
     * @param results (optional query param - default: false)
     * @return Feedback form
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{formId}")
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    public FeedbackForm getFeedbackForm(@RestPath String courseId, @RestPath String formId, @QueryParam("results") @DefaultValue("false") Boolean results) {

        ObjectId courseObjectId = new ObjectId(courseId);
        ObjectId formObjectId = new ObjectId(formId);

        // fill the questionContent with the linked question
        Course course = courseRepository.findById(courseObjectId);
        FeedbackForm feedbackForm = course.getFeedbackFormById(formObjectId);

        if (results) {
            FeedbackForm feedbackFormWithQuestionContents = feedbackForm.copyWithQuestionContents(course);
            return feedbackFormWithQuestionContents;
        }

        FeedbackForm feedbackFormWithQuestionContents = feedbackForm.copyWithoutResultsButWithQuestionContents(course);
        return feedbackFormWithQuestionContents;
    }

    /**
     * Creates a new feedback form.
     * 
     * @param courseId
     * @param feedbackForm
     * @return Created feedback form
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("")
    @RolesAllowed({ UserRole.PROF })
    public FeedbackForm createFeedbackForm(@RestPath String courseId, FeedbackForm feedbackForm) {
        
        // TODO: add validation
        ObjectId courseObjectId = new ObjectId(courseId);
        Course course = courseRepository.findById(courseObjectId);
        
        FeedbackForm newFeedbackForm = new FeedbackForm(
            course.getId(),
            feedbackForm.getName(),
            feedbackForm.getDescription(),
            feedbackForm.getQuestions(),
            FormStatus.NOT_STARTED
        );

        course.addFeedbackForm(newFeedbackForm);
        courseRepository.update(course);

        return feedbackForm;
    }

    /**
     * Clears the results of a feedback form.
     * User has to be owner to clear the results.
     * 
     * @param courseId
     * @param formId
     * @return Cleared feedback form
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{formId}/clearresults")
    @RolesAllowed({ UserRole.PROF })
    public FeedbackForm clearFeedbackFormResults(@RestPath String courseId, @RestPath String formId) {
        ObjectId courseObjectId = new ObjectId(courseId);
        ObjectId formObjectId = new ObjectId(formId);
        Course course = courseRepository.findById(courseObjectId);
        FeedbackForm feedbackForm = course.getFeedbackFormById(formObjectId);

        if (feedbackForm == null) {
            throw new NotFoundException("Feedbackcourse not found");
        }
        if (!course.isOwner(jwt.getSubject())) {
            System.out.println("User is not owner of course");
            return null;
        }
        feedbackForm.clearResults();
        courseRepository.update(course);
        return feedbackForm;
    }

    /*
     * Endpoint to participate in a feedback.
     * The user has to be registered with the user id.
     * NOT NEEDED CURRENTLY (student gets registered directly in the socket service when he connects to the feedback form)
     * 
     * @param courseId
     * @param formId
     * @return RestResponse
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.STUDENT, UserRole.PROF })
    @Path("/{formId}/participate")
    public RestResponse<String> participate(@RestPath String courseId, @RestPath String formId) {

        // get the user
        String userId = jwt.getSubject();

        // get the course and the feedbackForm
        ObjectId courseObjectId = new ObjectId(courseId);
        ObjectId formObjectId = new ObjectId(formId);
        Course course = courseRepository.findById(courseObjectId);
        if (course == null) { throw new NotFoundException("Course not found"); }
        FeedbackForm feedbackForm = course.getFeedbackFormById(formObjectId);
        if (feedbackForm == null) { throw new NotFoundException("FeedbackForm not found"); }
        
        // check if user is student of the course
        if (!course.isStudent(userId)) {
            return RestResponse.status(Response.Status.FORBIDDEN, "User is not student of the course");
        }

        // add the participant
        Boolean successfullyAdded = feedbackForm.addParticipant(new ObjectId(userId));
        if (!successfullyAdded) {
            return RestResponse.status(Response.Status.CONFLICT, "Error while adding participant (should not happen)");
        }
        courseRepository.update(course);

        return RestResponse.ok("Successfully added");
    }

    // @Path("/{formId}/downloadresults")
    // @GET
    // @Produces(MediaType.APPLICATION_OCTET_STREAM)
    // public Response downloadResults(@RestPath String courseId, @RestPath String formId) {
    //     Course course = courseRepository.findById(new ObjectId(courseId));
    //     FeedbackForm feedbackForm = course.getFeedbackFormById(new ObjectId(formId));
    //     return Response.ok(feedbackForm.getResultsAsCsv(course)).header("Content-Disposition", "attachment; filename=results_" + feedbackForm.name + ".csv").build();
    // }
    
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

        FeedbackForm feedbackForm = course.getFeedbackFormById(new ObjectId(formId));
        return Response.ok(feedbackForm.getResultsAsCsv(course)).header("Content-Disposition", "attachment; filename=results_" + feedbackForm.name + ".csv").build();
    }

    @Path("/{formId}/question/{questionId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    public Response getQuestion(@RestPath String courseId, @RestPath String formId, @RestPath String questionId) {
        Course course = courseRepository.findById(new ObjectId(courseId));
        FeedbackForm feedbackForm = course.getFeedbackFormById(new ObjectId(formId));
        QuestionWrapper question = feedbackForm.getQuestionById(new ObjectId(questionId));
        FeedbackQuestion feedbackQuestion = course.getFeedbackQuestionById(question.getQuestionId());
        return Response.ok(feedbackQuestion).build();
    }

}
