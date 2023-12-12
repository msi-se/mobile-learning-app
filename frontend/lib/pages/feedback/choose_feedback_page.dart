import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:frontend/components/feedback/choose_feedback_channel.dart';
import 'package:frontend/components/feedback/choose_feedback_form.dart';
import 'package:frontend/models/feedback/feedback_channel.dart';
import 'package:frontend/utils.dart';
import 'package:http/http.dart' as http;

class ChooseFeedbackPage extends StatefulWidget {
  const ChooseFeedbackPage({super.key});

  @override
  State<ChooseFeedbackPage> createState() => _ChooseFeedbackPageState();
}

class _ChooseFeedbackPageState extends State<ChooseFeedbackPage> {
  late List<FeedbackChannel> _channels;

  FeedbackChannel? _selectedChannel;

  bool _loading = true;

  @override
  void initState() {
    super.initState();

    fetchChannels();
  }

  Future fetchChannels() async {
    try {
      final response =
          await http.get(Uri.parse("${getBackendUrl()}/feedback/channel"));
      if (response.statusCode == 200) {
        var data = jsonDecode(response.body);
        setState(() {
          _channels = getChannelsFromJson(data);
          _loading = false;
        });
      }
    } on http.ClientException catch (_) {
      // TODO: handle error
    }
  }

  List<FeedbackChannel> getChannelsFromJson(List<dynamic> json) {
    return json.map((e) => FeedbackChannel.fromJson(e)).toList();
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
          title: Text(
              _selectedChannel != null
                  ? _selectedChannel!.name
                  : "Feedbackbogen auswählen",
              style: const TextStyle(
                  color: Colors.white, fontWeight: FontWeight.bold)),
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
                  Navigator.pushNamed(context, '/feedback-info', arguments: {
                    "channelId": _selectedChannel!.id,
                    "formId": id,
                  });
                },
              ),
      ),
    );
  }
}
