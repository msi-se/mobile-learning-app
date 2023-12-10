import 'package:shared_preferences/shared_preferences.dart';

class Session{
  String userId;
  String username;

  Session({required this.userId, required this.username});
}

SharedPreferences? _preferences;

Future initPreferences() async {
  _preferences = await SharedPreferences.getInstance();
}

Session? getSession() {
  var userId = _preferences?.getString("userId");
  var username = _preferences?.getString("username");
  if (userId == null || username == null) {
    return null;
  }
  return Session(userId: userId, username: username);
}

Future<void> setSession(Session session) async {
  if (_preferences == null) {
    await initPreferences();
  }
  _preferences!.setString("userId", session.userId);
  _preferences!.setString("username", session.username);
}
