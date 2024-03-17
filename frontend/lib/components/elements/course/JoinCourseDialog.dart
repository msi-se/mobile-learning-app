import 'package:flutter/material.dart';

class JoinCourseDialog extends StatefulWidget {
  final Function(String courseId) onJoinCourse;

  const JoinCourseDialog({Key? key, required this.onJoinCourse}) : super(key: key);

  @override
  State<JoinCourseDialog> createState() => _JoinCourseDialogState();
}

class _JoinCourseDialogState extends State<JoinCourseDialog> {
  final TextEditingController _controller = TextEditingController();

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: const Text('Kurs beitreten'),
      content: TextField(
        controller: _controller,
        decoration: const InputDecoration(
          hintText: 'Gebe die Kurs-Id ein...',
        ),
      ),
      actions: <Widget>[
        TextButton(
          onPressed: () {
            Navigator.of(context).pop();
          },
          child: const Text('Abbruch', style: TextStyle(color: Colors.red)),
        ),
        TextButton(
          onPressed: () {
            if (_controller.text.isNotEmpty) {
              widget.onJoinCourse(_controller.text);
              Navigator.of(context).pop();
            }
          },
          child: const Text('Beitreten'),
        ),
      ],
    );
  }
}
