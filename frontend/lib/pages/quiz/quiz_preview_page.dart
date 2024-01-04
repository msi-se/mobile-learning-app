import 'package:flutter/material.dart';
import 'package:frontend/components/layout/sliver_layout.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';

import 'package:frontend/models/quiz/quiz_form.dart';
import 'package:frontend/utils.dart';
import 'package:frontend/global.dart';

class QuizPreviewPage extends StatefulWidget {
  final String courseId;
  final String formId;

  const QuizPreviewPage({
    Key? key,
    required this.courseId,
    required this.formId,
  }) : super(key: key);

  @override
  State<QuizPreviewPage> createState() => _QuizPreviewPageState();
}

class _QuizPreviewPageState extends State<QuizPreviewPage> {
  bool _loading = true;
  QuizForm? _form;

  @override
  void initState() {
    super.initState();
    fetchForm();
  }

  Future<void> fetchForm() async {
    try {
      print(
          "${getBackendUrl()}/course/${widget.courseId}/quiz/form/${widget.formId}");
      final response = await http.get(
        Uri.parse(
            "${getBackendUrl()}/course/${widget.courseId}/quiz/form/${widget.formId}"),
        headers: {
          "Content-Type": "application/json",
          "AUTHORIZATION": "Bearer ${getSession()!.jwt}",
        },
      );
      if (response.statusCode == 200) {
        setState(() {
          _form = QuizForm.fromJson(json.decode(response.body));
          _loading = false;
        });
      } else {
        // TODO: Handle the case where the server returns a non-200 status code
        // setState(() {
        //   _loading = false;
        // });
      }
    } catch (e) {
      // TODO: Handle any exceptions
      // setState(() {
      //   _loading = false;
      // });
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_loading) {
      return const Scaffold(
        body: Center(child: CircularProgressIndicator()),
      );
    }
    final colors = Theme.of(context).colorScheme;

    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.primary,
        title: const Text("Quiz Info",
            style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
      ),
      body: SliverLayout(
        collapsable: false,
        headerHeight: 100,
        navBarHeight: 0,
        title: (_) {
          return Padding(
            padding: const EdgeInsets.only(left: 16, top: 16, right: 16),
            child: Column(
              children: [
                Text(
                  _form!.name,
                  style: TextStyle(
                    color: colors.onSurface,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                Row(
                  children: [
                    Expanded(
                      child: FilledButton(
                        onPressed: () {
                          Navigator.pushNamed(context, '/quiz-result',
                              arguments: {
                                "courseId": widget.courseId,
                                "formId": widget.formId,
                              });
                        },
                        child: _form!.status == "NOT_STARTED"
                            ? const Text('Starten')
                            : const Text('Ergebnisse'),
                      ),
                    ),
                    const SizedBox(width: 10),
                    Expanded(
                      child: FilledButton(
                        onPressed: () {
                          Navigator.pushNamed(context, '/attend-quiz',
                              arguments: _form!.connectCode);
                        },
                        child: const Text('Beitreten'),
                      ),
                    ),
                  ],
                ),
              ],
            ),
          );
        },
        body: Padding(
          padding: const EdgeInsets.all(8.0),
          child: ListView.builder(
            shrinkWrap: true,
            physics: const NeverScrollableScrollPhysics(),
            itemCount: _form!.questions.length,
            itemBuilder: (context, index) {
              var element = _form!.questions[index];
              return Card(
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(10.0),
                ),
                elevation: 1.0,
                surfaceTintColor: Colors.white,
                child: ListTile(
                  title: Text(element.description),
                ),
              );
            },
          ),
        ),
      ),
    );
  }
}
