import 'package:shared_preferences/shared_preferences.dart';

class Session {
  String jwt;
  String userId;
  String username;
  String fullName;
  List<String> roles;

  Session(
      {required this.userId,
      required this.username,
      required this.fullName,
      required this.roles,
      required this.jwt});
}

SharedPreferences? _preferences;

Future initPreferences() async {
  if (_preferences != null) {
    return;
  }
  _preferences = await SharedPreferences.getInstance();
}

Session? getSession() {
  var jwt = _preferences?.getString("jwt");
  var userId = _preferences?.getString("userId");
  var username = _preferences?.getString("username");
  var fullName = _preferences?.getString("fullName");
  var roles = _preferences?.getStringList("roles");
  if (jwt == null ||
      userId == null ||
      username == null ||
      fullName == null ||
      roles == null) {
    return null;
  }
  return Session(
      jwt: jwt,
      userId: userId,
      username: username,
      fullName: fullName,
      roles: roles);
}

Future<void> setSession(Session session) async {
  if (_preferences == null) {
    await initPreferences();
  }
  _preferences!.setString("jwt", session.jwt);
  _preferences!.setString("userId", session.userId);
  _preferences!.setString("username", session.username);
  _preferences!.setString("fullName", session.fullName);
  _preferences!.setStringList("roles", session.roles);
}

void clearSession() {
  _preferences?.clear();
}
