import 'package:flutter/material.dart';
import 'package:flutter_svg/svg.dart';
import 'package:frontend/components/code_input.dart';
import 'package:frontend/theme/assets.dart';

class LiveTab extends StatefulWidget {
  const LiveTab({super.key});

  @override
  State<LiveTab> createState() => _LiveTabState();
}

class _LiveTabState extends State<LiveTab> {
  final TextEditingController _joinCodeController = TextEditingController();

  @override
  void initState() {
    super.initState();
  }

  void joinCourse() {
    var code = _joinCodeController.text.replaceAll(' ', '');
    Navigator.pushNamed(context, '/attend-feedback', arguments: code);
    // Navigator.pushNamed(context, '/feedback-result', arguments: code);
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;

    return Column(
      children: <Widget>[
        Padding(
          padding:
              const EdgeInsets.only(top: 32.0, bottom: 32, left: 16, right: 16),
          child: Row(
            children: [
              const SizedBox(
                width: 200,
                child: Text(
                  "Live Umfrage",
                  style: TextStyle(fontSize: 40, fontWeight: FontWeight.bold),
                ),
              ),
              SvgPicture.asset(
                undrawQuestions,
                width: 150,
              ),
            ],
          ),
        ),
        Padding(
          padding: const EdgeInsets.all(16.0),
          child: Card(
            color: colors.background,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(25),
            ),
            // color: colors.secondary,
            child: Center(
              child: SizedBox(
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
                        joinCourse();
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
                        onPressed: () {},
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
          ),
        ),
      ],
    );
  }
}
