import 'package:flutter/material.dart';
import 'package:frontend/components/layout/sliver_layout.dart';
import 'package:frontend/models/feedback/feedback_course.dart';
import 'package:frontend/theme/assets.dart';

class ChooseForm extends StatefulWidget {
  final FeedbackCourse course;
  final Function(String id) choose;

  const ChooseForm({super.key, required this.course, required this.choose});

  @override
  State<ChooseForm> createState() => _ChooseFormState();
}

class _ChooseFormState extends State<ChooseForm> {
  String feedbackOrQuiz = "Feedback";

  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    var forms = feedbackOrQuiz == "Feedback" ? widget.course.feedbackForms : [];

    return SliverLayout(
      collapsable: false,
      expandedTitleScale: 1,
      title: (percentage) {
        return Align(
          alignment: Alignment.centerLeft,
          child: Padding(
            padding:
                EdgeInsets.only(left: 16, right: percentage < 0.2 ? 0 : 120),
            child: Text(
              widget.course.name,
              style: TextStyle(
                color: colors.onSurface,
                fontWeight: FontWeight.bold,
              ),
            ),
          ),
        );
      },
      // round button in Background with "Moodle"
      background: Padding(
          padding: const EdgeInsets.only(right: 20),
          child: Align(
            alignment: Alignment.centerRight,
            child: ElevatedButton(
              onPressed: () {},
              style: ElevatedButton.styleFrom(
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(20.0),
                ),
              ),
              child: Image.asset(moodleLogo, width: 80),
            ),
          )),
      body: Column(
        children: [
          Row(
            children: [
              Expanded(
                child: TextButton(
                  onPressed: () {
                    setState(() {
                      feedbackOrQuiz = "Feedback";
                    });
                  },
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Icon(
                        Icons.feedback,
                        color: feedbackOrQuiz == "Feedback"
                            ? colors.primary
                            : colors.tertiary,
                      ),
                      const SizedBox(width: 10),
                      Text(
                        "Feedback",
                        style: feedbackOrQuiz == "Feedback"
                            ? TextStyle(
                                color: colors.primary,
                                fontWeight: FontWeight.bold,
                              )
                            : TextStyle(
                                color: colors.tertiary,
                                fontWeight: FontWeight.normal,
                              ),
                      ),
                    ],
                  ),
                ),
              ),
              Expanded(
                child: TextButton(
                  onPressed: () {
                    setState(() {
                      feedbackOrQuiz = "Quiz";
                    });
                  },
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Icon(
                        Icons.quiz,
                        color: feedbackOrQuiz == "Quiz"
                            ? colors.primary
                            : colors.tertiary,
                      ),
                      const SizedBox(width: 10),
                      Text(
                        "Quiz",
                        style: feedbackOrQuiz == "Quiz"
                            ? TextStyle(
                                color: colors.primary,
                                fontWeight: FontWeight.bold,
                              )
                            : TextStyle(
                                color: colors.tertiary,
                                fontWeight: FontWeight.normal,
                              ),
                      ),
                    ],
                  ),
                ),
              ),
            ],
          ),
          const Divider(
            thickness: 1,
          ),
          Padding(
            padding: const EdgeInsets.all(8.0),
            child: ListView.builder(
              shrinkWrap: true,
              physics: const NeverScrollableScrollPhysics(),
              itemCount: forms.length,
              itemBuilder: (context, index) {
                return Card(
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(10.0),
                  ),
                  elevation: 1.0,
                  surfaceTintColor: Colors.white,
                  child: ListTile(
                    title: Text(forms[index].name),
                    subtitle: Text(forms[index].description),
                    // trailing: const Icon(Icons.arrow_forward_ios),
                    // TODO: display status of form
                    onTap: () {
                      widget.choose(forms[index].id);
                    },
                  ),
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}
