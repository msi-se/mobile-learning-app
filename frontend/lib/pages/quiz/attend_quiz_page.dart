import 'dart:convert';
import 'dart:io';
import 'dart:math';

import 'package:flutter/material.dart';
import 'package:frontend/auth_state.dart';
import 'package:frontend/components/animations/throw.dart';
import 'package:frontend/components/elements/quiz/single_choice_quiz.dart';
import 'package:frontend/components/elements/quiz/yes_no_quiz.dart';
import 'package:frontend/components/error/general_error_widget.dart';
import 'package:frontend/components/error/network_error_widget.dart';
import 'package:frontend/components/general/quiz/QuizScoreboard.dart';
import 'package:frontend/components/general/quiz/choose_alias.dart';
import 'package:frontend/enums/form_status.dart';
import 'package:frontend/enums/question_type.dart';
import 'package:frontend/global.dart';
import 'package:frontend/models/quiz/quiz_form.dart';
import 'package:frontend/theme/assets.dart';
import 'package:frontend/utils.dart';
import 'package:web_socket_channel/web_socket_channel.dart';
import 'package:http/http.dart' as http;
import 'package:rive/rive.dart';

class AttendQuizPage extends StatefulWidget {
  final String code;

  const AttendQuizPage({super.key, required this.code});

  @override
  State<AttendQuizPage> createState() => _AttendQuizPageState();
}

class _AttendQuizPageState extends AuthState<AttendQuizPage> {
  bool _aliasChosen = false;

  late String _courseId;
  late String _formId;
  late String _userId;
  late String _alias;
  QuizForm? _form;

  String _aliasError = '';

  WebSocketChannel? _socketChannel;

  late List<dynamic> _scoreboard;
  SMITrigger? _bump;
  SMITrigger? _notVoted;
  SMIInput<bool>? _boolInput;

  dynamic _value;
  bool _voted = false;
  dynamic _userHasAnsweredCorrectly = false;
  List<dynamic> _correctAnswers = [];

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
    _alias = getSession()!.username;
    init();
  }

  Future init() async {
    var code = widget.code;
    setState(() {
      _loading = true;
      _fetchResult = '';
    });
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
        setState(() {
          _loading = false;
          _fetchResult = 'success';
        });
        // await fetchForm();
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

    if (!mounted) return;
    //Navigator.pop(context);
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
        setState(() {
          _aliasChosen = true;
          _fetchResult = 'success';
        });
        return true;
      } else if (response.statusCode == 409) {
        setState(() {
          _aliasError =
              "Dieser Nickname ist bereits vergeben. Bitte wählen Sie einen anderen.";
          _aliasChosen = false;
        });
        return false;
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
        if (form.status == FormStatus.finished) {
          _scoreboard = getScoreboard(data);
        }

        startWebsocket();

        setState(() {
          _form = form;
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

  void _hitBump() {
    _boolInput?.value = _userHasAnsweredCorrectly;
    _bump?.fire();
  }

  void _notVotedAnimation() {
    _notVoted?.fire();
  }

  void startWebsocket() {
    _socketChannel = WebSocketChannel.connect(
      Uri.parse(
          "${getBackendUrl(protocol: "ws")}/course/$_courseId/quiz/form/$_formId/subscribe/$_userId/${getSession()!.jwt}"),
    );

    _socketChannel!.stream.listen((event) {
      var data = jsonDecode(event);
      if (data["formStatus"] == "FINISHED") {
        _form!.status = FormStatus.fromString(data["formStatus"]);
        _value = null;
        _voted = false;
        _userHasAnsweredCorrectly = false;
        _correctAnswers = [];
      }
      if (data["action"] == "FORM_STATUS_CHANGED") {
        var form = QuizForm.fromJson(data["form"]);
        setState(() {
          _form!.status = FormStatus.fromString(data["formStatus"]);
          _value = null;
          _voted = false;
          _form!.currentQuestionIndex = form.currentQuestionIndex;
          _form!.currentQuestionFinished = form.currentQuestionFinished;
          _scoreboard = getScoreboard(data["form"]);
        });
      }
      if (data["action"] == "CLOSED_QUESTION" ||
          data["action"] == "OPENED_NEXT_QUESTION") {
        var form = QuizForm.fromJson(data["form"]);
        setState(() {
          _form!.currentQuestionIndex = form.currentQuestionIndex;
          _form!.currentQuestionFinished = form.currentQuestionFinished;
        });
        if (data["action"] == "OPENED_NEXT_QUESTION") {
          setState(() {
            _correctAnswers = [];
            _value = null;
            _voted = false;
          });
        }
        if (data["action"] == "CLOSED_QUESTION") {
          _userHasAnsweredCorrectly = data["userHasAnsweredCorrectly"];
          _correctAnswers = data["correctAnswers"];
          _hitBump();
          if (!_voted) {
            _notVotedAnimation();
          }
        }
      }
      // if action is ALREADY_SUBMITTED, the user has already submitted feedback
      if (data["action"] == "ALREADY_SUBMITTED") {
        setState(() {
          _voted = true;
          _value = data["userAnswers"][0];
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
        _form!.status = FormStatus.error;
      });
    });
  }

  List<dynamic> getScoreboard(Map<String, dynamic> form) {
    print("GET SCOREBOARD");
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

  void _onRiveInit(Artboard artboard) {
    final controller =
        StateMachineController.fromArtboard(artboard, 'tf state machine');
    artboard.addController(controller!);
    // Get a reference to the "bump" state machine input
    _bump = controller.findInput<bool>('tf trigger') as SMITrigger;
    _notVoted = controller.findInput<bool>('nicht abgestimmt') as SMITrigger;
    _boolInput = controller.findInput<bool>('answer') as SMIInput<bool>;
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
        "role": "STUDENT",
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

  @override
  Widget build(BuildContext context) {
    if (_loading) {
      return const Scaffold(
        body: Center(
          child: CircularProgressIndicator(),
        ),
      );
    } 
    
    if (_fetchResult == 'success') {

      final appBar = AppBar(
        title: const Text(
            'Einem Quiz beitreten', //_form.name, TODO: find better solution
            style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
        backgroundColor: Theme.of(context).colorScheme.primary,
      );

      if (!_aliasChosen) {
        return Scaffold(
          appBar: appBar,
          body: ChooseAlias(
            onAliasSubmitted: (chosenAlias) async {
              setState(() {
                _loading = true;
                _alias = chosenAlias;
              });
              bool success = await participate();
              if (!success) {
                ScaffoldMessenger.of(context).showSnackBar(SnackBar(
                    content: Text(_aliasError),
                    backgroundColor: Colors.redAccent));
              } else {
                await fetchForm();
              }
            },
          ),
        );
      }

      final int totalQuestions = _form!.questions.length;
      final double progress = (_form!.currentQuestionIndex + 1) / totalQuestions;

      final appBarWithProgress = AppBar(
        title: Text(_form!.name,
            style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
        backgroundColor: Theme.of(context).colorScheme.primary,
        bottom: PreferredSize(
          preferredSize: const Size.fromHeight(10.0),
          child: SizedBox(
            height: 10.0,
            child: LinearProgressIndicator(
              value: progress,
              backgroundColor: Colors.grey[300],
              valueColor: AlwaysStoppedAnimation<Color>(Theme.of(context).colorScheme.secondary),
            ),
          ),
        ),
      );

      if (_form?.status == FormStatus.finished) {
        return Scaffold(
          appBar: appBar,
          body: Stack(
            key: mainStackKey,
            children: [
              Center(
                child: Container(
                  margin: const EdgeInsets.only(top: 100.0, bottom: 100.0),
                  width: 400,
                  height: 400,
                  child: RiveAnimation.asset(
                    'assets/animations/rive/animations.riv',
                    fit: BoxFit.cover,
                    artboard: 'firework',
                    stateMachines: ['Firework State Machine'],
                  ),
                ),
              ),
              SingleChildScrollView(
                child: Padding(
                  padding: const EdgeInsets.all(16.0),
                  child: Column(
                    children: <Widget>[
                      Text(
                        "Quiz beendet",
                        style: Theme.of(context).textTheme.headlineMedium,
                      ),
                      // Container(
                      //   margin: const EdgeInsets.only(top: 30.0, bottom: 10.0),
                      //   width: 250,
                      //   height: 250,
                      //   child: RiveAnimation.asset(
                      //     'assets/animations/rive/animations.riv',
                      //     fit: BoxFit.cover,
                      //     artboard: 'rigged without bodyparts darker firework',
                      //     stateMachines: ['State Machine Winner'],
                      //   ),
                      // ),
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

      if (_form!.status != FormStatus.started) {
        return Scaffold(
          appBar: appBar,
          body: Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: <Widget>[
                const Padding(
                    padding: EdgeInsets.only(left: 20.0, right: 20.0),
                    child:
                        Text("Bitte warten Sie bis das Quiz gestartet wird")),
                Container(
                  margin: const EdgeInsets.only(top: 100.0, bottom: 100.0),
                  width: 150,
                  height: 150,
                  child: RiveAnimation.asset(
                    'assets/animations/rive/animations.riv',
                    fit: BoxFit.cover,
                    artboard: 'Waiting with coffee shorter arm',
                    stateMachines: ['Waiting State Machine'],
                  ),
                ),
              ],
            ),
          ),
        );
      }

      final element = _form!.questions[_form!.currentQuestionIndex];

      return Scaffold(
        appBar: appBarWithProgress,
        body: SizedBox(
          width: double.infinity,
          child: Column(
            children: [
              Padding(
                padding:
                    const EdgeInsets.only(left: 16.0, top: 16.0, right: 16.0),
                child: Column(
                  children: <Widget>[
                    const SizedBox(height: 16),
                    Text(element.name,
                        style: const TextStyle(
                            fontSize: 25, fontWeight: FontWeight.w700)),
                    Text(element.description,
                        style: const TextStyle(
                            fontSize: 20, fontWeight: FontWeight.w500),
                        textAlign: TextAlign.center),
                    const SizedBox(height: 16),
                    Card(
                      child: Padding(
                        padding: const EdgeInsets.all(16),
                        child: element.type == QuestionType.single_choice
                            ? SingleChoiceQuiz(
                                correctAnswers: _correctAnswers,
                                currentQuestionFinished:
                                    _form!.currentQuestionFinished,
                                voted: _voted,
                                value: _value,
                                options: element.options,
                                onSelectionChanged: (newValue) {
                                  setState(() {
                                    _value = newValue;
                                  });
                                },
                              )
                            : element.type == QuestionType.yes_no
                                ? YesNoQuiz(
                                    correctAnswers: _correctAnswers,
                                    currentQuestionFinished:
                                        _form!.currentQuestionFinished,
                                    voted: _voted,
                                    value: _value,
                                    onSelectionChanged: (newValue) {
                                      setState(() {
                                        _value = newValue;
                                      });
                                    },
                                  )
                                : Text(element.type.toString()),
                      ),
                    ),
                  ],
                ),
              ),
              if (!_form!.currentQuestionFinished && !_voted)
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
              if (!_form!.currentQuestionFinished && _voted)
                Container(
                    margin: const EdgeInsets.only(
                        top: 0.0,
                        bottom: 10.0), // specify the top and bottom margin

                    width: 130,
                    height: 130,
                    child: RiveAnimation.asset(
                      'assets/animations/rive/animations.riv',
                      fit: BoxFit.cover,
                      artboard: 'true & false',
                      stateMachines: ['tf State Machine'],
                      onInit: _onRiveInit,
                    )),
              if (_form!.currentQuestionFinished && _voted)
                Container(
                    margin: const EdgeInsets.only(
                        top: 0.0,
                        bottom: 10.0), // specify the top and bottom margin

                    width: 130,
                    height: 130,
                    child: RiveAnimation.asset(
                      'assets/animations/rive/animations.riv',
                      fit: BoxFit.cover,
                      artboard: 'true & false',
                      stateMachines: ['tf State Machine'],
                      onInit: _onRiveInit,
                    )),
              if (_form!.currentQuestionFinished && !_voted)
                Container(
                    margin: const EdgeInsets.only(
                        top: 0.0,
                        bottom: 10.0), // specify the top and bottom margin

                    width: 130,
                    height: 130,
                    child: RiveAnimation.asset(
                      'assets/animations/rive/animations.riv',
                      fit: BoxFit.cover,
                      artboard: 'true & false',
                      animations: ['nicht abgestimmt'],
                    )),
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
