import 'package:flutter/material.dart';

class StarFeedbackResult extends StatefulWidget {
  final double average;

  const StarFeedbackResult({super.key, required this.average});

  @override
  State<StarFeedbackResult> createState() => _StarFeedbackResultState();
}

class _StarFeedbackResultState extends State<StarFeedbackResult> {
  late double _rating;

  @override
  void initState() {
    super.initState();
    _rating = widget.average;
  }

  @override
  void didUpdateWidget(StarFeedbackResult oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.average != oldWidget.average) {
      _rating = widget.average;
    }
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;

    return Row(
      mainAxisSize: MainAxisSize.min,
      children: List.generate(5, (index) {
        return Stack(
          children: [
            Icon(
              Icons.star,
              size: 40.0,
              color: colors.tertiary.withAlpha(48),
            ),
            ClipRect(
              clipper: _StarClipper(_rating - index),
              child: Icon(
                Icons.star,
                size: 40.0,
                color: index < _rating
                    ? colors.primary
                    : colors.tertiary.withAlpha(48),
              ),
            ),
          ],
        );
      }),
    );
  }
}

class _StarClipper extends CustomClipper<Rect> {
  final double widthFactor;

  _StarClipper(this.widthFactor);

  @override
  Rect getClip(Size size) {
    return Rect.fromLTRB(0, 0, size.width * widthFactor, size.height);
  }

  @override
  bool shouldReclip(_StarClipper oldClipper) {
    return widthFactor != oldClipper.widthFactor;
  }
}
