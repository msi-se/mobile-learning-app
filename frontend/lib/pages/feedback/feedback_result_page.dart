import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:frontend/components/feedback/elements/single_choice_feedback_result.dart';
import 'package:frontend/components/feedback/elements/slider_feedback_result.dart';
import 'package:frontend/components/feedback/elements/star_feedback_result.dart';
import 'package:frontend/global.dart';
import 'package:frontend/models/feedback/feedback_form.dart';
import 'package:frontend/utils.dart';
import 'package:web_socket_channel/web_socket_channel.dart';
import 'package:http/http.dart' as http;

class FeedbackResultPage extends StatefulWidget {
  final String channelId;
  final String formId;

  const FeedbackResultPage(
      {super.key, required this.channelId, required this.formId});

  @override
  State<FeedbackResultPage> createState() => _FeedbackResultPageState();
}

class _FeedbackResultPageState extends State<FeedbackResultPage> {
  bool _loading = true;

  late String _channelId;
  late String _formId;
  late String _userId;

  late FeedbackForm _form;
  late String _status;
  WebSocketChannel? _socketChannel;

  late List<Map<String, dynamic>> _results;

  @override
  void initState() {
    super.initState();

    _userId = getSession()!.userId;
    init();
  }

  Future init() async {
    _channelId = widget.channelId;
    _formId = widget.formId;
    fetchForm();
  }

  Future fetchForm() async {
    try {
      final response = await http.get(Uri.parse(
          "${getBackendUrl()}/feedback/channel/$_channelId/form/$_formId"));
      if (response.statusCode == 200) {
        var data = jsonDecode(response.body);
        startWebsocket();
        setState(() {
          _form = FeedbackForm.fromJson(data);
          _results = getTestResults(data);
          _status = data["status"];
          _loading = false;
        });
      }
    } on http.ClientException catch (_) {
      // TODO: handle error
    }
  }

  void startWebsocket() {
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
      if (data["action"] == "RESULT_ADDED") {
        setState(() {
          _results = getTestResults(data["form"]);
        });
      }
    }, onError: (error) {
      setState(() {
        _status = "ERROR";
      });
    });
  }

  void startForm() {
    if (_socketChannel != null) {
      _socketChannel!.sink.add(jsonEncode({
        "action": "CHANGE_FORM_STATUS",
        "formStatus": "STARTED",
        "role": "PROF",
        "userId": _userId,
      }));
    }
  }

  void stopForm() {
    if (_socketChannel != null) {
      _socketChannel!.sink.add(jsonEncode({
        "action": "CHANGE_FORM_STATUS",
        "formStatus": "FINISHED",
        "role": "PROF",
        "userId": _userId,
      }));
    }
  }

  void resetForm() {
    if (_socketChannel != null) {
      _socketChannel!.sink.add(jsonEncode({
        "action": "CHANGE_FORM_STATUS",
        "formStatus": "NOT_STARTED",
        "role": "PROF",
        "userId": _userId,
      }));
    }
  }

  List<Map<String, dynamic>> getTestResults(Map<String, dynamic> json) {
    List<dynamic> elements = json["elements"];
    return elements.map((element) {
      List<dynamic> results = element["results"];
      List<int> resultValues =
          results.map((result) => int.parse(result["value"])).toList();
      double average = 0;
      if (resultValues.isNotEmpty) {
        average = resultValues.reduce((curr, next) => curr + next) /
            resultValues.length;
      }
      return {"values": resultValues, "average": average};
    }).toList();
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

    if (_status == "NOT_STARTED") {
      var code = _form.connectCode;
      code = "${code.substring(0, 3)} ${code.substring(3, 6)}";

      return Scaffold(
        appBar: AppBar(
          title: Text(_form.name,
              style: const TextStyle(
                  color: Colors.white, fontWeight: FontWeight.bold)),
          backgroundColor: colors.primary,
        ),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: <Widget>[
              Text(
                code,
                style: Theme.of(context).textTheme.headlineMedium,
              ),
              const SizedBox(height: 16),
              ElevatedButton(
                onPressed: startForm,
                child: const Text('Feedback starten'),
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
        body: Stack(
          children: [
            SingleChildScrollView(
              child: Column(
                children: <Widget>[
                  const SizedBox(height: 16),
                  Column(
                    children: [
                      ListView.builder(
                        shrinkWrap: true,
                        physics: const NeverScrollableScrollPhysics(),
                        itemCount: _form.feedbackElements.length,
                        itemBuilder: (context, index) {
                          final element = _form.feedbackElements[index];
                          final double average = _results[index]["average"];
                          final roundAverage = (average * 100).round() / 100;
                          final values = _results[index]["values"];
                          return Padding(
                            padding: const EdgeInsets.all(32.0),
                            child: Column(
                              children: <Widget>[
                                Text(element.name,
                                    style: const TextStyle(
                                        fontSize: 24,
                                        fontWeight: FontWeight.bold)),
                                Text(element.description,
                                    style: const TextStyle(fontSize: 15),
                                    textAlign: TextAlign.center),
                                if (element.type == 'STARS')
                                  StarFeedbackResult(average: average)
                                else if (element.type == 'SLIDER')
                                  SliderFeedbackResult(
                                    results: values,
                                    average: average,
                                    min: 0,
                                    max: 10,
                                  )
                                else if (element.type == 'SINGLE_CHOICE')
                                  SingleChoiceFeedbackResult(
                                    results: values,
                                    options: element.options,
                                  )
                                else
                                  const Text('Unknown element type'),
                                if (element.type == 'STARS' ||
                                    element.type == 'SLIDER')
                                  Text("$roundAverage",
                                      style: const TextStyle(fontSize: 20),
                                      textAlign: TextAlign.center),
                              ],
                            ),
                          );
                        },
                      ),
                    ],
                  ),
                  if (_status == "STARTED")
                    ElevatedButton(
                      onPressed: stopForm,
                      child: const Text('Feedback beenden'),
                    ),
                  if (_status == "FINISHED")
                    Column(
                      children: [
                        ElevatedButton(
                          onPressed: startForm,
                          child: const Text('Feedback fortsetzen'),
                        ),
                        const SizedBox(height: 8),
                        ElevatedButton(
                          onPressed: resetForm,
                          child: Text('Feedback zurücksetzen',
                              style: TextStyle(color: colors.error)),
                        ),
                      ],
                    ),
                  const SizedBox(height: 32),
                ],
              ),
            ),
            Positioned(
              top: 0,
              left: 0,
              right: 0,
              child: Container(
                color: colors.surfaceVariant,
                child: Text(
                  "${_form.connectCode.substring(0, 3)} ${_form.connectCode.substring(3, 6)}",
                  style: Theme.of(context).textTheme.headlineSmall,
                  textAlign: TextAlign.center,
                ),
              ),
            )
          ],
        ));
  }
}
// 