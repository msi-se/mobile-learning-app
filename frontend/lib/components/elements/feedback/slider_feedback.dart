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
  late bool _hasChanged;

  @override
  void initState() {
    super.initState();
    _feedback = widget.initialFeedback.toDouble();
    _hasChanged = false;
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    var activeColor = colors.primary;
    if (_hasChanged) {
      if (_feedback == 5) {
        activeColor = Colors.orange;
      } else if (_feedback < 5) {
        activeColor = Color.lerp(Colors.red, Colors.orange, _feedback / 5)!;
      } else {
        activeColor = Color.lerp(const Color.fromARGB(255, 212, 217, 62),
            const Color.fromARGB(255, 0, 228, 95), (_feedback - 5) / 5)!;
      }
    }

    return Slider(
      value: _feedback,
      min: 0,
      max: 10,
      divisions: 10,
      onChanged: (newFeedback) {
        setState(() {
          _feedback = newFeedback;
          _hasChanged = true;
        });
        widget.onFeedbackChanged(newFeedback.toInt());
      },
      activeColor: activeColor,
      label: _feedback.toInt().toString(),
    );
  }
}
