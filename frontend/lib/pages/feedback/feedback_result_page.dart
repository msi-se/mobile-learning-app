import 'dart:convert';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:frontend/components/elements/feedback/fulltext_feedback_result.dart';
import 'package:frontend/components/elements/feedback/single_choice_feedback_result.dart';
import 'package:frontend/components/elements/feedback/slider_feedback_result.dart';
import 'package:frontend/components/elements/feedback/star_feedback_result.dart';
import 'package:frontend/components/error/general_error_widget.dart';
import 'package:frontend/components/error/network_error_widget.dart';
import 'package:frontend/global.dart';
import 'package:frontend/models/feedback/feedback_form.dart';
import 'package:frontend/models/feedback/feedback_question.dart';
import 'package:frontend/utils.dart';
import 'package:web_socket_channel/web_socket_channel.dart';
import 'package:http/http.dart' as http;

class FeedbackResultPage extends StatefulWidget {
  final String courseId;
  final String formId;

  const FeedbackResultPage(
      {super.key, required this.courseId, required this.formId});

  @override
  State<FeedbackResultPage> createState() => _FeedbackResultPageState();
}

// helper function to convert a list of strings to a list of integers
List<int> convertStringListToIntList(List<String> list) {
  List<int> result = [];
  for (var value in list) {
    try {
      result.add(int.parse(value));
    } catch (e) {
      return [];
    }
  }
  return result;
}

class _FeedbackResultPageState extends State<FeedbackResultPage> {
  late String _courseId;
  late String _formId;
  late String _userId;
  late List<String> _roles;

  late FeedbackForm _form;
  WebSocketChannel? _socketChannel;

  late List<Map<String, dynamic>> _results;

  bool _loading = false;
  String _fetchResult = '';

  @override
  void initState() {
    super.initState();

    _userId = getSession()!.userId;
    _roles = getSession()!.roles;
    init();
  }

  Future init() async {
    _courseId = widget.courseId;
    _formId = widget.formId;
    fetchForm();
  }

  Future fetchForm() async {
    setState(() {
      _loading = true;
      _fetchResult = '';
    });
    try {
      final response = await http.get(
        Uri.parse(
            "${getBackendUrl()}/course/$_courseId/feedback/form/$_formId?results=true"),
        headers: {
          "Content-Type": "application/json",
          "AUTHORIZATION": "Bearer ${getSession()!.jwt}",
        },
      );
      if (response.statusCode == 200) {
        var data = jsonDecode(response.body);
        var form = FeedbackForm.fromJson(data);

        startWebsocket();

        setState(() {
          _form = form;
          _results = getResults(data);
          _loading = false;
          _fetchResult = 'success';
        });
      }
    } on http.ClientException {
      setState(() {
        _loading = false;
        _fetchResult = 'network_error';
      });
    } on SocketException {
      setState(() {
        _loading = false;
        _fetchResult = 'network_error';
      });
    } catch (e) {
      setState(() {
        _loading = false;
        _fetchResult = 'general_error';
      });
    }
  }

  void _showErrorDialog(String errorType) {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      showDialog(
          context: context,
          barrierDismissible: false,
          builder: (BuildContext context) {
            return (errorType == 'network_error')
                ? const NetworkErrorWidget()
                : const GeneralErrorWidget();
          }).then((value) {
        if (value == 'back') {
          Navigator.pushReplacementNamed(
              context, '/feedback-info'); // TODO: Where should you go here?
        }
      });
    });
  }

  void startWebsocket() {
    _socketChannel = WebSocketChannel.connect(
      Uri.parse(
          "${getBackendUrl(protocol: "ws")}/course/$_courseId/feedback/form/$_formId/subscribe/$_userId/${getSession()!.jwt}"),
    );

    _socketChannel!.stream.listen((event) {
      var data = jsonDecode(event);
      if (data["action"] == "FORM_STATUS_CHANGED") {
        setState(() {
          _form.status = data["formStatus"];
        });
      }
      if (data["action"] == "RESULT_ADDED") {
        setState(() {
          _results = getResults(data["form"]);
        });
      }
    }, onError: (error) {
      //TODO: Should there be another error handling for this?
      setState(() {
        _form.status = "ERROR";
      });
    });
  }

  void startForm() {
    if (_socketChannel != null) {
      _socketChannel!.sink.add(jsonEncode({
        "action": "CHANGE_FORM_STATUS",
        "formStatus": "STARTED",
        "roles": _roles,
        "userId": _userId,
      }));
    }
  }

  void stopForm() {
    if (_socketChannel != null) {
      _socketChannel!.sink.add(jsonEncode({
        "action": "CHANGE_FORM_STATUS",
        "formStatus": "FINISHED",
        "roles": _roles,
        "userId": _userId,
      }));
    }
  }

  void resetForm() {
    if (_socketChannel != null) {
      _socketChannel!.sink.add(jsonEncode({
        "action": "CHANGE_FORM_STATUS",
        "formStatus": "NOT_STARTED",
        "roles": _roles,
        "userId": _userId,
      }));
    }
  }

  List<Map<String, dynamic>> getResults(Map<String, dynamic> json) {
    List<dynamic> elements = json["questions"];
    return elements.map((element) {
      List<dynamic> results = element["results"];
      List<String> resultValues =
          results.map((result) => (result["values"][0]).toString()).toList();
      double average = 0;
      if (resultValues.isNotEmpty) {
        // try convert the values to int and calculate the average
        List<int> resultValuesInts = convertStringListToIntList(resultValues);

        if (resultValuesInts.isNotEmpty) {
          average = resultValuesInts.reduce((curr, next) => curr + next) /
              resultValues.length;
        }
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
    } else if (_fetchResult == 'success') {
      final colors = Theme.of(context).colorScheme;

      final appbar = AppBar(
        title: Text(_form.name,
            style: const TextStyle(
                color: Colors.white, fontWeight: FontWeight.bold)),
        backgroundColor: colors.primary,
      );

      if (_form.status == "NOT_STARTED") {
        var code = _form.connectCode;
        code = "${code.substring(0, 3)} ${code.substring(3, 6)}";

        return Scaffold(
          appBar: appbar,
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
          appBar: appbar,
          body: Stack(
            children: [
              SingleChildScrollView(
                child: SizedBox(
                  width: double.infinity,
                  child: Column(
                    children: <Widget>[
                      const SizedBox(height: 16),
                      Container(
                        constraints: const BoxConstraints(maxWidth: 1600),
                        padding: const EdgeInsets.all(16),
                        child: Wrap(
                          alignment: WrapAlignment.spaceEvenly,
                          spacing: 16.0,
                          runSpacing: 16.0,
                          children: List<Widget>.generate(
                            _form.questions.length,
                            (index) {
                              final element =
                                  _form.questions[index] as FeedbackQuestion;
                              final double average = _results[index]["average"];
                              final roundAverage =
                                  (average * 100).round() / 100;
                              final values = _results[index]["values"];
                              return SizedBox(
                                width: MediaQuery.of(context).size.width < 600
                                    ? double.infinity
                                    : 600,
                                child: Card(
                                  color: colors.surface,
                                  child: Padding(
                                    padding: const EdgeInsets.all(16.0),
                                    child: Column(
                                      crossAxisAlignment:
                                          CrossAxisAlignment.center,
                                      children: <Widget>[
                                        Text('${index + 1}. ${element.name}',
                                            style: const TextStyle(
                                                fontSize: 24,
                                                fontWeight: FontWeight.bold),
                                            textAlign: TextAlign.center),
                                        const SizedBox(height: 8),
                                        Text(element.description,
                                            style:
                                                const TextStyle(fontSize: 15),
                                            textAlign: TextAlign.center),
                                        const SizedBox(height: 16),
                                        if (element.type == 'STARS')
                                          StarFeedbackResult(average: average)
                                        else if (element.type == 'SLIDER')
                                          SliderFeedbackResult(
                                            results: convertStringListToIntList(
                                                values),
                                            rangeLow: element.rangeLow,
                                            rangeHigh: element.rangeHigh,
                                            average: average,
                                            min: 0,
                                            max: 10,
                                          )
                                        else if (element.type ==
                                            'SINGLE_CHOICE')
                                          SingleChoiceFeedbackResult(
                                            results: convertStringListToIntList(
                                                values),
                                            options: element.options,
                                          )
                                        else if (element.type == 'FULLTEXT')
                                          FulltextFeedbackResult(
                                              results: values)
                                        else
                                          const Text('Unknown element type',
                                              textAlign: TextAlign.center),
                                        if (element.type == 'STARS' ||
                                            element.type == 'SLIDER')
                                          Text("$roundAverage",
                                              style:
                                                  const TextStyle(fontSize: 20),
                                              textAlign: TextAlign.center),
                                      ],
                                    ),
                                  ),
                                ),
                              );
                            },
                          ),
                        ),
                      ),
                      if (_form.status == "STARTED")
                        ElevatedButton(
                          onPressed: stopForm,
                          child: const Text('Feedback beenden'),
                        ),
                      if (_form.status == "FINISHED")
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
              ),
            ],
          ));
    } else {
      _showErrorDialog(_fetchResult);
      return Container();
    }
  }
}
