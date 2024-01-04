package de.htwg_konstanz.mobilelearning.models.quiz;

import java.util.Arrays;
import java.util.List;

import org.bson.types.ObjectId;

import de.htwg_konstanz.mobilelearning.enums.FormStatus;
import de.htwg_konstanz.mobilelearning.models.Course;
import de.htwg_konstanz.mobilelearning.models.Form;
import de.htwg_konstanz.mobilelearning.models.QuestionWrapper;

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
            questionWrapper.setQuestionContent(course.getFeedbackQuestionById(questionWrapper.getQuestionId()));
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
                this.currentQuestionIndex++;

                // check if it is the last question
                if (this.currentQuestionIndex >= this.questions.size()) {
                    this.status = FormStatus.FINISHED;
                    return Arrays.asList("CLOSED_QUESTION", "FORM_STATUS_CHANGED");
                }

                this.currentQuestionFinished = false;
                return Arrays.asList("OPENED_NEXT_QUESTION");
            }
            else {
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
}
