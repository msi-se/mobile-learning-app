import 'package:flutter/material.dart';
import 'package:frontend/models/feedback/feedback_channel.dart';

class ChooseFeedbackChannel extends StatefulWidget {
  final List<FeedbackChannel> channels;
  final Function(String id) choose;

  const ChooseFeedbackChannel({super.key, required this.channels, required this.choose});

  @override
  State<ChooseFeedbackChannel> createState() => _ChooseFeedbackChannelState();
}

class _ChooseFeedbackChannelState extends State<ChooseFeedbackChannel> {


  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: ListView.builder(
        itemCount: widget.channels.length,
        itemBuilder: (context, index) {
          return ListTile(
            title: Text(widget.channels[index].name),
            subtitle: Text(widget.channels[index].description),
            trailing: const Icon(Icons.arrow_forward_ios),
            onTap: () {
              widget.choose(widget.channels[index].id);
            },
          );
        },
      ),
    );
  }
}
