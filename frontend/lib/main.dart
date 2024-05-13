import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
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
import 'package:rive/rive.dart';

void main() async {
  await dotenv.load(fileName: ".env");
  WidgetsFlutterBinding.ensureInitialized();
  await initPreferences();

  // Preload Rive animation
  RiveFile? riveFile;
  await RiveFile.initializeText();
  await rootBundle.load('assets/animations/rive/animations.riv').then((data) {
    riveFile = RiveFile.import(data);
  });

  runApp(MyApp(riveFile: riveFile));
}

class MyApp extends StatelessWidget {
  final RiveFile? riveFile;

  const MyApp({Key? key, required this.riveFile});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'HTWG Connect',
      theme: getTheme(lightColorScheme),
      initialRoute: '/login',
      routes: <String, WidgetBuilder>{
        '/login': (context) => const LoginPage(),
        '/main': (context) => MainPage(riveFile: riveFile),
        '/profile': (context) => const ProfilePage(),
        '/menu': (context) => const MenuPage(),
        '/datenschutz': (context) => const PrivacyPage(),
        '/test': (context) => const TestPage(),
        '/feedback-info': (context) {
          var arguments = ModalRoute.of(context)!.settings.arguments
              as Map<String, dynamic>?;
          if (arguments == null) {
            return MainPage(riveFile: riveFile);
          }
          return FeedbackPreviewPage(
            courseId: arguments["courseId"],
            formId: arguments["formId"],
          );
        },
        '/attend-feedback': (context) {
          var code = ModalRoute.of(context)!.settings.arguments as String?;
          if (code == null) {
            return MainPage(riveFile: riveFile);
          }
          return AttendFeedbackPage(code: code, riveFile: riveFile);
        },
        '/feedback-result': (context) {
          var arguments = ModalRoute.of(context)!.settings.arguments
              as Map<String, dynamic>?;
          if (arguments == null) {
            return MainPage(riveFile: riveFile);
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
            return MainPage(riveFile: riveFile);
          }
          return QuizPreviewPage(
            courseId: arguments["courseId"],
            formId: arguments["formId"],
          );
        },
        '/attend-quiz': (context) {
          var code = ModalRoute.of(context)!.settings.arguments as String?;
          if (code == null) {
            return MainPage(riveFile: riveFile);
          }
          return AttendQuizPage(code: code, riveFile: riveFile);
        },
        '/quiz-control': (context) {
          var arguments = ModalRoute.of(context)!.settings.arguments
              as Map<String, dynamic>?;
          if (arguments == null) {
            return MainPage(riveFile: riveFile);
          }
          return QuizControlPage(
            courseId: arguments["courseId"],
            formId: arguments["formId"],
            riveFile: riveFile,
          );
        },
      },
    );
  }
}
