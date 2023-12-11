import 'package:frontend/models/feedback/feedback_element.dart';

class FeedbackForm {
  final String id;
  final String name;
  final String description;
  final String connectCode;
  final String status;
  final List<FeedbackElement> feedbackElements;

  FeedbackForm({
    required this.id,
    required this.name,
    required this.description,
    required this.connectCode,
    required this.status,
    required this.feedbackElements,
  });

  factory FeedbackForm.fromJson(Map<String, dynamic> json) {
    return FeedbackForm(
      id: json['id'],
      name: json['name'],
      description: json['description'],
      connectCode: (json['connectCode'] as int).toString(),
      status: json['status'],
      feedbackElements: (json['elements'] as List<dynamic>)
          .map((e) => FeedbackElement.fromJson(e))
          .toList(),
    );
  }
}