import 'package:flutter/material.dart';
import 'package:frontend/components/animations/throw.dart';
import 'package:frontend/components/general/quiz/animated_scoreboard.dart';
import 'package:rive/rive.dart';

class TestPage extends StatefulWidget {
  const TestPage({super.key});

  @override
  State<TestPage> createState() => _TestPageState();
}

class _TestPageState extends State<TestPage> with TickerProviderStateMixin {
  List<Widget> _animations = [];

  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;

    return Scaffold(
      appBar: AppBar(
        title: const Text("Test Seite",
            style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
        centerTitle: true,
        backgroundColor: colors.primary,
      ),
      body: AnimatedScoreboard(
          scoreboard: []), // This container takes up the whole screen
    );
  }
}
