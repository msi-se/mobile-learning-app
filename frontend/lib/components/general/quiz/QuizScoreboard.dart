import 'package:flutter/material.dart';

class QuizScoreboard extends StatefulWidget {
  final List<dynamic> scoreboard;

  const QuizScoreboard({
    super.key,
    required this.scoreboard,
  });

  @override
  State<QuizScoreboard> createState() => _QuizScoreboardState();
}

class _QuizScoreboardState extends State<QuizScoreboard> {
  late List<dynamic> _scoreboard;

  @override
  void initState() {
    super.initState();
    _scoreboard = widget.scoreboard;
  }

  @override
  void didUpdateWidget(QuizScoreboard oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.scoreboard != widget.scoreboard) {
      setState(() {
        _scoreboard = widget.scoreboard;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;

    return ListView.builder(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      itemCount: _scoreboard.length,
      itemBuilder: (BuildContext context, int index) {
        return ListTile(
          leading: Text(
            _scoreboard[index]["rank"].toString(),
            style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 20),
          ),
          title: Text(_scoreboard[index]["userAlias"]),
          trailing: Text(
            _scoreboard[index]["score"].toString(),
            style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 20),
          ),
        );
      },
    );
  }
}
