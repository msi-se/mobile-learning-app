package de.htwg_konstanz.mobilelearning.models.feedback;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.bson.types.ObjectId;

import de.htwg_konstanz.mobilelearning.enums.FeedbackQuestionType;
import de.htwg_konstanz.mobilelearning.enums.FormStatus;
import de.htwg_konstanz.mobilelearning.helper.Hasher;
import de.htwg_konstanz.mobilelearning.models.Course;
import de.htwg_konstanz.mobilelearning.models.Form;
import de.htwg_konstanz.mobilelearning.models.QuestionWrapper;
import de.htwg_konstanz.mobilelearning.models.Result;
import de.htwg_konstanz.mobilelearning.services.api.models.ApiFeedbackForm;
import de.htwg_konstanz.mobilelearning.services.api.models.ApiFeedbackForm.ApiFeedbackQuestion;

public class FeedbackForm extends Form {

    public List<FeedbackParticipant> participants;

    public FeedbackForm() {
    }

    public FeedbackForm(ObjectId courseId, String name, String description, List<QuestionWrapper> questions,
            FormStatus status) {
        super(courseId, name, description, questions, status);
        this.participants = new ArrayList<FeedbackParticipant>();
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
        copy.participants = this.participants;
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

    public List<FeedbackParticipant> getParticipants() {
        return this.participants;
    }

    public Boolean addParticipant(ObjectId userId) {
        if (this.participants == null) {
            this.participants = new java.util.ArrayList<FeedbackParticipant>();
        }

        // if user is already participating, just return true
        for (FeedbackParticipant participant : this.participants) {
            if (participant.getUserId().toHexString().equals(userId.toHexString())) {
                return true;
            }
        }

        // otherwise add a new participant
        this.participants.add(new FeedbackParticipant(userId));
        return true;
    }

    public void clearParticipants() {
        if (this.participants == null) { this.participants = new ArrayList<FeedbackParticipant>(); }
        this.participants.clear();
    }

    public Boolean isParticipant(String userId) {
        if (this.participants == null) {
            this.participants = new ArrayList<FeedbackParticipant>();
            return false;
        }
        for (FeedbackParticipant participant : this.participants) {
            if (participant.getUserId().toHexString().equals(userId)) {
                return true;
            }
        }
        return false;
    }

    public static FeedbackForm fromApiFeedbackForm(ApiFeedbackForm apiFeedbackForm, Course course)
            throws IllegalArgumentException {

        // validate input
        if (apiFeedbackForm.getName() == null || apiFeedbackForm.getName().isEmpty()) {
            throw new IllegalArgumentException("Feedback form name must not be empty.");
        }

        if (apiFeedbackForm.getDescription() == null || apiFeedbackForm.getDescription().isEmpty()) {
            throw new IllegalArgumentException("Feedback form description must not be empty.");
        }

        if (apiFeedbackForm.getQuestions() == null || apiFeedbackForm.getQuestions().isEmpty()) {
            throw new IllegalArgumentException("Feedback form must have at least one question.");
        }

        if (apiFeedbackForm.getKey() == null || apiFeedbackForm.getKey().isEmpty()) {
            throw new IllegalArgumentException("Feedback form key must not be empty.");
        }

        List<QuestionWrapper> questionWrappers = FeedbackForm
                .questionWrappersFromApiFeedbackFormQuestions(apiFeedbackForm.getQuestions(), course);

        // add feedback form to course
        FeedbackForm feedbackForm = new FeedbackForm(
                course.getId(),
                apiFeedbackForm.getName(),
                apiFeedbackForm.getDescription(),
                questionWrappers,
                FormStatus.NOT_STARTED);
        feedbackForm.setKey(apiFeedbackForm.getKey());

        return feedbackForm;

    }

    private static List<QuestionWrapper> questionWrappersFromApiFeedbackFormQuestions(
            List<ApiFeedbackQuestion> questions, Course course) {

        // create feedback questions
        List<ObjectId> feedbackQuestionIds = new ArrayList<ObjectId>();
        for (ApiFeedbackForm.ApiFeedbackQuestion apiFeedbackQuestion : questions) {

            if (apiFeedbackQuestion.getName() == null || apiFeedbackQuestion.getName().isEmpty()) {
                throw new IllegalArgumentException("Feedback question name must not be empty.");
            }

            if (apiFeedbackQuestion.getDescription() == null || apiFeedbackQuestion.getDescription().isEmpty()) {
                throw new IllegalArgumentException("Feedback question description must not be empty.");
            }

            if (apiFeedbackQuestion.getType() == null || apiFeedbackQuestion.getType().isEmpty()) {
                throw new IllegalArgumentException("Feedback question type must not be empty.");
            }

            // check if type is valid (in enum)
            try {
                FeedbackQuestionType.valueOf(apiFeedbackQuestion.getType());
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid feedback question type.");
            }

            // if it is a single choice question, there must be options
            if (apiFeedbackQuestion.getType().equals(FeedbackQuestionType.SINGLE_CHOICE.toString())
                    && apiFeedbackQuestion.getOptions().size() < 2) {
                throw new IllegalArgumentException("Single choice feedback question must have at least two options.");
            }

            // check if the same question already exists (if so just add the id to the list
            // and continue)
            FeedbackQuestion existingQuestion = course.getFeedbackQuestionByKey(apiFeedbackQuestion.getKey());
            if (existingQuestion != null) {
                existingQuestion.setName(apiFeedbackQuestion.getName());
                existingQuestion.setDescription(apiFeedbackQuestion.getDescription());
                existingQuestion.setType(FeedbackQuestionType.valueOf(apiFeedbackQuestion.getType()));
                existingQuestion.setOptions(apiFeedbackQuestion.getOptions());
                feedbackQuestionIds.add(existingQuestion.getId());
                continue;
            }

            // create feedback question
            FeedbackQuestion feedbackQuestion = new FeedbackQuestion(
                    apiFeedbackQuestion.getName(),
                    apiFeedbackQuestion.getDescription(),
                    FeedbackQuestionType.valueOf(apiFeedbackQuestion.getType()),
                    apiFeedbackQuestion.getOptions(),
                    apiFeedbackQuestion.getKey(),
                    apiFeedbackQuestion.getRangeLow(),
                    apiFeedbackQuestion.getRangeHigh());

            course.addFeedbackQuestion(feedbackQuestion);
            feedbackQuestionIds.add(feedbackQuestion.getId());
        }

        // create question wrappers
        List<QuestionWrapper> questionWrappers = new ArrayList<QuestionWrapper>();
        for (ObjectId feedbackQuestionId : feedbackQuestionIds) {
            questionWrappers.add(new QuestionWrapper(feedbackQuestionId, null));
        }

        return questionWrappers;
    }

    public void updateFromApiFeedbackForm(ApiFeedbackForm apiFeedbackForm, Course course)
            throws IllegalArgumentException {

        // validate input
        if (apiFeedbackForm.getName() == null || apiFeedbackForm.getName().isEmpty()) {
            throw new IllegalArgumentException("Feedback form name must not be empty.");
        }

        if (apiFeedbackForm.getDescription() == null || apiFeedbackForm.getDescription().isEmpty()) {
            throw new IllegalArgumentException("Feedback form description must not be empty.");
        }

        if (apiFeedbackForm.getQuestions() == null || apiFeedbackForm.getQuestions().isEmpty()) {
            throw new IllegalArgumentException("Feedback form must have at least one question.");
        }

        // update feedback questions
        List<QuestionWrapper> questionWrappers = FeedbackForm
                .questionWrappersFromApiFeedbackFormQuestions(apiFeedbackForm.getQuestions(), course);
        this.setQuestions(questionWrappers);

        // update feedback form
        this.setName(apiFeedbackForm.getName());
        this.setDescription(apiFeedbackForm.getDescription());

    }

    public void setParticipants(List<FeedbackParticipant> participants) {
        this.participants = participants;
    }

    public Object getResultsAsCsv(Course course) {

        /*
        
        userId; question1; question2; question3; ...
        userX; 1; 2; 3; ...
        userY; 2; 3; 4; ...
        userY; ""; ""; 3; ... (important: if a user adds more than one result for a question, add a new row for each result (the rest of the row is empty)
        userZ; 7; 8; 9; ...
        */

        List<String> headers = new ArrayList<String>();
        headers.add("user");

        // add question headers
        for (QuestionWrapper questionWrapper : this.questions) {
            FeedbackQuestion question = course.getFeedbackQuestionById(questionWrapper.getQuestionId());
            headers.add(question.getKey() + " | " + question.getName() + " | " + question.getDescription());
        }
        String[] HEADERS = headers.toArray(new String[headers.size()]);

        StringWriter sw = new StringWriter();
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
            .setHeader(HEADERS)
            .build();

        class ResultWithQuestion {
            public Result result;
            public ObjectId questionId;

            public ResultWithQuestion(Result result, ObjectId questionId) {
                this.result = result;
                this.questionId = questionId;
            }
        }

        // iterate over participants
        try (final CSVPrinter printer = new CSVPrinter(sw, csvFormat)) {
            Integer userIndex = 0;
            for (FeedbackParticipant participant : this.participants) {
                String hashedUserId = Hasher.hash(participant.getUserId().toHexString());

                // get all results for this user
                List<ResultWithQuestion> userResults = this.questions.stream()
                    .flatMap(questionWrapper -> questionWrapper.getResults().stream()
                        .filter(result -> result.getHashedUserId().equals(hashedUserId))
                        .map(result -> new ResultWithQuestion(result, questionWrapper.getId())))
                    .collect(Collectors.toList());

                // check how many rows we need for this user
                Integer maxResults = userResults.stream()
                    .map(result -> result.result.getValues().size())
                    .max(Integer::compareTo)
                    .orElse(0);

                // add rows
                for (Integer rowIndex = 0; rowIndex < maxResults; rowIndex++) {
                    final Integer finalRowIndex = rowIndex;
                    List<String> record = new ArrayList<>();
                    record.add("participant-" + userIndex);

                    // add question results
                    this.questions.stream()
                        .map(questionWrapper -> userResults.stream()
                            .filter(result -> result.questionId.equals(questionWrapper.getId()) && result.result.getValues().size() > finalRowIndex)
                            .map(result -> result.result.getValues().get(finalRowIndex))
                            .findFirst()
                            .orElse(""))
                        .forEach(record::add);
                    printer.printRecord(record);
                }
                userIndex++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sw.toString();
    }

    public FeedbackForm deepCopy() {
        FeedbackForm copy = new FeedbackForm();
        copy.id = new ObjectId(this.id.toHexString());
        copy.courseId = new ObjectId(this.courseId.toHexString());
        copy.name = this.name;
        copy.description = this.description;
        copy.questions = new ArrayList<QuestionWrapper>();
        this.questions.forEach(questionWrapper -> {
            copy.questions.add(questionWrapper.deepCopy());
        });
        copy.status = this.status;
        copy.connectCode = this.connectCode;
        copy.participants = new ArrayList<FeedbackParticipant>();
        this.participants.forEach(participant -> {
            copy.participants.add(participant.deepCopy());
        });
        return copy;
    }
}