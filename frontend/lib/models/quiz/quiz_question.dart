import 'package:frontend/enums/question_type.dart';
import 'package:frontend/models/question.dart';

class QuizQuestion extends Question {
  final List<String> correctAnswers;

  QuizQuestion(
      {required super.id,
      required super.name,
      required super.description,
      required super.type,
      required super.options,
      required this.correctAnswers});

  factory QuizQuestion.fromJson(Map<String, dynamic> json) {
    return QuizQuestion(
      id: json['id'],
      name: json['name'],
      description: json['description'],
      type: QuestionType.fromString(json['type']),
      options: json['options'].cast<String>(),
      correctAnswers: json['correctAnswers'] == null
          ? []
          : json['correctAnswers'].cast<String>(),
    );
  }
}
