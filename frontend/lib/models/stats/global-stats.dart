class GlobalStats {
  final String id;
  // final int totalUsers;
  // final int totalCourses;
  // final int totalFeedbackForms;
  // final int totalQuizForms;
  final int completedFeedbackForms;
  final int completedQuizForms;

  GlobalStats({
    required this.id,
    // required this.totalUsers,
    // required this.totalCourses,
    // required this.totalFeedbackForms,
    // required this.totalQuizForms,
    required this.completedFeedbackForms,
    required this.completedQuizForms,
  });

  factory GlobalStats.fromJson(Map<String, dynamic> json) {
    return GlobalStats(
      id: json['id'],
      // totalUsers: json['totalUsers'] as int,
      // totalCourses: json['totalCourses'] as int,
      // totalFeedbackForms: json['totalFeedbackForms'] as int,
      // totalQuizForms: json['totalQuizForms'] as int,
      completedFeedbackForms: json['completedFeedbackForms'] as int,
      completedQuizForms: json['completedQuizForms'] as int,
    );
  }
}
