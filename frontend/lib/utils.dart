import 'package:flutter/foundation.dart';
import 'package:flutter_dotenv/flutter_dotenv.dart';

String getBackendUrl({String protocol = 'http'}) {
  // final String? backendUrl = Platform.environment['BACKEND_URL']; // later maybe

  if (!kDebugMode) {
    String domain = dotenv.get('DOMAIN', fallback: 'connect.in.htwg-konstanz.de');
    return '${protocol}s://$domain/api';
  }

  // get the current domain (when running in a browser)
  final String domain = Uri.base.host;

  // if the domain is localhost, use the local backend
  if (domain == '' || domain == 'localhost') {
    if (defaultTargetPlatform == TargetPlatform.android) {
      return '$protocol://10.0.2.2:8080';
    }
    return '$protocol://localhost:8080';

    // if the domain is not localhost, use the deployed backend
  } else {
    return '${protocol}s://$domain/api';
  }
}
