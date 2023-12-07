class FeedBackForm {
  final String id;
  final String name;
  final String description;

  FeedBackForm({
    required this.id,
    required this.name,
    required this.description
  });

  factory FeedBackForm.fromJson(Map<String, dynamic> json) {
    return FeedBackForm(
      id: json['id'],
      name: json['name'],
      description: json['description'],
    );
  }
}