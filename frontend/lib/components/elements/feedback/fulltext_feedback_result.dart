import 'package:flutter/material.dart';

class FulltextFeedbackResult extends StatefulWidget {
  final List<dynamic> results;

  const FulltextFeedbackResult({
    super.key,
    required this.results,
  });

  @override
  State<FulltextFeedbackResult> createState() => _FulltextFeedbackResultState();
}

class _FulltextFeedbackResultState extends State<FulltextFeedbackResult> {
  late String _resultString;

  @override
  void initState() {
    super.initState();
    _updateResultString();
  }

  @override
  void didUpdateWidget(FulltextFeedbackResult oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.results != oldWidget.results) {
      _updateResultString();
    }
  }

  void _updateResultString() {
    List<String> resultsAsStrings = [];

    for (var result in widget.results) {
      String resultAsString = result.toString();
      resultsAsStrings.add('"$resultAsString"');
    }
    String resultString = resultsAsStrings.join("\n");

    setState(() {
      _resultString = resultString;
    });
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        // Text(
        //   "Results",
        //   style: TextStyle(
        //     color: colors.primary,
        //     fontSize: 20,
        //   ),
        // ),
        Text(
          _resultString,
          style: TextStyle(
            color: colors.primary,
            fontSize: 16,
          ),
        ),
      ],
    );
  }
}
