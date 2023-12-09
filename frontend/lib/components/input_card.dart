import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

// import 'package:flutter/src/widgets/container.dart';
// import 'package:flutter/src/widgets/framework.dart';

class InputCard extends StatefulWidget {
  final Size size;
  final String inputText;
  final TextEditingController textInputController;
  final Function onSubmit;
  final Function(String) onChanged;
  final String sublineText;
  final TextInputType textInput;
  final int maxLength;

  const InputCard({
    super.key,
    required this.size,
    required this.inputText,
    required this.onSubmit,
    required this.onChanged,
    required this.textInputController,
    required this.sublineText,
    required this.textInput,
    required this.maxLength,
  });

  @override
  State<InputCard> createState() => _InputCardState();
}

class _InputCardState extends State<InputCard> {
  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;

    return Card(
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(25),
      ),
      margin: const EdgeInsets.symmetric(
        horizontal: 25,
        vertical: 20
      ),
      // color: colors.secondary,
      child: SizedBox(
        child: Stack(
          children: [
            Center(
              child: Container(
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
                              hintStyle: Theme.of(context).textTheme.bodyMedium,
                              border: InputBorder.none,
                            ),
                            style: Theme.of(context).textTheme.bodyMedium),
                      ),
                    ),
                    Align(
                      alignment: Alignment.topRight,
                      child: SizedBox(
                        width: 49,
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
              ),
            ),
            Align(
                alignment: Alignment.bottomCenter,
                child: Padding(
                  padding: const EdgeInsets.only(bottom: 15),
                  child: Text(widget.sublineText,
                      style: Theme.of(context).textTheme.bodySmall),
                ))
          ],
        ),
      ),
    );
  }
}
