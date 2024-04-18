import 'package:flutter/material.dart';
import 'package:frontend/components/animations/throw.dart';
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
      body: SafeArea(
        child: GestureDetector(
          onTapUp: (details) {
            print("ADD ANIMATION");
            setState(() {
              _animations.insert(0, Throw(
                  key: UniqueKey(),
                  // throwType: ThrowType.paperPlane,
                  // throwType: ThrowType.stone,
                  // throwType: ThrowType.dart,
                  throwType: ThrowType.ball,
                  clickX: details.localPosition.dx,
                  clickY: details.localPosition.dy));
              print(_animations.length);
            });
            Future.delayed(const Duration(milliseconds: 2500), () {
              if (!mounted) return;
              setState(() {
                _animations.removeLast();
              });
            });
          },
          child: Stack(children: [
            Container(
                color: Colors
                    .transparent), // This container takes up the whole screen
            ..._animations,
          ]),
        ),
      ),
    );
  }
}
