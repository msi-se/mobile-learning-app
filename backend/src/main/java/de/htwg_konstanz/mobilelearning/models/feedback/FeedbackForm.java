package de.htwg_konstanz.mobilelearning.models.feedback;

import java.util.List;

import org.bson.types.ObjectId;

import de.htwg_konstanz.mobilelearning.enums.FormStatus;
import de.htwg_konstanz.mobilelearning.models.Course;
import de.htwg_konstanz.mobilelearning.models.Form;
import de.htwg_konstanz.mobilelearning.models.QuestionWrapper;

public class FeedbackForm extends Form {
    public FeedbackForm() {
    }

    public FeedbackForm(ObjectId courseId, String name, String description, List<QuestionWrapper> questions, FormStatus status) {
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
        copy.id = this.id;
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
}