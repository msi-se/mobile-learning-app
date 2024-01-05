package de.htwg_konstanz.mobilelearning.services;

import java.util.List;

import org.bson.types.ObjectId;

import de.htwg_konstanz.mobilelearning.enums.FormStatus;
import de.htwg_konstanz.mobilelearning.enums.QuizQuestionType;
import de.htwg_konstanz.mobilelearning.enums.FeedbackQuestionType;
import de.htwg_konstanz.mobilelearning.models.Course;
import de.htwg_konstanz.mobilelearning.models.QuestionWrapper;
import de.htwg_konstanz.mobilelearning.models.auth.User;
import de.htwg_konstanz.mobilelearning.models.feedback.FeedbackForm;
import de.htwg_konstanz.mobilelearning.models.feedback.FeedbackQuestion;
import de.htwg_konstanz.mobilelearning.models.quiz.QuizForm;
import de.htwg_konstanz.mobilelearning.models.quiz.QuizQuestion;
import de.htwg_konstanz.mobilelearning.repositories.CourseRepository;
import de.htwg_konstanz.mobilelearning.repositories.UserRepository;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/mock")
public class MockingService {

    @Inject
    private CourseRepository courseRepository;

    @Inject
    private UserRepository userRepository;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/mock")
    public Object addData() {
        courseRepository.deleteAll();
        // userRepository.deleteAll();

        // generate some FeedbackQuestions
        FeedbackQuestion question1 = new FeedbackQuestion(
            "Verständlichkeit",
            "Wie verständlich war das Thema Kombinatorik?",
            FeedbackQuestionType.STARS,
            null
        );
        FeedbackQuestion question2 = new FeedbackQuestion(
            "Kurzweiligkeit",
            "Wie kurzweilig war die Vorlesung? (links = langweilig, rechts = kurzweilig)",
            FeedbackQuestionType.SLIDER,
            null
        );
        FeedbackQuestion question3 = new FeedbackQuestion(
            "Praxisbezug",
            "Wie bewerten Sie den Praxisbezug der Vorlesung? (links = wenig Praxisbezug, rechts = viel Praxisbezug)",
            FeedbackQuestionType.SLIDER,
            null
        );
        FeedbackQuestion question4 = new FeedbackQuestion(
            "Sprachbarriere",
            "Die Vorlesung wurde auf Englisch gehalten. Wie fanden Sie die Verständlichkeit?",
            FeedbackQuestionType.SLIDER,
            null
        );
        FeedbackQuestion question5 = new FeedbackQuestion(
            "Prüfungsvorbereitung",
            "Wenn jetzt direkt die Prüfung wäre, wie gut fühlen Sie sich vorbereitet?",
            FeedbackQuestionType.STARS,
            null
        );
        FeedbackQuestion question6 = new FeedbackQuestion(
            "Technische Mittel",
            "Wie bewerten Sie die technischen Mittel, die in der Vorlesung verwendet wurden?",
            FeedbackQuestionType.STARS,
            null
        );
        FeedbackQuestion question7 = new FeedbackQuestion(
            "Schwierigstes Thema",
            "Welches Thema war für Sie am schwierigsten?",
            FeedbackQuestionType.SINGLE_CHOICE,
            List.of("Kombinatorik", "Graphen", "Relationen", "Formale Sprachen", "Endliche Automaten", "Turingmaschinen", "Berechenbarkeit")
        );
        FeedbackQuestion question8 = new FeedbackQuestion(
            "Schwierigstes Thema",
            "Welches Thema war für Sie am schwierigsten?",
            FeedbackQuestionType.SINGLE_CHOICE,
            List.of("Multitenancy", "Microservices", "Cloud Foundry", "Docker", "Kubernetes", "Cloud Native", "Cloud Native Buildpacks")
        );

        // generate a Course
        Course courseDima = new Course("Diskrete Mathematik", "Feedback-Kanal für DiMa");
        Course courseAUME = new Course("AUME", "Feedback-Kanal für Agile Vorgehensmodelle und Mobile Kommunikation");
        Course courseCloud = new Course("Cloud Application Development", "Feedback-Kanal für Cloud Application Development");

        // add the FeedbackQuestions to the Course
        courseDima.addFeedbackQuestion(question1);
        courseDima.addFeedbackQuestion(question2);
        courseDima.addFeedbackQuestion(question3);
        courseDima.addFeedbackQuestion(question4);
        courseDima.addFeedbackQuestion(question5);
        courseDima.addFeedbackQuestion(question6);
        courseDima.addFeedbackQuestion(question7);
        
        courseAUME.addFeedbackQuestion(question2);
        courseAUME.addFeedbackQuestion(question4);
        courseAUME.addFeedbackQuestion(question5);

        courseCloud.addFeedbackQuestion(question2);
        courseCloud.addFeedbackQuestion(question4);
        courseCloud.addFeedbackQuestion(question5);
        courseCloud.addFeedbackQuestion(question8); 
        

        // generate a FeedbackForm
        ObjectId courseDimaId = courseDima.getId();
        ObjectId courseAUMEId = courseAUME.getId();
        ObjectId courseCloudId = courseCloud.getId();
        FeedbackForm feedbackForm1 = new FeedbackForm(
            courseDimaId,
            "1. Monat im neuen Semester",
            "Dies ist das Feedback für den 1. Monat im neuen Semester",
            List.of(
                new QuestionWrapper(question2.getId(), null), 
                new QuestionWrapper(question4.getId(), null), 
                new QuestionWrapper(question5.getId(), null)
            ),
            FormStatus.NOT_STARTED
        );
        courseDima.addFeedbackForm(feedbackForm1);
        FeedbackForm feedbackForm2 = new FeedbackForm(
            courseDimaId,
            "Kombinatorik",
            "Dies ist das Feedback zum Thema Kombinatorik",
            List.of(
                new QuestionWrapper(question1.getId(), null), 
                new QuestionWrapper(question2.getId(), null), 
                new QuestionWrapper(question3.getId(), null), 
                new QuestionWrapper(question4.getId(), null), 
                new QuestionWrapper(question5.getId(), null), 
                new QuestionWrapper(question6.getId(), null), 
                new QuestionWrapper(question7.getId(), null)),
            FormStatus.NOT_STARTED
        );
        courseDima.addFeedbackForm(feedbackForm2);
        FeedbackForm feedbackForm3 = new FeedbackForm(
            courseAUMEId,
            "1. Monat im neuen Semester",
            "Dies ist das Feedback für den 1. Monat im neuen Semester",
            List.of(
                new QuestionWrapper(question2.getId(), null), 
                new QuestionWrapper(question4.getId(), null), 
                new QuestionWrapper(question5.getId(), null)
            ),
            FormStatus.NOT_STARTED
        );
        courseAUME.addFeedbackForm(feedbackForm3);
        FeedbackForm feedbackForm4 = new FeedbackForm(
            courseCloudId,
            "1. Monat im neuen Semester",
            "Dies ist das Feedback für den 1. Monat im neuen Semester",
            List.of(
                new QuestionWrapper(question2.getId(), null), 
                new QuestionWrapper(question4.getId(), null), 
                new QuestionWrapper(question5.getId(), null),
                new QuestionWrapper(question8.getId(), null)
            ),
            FormStatus.NOT_STARTED
        );
        courseCloud.addFeedbackForm(feedbackForm4);

        // set prof owner of dima course (if exists)
        User prof = userRepository.findByUsername("Prof");
        if (prof != null) {
            courseDima.addOwner(prof.getId());
        }

        // save the Course
        courseRepository.persist(courseDima);
        courseRepository.persist(courseAUME);
        courseRepository.persist(courseCloud);



        // ############################## QUIZ ####################################

        // generate a few questions
        QuizQuestion quizQuestion1 = new QuizQuestion(
            "Höchster Berg der Welt",
            "Welcher Berg ist der höchste der Welt?",
            QuizQuestionType.SINGLE_CHOICE,
            List.of("Mount Everest", "Mont Blanc", "Matterhorn", "Zugspitze"),
            true,
            "Mount Everest" 
        );
        QuizQuestion quizQuestion2 = new QuizQuestion(
            "Hauptstadt von Deutschland",
            "Ist Konstanz die Hauptstadt von Deutschland?",
            QuizQuestionType.YES_NO,
            null,
            true,
            "Nein"
        );

        // add the questions to the Dima course
        courseDima.addQuizQuestion(quizQuestion1);
        courseDima.addQuizQuestion(quizQuestion2);

        // create quiz form
        QuizForm quizForm1 = new QuizForm(
            courseDimaId,
            "Quiz 1",
            "Dies ist das erste Quiz",
            List.of(
                new QuestionWrapper(quizQuestion1.getId(), null), 
                new QuestionWrapper(quizQuestion2.getId(), null)
            ),
            FormStatus.NOT_STARTED,
            0,
            false
        );

        // add the quiz form to the Dima course
        courseDima.addQuizForm(quizForm1);

        // save the Course
        courseRepository.update(courseDima);

        // return all Courses but read it from the database
        return courseRepository.listAll();

    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/deleteallcourses")
    public String deleteAllCourses() {
        courseRepository.deleteAll();
        return "All Courses deleted";
    }
}
