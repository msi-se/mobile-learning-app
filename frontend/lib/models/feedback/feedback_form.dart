import 'package:frontend/models/feedback/feedback_question.dart';
import 'package:frontend/models/form.dart';

class FeedbackForm extends Form {

  FeedbackForm({
    required super.id,
    required super.courseId,
    required super.name,
    required super.description,
    required super.connectCode,
    required super.questions,
    required super.isOwner,
    required super.status,
  });

  factory FeedbackForm.fromJson(Map<String, dynamic> json, {bool isOwner = false}) {
    return FeedbackForm(
      id: json['id'],
      courseId: json['courseId'],
      name: json['name'],
      description: json['description'],
      connectCode: (json['connectCode'] as int).toString(),
      questions: json['questions'] == null
          ? []
          : (json['questions'] as List<dynamic>)
              .map((e) => FeedbackQuestion.fromJson(e['questionContent']))
              .toList(),
      isOwner: isOwner,
      status: json['status'],
    );
  }
}
