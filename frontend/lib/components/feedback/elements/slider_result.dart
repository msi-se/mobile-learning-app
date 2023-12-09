import 'package:flutter/material.dart';

class SliderResult extends StatefulWidget {
  final List<int> results;

  const SliderResult({super.key, required this.results});

  @override
  State<SliderResult> createState() => _SliderResultState();
}

class _SliderResultState extends State<SliderResult> {
  late List<double> _normCounts;

  @override
  void initState() {
    super.initState();
    _updateCounts();
  }

  @override
  void didUpdateWidget(SliderResult oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.results != oldWidget.results) {
      _updateCounts();
    }
  }

  void _updateCounts() {
    var counts = List.generate(11, (index) => 0);
    for (var result in widget.results) {
      if (result >= 0 && result <= 11) {
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
          children: List.generate(12, (index) {
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
            var radius = 10 + _normCounts[index] * (height - 10);
            return Positioned(
              left: height / 2 + (index) * innerWidth / 10 - radius / 2,
              top: height / 2 - radius / 2,
              child: Container(
                width: radius,
                height: radius,
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
