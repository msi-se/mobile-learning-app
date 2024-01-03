import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

// import 'package:flutter/src/widgets/container.dart';
// import 'package:flutter/src/widgets/framework.dart';

class CodeInput extends StatefulWidget {
  final String inputText;
  final TextEditingController textInputController;
  final Function onSubmit;
  final Function(String) onChanged;
  final TextInputType textInput;
  final int maxLength;

  const CodeInput({
    super.key,
    required this.inputText,
    required this.onSubmit,
    required this.onChanged,
    required this.textInputController,
    required this.textInput,
    required this.maxLength,
  });

  @override
  State<CodeInput> createState() => _CodeInputState();
}

class _CodeInputState extends State<CodeInput> {
  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;

    return Container(
      height: 50,
      width: 180,
      decoration: BoxDecoration(
          border: Border.all(
            color: colors.onSurface,
          ),
          borderRadius: const BorderRadius.all(Radius.circular(100))),
      child: Stack(
        children: [
          Center(
            child: SizedBox(
              width: 80,
              child: TextFormField(
                  keyboardType: widget.textInput,
                  cursorColor: colors.primary,
                  controller: widget.textInputController,
                  inputFormatters: [
                    LengthLimitingTextInputFormatter(widget.maxLength)
                  ],
                  onChanged: (value) {
                    widget.onChanged(value);
                  },
                  decoration: InputDecoration(
                    hintText: widget.inputText,
                    hintStyle: Theme.of(context).textTheme.bodyLarge,
                    border: InputBorder.none,
                  ),
                  style: Theme.of(context).textTheme.bodyLarge),
            ),
          ),
          Align(
            alignment: Alignment.topRight,
            child: SizedBox(
              width: 48,
              child: ElevatedButton(
                onPressed: () {
                  widget.onSubmit();
                },
                style: ElevatedButton.styleFrom(
                  padding: EdgeInsets.zero,
                  shape: const CircleBorder(),
                  fixedSize: const Size(50, 50),
                  backgroundColor: colors.primary,
                ),
                child: Icon(
                  Icons.arrow_forward,
                  color: colors.background,
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
