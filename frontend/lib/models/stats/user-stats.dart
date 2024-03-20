class UserStats {
  final int completedFeedbackForms;
  final int completedQuizForms;
  final int qainedQuizPoints;
  final double avgQuizPosition;

  UserStats({
    required this.completedFeedbackForms,
    required this.completedQuizForms,
    required this.qainedQuizPoints,
    required this.avgQuizPosition,
  });

  UserStats.withDefaultValues()
    : completedFeedbackForms = 0,
      completedQuizForms = 0,
      qainedQuizPoints = 0,
      avgQuizPosition = 0.0;

  factory UserStats.fromJson(Map<String, dynamic> json) {
    return UserStats(
      completedFeedbackForms: json['completedFeedbackForms'] as int,
      completedQuizForms: json['completedQuizForms'] as int,
      qainedQuizPoints: json['gainedQuizPoints'] as int,
      avgQuizPosition: json['avgQuizPosition'] as double,
    );
  }
}
