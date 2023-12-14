import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:frontend/components/feedback/choose_feedback_channel.dart';
import 'package:frontend/components/feedback/choose_feedback_form.dart';
import 'package:frontend/models/feedback/feedback_course.dart';
import 'package:frontend/utils.dart';
import 'package:http/http.dart' as http;

class ChooseFeedbackPage extends StatefulWidget {
  const ChooseFeedbackPage({super.key});

  @override
  State<ChooseFeedbackPage> createState() => _ChooseFeedbackPageState();
}

class _ChooseFeedbackPageState extends State<ChooseFeedbackPage> {
  late List<FeedbackCourse> _courses;

  FeedbackCourse? _selectedCourse;

  bool _loading = true;

  @override
  void initState() {
    super.initState();

    fetchCourses();
  }

  Future fetchCourses() async {
    try {
      final response =
          await http.get(Uri.parse("${getBackendUrl()}/course"));
      if (response.statusCode == 200) {
        var data = jsonDecode(response.body);
        setState(() {
          _courses = getCoursesFromJson(data);
          _loading = false;
        });
      }
    } on http.ClientException catch (_) {
      // TODO: handle error
    }
  }

  List<FeedbackCourse> getCoursesFromJson(List<dynamic> json) {
    return json.map((e) => FeedbackCourse.fromJson(e)).toList();
  }

  @override
  Widget build(BuildContext context) {
    if (_loading) {
      return const Scaffold(
        body: Center(
          child: CircularProgressIndicator(),
        ),
      );
    }

    return PopScope(
      canPop: false,
      onPopInvoked: (bool didPop) {
        if (didPop) {
          return;
        }
        if (_selectedCourse != null) {
          setState(() {
            _selectedCourse = null;
          });
        } else {
          Navigator.pop(context);
        }
      },
      child: Scaffold(
        appBar: AppBar(
          backgroundColor: Theme.of(context).colorScheme.primary,
          title: Text(
              _selectedCourse != null
                  ? _selectedCourse!.name
                  : "Feedbackbogen auswählen",
              style: const TextStyle(
                  color: Colors.white, fontWeight: FontWeight.bold)),
        ),
        // display _courses in list view with clickable tiles
        body: _selectedCourse == null
            ? ChooseFeedbackCourse(
                courses: _courses,
                choose: (id) {
                  setState(() {
                    _selectedCourse =
                        _courses.firstWhere((element) => element.id == id);
                  });
                },
              )
            : ChooseFeedbackForm(
                course: _selectedCourse!,
                choose: (id) {
                  Navigator.pushNamed(context, '/feedback-info', arguments: {
                    "courseId": _selectedCourse!.id,
                    "formId": id,
                  });
                },
              ),
      ),
    );
  }
}
