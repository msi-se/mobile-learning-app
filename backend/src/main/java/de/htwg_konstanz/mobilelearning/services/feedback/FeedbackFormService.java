package de.htwg_konstanz.mobilelearning.services.feedback;

import java.util.List;

import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestResponse;

import de.htwg_konstanz.mobilelearning.enums.FormStatus;
import de.htwg_konstanz.mobilelearning.models.Course;
import de.htwg_konstanz.mobilelearning.models.auth.UserRole;
import de.htwg_konstanz.mobilelearning.models.feedback.FeedbackForm;
import de.htwg_konstanz.mobilelearning.repositories.CourseRepository;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
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
     * Updates a feedback form.
     * User has to be owner to update the form.
     * 
     * @param courseId
     * @param formId
     * @param feedbackForm
     * @return Updated feedback form
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{formId}")
    @RolesAllowed({ UserRole.PROF })
    public FeedbackForm updateFeedbackForm(@RestPath String courseId, @RestPath String formId, FeedbackForm feedbackForm) {
        ObjectId courseObjectId = new ObjectId(courseId);
        ObjectId formObjectId = new ObjectId(formId);
        Course course = courseRepository.findById(courseObjectId);
        FeedbackForm feedbackFormToUpdate = course.getFeedbackFormById(formObjectId);
        
        if (feedbackFormToUpdate == null) {
            throw new NotFoundException("Feedbackcourse not found");
        }
        if(!course.isOwner(jwt.getSubject())){
            return null;
        }
        if (feedbackForm.description != null) {
            feedbackFormToUpdate.description = feedbackForm.description;
        }
        if (feedbackForm.name != null) {
            feedbackFormToUpdate.name = feedbackForm.name;
        }
        if (feedbackForm.questions != null) {
            feedbackFormToUpdate.questions = feedbackForm.questions;
        }
        if (feedbackForm.connectCode != null) {
            feedbackFormToUpdate.connectCode = feedbackForm.connectCode;
        }
        if (feedbackForm.status != null) {
            feedbackFormToUpdate.status = feedbackForm.status;
        }

        courseRepository.update(course);
        return feedbackFormToUpdate;
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

}
