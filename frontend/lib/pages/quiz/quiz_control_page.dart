import 'dart:convert';
import 'dart:io';
import 'dart:async';
import 'dart:math';

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:frontend/auth_state.dart';
import 'package:frontend/components/animations/throw.dart';
import 'package:frontend/enums/form_status.dart';
import 'package:frontend/enums/question_type.dart';
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
import 'package:rive/rive.dart';

class QuizControlPage extends StatefulWidget {
  final String courseId;
  final String formId;

  const QuizControlPage(
      {super.key, required this.courseId, required this.formId});

  @override
  State<QuizControlPage> createState() => _QuizControlPageState();
}

class _QuizControlPageState extends AuthState<QuizControlPage> {
  late String _courseId;
  late String _formId;
  late String _userId;
  late List<String> _roles;

  late QuizForm _form;
  WebSocketChannel? _socketChannel;

  late List<Map<String, dynamic>> _results;
  late List<dynamic> _scoreboard;
  bool _showLeaderboard = false;

  int _participantCounter = 0;
  List<dynamic> _userNames = [];

  bool _isPhone = false;

  bool _loading = false;
  String _fetchResult = '';

  // for the paper plane animation
  final mainStackKey = GlobalKey();
  final scoreboardKey = GlobalKey();
  List<Widget> _animations = [];

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
        _scoreboard = getScoreboard(data);

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
          _showLeaderboard = false;
          _form.status = FormStatus.fromString(data["formStatus"]);
          _form.currentQuestionIndex = form.currentQuestionIndex;
          _form.currentQuestionFinished = form.currentQuestionFinished;
          _scoreboard = getScoreboard(data["form"]);
        });
      }
      if (data["action"] == "RESULT_ADDED") {
        setState(() {
          _scoreboard = getScoreboard(data["form"]);
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
      if (data["action"] == "FUN") {
        if (data["fun"]["action"] == "THROW_PAPER_PLANE") {
          double percentageX = data["fun"]["percentageX"];
          double percentageY = data["fun"]["percentageY"];
          animateThrow(ThrowType.paperPlane, percentageX, percentageY);
        }
        if (data["fun"]["action"] == "THROW_BALL") {
          double percentageX = data["fun"]["percentageX"];
          double percentageY = data["fun"]["percentageY"];
          animateThrow(ThrowType.ball, percentageX, percentageY);
        }
      }
    }, onError: (error) {
      //TODO: Should there be another error handling for this?
      setState(() {
        _form.status = FormStatus.error;
      });
    });

    if (_form.status == FormStatus.not_started) {
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
    if (_form.currentQuestionFinished) {
      if (_showLeaderboard) {
        if (_socketChannel != null) {
          _socketChannel!.sink.add(jsonEncode({
            "action": "NEXT",
            "roles": _roles,
            "userId": _userId,
          }));
        }
      } else {
        // Show the leaderboard first before moving to the next question
        setState(() {
          _showLeaderboard = true;
        });
      }
    } else {
      if (_socketChannel != null) {
        _socketChannel!.sink.add(jsonEncode({
          "action": "NEXT",
          "roles": _roles,
          "userId": _userId,
        }));
      }
    }
  }

  void throwAtScoreboard(double percentageX, double percentageY) {
    if (_socketChannel != null) {
      // random between THROW_PAPER_PLANE and THROW_BALL
      var random = Random().nextInt(2);
      var action = random == 0 ? "THROW_PAPER_PLANE" : "THROW_BALL";
      _socketChannel!.sink.add(jsonEncode({
        "action": "FUN",
        "fun": {
          "action": action,
          "percentageX": percentageX,
          "percentageY": percentageY,
        },
        "roles": _roles,
        "userId": _userId,
      }));
    }
  }

  void animateThrow(ThrowType type, double percentageX, double percentageY) {
    print("ADD ANIMATION");

    final RenderBox mainStackBox =
        mainStackKey.currentContext!.findRenderObject() as RenderBox;
    final RenderBox scoreBoardBox =
        scoreboardKey.currentContext!.findRenderObject() as RenderBox;
    final mainStackPosition = mainStackBox.localToGlobal(Offset.zero);
    final scoreboardPosition = scoreBoardBox.localToGlobal(Offset.zero);
    double dX = scoreboardPosition.dx -
        mainStackPosition.dx +
        percentageX * scoreBoardBox.size.width;
    double dY = scoreboardPosition.dy -
        mainStackPosition.dy +
        percentageY * scoreBoardBox.size.height;

    setState(() {
      _animations.insert(
          0, Throw(key: UniqueKey(), throwType: type, clickX: dX, clickY: dY));
      print(_animations.length);
    });

    Future.delayed(const Duration(milliseconds: 2500), () {
      if (!mounted) return;
      setState(() {
        _animations.removeLast();
      });
    });
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
      final int totalQuestions = _form.questions.length;
      final double progress = (_form.currentQuestionIndex + 1) / totalQuestions;

      final appBar = AppBar(
        title: Text(_form.name,
            style: const TextStyle(
                color: Colors.white, fontWeight: FontWeight.bold)),
        backgroundColor: Theme.of(context).colorScheme.primary,
        bottom: PreferredSize(
          preferredSize: const Size.fromHeight(10.0),
          child: SizedBox(
            height: 10.0,
            child: LinearProgressIndicator(
              value: progress,
              backgroundColor: Colors.grey[300],
              valueColor: AlwaysStoppedAnimation<Color>(
                  Theme.of(context).colorScheme.secondary),
            ),
          ),
        ),
      );

      if (_form.status == FormStatus.not_started ||
          _form.status == FormStatus.waiting) {
        var code = _form.connectCode;
        code = "${code.substring(0, 3)} ${code.substring(3, 6)}";

        return Scaffold(
            appBar: AppBar(
              title: Text(_form.name,
                  style: const TextStyle(
                      color: Colors.white, fontWeight: FontWeight.bold)),
              backgroundColor: colors.primary,
            ),
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
                        // scrollable list of participants
                        child: Padding(
                          padding: const EdgeInsets.all(8.0),
                          child: ListView.builder(
                            itemCount: _userNames.length,
                            itemBuilder: (context, index) {
                              return Padding(
                                padding: const EdgeInsets.all(4.0),
                                child: Align(
                                  alignment: Alignment.center,
                                  // padding: const EdgeInsets.symmetric(
                                  //     horizontal: 10, vertical: 3),
                                  child: Container(
                                    decoration: BoxDecoration(
                                      color: const Color.fromARGB(255, 165, 224, 211),
                                      borderRadius: BorderRadius.circular(20.0),
                                    ),
                                    child: Padding(
                                      padding: const EdgeInsets.symmetric(
                                          horizontal: 10, vertical: 3),
                                      child: Text(
                                        _userNames[index],
                                        style: const TextStyle(
                                            fontSize: 14,
                                            fontWeight: FontWeight.bold),
                                      ),
                                    ),
                                  ),
                                ),
                              );
                            },
                          ),
                        ),
                        // child: GridView.count(
                        //   crossAxisCount: crossAxisCount,
                        //   mainAxisSpacing: 5.0,
                        //   crossAxisSpacing: 5.0,
                        //   childAspectRatio: childAspectRatio,
                        //   children: List.generate(_userNames.length, (index) {
                        //     return Center(
                        //       child: Container(
                        //         padding: const EdgeInsets.symmetric(
                        //             horizontal: 10, vertical: 3),
                        //         decoration: BoxDecoration(
                        //           color:
                        //               const Color.fromARGB(255, 165, 224, 211),
                        //           borderRadius: BorderRadius.circular(20.0),
                        //         ),
                        //         child: Text(
                        //           _userNames[index],
                        //           style: const TextStyle(
                        //               fontSize: 14,
                        //               fontWeight: FontWeight.bold),
                        //         ),
                        //       ),
                        //     );
                        //   }),
                        //),
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

      if (_form.status == FormStatus.finished) {
        return Scaffold(
          appBar: appBar,
          body: Stack(
            key: mainStackKey,
            children: [
              SingleChildScrollView(
                child: Padding(
                  padding: const EdgeInsets.all(16.0),
                  child: Column(
                    children: <Widget>[
                      Text(
                        "Quiz beendet",
                        style: Theme.of(context).textTheme.headlineMedium,
                      ),
                      Container(
                        margin: const EdgeInsets.only(top: 30.0, bottom: 10.0),
                        width: 250,
                        height: 250,
                        child: RiveAnimation.asset(
                          'assets/animations/rive/animations.riv',
                          fit: BoxFit.cover,
                          artboard: 'rigged without bodyparts darker firework',
                          stateMachines: ['State Machine Winner'],
                        ),
                      ),
                      const SizedBox(height: 16),
                      Center(
                        child: GestureDetector(
                          key: scoreboardKey,
                          onTapUp: (details) {
                            // get the position of the tap and convert it to a percentage of the total height
                            final RenderBox box = scoreboardKey.currentContext!
                                .findRenderObject() as RenderBox;
                            double x = details.localPosition.dx;
                            double percentageX = x / box.size.width;
                            double y = details.localPosition.dy;
                            double percentageY = y / box.size.height;
                            throwAtScoreboard(percentageX, percentageY);
                          },
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
                      ),
                      const SizedBox(height: 16),
                      ElevatedButton(
                        onPressed: resetForm,
                        child: const Text('Quiz zurücksetzen'),
                      ),
                    ],
                  ),
                ),
              ),
              IgnorePointer(
                child: Stack(
                  children: _animations,
                ),
              )
            ],
          ),
        );
      }

      final QuizQuestion element =
          _form.questions[_form.currentQuestionIndex] as QuizQuestion;
      final values = _results[_form.currentQuestionIndex]["values"];

      return RawKeyboardListener(
        focusNode: FocusNode(),
        autofocus: true,
        onKey: (RawKeyEvent event) async {
          if (event is RawKeyDownEvent &&
              event.logicalKey == LogicalKeyboardKey.arrowRight) {
            next();
          }
        },
        child: Scaffold(
          appBar: appBar,
          body: Stack(
            children: [
              SingleChildScrollView(
                child: SizedBox(
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
                                      fontSize: 25, fontWeight: FontWeight.w700)),
                              Text(element.description,
                                  style: const TextStyle(
                                      fontSize: 20, fontWeight: FontWeight.w500),
                                  textAlign: TextAlign.center),
                              const SizedBox(height: 16),
                              if (_form.currentQuestionFinished == true)
                                if (_showLeaderboard)
                                  Center(
                                      child: Container(
                                    constraints:
                                        const BoxConstraints(maxWidth: 800),
                                    child: Card(
                                      child: Padding(
                                        padding: const EdgeInsets.all(8),
                                        child: QuizScoreboard(
                                            scoreboard: _scoreboard),
                                      ),
                                    ),
                                  ))
                                else
                                  Card(
                                    child: Padding(
                                      padding: const EdgeInsets.all(16),
                                      child: element.type ==
                                              QuestionType.single_choice
                                          ? SingleChoiceQuizResult(
                                              results: values
                                                  .map((e) => int.parse(e))
                                                  .toList()
                                                  .cast<int>(),
                                              options: element.options,
                                              correctAnswer:
                                                  element.correctAnswers[0],
                                            )
                                          : element.type == QuestionType.yes_no
                                              ? SingleChoiceQuizResult(
                                                  results: values
                                                      .map((e) =>
                                                          e == "yes" ? 0 : 1)
                                                      .toList()
                                                      .cast<int>(),
                                                  options: const ["Ja", "Nein"],
                                                  correctAnswer:
                                                      element.correctAnswers[0] ==
                                                              "yes"
                                                          ? "0"
                                                          : "1",
                                                )
                                              : Text(element.type.toString()),
                                    ),
                                  ),
                            ],
                          ),
                        ),
                      ),
                      if (_form.status == FormStatus.started)
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
                      if (_form.status == FormStatus.finished)
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
              ),
              Positioned(
                top: 0,
                left: 0,
                right: 0,
                child: Container(
                  color: colors.surfaceVariant,
                  child: Row(
                    children: <Widget>[
                      Expanded(
                        child: Container(), // Empty container to take up space
                      ),
                      Expanded(
                        child: Text(
                          "${_form.connectCode.substring(0, 3)} ${_form.connectCode.substring(3, 6)}",
                          style: Theme.of(context).textTheme.headlineSmall,
                          textAlign: TextAlign.center,
                        ),
                      ),
                      Expanded(
                        child: Padding(
                          padding: const EdgeInsets.only(right: 8.0),
                          child: Row(
                            mainAxisAlignment: MainAxisAlignment.end,
                            children: <Widget>[
                              const Icon(Icons.person),
                              Text(
                                "${values.length}/$_participantCounter",
                                style:
                                    Theme.of(context).textTheme.headlineSmall,
                                textAlign: TextAlign.right,
                              ),
                            ],
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              )
            ],
          ),
        ),
      );
    } else {
      _showErrorDialog(_fetchResult);
      return Container();
    }
  }
}
