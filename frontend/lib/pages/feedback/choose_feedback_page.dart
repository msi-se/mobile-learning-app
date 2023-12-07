import 'package:flutter/material.dart';
import 'package:frontend/components/feedback/choose_feedback_channel.dart';
import 'package:frontend/components/feedback/choose_feedback_form.dart';
import 'package:frontend/models/feedback/feedback_channel.dart';

class ChooseFeedbackPage extends StatefulWidget {
  const ChooseFeedbackPage({super.key});

  @override
  State<ChooseFeedbackPage> createState() => _ChooseFeedbackPageState();
}

class _ChooseFeedbackPageState extends State<ChooseFeedbackPage> {
  late List<FeedBackChannel> _channels;

  FeedBackChannel? _selectedChannel;

  bool _loading = true;

  @override
  void initState() {
    super.initState();
    setState(() {
      _channels = getChannelsFromJson([
        {
          "id": "channel1",
          "name": "Channel 1",
          "description": "Channel 1 Description",
          "feedbackForms": [
            {
              "id": "form1",
              "name": "Form 1",
              "description": "Form 1 Description",
            },
            {
              "id": "form2",
              "name": "Form 2",
              "description": "Form 2 Description",
            },
          ]
        },
        {
          "id": "channel2",
          "name": "Channel 2",
          "description": "Channel 2 Description",
          "feedbackForms": []
        },
        {
          "id": "channel3",
          "name": "Channel 3",
          "description": "Channel 3 Description",
          "feedbackForms": []
        },
      ]);
      _loading = false;
    });
  }

  List<FeedBackChannel> getChannelsFromJson(List<dynamic> json) {
    return json.map((e) => FeedBackChannel.fromJson(e)).toList();
  }

  @override
  Widget build(BuildContext context) {
    if (_loading) {
      return const Scaffold(
        body: Center(
          child: CircularProgressIndicator(),
        ),
      );
    }

    return PopScope(
      canPop: false,
      onPopInvoked: (bool didPop) {
        if (didPop) {
          return;
        }
        if (_selectedChannel != null) {
          setState(() {
            _selectedChannel = null;
          });
        } else {
          Navigator.pop(context);
        }
      },
      child: Scaffold(
        appBar: AppBar(
          backgroundColor: Theme.of(context).colorScheme.primary,
          title: Text(_selectedChannel != null
              ? _selectedChannel!.name
              : "Feedbackbogen auswählen"),
        ),
        // display _channels in list view with clickable tiles
        body: _selectedChannel == null
            ? ChooseFeedbackChannel(
                channels: _channels,
                choose: (id) {
                  setState(() {
                    _selectedChannel =
                        _channels.firstWhere((element) => element.id == id);
                  });
                },
              )
            : ChooseFeedbackForm(
                channel: _selectedChannel!,
                choose: (id) {
                  // Navigator.pushNamed(context, '/feedback', arguments: id);
                },
              ),
      ),
    );
  }
}
