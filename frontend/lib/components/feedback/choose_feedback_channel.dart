import 'package:flutter/material.dart';
import 'package:frontend/models/feedback/feedback_course.dart';

class ChooseFeedbackCourse extends StatefulWidget {
  final List<FeedbackCourse> courses;
  final Function(String id) choose;

  const ChooseFeedbackCourse({super.key, required this.courses, required this.choose});

  @override
  State<ChooseFeedbackCourse> createState() => _ChooseFeedbackCourseState();
}

class _ChooseFeedbackCourseState extends State<ChooseFeedbackCourse> {


  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: ListView.builder(
        itemCount: widget.courses.length,
        itemBuilder: (context, index) {
          return ListTile(
            title: Text(widget.courses[index].name),
            subtitle: Text(widget.courses[index].description),
            trailing: const Icon(Icons.arrow_forward_ios),
            onTap: () {
              widget.choose(widget.courses[index].id);
            },
          );
        },
      ),
    );
  }
}
