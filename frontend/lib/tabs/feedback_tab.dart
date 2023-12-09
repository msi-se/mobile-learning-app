import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:frontend/components/input_card.dart';
import 'package:frontend/utils.dart';
import 'package:http/http.dart' as http;


class FeedbackTab extends StatefulWidget {
  const FeedbackTab({super.key});

  @override
  State<FeedbackTab> createState() => _FeedbackTabState();
}

class _FeedbackTabState extends State<FeedbackTab> {
  final TextEditingController _joinCodeController = TextEditingController();

  @override
  void initState() {
    super.initState();
  }

  void joinChannel() {
    var code = _joinCodeController.text.replaceAll(' ', '');
    Navigator.pushNamed(context, '/attend-feedback', arguments: code);
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    Size size = MediaQuery.of(context).size;

    return Scaffold(
      body: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: <Widget>[
          Expanded(
            child: InputCard(
                size: size,
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
                  joinChannel();
                },
                sublineText: 'Mit Code beitreten'),
          ),

          Expanded(
            child: Card(
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(25),
              ),
              margin: const EdgeInsets.symmetric(
                horizontal: 25,
                vertical: 20
              ),
              child: InkWell(
                onTap: () {
                  Navigator.pushNamed(context, '/choose-feedback');
                },
                child: Padding(
                  padding: const EdgeInsets.all(16.0),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: <Widget>[
                      Text('Feedback erstellen',
                          style: TextStyle(
                              fontSize: 24.0, // Adjust font size
                              fontWeight: FontWeight.bold,
                              color: colors.primary)),
                      Icon(Icons.add, color: colors.primary),
                    ],
                  ),
                ),
              ),
            ),
          ),
          
          Expanded(
            child: Card(
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(25),
              ),
              margin: const EdgeInsets.symmetric(
                horizontal: 25,
                vertical: 20
              ),
              child: InkWell(
                onTap: () {
                  Navigator.pushNamed(context, '/history-feedback');
                },
                child: Padding(
                  padding: const EdgeInsets.all(16.0),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: <Widget>[
                      Text('Feedback Historie',
                          style: TextStyle(
                              fontSize: 24.0, // Adjust font size
                              fontWeight: FontWeight.bold,
                              color: colors.primary)),
                      Icon(Icons.history, color: colors.primary),
                    ],
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
