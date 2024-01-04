import 'package:flutter/material.dart';

class StarFeedback extends StatefulWidget {
  final int initialRating;
  final ValueChanged<int> onRatingChanged;

  const StarFeedback(
      {super.key, this.initialRating = 0, required this.onRatingChanged});

  @override
  State<StarFeedback> createState() => _StarFeedbackState();
}

class _StarFeedbackState extends State<StarFeedback> {
  late int _rating;

  @override
  void initState() {
    super.initState();
    _rating = widget.initialRating;
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;

    return GestureDetector(
      onHorizontalDragUpdate: (dragDetails) {
        final box = context.findRenderObject() as RenderBox;
        final dx = dragDetails.localPosition.dx;
        final starWidth = box.size.width / 5;
        final newRating = (dx / starWidth).ceil();
        if (newRating != _rating && newRating >= 1 && newRating <= 5) {
          setState(() {
            _rating = newRating;
          });
          widget.onRatingChanged(newRating);
        }
      },
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: List.generate(5, (index) {
          return IconButton(
            icon: Icon(
              Icons.star,
              size: 40.0,
              color: index < _rating
                  ? colors.primary
                  : colors.tertiary.withAlpha(48),
            ),
            onPressed: () {
              setState(() {
                _rating = index + 1;
              });
              widget.onRatingChanged(index + 1);
            },
          );
        }),
      ),
    );
  }

}