import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:frontend/components/feedback/elements/slider_feedback.dart';
import 'package:frontend/components/feedback/elements/star_feedback.dart';
import 'package:frontend/models/feedback/feedback_form.dart';
import 'package:frontend/utils.dart';
import 'package:web_socket_channel/web_socket_channel.dart';
import 'package:http/http.dart' as http;

class AttendFeedbackPage extends StatefulWidget {
  const AttendFeedbackPage({super.key});

  @override
  State<AttendFeedbackPage> createState() => _AttendFeedbackPageState();
}

class _AttendFeedbackPageState extends State<AttendFeedbackPage> {
  bool _loading = true;

  static const channelId = "6573a251ed01282ce7782bca";
  static const formId = "6573a251ed01282ce7782bc9";
  static const studentUserId = "6573a251ed01282ce7782bcc";

  late FeedbackForm _form;
  late String _status;
  late WebSocketChannel _socketChannel;

  Map<String, dynamic> feedbackValues = {};

  @override
  void initState() {
    super.initState();

    fetchForm();
  }

  Future fetchForm() async {
    try {
      final response = await http.get(Uri.parse(
          "${getBackendUrl()}/feedback/channel/$channelId/form/$formId"));
      if (response.statusCode == 200) {
        var data = jsonDecode(response.body);

        _socketChannel = WebSocketChannel.connect(
          Uri.parse(
              "${getBackendUrl(protocol: "ws")}/feedback/channel/$channelId/form/$formId/subscribe/$studentUserId"),
        );

        _socketChannel.stream.listen((event) {
          var data = jsonDecode(event);
          if (data["action"] == "FORM_STATUS_CHANGED") {
            setState(() {
              _status = data["formStatus"];
            });
          }
        });

        setState(() {
          _form = FeedbackForm.fromJson(data);
          _status = data["status"];
          _loading = false;
        });
      }
    } on http.ClientException catch (_) {
      // TODO: handle error
    }
  }

  @override
  void dispose() {
    _socketChannel.sink.close();
    super.dispose();
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

    final colors = Theme.of(context).colorScheme;

    if (_status != "STARTED") {
      return Scaffold(
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: <Widget>[
              const Padding(
                padding: EdgeInsets.only(left: 20.0, right: 20.0),
                child: Text(
                    "Bitte warten Sie bis die Feedbackrunde gestartet wird"),
              ),
              Padding(
                padding: const EdgeInsets.all(20.0),
                child: LinearProgressIndicator(
                  valueColor: AlwaysStoppedAnimation<Color>(colors.primary),
                  backgroundColor: colors.secondary.withAlpha(32),
                ),
              ),
            ],
          ),
        ),
      );
    }

    return Scaffold(
      body: Center(
        child: Column(
          children: [
            ListView.builder(
              shrinkWrap: true,
              physics: const NeverScrollableScrollPhysics(),
              itemCount: _form.feedbackElements.length,
              itemBuilder: (context, index) {
                var element = _form.feedbackElements[index];
                return Padding(
                  padding: const EdgeInsets.all(32.0),
                  child: Column(
                    children: <Widget>[
                      Text(element.name,
                          style: const TextStyle(
                              fontSize: 24, fontWeight: FontWeight.bold)),
                      Text(element.description,
                          style: const TextStyle(fontSize: 15),
                          textAlign: TextAlign.center),
                      if (element.type == 'STARS')
                        StarFeedback(
                          rating: 3,
                          onRatingChanged: (newRating) {
                            setState(() {
                              feedbackValues[element.id] = newRating;
                            });
                          },
                        )
                      else if (element.type == 'SLIDER')
                        SliderFeedback(onFeedbackChanged: (newFeedback) {
                          setState(() {
                            feedbackValues[element.id] = newFeedback;
                          });
                        })
                      else
                        const Text('Unknown element type')
                    ],
                  ),
                );
              },
            ),
            ElevatedButton(
              child: const Text('Senden'),
              onPressed: () {
                // Iterate over the feedbackValues Map and send each feedback value to the socket
                for (var entry in feedbackValues.entries) {
                  var message = {
                    "action": "ADD_RESULT",
                    "resultElementId": entry.key,
                    "resultValue": entry.value,
                    "role": "STUDENT"
                  };
                  _socketChannel.sink.add(jsonEncode(message));
                }
              },
            )
          ],
        ),
      ),
    );
  }
}
// 