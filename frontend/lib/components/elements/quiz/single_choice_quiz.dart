import 'package:flutter/material.dart';

class SingleChoiceQuiz extends StatefulWidget {
  final ValueChanged<int> onSelectionChanged;
  final List<String> options;

  const SingleChoiceQuiz({
    super.key,
    required this.onSelectionChanged,
    required this.options,
  });

  @override
  State<SingleChoiceQuiz> createState() => _SingleChoiceQuizState();
}

class _SingleChoiceQuizState extends State<SingleChoiceQuiz> {
  int _selection = -1;
  late List<String> _options;

  @override
  void initState() {
    super.initState();
    _options = widget.options;
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;

    return ListView.builder(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      itemCount: _options.length,
      itemBuilder: (BuildContext context, int index) {
        String letter = String.fromCharCode(65 + index);
        bool selected = _selection == index;
        return Card(
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(10.0),
            side: BorderSide(
              color: selected ? colors.primary : Colors.grey,
              width: selected ? 2.0 : 1.0,
            ),
          ),
          elevation: 1.0,
          surfaceTintColor: Colors.white,
          child: ListTile(
            leading: Container(
              width: 30.0,
              height: 30.0,
              decoration: BoxDecoration(
                color: selected ? colors.primary : Colors.grey,
                shape: BoxShape.circle,
              ),
              child: Center(
                child: Text(
                  letter,
                  style: const TextStyle(
                    color: Colors.white,
                    fontWeight: FontWeight.bold,
                    fontSize: 18,
                  ),
                ),
              ),
            ),
            title: Text(_options[index]),
            onTap: () {
              setState(() {
                _selection = index;
              });
              widget.onSelectionChanged(index);
            },
          ),
        );
      },
    );
  }
}
