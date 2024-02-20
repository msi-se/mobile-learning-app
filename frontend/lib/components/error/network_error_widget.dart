import 'package:flutter/material.dart';

class NetworkErrorWidget extends StatelessWidget {
  const NetworkErrorWidget({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Center(
      child: AlertDialog(
        shape: const RoundedRectangleBorder(
          borderRadius: BorderRadius.all(Radius.circular(20.0)),
        ),
        title: const Icon(
          Icons.error_outline,
          color: Colors.red,
          size: 50.0,
        ),
        content: const Text(
          'Netzwerkfehler\n'
          'Versichere dich, dass du Internet hast.\n'
          'Ansonsten versuche es später erneut.',
          textAlign: TextAlign.center,
        ),
        actions: <Widget>[
          TextButton(
            child: const Text('OK'),
            onPressed: () {
              Navigator.of(context).pop('back');
              return;
            },
          ),
        ],
      ),
    );
  }
}
