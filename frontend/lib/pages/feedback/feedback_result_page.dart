import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:frontend/components/feedback/elements/slider_feedback_result.dart';
import 'package:frontend/components/feedback/elements/star_feedback_result.dart';
import 'package:frontend/models/feedback/feedback_form.dart';
import 'package:frontend/utils.dart';
import 'package:web_socket_channel/web_socket_channel.dart';
import 'package:http/http.dart' as http;

class FeedbackResultPage extends StatefulWidget {
  final String code;

  const FeedbackResultPage({super.key, required this.code});

  @override
  State<FeedbackResultPage> createState() => _FeedbackResultPageState();
}

class _FeedbackResultPageState extends State<FeedbackResultPage> {
  bool _loading = true;

  late String _channelId;
  late String _formId;
  static const userId = "6574ddd385c3896638153102";

  late FeedbackForm _form;
  late String _status;
  WebSocketChannel? _socketChannel;

  late List<Map<String, dynamic>> _results;

  @override
  void initState() {
    super.initState();

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
              "${getBackendUrl(protocol: "ws")}/feedback/channel/$_channelId/form/$_formId/subscribe/$userId"),
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

  List<Map<String, dynamic>> getTestResults(Map<String, dynamic> json) {
    List<dynamic> elements = json["elements"];
    return elements.map((element) {
      List<dynamic> results = element["results"];
      List<int> resultValues =
          results.map((result) => int.parse(result["value"])).toList();
      double average = resultValues.reduce((curr, next) => curr + next) /
          resultValues.length;
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
                              fontSize: 24, fontWeight: FontWeight.bold)),
                      Text(element.description,
                          style: const TextStyle(fontSize: 15),
                          textAlign: TextAlign.center),
                      if (element.type == 'STARS')
                        StarFeedbackResult(average: average)
                      else if (element.type == 'SLIDER')
                        SliderFeedbackResult(
                          results: values,
                          min: 0,
                          max: 10,
                        )
                      else
                        const Text('Unknown element type'),
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
      ),
    );
  }
}
// 