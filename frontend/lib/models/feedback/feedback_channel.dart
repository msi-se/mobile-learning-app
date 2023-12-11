import 'package:frontend/models/feedback/feedback_form.dart';

class FeedbackChannel {
  final String id;
  final String name;
  final String description;
  final List<FeedbackForm> feedbackForms;

  FeedbackChannel({
    required this.id,
    required this.name,
    required this.description,
    required this.feedbackForms,
  });

  factory FeedbackChannel.fromJson(Map<String, dynamic> json) {
    return FeedbackChannel(
      id: json['id'],
      name: json['name'],
      description: json['description'],
      feedbackForms: (json['feedbackForms'] as List<dynamic>)
          .map((e) => FeedbackForm.fromJson(e))
          .toList(),
    );
  }
}
