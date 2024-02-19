import 'package:flutter/foundation.dart';

String getBackendUrl({String protocol = 'http'}) {
  // final String? backendUrl = Platform.environment['BACKEND_URL']; // later maybe
  if (defaultTargetPlatform == TargetPlatform.android) {
    return '$protocol://10.0.2.2/api';
  }
  return '$protocol://localhost/api';
}
