import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:frontend/components/elements/quiz/single_choice_quiz_result.dart';
import 'package:frontend/components/general/quiz/QuizScoreboard.dart';
import 'package:frontend/global.dart';
import 'package:frontend/models/quiz/quiz_form.dart';
import 'package:frontend/models/quiz/quiz_question.dart';
import 'package:frontend/utils.dart';
import 'package:qr_flutter/qr_flutter.dart';
import 'package:web_socket_channel/web_socket_channel.dart';
import 'package:http/http.dart' as http;

class QuizControlPage extends StatefulWidget {
  final String courseId;
  final String formId;

  const QuizControlPage(
      {super.key, required this.courseId, required this.formId});

  @override
  State<QuizControlPage> createState() => _QuizControlPageState();
}

class _QuizControlPageState extends State<QuizControlPage> {
  bool _loading = true;

  late String _courseId;
  late String _formId;
  late String _userId;
  late List<String> _roles;

  late QuizForm _form;
  WebSocketChannel? _socketChannel;

  late List<Map<String, dynamic>> _results;
  late List<dynamic> _scoreboard;

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
    try {
      final response = await http.get(
        Uri.parse(
            "${getBackendUrl()}/course/$_courseId/quiz/form/$_formId?results=true"),
        headers: {
          "Content-Type": "application/json",
          "AUTHORIZATION": "Bearer ${getSession()!.jwt}",
        },
      );
      if (response.statusCode == 200) {
        var data = jsonDecode(response.body);
        print(data);
        var form = QuizForm.fromJson(data);

        startWebsocket();

        setState(() {
          _form = form;
          _results = getResults(data);
          if (_form.status == "FINISHED") {
            _scoreboard = getScoreboard(data);
          }
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
      print(event);
      var data = jsonDecode(event);
      if (data["action"] == "FORM_STATUS_CHANGED") {
        var form = QuizForm.fromJson(data["form"]);
        setState(() {
          _form.status = data["formStatus"];
          _form.currentQuestionIndex = form.currentQuestionIndex;
          _form.currentQuestionFinished = form.currentQuestionFinished;
          if (_form.status == "FINISHED") {
            _scoreboard = getScoreboard(data["form"]);
          }
        });
      }
      if (data["action"] == "RESULT_ADDED") {
        setState(() {
          _results = getResults(data["form"]);
        });
      }
      if (data["action"] == "CLOSED_QUESTION" ||
          data["action"] == "OPENED_NEXT_QUESTION") {
        var form = QuizForm.fromJson(data["form"]);
        setState(() {
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

  void next() {
    if (_socketChannel != null) {
      _socketChannel!.sink.add(jsonEncode({
        "action": "NEXT",
        "roles": _roles,
        "userId": _userId,
      }));
    }
  }

  List<Map<String, dynamic>> getResults(Map<String, dynamic> form) {
    List<dynamic> elements = form["questions"];
    return elements.map((element) {
      List<dynamic> results = element["results"];
      List<dynamic> resultValues =
          results.map((result) => result["values"][0]).toList();
      return {"values": resultValues};
    }).toList();
  }

  List<dynamic> getScoreboard(Map<String, dynamic> form) {
    List<dynamic> elements = form["participants"];
    int rank = 0;
    int lastScore = -1;
    List<dynamic> sortedElements = List.from(elements);
    sortedElements.sort((a, b) => b["score"] - a["score"]);
    return sortedElements.map((element) {
      if (lastScore != element["score"]) {
        rank++;
      }
      lastScore = element["score"];
      return {
        "userAlias": element["userAlias"],
        "score": element["score"],
        "rank": rank,
      };
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

    final appBar = AppBar(
      title: Text(_form.name,
          style: const TextStyle(
              color: Colors.white, fontWeight: FontWeight.bold)),
      backgroundColor: colors.primary,
    );

    if (_form.status == "NOT_STARTED") {
      var code = _form.connectCode;
      code = "${code.substring(0, 3)} ${code.substring(3, 6)}";

      return Scaffold(
        appBar: appBar,
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: <Widget>[
              QrImageView(
                data: code,
                version: QrVersions.auto,
                size: 200.0,
              ),
              Text(
                code,
                style: Theme.of(context).textTheme.headlineMedium,
              ),
              const SizedBox(height: 16),
              ElevatedButton(
                onPressed: startForm,
                child: const Text('Quiz starten'),
              ),
            ],
          ),
        ),
      );
    }

    if (_form.status == "FINISHED") {
      return Scaffold(
        appBar: appBar,
        body: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            children: <Widget>[
              Text(
                "Quiz beendet",
                style: Theme.of(context).textTheme.headlineMedium,
              ),
              const SizedBox(height: 16),
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(8),
                  child: QuizScoreboard(scoreboard: _scoreboard),
                ),
              ),
              const SizedBox(height: 16),
              ElevatedButton(
                onPressed: resetForm,
                child: const Text('Quiz zurücksetzen'),
              ),
            ],
          ),
        ),
      );
    }

    final QuizQuestion element =
        _form.questions[_form.currentQuestionIndex] as QuizQuestion;
    final values = _results[_form.currentQuestionIndex]["values"];

    return Scaffold(
      appBar: appBar,
      body: Stack(
        children: [
          SizedBox(
            width: double.infinity,
            child: Column(
              children: <Widget>[
                const SizedBox(height: 32),
                Padding(
                  padding: const EdgeInsets.all(16),
                  child: Container(
                    constraints: const BoxConstraints(maxWidth: 800),
                    child: Column(
                      children: <Widget>[
                        Text(element.name,
                            style: const TextStyle(
                                fontSize: 24, fontWeight: FontWeight.bold)),
                        Text(element.description,
                            style: const TextStyle(fontSize: 15),
                            textAlign: TextAlign.center),
                        const SizedBox(height: 16),
                        if (_form.currentQuestionFinished == true)
                          Card(
                            child: Padding(
                              padding: const EdgeInsets.all(16),
                              child: element.type == 'SINGLE_CHOICE'
                                  ? SingleChoiceQuizResult(
                                      results: values
                                          .map((e) => int.parse(e))
                                          .toList()
                                          .cast<int>(),
                                      options: element.options,
                                      correctAnswer: element.correctAnswers[0],
                                    )
                                  : element.type == 'YES_NO'
                                      ? SingleChoiceQuizResult(
                                          results: values
                                              .map((e) => e == "yes" ? 0 : 1)
                                              .toList()
                                              .cast<int>(),
                                          options: const ["Ja", "Nein"],
                                          correctAnswer:
                                              element.correctAnswers[0] == "yes"
                                                  ? "0"
                                                  : "1",
                                        )
                                      : Text(element.type),
                            ),
                          )
                      ],
                    ),
                  ),
                ),
                if (_form.status == "STARTED")
                  Column(
                    children: [
                      ElevatedButton(
                        onPressed: next,
                        child: const Text('Next'),
                      ),
                      const SizedBox(height: 8),
                      ElevatedButton(
                        onPressed: stopForm,
                        child: const Text('Quiz beenden'),
                      ),
                    ],
                  ),
                if (_form.status == "FINISHED")
                  Column(
                    children: [
                      ElevatedButton(
                        onPressed: startForm,
                        child: const Text('Quiz fortsetzen'),
                      ),
                      const SizedBox(height: 8),
                      ElevatedButton(
                        onPressed: resetForm,
                        child: Text('Quiz zurücksetzen',
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
      ),
    );
  }
}
