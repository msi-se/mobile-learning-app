class UserStats {
  final int completedFeedbackForms;
  final int completedQuizForms;
  final int gainedQuizPoints;
  final double avgQuizPosition;

  UserStats({
    required this.completedFeedbackForms,
    required this.completedQuizForms,
    required this.gainedQuizPoints,
    required this.avgQuizPosition,
  });

  UserStats.withDefaultValues()
    : completedFeedbackForms = 0,
      completedQuizForms = 0,
      gainedQuizPoints = 0,
      avgQuizPosition = 0.0;

  factory UserStats.fromJson(Map<String, dynamic> json) {
    return UserStats(
      completedFeedbackForms: json['completedFeedbackForms'] as int,
      completedQuizForms: json['completedQuizForms'] as int,
      gainedQuizPoints: json['gainedQuizPoints'] as int,
      avgQuizPosition: json['avgQuizPosition'] as double,
    );
  }
}
