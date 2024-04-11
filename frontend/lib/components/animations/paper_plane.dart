import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:rive/rive.dart';

class PaperPlane extends StatefulWidget {
  final double clickX;
  final double clickY;

  const PaperPlane({required this.clickX, required this.clickY, super.key});

  @override
  State<PaperPlane> createState() => _PaperPlaneState();
}

class _PaperPlaneState extends State<PaperPlane> {
  final riveFileName = 'assets/animations/rive/paper_plane.riv';

  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return Positioned(
      key: UniqueKey(),
      top: widget.clickY - 144,
      left: widget.clickX - 602,
      child: SizedBox(
        width: 700,
        height: 1200,
        child: RiveAnimation.asset(
          riveFileName,
          fit: BoxFit.cover,
          artboard: 'Artboard',
          stateMachines: const ['Hit State Machine'],
        ),
      ),
    );
  }
}
