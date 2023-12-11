class FeedbackElement {
  final String id;
  final String name;
  final String description;
  final String type;

  FeedbackElement({
    required this.id,
    required this.name,
    required this.description,
    required this.type,
  });

  factory FeedbackElement.fromJson(Map<String, dynamic> json) {
    return FeedbackElement(
      id: json['id'],
      name: json['name'],
      description: json['description'],
      type: json['type'],
    );
  }
}
