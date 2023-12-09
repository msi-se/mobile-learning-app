import 'package:flutter/material.dart';

class SliderFeedbackResult extends StatefulWidget {
  final List<int> results;
  final int min;
  final int max;

  const SliderFeedbackResult({
    super.key,
    required this.results,
    required this.min,
    required this.max,
  });

  @override
  State<SliderFeedbackResult> createState() => _SliderFeedbackResultState();
}

class _SliderFeedbackResultState extends State<SliderFeedbackResult> {
  late List<double> _normCounts;
  late int _min;
  late int _max;

  @override
  void initState() {
    super.initState();
    _min = widget.min;
    _max = widget.max;
    _updateCounts();
  }

  @override
  void didUpdateWidget(SliderFeedbackResult oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.results != oldWidget.results) {
      _updateCounts();
    }
  }

  void _updateCounts() {
    var counts = List.generate(_max - _min + 1, (index) => 0);
    for (var result in widget.results) {
      if (result >= _min && result <= _max) {
        counts[result]++;
      }
    }

    int maxCount = counts.reduce((curr, next) => curr > next ? curr : next);
    int minCount = counts.reduce((curr, next) => curr < next ? curr : next);

    if (maxCount == minCount) {
      maxCount++;
    }

    _normCounts = counts
        .map((count) => (count - minCount) / (maxCount - minCount))
        .toList();
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    const height = 80.0;

    return LayoutBuilder(
        builder: (BuildContext context, BoxConstraints constraints) {
      double width = constraints.maxWidth;
      double innerWidth = constraints.maxWidth - height;

      return SizedBox(
        width: width, // Adjust this value to change the width of the box
        height: height, // Adjust this value to change the height of the box
        child: Stack(
          children: List.generate(_max - _min + 2, (index) {
            if (index == 0) {
              return Positioned(
                left: height / 2,
                top: height / 2 - 1,
                child: Container(
                  width: innerWidth,
                  height: 2,
                  decoration: BoxDecoration(
                    color: colors.secondary.withAlpha(64),
                  ),
                ),
              );
            }
            index--;
            var size = 10 + _normCounts[index] * (height - 10);
            return Positioned(
              left:
                  height / 2 + (index) * innerWidth / (_max - _min) - size / 2,
              top: height / 2 - size / 2,
              child: Container(
                width: size,
                height: size,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  color: colors.primary.withOpacity(0.3),
                ),
              ),
            );
          }),
        ),
      );
    });
  }
}
