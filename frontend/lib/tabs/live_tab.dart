import 'package:flutter/material.dart';
import 'package:frontend/components/input_card.dart';


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
    Size size = MediaQuery.of(context).size;

    return Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: <Widget>[
          SizedBox(
            height: 300,
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
                  joinCourse();
                },
                sublineText: 'Mit Code beitreten'),
          ),
        ],
      );
  }
}
