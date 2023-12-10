String getBackendUrl({String protocol = 'http'}) {
  // final String? backendUrl = Platform.environment['BACKEND_URL']; // later maybe
  //return '$protocol://10.0.2.2:8080';
  return '$protocol://localhost:8080';
}