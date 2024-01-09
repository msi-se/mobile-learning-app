package de.htwg_konstanz.mobilelearning.models.quiz;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.types.ObjectId;

import de.htwg_konstanz.mobilelearning.enums.FormStatus;
import de.htwg_konstanz.mobilelearning.enums.QuizQuestionType;
import de.htwg_konstanz.mobilelearning.models.Course;
import de.htwg_konstanz.mobilelearning.models.Form;
import de.htwg_konstanz.mobilelearning.models.QuestionWrapper;
import de.htwg_konstanz.mobilelearning.services.api.models.ApiQuizForm;

public class QuizForm extends Form {

    public Integer currentQuestionIndex;
    public Boolean currentQuestionFinished;

    public List<QuizParticipant> participants;

    public QuizForm() {
    }

    public QuizForm(
            ObjectId courseId,
            String name,
            String description,
            List<QuestionWrapper> questions,
            FormStatus status,
            Integer currentQuestionIndex,
            Boolean currentQuestionFinished) {

        super(courseId, name, description, questions, status);
        this.currentQuestionIndex = currentQuestionIndex;
        this.currentQuestionFinished = currentQuestionFinished;
        this.participants = List.of();
    }

    public void fillQuestionContents(Course course) {
        for (QuestionWrapper questionWrapper : this.questions) {
            questionWrapper.setQuestionContent(course.getQuizQuestionById(questionWrapper.getQuestionId()));
        }
    }

    public void clearQuestionContents() {
        for (QuestionWrapper questionWrapper : this.questions) {
            questionWrapper.setQuestionContent(null);
        }
    }

    public QuizForm copy() {
        QuizForm copy = new QuizForm(
                this.courseId,
                this.name,
                this.description,
                this.questions,
                this.status,
                this.currentQuestionIndex,
                this.currentQuestionFinished);
        copy.id = new ObjectId(this.id.toHexString());
        copy.connectCode = this.connectCode;
        copy.participants = this.participants;
        return copy;
    }

    public QuizForm copyWithoutResults() {
        QuizForm copy = this.copy();
        copy.clearResults();
        return copy;
    }

    public QuizForm copyWithoutResultsButWithQuestionContents(Course course) {
        QuizForm copy = this.copyWithoutResults();
        copy.fillQuestionContents(course);
        return copy;
    }

    public QuizForm copyWithQuestionContents(Course course) {
        QuizForm copy = this.copy();
        copy.fillQuestionContents(course);
        return copy;
    }

    public void addParticipant(ObjectId userId, String userAlias) {
        if (this.participants == null) {
            this.participants = new java.util.ArrayList<QuizParticipant>();
        }

        // if user is already participating, just update the alias
        for (QuizParticipant participant : this.participants) {
            if (participant.getUserId().equals(userId)) {
                participant.userAlias = userAlias;
                return;
            }
        }

        // otherwise add a new participant
        this.participants.add(new QuizParticipant(userId, userAlias));
    }

    public Integer increaseScoreOfParticipant(ObjectId userId, Integer by) {
        for (QuizParticipant participant : this.participants) {
            if (participant.getUserId().equals(userId)) {
                return participant.increaseScore(by);
            }
        }
        return null;
    }

    public List<String> next() {

        if (this.status == FormStatus.NOT_STARTED) {
            this.status = FormStatus.STARTED;
            this.currentQuestionIndex = 0;
            this.currentQuestionFinished = false;
            return Arrays.asList("OPENED_FIRST_QUESTION", "FORM_STATUS_CHANGED");
        }

        if (this.status == FormStatus.STARTED) {
            if (this.currentQuestionFinished) {
                // check if it is the last question
                if (this.currentQuestionIndex >= this.questions.size() - 1) {
                    this.status = FormStatus.FINISHED;
                    return Arrays.asList("CLOSED_QUESTION", "FORM_STATUS_CHANGED");
                }

                this.currentQuestionIndex++;

                this.currentQuestionFinished = false;
                return Arrays.asList("OPENED_NEXT_QUESTION");
            } else {
                this.currentQuestionFinished = true;
                return Arrays.asList("CLOSED_QUESTION");
            }
        }

        // TODO: remove later (only debug)
        if (this.status == FormStatus.FINISHED) {
            this.status = FormStatus.NOT_STARTED;
            this.currentQuestionIndex = 0;
            this.currentQuestionFinished = false;
            this.clearResults();
            this.clearParticipants();
            return Arrays.asList("FORM_STATUS_CHANGED");
        }

        return Arrays.asList();
    }

    public void clearParticipants() {
        this.participants.clear();
    }

    public QuizForm copyWithoutResultsAndParticipantsButWithQuestionContents(Course byId) {
        QuizForm copy = this.copyWithoutResultsButWithQuestionContents(byId);
        copy.clearParticipants();
        return copy;
    }

    public Boolean isParticipant(String userId) {
        if (this.participants == null) {
            return false;
        }
        for (QuizParticipant participant : this.participants) {
            if (participant.getUserId().toHexString().equals(userId)) {
                return true;
            }
        }
        return false;
    }

    public static QuizForm fromApiQuizForm(ApiQuizForm apiQuizForm, Course course) throws IllegalArgumentException {

        // validate input
        if (apiQuizForm.name == null || apiQuizForm.name.isEmpty()) {
            throw new IllegalArgumentException("Quiz form name must not be empty.");
        }

        if (apiQuizForm.description == null || apiQuizForm.description.isEmpty()) {
            throw new IllegalArgumentException("Quiz form description must not be empty.");
        }

        if (apiQuizForm.questions == null || apiQuizForm.questions.isEmpty()) {
            throw new IllegalArgumentException("Quiz form must have at least one question.");
        }

        if (apiQuizForm.key == null || apiQuizForm.key.isEmpty()) {
            throw new IllegalArgumentException("Quiz form key must not be empty.");
        }

        // create quiz questions
        List<QuestionWrapper> questionWrappers = QuizForm.questionWrappersFromApiQuizQuestions(apiQuizForm.questions,
                course);

        // add quiz form to course
        QuizForm quizForm = new QuizForm(
                course.getId(),
                apiQuizForm.name,
                apiQuizForm.description,
                questionWrappers,
                FormStatus.NOT_STARTED,
                0,
                false);
        quizForm.setKey(apiQuizForm.key);

        return quizForm;

    }

    private static List<QuestionWrapper> questionWrappersFromApiQuizQuestions(
            List<ApiQuizForm.ApiQuizQuestion> questions,
            Course course) throws IllegalArgumentException {

        List<ObjectId> quizQuestionIds = new ArrayList<>();
        for (ApiQuizForm.ApiQuizQuestion apiQuizQuestion : questions) {
            if (apiQuizQuestion.name == null || apiQuizQuestion.name.isEmpty()) {
                throw new IllegalArgumentException("Quiz question name must not be empty.");
            }

            if (apiQuizQuestion.description == null || apiQuizQuestion.description.isEmpty()) {
                throw new IllegalArgumentException("Quiz question description must not be empty.");
            }

            if (apiQuizQuestion.type == null || apiQuizQuestion.type.isEmpty()) {
                throw new IllegalArgumentException("Quiz question type must not be empty.");
            }

            // check if type is valid (in enum)
            try {
                QuizQuestionType.valueOf(apiQuizQuestion.type);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid quiz question type.");
            }

            // if it is a single choice question, there must be options
            if ((apiQuizQuestion.type.equals(QuizQuestionType.SINGLE_CHOICE.toString())
                    || apiQuizQuestion.type.equals(QuizQuestionType.MULTIPLE_CHOICE.toString()))
                    && apiQuizQuestion.options.size() < 2) {
                throw new IllegalArgumentException(
                        "Single or Multiple choice quiz question must have at least two options.");
            }

            // check if the same question already exists (if so just add the id to the list
            // and continue)
            QuizQuestion existingQuizQuestion = null; // TODO: course.getQuizQuestionByNameAndDescription(...);
            for (QuizQuestion quizQuestion : course.getQuizQuestions()) {
                if (quizQuestion.getName().equals(apiQuizQuestion.name)
                        && quizQuestion.getDescription().equals(apiQuizQuestion.description)) {
                    quizQuestionIds.add(quizQuestion.getId());
                    existingQuizQuestion = quizQuestion;
                    break;
                }
            }
            if (existingQuizQuestion != null) {
                
                // update the other question properties
                existingQuizQuestion.setType(QuizQuestionType.valueOf(apiQuizQuestion.type));
                existingQuizQuestion.setOptions(apiQuizQuestion.options);
                existingQuizQuestion.setHasCorrectAnswer(apiQuizQuestion.hasCorrectAnswer);
                existingQuizQuestion.setCorrectAnswer(apiQuizQuestion.correctAnswer);
            }

            // create quiz question
            QuizQuestion quizQuestion = new QuizQuestion(
                    apiQuizQuestion.name,
                    apiQuizQuestion.description,
                    QuizQuestionType.valueOf(apiQuizQuestion.type),
                    apiQuizQuestion.options,
                    apiQuizQuestion.hasCorrectAnswer,
                    apiQuizQuestion.correctAnswer);

            course.addQuizQuestion(quizQuestion);
            quizQuestionIds.add(quizQuestion.getId());
        }

        // create question wrappers
        List<QuestionWrapper> questionWrappers = new ArrayList<>();
        for (ObjectId quizQuestionId : quizQuestionIds) {
            questionWrappers.add(new QuestionWrapper(quizQuestionId, null));
        }

        return questionWrappers;

    }

    public void updateFromApiQuizForm(ApiQuizForm apiQuizForm, Course course) throws IllegalArgumentException {

        // validate input
        if (apiQuizForm.name == null || apiQuizForm.name.isEmpty()) {
            throw new IllegalArgumentException("Quiz form name must not be empty.");
        }

        if (apiQuizForm.description == null || apiQuizForm.description.isEmpty()) {
            throw new IllegalArgumentException("Quiz form description must not be empty.");
        }

        if (apiQuizForm.questions == null || apiQuizForm.questions.isEmpty()) {
            throw new IllegalArgumentException("Quiz form must have at least one question.");
        }

        // update quiz questions
        List<QuestionWrapper> questionWrappers = QuizForm.questionWrappersFromApiQuizQuestions(apiQuizForm.questions,
                course);
        this.setQuestions(questionWrappers);

        // update quiz form
        this.setName(apiQuizForm.name);
        this.setDescription(apiQuizForm.description);
    }
}
