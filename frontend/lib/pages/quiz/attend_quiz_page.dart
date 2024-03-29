import 'dart:convert';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:frontend/auth_state.dart';
import 'package:frontend/components/elements/quiz/single_choice_quiz.dart';
import 'package:frontend/components/elements/quiz/yes_no_quiz.dart';
import 'package:frontend/components/error/general_error_widget.dart';
import 'package:frontend/components/error/network_error_widget.dart';
import 'package:frontend/components/general/quiz/choose_alias.dart';
import 'package:frontend/enums/form_status.dart';
import 'package:frontend/enums/question_type.dart';
import 'package:frontend/global.dart';
import 'package:frontend/models/quiz/quiz_form.dart';
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
  String _aliasError = '';

  QuizForm? _form;
  WebSocketChannel? _socketChannel;

  dynamic _value;
  bool _voted = false;

  bool _loading = false;
  String _fetchResult = '';

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
        //await fetchForm();
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
          _loading = false;
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
          _form?.status = FormStatus.fromString(data["formStatus"]);
          _value = null;
          _voted = false;
          _form?.currentQuestionIndex = form.currentQuestionIndex;
          _form?.currentQuestionFinished = form.currentQuestionFinished;
        });
      }
      if (data["action"] == "CLOSED_QUESTION" ||
          data["action"] == "OPENED_NEXT_QUESTION") {
        var form = QuizForm.fromJson(data["form"]);
        setState(() {
          _value = null;
          _voted = false;
          _form?.currentQuestionIndex = form.currentQuestionIndex;
          _form?.currentQuestionFinished = form.currentQuestionFinished;
        });
      }
    }, onError: (error) {
      //TODO: Should there be another error handling for this?
      setState(() {
        _form?.status = FormStatus.error;
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
    final colors = Theme.of(context).colorScheme;
    if (_loading) {
      return const Scaffold(
        body: Center(
          child: CircularProgressIndicator(),
        ),
      );
    } else if (_fetchResult == 'success') {
      final appbar = AppBar(
        title: Text(
            'Einem Quiz beitreten', //_form.name, TODO: find better solution
            style: const TextStyle(
                color: Colors.white, fontWeight: FontWeight.bold)),
        backgroundColor: colors.primary,
      );

      if (!_aliasChosen) {
        return Scaffold(
          appBar: appbar,
          body: ChooseAlias(
            onAliasSubmitted: (chosenAlias) async {
              setState(() {
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

      if (_form?.status != FormStatus.started || _voted) {
        return Scaffold(
          appBar: appbar,
          body: Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: <Widget>[
                Padding(
                  padding: const EdgeInsets.only(left: 20.0, right: 20.0),
                  child: _form?.status != FormStatus.started
                      ? const Text(
                          "Bitte warten Sie bis das Quiz gestartet wird")
                      : const Text(
                          "Bitte warten Sie bis die nächste Frage gestellt wird"),
                ),
                Container(
                  width: 400,
                  height: 400,
                  child: RiveAnimation.asset(
                    'animations/rive/waiting.riv',
                    fit: BoxFit.cover,
                    artboard: 'Waiting',
                    stateMachines: ['Waiting State Machine'],
                  ),
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

      if (_form!.currentQuestionFinished) {
        return Scaffold(
          appBar: appbar,
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

      final element = _form?.questions[_form!.currentQuestionIndex];

      return Scaffold(
        appBar: appbar,
        body: SizedBox(
          width: double.infinity,
          child: Column(
            children: [
              Padding(
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  children: <Widget>[
                    const SizedBox(height: 16),
                    Text(element!.name,
                        style: const TextStyle(
                            fontSize: 24, fontWeight: FontWeight.bold)),
                    Text(element.description,
                        style: const TextStyle(fontSize: 15),
                        textAlign: TextAlign.center),
                    const SizedBox(height: 16),
                    Card(
                      child: Padding(
                        padding: const EdgeInsets.all(16),
                        child: element.type == QuestionType.single_choice
                            ? SingleChoiceQuiz(
                                options: element.options,
                                onSelectionChanged: (newValue) {
                                  setState(() {
                                    _value = newValue;
                                  });
                                },
                              )
                            : element.type == QuestionType.yes_no
                                ? YesNoQuiz(
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
    } else {
      _showErrorDialog(_fetchResult);
      return Container();
    }
  }
}
