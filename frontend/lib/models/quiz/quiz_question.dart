import 'package:frontend/models/question.dart';

class QuizQuestion extends Question {
  
  QuizQuestion(
      {required super.id,
      required super.name,
      required super.description,
      required super.type,
      required super.options});

  factory QuizQuestion.fromJson(Map<String, dynamic> json) {
    return QuizQuestion(
      id: json['id'],
      name: json['name'],
      description: json['description'],
      type: json['type'],
      options: json['options'].cast<String>(),
    );
  }
}
