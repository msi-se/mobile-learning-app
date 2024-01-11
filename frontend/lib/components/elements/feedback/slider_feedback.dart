import 'package:flutter/material.dart';

class SliderFeedback extends StatefulWidget {
  final int initialFeedback;
  final ValueChanged<int> onFeedbackChanged;

  const SliderFeedback(
      {super.key, this.initialFeedback = 5, required this.onFeedbackChanged});

  @override
  State<SliderFeedback> createState() => _SliderFeedbackState();
}

class _SliderFeedbackState extends State<SliderFeedback> {
  late double _feedback;

  @override
  void initState() {
    super.initState();
    _feedback = widget.initialFeedback.toDouble();
  }

  @override
  Widget build(BuildContext context) {
    return Slider(
      value: _feedback,
      min: 0,
      max: 10,
      divisions: 10,
      onChanged: (newFeedback) {
        setState(() {
          _feedback = newFeedback;
        });
        widget.onFeedbackChanged(newFeedback.toInt());
      },
    );
  }
}
