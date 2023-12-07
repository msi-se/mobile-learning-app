import 'package:flutter/material.dart';
import 'package:frontend/components/input_card.dart';

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

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    Size size = MediaQuery.of(context).size;

    return Scaffold(
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            InputCard(
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
                  print(_joinCodeController.text);
                },
                sublineText: 'Mit Code beitreten'),
            ElevatedButton(
              onPressed: () {
                Navigator.pushNamed(context, '/choose-feedback');
              },
              child: Text('Feedbackbogen auswählen',
                  style: TextStyle(color: colors.primary)),
            ),
          ],
        ),
      ),
    );
  }
}
