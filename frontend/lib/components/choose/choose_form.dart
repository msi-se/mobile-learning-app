import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:frontend/components/layout/sliver_layout.dart';
import 'package:frontend/enums/form_status.dart';
import 'package:frontend/models/course.dart';
import 'package:frontend/theme/assets.dart';
import 'package:frontend/enums/form_type.dart';
import 'package:rive/rive.dart';
import 'package:url_launcher/url_launcher.dart';

class ChooseForm extends StatefulWidget {
  final Course course;
  final Function(String id, FormType type) choose;

  const ChooseForm({super.key, required this.course, required this.choose});

  @override
  State<ChooseForm> createState() => _ChooseFormState();
}

class _ChooseFormState extends State<ChooseForm> {
  FormType formType = FormType.feedback;
  Map<String, Color> statusColors = {};

  @override
  void initState() {
    super.initState();
    // STARTED, FINISHED, NOT_STARTED
    statusColors = {
      FormStatus.waiting.toString(): Colors.green,
      FormStatus.started.toString(): Colors.orange,
      FormStatus.finished.toString(): Colors.red,
      FormStatus.not_started.toString(): Colors.grey,
    };
  }

  void _showInfoDialog() {
    showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: Text(widget.course.name),
          content: Row(
            children: [
              Expanded(
                child: Text("Kurs ID: ${widget.course.id}"),
              ),
              IconButton(
                icon: const Icon(Icons.copy),
                onPressed: () {
                  Clipboard.setData(ClipboardData(text: widget.course.id));
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(
                      content: Text('Kurs ID in die Abblage kopiert!'),
                      duration: Duration(seconds: 1),
                    ),
                  );
                },
              ),
            ],
          ),
          actions: <Widget>[
            TextButton(
              child: const Text('Schließen'),
              onPressed: () {
                Navigator.of(context).pop();
              },
            ),
          ],
        );
      },
    );
  }

  void _openMoodle() async {
    final moodleCourseId = widget.course.moodleCourseId;
    final moodleUrl =
        "https://moodle.htwg-konstanz.de/moodle/course/view.php?id=$moodleCourseId";
    final moodleUri = Uri.parse(moodleUrl);
    if (await canLaunchUrl(moodleUri)) {
      await launchUrl(moodleUri);
    } else {
      throw 'Could not launch $moodleUrl';
    }
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    var forms = formType == FormType.feedback
        ? widget.course.feedbackForms
        : widget.course.quizForms;

    return SliverLayout(
      collapsable: true,
      expandedTitleScale: 1,
      title: (percentage) {
        return LayoutBuilder(
          builder: (context, constraints) {
            final isNarrow = constraints.maxWidth < 360;
            return Align(
              alignment: Alignment.centerLeft,
              child: Padding(
                  padding: EdgeInsets.only(
                    left: 16,
                    right: percentage < 0.2 || isNarrow ? 0 : 120,
                  ),
                  child: Row(
                    children: [
                      Text(
                        widget.course.name,
                        style: TextStyle(
                          color: colors.onSurface,
                          fontWeight: FontWeight.bold,
                          fontSize: 16,
                        ),
                        overflow: TextOverflow.ellipsis,
                      ),
                      IconButton(
                        icon: Icon(Icons.info_outline, color: colors.onSurface),
                        onPressed: _showInfoDialog,
                      ),
                    ],
                  )),
            );
          },
        );
      },
      background: Padding(
        padding: const EdgeInsets.only(right: 20),
        child: Align(
          alignment: Alignment.centerRight,
          child: widget.course.moodleCourseId != ''
              ? ElevatedButton(
                  onPressed: () {
                    _openMoodle();
                  },
                  style: ElevatedButton.styleFrom(
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(20.0),
                    ),
                  ),
                  child: Image.asset(moodleLogo, width: 80),
                )
              : null,
        ),
      ),
      body: Column(
        children: [
          Row(
            children: [
              Expanded(
                child: TextButton(
                  onPressed: () {
                    setState(() {
                      formType = FormType.feedback;
                    });
                  },
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Icon(
                        Icons.feedback,
                        color: formType == FormType.feedback
                            ? colors.primary
                            : colors.tertiary,
                      ),
                      const SizedBox(width: 10),
                      Text(
                        "Feedback",
                        style: formType == FormType.feedback
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
                      formType = FormType.quiz;
                    });
                  },
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Icon(
                        Icons.quiz,
                        color: formType == FormType.quiz
                            ? colors.primary
                            : colors.tertiary,
                      ),
                      const SizedBox(width: 10),
                      Text(
                        "Quiz",
                        style: formType == FormType.quiz
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
          if (forms.isNotEmpty)
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
                    clipBehavior: Clip.antiAlias,
                    child: ListTile(
                      trailing: Icon(Icons.circle,
                          color: statusColors[forms[index].status.toString()],
                          size: 20.0),
                      title: Text(forms[index].name),
                      subtitle: Text(forms[index].description),
                      // trailing: const Icon(Icons.arrow_forward_ios),
                      // TODO: display status of form
                      onTap: () {
                        widget.choose(forms[index].id, formType);
                      },
                    ),
                  );
                },
              ),
            ),
          if (forms.isEmpty && formType == FormType.feedback)
            Text("Keine Feedbacks gefunden"),
          if (forms.isEmpty && formType == FormType.quiz)
            Text("Keine Quizze gefunden"),
          if (forms.isEmpty)
            Container(
              margin: const EdgeInsets.only(top: 20.0, bottom: 100.0),
              width: 250,
              height: 250,
              child: RiveAnimation.asset(
                'assets/animations/rive/animations.riv',
                fit: BoxFit.cover,
                artboard: 'Sleeping Mascot',
                stateMachines: ['Sleeping State Machine'],
              ),
            ),
        ],
      ),
    );
  }
}
