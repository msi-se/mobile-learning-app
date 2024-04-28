import 'dart:convert';
import 'dart:io';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:http/http.dart' as http;
import 'package:jwt_decoder/jwt_decoder.dart';
import 'package:frontend/components/button.dart';
import 'package:frontend/components/textfield.dart';
import 'package:frontend/components/error/general_error_widget.dart';
import 'package:frontend/components/error/network_error_widget.dart';
import 'package:frontend/global.dart';
import 'package:frontend/theme/assets.dart';
import 'package:frontend/utils.dart';
import 'package:flutter_svg/svg.dart';


class LoginPage extends StatefulWidget {
  const LoginPage({super.key});

  @override
  State<LoginPage> createState() => _LoginPageState();
}

class _LoginPageState extends State<LoginPage> {
  final usernameController = TextEditingController();
  final passwordController = TextEditingController();
  bool _isLoading = false;
  bool _serverNotResponding = false;
  bool _isCheckingLoggedIn = true;

  @override
  void initState() {
    super.initState();
    checkLoggedIn();
    RawKeyboard.instance.addListener(_keyboardCallback);
  }

  @override
  void dispose() {
    RawKeyboard.instance.removeListener(_keyboardCallback);
    super.dispose();
  }

  void _keyboardCallback(RawKeyEvent event) {
    if (event.logicalKey == LogicalKeyboardKey.enter && !_isLoading) {
      _signInUser();
    }
  }

  Future<void> checkLoggedIn() async {
    var session = getSession();
    if (session != null && !JwtDecoder.isExpired(session.jwt)) {
      try {
        final response = await http.get(
          Uri.parse("${getBackendUrl()}/user/verify"),
          headers: {
            "Content-Type": "application/json",
            "Authorization": "Bearer ${session.jwt}"
          },
        ).timeout(const Duration(seconds: 4));

        if (response.statusCode == 200 && mounted) {
          Navigator.pushReplacementNamed(context, '/main');
          return;
        }
      } catch (e) {
        if (!mounted) return;
        setState(() {
          _serverNotResponding = true;
          _isCheckingLoggedIn = false;
        });
      }
    }
    clearSession();
    if (mounted) {
      setState(() => _isCheckingLoggedIn = false);
    }
  }

  void _retryConnection() {
    setState(() {
      _serverNotResponding = false;
    });
    checkLoggedIn();
  }

  Future<void> _signInUser() async {
    setState(() => _isLoading = true);

    var username = usernameController.text;
    var password = passwordController.text;
    try {
      final response = await http.post(
        Uri.parse("${getBackendUrl()}/user/login"),
        headers: {
          "Content-Type": "application/json",
          "Authorization": "Basic ${base64Encode(utf8.encode('$username:$password'))}"
        },
      );

      if (response.statusCode == 200) {
        var jwt = JwtDecoder.decode(response.body);
        await setSession(Session(
          jwt: response.body,
          userId: jwt["sub"],
          username: jwt["preferred_username"],
          fullName: jwt["full_name"],
          roles: jwt["groups"].cast<String>(),
        ));

        if (mounted) {
          Navigator.pushReplacementNamed(context, '/main');
        }
      } else {
        if (mounted) {
          _showDialog('Falsche Anmeldeinformationen');
        }
      }
    } catch (e) {
      if (mounted) {
        _handleLoginError(e as Exception);
      }
    } finally {
      if (mounted) {
        setState(() => _isLoading = false);
      }
    }
  }


  void _showDialog(String message) {
    showDialog(
      context: context,
      builder: (BuildContext context) => AlertDialog(
        title: Text(message),
        actions: <Widget>[
          TextButton(
            child: const Text('Schließen'),
            onPressed: () => Navigator.of(context).pop(),
          ),
        ],
      ),
    );
  }

  void _handleLoginError(Exception e) {
    showDialog(
      context: context,
      builder: (BuildContext context) {
        if (e is SocketException) {
          return const NetworkErrorWidget();
        } else {
          return const GeneralErrorWidget();
        }
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;

    if (_isCheckingLoggedIn) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }

    if (_serverNotResponding) {
      return _buildNoConnectionScreen();
    }


    return Scaffold(
      //backgroundColor: colors.secondary,
      appBar: AppBar(backgroundColor: colors.onError, toolbarHeight: 0),
      body: Stack(
        children: [
          Positioned.fill(
            child: SvgPicture.asset(
              htwgPattern,
              fit: BoxFit.cover,
            ),
          ),
        SafeArea(
        child: Center(
          child: SingleChildScrollView(
            child: LayoutBuilder(
              builder: (BuildContext context, BoxConstraints constraints) {
                double width = constraints.maxWidth > 480 ? 480 : constraints.maxWidth;
                return Container(
                  width: width,
                  padding: const EdgeInsets.all(20),
                  margin: const EdgeInsets.symmetric(horizontal: 20, vertical: 50),
                  decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(15),
                    border: Border.all(color: colors.outlineVariant, width: 1),
                    color: colors.background,
                  ),
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Row(
                        mainAxisAlignment: MainAxisAlignment.start,
                        children: [
                          SvgPicture.asset(htwgConnect, height: 130),
                          const SizedBox(width: 20),
                          Text('HTWG Connect', style: TextStyle(fontSize: 36, fontWeight: FontWeight.w900, color: colors.primary))
                        ],
                      ),
                      const SizedBox(height: 20),
                      const Divider(),
                      const SizedBox(height: 20),
                      const Text('Log In', style: TextStyle(fontSize: 36, fontWeight: FontWeight.bold)),
                      const SizedBox(height: 20),
                      const Text('Melden Sie sich mit Ihren HTWG-Zugangsdaten an', textAlign: TextAlign.center, style: TextStyle(fontSize: 16)), // Hinzugefügter Text
                      const SizedBox(height: 20),
                      _buildLoginField(title: 'Benutzername', hintText: 'z.B. ma871mu', controller: usernameController, obscureText: false),
                      const SizedBox(height: 20),
                      _buildLoginField(title: 'Passwort', hintText: '••••••••',controller: passwordController, obscureText: true),
                      const SizedBox(height: 20),
                      _isLoading ? const CircularProgressIndicator() : SubmitButton(onTap: _signInUser),
                      const SizedBox(height: 20),
                    ],
                  ),
                );
              },
            ),
          ),
        ),
      ),
    ]),
    );
  }

  Widget _buildLoginField({required String title, required String hintText, required TextEditingController controller, required bool obscureText}) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(title, style: Theme.of(context).textTheme.titleMedium),
        MyTextField(
          controller: controller,
          hintText: hintText,
          obscureText: obscureText,
        ),
      ],
    );
  }

  Widget _buildNoConnectionScreen() {
    return Scaffold(
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Text('Es scheint, als hättest du keine Internetverbindung.'),
            const SizedBox(height: 10),
            TextButton(
              onPressed: _retryConnection,
              child: const Text('Erneut versuchen'),
            ),
          ],
        ),
      ),
    );
  }
}