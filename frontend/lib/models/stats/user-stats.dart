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

  factory UserStats.fromJson(Map<String, dynamic> json) {
    return UserStats(
      completedFeedbackForms: json['completedFeedbackForms'] as int,
      completedQuizForms: json['completedQuizForms'] as int,
      qainedQuizPoints: json['qainedQuizPoints'] as int,
      avgQuizPosition: json['avgQuizPosition'] as double,
    );
  }
}
