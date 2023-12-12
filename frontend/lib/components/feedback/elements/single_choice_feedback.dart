import 'package:flutter/material.dart';


class SingleChoiceFeedback extends StatefulWidget {
  final int initialFeedback;
  final ValueChanged<int> onFeedbackChanged;
  final List<String> options;

  const SingleChoiceFeedback({
    super.key,
    this.initialFeedback = 0,
    required this.onFeedbackChanged,
    required this.options
  });

  @override
  State<SingleChoiceFeedback> createState() => _SingleChoiceFeedbackState();
}

class _SingleChoiceFeedbackState extends State<SingleChoiceFeedback> {
  late int _feedback;
  late List<String> _options;
  late List<DropdownMenuItem> _items;

  @override
  void initState() {
    super.initState();
    _feedback = 0;
    _options = widget.options;
    _items = [];

    for (var i = 0; i < _options.length; i++) {
      _items.add(DropdownMenuItem(
        value: i,
        child: Text(_options[i]),
      ));
    }
  }

  @override
  Widget build(BuildContext context) {
    return DropdownButton(
      value: _feedback,
      items: _items,
      onChanged: (newFeedback) {
        setState(() {
          _feedback = newFeedback;
        });
        widget.onFeedbackChanged(newFeedback.toInt());
      },
    );
  }
}
