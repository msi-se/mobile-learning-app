import 'package:flutter/material.dart';

class SingleChoiceQuiz extends StatefulWidget {
  final int initialQuiz;
  final ValueChanged<int> onQuizChanged;
  final List<String> options;

  const SingleChoiceQuiz({
    super.key,
    this.initialQuiz = 0,
    required this.onQuizChanged,
    required this.options,
  });

  @override
  State<SingleChoiceQuiz> createState() => _SingleChoiceQuizState();
}

class _SingleChoiceQuizState extends State<SingleChoiceQuiz> {
  late int _quiz;
  late List<String> _options;

  @override
  void initState() {
    super.initState();
    _quiz = widget.initialQuiz;
    _options = widget.options;
  }

  @override
  Widget build(BuildContext context) {
    return ListView.builder(
      shrinkWrap: true,
      physics: NeverScrollableScrollPhysics(),
      itemCount: _options.length,
      itemBuilder: (BuildContext context, int index) {
        String letter = String.fromCharCode(65 + index);
        return Card(
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(10.0),
          ),
          elevation: 1.0,
          surfaceTintColor: Colors.white,
          child: ListTile(
            title: Text('$letter: ${_options[index]}'),
            onTap: () {
              setState(() {
                _quiz = index;
              });
              widget.onQuizChanged(index);
            },
          ),
        );
      },
    );
  }
}
