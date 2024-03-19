import 'dart:convert';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter_svg/svg.dart';
import 'package:frontend/components/code_input.dart';
import 'package:frontend/components/error/general_error_widget.dart';
import 'package:frontend/components/error/network_error_widget.dart';
import 'package:frontend/enums/form_status.dart';
import 'package:frontend/global.dart';
import 'package:frontend/theme/assets.dart';
import 'package:frontend/enums/form_type.dart';
import 'package:frontend/utils.dart';
import 'package:http/http.dart' as http;
// import 'package:mobile_scanner/mobile_scanner.dart';

class LiveTab extends StatefulWidget {
  const LiveTab({super.key});

  @override
  State<LiveTab> createState() => _LiveTabState();
}

class FormShell {
  final String connectCode;
  final String name;
  final String course;
  final FormType type;
  final FormStatus status;
  FormShell(this.connectCode, this.name, this.course, this.type, this.status);

  factory FormShell.fromJson(Map<String, dynamic> json) {
    return FormShell(
        (json['connectCode'] as int).toString(),
        json['name'],
        json['course'],
        FormType.fromString(json['type']),
        FormStatus.fromString(json['status']));
  }
}

List<FormShell> getFormShellsFromJson(List<dynamic> json) {
  return json.map((e) => FormShell.fromJson(e)).toList();
}

class _LiveTabState extends State<LiveTab> {
  final TextEditingController _joinCodeController = TextEditingController();
  Map<String, Color> statusColors = {};

  late List<FormShell> _forms;

  bool _loading = false;
  String _fetchResult = '';

  @override
  void initState() {
    super.initState();

    _forms = [];
    statusColors = {
      FormStatus.waiting.toString(): Colors.green,
      FormStatus.started.toString(): Colors.orange,
    };

    fetchForms();
  }

  Future fetchForms() async {
    setState(() {
      _loading = true;
      _fetchResult = '';
    });
    try {
      final response = await http.get(
        Uri.parse("${getBackendUrl()}/live"),
        headers: {
          "Content-Type": "application/json",
          "AUTHORIZATION": "Bearer ${getSession()!.jwt}",
        },
      );
      if (!mounted) return;
      if (response.statusCode == 200) {
        var data = jsonDecode(response.body);
        setState(() {
          _loading = false;
          _forms = _forms + getFormShellsFromJson(data);
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

  void joinCourse(code) async {
    setState(() {
      _loading = true;
      _fetchResult = '';
    });
    code = code.replaceAll(' ', '');
    // TODO: do nicer
    try {
      final response = await http.get(
        Uri.parse("${getBackendUrl()}/connectto/feedback/$code"),
        headers: {
          "Content-Type": "application/json",
          "AUTHORIZATION": "Bearer ${getSession()!.jwt}",
        },
      );
      if (response.statusCode == 200 && mounted) {
        setState(() {
          _loading = false;
          _fetchResult = 'success';
        });
        Navigator.pushNamed(context, '/attend-feedback', arguments: code);
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
    // Try Quiz
    try {
      final response = await http.get(
        Uri.parse("${getBackendUrl()}/connectto/quiz/$code"),
        headers: {
          "Content-Type": "application/json",
          "AUTHORIZATION": "Bearer ${getSession()!.jwt}",
        },
      );
      if (response.statusCode == 200 && mounted) {
        setState(() {
          _loading = false;
          _fetchResult = 'success';
        });
        Navigator.pushNamed(context, '/attend-quiz', arguments: code);
        return;
      } else {
        if (!context.mounted) return;
        showDialog(
          context: context,
          builder: (BuildContext context) {
            return AlertDialog(
              title: const Text('Ungültiger Zugangscode'),
              actions: <Widget>[
                TextButton(
                  child: const Text('Schließen'),
                  onPressed: () {
                    Navigator.of(context).pop();
                    Navigator.pushReplacementNamed(context, '/main');
                  },
                ),
              ],
            );
          },
        );
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

  // void openScanner() {
  //       Navigator.push(context, MaterialPageRoute(builder: (context) {
  //     return Scaffold(
  //       appBar: AppBar(title: const Text('Scanne QR-Code zum Beitreten')),
  //       body: MobileScanner(
  //         onDetect: (capture) {
  //           final List<Barcode> barcodes = capture.barcodes;
  //           for (final barcode in barcodes) {
  //             if (barcode.rawValue != null) {
  //               var qrCodeValue = barcode.rawValue;
  //               Navigator.pop(context, qrCodeValue);
  //             }
  //           }
  //         },
  //       ),
  //     );
  //   })).then((qrCodeValue) {
  //     if (qrCodeValue != null) {
  //       joinCourse(qrCodeValue);
  //     }
  //   });
  // }

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
    final colors = Theme.of(context).colorScheme;
    if (_loading) {
      return const Center(
        child: CircularProgressIndicator(),
      );
    } else if (_fetchResult == 'success') {
      return SingleChildScrollView(
        child: Column(
          children: [
            Padding(
              padding: const EdgeInsets.only(
                  top: 16.0, bottom: 0, left: 16, right: 16),
              child: Row(
                children: [
                  const Expanded(
                    child: Text(
                      "Live Umfrage",
                      style:
                          TextStyle(fontSize: 40, fontWeight: FontWeight.bold),
                    ),
                  ),
                  SvgPicture.asset(undrawQuestions, width: 150),
                ],
              ),
            ),
            Padding(
              padding: const EdgeInsets.all(16.0),
              child: Card(
                color: colors.background,
                shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(25)),
                child: Column(
                  children: [
                    const Padding(
                      padding: EdgeInsets.only(top: 24, bottom: 16),
                      child: Text(
                        'Beitreten',
                        style: TextStyle(
                          fontWeight: FontWeight.bold,
                          fontSize: 28,
                        ),
                      ),
                    ),
                    Padding(
                      padding: const EdgeInsets.only(bottom: 4),
                      child: Text(
                        'Mit Code beitreten',
                        style: Theme.of(context).textTheme.bodyMedium,
                      ),
                    ),
                    CodeInput(
                      inputText: '123 456',
                      textInputController: _joinCodeController,
                      textInput: TextInputType.number,
                      maxLength: 7,
                      onChanged: (value) {
                        var code = value.replaceAll(' ', '');
                        if (code.length > 3) {
                          code = '${code.substring(0, 3)} ${code.substring(3)}';
                        }
                        var selection = TextSelection.fromPosition(
                          TextPosition(offset: code.length),
                        );
                        _joinCodeController.value = TextEditingValue(
                          text: code,
                          selection: selection,
                        );
                      },
                      onSubmit: () {
                        joinCourse(_joinCodeController.text);
                      },
                    ),
                    const Padding(
                      padding: EdgeInsets.all(8.0),
                      child: Row(
                        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                        children: [
                          Expanded(
                            child: Padding(
                              padding: EdgeInsets.all(8.0),
                              child: Divider(
                                thickness: 1,
                              ),
                            ),
                          ),
                          Text(
                            'oder',
                            style: TextStyle(
                              fontWeight: FontWeight.normal,
                            ),
                          ),
                          Expanded(
                            child: Padding(
                              padding: EdgeInsets.all(8.0),
                              child: Divider(
                                thickness: 1,
                              ),
                            ),
                          ),
                        ],
                      ),
                    ),
                    Padding(
                      padding: const EdgeInsets.only(bottom: 24),
                      child: ElevatedButton(
                        onPressed: () {
                          // openScanner(); Uncomment and implement as needed
                        },
                        style: ElevatedButton.styleFrom(
                          padding: EdgeInsets.zero,
                          shape: const CircleBorder(),
                          fixedSize: const Size(50, 50),
                          backgroundColor: colors.primary,
                        ),
                        child: Icon(
                          size: 35,
                          Icons.qr_code_scanner,
                          color: colors.background,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ),
            const Padding(padding: EdgeInsets.all(8.0), child: Divider()),
            const Padding(
              padding: EdgeInsets.fromLTRB(16.0, 0.0, 16.0, 0.0),
              child: Text(
                "Aktive Umfragen",
                style: TextStyle(fontSize: 24.0, fontWeight: FontWeight.bold),
              ),
            ),
            ..._forms.map((form) {
              print(form.status.toString());
              return Card(
                shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(10.0)),
                child: ListTile(
                  leading: form.type == FormType.feedback
                      ? const Icon(Icons.feedback)
                      : const Icon(Icons.quiz),
                  trailing: Icon(Icons.circle,
                      color: statusColors[form.status.toString()], size: 20),
                  title: Text(form.name),
                  onTap: () => joinCourse(form.connectCode),
                ),
              );
            }).toList(),
          ],
        ),
      );
    } else {
      _showErrorDialog(_fetchResult);
      return Container();
    }
  }
}
