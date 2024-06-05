package de.htwg_konstanz.mobilelearning.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.RestPath;

import de.htwg_konstanz.mobilelearning.enums.FeedbackQuestionType;
import de.htwg_konstanz.mobilelearning.enums.FormStatus;
import de.htwg_konstanz.mobilelearning.enums.QuizQuestionType;
import de.htwg_konstanz.mobilelearning.helper.moodle.MoodleCourse;
import de.htwg_konstanz.mobilelearning.helper.moodle.MoodleInterface;
import de.htwg_konstanz.mobilelearning.models.Course;
import de.htwg_konstanz.mobilelearning.models.Form;
import de.htwg_konstanz.mobilelearning.models.Question;
import de.htwg_konstanz.mobilelearning.models.QuestionWrapper;
import de.htwg_konstanz.mobilelearning.models.Result;
import de.htwg_konstanz.mobilelearning.models.auth.User;
import de.htwg_konstanz.mobilelearning.models.auth.UserRole;
import de.htwg_konstanz.mobilelearning.models.feedback.FeedbackForm;
import de.htwg_konstanz.mobilelearning.models.feedback.FeedbackQuestion;
import de.htwg_konstanz.mobilelearning.models.quiz.QuizForm;
import de.htwg_konstanz.mobilelearning.models.quiz.QuizQuestion;
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
 * 
 * // listCourses(); -> Name, Description
 * 
 * // getCourse(params.courseId); -> Name, Description, MoodleCourseId,
 * FeedbackForms (Name, Description), QuizForms (Name, Description)
 * // updateCourse(params.courseId, courseName, courseDescription,
 * courseMoodleCourseId); -> Error | Name, Description, MoodleCourseId,
 * FeedbackForms (Name, Description), QuizForms (Name, Description)
 * // addCourse(courseName, courseDescription); -> Error | Name, Description,
 * MoodleCourseId, FeedbackForms (Name, Description), QuizForms (Name,
 * Description)
 * // deleteCourse(params.courseId); -> Error | Success
 * 
 * // getFeedbackForm(params.courseId, params.formId); -> Error | Name,
 * Description, Questions (Name, Description, Type, Options, RangeLow,
 * RangeHigh)
 * // updateFeedbackForm(params.courseId, params.formId, feedbackformName,
 * feedbackformDescription) -> Error | Name, Description, Questions (Name,
 * Description, Type, Options, RangeLow, RangeHigh)
 * // addFeedbackForm(params.courseId, feedbackformName,
 * feedbackformDescription) -> Error | Name, Description, Questions (Name,
 * Description, Type, Options, RangeLow, RangeHigh)
 * // deleteFeedbackForm(params.courseId, params.formId) -> Error | Success
 * 
 * // getFeedbackQuestion(params.courseId, params.formId, params.questionId); ->
 * Error | Name, Description, Type, Options, RangeLow, RangeHigh
 * // updateFeedbackQuestion(params.courseId, params.formId, params.questionId,
 * feedbackQuestion?.name, feedbackQuestion?.description,
 * feedbackQuestion?.type, feedbackQuestion?.options,
 * feedbackQuestion?.rangeLow, feedbackQuestion?.rangeHigh) -> Error | Name,
 * Description, Type, Options, RangeLow, RangeHigh
 * // addFeedbackQuestion(params.courseId, params.formId,
 * feedbackQuestion?.name, feedbackQuestion?.description,
 * feedbackQuestion?.type, feedbackQuestion?.options,
 * feedbackQuestion?.rangeLow, feedbackQuestion?.rangeHigh) -> Error | Name,
 * Description, Type, Options, RangeLow, RangeHigh
 * // deleteFeedbackQuestion(params.courseId, params.formId, params.questionId)
 * -> Error | Success
 * 
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
        List<Course> courses = courseRepository
                .findAll()
                .list()
                .stream()
                .filter(course -> isOwner(course))
                .sorted((c1, c2) -> -c1.getLastModified().compareTo(c2.getLastModified()))
                .toList();
        List<Course> coursesWithLessData = courses.stream().map(course -> {
            Course courseWithLessData = new Course();
            courseWithLessData.setId(course.getId());
            courseWithLessData.setName(course.getName());
            courseWithLessData.setDescription(course.getDescription());
            courseWithLessData.setMoodleCourseId(course.getMoodleCourseId());
            courseWithLessData.setLastModified(course.getLastModified());
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
        courseWithLessData.setLastModified(course.getLastModified());
        courseWithLessData.setFeedbackForms(course.getFeedbackForms().stream().map(feedbackForm -> {
            FeedbackForm feedbackFormWithLessData = new FeedbackForm();
            feedbackFormWithLessData.setId(feedbackForm.getId());
            feedbackFormWithLessData.setName(feedbackForm.getName());
            feedbackFormWithLessData.setDescription(feedbackForm.getDescription());
            feedbackFormWithLessData.setStatus(feedbackForm.getStatus());
            feedbackFormWithLessData.setLastModified(feedbackForm.getLastModified());
            return feedbackFormWithLessData;
        }).sorted((f1, f2) -> -f1.getLastModified().compareTo(f2.getLastModified())).toList());
        courseWithLessData.setQuizForms(course.getQuizForms().stream().map(quizForm -> {
            QuizForm quizFormWithLessData = new QuizForm();
            quizFormWithLessData.setId(quizForm.getId());
            quizFormWithLessData.setName(quizForm.getName());
            quizFormWithLessData.setDescription(quizForm.getDescription());
            quizFormWithLessData.setStatus(quizForm.getStatus());
            quizFormWithLessData.setLastModified(quizForm.getLastModified());
            return quizFormWithLessData;
        }).sorted((f1, f2) -> -f1.getLastModified().compareTo(f2.getLastModified())).toList());
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
        course.wasUpdated();
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
        FeedbackForm feedbackFormWithQuestionContents = feedbackForm.copyWithQuestionContents(course);
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
    public FeedbackForm updateFeedbackForm(@RestPath String courseId, @RestPath String formId,
            UpdateFeedbackFormRequest request) {
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
        feedbackForm.wasUpdated();
        course.wasUpdated();
        courseRepository.update(course);
        return getFeedbackForm(courseId, formId);
    }

    // PUT /maint/course/${courseId}/feedback/form/${formId}/reorder ({String[]
    // questionIds})
    // reorderFeedbackFormQuestions(params.courseId, params.formId, questionIds) ->
    // Error | Name, Description, Questions (Name, Description, Type, Options,
    // RangeLow, RangeHigh)
    static class ReorderFeedbackFormQuestionsRequest {
        public List<String> questionIds;
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    @Path("/course/{courseId}/feedback/form/{formId}/reorder")
    public FeedbackForm reorderFeedbackFormQuestions(@RestPath String courseId, @RestPath String formId,
            ReorderFeedbackFormQuestionsRequest request) {
        Course course = courseRepository.findById(new ObjectId(courseId));
        if (!isOwner(course)) {
            throw new NotFoundException();
        }
        FeedbackForm feedbackForm = course.getFeedbackFormById(new ObjectId(formId));
        if (feedbackForm == null) {
            throw new NotFoundException();
        }
        List<QuestionWrapper> reorderedQuestions = new ArrayList<>();
        for (String questionId : request.questionIds) {
            QuestionWrapper questionWrapper = feedbackForm.getQuestionById(new ObjectId(questionId));
            if (questionWrapper == null) {
                throw new NotFoundException();
            }
            reorderedQuestions.add(questionWrapper);
        }
        feedbackForm.setQuestions(reorderedQuestions);
        feedbackForm.wasUpdated();
        course.wasUpdated();
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
        FeedbackForm feedbackForm = new FeedbackForm(course.getId(), request.name, request.description,
                new ArrayList<>(), FormStatus.NOT_STARTED);
        course.addFeedbackForm(feedbackForm);
        course.wasUpdated();
        courseRepository.update(course);
        return feedbackForm;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    @Path("/course/{courseId}/feedback/form/{formId}/copy")
    public FeedbackForm copyFeedbackForm(@RestPath String courseId, @RestPath String formId) {
        Course course = courseRepository.findById(new ObjectId(courseId));
        if (!isOwner(course)) {
            throw new NotFoundException();
        }
        FeedbackForm feedbackForm = course.getFeedbackFormById(new ObjectId(formId));
        if (feedbackForm == null) {
            throw new NotFoundException();
        }
        FeedbackForm copiedFeedbackForm = new FeedbackForm(course.getId(), feedbackForm.getName() + (" (Copy)"),
                feedbackForm.getDescription(), new ArrayList<>(), FormStatus.NOT_STARTED);

        // TODO: check if this is the correct way to copy questions (sync or copy)
        for (QuestionWrapper questionWrapper : feedbackForm.getQuestions()) {
            FeedbackQuestion feedbackQuestion = course.getFeedbackQuestionById(questionWrapper.getQuestionId());
            copiedFeedbackForm.addQuestion(new QuestionWrapper(feedbackQuestion.getId(), new ArrayList<>()));
        }
        course.addFeedbackForm(copiedFeedbackForm);
        feedbackForm.wasUpdated();
        course.wasUpdated();
        courseRepository.update(course);

        return getFeedbackForm(courseId, copiedFeedbackForm.getId().toString());
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
        course.wasUpdated();
        courseRepository.update(course);
        return Response.ok().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    @Path("/course/{courseId}/feedback/form/{formId}/question/{questionId}")
    public QuestionWrapper getFeedbackQuestion(@RestPath String courseId, @RestPath String formId,
            @RestPath String questionId) {
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
    public QuestionWrapper updateFeedbackQuestion(@RestPath String courseId, @RestPath String formId,
            @RestPath String questionId, UpdateFeedbackQuestionRequest request) {
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
        feedbackQuestion.setRangeHigh(request.rangeHigh);

        try {
            feedbackQuestion.setType(FeedbackQuestionType.valueOf(request.type));
        } catch (IllegalArgumentException e) {
            throw new NotFoundException();
        }

        feedbackForm.wasUpdated();
        course.wasUpdated();
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
    public QuestionWrapper addFeedbackQuestion(@RestPath String courseId, @RestPath String formId,
            AddFeedbackQuestionRequest request) {
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
            FeedbackQuestion feedbackQuestion = new FeedbackQuestion(request.name, request.description, questionType,
                    request.options, "", request.rangeLow, request.rangeHigh);
            course.addFeedbackQuestion(feedbackQuestion);
            QuestionWrapper questionWrapper = new QuestionWrapper(feedbackQuestion.getId(), new ArrayList<>());
            feedbackForm.addQuestion(questionWrapper);
            feedbackForm.wasUpdated();
            course.wasUpdated();
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
    public Response deleteFeedbackQuestion(@RestPath String courseId, @RestPath String formId,
            @RestPath String questionId) {
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
        feedbackForm.wasUpdated();
        course.wasUpdated();
        courseRepository.update(course);
        return Response.ok().build();
    }

    // NOW THE SAME FOR QUIZ FORMS
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    @Path("/course/{courseId}/quiz/form/{formId}")
    public QuizForm getQuizForm(@RestPath String courseId, @RestPath String formId) {
        Course course = courseRepository.findById(new ObjectId(courseId));
        if (!isOwner(course)) {
            throw new NotFoundException();
        }
        QuizForm quizForm = course.getQuizFormById(new ObjectId(formId));
        if (quizForm == null) {
            throw new NotFoundException();
        }
        QuizForm quizFormWithQuestionContents = quizForm
                .copyWithoutResultsAndParticipantsButWithQuestionContents(course);
        return quizFormWithQuestionContents;
    }

    static class UpdateQuizFormRequest {
        public String name;
        public String description;
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    @Path("/course/{courseId}/quiz/form/{formId}")
    public QuizForm updateQuizForm(@RestPath String courseId, @RestPath String formId, UpdateQuizFormRequest request) {
        Course course = courseRepository.findById(new ObjectId(courseId));
        if (!isOwner(course)) {
            throw new NotFoundException();
        }
        QuizForm quizForm = course.getQuizFormById(new ObjectId(formId));
        if (quizForm == null) {
            throw new NotFoundException();
        }
        quizForm.setName(request.name);
        quizForm.setDescription(request.description);
        quizForm.wasUpdated();
        course.wasUpdated();
        courseRepository.update(course);
        return getQuizForm(courseId, formId);
    }

    static class ReorderQuizFormQuestionsRequest {
        public List<String> questionIds;
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    @Path("/course/{courseId}/quiz/form/{formId}/reorder")
    public QuizForm reorderQuizFormQuestions(@RestPath String courseId, @RestPath String formId,
            ReorderQuizFormQuestionsRequest request) {
        Course course = courseRepository.findById(new ObjectId(courseId));
        if (!isOwner(course)) {
            throw new NotFoundException();
        }
        QuizForm quizForm = course.getQuizFormById(new ObjectId(formId));
        if (quizForm == null) {
            throw new NotFoundException();
        }
        List<QuestionWrapper> reorderedQuestions = new ArrayList<>();
        for (String questionId : request.questionIds) {
            QuestionWrapper questionWrapper = quizForm.getQuestionById(new ObjectId(questionId));
            if (questionWrapper == null) {
                throw new NotFoundException();
            }
            reorderedQuestions.add(questionWrapper);
        }
        quizForm.setQuestions(reorderedQuestions);
        quizForm.wasUpdated();
        course.wasUpdated();
        courseRepository.update(course);
        return getQuizForm(courseId, formId);
    }

    static class AddQuizFormRequest {
        public String name;
        public String description;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    @Path("/course/{courseId}/quiz/form")
    public QuizForm addQuizForm(@RestPath String courseId, AddQuizFormRequest request) {
        Course course = courseRepository.findById(new ObjectId(courseId));
        if (!isOwner(course)) {
            throw new NotFoundException();
        }
        QuizForm quizForm = new QuizForm(course.getId(), request.name, request.description, new ArrayList<>(),
                FormStatus.NOT_STARTED, 0, false);
        course.addQuizForm(quizForm);
        course.wasUpdated();
        courseRepository.update(course);
        return quizForm;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    @Path("/course/{courseId}/quiz/form/{formId}/copy")
    public QuizForm copyQuizForm(@RestPath String courseId, @RestPath String formId) {
        Course course = courseRepository.findById(new ObjectId(courseId));
        if (!isOwner(course)) {
            throw new NotFoundException();
        }
        QuizForm quizForm = course.getQuizFormById(new ObjectId(formId));
        if (quizForm == null) {
            throw new NotFoundException();
        }
        QuizForm copiedQuizForm = new QuizForm(course.getId(), quizForm.getName() + (" (Copy)"),
                quizForm.getDescription(), new ArrayList<>(), FormStatus.NOT_STARTED, 0, false);

        // TODO: check if this is the correct way to copy questions (sync or copy)
        for (QuestionWrapper questionWrapper : quizForm.getQuestions()) {
            QuizQuestion quizQuestion = course.getQuizQuestionById(questionWrapper.getQuestionId());
            copiedQuizForm.addQuestion(new QuestionWrapper(quizQuestion.getId(), new ArrayList<>()));
        }

        copiedQuizForm.wasUpdated();
        course.addQuizForm(copiedQuizForm);
        course.wasUpdated();
        courseRepository.update(course);

        return getQuizForm(courseId, copiedQuizForm.getId().toString());

    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    @Path("/course/{courseId}/quiz/form/{formId}")
    public Response deleteQuizForm(@RestPath String courseId, @RestPath String formId) {
        Course course = courseRepository.findById(new ObjectId(courseId));
        if (!isOwner(course)) {
            throw new NotFoundException();
        }
        QuizForm quizForm = course.getQuizFormById(new ObjectId(formId));
        if (quizForm == null) {
            throw new NotFoundException();
        }
        course.removeQuizForm(quizForm);
        course.wasUpdated();
        courseRepository.update(course);
        return Response.ok().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    @Path("/course/{courseId}/quiz/form/{formId}/question/{questionId}")
    public QuestionWrapper getQuizQuestion(@RestPath String courseId, @RestPath String formId,
            @RestPath String questionId) {
        Course course = courseRepository.findById(new ObjectId(courseId));
        if (!isOwner(course)) {
            throw new NotFoundException();
        }
        QuizForm quizForm = course.getQuizFormById(new ObjectId(formId));
        if (quizForm == null) {
            throw new NotFoundException();
        }
        QuestionWrapper questionWrapper = quizForm.getQuestionById(new ObjectId(questionId));
        if (questionWrapper == null) {
            throw new NotFoundException();
        }
        QuizQuestion quizQuestion = course.getQuizQuestionById(questionWrapper.getQuestionId());
        if (quizQuestion == null) {
            throw new NotFoundException();
        }
        questionWrapper.setQuestionContent(quizQuestion);
        return questionWrapper;
    }

    static class UpdateQuizQuestionRequest {
        public String name;
        public String description;
        public String type;
        public List<String> options;
        public Boolean hasCorrectAnswers;
        public List<String> correctAnswers;
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    @Path("/course/{courseId}/quiz/form/{formId}/question/{questionId}")
    public QuestionWrapper updateQuizQuestion(@RestPath String courseId, @RestPath String formId,
            @RestPath String questionId, UpdateQuizQuestionRequest request) {
        Course course = courseRepository.findById(new ObjectId(courseId));
        if (!isOwner(course)) {
            throw new NotFoundException();
        }
        QuizForm quizForm = course.getQuizFormById(new ObjectId(formId));
        if (quizForm == null) {
            throw new NotFoundException();
        }
        QuestionWrapper questionWrapper = quizForm.getQuestionById(new ObjectId(questionId));
        if (questionWrapper == null) {
            throw new NotFoundException();
        }
        QuizQuestion quizQuestion = course.getQuizQuestionById(questionWrapper.getQuestionId());
        quizQuestion.setName(request.name);
        quizQuestion.setDescription(request.description);
        quizQuestion.setOptions(request.options);
        quizQuestion.setHasCorrectAnswers(request.hasCorrectAnswers);
        quizQuestion.setCorrectAnswers(request.correctAnswers);
        try {
            quizQuestion.setType(QuizQuestionType.valueOf(request.type));
        } catch (IllegalArgumentException e) {
            throw new NotFoundException();
        }
        quizForm.wasUpdated();
        course.wasUpdated();
        courseRepository.update(course);
        return getQuizQuestion(courseId, formId, questionId);
    }

    static class AddQuizQuestionRequest {
        public String name;
        public String description;
        public String type;
        public List<String> options;
        public Boolean hasCorrectAnswers;
        public List<String> correctAnswers;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    @Path("/course/{courseId}/quiz/form/{formId}/question")
    public QuestionWrapper addQuizQuestion(@RestPath String courseId, @RestPath String formId,
            AddQuizQuestionRequest request) {
        Course course = courseRepository.findById(new ObjectId(courseId));
        if (!isOwner(course)) {
            throw new NotFoundException();
        }
        QuizForm quizForm = course.getQuizFormById(new ObjectId(formId));
        if (quizForm == null) {
            throw new NotFoundException();
        }
        try {
            QuizQuestionType questionType = QuizQuestionType.valueOf(request.type);
            QuizQuestion quizQuestion = new QuizQuestion(request.name, request.description, questionType,
                    request.options, request.hasCorrectAnswers, request.correctAnswers, "");
            course.addQuizQuestion(quizQuestion);
            QuestionWrapper questionWrapper = new QuestionWrapper(quizQuestion.getId(), new ArrayList<>());
            quizForm.addQuestion(questionWrapper);
            course.wasUpdated();
            quizForm.wasUpdated();
            courseRepository.update(course);
            questionWrapper.setQuestionContent(quizQuestion);
            return questionWrapper;
        } catch (IllegalArgumentException e) {
            throw new NotFoundException();
        }
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    @Path("/course/{courseId}/quiz/form/{formId}/question/{questionId}")
    public Response deleteQuizQuestion(@RestPath String courseId, @RestPath String formId,
            @RestPath String questionId) {
        Course course = courseRepository.findById(new ObjectId(courseId));
        if (!isOwner(course)) {
            throw new NotFoundException();
        }
        QuizForm quizForm = course.getQuizFormById(new ObjectId(formId));
        if (quizForm == null) {
            throw new NotFoundException();
        }
        QuestionWrapper questionWrapper = quizForm.getQuestionById(new ObjectId(questionId));
        if (questionWrapper == null) {
            throw new NotFoundException();
        }
        QuizQuestion quizQuestion = course.getQuizQuestionById(questionWrapper.getQuestionId());
        if (quizQuestion == null) {
            throw new NotFoundException();
        }
        course.removeQuizQuestion(quizQuestion);
        quizForm.removeQuestion(questionWrapper);
        quizForm.wasUpdated();
        course.wasUpdated();
        courseRepository.update(course);
        return Response.ok().build();
    }

    static class ChangeCourseOfFormRequest {
        public String newCourseId;
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    @Path("/course/{courseId}/form/{formId}/change-course")
    public Boolean changeCourseOfForm(@RestPath String courseId, @RestPath String formId,
            ChangeCourseOfFormRequest request) {
        Course course = courseRepository.findById(new ObjectId(courseId));
        if (!isOwner(course)) {
            throw new NotFoundException();
        }
        Course newCourse = courseRepository.findById(new ObjectId(request.newCourseId));
        if (!isOwner(newCourse)) {
            throw new NotFoundException();
        }

        Form form = course.getFormById(new ObjectId(formId));
        if (form == null) {
            throw new NotFoundException();
        }

        if (form instanceof QuizForm) {
            QuizForm quizForm = (QuizForm) form;
            course.removeQuizForm(quizForm);

            // copy the question contents
            List<QuestionWrapper> questions = new ArrayList<>();
            for (QuestionWrapper questionWrapper : quizForm.getQuestions()) {
                QuizQuestion quizQuestion = course.getQuizQuestionById(questionWrapper.getQuestionId());
                QuizQuestion copiedQuizQuestion = new QuizQuestion(
                        quizQuestion.getName(),
                        quizQuestion.getDescription(),
                        quizQuestion.getType(),
                        quizQuestion.getOptions(),
                        quizQuestion.getHasCorrectAnswers(),
                        quizQuestion.getCorrectAnswers(),
                        "");
                newCourse.addQuizQuestion(copiedQuizQuestion);
                questions.add(new QuestionWrapper(copiedQuizQuestion.getId(), new ArrayList<>()));
            }

            quizForm.setQuestions(questions);
            newCourse.addQuizForm(quizForm);
        } else {

            FeedbackForm feedbackForm = (FeedbackForm) form;
            course.removeFeedbackForm(feedbackForm);

            // copy the question contents
            List<QuestionWrapper> questions = new ArrayList<>();
            for (QuestionWrapper questionWrapper : feedbackForm.getQuestions()) {
                FeedbackQuestion feedbackQuestion = course.getFeedbackQuestionById(questionWrapper.getQuestionId());
                FeedbackQuestion copiedFeedbackQuestion = new FeedbackQuestion(
                        feedbackQuestion.getName(),
                        feedbackQuestion.getDescription(),
                        feedbackQuestion.getType(),
                        feedbackQuestion.getOptions(),
                        "",
                        feedbackQuestion.getRangeLow(),
                        feedbackQuestion.getRangeHigh());
                newCourse.addFeedbackQuestion(copiedFeedbackQuestion);
                questions.add(new QuestionWrapper(copiedFeedbackQuestion.getId(), new ArrayList<>()));
            }
            feedbackForm.setQuestions(questions);
            newCourse.addFeedbackForm(feedbackForm);
        }
        Date oneSecondAgo = new Date(System.currentTimeMillis() - 1000);
        course.setLastModified(oneSecondAgo);
        newCourse.wasUpdated();
        courseRepository.update(course);
        courseRepository.update(newCourse);
        return true;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    @Path("/course/{courseId}/feedback/form/{formId}/question/{questionId}/results")
    public List<Result> getFeedbackQuestionResults(@RestPath String courseId, @RestPath String formId,
            @RestPath String questionId) {
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
        List<Result> results = questionWrapper.getResults();
        return results;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ UserRole.PROF, UserRole.STUDENT })
    @Path("/course/{courseId}/quiz/form/{formId}/question/{questionId}/results")
    public List<Result> getQuizQuestionResults(@RestPath String courseId, @RestPath String formId,
            @RestPath String questionId) {
        Course course = courseRepository.findById(new ObjectId(courseId));
        if (!isOwner(course)) {
            throw new NotFoundException();
        }
        QuizForm quizForm = course.getQuizFormById(new ObjectId(formId));
        if (quizForm == null) {
            throw new NotFoundException();
        }
        QuestionWrapper questionWrapper = quizForm.getQuestionById(new ObjectId(questionId));
        if (questionWrapper == null) {
            throw new NotFoundException();
        }
        List<Result> results = questionWrapper.getResults();
        return results;
    }

}
