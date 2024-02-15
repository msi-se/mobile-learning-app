import 'dart:convert';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:frontend/components/button.dart';
import 'package:frontend/components/error/network-specific-error.dart';
import 'package:frontend/components/textfield.dart';
import 'package:frontend/global.dart';
import 'package:frontend/theme/assets.dart';
import 'package:frontend/utils.dart';
import 'package:http/http.dart' as http;
import 'package:jwt_decoder/jwt_decoder.dart';

class LoginPage extends StatefulWidget {
  const LoginPage({super.key});

  @override
  State<LoginPage> createState() => _LoginPageState();
}

class _LoginPageState extends State<LoginPage> {
  final usernameController = TextEditingController();
  final passwordController = TextEditingController();
  bool _isLoading = false;

  @override
  void initState() {
    super.initState();
    checkLoggedIn();
  }

  Future checkLoggedIn() async {
    if (getSession() != null && mounted) {
      Navigator.pushReplacementNamed(context, '/main');
    }
  }

  Future signUserIn(BuildContext context) async {
    try {
      var username = usernameController.text;
      var password = passwordController.text;
      final response = await http.post(
        Uri.parse("${getBackendUrl()}/user/login"),
        headers: {
          "Content-Type": "application/json",
          "AUTHORIZATION":
              "Basic ${base64Encode(utf8.encode('$username:$password'))}"
        },
      );

      if (response.statusCode == 200) {
        var jwt = JwtDecoder.decode(response.body);
        var userId = jwt["sub"];
        var username = jwt["preferred_username"];
        var roles = jwt["groups"].cast<String>();
        await setSession(Session(
            jwt: response.body,
            userId: userId,
            username: username,
            roles: roles));
        if (!mounted) return;
        Navigator.pushReplacementNamed(context, '/main');
      } else {
        if (!mounted) return;
        showDialog(
          context: context,
          builder: (BuildContext context) {
            return AlertDialog(
              title: const Text('Falsche Anmeldeinformationen'),
              actions: <Widget>[
                TextButton(
                  child: const Text('Schließen'),
                  onPressed: () {
                    Navigator.of(context).pop();
                  },
                ),
              ],
            );
          },
        );
      }
    } on http.ClientException {
      if (mounted) {
        Navigator.push(
          context,
          MaterialPageRoute(
              builder: (context) =>
                  const NetworkErrorWidget(originalRoute: '/login')),
        );
      }
    } on SocketException {
      if (mounted) {
        Navigator.push(
          context,
          MaterialPageRoute(
              builder: (context) =>
                  const NetworkErrorWidget(originalRoute: '/login')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;

    return Scaffold(
      appBar: AppBar(
        backgroundColor: colors.outlineVariant,
        toolbarHeight: 0,
      ),
      body: SafeArea(
        child: SingleChildScrollView(
          child: Container(
            color: colors.outlineVariant,
            child: Column(
              children: [
                Container(
                  color: colors.outlineVariant,
                  child: Column(
                    children: [
                      Container(
                        margin: const EdgeInsets.all(20.0),
                        child: Image.asset(
                          htwgExtendedLogo,
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
                // Bottom Half of the Login Screen
                Container(
                  decoration: BoxDecoration(
                      color: colors.surface,
                      borderRadius: const BorderRadius.only(
                          topLeft: Radius.circular(20),
                          topRight: Radius.circular(20))),
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
                          hintText: '',
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
                          hintText: '',
                          obscureText: true),
                      const SizedBox(height: 10),

                      // Submit Button
                      if (_isLoading)
                        const CircularProgressIndicator()
                      else
                        SubmitButton(
                          onTap: () async {
                            setState(() {
                              _isLoading = true;
                            });
                            await signUserIn(context);
                            setState(() {
                              _isLoading = false;
                            });
                          },
                        ),
                    ],
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
