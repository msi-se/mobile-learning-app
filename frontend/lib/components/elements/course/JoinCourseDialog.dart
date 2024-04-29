import 'package:flutter/material.dart';
import 'package:frontend/components/basicButton.dart';
import 'package:frontend/components/textfield.dart';

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
      title: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          const Text('Kurs beitreten'),
          IconButton(
            icon: const Icon(Icons.close),
            onPressed: () => Navigator.of(context).pop(),
          ),
        ],
      ),
      content: MyTextField(
        controller: _controller,
        hintText: 'Gebe die Kurs-Id ein...',
        obscureText: false,
      ),
      actions: <Widget>[
        Center(
          child: BasicButton(
            type: ButtonType.primary,
            text: "Beitreten",
            onPressed: () {
              if (_controller.text.isNotEmpty) {
                widget.onJoinCourse(_controller.text);
                Navigator.of(context).pop();
              }
            },
          ),
        ),
      ],
    );
  }
}
