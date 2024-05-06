import 'dart:convert';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:frontend/auth_state.dart';
import 'package:frontend/components/elements/feedback/single_choice_feedback.dart';
import 'package:frontend/components/elements/feedback/slider_feedback.dart';
import 'package:frontend/components/elements/feedback/star_feedback.dart';
import 'package:frontend/components/error/general_error_widget.dart';
import 'package:frontend/components/error/network_error_widget.dart';
import 'package:frontend/enums/form_status.dart';
import 'package:frontend/enums/question_type.dart';
import 'package:frontend/global.dart';
import 'package:frontend/models/feedback/feedback_form.dart';
import 'package:frontend/models/feedback/feedback_question.dart';
import 'package:frontend/theme/assets.dart';
import 'package:frontend/utils.dart';
import 'package:web_socket_channel/web_socket_channel.dart';
import 'package:http/http.dart' as http;
import 'package:rive/rive.dart';

class AttendFeedbackPage extends StatefulWidget {
  final String code;

  const AttendFeedbackPage({super.key, required this.code});

  @override
  State<AttendFeedbackPage> createState() => _AttendFeedbackPageState();
}

class _AttendFeedbackPageState extends AuthState<AttendFeedbackPage> {
  late String _courseId;
  late String _formId;
  late String _userId;

  late FeedbackForm _form;
  WebSocketChannel? _socketChannel;

  final Map<String, dynamic> _feedbackValues = {};
  bool _voted = false;

  bool _loading = false;
  String _fetchResult = '';

  @override
  void initState() {
    super.initState();

    _userId = getSession()!.userId;
    init();
  }

  Future init() async {
    setState(() {
      _loading = true;
      _fetchResult = '';
    });
    var code = widget.code;
    try {
      final response = await http.get(
        Uri.parse("${getBackendUrl()}/connectto/feedback/$code"),
        headers: {
          "Content-Type": "application/json",
          "AUTHORIZATION": "Bearer ${getSession()!.jwt}",
        },
      );
      if (response.statusCode == 200) {
        var data = jsonDecode(response.body);
        _courseId = data["courseId"];
        _formId = data["formId"];
        fetchForm();
        return;
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
    Navigator.pop(context);
  }

  Future fetchForm() async {
    setState(() {
      _loading = true;
      _fetchResult = '';
    });
    try {
      final response = await http.get(
        Uri.parse(
            "${getBackendUrl()}/course/$_courseId/feedback/form/$_formId"),
        headers: {
          "Content-Type": "application/json",
          "AUTHORIZATION": "Bearer ${getSession()!.jwt}",
        },
      );
      if (response.statusCode == 200) {
        var data = jsonDecode(response.body);
        var form = FeedbackForm.fromJson(data);

        startWebsocket();

        for (var element in form.questions) {
          switch (element.type) {
            case QuestionType.stars:
              _feedbackValues[element.id] = 3;
              break;
            case QuestionType.slider:
              _feedbackValues[element.id] = 5;
              break;
            case QuestionType.single_choice:
              _feedbackValues[element.id] = 0;
              break;
            default:
              _feedbackValues[element.id] = 0;
              break;
          }
        }

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
          "${getBackendUrl(protocol: "ws")}/course/$_courseId/feedback/form/$_formId/subscribe/$_userId/${getSession()!.jwt}"),
    );

    _socketChannel!.stream.listen((event) {
      var data = jsonDecode(event);
      if (data["action"] == "FORM_STATUS_CHANGED") {
        setState(() {
          _form.status = FormStatus.fromString(data["formStatus"]);
          _voted = false;
        });
      }

      // if action is ALREADY_SUBMITTED, the user has already submitted feedback
      if (data["action"] == "ALREADY_SUBMITTED") {
        setState(() {
          _voted = true;
        });
      }
    }, onError: (error) {
      //TODO: Should there be another error handling for this?
      setState(() {
        _form.status = FormStatus.error;
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
    } else if (_fetchResult == 'success') {
      double screenWidth = MediaQuery.of(context).size.width;
      double buttonWidth =
          screenWidth <= 600 ? screenWidth * 0.92 : screenWidth * 0.4;
      final colors = Theme.of(context).colorScheme;
      final appbar = AppBar(
        title: Text(_form.name,
            style: const TextStyle(
              color: Colors.black,
              fontWeight: FontWeight.bold,
            )),
        backgroundColor: Colors.black38,
        actions: [
          Padding(
            padding: const EdgeInsets.all(8.0),
            child: Image.asset(incognitoCirclePng, width: 40, height: 40),
          )
        ],
      );

      if (_form?.status != FormStatus.started) {
        return Scaffold(
          appBar: appbar,
          body: Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: <Widget>[
                Padding(
                    padding: const EdgeInsets.only(left: 20.0, right: 20.0),
                    child: const Text(
                        "Bitte warten Sie bis das Feedback gestartet wird")),
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

      if (_voted) {
        return Scaffold(
          appBar: appbar,
          body: Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: <Widget>[
                Container(
                    margin: EdgeInsets.only(
                        top: 0.0,
                        bottom: 10.0), // specify the top and bottom margin

                    width: 300,
                    height: 300,
                    child: RiveAnimation.asset(
                      'assets/animations/rive/animations.riv',
                      fit: BoxFit.cover,
                      artboard: 'High Five',
                      stateMachines: ['High Five State Machine'],
                    )),
                Padding(
                  padding: EdgeInsets.only(left: 20.0, right: 20.0),
                  child: Text("Vielen Dank für Ihr Feedback"),
                )
              ],
            ),
          ),
        );
      }

      return Scaffold(
        appBar: appbar,
        body: SingleChildScrollView(
          child: Column(children: [
            Center(
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Wrap(
                  alignment: WrapAlignment.spaceEvenly,
                  spacing: 16.0,
                  runSpacing: 16.0,
                  children:
                      List<Widget>.generate(_form.questions.length, (index) {
                    final element = _form.questions[index] as FeedbackQuestion;
                    return SizedBox(
                      width: MediaQuery.of(context).size.width < 600
                          ? double.infinity
                          : 600,
                      child: Card(
                        color: colors.surface,
                        child: Padding(
                          padding: const EdgeInsets.all(16.0),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.center,
                            children: <Widget>[
                              Text('${index + 1}. ${element.name}',
                                  style: const TextStyle(
                                      fontSize: 24,
                                      fontWeight: FontWeight.bold),
                                  textAlign: TextAlign.center),
                              const SizedBox(height: 8),
                              Text(element.description,
                                  style: const TextStyle(fontSize: 15),
                                  textAlign: TextAlign.center),
                              const SizedBox(height: 16),
                              if (element.type == QuestionType.stars)
                                StarFeedback(
                                  initialRating: _feedbackValues[element.id],
                                  onRatingChanged: (newRating) {
                                    setState(() {
                                      _feedbackValues[element.id] = newRating;
                                    });
                                  },
                                )
                              else if (element.type == QuestionType.slider)
                                SliderFeedback(
                                  initialFeedback: _feedbackValues[element.id],
                                  rangeLow: element.rangeLow,
                                  rangeHigh: element.rangeHigh,
                                  onFeedbackChanged: (newFeedback) {
                                    setState(() {
                                      _feedbackValues[element.id] = newFeedback;
                                    });
                                  },
                                )
                              else if (element.type ==
                                  QuestionType.single_choice)
                                SingleChoiceFeedback(
                                  options: element.options,
                                  initialFeedback: _feedbackValues[element.id],
                                  onFeedbackChanged: (newFeedback) {
                                    setState(() {
                                      _feedbackValues[element.id] = newFeedback;
                                    });
                                  },
                                )
                              else if (element.type == QuestionType.fulltext)
                                TextField(
                                  onChanged: (newFeedback) {
                                    setState(() {
                                      _feedbackValues[element.id] = newFeedback;
                                    });
                                  },
                                )
                              else
                                const Text('Unknown element type')
                            ],
                          ),
                        ),
                      ),
                    );
                  }),
                ),
              ),
            ),
            SizedBox(
              height: 55,
              width: buttonWidth,
              child: ElevatedButton(
                style: ButtonStyle(
                  elevation: MaterialStateProperty.all<double>(6.0),
                  shape: MaterialStateProperty.all<OutlinedBorder>(
                    RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(14.0),
                    ),
                  ),
                  backgroundColor: MaterialStateProperty.all<Color>(
                      Theme.of(context).colorScheme.surface),
                  foregroundColor: MaterialStateProperty.all<Color>(
                      Theme.of(context).colorScheme.primary),
                ),
                child: const Text('Senden', style: TextStyle(fontSize: 20)),
                onPressed: () {
                  // Iterate over the feedbackValues Map and send each feedback value to the socket
                  for (var entry in _feedbackValues.entries) {
                    var message = {
                      "action": "ADD_RESULT",
                      "resultElementId": entry.key,
                      "resultValues": [entry.value],
                      "role": "STUDENT"
                    };
                    _socketChannel?.sink.add(jsonEncode(message));
                  }
                  setState(() {
                    _voted = true;
                  });
                },
              ),
            ),
            const SizedBox(height: 20),
          ]),
        ),
      );
    } else {
      _showErrorDialog(_fetchResult);
      return Container();
    }
  }
}
