import 'package:flutter/material.dart';
import 'package:frontend/theme/assets.dart';

class LoginPage extends StatefulWidget {
  const LoginPage({super.key});

  @override
  State<LoginPage> createState() => _LoginPageState();
}

class _LoginPageState extends State<LoginPage> {
  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;

    return Scaffold(
      body: SafeArea(
        child: Column(
          children: [
            // Oberer Abschnitt
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
                                  fontSize: 30,
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

            // Unterer Abschnitt
            Container(
              color:
                  colors.surface, // Hintergrundfarbe für den unteren Abschnitt
              child: SingleChildScrollView(
                child: Column(
                  children: [
                    // Username TextField

                    // Password TextField

                    const SizedBox(height: 400), //Placeholder

                    // Submit Button
                    ElevatedButton(
                      onPressed: () {
                        Navigator.pushReplacementNamed(context, '/home');
                      },
                      style: ButtonStyle(
                        backgroundColor:
                            MaterialStateProperty.all(colors.primary),
                        shape:
                            MaterialStateProperty.all<RoundedRectangleBorder>(
                          RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(10.0),
                          ),
                        ),
                      ),
                      child: Text(
                        'Login',
                        style: TextStyle(color: colors.surface),
                      ),
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
