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
import de.htwg_konstanz.mobilelearning.models.Question;
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

    static class UpdateCourseRequest {
        public String name;
        public String description;
        public String moodleCourseId;        
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    @Path("/course/{courseId}")
    public Course updateCourse(@RestPath String courseId, UpdateCourseRequest request) {
        Course course = courseRepository.findById(new ObjectId(courseId));
        if (!isOwner(course)) {
            throw new NotFoundException();
        }
        course.setName(request.name);
        course.setDescription(request.description);
        course.setMoodleCourseId(request.moodleCourseId);
        courseRepository.update(course);

        return getCourse(courseId);
    }

    static class AddCourseRequest {
        public String name;
        public String description;
        public String moodleCourseId;        
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    @Path("/course")
    public Course addCourse(AddCourseRequest request) {
        Course course = new Course(request.name, request.description);
        course.setMoodleCourseId(request.moodleCourseId);
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
    @Path("/course/{courseId}/feedback/form/{formId}")
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

    static class UpdateFeedbackFormRequest {
        public String name;
        public String description;
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    @Path("/course/{courseId}/feedback/form/{formId}")
    public FeedbackForm updateFeedbackForm(@RestPath String courseId, @RestPath String formId, UpdateFeedbackFormRequest request) {
        Course course = courseRepository.findById(new ObjectId(courseId));
        if (!isOwner(course)) {
            throw new NotFoundException();
        }
        FeedbackForm feedbackForm = course.getFeedbackFormById(new ObjectId(formId));
        if (feedbackForm == null) {
            throw new NotFoundException();
        }
        feedbackForm.setName(request.name);
        feedbackForm.setDescription(request.description);
        courseRepository.update(course);
        return getFeedbackForm(courseId, formId);
    }

    static class AddFeedbackFormRequest {
        public String name;
        public String description;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    @Path("/course/{courseId}/feedback/form")
    public FeedbackForm addFeedbackForm(@RestPath String courseId, AddFeedbackFormRequest request) {
        Course course = courseRepository.findById(new ObjectId(courseId));
        if (!isOwner(course)) {
            throw new NotFoundException();
        }
        FeedbackForm feedbackForm = new FeedbackForm(course.getId(), request.name, request.description, new ArrayList<>(), FormStatus.NOT_STARTED);
        course.addFeedbackForm(feedbackForm);
        courseRepository.update(course);
        return feedbackForm;
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    @Path("/course/{courseId}/feedback/form/{formId}")
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
    @Path("/course/{courseId}/feedback/form/{formId}/question/{questionId}")
    public QuestionWrapper getFeedbackQuestion(@RestPath String courseId, @RestPath String formId, @RestPath String questionId) {
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
        questionWrapper.setQuestionContent(feedbackQuestion);
        return questionWrapper;
    }

    static class UpdateFeedbackQuestionRequest {
        public String name;
        public String description;
        public String type;
        public List<String> options;
        public String rangeLow;
        public String rangeHigh;
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    @Path("/course/{courseId}/feedback/form/{formId}/question/{questionId}")
    public QuestionWrapper updateFeedbackQuestion(@RestPath String courseId, @RestPath String formId, @RestPath String questionId, UpdateFeedbackQuestionRequest request) {
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
        feedbackQuestion.setName(request.name);
        feedbackQuestion.setDescription(request.description);
        feedbackQuestion.setOptions(request.options);
        feedbackQuestion.setRangeLow(request.rangeLow);

        try {
            feedbackQuestion.setType(FeedbackQuestionType.valueOf(request.type));
        } catch (IllegalArgumentException e) {
            throw new NotFoundException();
        }

        courseRepository.update(course);
        return getFeedbackQuestion(courseId, formId, questionId);
    }

    static class AddFeedbackQuestionRequest {
        public String name;
        public String description;
        public String type;
        public List<String> options;
        public String rangeLow;
        public String rangeHigh;
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    @Path("/course/{courseId}/feedback/form/{formId}/question")
    public QuestionWrapper addFeedbackQuestion(@RestPath String courseId, @RestPath String formId, AddFeedbackQuestionRequest request) {
        Course course = courseRepository.findById(new ObjectId(courseId));
        if (!isOwner(course)) {
            throw new NotFoundException();
        }
        FeedbackForm feedbackForm = course.getFeedbackFormById(new ObjectId(formId));
        if (feedbackForm == null) {
            throw new NotFoundException();
        }
        
        try {
            FeedbackQuestionType questionType = FeedbackQuestionType.valueOf(request.type);
            FeedbackQuestion feedbackQuestion = new FeedbackQuestion(request.name, request.description, questionType, request.options, "", request.rangeLow, request.rangeHigh);
            course.addFeedbackQuestion(feedbackQuestion);
            QuestionWrapper questionWrapper = new QuestionWrapper(feedbackQuestion.getId(), new ArrayList<>());
            feedbackForm.addQuestion(questionWrapper);
            courseRepository.update(course);

            questionWrapper.setQuestionContent(feedbackQuestion);
            return questionWrapper;
        } catch (IllegalArgumentException e) {
            throw new NotFoundException();
        }
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    @Path("/course/{courseId}/feedback/form/{formId}/question/{questionId}")
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
