import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';

import 'package:frontend/models/feedback/feedback_form.dart';
import 'package:frontend/utils.dart';

class FeedbackPreviewPage extends StatefulWidget {
  final String courseId;
  final String formId;

  const FeedbackPreviewPage({
    Key? key,
    required this.courseId,
    required this.formId,
  }) : super(key: key);

  @override
  State<FeedbackPreviewPage> createState() => _FeedbackPreviewPageState();
}

class _FeedbackPreviewPageState extends State<FeedbackPreviewPage> {
  bool _loading = true;
  FeedbackForm? _form;

  @override
  void initState() {
    super.initState();
    fetchForm();
  }

  Future<void> fetchForm() async {
    try {
      print("${getBackendUrl()}/course/${widget.courseId}/feedback/form/${widget.formId}");
      final response = await http.get(Uri.parse(
          "${getBackendUrl()}/course/${widget.courseId}/feedback/form/${widget.formId}"));
      if (response.statusCode == 200) {
        setState(() {
          _form = FeedbackForm.fromJson(json.decode(response.body));
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
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.primary,
        title: const Text("Feedback Info",
            style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
      ),
      body: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16.0),
        child: _loading
            ? const Center(child: CircularProgressIndicator())
            : Column(
                children: [
                  Container(
                    width: double.infinity,
                    padding: const EdgeInsets.all(16),
                    margin: const EdgeInsets.symmetric(vertical: 16),
                    decoration: BoxDecoration(
                      color: Colors.grey[200],
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Text(
                      _form?.name ?? 'Loading...',
                      style: const TextStyle(
                        fontSize: 24,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
                  Expanded(
                    child: Card(
                      margin: EdgeInsets.zero,
                      child: ListView.separated(
                        itemCount: _form?.questions.length ?? 0,
                        separatorBuilder: (context, index) =>
                            const Divider(height: 1),
                        itemBuilder: (context, index) {
                          var element = _form!.questions[index];
                          return ListTile(
                            title: Text(element.description),
                          );
                        },
                      ),
                    ),
                  ),
                  Container(
                    width: double.infinity,
                    padding: const EdgeInsets.symmetric(vertical: 8),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                      children: [
                        Expanded(
                          child: FilledButton(
                            onPressed: () {
                              Navigator.pushNamed(context, '/feedback-result',
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
                              Navigator.pushNamed(context, '/attend-feedback',
                                  arguments: _form!.connectCode);
                            },
                            child: const Text('Beitreten'),
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
      ),
    );
  }
}
