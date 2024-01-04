import 'package:flutter/material.dart';
import 'package:flutter_svg/svg.dart';
import 'package:frontend/components/layout/sliver_layout.dart';
import 'package:frontend/models/feedback/feedback_course.dart';
import 'package:frontend/theme/assets.dart';

class ChooseCourse extends StatefulWidget {
  final List<FeedbackCourse> courses;
  final Function(String id) choose;

  const ChooseCourse({super.key, required this.courses, required this.choose});

  @override
  State<ChooseCourse> createState() => _ChooseCourseState();
}

class _ChooseCourseState extends State<ChooseCourse> {
  final double height = 140;

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;

    return SliverLayout(
      collapsable: true,
      title: (percentage) {
        return Padding(
          padding: EdgeInsets.only(
              left: 16 + 18.0 * percentage, bottom: 12 + 6.0 * percentage),
          child: Text(
            "Kurse",
            style: TextStyle(
              color: colors.onSurface,
              fontWeight: FontWeight.bold,
            ),
          ),
        );
      },
      background: Padding(
        padding: const EdgeInsets.only(top: 25.0, right: 20),
        child: SvgPicture.asset(
          undrawEducation,
          alignment: Alignment.bottomRight,
        ),
      ),
      body: Padding(
        padding: const EdgeInsets.all(8.0),
        child: ListView.builder(
          shrinkWrap: true,
          physics: const NeverScrollableScrollPhysics(),
          itemCount: widget.courses.length,
          itemBuilder: (context, index) {
            return Card(
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(10.0),
              ),
              elevation: 1.0,
              surfaceTintColor: Colors.white,
              child: ListTile(
                title: Text(widget.courses[index].name),
                subtitle: Text(widget.courses[index].description),
                // dense: true,
                // trailing: const Icon(Icons.arrow_forward_ios),
                onTap: () {
                  widget.choose(widget.courses[index].id);
                },
              ),
            );
          },
        ),
      ),
    );
  }
}
