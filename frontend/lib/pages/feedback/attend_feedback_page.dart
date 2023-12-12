import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:frontend/components/feedback/elements/slider_feedback.dart';
import 'package:frontend/components/feedback/elements/star_feedback.dart';
import 'package:frontend/global.dart';
import 'package:frontend/models/feedback/feedback_form.dart';
import 'package:frontend/utils.dart';
import 'package:web_socket_channel/web_socket_channel.dart';
import 'package:http/http.dart' as http;

class AttendFeedbackPage extends StatefulWidget {
  final String code;

  const AttendFeedbackPage({super.key, required this.code});

  @override
  State<AttendFeedbackPage> createState() => _AttendFeedbackPageState();
}

class _AttendFeedbackPageState extends State<AttendFeedbackPage> {
  bool _loading = true;

  late String _channelId;
  late String _formId;
  late String _userId;

  late FeedbackForm _form;
  late String _status;
  WebSocketChannel? _socketChannel;

  final Map<String, dynamic> _feedbackValues = {};

  @override
  void initState() {
    super.initState();

    _userId = getSession()!.userId;
    init();
  }

  Future init() async {
    var code = widget.code;
    try {
      final response = await http
          .get(Uri.parse("${getBackendUrl()}/feedback/connectto/$code"));
      if (response.statusCode == 200) {
        var data = jsonDecode(response.body);
        _channelId = data["channelId"];
        _formId = data["formId"];
        fetchForm();
        return;
      }
    } on http.ClientException catch (_) {
      // TODO: handle error
    }
    if (!mounted) return;
    Navigator.pop(context);
  }

  Future fetchForm() async {
    try {
      final response = await http.get(Uri.parse(
          "${getBackendUrl()}/feedback/channel/$_channelId/form/$_formId"));
      if (response.statusCode == 200) {
        var data = jsonDecode(response.body);

        _socketChannel = WebSocketChannel.connect(
          Uri.parse(
              "${getBackendUrl(protocol: "ws")}/feedback/channel/$_channelId/form/$_formId/subscribe/$_userId"),
        );

        _socketChannel!.stream.listen((event) {
          var data = jsonDecode(event);
          if (data["action"] == "FORM_STATUS_CHANGED") {
            setState(() {
              _status = data["formStatus"];
            });
          }
        }, onError: (error) {
          setState(() {
            _status = "ERROR";
          });
        });

        var form = FeedbackForm.fromJson(data);
        for (var element in form.feedbackElements) {
          if (element.type == "STARS") {
            _feedbackValues[element.id] = 3;
          } else if (element.type == "SLIDER") {
            _feedbackValues[element.id] = 5;
          }
        }

        setState(() {
          _form = form;
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
    _socketChannel?.sink.close();
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
      appBar: AppBar(
        title: Text(_form.name,
            style: const TextStyle(
                color: Colors.white, fontWeight: FontWeight.bold)),
        backgroundColor: colors.primary,
      ),
      body: SingleChildScrollView(
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
                          initialRating: _feedbackValues[element.id],
                          onRatingChanged: (newRating) {
                            setState(() {
                              _feedbackValues[element.id] = newRating;
                            });
                          },
                        )
                      else if (element.type == 'SLIDER')
                        SliderFeedback(
                            initialFeedback: _feedbackValues[element.id],
                            onFeedbackChanged: (newFeedback) {
                              setState(() {
                                _feedbackValues[element.id] = newFeedback;
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
                for (var entry in _feedbackValues.entries) {
                  var message = {
                    "action": "ADD_RESULT",
                    "resultElementId": entry.key,
                    "resultValue": entry.value,
                    "role": "STUDENT"
                  };
                  _socketChannel?.sink.add(jsonEncode(message));
                }
              },
            ),
            const SizedBox(height: 32),
          ],
        ),
      ),
    );
  }
}
// 