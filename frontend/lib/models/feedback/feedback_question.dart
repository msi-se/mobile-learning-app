import 'package:frontend/models/question.dart';

class FeedbackQuestion extends Question {
  
  final String rangeLow;
  final String rangeHigh;

  FeedbackQuestion(
      {required super.id,
      required super.name,
      required super.description,
      required super.type,
      required super.options,
      required this.rangeLow,
      required this.rangeHigh});

  factory FeedbackQuestion.fromJson(Map<String, dynamic> json) {
    return FeedbackQuestion(
      id: json['id'],
      name: json['name'],
      description: json['description'],
      type: json['type'],
      options: json['options'].cast<String>(),
      rangeLow: json['rangeLow'] ?? "0",
      rangeHigh: json['rangeHigh'] ?? "10",
    );
  }
}
