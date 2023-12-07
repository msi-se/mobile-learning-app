import 'package:flutter/material.dart';
import 'package:frontend/pages/feedback/choose_feedback_page.dart';
import 'package:frontend/pages/login_page.dart';
import 'package:frontend/pages/main_page.dart';
import 'package:frontend/theme/themes.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: getTheme(lightColorScheme),
      initialRoute: '/login',
      routes: <String, WidgetBuilder>{
        '/login': (_) => const LoginPage(),
        '/home': (_) => const MainPage(),
        '/choose-feedback': (_) => const ChooseFeedbackPage(),
      },
    );
  }
}
