import 'dart:math';

import 'package:flutter/material.dart';

class SingleChoiceQuizResult extends StatefulWidget {
  final List<int> results;
  final List<String> options;
  final String correctAnswer;

  const SingleChoiceQuizResult({
    super.key,
    required this.results,
    required this.options,
    required this.correctAnswer,
  });

  @override
  State<SingleChoiceQuizResult> createState() => _SingleChoiceQuizResultState();
}

class OptionDerivation {
  final String option;
  final int count;
  final double percentage;
  final double normalizedPercentage;

  OptionDerivation(
      this.option, this.count, this.percentage, this.normalizedPercentage);
}

class _SingleChoiceQuizResultState extends State<SingleChoiceQuizResult> {
  late List<OptionDerivation> _optionDerivations;

  @override
  void initState() {
    super.initState();
    _updateOptionDerivations();
  }

  @override
  void didUpdateWidget(SingleChoiceQuizResult oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.results != oldWidget.results) {
      _updateOptionDerivations();
    }
  }

  void _updateOptionDerivations() {
    var counts = List.filled(widget.options.length, 0);
    for (var result in widget.results) {
      counts[result]++;
    }
    var total = counts.reduce((a, b) => a + b);
    if (total == 0) {
      total = 1;
    }
    var percentages = counts.map((e) => (e / total)).toList();
    var maxPercentage = percentages.reduce((a, b) => a > b ? a : b);
    if (maxPercentage == 0) {
      maxPercentage = 1;
    }
    var normalizedPercentages =
        percentages.map((e) => max(e / maxPercentage, 0.01)).toList();
    var optionDerivations = <OptionDerivation>[];
    for (var i = 0; i < widget.options.length; i++) {
      optionDerivations.add(OptionDerivation(widget.options[i], counts[i],
          percentages[i], normalizedPercentages[i]));
    }
    setState(() {
      _optionDerivations = optionDerivations;
    });
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;

    // create a simple bar chart
    var bars = <Widget>[];
    for (final (index, optionDerivation) in _optionDerivations.indexed) {
      bool correctAnswer = index.toString() == widget.correctAnswer;
      bars.add(
        Column(
          children: [
            const SizedBox(height: 5),
            Align(
              alignment: Alignment.center,
              child: Text(
                optionDerivation.option,
                style: TextStyle(
                  color: correctAnswer ? Colors.green : colors.onBackground,
                  fontWeight:
                      correctAnswer ? FontWeight.bold : FontWeight.normal,
                  fontSize: 20,
                ),
              ),
            ),
            Row(
              children: [
                SizedBox(
                  width: 20,
                  child: Text(
                    "${optionDerivation.count}",
                    style: TextStyle(
                      color: colors.onBackground,
                      fontWeight:
                          correctAnswer ? FontWeight.bold : FontWeight.normal,
                    ),
                  ),
                ),
                Expanded(
                  child: Padding(
                    padding: const EdgeInsets.all(2),
                    child: SizedBox(
                      height: 20,
                      child: LinearProgressIndicator(
                        value: optionDerivation.normalizedPercentage,
                        backgroundColor: colors.secondary.withOpacity(0.1),
                        valueColor: AlwaysStoppedAnimation<Color>(
                          correctAnswer ? Colors.green : colors.tertiary,
                        ),
                      ),
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 5),
          ],
        ),
      );
    }

    return Column(
      children: [
        ...bars,
      ],
    );
  }
}
