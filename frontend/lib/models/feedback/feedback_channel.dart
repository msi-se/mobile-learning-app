import 'package:frontend/models/feedback/feedback_form.dart';

class FeedBackChannel {
  final String id;
  final String name;
  final String description;
  final List<FeedBackForm> feedbackForms;

  FeedBackChannel(
      {required this.id,
      required this.name,
      required this.description,
      required this.feedbackForms});

  factory FeedBackChannel.fromJson(Map<String, dynamic> json) {
    return FeedBackChannel(
      id: json['id'],
      name: json['name'],
      description: json['description'],
      feedbackForms: (json['feedbackForms'] as List<dynamic>)
          .map((e) => FeedBackForm.fromJson(e))
          .toList(),
    );
  }
}
