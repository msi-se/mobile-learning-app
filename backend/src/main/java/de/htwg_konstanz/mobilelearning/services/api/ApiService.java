package de.htwg_konstanz.mobilelearning.services.api;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import de.htwg_konstanz.mobilelearning.enums.FeedbackQuestionType;
import de.htwg_konstanz.mobilelearning.enums.FormStatus;
import de.htwg_konstanz.mobilelearning.models.Course;
import de.htwg_konstanz.mobilelearning.models.QuestionWrapper;
import de.htwg_konstanz.mobilelearning.models.auth.User;
import de.htwg_konstanz.mobilelearning.models.feedback.FeedbackForm;
import de.htwg_konstanz.mobilelearning.models.feedback.FeedbackQuestion;
import de.htwg_konstanz.mobilelearning.repositories.CourseRepository;
import de.htwg_konstanz.mobilelearning.repositories.UserRepository;
import de.htwg_konstanz.mobilelearning.services.api.models.ApiCourse;
import de.htwg_konstanz.mobilelearning.services.api.models.ApiFeedbackForm;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

@Path("/public")
public class ApiService {

    @Inject
    private CourseRepository courseRepository;

    @Inject
    private UserRepository userRepository;

    @POST
    @Path("/course")
    @Produces(MediaType.APPLICATION_JSON)
    public Course createCourse(ApiCourse apiCourse, @Context SecurityContext ctx) {

        User user = userRepository.findByUsername(ctx.getUserPrincipal().getName());
        if (user == null) {
            throw new NotFoundException("User not found.");
        }

        // validate input
        if (apiCourse.name == null || apiCourse.name.isEmpty()) {
            throw new IllegalArgumentException("Course name must not be empty.");
        }

        if (apiCourse.description == null || apiCourse.description.isEmpty()) {
            throw new IllegalArgumentException("Course description must not be empty.");
        }

        // create course with current user as owner
        Course course = new Course(apiCourse.name, apiCourse.description);
        course.addOwner(user.getId());
        courseRepository.persist(course);

        return course;
    }

    @POST
    @Path("/course/feedback/form")
    public FeedbackForm createFeedbackForm(ApiFeedbackForm apiFeedbackForm, @Context SecurityContext ctx) {

        User user = userRepository.findByUsername(ctx.getUserPrincipal().getName());
        if (user == null) {
            throw new NotFoundException("User not found.");
        }

        // validate input
        if (apiFeedbackForm.name == null || apiFeedbackForm.name.isEmpty()) {
            throw new IllegalArgumentException("Feedback form name must not be empty.");
        }

        if (apiFeedbackForm.description == null || apiFeedbackForm.description.isEmpty()) {
            throw new IllegalArgumentException("Feedback form description must not be empty.");
        }

        if (apiFeedbackForm.questions == null || apiFeedbackForm.questions.isEmpty()) {
            throw new IllegalArgumentException("Feedback form must have at least one question.");
        }

        if (apiFeedbackForm.courseId == null || apiFeedbackForm.courseId.isEmpty()) {
            throw new IllegalArgumentException("Feedback form must be assigned to a course.");
        }

        // get the course
        ObjectId courseId = null;
        try {
            courseId = new ObjectId(apiFeedbackForm.courseId);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid course id.");
        }

        Course course = courseRepository.findById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("Course not found.");
        }

        // check if user is owner of the course
        if (!course.getOwners().contains(user.getId())) {
            throw new IllegalArgumentException("User is not owner of the course.");
        }

        // create feedback questions
        List<ObjectId> feedbackQuestionIds = new ArrayList<>();
        for (ApiFeedbackForm.ApiFeedbackQuestion apiFeedbackQuestion : apiFeedbackForm.questions) {
            if (apiFeedbackQuestion.name == null || apiFeedbackQuestion.name.isEmpty()) {
                throw new IllegalArgumentException("Feedback question name must not be empty.");
            }

            if (apiFeedbackQuestion.description == null || apiFeedbackQuestion.description.isEmpty()) {
                throw new IllegalArgumentException("Feedback question description must not be empty.");
            }

            if (apiFeedbackQuestion.type == null || apiFeedbackQuestion.type.isEmpty()) {
                throw new IllegalArgumentException("Feedback question type must not be empty.");
            }

            // check if type is valid (in enum)
            try {
                FeedbackQuestionType.valueOf(apiFeedbackQuestion.type);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid feedback question type.");
            }

            // if it is a single choice question, there must be options
            if (apiFeedbackQuestion.type.equals(FeedbackQuestionType.SINGLE_CHOICE.toString()) && apiFeedbackQuestion.options.size() < 2) {
                throw new IllegalArgumentException("Single choice feedback question must have at least two options.");
            }

            // check if the same question already exists (if so just add the id to the list and continue)
            Boolean questionExists = false;
            for (FeedbackQuestion feedbackQuestion : course.getFeedbackQuestions()) {
                if (feedbackQuestion.getName().equals(apiFeedbackQuestion.name) && feedbackQuestion.getDescription().equals(apiFeedbackQuestion.description)) {
                    feedbackQuestionIds.add(feedbackQuestion.getId());
                    questionExists = true;
                    break;
                }
            }
            if (questionExists) {
                continue;
            }

            // create feedback question
            FeedbackQuestion feedbackQuestion = new FeedbackQuestion(
                apiFeedbackQuestion.name,
                apiFeedbackQuestion.description,
                FeedbackQuestionType.valueOf(apiFeedbackQuestion.type),
                apiFeedbackQuestion.options
            );

            course.addFeedbackQuestion(feedbackQuestion);
            feedbackQuestionIds.add(feedbackQuestion.getId());
        }

        // create question wrappers
        List<QuestionWrapper> questionWrappers = new ArrayList<>();
        for (ObjectId feedbackQuestionId : feedbackQuestionIds) {
            questionWrappers.add(new QuestionWrapper(feedbackQuestionId, null));
        }

        // add feedback form to course
        FeedbackForm feedbackForm = new FeedbackForm(
            courseId,
            apiFeedbackForm.name,
            apiFeedbackForm.description,
            questionWrappers,
            FormStatus.NOT_STARTED
        );

        
        // save form and course
        course.addFeedbackForm(feedbackForm);
        courseRepository.update(course);

        return feedbackForm;
    }
}
