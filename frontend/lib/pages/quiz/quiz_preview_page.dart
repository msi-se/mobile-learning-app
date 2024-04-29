import 'dart:io';

import 'package:flutter/material.dart';
import 'package:frontend/auth_state.dart';
import 'package:frontend/components/error/general_error_widget.dart';
import 'package:frontend/components/error/network_error_widget.dart';
import 'package:frontend/components/layout/sliver_layout.dart';
import 'package:frontend/components/basicButton.dart';
import 'package:frontend/enums/form_status.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';

import 'package:frontend/models/quiz/quiz_form.dart';
import 'package:frontend/utils.dart';
import 'package:frontend/global.dart';

class QuizPreviewPage extends StatefulWidget {
  final String courseId;
  final String formId;

  const QuizPreviewPage({
    Key? key,
    required this.courseId,
    required this.formId,
  }) : super(key: key);

  @override
  State<QuizPreviewPage> createState() => _QuizPreviewPageState();
}

class _QuizPreviewPageState extends AuthState<QuizPreviewPage> {
  QuizForm? _form;

  bool _loading = false;
  String _fetchResult = '';

  @override
  void initState() {
    super.initState();
    fetchForm();
  }

  Future<void> fetchForm() async {
    setState(() {
      _loading = true;
      _fetchResult = '';
    });
    try {
      //TODO: Print in production code not good:
      print(
          "${getBackendUrl()}/course/${widget.courseId}/quiz/form/${widget.formId}");
      final response = await http.get(
        Uri.parse(
            "${getBackendUrl()}/course/${widget.courseId}/quiz/form/${widget.formId}"),
        headers: {
          "Content-Type": "application/json",
          "AUTHORIZATION": "Bearer ${getSession()!.jwt}",
        },
      );

      if (response.statusCode == 200) {
        setState(() {
          _form = QuizForm.fromJson(json.decode(response.body));
          _loading = false;
          _fetchResult = 'success';
        });
      } else {
        //TODO: There might be a better way for this scenario
        setState(() {
          _loading = false;
          _fetchResult = 'general_error';
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

  @override
  Widget build(BuildContext context) {
    if (_loading) {
      return const Scaffold(
        body: Center(child: CircularProgressIndicator()),
      );
    } else if (_fetchResult == 'success') {
      final colors = Theme.of(context).colorScheme;

      return Scaffold(
        appBar: AppBar(
          backgroundColor: Theme.of(context).colorScheme.primary,
          title: const Text("Quiz Info",
              style:
                  TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
        ),
        body: SliverLayout(
          collapsable: false,
          headerHeight: 120,
          navBarHeight: 0,
          title: (_) {
            return Padding(
              padding: const EdgeInsets.only(left: 16, top: 16, right: 16),
              child: Column(
                children: [
                  Text(
                    _form!.name,
                    style: TextStyle(
                      color: colors.onSurface,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const SizedBox(height: 10),
                  Row(
                    children: [
                      Expanded(
                        child: BasicButton(
                          type: ButtonType.primary,
                          text: _form!.status == FormStatus.not_started ||
                                  _form?.status == FormStatus.waiting
                              ? "Starten" : "Ergebnisse",
                          onPressed: () async {
                            final _ = await Navigator.pushNamed(
                                context, '/quiz-control',
                                arguments: {
                                  "courseId": widget.courseId,
                                  "formId": widget.formId,
                                });
                            fetchForm();
                          },
                        ),
                      ),
                      const SizedBox(width: 10),
                      Expanded(
                        child: BasicButton(
                          type: ButtonType.primary,
                          text: "Beitreten",
                          onPressed: () async {
                            final _ = await Navigator.pushNamed(
                                context, '/attend-quiz',
                                arguments: _form!.connectCode);
                            fetchForm();
                          },
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            );
          },
          body: Padding(
            padding: const EdgeInsets.all(8.0),
            child: ListView.builder(
              shrinkWrap: true,
              physics: const NeverScrollableScrollPhysics(),
              itemCount: _form!.questions.length,
              itemBuilder: (context, index) {
                var element = _form!.questions[index];
                return Card(
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(10.0),
                  ),
                  elevation: 1.0,
                  surfaceTintColor: Colors.white,
                  child: ListTile(
                    title: Text(element.description),
                  ),
                );
              },
            ),
          ),
        ),
      );
    } else {
      _showErrorDialog(_fetchResult);
      return Container();
    }
  }
}
