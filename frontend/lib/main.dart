import 'package:flutter/material.dart';
import 'package:frontend/global.dart';
import 'package:frontend/pages/feedback/attend_feedback_page.dart';
import 'package:frontend/pages/feedback/feedback_preview_page.dart';
import 'package:frontend/pages/feedback/feedback_result_page.dart';
import 'package:frontend/pages/login_page.dart';
import 'package:frontend/pages/main_page.dart';
import 'package:frontend/pages/menu_page.dart';
import 'package:frontend/pages/privacy_page.dart';
import 'package:frontend/pages/test_page.dart';
import 'package:frontend/pages/profile_page.dart';
import 'package:frontend/pages/quiz/attend_quiz_page.dart';
import 'package:frontend/pages/quiz/quiz_control_page.dart';
import 'package:frontend/pages/quiz/quiz_preview_page.dart';
import 'package:frontend/theme/themes.dart';
import 'package:flutter_dotenv/flutter_dotenv.dart';

void main() async {
  await dotenv.load(fileName: ".env");
  WidgetsFlutterBinding.ensureInitialized();
  await initPreferences();
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'HTWG Connect',
      theme: getTheme(lightColorScheme),
      initialRoute: '/login',
      routes: <String, WidgetBuilder>{
        '/login': (context) => const LoginPage(),
        '/main': (context) => const MainPage(),
        '/profile': (context) => const ProfilePage(),
        '/menu': (context) => const MenuPage(),
        '/datenschutz': (context) => const PrivacyPage(),
        '/test': (context) => const TestPage(),
        '/feedback-info': (context) {
          var arguments = ModalRoute.of(context)!.settings.arguments
              as Map<String, dynamic>?;
          if (arguments == null) {
            return const MainPage();
          }
          return FeedbackPreviewPage(
            courseId: arguments["courseId"],
            formId: arguments["formId"],
          );
        },
        '/attend-feedback': (context) {
          var code = ModalRoute.of(context)!.settings.arguments as String?;
          if (code == null) {
            return const MainPage();
          }
          return AttendFeedbackPage(code: code);
        },
        '/feedback-result': (context) {
          var arguments = ModalRoute.of(context)!.settings.arguments
              as Map<String, dynamic>?;
          if (arguments == null) {
            return const MainPage();
          }
          return FeedbackResultPage(
            courseId: arguments["courseId"],
            formId: arguments["formId"],
          );
        },
        // '/history-feedback': (_) => const HistoryFeedbackPage(),
        '/quiz-info': (context) {
          var arguments = ModalRoute.of(context)!.settings.arguments
              as Map<String, dynamic>?;
          if (arguments == null) {
            return const MainPage();
          }
          return QuizPreviewPage(
            courseId: arguments["courseId"],
            formId: arguments["formId"],
          );
        },
        '/attend-quiz': (context) {
          var code = ModalRoute.of(context)!.settings.arguments as String?;
          if (code == null) {
            return const MainPage();
          }
          return AttendQuizPage(code: code);
        },
        '/quiz-control': (context) {
          var arguments = ModalRoute.of(context)!.settings.arguments
              as Map<String, dynamic>?;
          if (arguments == null) {
            return const MainPage();
          }
          return QuizControlPage(
            courseId: arguments["courseId"],
            formId: arguments["formId"],
          );
        },
      },
    );
  }
}
