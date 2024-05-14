import 'package:flutter/material.dart';
import 'package:flutter_svg/svg.dart';
import 'package:frontend/components/layout/sliver_layout.dart';
import 'package:frontend/global.dart';
import 'package:frontend/models/course.dart';
import 'package:frontend/theme/assets.dart';
import 'package:frontend/components/elements/course/JoinCourseDialog.dart';

class ChooseCourse extends StatefulWidget {
  final List<Course> courses;
  final Function(String id) choose;
  final Function(String courseId) joinCourse;

  const ChooseCourse({
    super.key,
    required this.courses,
    required this.choose,
    required this.joinCourse,
  });

  @override
  State<ChooseCourse> createState() => _ChooseCourseState();
}

class _ChooseCourseState extends State<ChooseCourse> {
  final double height = 140;

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;

    return Stack(
      children: [
        SliverLayout(
          collapsable: true,
          title: (percentage) {
            return Align(
              alignment: Alignment.bottomLeft,
              child: Padding(
                padding: EdgeInsets.only(
                    left: 16 + 16.0 * percentage,
                    bottom: 12 + 6.0 * percentage),
                child: Text(
                  "Kurse",
                  style: TextStyle(
                    color: colors.onSurface,
                    fontWeight: FontWeight.bold,
                  ),
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
                    side: widget.courses[index].isOwner ? BorderSide(
                      color: colors.primary,
                      width: 2.0,
                    ) : BorderSide.none,
                  ),
                  elevation: 1.0,
                  surfaceTintColor: Colors.white,
                  clipBehavior: Clip.antiAlias,
                  child: ListTile(
                    title: Text(widget.courses[index].name),
                    subtitle: Text(widget.courses[index].description),
                    onTap: () {
                      widget.choose(widget.courses[index].id);
                    },
                  ),
                );
              },
            ),
          ),
        ),
        Positioned(
          right: 16,
          bottom: 16,
          child: FloatingActionButton(
            backgroundColor: colors.primary,
            onPressed: () {
              showDialog(
                context: context,
                builder: (BuildContext context) {
                  return JoinCourseDialog(
                    onJoinCourse: widget.joinCourse,
                  );
                },
              );
            },
            child: const Icon(Icons.add,
                color: Color.fromARGB(255, 255, 255, 255)),
          ),
        ),
      ],
    );
  }
}
