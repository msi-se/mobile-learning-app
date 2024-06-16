import 'package:flutter/material.dart';
import 'package:frontend/pages/privacy_page.dart';

class PrivacyApproval extends StatefulWidget {
  @override
  State<PrivacyApproval> createState() => _PrivacyApprovalState();
  final ValueChanged<bool?> onSelectionChanged;
  final bool? isChecked;

  const PrivacyApproval(
      {super.key, required this.onSelectionChanged, required this.isChecked});
}

class _PrivacyApprovalState extends State<PrivacyApproval> {
  @override
  Widget build(BuildContext context) {
    return CheckboxListTile(
      title: TextButton(
        onPressed: () {
          Navigator.push(
            context,
            MaterialPageRoute(builder: (context) => PrivacyPage()),
          );
        },
        child: const Text(
          'Ich habe die Datenschutzerklärung gelesen und akzeptiere diese.',
          style: TextStyle(fontSize: 16),
        ),
      ),
      value: widget.isChecked,
      onChanged: widget.onSelectionChanged,
      controlAffinity: ListTileControlAffinity.leading,
    );
  }
}
