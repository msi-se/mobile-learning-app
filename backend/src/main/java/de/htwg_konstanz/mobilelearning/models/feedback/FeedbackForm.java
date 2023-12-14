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
            questionWrapper.questionContent.setId(questionWrapper.getQuestionId());
        }
    }

    public void clearQuestionContents() {
        for (QuestionWrapper questionWrapper : this.questions) {
            questionWrapper.setQuestionContent(null);
        }
    }
}