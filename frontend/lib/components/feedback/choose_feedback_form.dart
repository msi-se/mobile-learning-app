import 'package:flutter/material.dart';
import 'package:frontend/models/feedback/feedback_channel.dart';

class ChooseFeedbackForm extends StatefulWidget {
  final FeedBackChannel channel;
  final Function(String id) choose;

  const ChooseFeedbackForm({super.key, required this.channel, required this.choose});

  @override
  State<ChooseFeedbackForm> createState() => _ChooseFeedbackFormState();
}

class _ChooseFeedbackFormState extends State<ChooseFeedbackForm> {
  @override
  Widget build(BuildContext context) {
    var forms = widget.channel.feedbackForms;

    return Scaffold(
      body: ListView.builder(
        itemCount: forms.length,
        itemBuilder: (context, index) {
          return ListTile(
            title: Text(forms[index].name),
            subtitle: Text(forms[index].description),
            trailing: const Icon(Icons.arrow_forward_ios),
            // TODO: display status of form
            onTap: () {
              widget.choose(forms[index].id);
            },
          );
        },
      ),
    );
  }
}
