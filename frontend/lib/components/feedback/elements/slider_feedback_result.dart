import 'package:flutter/material.dart';

class SliderFeedbackResult extends StatefulWidget {
  final List<int> results;
  final double average;
  final int min;
  final int max;

  const SliderFeedbackResult({
    super.key,
    required this.results,
    required this.average,
    required this.min,
    required this.max,
  });

  @override
  State<SliderFeedbackResult> createState() => _SliderFeedbackResultState();
}

class _SliderFeedbackResultState extends State<SliderFeedbackResult> {
  late List<double> _normCounts;
  late double _average;
  late int _min;
  late int _max;
  late int _itemCount;

  @override
  void initState() {
    super.initState();
    _average = widget.average;
    _min = widget.min;
    _max = widget.max;
    _itemCount = _max - _min + 1;
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
    var counts = List.generate(_itemCount, (index) => 0);
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

    return LayoutBuilder(
        builder: (BuildContext context, BoxConstraints constraints) {
      double width = constraints.maxWidth;
      double height = width / 4;
      double innerWidth = constraints.maxWidth - height;

      return SizedBox(
        width: width,
        height: height,
        child: Stack(
          children: List.generate(_itemCount + 2, (index) {
            // Horizontal line
            if (index == 0) {
              return Positioned(
                left: height / 2,
                top: height / 2 - 1,
                child: Container(
                  width: innerWidth,
                  height: 2,
                  decoration: BoxDecoration(
                    color: colors.tertiary.withAlpha(48),
                  ),
                ),
              );
            }
            // Average line
            if (index == _itemCount + 1) {
              return Positioned(
                left:
                    height / 2 + (_average - _min) * innerWidth / (_max - _min),
                top: 0,
                child: Container(
                  width: 2,
                  height: height,
                  decoration: BoxDecoration(
                    color: colors.secondary,
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
                  color: colors.primary.withOpacity(0.4),
                ),
              ),
            );
          }),
        ),
      );
    });
  }
}
