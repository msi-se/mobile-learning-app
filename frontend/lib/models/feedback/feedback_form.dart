import 'package:frontend/models/feedback/feedback_question.dart';

class FeedbackForm {
  final String id;
  final String courseId;
  final String name;
  final String description;
  final String connectCode;
  final String status;
  final List<FeedbackQuestion> questions;

  FeedbackForm({
    required this.id,
    required this.courseId,
    required this.name,
    required this.description,
    required this.connectCode,
    required this.status,
    required this.questions,
  });

  factory FeedbackForm.fromJson(Map<String, dynamic> json) {
    return FeedbackForm(
      id: json['id'],
      courseId: json['courseId'],
      name: json['name'],
      description: json['description'],
      connectCode: (json['connectCode'] as int).toString(),
      status: json['status'],
      questions: json['questions'] == null
          ? []
          : (json['questions'] as List<dynamic>)
              .map((e) => FeedbackQuestion.fromJson(e['questionContent']))
              .toList(),
    );
  }
}
