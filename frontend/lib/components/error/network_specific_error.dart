import 'package:flutter/material.dart';

class NetworkErrorWidget extends StatelessWidget {
  final String originalRoute;

  const NetworkErrorWidget({Key? key, required this.originalRoute})
      : super(key: key);

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
              const Icon(
                Icons.error_outline,
                color: Colors.red,
                size: 50.0,
              ),
              const SizedBox(height: 10.0),
              const Text(
                'Es gab einen Netzwerk-Fehler',
                style: TextStyle(fontSize: 18.0, fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 5.0),
              const Text(
                'Versichere dich, dass eine Internetverbindung besteht.',
                style: TextStyle(
                    fontSize: 18.0, color: Color.fromARGB(255, 158, 158, 158)),
              ),
              const SizedBox(height: 5),
              const Text(
                'Falls der Fehler weiterhin besteht, kontaktiere die App-Betreiber',
                style: TextStyle(
                    fontSize: 18.0, color: Color.fromARGB(255, 158, 158, 158)),
              ),
              ElevatedButton(
                onPressed: () {
                  Navigator.pop(context);
                  Navigator.pushNamed(context, originalRoute);
                },
                child: const Text('Erneut versuchen'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
