import 'package:flutter/material.dart';
import 'package:frontend/components/button.dart';
import 'package:frontend/components/textfield.dart';
//import 'package:frontend/theme/assets.dart';

class LoginPage extends StatefulWidget {
  const LoginPage({super.key});

  @override
  State<LoginPage> createState() => _LoginPageState();
}

class _LoginPageState extends State<LoginPage> {
  void signUserIn(BuildContext context) {
    // Code, um zu überprüfen, ob der Benutzer von der HTWG ist

    // Wenn der Benutzer von der HTWG ist, dann einloggen und zur Seite "/home" navigieren
    Navigator.pushNamed(context, '/home');
  }

  final passwordController = TextEditingController();
  final usernameController = TextEditingController();

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;

    return Scaffold(
      body: SafeArea(
        child: Column(
          children: [
            Container(
              color: colors.outlineVariant,
              child: SingleChildScrollView(
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
                      margin: const EdgeInsets.all(30.0),
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
            ),
            Container(
              color: colors.surface,
              child: SingleChildScrollView(
                child: Column(
                  children: [
                    // Username TextField
                    Container(
                      margin:
                          const EdgeInsets.only(left: 5, top: 15, bottom: 5),
                      child: const Row(
                        mainAxisAlignment: MainAxisAlignment.start,
                        children: [
                          Text('Your E-Mail',
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
                        hintText: 'Max.Mustermann@htwg-konstanz.de',
                        obscureText: false),

                    // Password TextField
                    Container(
                      margin: const EdgeInsets.all(5.0),
                      child: const Row(
                        mainAxisAlignment: MainAxisAlignment.start,
                        children: [
                          Text('Your Password',
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
            ),
          ],
        ),
      ),
    );
  }
}
