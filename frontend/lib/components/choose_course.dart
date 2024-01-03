import 'package:flutter/material.dart';
import 'package:flutter_svg/svg.dart';
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
    double heightWithoutappBarNavBar = MediaQuery.of(context).size.height -
        (kBottomNavigationBarHeight + kToolbarHeight);

    final colors = Theme.of(context).colorScheme;

    return Scaffold(
      body: CustomScrollView(
        slivers: [
          sliverHeader(colors),
          sliverList(heightWithoutappBarNavBar, colors),
        ],
      ),
    );
  }

  SliverAppBar sliverHeader(ColorScheme colors) {
    return SliverAppBar(
      surfaceTintColor: colors.background,
      expandedHeight: height,
      pinned: true,
      flexibleSpace: FlexibleSpaceBar(
        expandedTitleScale: 2,
        titlePadding: const EdgeInsetsDirectional.only(start: 16),
        title: LayoutBuilder(
          builder: (context, constraints) {
            double percent = ((constraints.biggest.height - kToolbarHeight) /
                    (height - kToolbarHeight))
                .clamp(0.0, 1.0);
            return Padding(
              padding: EdgeInsets.only(
                  left: 18.0 * percent, bottom: 12 + 6.0 * percent),
              child: const Text(
                "Kurse",
                style: TextStyle(
                  color: Colors.black,
                  fontWeight: FontWeight.bold,
                ),
              ),
            );
          },
        ),
        background: Padding(
          padding: const EdgeInsets.only(top: 25.0, right: 20),
          child: SvgPicture.asset(
            undrawEducation,
            alignment: Alignment.bottomRight,
          ),
        ),
      ),
    );
  }

  SliverList sliverList(double heightWithoutappBarNavBar, ColorScheme colors) {
    return SliverList(
      delegate: SliverChildListDelegate([
        ConstrainedBox(
          constraints: BoxConstraints(
            minHeight: heightWithoutappBarNavBar - 130,
          ),
          child: Card(
            shape: const RoundedRectangleBorder(
              borderRadius: BorderRadius.only(
                  topLeft: Radius.circular(25), topRight: Radius.circular(25)),
            ),
            color: colors.background,
            margin: const EdgeInsets.only(top: 0),
            child: Padding(
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
          ),
        ),
      ]),
    );
  }
}
