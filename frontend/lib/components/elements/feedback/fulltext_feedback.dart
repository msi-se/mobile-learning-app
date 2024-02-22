import 'package:flutter/material.dart';

class FulltextFeedback extends StatefulWidget {
  final ValueChanged<String> onFeedbackChanged;

  const FulltextFeedback({
    super.key,
    required this.onFeedbackChanged,
  });

  @override
  State<FulltextFeedback> createState() => _FulltextFeedbackState();
}

class _FulltextFeedbackState extends State<FulltextFeedback> {
  late String _feedback;

  @override
  void initState() {
    super.initState();
    _feedback = "";
  }

  @override
  Widget build(BuildContext context) {
    return TextField(
      onChanged: (newFeedback) {
        setState(() {
          _feedback = newFeedback;
        });
        widget.onFeedbackChanged(newFeedback);
      },
    );
  }
}
