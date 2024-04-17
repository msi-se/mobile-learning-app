import 'package:flutter/material.dart';
import 'package:frontend/global.dart';
import 'package:jwt_decoder/jwt_decoder.dart';

abstract class AuthState<T extends StatefulWidget> extends State<T> {
  
  @protected
  @mustCallSuper
  @override
  void initState() {
    super.initState();

    _checkLogin(context);
  }

  Future _checkLogin(BuildContext context) async {
    await initPreferences();
    if (getSession() != null) {
      if (!JwtDecoder.isExpired(getSession()!.jwt)) {
        return;
      }
    }
    clearSession();
    if (!context.mounted) return;
    Navigator.pushReplacementNamed(context, '/login');
  }
}