import 'package:flutter/material.dart';
import 'package:flutter/services.dart'; 
import 'package:frontend/components/layout/sliver_layout.dart';
import 'package:frontend/models/course.dart';
import 'package:frontend/theme/assets.dart';

class ChooseForm extends StatefulWidget {
  final Course course;
  final Function(String id, String type) choose;

  const ChooseForm({super.key, required this.course, required this.choose});

  @override
  State<ChooseForm> createState() => _ChooseFormState();
}

class _ChooseFormState extends State<ChooseForm> {
  String feedbackOrQuiz = "Feedback";
  Map<String, Color> statusColors = {};


  @override
  void initState() {
    super.initState();
    // STARTED, FINISHED, NOT_STARTED
    statusColors = {
      "WAITING": Colors.green,
      "STARTED": Colors.orange,
      "FINISHED": Colors.red,
      "NOT_STARTED": Colors.grey,
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


  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    var forms = feedbackOrQuiz == "Feedback"
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
                child: Row(children: [
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
                ],) 
              ),
            );
          },
        );
      },
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
                  clipBehavior: Clip.antiAlias,
                  child: ListTile(
                    trailing: Icon(Icons.circle,
                        color: statusColors[forms[index].status], size: 20.0),
                    title: Text(forms[index].name),
                    subtitle: Text(forms[index].description),
                    // trailing: const Icon(Icons.arrow_forward_ios),
                    // TODO: display status of form
                    onTap: () {
                      widget.choose(forms[index].id, feedbackOrQuiz);
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
