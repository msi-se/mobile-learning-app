import 'package:frontend/models/question.dart';

class FeedbackQuestion extends Question {
  FeedbackQuestion(
      {required super.id,
      required super.name,
      required super.description,
      required super.type,
      required super.options});

  factory FeedbackQuestion.fromJson(Map<String, dynamic> json) {
    return FeedbackQuestion(
      id: json['id'],
      name: json['name'],
      description: json['description'],
      type: json['type'],
      options: json['options'].cast<String>(),
    );
  }
}
