import 'package:flutter/material.dart';

class NetworkErrorWidget extends StatelessWidget {
  final String originalRoute;

  const NetworkErrorWidget({Key? key, required this.originalRoute})
      : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const Directionality(
            textDirection: TextDirection.ltr,
            child: Icon(
              Icons.error_outline,
              color: Colors.red,
              size: 50.0,
            ),
          ),
          const SizedBox(height: 10.0),
          const Directionality(
            textDirection: TextDirection.ltr,
            child: Text(
              'There was a network connection issue',
              style: TextStyle(fontSize: 18.0, fontWeight: FontWeight.bold),
            ),
          ),
          const SizedBox(height: 5.0),
          const Directionality(
            textDirection: TextDirection.ltr,
            child: Text(
              'Ensure that you are connected to the internet',
              style: TextStyle(
                  fontSize: 18.0, color: Color.fromARGB(255, 158, 158, 158)),
            ),
          ),
          const SizedBox(height: 10.0),
          ElevatedButton(
            onPressed: () {
              Navigator.pop(context);
              Navigator.pushNamed(context, originalRoute);
            },
            child: const Text('Retry'),
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
