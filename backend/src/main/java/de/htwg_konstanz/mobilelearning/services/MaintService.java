package de.htwg_konstanz.mobilelearning.services;


import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.RestPath;

import de.htwg_konstanz.mobilelearning.enums.FeedbackQuestionType;
import de.htwg_konstanz.mobilelearning.enums.FormStatus;
import de.htwg_konstanz.mobilelearning.helper.moodle.MoodleCourse;
import de.htwg_konstanz.mobilelearning.helper.moodle.MoodleInterface;
import de.htwg_konstanz.mobilelearning.models.Course;
import de.htwg_konstanz.mobilelearning.models.QuestionWrapper;
import de.htwg_konstanz.mobilelearning.models.auth.User;
import de.htwg_konstanz.mobilelearning.models.auth.UserRole;
import de.htwg_konstanz.mobilelearning.models.feedback.FeedbackForm;
import de.htwg_konstanz.mobilelearning.models.feedback.FeedbackQuestion;
import de.htwg_konstanz.mobilelearning.models.quiz.QuizForm;
import de.htwg_konstanz.mobilelearning.repositories.CourseRepository;
import de.htwg_konstanz.mobilelearning.repositories.UserRepository;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Service used to manage courses via a web ui.
 * Only an owner of a course can manage it -> always has to be checked
 * 
 * 
 * // ENDPOINTS:

    // listCourses(); -> Name, Description

    // getCourse(params.courseId); -> Name, Description, MoodleCourseId, FeedbackForms (Name, Description), QuizForms (Name, Description)
    // updateCourse(params.courseId, courseName, courseDescription, courseMoodleCourseId); -> Error | Name, Description, MoodleCourseId, FeedbackForms (Name, Description), QuizForms (Name, Description)
    // addCourse(courseName, courseDescription); -> Error | Name, Description, MoodleCourseId, FeedbackForms (Name, Description), QuizForms (Name, Description)
    // deleteCourse(params.courseId); -> Error | Success

    // getFeedbackForm(params.courseId, params.formId); -> Error | Name, Description, Questions (Name, Description, Type, Options, RangeLow, RangeHigh)
    // updateFeedbackForm(params.courseId, params.formId, feedbackformName, feedbackformDescription) -> Error | Name, Description, Questions (Name, Description, Type, Options, RangeLow, RangeHigh)
    // addFeedbackForm(params.courseId, feedbackformName, feedbackformDescription) -> Error | Name, Description, Questions (Name, Description, Type, Options, RangeLow, RangeHigh)
    // deleteFeedbackForm(params.courseId, params.formId) -> Error | Success

    // getFeedbackQuestion(params.courseId, params.formId, params.questionId); -> Error | Name, Description, Type, Options, RangeLow, RangeHigh
    // updateFeedbackQuestion(params.courseId, params.formId, params.questionId, feedbackQuestion?.name, feedbackQuestion?.description, feedbackQuestion?.type, feedbackQuestion?.options, feedbackQuestion?.rangeLow, feedbackQuestion?.rangeHigh) -> Error | Name, Description, Type, Options, RangeLow, RangeHigh
    // addFeedbackQuestion(params.courseId, params.formId, feedbackQuestion?.name, feedbackQuestion?.description, feedbackQuestion?.type, feedbackQuestion?.options, feedbackQuestion?.rangeLow, feedbackQuestion?.rangeHigh) -> Error | Name, Description, Type, Options, RangeLow, RangeHigh
    // deleteFeedbackQuestion(params.courseId, params.formId, params.questionId) -> Error | Success

 */
@Path("/maint")
public class MaintService {


    @Inject
    private CourseRepository courseRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    JsonWebToken jwt;

    private Boolean isOwner(Course course) {
        User user = userRepository.findByUsername(jwt.getName());
        return course.isOwner(user);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    @Path("/courses")
    public List<Course> listCourses() {
        List<Course> courses = courseRepository.findAll().list().stream().filter(course -> isOwner(course)).toList();
        List<Course> coursesWithLessData = courses.stream().map(course -> {
            Course courseWithLessData = new Course();
            courseWithLessData.setId(course.getId());
            courseWithLessData.setName(course.getName());
            courseWithLessData.setDescription(course.getDescription());
            courseWithLessData.setMoodleCourseId(course.getMoodleCourseId());
            return courseWithLessData;
        }).toList();
        return coursesWithLessData;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    @Path("/course/{courseId}")
    public Course getCourse(@RestPath String courseId) {
        Course course = courseRepository.findById(new ObjectId(courseId));
        if (!isOwner(course)) {
            throw new NotFoundException();
        }
        Course courseWithLessData = new Course();
        courseWithLessData.setId(course.getId());
        courseWithLessData.setName(course.getName());
        courseWithLessData.setDescription(course.getDescription());
        courseWithLessData.setMoodleCourseId(course.getMoodleCourseId());
        courseWithLessData.setFeedbackForms(course.getFeedbackForms().stream().map(feedbackForm -> {
            FeedbackForm feedbackFormWithLessData = new FeedbackForm();
            feedbackFormWithLessData.setId(feedbackForm.getId());
            feedbackFormWithLessData.setName(feedbackForm.getName());
            feedbackFormWithLessData.setDescription(feedbackForm.getDescription());
            return feedbackFormWithLessData;
        }).toList());
        courseWithLessData.setQuizForms(course.getQuizForms().stream().map(quizForm -> {
            QuizForm quizFormWithLessData = new QuizForm();
            quizFormWithLessData.setId(quizForm.getId());
            quizFormWithLessData.setName(quizForm.getName());
            quizFormWithLessData.setDescription(quizForm.getDescription());
            return quizFormWithLessData;
        }).toList());
        return courseWithLessData;
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    @Path("/course/{courseId}")
    public Course updateCourse(@RestPath String courseId, String courseName, String courseDescription, String courseMoodleCourseId) {
        Course course = courseRepository.findById(new ObjectId(courseId));
        if (!isOwner(course)) {
            throw new NotFoundException();
        }
        course.setName(courseName);
        course.setDescription(courseDescription);
        course.setMoodleCourseId(courseMoodleCourseId);
        courseRepository.update(course);

        return getCourse(courseId);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    @Path("/course")
    public Course addCourse(String courseName, String courseDescription) {
        Course course = new Course(courseName, courseDescription);
        course.addOwner(userRepository.findByUsername(jwt.getName()).getId());
        courseRepository.persist(course);
        return course;
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    @Path("/course/{courseId}")
    public Response deleteCourse(@RestPath String courseId) {
        Course course = courseRepository.findById(new ObjectId(courseId));
        if (!isOwner(course)) {
            throw new NotFoundException();
        }
        courseRepository.delete(course);
        return Response.ok().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    @Path("/course/{courseId}/feedbackform/{formId}")
    public FeedbackForm getFeedbackForm(@RestPath String courseId, @RestPath String formId) {
        Course course = courseRepository.findById(new ObjectId(courseId));
        if (!isOwner(course)) {
            throw new NotFoundException();
        }
        FeedbackForm feedbackForm = course.getFeedbackFormById(new ObjectId(formId));
        if (feedbackForm == null) {
            throw new NotFoundException();
        }
        FeedbackForm feedbackFormWithQuestionContents =feedbackForm.copyWithQuestionContents(course);
        return feedbackFormWithQuestionContents;
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    @Path("/course/{courseId}/feedbackform/{formId}")
    public FeedbackForm updateFeedbackForm(@RestPath String courseId, @RestPath String formId, String feedbackformName, String feedbackformDescription) {
        Course course = courseRepository.findById(new ObjectId(courseId));
        if (!isOwner(course)) {
            throw new NotFoundException();
        }
        FeedbackForm feedbackForm = course.getFeedbackFormById(new ObjectId(formId));
        if (feedbackForm == null) {
            throw new NotFoundException();
        }
        feedbackForm.setName(feedbackformName);
        feedbackForm.setDescription(feedbackformDescription);
        courseRepository.update(course);
        return getFeedbackForm(courseId, formId);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    @Path("/course/{courseId}/feedbackform")
    public FeedbackForm addFeedbackForm(@RestPath String courseId, String feedbackformName, String feedbackformDescription) {
        Course course = courseRepository.findById(new ObjectId(courseId));
        if (!isOwner(course)) {
            throw new NotFoundException();
        }
        FeedbackForm feedbackForm = new FeedbackForm(course.getId(), feedbackformName, feedbackformDescription, new ArrayList<>(), FormStatus.NOT_STARTED);
        course.addFeedbackForm(feedbackForm);
        courseRepository.update(course);
        return feedbackForm;
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    @Path("/course/{courseId}/feedbackform/{formId}")
    public Response deleteFeedbackForm(@RestPath String courseId, @RestPath String formId) {
        Course course = courseRepository.findById(new ObjectId(courseId));
        if (!isOwner(course)) {
            throw new NotFoundException();
        }
        FeedbackForm feedbackForm = course.getFeedbackFormById(new ObjectId(formId));
        if (feedbackForm == null) {
            throw new NotFoundException();
        }
        course.removeFeedbackForm(feedbackForm);
        courseRepository.update(course);
        return Response.ok().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    @Path("/course/{courseId}/feedbackform/{formId}/question/{questionId}")
    public FeedbackQuestion getFeedbackQuestion(@RestPath String courseId, @RestPath String formId, @RestPath String questionId) {
        Course course = courseRepository.findById(new ObjectId(courseId));
        if (!isOwner(course)) {
            throw new NotFoundException();
        }
        FeedbackForm feedbackForm = course.getFeedbackFormById(new ObjectId(formId));
        if (feedbackForm == null) {
            throw new NotFoundException();
        }
        QuestionWrapper questionWrapper = feedbackForm.getQuestionById(new ObjectId(questionId));
        if (questionWrapper == null) {
            throw new NotFoundException();
        }
        FeedbackQuestion feedbackQuestion = course.getFeedbackQuestionById(questionWrapper.getQuestionId());
        return feedbackQuestion;
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    @Path("/course/{courseId}/feedbackform/{formId}/question/{questionId}")
    public FeedbackQuestion updateFeedbackQuestion(@RestPath String courseId, @RestPath String formId, @RestPath String questionId, String feedbackQuestionName, String feedbackQuestionDescription, String feedbackQuestionType, List<String> feedbackQuestionOptions, String feedbackQuestionRangeLow, String feedbackQuestionRangeHigh) {
        Course course = courseRepository.findById(new ObjectId(courseId));
        if (!isOwner(course)) {
            throw new NotFoundException();
        }
        FeedbackForm feedbackForm = course.getFeedbackFormById(new ObjectId(formId));
        if (feedbackForm == null) {
            throw new NotFoundException();
        }
        QuestionWrapper questionWrapper = feedbackForm.getQuestionById(new ObjectId(questionId));
        if (questionWrapper == null) {
            throw new NotFoundException();
        }
        FeedbackQuestion feedbackQuestion = course.getFeedbackQuestionById(questionWrapper.getQuestionId());
        feedbackQuestion.setName(feedbackQuestionName);
        feedbackQuestion.setDescription(feedbackQuestionDescription);
        feedbackQuestion.setOptions(feedbackQuestionOptions);
        feedbackQuestion.setRangeLow(feedbackQuestionRangeLow);

        try {
            feedbackQuestion.setType(FeedbackQuestionType.valueOf(feedbackQuestionType));
        } catch (IllegalArgumentException e) {
            throw new NotFoundException();
        }

        courseRepository.update(course);
        return getFeedbackQuestion(courseId, formId, questionId);
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    @Path("/course/{courseId}/feedbackform/{formId}/question")
    public FeedbackQuestion addFeedbackQuestion(@RestPath String courseId, @RestPath String formId, String feedbackQuestionName, String feedbackQuestionDescription, String feedbackQuestionType, List<String> feedbackQuestionOptions, String feedbackQuestionRangeLow, String feedbackQuestionRangeHigh) {
        Course course = courseRepository.findById(new ObjectId(courseId));
        if (!isOwner(course)) {
            throw new NotFoundException();
        }
        FeedbackForm feedbackForm = course.getFeedbackFormById(new ObjectId(formId));
        if (feedbackForm == null) {
            throw new NotFoundException();
        }
        
        try {
            FeedbackQuestionType type = FeedbackQuestionType.valueOf(feedbackQuestionType);
            // public FeedbackQuestion(String name, String description, FeedbackQuestionType type, List<String> options, String key, String rangeLow, String rangeHigh) {
            FeedbackQuestion feedbackQuestion = new FeedbackQuestion(feedbackQuestionName, feedbackQuestionDescription, type, feedbackQuestionOptions, "", feedbackQuestionRangeLow, feedbackQuestionRangeHigh);
            course.addFeedbackQuestion(feedbackQuestion);
            QuestionWrapper questionWrapper = new QuestionWrapper(feedbackQuestion.getId(), new ArrayList<>());
            feedbackForm.addQuestion(questionWrapper);
            courseRepository.update(course);
            return feedbackQuestion;
        } catch (IllegalArgumentException e) {
            throw new NotFoundException();
        }
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    @Path("/course/{courseId}/feedbackform/{formId}/question/{questionId}")
    public Response deleteFeedbackQuestion(@RestPath String courseId, @RestPath String formId, @RestPath String questionId) {
        Course course = courseRepository.findById(new ObjectId(courseId));
        if (!isOwner(course)) {
            throw new NotFoundException();
        }
        FeedbackForm feedbackForm = course.getFeedbackFormById(new ObjectId(formId));
        if (feedbackForm == null) {
            throw new NotFoundException();
        }
        QuestionWrapper questionWrapper = feedbackForm.getQuestionById(new ObjectId(questionId));
        if (questionWrapper == null) {
            throw new NotFoundException();
        }
        FeedbackQuestion feedbackQuestion = course.getFeedbackQuestionById(questionWrapper.getQuestionId());
        if (feedbackQuestion == null) {
            throw new NotFoundException();
        }
        course.removeFeedbackQuestion(feedbackQuestion);
        feedbackForm.removeQuestion(questionWrapper);
        courseRepository.update(course);
        return Response.ok().build();
    }
}
