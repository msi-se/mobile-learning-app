import 'package:flutter/material.dart';

class YesNoQuiz extends StatefulWidget {
  final ValueChanged<String> onSelectionChanged;

  const YesNoQuiz({
    super.key,
    required this.onSelectionChanged,
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
                color: _selection == 0 ? colors.primary : Colors.grey,
                width: _selection == 0 ? 2.0 : 1.0,
              ),
            ),
          ),
          onPressed: () {
            setState(() {
              _selection = 0;
            });
            widget.onSelectionChanged("Ja");
          },
          child: Row(
            children: [
              Container(
                width: 30.0,
                height: 30.0,
                decoration: BoxDecoration(
                  color: _selection == 0 ? Colors.green : Colors.grey,
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
                color: _selection == 1 ? colors.primary : Colors.grey,
                width: _selection == 1 ? 2.0 : 1.0,
              ),
            ),
          ),
          onPressed: () {
            setState(() {
              _selection = 1;
            });
            widget.onSelectionChanged("Nein");
          },
          child: Row(
            children: [
              Container(
                width: 30.0,
                height: 30.0,
                decoration: BoxDecoration(
                  color: _selection == 1 ? Colors.red : Colors.grey,
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
