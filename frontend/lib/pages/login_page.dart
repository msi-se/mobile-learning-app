import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:frontend/components/button.dart';
import 'package:frontend/components/textfield.dart';
import 'package:frontend/global.dart';
import 'package:frontend/utils.dart';
import 'package:http/http.dart' as http;

class LoginPage extends StatefulWidget {
  const LoginPage({super.key});

  @override
  State<LoginPage> createState() => _LoginPageState();
}

class _LoginPageState extends State<LoginPage> {
  final usernameController = TextEditingController();
  final passwordController = TextEditingController();

  @override
  void initState() {
    super.initState();
    checkLoggenIn();
  }

  Future checkLoggenIn() async {
    if (getSession() != null && mounted) {
      Navigator.pushReplacementNamed(context, '/main');
    }
  }

  Future signUserIn(BuildContext context) async {
    try {
      String body = jsonEncode({
        "id": null,
        "username": usernameController.text,
        "password": passwordController.text
      });
      final response =
          await http.post(Uri.parse("${getBackendUrl()}/auth/login"),
              headers: {
                "Content-Type": "application/json",
              },
              body: body);
      if (response.statusCode == 200) {
        var data = jsonDecode(response.body);
        var userId = data["id"];
        var username = data["username"];
        await setSession(Session(userId: userId, username: username));
        if (!mounted) return;
        Navigator.pushReplacementNamed(context, '/main');
      } else {
        // TODO: wrong credentials
      }
    } on http.ClientException catch (_) {
      // TODO: handle error
    }
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;

    return Scaffold(
      appBar: AppBar(
        backgroundColor: colors.outlineVariant,
        toolbarHeight: 5,
      ),
      body: SafeArea(
        child: SingleChildScrollView(
          child: Column(
            children: [
              Container(
                color: colors.outlineVariant,
                child: Column(
                  children: [
                    Container(
                      margin: const EdgeInsets.all(20.0),
                      child: Image.asset(
                        'assets/logo/HTWG_extended.png',
                        height: 100.0,
                      ),
                    ),
                    // Login Text
                    Container(
                      margin: const EdgeInsets.all(18.0),
                      child: const Row(
                        mainAxisAlignment: MainAxisAlignment.start,
                        children: [
                          Text('Log In',
                              style: TextStyle(
                                  fontSize: 40,
                                  color: Colors.black,
                                  fontWeight: FontWeight.bold),
                              textAlign: TextAlign.left),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
              Container(
                color: colors.surface,
                child: Column(
                  children: [
                    // Username TextField
                    Container(
                      padding: const EdgeInsets.only(top: 10),
                      margin: const EdgeInsets.symmetric(horizontal: 20),
                      child: const Row(
                        mainAxisAlignment: MainAxisAlignment.start,
                        children: [
                          Text('Benutzername',
                              style: TextStyle(
                                fontSize: 15,
                                color: Colors.grey,
                              ),
                              textAlign: TextAlign.left),
                        ],
                      ),
                    ),
                    MyTextField(
                        controller: usernameController,
                        hintText: 'Max.Mustermann',
                        obscureText: false),

                    // Password TextField
                    Container(
                      padding: const EdgeInsets.only(top: 10),
                      margin: const EdgeInsets.symmetric(horizontal: 20),
                      child: const Row(
                        mainAxisAlignment: MainAxisAlignment.start,
                        children: [
                          Text('Passwort',
                              style: TextStyle(
                                fontSize: 15,
                                color: Colors.grey,
                              ),
                              textAlign: TextAlign.left),
                        ],
                      ),
                    ),
                    MyTextField(
                        controller: passwordController,
                        hintText: '***********',
                        obscureText: true),
                    const SizedBox(height: 10),

                    // Submit Button
                    SubmitButton(
                      onTap: () {
                        signUserIn(context);
                      },
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
