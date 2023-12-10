import 'package:flutter/foundation.dart';

String getBackendUrl({String protocol = 'http'}) {
  // final String? backendUrl = Platform.environment['BACKEND_URL']; // later maybe
  if (defaultTargetPlatform == TargetPlatform.android) {
    return '$protocol://10.0.2.2:8080';
  }
  return '$protocol://localhost:8080';
}
