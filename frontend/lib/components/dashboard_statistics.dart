import 'dart:convert';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:frontend/global.dart';
import 'package:frontend/models/stats/global-stats.dart';
import 'package:frontend/models/stats/stats.dart';
import 'package:frontend/models/stats/user-stats.dart';
import 'package:frontend/utils.dart';
import 'package:http/http.dart' as http;

class DashboardStatisticsWidget extends StatefulWidget {
  const DashboardStatisticsWidget({Key? key}) : super(key: key);

  @override
  State<DashboardStatisticsWidget> createState() =>
      _DashboardStatisticsWidgetState();
}

class _DashboardStatisticsWidgetState extends State<DashboardStatisticsWidget>
    with TickerProviderStateMixin {
  late AnimationController _animationController;
  late Animation<double> _opacityAnimation;
  late Animation<Offset> _positionAnimation;
  late Stats _stats;

  @override
  void initState() {
    super.initState();
    _animationController = AnimationController(
      duration: const Duration(milliseconds: 300),
      vsync: this,
    );

    _opacityAnimation = Tween<double>(begin: 0, end: 1).animate(
      CurvedAnimation(parent: _animationController, curve: Curves.easeInOut),
    );

    _positionAnimation =
        Tween<Offset>(begin: const Offset(0, 0.5), end: Offset.zero).animate(
      CurvedAnimation(parent: _animationController, curve: Curves.easeInOut),
    );

    _animationController.forward();

    _stats = Stats(
        globalStats: GlobalStats(
            // totalFeedbackForms: 0,
            // totalQuizForms: 0,
            // totalCourses: 0,
            // totalUsers: 0,
            completedFeedbackForms: 0,
            completedQuizForms: 0,
            id: ''),
        userStats: UserStats(
          avgQuizPosition: 0,
          completedFeedbackForms: 0,
          completedQuizForms: 0,
          qainedQuizPoints: 0,
        ));

    fetchStats();
  }

  @override
  void dispose() {
    _animationController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsetsDirectional.fromSTEB(4, 0, 4, 12),
      child: FadeTransition(
        opacity: _opacityAnimation,
        child: SlideTransition(
          position: _positionAnimation,
          child: Container(
            constraints: const BoxConstraints(
              minWidth: 350,
              maxWidth: double.infinity,
            ),
            decoration: BoxDecoration(
              color: Colors.white,
              boxShadow: const [
                BoxShadow(
                  blurRadius: 4,
                  color: Color(0x33000000),
                  offset: Offset(0, 2),
                ),
              ],
              borderRadius: BorderRadius.circular(12),
            ),
            child: Padding(
              padding: const EdgeInsetsDirectional.fromSTEB(0, 0, 0, 8),
              child: Column(
                mainAxisSize: MainAxisSize.max,
                children: [
                  const Padding(
                    padding: EdgeInsetsDirectional.fromSTEB(0, 0, 12, 0),
                    child: Row(
                      mainAxisSize: MainAxisSize.max,
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Padding(
                          padding:
                              EdgeInsetsDirectional.fromSTEB(16, 12, 12, 0),
                          child: Column(
                            mainAxisSize: MainAxisSize.max,
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                'Deine Statistiken',
                                style: TextStyle(
                                  color: Color(0xFF14181B),
                                  fontSize: 24,
                                  fontWeight: FontWeight.w600,
                                ),
                              ),
                            ],
                          ),
                        ),
                      ],
                    ),
                  ),
                  Padding(
                    padding:
                        const EdgeInsetsDirectional.fromSTEB(16, 12, 16, 12),
                    child: Row(
                      mainAxisSize: MainAxisSize.max,
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Expanded(
                          child: Column(
                            mainAxisSize: MainAxisSize.max,
                            crossAxisAlignment: CrossAxisAlignment.center,
                            children: [
                              Text(
                                _stats.userStats.completedFeedbackForms
                                    .toString(),
                                style: const TextStyle(
                                  color: Color(0xFF14181B),
                                  fontSize: 36,
                                  fontWeight: FontWeight.w600,
                                ),
                              ),
                              const Padding(
                                padding:
                                    EdgeInsetsDirectional.fromSTEB(0, 4, 0, 0),
                                child: Text(
                                  'Absolvierte Feedbacks',
                                  style: TextStyle(
                                    color: Color(0xFF57636C),
                                    fontSize: 12,
                                    fontWeight: FontWeight.w500,
                                  ),
                                ),
                              ),
                            ],
                          ),
                        ),
                        Expanded(
                          child: Column(
                            mainAxisSize: MainAxisSize.max,
                            crossAxisAlignment: CrossAxisAlignment.center,
                            children: [
                              Text(
                                _stats.userStats.qainedQuizPoints.toString(),
                                style: const TextStyle(
                                  color: Color(0xFF14181B),
                                  fontSize: 36,
                                  fontWeight: FontWeight.w600,
                                ),
                              ),
                              const Padding(
                                padding:
                                    EdgeInsetsDirectional.fromSTEB(0, 4, 0, 0),
                                child: Text(
                                  'Gesammelte Punkte',
                                  style: TextStyle(
                                    color: Color(0xFF57636C),
                                    fontSize: 12,
                                    fontWeight: FontWeight.w500,
                                  ),
                                ),
                              ),
                            ],
                          ),
                        ),
                      ],
                    ),
                  ),
                  Padding(
                    padding:
                        const EdgeInsetsDirectional.fromSTEB(16, 12, 16, 12),
                    child: Row(
                      mainAxisSize: MainAxisSize.max,
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Expanded(
                          child: Column(
                            mainAxisSize: MainAxisSize.max,
                            crossAxisAlignment: CrossAxisAlignment.center,
                            children: [
                              Text(
                                _stats.userStats.completedQuizForms.toString(),
                                style: const TextStyle(
                                  color: Color(0xFF14181B),
                                  fontSize: 36,
                                  fontWeight: FontWeight.w600,
                                ),
                              ),
                              const Padding(
                                padding:
                                    EdgeInsetsDirectional.fromSTEB(0, 4, 0, 0),
                                child: Text(
                                  'Absolvierte Quizze',
                                  style: TextStyle(
                                    color: Color(0xFF57636C),
                                    fontSize: 12,
                                    fontWeight: FontWeight.w500,
                                  ),
                                ),
                              ),
                            ],
                          ),
                        ),
                        Expanded(
                          child: Column(
                            mainAxisSize: MainAxisSize.max,
                            crossAxisAlignment: CrossAxisAlignment.center,
                            children: [
                              Text(
                                _stats.userStats.avgQuizPosition.toString(),
                                style: const TextStyle(
                                  color: Color(0xFF14181B),
                                  fontSize: 36,
                                  fontWeight: FontWeight.w600,
                                ),
                              ),
                              const Padding(
                                padding:
                                    EdgeInsetsDirectional.fromSTEB(0, 4, 0, 0),
                                child: Text(
                                  '⌀-Quiz-Position',
                                  style: TextStyle(
                                    color: Color(0xFF57636C),
                                    fontSize: 12,
                                    fontWeight: FontWeight.w500,
                                  ),
                                ),
                              ),
                            ],
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }

  Future fetchStats() async {
    try {
      if (getSession() == null) {
        await initPreferences();
      }
      final response = await http.get(
        Uri.parse("${getBackendUrl()}/stats"),
        headers: {
          "Content-Type": "application/json",
          "AUTHORIZATION": "Bearer ${getSession()!.jwt}",
        },
      );
      if (!mounted) return;
      if (response.statusCode == 200) {
        var data = jsonDecode(response.body);
        setState(() {
          _stats = Stats.fromJson(data);
        });
      }
    } on http.ClientException catch (_) {
      // TODO: handle error
    }
  }
}
