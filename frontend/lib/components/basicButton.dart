import 'package:flutter/material.dart';

enum ButtonType { primary, secondary, cancel }

class BasicButton extends StatelessWidget {
  final ButtonType type;
  final String text;
  final VoidCallback onPressed;

  const BasicButton({
    Key? key,
    required this.type,
    required this.text,
    required this.onPressed,
  }) : super(key: key);

  Color _getButtonColor(ColorScheme colors) {
    switch (type) {
      case ButtonType.primary:
        return colors.primary;
      case ButtonType.secondary:
        return colors.surfaceTint;
      case ButtonType.cancel:
        return const Color(0xFFEDF5F3);
      default:
        return colors.primary;
    }
  }

    Color _getTextColor(ColorScheme colors) {
    switch (type) {
      case ButtonType.primary:
        return Colors.white;
      case ButtonType.secondary:
        return colors.primary;
      case ButtonType.cancel:
        return Colors.red;
      default:
        return Colors.white;
    }
  }

  @override
  Widget build(BuildContext context) 
  {
    final colors = Theme.of(context).colorScheme;
    return ElevatedButton(
      onPressed: onPressed,
      style: ElevatedButton.styleFrom(
        backgroundColor: _getButtonColor(colors),
      ),
      child: Text(
        text,
        style: TextStyle(color: _getTextColor(colors)),
      ),
    );
  }
}
