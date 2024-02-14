import 'package:flutter/material.dart';

class CustomErrorWidget extends StatelessWidget {
  final String errorMessage;

  CustomErrorWidget({required this.errorMessage});

  @override
  Widget build(BuildContext context) {
    return const Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Directionality(
            textDirection: TextDirection.ltr,
            child: Icon(
              Icons.error_outline,
              color: Colors.red,
              size: 50.0,
            ),
          ),
          SizedBox(height: 10.0),
          Directionality(
            textDirection: TextDirection.ltr,
            child: Text(
              'An error occured. We are sorry!',
              style: TextStyle(fontSize: 18.0, fontWeight: FontWeight.bold),
            ),
          ),
          SizedBox(height: 5.0),
          Directionality(
            textDirection: TextDirection.ltr,
            child: Text(
              'Try restarting your application',
              style: TextStyle(
                  fontSize: 18.0, color: Color.fromARGB(255, 158, 158, 158)),
            ),
          ),

          // Directionality(
          //   textDirection: TextDirection.ltr,
          //   child: Padding(
          //     padding: const EdgeInsets.all(10),
          //     child: Text(
          //       errorMessage,
          //       textAlign: TextAlign.center,
          //       style: const TextStyle(fontSize: 16.0),
          //     ),
          //   ),
          // ),
        ],
      ),
    );
  }
}
