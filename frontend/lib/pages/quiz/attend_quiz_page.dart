import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:frontend/components/elements/quiz/single_choice_quiz.dart';
import 'package:frontend/components/elements/quiz/yes_no_quiz.dart';
import 'package:frontend/global.dart';
import 'package:frontend/models/quiz/quiz_form.dart';
import 'package:frontend/utils.dart';
import 'package:web_socket_channel/web_socket_channel.dart';
import 'package:http/http.dart' as http;

class AttendQuizPage extends StatefulWidget {
  final String code;

  const AttendQuizPage({super.key, required this.code});

  @override
  State<AttendQuizPage> createState() => _AttendQuizPageState();
}

class _AttendQuizPageState extends State<AttendQuizPage> {
  bool _loading = true;

  late String _courseId;
  late String _formId;
  late String _userId;
  late String _alias;

  late QuizForm _form;
  WebSocketChannel? _socketChannel;

  dynamic _value;
  bool _voted = false;

  @override
  void initState() {
    super.initState();

    _userId = getSession()!.userId;
    _alias = getSession()!.username;
    init();
  }

  Future init() async {
    var code = widget.code;
    try {
      final response = await http.get(
        Uri.parse("${getBackendUrl()}/connectto/quiz/$code"),
        headers: {
          "Content-Type": "application/json",
          "AUTHORIZATION": "Bearer ${getSession()!.jwt}",
        },
      );
      if (response.statusCode == 200) {
        var data = jsonDecode(response.body);
        _courseId = data["courseId"];
        _formId = data["formId"];
        if (await participate()) {
          fetchForm();
          return;
        }
      }
    } on http.ClientException catch (_) {
      // TODO: handle error
    }
    if (!mounted) return;
    Navigator.pop(context);
  }

  Future<bool> participate() async {
    try {
      final response = await http.post(
        Uri.parse(
            "${getBackendUrl()}/course/$_courseId/quiz/form/$_formId/participate"),
        headers: {
          "Content-Type": "application/json",
          "AUTHORIZATION": "Bearer ${getSession()!.jwt}",
        },
        body: _alias,
      );
      if (response.statusCode == 200) {
        return true;
      }
    } on http.ClientException catch (_) {
      // TODO: handle error
    }
    return false;
  }

  Future fetchForm() async {
    try {
      final response = await http.get(
        Uri.parse("${getBackendUrl()}/course/$_courseId/quiz/form/$_formId"),
        headers: {
          "Content-Type": "application/json",
          "AUTHORIZATION": "Bearer ${getSession()!.jwt}",
        },
      );
      if (response.statusCode == 200) {
        var data = jsonDecode(response.body);
        var form = QuizForm.fromJson(data);

        startWebsocket();

        setState(() {
          _form = form;
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
          "${getBackendUrl(protocol: "ws")}/course/$_courseId/quiz/form/$_formId/subscribe/$_userId/${getSession()!.jwt}"),
    );

    _socketChannel!.stream.listen((event) {
      var data = jsonDecode(event);
      if (data["action"] == "FORM_STATUS_CHANGED") {
        var form = QuizForm.fromJson(data["form"]);
        setState(() {
          _form.status = data["formStatus"];
          _value = null;
          _voted = false;
          _form.currentQuestionIndex = form.currentQuestionIndex;
          _form.currentQuestionFinished = form.currentQuestionFinished;
        });
      }
      if (data["action"] == "CLOSED_QUESTION" ||
          data["action"] == "OPENED_NEXT_QUESTION") {
        var form = QuizForm.fromJson(data["form"]);
        setState(() {
          _value = null;
          _voted = false;
          _form.currentQuestionIndex = form.currentQuestionIndex;
          _form.currentQuestionFinished = form.currentQuestionFinished;
        });
      }
    }, onError: (error) {
      setState(() {
        _form.status = "ERROR";
      });
    });
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

    if (_form.status != "STARTED" || _voted) {
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
              Padding(
                padding: const EdgeInsets.only(left: 20.0, right: 20.0),
                child: _form.status != "STARTED"
                    ? const Text("Bitte warten Sie bis das Quiz gestartet wird")
                    : const Text(
                        "Bitte warten Sie bis die nächste Frage gestellt wird"),
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

    if (_form.currentQuestionFinished) {
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
              const Padding(
                padding: EdgeInsets.only(left: 20.0, right: 20.0),
                child: Text(
                    "Bitte warten Sie bis die nächste Frage gestellt wird"),
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

    final element = _form.questions[_form.currentQuestionIndex];

    return Scaffold(
      appBar: AppBar(
        title: Text(_form.name,
            style: const TextStyle(
                color: Colors.white, fontWeight: FontWeight.bold)),
        backgroundColor: colors.primary,
      ),
      body: SizedBox(
        width: double.infinity,
        child: Column(
          children: [
            Padding(
              padding: const EdgeInsets.all(16.0),
              child: Column(
                children: <Widget>[
                  const SizedBox(height: 16),
                  Text(element.name,
                      style: const TextStyle(
                          fontSize: 24, fontWeight: FontWeight.bold)),
                  Text(element.description,
                      style: const TextStyle(fontSize: 15),
                      textAlign: TextAlign.center),
                  const SizedBox(height: 16),
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: element.type == 'SINGLE_CHOICE'
                          ? SingleChoiceQuiz(
                              options: element.options,
                              onSelectionChanged: (newValue) {
                                setState(() {
                                  _value = newValue;
                                });
                              },
                            )
                          : element.type == 'YES_NO'
                              ? YesNoQuiz(
                                  onSelectionChanged: (newValue) {
                                    setState(() {
                                      _value = newValue;
                                    });
                                  },
                                )
                              : Text(element.type),
                    ),
                  ),
                ],
              ),
            ),
            ElevatedButton(
              child: const Text('Senden'),
              onPressed: () {
                if (_value == null) {
                  return;
                }
                var message = {
                  "action": "ADD_RESULT",
                  "resultElementId": element.id,
                  "resultValues": [_value],
                  "role": "STUDENT"
                };
                _socketChannel?.sink.add(jsonEncode(message));
                setState(() {
                  _voted = true;
                });
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