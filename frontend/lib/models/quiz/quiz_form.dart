import 'package:frontend/models/form.dart';
import 'package:frontend/models/quiz/quiz_question.dart';

class QuizForm extends Form {

  int currentQuestionIndex;
  bool currentQuestionFinished;

  QuizForm({
    required super.id,
    required super.courseId,
    required super.name,
    required super.description,
    required super.connectCode,
    required super.questions,
    required super.isOwner,
    required super.status,
    required this.currentQuestionIndex,
    required this.currentQuestionFinished,
  });

  factory QuizForm.fromJson(Map<String, dynamic> json, {bool isOwner = false}) {
    return QuizForm(
      id: json['id'],
      courseId: json['courseId'],
      name: json['name'],
      description: json['description'],
      connectCode: (json['connectCode'] as int).toString(),
      questions: json['questions'] == null
          ? []
          : (json['questions'] as List<dynamic>)
              .map((e) => QuizQuestion.fromJson(e['questionContent']))
              .toList(),
      isOwner: isOwner,
      status: json['status'],
      currentQuestionIndex: json['currentQuestionIndex'] ?? 0,
      currentQuestionFinished: json['currentQuestionFinished'] ?? false,
    );
  }
}
