import 'package:flutter/foundation.dart';

String getBackendUrl({String protocol = 'http'}) {
  // final String? backendUrl = Platform.environment['BACKEND_URL']; // later maybe
  if (defaultTargetPlatform == TargetPlatform.android) {
    return '$protocol://loco.in.htwg-konstanz.de/api';
  }
  return '$protocol://loco.in.htwg-konstanz.de/api';
}
