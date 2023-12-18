import 'package:shared_preferences/shared_preferences.dart';

class Session{
  String userId;
  String username;
  String role;

  Session({required this.userId, required this.username, required this.role});
}

SharedPreferences? _preferences;

Future initPreferences() async {
  _preferences = await SharedPreferences.getInstance();
}

Session? getSession() {
  var userId = _preferences?.getString("userId");
  var username = _preferences?.getString("username");
  var role = _preferences?.getString("role");
  if (userId == null || username == null) {
    return null;
  }
  return Session(userId: userId, username: username, role: role!);
}

Future<void> setSession(Session session) async {

  if (_preferences == null) {
    await initPreferences();
  }
  _preferences!.setString("userId", session.userId);
  _preferences!.setString("username", session.username);
  _preferences!.setString("role", session.role);
}
