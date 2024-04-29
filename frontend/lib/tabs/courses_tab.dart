import 'dart:convert';
import 'dart:io';
import 'package:flutter/material.dart';
import 'package:frontend/components/choose/choose_course.dart';
import 'package:frontend/components/choose/choose_form.dart';
import 'package:frontend/components/elements/course/JoinCourseDialog.dart';
import 'package:frontend/components/error/general_error_widget.dart';
import 'package:frontend/components/error/network_error_widget.dart';
import 'package:frontend/models/course.dart';
import 'package:frontend/models/feedback/feedback_form.dart';
import 'package:frontend/models/quiz/quiz_form.dart';
import 'package:frontend/enums/form_type.dart';
import 'package:frontend/utils.dart';
import 'package:http/http.dart' as http;
import 'package:frontend/global.dart';
import 'package:rive/rive.dart';

class CoursesTab extends StatefulWidget {
  final Function(Function?) setPopFunction;

  const CoursesTab({super.key, required this.setPopFunction});

  @override
  State<CoursesTab> createState() => _CoursesTabState();
}

class _CoursesTabState extends State<CoursesTab> {
  late List<Course> _courses;

  Course? _selectedCourse;

  bool _loading = false;
  String _fetchResult = '';

  @override
  void initState() {
    super.initState();
    fetchCourses();
  }

  Future fetchCourses() async {
    setState(() {
      _loading = true;
      _fetchResult = '';
    });
    try {
      final response = await http.get(
        Uri.parse("${getBackendUrl()}/course"),
        headers: {
          "Content-Type": "application/json",
          "AUTHORIZATION": "Bearer ${getSession()!.jwt}",
        },
      );
      if (response.statusCode == 200) {
        var data = jsonDecode(response.body);
        setState(() {
          _courses = getCoursesFromJson(data);
          _loading = false;
          _fetchResult = 'success';
        });
      }
    } on http.ClientException {
      setState(() {
        _loading = false;
        _fetchResult = 'network_error';
      });
    } on SocketException {
      setState(() {
        _loading = false;
        _fetchResult = 'network_error';
      });
    }
  }

  Future<void> joinCourse(String courseId) async {
    setState(() {
      _loading = true;
    });

    try {
      final response = await http.post(
        Uri.parse("${getBackendUrl()}/course/$courseId/join"),
        headers: {
          "Content-Type": "application/json",
          "AUTHORIZATION": "Bearer ${getSession()!.jwt}",
        },
      );

      if (response.statusCode == 200) {
        await fetchCourses();
      } else {
        // TODO: Handle different status codes other than 200
        setState(() {
          _loading = false;
        });
      }
    } on http.ClientException {
      setState(() {
        _loading = false;
        _fetchResult = 'network_error';
      });
    } on SocketException {
      setState(() {
        _loading = false;
        _fetchResult = 'network_error';
      });
    } catch (e) {
      setState(() {
        _loading = false;
        _fetchResult = 'general_error';
      });
    }
  }

  void _showErrorDialog(String errorType) {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      showDialog(
          context: context,
          barrierDismissible: false,
          builder: (BuildContext context) {
            return (errorType == 'network_error')
                ? const NetworkErrorWidget()
                : const GeneralErrorWidget();
          }).then((value) {
        if (value == 'back') {
          Navigator.pushReplacementNamed(context, '/main');
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
    if (_loading) {
      return const Center(
        child: CircularProgressIndicator(),
      );
    } else if (_fetchResult == 'success') {
      if (_courses.isEmpty) {
        return Center(
            child:
                Column(mainAxisAlignment: MainAxisAlignment.center, children: [
          Text('Keine Kurse gefunden!'),
          Container(
            margin: EdgeInsets.only(top: 20.0, bottom: 100.0),
            width: 250,
            height: 250,
            child: RiveAnimation.asset(
              'assets/animations/rive/animations.riv',
              fit: BoxFit.cover,
              artboard: 'Sleeping Mascot',
              stateMachines: ['Sleeping State Machine'],
            ),
          ),
          Positioned(
            right: 16,
            bottom: 16,
            child: FloatingActionButton(
              backgroundColor: Theme.of(context).colorScheme.primary,
              onPressed: () {
                showDialog(
                  context: context,
                  builder: (BuildContext context) {
                    return JoinCourseDialog(
                      onJoinCourse: joinCourse,
                    );
                  },
                );
              },
              child: const Icon(Icons.add,
                  color: Color.fromARGB(255, 255, 255, 255)),
            ),
          ),
        ]));
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
                      _selectedCourse =
                          _courses.firstWhere((element) => element.id == id);
                    });
                    widget.setPopFunction(pop);
                  },
                  joinCourse: joinCourse,
                )
              : ChooseForm(
                  course: _selectedCourse!,
                  choose: (id, formType) {
                    if (formType == FormType.feedback) {
                      if (_selectedCourse!.isOwner) {
                        Navigator.pushNamed(context, '/feedback-info',
                            arguments: {
                              "courseId": _selectedCourse!.id,
                              "formId": id,
                            });
                      } else {
                        FeedbackForm form = _selectedCourse!.feedbackForms
                            .firstWhere((element) => element.id == id);
                        Navigator.pushNamed(context, '/attend-feedback',
                            arguments: form.connectCode);
                      }
                    } else if (formType == FormType.quiz) {
                      if (_selectedCourse!.isOwner) {
                        Navigator.pushNamed(context, '/quiz-info', arguments: {
                          "courseId": _selectedCourse!.id,
                          "formId": id,
                        });
                      } else {
                        QuizForm form = _selectedCourse!.quizForms
                            .firstWhere((element) => element.id == id);
                        Navigator.pushNamed(context, '/attend-quiz',
                            arguments: form.connectCode);
                      }
                    }
                  },
                ),
        );
      }
    } else {
      _showErrorDialog(_fetchResult);
      return Container();
    }
  }
}
