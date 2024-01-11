package de.htwg_konstanz.mobilelearning.models.feedback;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import de.htwg_konstanz.mobilelearning.enums.FeedbackQuestionType;
import de.htwg_konstanz.mobilelearning.enums.FormStatus;
import de.htwg_konstanz.mobilelearning.models.Course;
import de.htwg_konstanz.mobilelearning.models.Form;
import de.htwg_konstanz.mobilelearning.models.QuestionWrapper;
import de.htwg_konstanz.mobilelearning.services.api.models.ApiFeedbackForm;
import de.htwg_konstanz.mobilelearning.services.api.models.ApiFeedbackForm.ApiFeedbackQuestion;

public class FeedbackForm extends Form {
    public FeedbackForm() {
    }

    public FeedbackForm(ObjectId courseId, String name, String description, List<QuestionWrapper> questions,
            FormStatus status) {
        super(courseId, name, description, questions, status);
    }

    public void fillQuestionContents(Course course) {
        for (QuestionWrapper questionWrapper : this.questions) {
            questionWrapper.setQuestionContent(course.getFeedbackQuestionById(questionWrapper.getQuestionId()));
        }
    }

    public void clearQuestionContents() {
        for (QuestionWrapper questionWrapper : this.questions) {
            questionWrapper.setQuestionContent(null);
        }
    }

    public FeedbackForm copy() {
        FeedbackForm copy = new FeedbackForm(this.courseId, this.name, this.description, this.questions, this.status);
        copy.id = new ObjectId(this.id.toHexString());
        copy.connectCode = this.connectCode;
        return copy;
    }

    public FeedbackForm copyWithoutResults() {
        FeedbackForm copy = this.copy();
        copy.clearResults();
        return copy;
    }

    public FeedbackForm copyWithoutResultsButWithQuestionContents(Course course) {
        FeedbackForm copy = this.copyWithoutResults();
        copy.fillQuestionContents(course);
        return copy;
    }

    public FeedbackForm copyWithQuestionContents(Course course) {
        FeedbackForm copy = this.copy();
        copy.fillQuestionContents(course);
        return copy;
    }

    public static FeedbackForm fromApiFeedbackForm(ApiFeedbackForm apiFeedbackForm, Course course)
            throws IllegalArgumentException {

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

        if (apiFeedbackForm.key == null || apiFeedbackForm.key.isEmpty()) {
            throw new IllegalArgumentException("Feedback form key must not be empty.");
        }

        List<QuestionWrapper> questionWrappers = FeedbackForm
                .questionWrappersFromApiFeedbackFormQuestions(apiFeedbackForm.questions, course);

        // add feedback form to course
        FeedbackForm feedbackForm = new FeedbackForm(
                course.getId(),
                apiFeedbackForm.name,
                apiFeedbackForm.description,
                questionWrappers,
                FormStatus.NOT_STARTED);
        feedbackForm.setKey(apiFeedbackForm.key);

        return feedbackForm;

    }

    private static List<QuestionWrapper> questionWrappersFromApiFeedbackFormQuestions(
            List<ApiFeedbackQuestion> questions, Course course) {

        // create feedback questions
        List<ObjectId> feedbackQuestionIds = new ArrayList<>();
        for (ApiFeedbackForm.ApiFeedbackQuestion apiFeedbackQuestion : questions) {

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
            if (apiFeedbackQuestion.type.equals(FeedbackQuestionType.SINGLE_CHOICE.toString())
                    && apiFeedbackQuestion.options.size() < 2) {
                throw new IllegalArgumentException("Single choice feedback question must have at least two options.");
            }

            // check if the same question already exists (if so just add the id to the list
            // and continue)
            FeedbackQuestion existingQuestion = course.getFeedbackQuestionByKey(apiFeedbackQuestion.key);
            if (existingQuestion != null) {
                existingQuestion.setName(apiFeedbackQuestion.name);
                existingQuestion.setDescription(apiFeedbackQuestion.description);
                existingQuestion.setType(FeedbackQuestionType.valueOf(apiFeedbackQuestion.type));
                existingQuestion.setOptions(apiFeedbackQuestion.options);
                feedbackQuestionIds.add(existingQuestion.getId());
                continue;
            }

            // create feedback question
            FeedbackQuestion feedbackQuestion = new FeedbackQuestion(
                    apiFeedbackQuestion.name,
                    apiFeedbackQuestion.description,
                    FeedbackQuestionType.valueOf(apiFeedbackQuestion.type),
                    apiFeedbackQuestion.options,
                    apiFeedbackQuestion.key
                    );

            course.addFeedbackQuestion(feedbackQuestion);
            feedbackQuestionIds.add(feedbackQuestion.getId());
        }

        // create question wrappers
        List<QuestionWrapper> questionWrappers = new ArrayList<>();
        for (ObjectId feedbackQuestionId : feedbackQuestionIds) {
            questionWrappers.add(new QuestionWrapper(feedbackQuestionId, null));
        }

        return questionWrappers;
    }

    public void updateFromApiFeedbackForm(ApiFeedbackForm apiFeedbackForm, Course course)
            throws IllegalArgumentException {

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

        // update feedback questions
        List<QuestionWrapper> questionWrappers = FeedbackForm
                .questionWrappersFromApiFeedbackFormQuestions(apiFeedbackForm.questions, course);
        this.setQuestions(questionWrappers);

        // update feedback form
        this.setName(apiFeedbackForm.name);
        this.setDescription(apiFeedbackForm.description);

    }
}