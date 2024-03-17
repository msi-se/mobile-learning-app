

import 'package:frontend/models/stats/global-stats.dart';
import 'package:frontend/models/stats/user-stats.dart';

class Stats {
  final GlobalStats globalStats;
  final UserStats userStats;

  Stats({
    required this.globalStats,
    required this.userStats,
  });

  factory Stats.fromJson(Map<String, dynamic> json) {
    return Stats(
      globalStats: GlobalStats.fromJson(json['globalStats']),
      userStats: json['userStats'] != null ? UserStats.fromJson(json['userStats']) : UserStats.withDefaultValues(),
    );
  }
}
