import 'package:frontend/models/question.dart';

class Form {
  final String id;
  final String courseId;
  final String name;
  final String description;
  final String connectCode;
  final String status;
  final List<Question> questions;

  Form({
    required this.id,
    required this.courseId,
    required this.name,
    required this.description,
    required this.connectCode,
    required this.status,
    required this.questions,
  });

  factory Form.fromJson(Map<String, dynamic> json) {
    return Form(
      id: json['id'],
      courseId: json['courseId'],
      name: json['name'],
      description: json['description'],
      connectCode: (json['connectCode'] as int).toString(),
      status: json['status'],
      questions: json['questions'] == null
          ? []
          : (json['questions'] as List<dynamic>)
              .map((e) => Question.fromJson(e['questionContent']))
              .toList(),
    );
  }
}
