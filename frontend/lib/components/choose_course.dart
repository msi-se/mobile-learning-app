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
  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;

    return Scaffold(
      body: Column(
        children: [
          Row(
            children: [
              const Padding(
                padding: EdgeInsets.only(left: 40.0, top: 25.0, bottom: 10.0),
                child: Text(
                  "Kurse",
                  style: TextStyle(
                    fontSize: 50.0,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
              Expanded(
                child: Padding(
                  padding: const EdgeInsets.only(top: 20.0, right: 20.0),
                  child: SvgPicture.asset(
                    undrawEducation,
                    alignment: Alignment.bottomRight,
                    height: 120,
                  ),
                ),
              ),
            ],
          ),
          Expanded(
            child: Card(
              shape: const RoundedRectangleBorder(
                borderRadius: BorderRadius.only(
                    topLeft: Radius.circular(25),
                    topRight: Radius.circular(25)),
              ),
              color: colors.background,
              margin: const EdgeInsets.only(top: 0),
              child: Padding(
                padding: const EdgeInsets.all(8.0),
                child: ListView.builder(
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
        ],
      ),
    );
  }
}
