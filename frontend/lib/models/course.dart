import 'package:frontend/models/feedback/feedback_form.dart';
import 'package:frontend/models/quiz/quiz_form.dart';

class Course {
  final String id;
  final String name;
  final String description;
  final List<FeedbackForm> feedbackForms;
  final List<QuizForm> quizForms;

  Course({
    required this.id,
    required this.name,
    required this.description,
    required this.feedbackForms,
    required this.quizForms,
  });

  factory Course.fromJson(Map<String, dynamic> json) {
    return Course(
      id: json['id'],
      name: json['name'],
      description: json['description'],
      feedbackForms: (json['feedbackForms'] as List<dynamic>)
          .map((e) => FeedbackForm.fromJson(e))
          .toList(),
      quizForms: (json['quizForms'] as List<dynamic>)
          .map((e) => QuizForm.fromJson(e))
          .toList(),
    );
  }
}
