import 'package:flutter/material.dart';

class CustomErrorWidget extends StatelessWidget {
  final String errorMessage;

  CustomErrorWidget({required this.errorMessage});

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Directionality(
        textDirection: TextDirection.ltr,
        child: Padding(
          padding: const EdgeInsets.all(8.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(
                Icons.error_outline,
                color: Colors.red,
                size: 50.0,
              ),
              SizedBox(height: 10.0),
              Text(
                'Ein unerwarteter Fehler ist aufgetreten. Bitte entschuldige!',
                style: TextStyle(fontSize: 18.0, fontWeight: FontWeight.bold),
                textAlign: TextAlign.center,
              ),
              SizedBox(height: 5.0),
              Text(
                'Versuche, die App neu zu starten',
                style: TextStyle(
                    fontSize: 18.0, color: Color.fromARGB(255, 158, 158, 158)),
                textAlign: TextAlign.center,
              ),
            ],
          ),
        ),
      ),
    );
  }
}
