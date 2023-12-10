import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';

import 'package:frontend/models/feedback/feedback_form.dart';
import 'package:frontend/utils.dart';

class FeedbackPreviewComponent extends StatefulWidget {
  final String channelId;
  final String formId;

  const FeedbackPreviewComponent({
    Key? key,
    required this.channelId,
    required this.formId,
  }) : super(key: key);

  @override
  _FeedbackPreviewComponentState createState() => _FeedbackPreviewComponentState();
}

class _FeedbackPreviewComponentState extends State<FeedbackPreviewComponent> {
  bool _loading = true;
  FeedbackForm? _form;

  @override
  void initState() {
    super.initState();
    fetchForm();
  }

  Future<void> fetchForm() async {
    try {
      final response = await http.get(Uri.parse(
          "${getBackendUrl()}/feedback/channel/${widget.channelId}/form/${widget.formId}"));
      if (response.statusCode == 200) {
        setState(() {
          _form = FeedbackForm.fromJson(json.decode(response.body));
          _loading = false;
        });
      } else {
        // TODO: Handle the case where the server returns a non-200 status code
        setState(() {
          _loading = false;
        });
      }
    } catch (e) {
      // TODO: Handle any exceptions
      setState(() {
        _loading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.primary,
        title: const Text(
          "Feedback History", 
          style: TextStyle(
            color: Colors.white, 
            fontWeight: FontWeight.bold
          )
        ),
      ),
      body: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16.0),
        child: _loading
            ? const Center(child: CircularProgressIndicator())
            : Column(
                children: [
                  Container(
                    width: double.infinity,
                    padding: EdgeInsets.all(16),
                    margin: EdgeInsets.symmetric(vertical: 16),
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
                        itemCount: _form?.feedbackElements.length ?? 0,
                        separatorBuilder: (context, index) => Divider(height: 1),
                        itemBuilder: (context, index) {
                          var element = _form!.feedbackElements[index];
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
                    child: FilledButton(
                      onPressed: () {
                        // TODO
                      },
                      child: const Text('Feedback starten'),
                    ),
                  ),
                ],
              ),
      ),
    );
  }
}
