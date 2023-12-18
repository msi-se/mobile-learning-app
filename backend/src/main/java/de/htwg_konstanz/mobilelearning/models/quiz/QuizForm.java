package de.htwg_konstanz.mobilelearning.models.quiz;

import java.util.List;

import org.bson.types.ObjectId;

import de.htwg_konstanz.mobilelearning.enums.FormStatus;
import de.htwg_konstanz.mobilelearning.models.Form;
import de.htwg_konstanz.mobilelearning.models.QuestionWrapper;

public class QuizForm extends Form {

    public Integer currentQuestionIndex;
    public Boolean currentQuestionFinished;

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
    }

}
