import 'dart:convert';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:frontend/components/choose/choose_course.dart';
import 'package:frontend/components/choose/choose_form.dart';
import 'package:frontend/components/error/general_error_widget.dart';
import 'package:frontend/components/error/network_error_widget.dart';
import 'package:frontend/models/course.dart';
import 'package:frontend/utils.dart';
import 'package:http/http.dart' as http;
import 'package:frontend/global.dart';

class CoursesTab extends StatefulWidget {
  final Function(Function?) setPopFunction;

  const CoursesTab({super.key, required this.setPopFunction});

  @override
  State<CoursesTab> createState() => _CoursesTabState();
}

class _CoursesTabState extends State<CoursesTab> {
  late List<Course> _courses;

  Course? _selectedCourse;

  late Future<String> _future;

  @override
  void initState() {
    super.initState();
    _future = fetchCourses();
  }

  Future<String> fetchCourses() async {
    try {
      //throw Error();
      //throw http.ClientException('rofl');
      final response = await http.get(
        Uri.parse(
            "${getBackendUrl()}/course?password=${getSession()!.password}"),
        headers: {
          "Content-Type": "application/json",
          "AUTHORIZATION": "Bearer ${getSession()!.jwt}",
        },
      );
      if (response.statusCode == 200) {
        var data = jsonDecode(response.body);
        setState(() {
          _courses = getCoursesFromJson(data);
        });
        return 'success';
      }
    } on http.ClientException {
      return 'network_error';
    } on SocketException {
      return 'network_error';
    } catch (e) {
      return 'general_error';
    }
    return 'general_error';
  }

  void _showErrorDialog(String errorType) {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      showDialog(
        context: context,
        barrierDismissible: false,
        builder: (BuildContext context) {
          return (errorType == 'network')
              ? const NetworkErrorWidget()
              : const GeneralErrorWidget();
        },
      ).then((value) {
        if (value == 'back') {
          Navigator.pushNamed(context, '/main');
        }
      });
    });
  }

  List<Course> getCoursesFromJson(List<dynamic> json) {
    return json.map((e) => Course.fromJson(e)).toList();
  }

  void pop() {
    if (_selectedCourse != null) {
      setState(() {
        _selectedCourse = null;
      });
    }
    widget.setPopFunction(null);
  }

  @override
  Widget build(BuildContext context) {
    return FutureBuilder(
      future: _future,
      builder: (context, AsyncSnapshot<String> snapshot) {
        if (snapshot.connectionState == ConnectionState.done) {
          String? result = snapshot.data;
          if (result == 'network_error') {
            _showErrorDialog('network');
          } else if (result == 'general_error') {
            _showErrorDialog('general');
          } else {
            return PopScope(
              canPop: _selectedCourse == null,
              onPopInvoked: (bool didPop) {
                if (didPop) {
                  return;
                }
                pop();
              },
              child: _selectedCourse == null
                  ? ChooseCourse(
                      courses: _courses,
                      choose: (id) {
                        setState(() {
                          _selectedCourse = _courses
                              .firstWhere((element) => element.id == id);
                        });
                        widget.setPopFunction(pop);
                      },
                    )
                  : ChooseForm(
                      course: _selectedCourse!,
                      choose: (id, feedbackOrQuiz) {
                        if (feedbackOrQuiz == "Feedback") {
                          Navigator.pushNamed(context, '/feedback-info',
                              arguments: {
                                "courseId": _selectedCourse!.id,
                                "formId": id,
                              });
                        } else if (feedbackOrQuiz == "Quiz") {
                          Navigator.pushNamed(context, '/quiz-info',
                              arguments: {
                                "courseId": _selectedCourse!.id,
                                "formId": id,
                              });
                        }
                      },
                    ),
            );
          }
        }
        return const Center(
          child: CircularProgressIndicator(),
        );
      },
    );
  }
}
