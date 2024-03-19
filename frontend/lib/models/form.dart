import 'package:frontend/global.dart';
import 'package:frontend/models/question.dart';

class Form {
  final String id;
  final String courseId;
  final String name;
  final String description;
  final String connectCode;
  final List<Question> questions;
  final bool isOwner;
  String status;

  Form({
    required this.id,
    required this.courseId,
    required this.name,
    required this.description,
    required this.connectCode,
    required this.questions,
    required this.isOwner,
    required this.status,
  });

  factory Form.fromJson(Map<String, dynamic> json, {bool isOwner = false}) {
    return Form(
      id: json['id'],
      courseId: json['courseId'],
      name: json['name'],
      description: json['description'],
      connectCode: (json['connectCode'] as int).toString(),
      questions: json['questions'] == null
          ? []
          : (json['questions'] as List<dynamic>)
              .map((e) => Question.fromJson(e['questionContent']))
              .toList(),
      isOwner: isOwner,
      status: json['status'],
    );
  }
}
