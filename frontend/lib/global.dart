import 'package:shared_preferences/shared_preferences.dart';

class Session {
  String jwt;
  String userId;
  String username;
  String role;

  Session(
      {required this.userId,
      required this.username,
      required this.role,
      required this.jwt});
}

SharedPreferences? _preferences;

Future initPreferences() async {
  _preferences = await SharedPreferences.getInstance();
}

Session? getSession() {
  var jwt = _preferences?.getString("jwt");
  var userId = _preferences?.getString("userId");
  var username = _preferences?.getString("username");
  var role = _preferences?.getString("role");
  if (userId == null || username == null) {
    return null;
  }
  return Session(jwt:jwt!, userId: userId, username: username, role: role!);
}

Future<void> setSession(Session session) async {
  if (_preferences == null) {
    await initPreferences();
  }
  _preferences!.setString("jwt", session.jwt);
  _preferences!.setString("userId", session.userId);
  _preferences!.setString("username", session.username);
  _preferences!.setString("role", session.role);
}
