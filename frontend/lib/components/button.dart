import 'package:flutter/material.dart';

class SubmitButton extends StatelessWidget {
  final Function()? onTap;

  const SubmitButton({super.key, required this.onTap});

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    return GestureDetector(
      onTap: onTap,
      child: Container(
        height: 60,
        padding: const EdgeInsets.symmetric(vertical: 15, horizontal: 5),
        margin: const EdgeInsets.symmetric(vertical: 10, horizontal: 40),
        decoration: BoxDecoration(
          color: colors.primary,
          borderRadius: BorderRadius.circular(12),
        ),
        child: const Center(
          child: Text(
            "Log In",
            style: TextStyle(
              color: Colors.white,
              fontWeight: FontWeight.bold,
              fontSize: 20,
            ),
          ),
        ),
      ),
    );
  }
}
