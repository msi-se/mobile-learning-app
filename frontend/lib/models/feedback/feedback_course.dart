import 'package:frontend/models/feedback/feedback_form.dart';

class FeedbackCourse {
  final String id;
  final String name;
  final String description;
  final List<FeedbackForm> feedbackForms;
  // final List<QuizForm> quizForms;
  // final List<FeedbackQuestion> feedbackQuestions;
  // final List<QuizQuestion> quizQuestions;

  FeedbackCourse({
    required this.id,
    required this.name,
    required this.description,
    required this.feedbackForms,
  });

  factory FeedbackCourse.fromJson(Map<String, dynamic> json) {
    return FeedbackCourse(
      id: json['id'],
      name: json['name'],
      description: json['description'],
      feedbackForms: (json['feedbackForms'] as List<dynamic>)
          .map((e) => FeedbackForm.fromJson(e))
          .toList(),
    );
  }
}
