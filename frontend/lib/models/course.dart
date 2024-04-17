import 'package:frontend/global.dart';
import 'package:frontend/models/feedback/feedback_form.dart';
import 'package:frontend/models/quiz/quiz_form.dart';

class Course {
  final String id;
  final String name;
  final String description;
  final List<FeedbackForm> feedbackForms;
  final List<QuizForm> quizForms;
  final bool isOwner;
  final String moodleCourseId;

  Course({
    required this.id,
    required this.name,
    required this.description,
    required this.feedbackForms,
    required this.quizForms,
    required this.isOwner,
    required this.moodleCourseId,
  });

  factory Course.fromJson(Map<String, dynamic> json) {
    bool isOwner = false;
    if (getSession() != null) {
      List<String> owners = json['owners'].cast<String>();
      isOwner = owners.contains(getSession()!.userId);
    }

    return Course(
      id: json['id'],
      name: json['name'],
      description: json['description'],
      feedbackForms: (json['feedbackForms'] as List<dynamic>)
          .map((e) => FeedbackForm.fromJson(e, isOwner: isOwner))
          .toList(),
      quizForms: (json['quizForms'] as List<dynamic>)
          .map((e) => QuizForm.fromJson(e, isOwner: isOwner))
          .toList(),
      isOwner: isOwner,
      moodleCourseId: json['moodleCourseId'] ?? '',
    );
  }
}
