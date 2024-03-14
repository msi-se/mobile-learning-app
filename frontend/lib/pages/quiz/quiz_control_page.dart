import 'dart:convert';
import 'dart:io';
import 'dart:async';

import 'package:flutter/material.dart';
import 'package:frontend/theme/assets.dart';
import 'package:frontend/components/elements/quiz/single_choice_quiz_result.dart';
import 'package:frontend/components/error/general_error_widget.dart';
import 'package:frontend/components/error/network_error_widget.dart';
import 'package:frontend/components/general/quiz/QuizScoreboard.dart';
import 'package:frontend/global.dart';
import 'package:frontend/models/quiz/quiz_form.dart';
import 'package:frontend/models/quiz/quiz_question.dart';
import 'package:frontend/utils.dart';
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
  late String _courseId;
  late String _formId;
  late String _userId;
  late List<String> _roles;

  late QuizForm _form;
  WebSocketChannel? _socketChannel;

  late List<Map<String, dynamic>> _results;
  late List<dynamic> _scoreboard;

  int _participantCounter = 0;
  List<dynamic> _userNames = [];

  bool _isPhone = false;

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
            "${getBackendUrl()}/course/$_courseId/quiz/form/$_formId?results=true"),
        headers: {
          "Content-Type": "application/json",
          "AUTHORIZATION": "Bearer ${getSession()!.jwt}",
        },
      );
      if (response.statusCode == 200) {
        var data = jsonDecode(response.body);
        var form = QuizForm.fromJson(data);

        _form = form;
        _results = getResults(data);
        _participantCounter = data["participants"].length;
        _userNames = data["participants"]
            .map((participant) => participant["userAlias"])
            .toList();
        if (_form.status == "FINISHED") {
          _scoreboard = getScoreboard(data);
        }

        startWebsocket();

        setState(() {
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
          Navigator.pushReplacementNamed(context, '/main');
        }
      });
    });
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
      if (data["action"] == "PARTICIPANT_JOINED") {
        List<dynamic> participants = data["form"]["participants"];
        setState(() {
          _participantCounter = participants.length;
          _userNames = participants
              .map((participant) => participant["userAlias"])
              .toList();
        });
      }
    }, onError: (error) {
      //TODO: Should there be another error handling for this?
      setState(() {
        _form.status = "ERROR";
      });
    });

    if (_form.status == "NOT_STARTED") {
      openWaitingRoom();
    }
  }

  void openWaitingRoom() {
    if (_socketChannel != null) {
      _socketChannel!.sink.add(jsonEncode({
        "action": "CHANGE_FORM_STATUS",
        "formStatus": "WAITING",
        "roles": _roles,
        "userId": _userId,
      }));
    }
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
    Navigator.pop(context);
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
    double screenWidth = MediaQuery.of(context).size.width;
    int crossAxisCount = screenWidth > 600 ? 2 : 1;
    double childAspectRatio = screenWidth > 600 ? 16 : 12;
    if (screenWidth < 600) {
      _isPhone = true;
    }
    if (_loading) {
      return const Scaffold(
        body: Center(
          child: CircularProgressIndicator(),
        ),
      );
    } else if (_fetchResult == 'success') {
      final colors = Theme.of(context).colorScheme;

      final appBar = AppBar(
        title: Text(_form.name,
            style: const TextStyle(
                color: Colors.white, fontWeight: FontWeight.bold)),
        backgroundColor: colors.primary,
      );

      if (_form.status == "NOT_STARTED" || _form.status == "WAITING") {
        var code = _form.connectCode;
        code = "${code.substring(0, 3)} ${code.substring(3, 6)}";

        return Scaffold(
            appBar: appBar,
            body: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: <Widget>[
                  const SizedBox(height: 50),
                  Card(
                    surfaceTintColor: Colors.white,
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(10.0),
                    ),
                    elevation: 3,
                    child: Padding(
                      padding: const EdgeInsets.only(
                          top: 10, bottom: 10, left: 60, right: 60),
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        children: <Widget>[
                          const Text(
                            'Beitritt zum Quiz',
                          ),
                          const SizedBox(height: 10),
                          Text(
                            code,
                            style: const TextStyle(
                                fontSize: 35, fontWeight: FontWeight.bold),
                          ),
                        ],
                      ),
                    ),
                  ),
                  const SizedBox(height: 20),
                  Text('Teilnehmer: $_participantCounter'),
                  const SizedBox(height: 20),
                  Expanded(
                    child: Padding(
                      padding: EdgeInsets.symmetric(
                          horizontal: _isPhone ? 20 : screenWidth * 0.2),
                      child: Container(
                        decoration: BoxDecoration(
                          borderRadius:
                              const BorderRadius.all(Radius.circular(20)),
                          border: Border.all(
                            color: colors.outlineVariant,
                            width: 0.5,
                          ),
                        ),
                        child: GridView.count(
                          crossAxisCount: crossAxisCount,
                          mainAxisSpacing: 5.0,
                          crossAxisSpacing: 5.0,
                          childAspectRatio: childAspectRatio,
                          children: List.generate(_userNames.length, (index) {
                            return Center(
                              child: Container(
                                padding: const EdgeInsets.symmetric(
                                    horizontal: 10, vertical: 3),
                                decoration: BoxDecoration(
                                  color:
                                      const Color.fromARGB(255, 165, 224, 211),
                                  borderRadius: BorderRadius.circular(20.0),
                                ),
                                child: Text(
                                  _userNames[index],
                                  style: const TextStyle(
                                      fontSize: 14,
                                      fontWeight: FontWeight.bold),
                                ),
                              ),
                            );
                          }),
                        ),
                      ),
                    ),
                  ),
                  const SizedBox(height: 30),
                  ElevatedButton(
                    onPressed: startForm,
                    style: ElevatedButton.styleFrom(
                      backgroundColor: colors.surfaceTint,
                    ),
                    child: const Text(
                      'Start',
                      style: TextStyle(
                        color: Colors.white,
                      ),
                    ),
                  ),
                  const SizedBox(height: 50)
                ]));
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
                Center(
                  child: Container(
                    constraints: const BoxConstraints(maxWidth: 800),
                    child: Card(
                      child: Padding(
                        padding: const EdgeInsets.all(8),
                        child: QuizScoreboard(scoreboard: _scoreboard),
                      ),
                    ),
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
                                        correctAnswer:
                                            element.correctAnswers[0],
                                      )
                                    : element.type == 'YES_NO'
                                        ? SingleChoiceQuizResult(
                                            results: values
                                                .map((e) => e == "yes" ? 0 : 1)
                                                .toList()
                                                .cast<int>(),
                                            options: const ["Ja", "Nein"],
                                            correctAnswer:
                                                element.correctAnswers[0] ==
                                                        "yes"
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
    } else {
      _showErrorDialog(_fetchResult);
      return Container();
    }
  }
}
