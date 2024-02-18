import 'package:shared_preferences/shared_preferences.dart';

class Session {
  String jwt;
  String userId;
  String username;
  String password;
  List<String> roles;

  Session(
      {required this.userId,
      required this.username,
      required this.password,
      required this.roles,
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
  var password = _preferences?.getString("password");
  var roles = _preferences?.getStringList("roles");
  if (jwt == null ||
      userId == null ||
      username == null ||
      password == null ||
      roles == null) {
    return null;
  }
  return Session(
      jwt: jwt,
      userId: userId,
      username: username,
      password: password,
      roles: roles);
}

Future<void> setSession(Session session) async {
  if (_preferences == null) {
    await initPreferences();
  }
  _preferences!.setString("jwt", session.jwt);
  _preferences!.setString("userId", session.userId);
  _preferences!.setString("username", session.username);
  _preferences!.setString("password", session.password);
  _preferences!.setStringList("roles", session.roles);
}
