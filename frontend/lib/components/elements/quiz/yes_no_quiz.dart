import 'package:flutter/material.dart';

class YesNoQuiz extends StatefulWidget {
  final ValueChanged<String> onSelectionChanged;
  final List<dynamic> correctAnswers;

  final bool voted;
  final dynamic value;

  const YesNoQuiz({
    super.key,
    required this.onSelectionChanged,
    required this.voted,
    required this.value,
    required this.correctAnswers,
  });

  @override
  State<YesNoQuiz> createState() => _YesNoQuizState();
}

class _YesNoQuizState extends State<YesNoQuiz> {
  int _selection = -1;

  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;

    if (widget.value == null) {
      _selection = -1;
    }
    if (widget.correctAnswers.isEmpty) {
      return Row(
        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
        children: [
          ElevatedButton(
            style: ElevatedButton.styleFrom(
              padding: const EdgeInsets.only(
                  top: 8.0, bottom: 8.0, left: 16.0, right: 16.0),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(10.0),
                side: BorderSide(
                  color: _selection == 1 ? colors.primary : Colors.grey,
                  width: _selection == 1 ? 2.0 : 1.0,
                ),
              ),
            ),
            onPressed: () {
              if (!widget.voted) {
                setState(() {
                  _selection = 1;
                });
                widget.onSelectionChanged("yes");
              }
            },
            child: Row(
              children: [
                Container(
                  width: 30.0,
                  height: 30.0,
                  decoration: BoxDecoration(
                    color: _selection == 1 ? colors.primary : Colors.grey,
                    shape: BoxShape.circle,
                  ),
                  child: const Center(
                      child: Icon(
                    Icons.check,
                    color: Colors.white,
                    size: 22,
                  )),
                ),
                const SizedBox(width: 16),
                Text(
                  "Ja",
                  style: TextStyle(
                    color: colors.onBackground,
                  ),
                ),
              ],
            ),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(
              padding: const EdgeInsets.only(
                  top: 8.0, bottom: 8.0, left: 16.0, right: 16.0),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(10.0),
                side: BorderSide(
                  color: _selection == 0 ? colors.primary : Colors.grey,
                  width: _selection == 0 ? 2.0 : 1.0,
                ),
              ),
            ),
            onPressed: () {
              if (!widget.voted) {
                setState(() {
                  _selection = 0;
                });
                widget.onSelectionChanged("no");
              }
            },
            child: Row(
              children: [
                Container(
                  width: 30.0,
                  height: 30.0,
                  decoration: BoxDecoration(
                    color: _selection == 0 ? colors.primary : Colors.grey,
                    shape: BoxShape.circle,
                  ),
                  child: const Center(
                      child: Icon(
                    Icons.close,
                    color: Colors.white,
                    size: 22,
                  )),
                ),
                const SizedBox(width: 16),
                Text(
                  "Nein",
                  style: TextStyle(
                    color: colors.onBackground,
                  ),
                ),
              ],
            ),
          ),
        ],
      );
    } else {
      return Row(
        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
        children: [
          ElevatedButton(
            style: ElevatedButton.styleFrom(
              padding: const EdgeInsets.only(
                  top: 8.0, bottom: 8.0, left: 16.0, right: 16.0),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(10.0),
                side: BorderSide(
                  color: widget.correctAnswers[0] == "yes"
                      ? Colors.green
                      : Colors.red,
                  width: _selection == 1 ? 3.0 : 1.0,
                ),
              ),
            ),
            onPressed: () {
              if (!widget.voted) {
                setState(() {
                  _selection = 1;
                });
                widget.onSelectionChanged("yes");
              }
            },
            child: Row(
              children: [
                Container(
                  width: 30.0,
                  height: 30.0,
                  decoration: BoxDecoration(
                    color: widget.correctAnswers[0] == "yes"
                        ? Colors.green
                        : Colors.red,
                    shape: BoxShape.circle,
                  ),
                  child: const Center(
                      child: Icon(
                    Icons.check,
                    color: Colors.white,
                    size: 22,
                  )),
                ),
                const SizedBox(width: 16),
                Text(
                  "Ja",
                  style: TextStyle(
                    color: colors.onBackground,
                  ),
                ),
              ],
            ),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(
              padding: const EdgeInsets.only(
                  top: 8.0, bottom: 8.0, left: 16.0, right: 16.0),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(10.0),
                side: BorderSide(
                  color: widget.correctAnswers[0] == "no"
                      ? Colors.green
                      : Colors.red,
                  width: _selection == 0 ? 3.0 : 1.0,
                ),
              ),
            ),
            onPressed: () {
              if (!widget.voted) {
                setState(() {
                  _selection = 0;
                });
                widget.onSelectionChanged("no");
              }
            },
            child: Row(
              children: [
                Container(
                  width: 30.0,
                  height: 30.0,
                  decoration: BoxDecoration(
                    color: widget.correctAnswers[0] == "no"
                        ? Colors.green
                        : Colors.red,
                    shape: BoxShape.circle,
                  ),
                  child: const Center(
                      child: Icon(
                    Icons.close,
                    color: Colors.white,
                    size: 22,
                  )),
                ),
                const SizedBox(width: 16),
                Text(
                  "Nein",
                  style: TextStyle(
                    color: colors.onBackground,
                  ),
                ),
              ],
            ),
          ),
        ],
      );
    }
  }
}
