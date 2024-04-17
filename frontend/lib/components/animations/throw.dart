import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:rive/rive.dart';

enum ThrowType {
  paperPlane(
      filename: "throw_animations.riv",
      artboard: "Paper Plane",
      stateMachine: "Hit State Machine",
      width: 700,
      height: 1200,
      hitX: 602,
      hitY: 144),
  stone(
      filename: "throw_animations.riv",
      artboard: "Stone",
      stateMachine: "State Machine 1",
      width: 700,
      height: 500,
      hitX: 614,
      hitY: 96),
  dart(
      filename: "throw_animations.riv",
      artboard: "Dart",
      stateMachine: "State Machine 1",
      width: 700,
      height: 1000,
      hitX: 660,
      hitY: 41),
  ball(
      filename: "throw_animations.riv",
      artboard: "Ball",
      stateMachine: "State Machine 1",
      width: 700,
      height: 1200,
      hitX: 640,
      hitY: 39);

  final String filename;
  final String artboard;
  final String stateMachine;
  final double width;
  final double height;
  final double hitX;
  final double hitY;

  const ThrowType(
      {required this.filename,
      required this.artboard,
      required this.stateMachine,
      required this.hitX,
      required this.width,
      required this.height,
      required this.hitY});

  static ThrowType fromString(String str) {
    for (var value in ThrowType.values) {
      if (value.name.toLowerCase() == str.toLowerCase()) return value;
    }
    throw ArgumentError('Invalid form type: $str');
  }
}

class Throw extends StatefulWidget {
  final ThrowType throwType;
  final double clickX;
  final double clickY;

  const Throw(
      {required this.throwType,
      required this.clickX,
      required this.clickY,
      super.key});

  @override
  State<Throw> createState() => _ThrowState();
}

class _ThrowState extends State<Throw> {
  final riveDirName = 'assets/animations/rive';

  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return Positioned(
      key: UniqueKey(),
      left: widget.clickX - widget.throwType.hitX,
      top: widget.clickY - widget.throwType.hitY,
      child: SizedBox(
        width: widget.throwType.width,
        height: widget.throwType.height,
        child: RiveAnimation.asset(
          "$riveDirName/${widget.throwType.filename}",
          fit: BoxFit.cover,
          artboard: widget.throwType.artboard,
          stateMachines: [widget.throwType.stateMachine],
        ),
      ),
    );
  }
}
