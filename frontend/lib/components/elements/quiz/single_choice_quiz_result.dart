import 'package:flutter/material.dart';

class SingleChoiceQuizResult extends StatefulWidget {
  final List<int> results;
  final List<String> options;

  const SingleChoiceQuizResult({
    super.key,
    required this.results,
    required this.options,
  });

  @override
  State<SingleChoiceQuizResult> createState() =>
      _SingleChoiceQuizResultState();
}

class OptionDerivation {
  final String option;
  final int count;
  final double percentage;
  final double normalizedPercentage;

  OptionDerivation(
      this.option, this.count, this.percentage, this.normalizedPercentage);
}

class _SingleChoiceQuizResultState
    extends State<SingleChoiceQuizResult> {
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
        percentages.map((e) => e / maxPercentage).toList();
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
    for (var optionDerivation in _optionDerivations) {
      bars.add(
        SizedBox(
          height: 20,
          child: Row(
            children: [
              SizedBox(
                width: 150,
                child: Align(
                  alignment: Alignment.centerRight,
                  child: Text(
                    optionDerivation.option,
                    style: TextStyle(
                      color: colors.onBackground,
                    ),
                  ),
                ),
              ),
              Expanded(
                child: Padding(
                  padding: const EdgeInsets.all(4),
                  child: SizedBox(
                    height: 20,
                    child: LinearProgressIndicator(
                      value: optionDerivation.normalizedPercentage,
                      backgroundColor: colors.background,
                      valueColor: AlwaysStoppedAnimation<Color>(
                        colors.primary,
                      ),
                    ),
                  ),
                ),
              ),
              SizedBox(
                width: 50,
                child: Text(
                  "${optionDerivation.count}",
                  style: TextStyle(
                    color: colors.onBackground,
                  ),
                ),
              ),
            ],
          ),
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
