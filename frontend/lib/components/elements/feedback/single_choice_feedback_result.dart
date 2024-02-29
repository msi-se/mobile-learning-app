import 'package:flutter/material.dart';

class SingleChoiceFeedbackResult extends StatefulWidget {
  final List<int> results;
  final List<String> options;

  const SingleChoiceFeedbackResult({
    super.key,
    required this.results,
    required this.options,
  });

  @override
  State<SingleChoiceFeedbackResult> createState() =>
      _SingleChoiceFeedbackResultState();
}

class OptionDerivation {
  final String option;
  final int count;
  final double percentage;
  final double normalizedPercentage;

  OptionDerivation(
      this.option, this.count, this.percentage, this.normalizedPercentage);
}

class _SingleChoiceFeedbackResultState
    extends State<SingleChoiceFeedbackResult> {
  late List<OptionDerivation> _optionDerivations;

  @override
  void initState() {
    super.initState();
    _updateOptionDerivations();
  }

  @override
  void didUpdateWidget(SingleChoiceFeedbackResult oldWidget) {
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
    for (final (_, optionDerivation) in _optionDerivations.indexed) {
      bars.add(
        Column(
          children: [
            const SizedBox(height: 5),
            Align(
              alignment: Alignment.center,
              child: Text(
                optionDerivation.option,
                style: TextStyle(
                  color: colors.onBackground,
                  fontWeight: FontWeight.normal,
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
                      fontWeight: FontWeight.normal,
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
                          colors.primary
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
