import 'package:flutter/material.dart';

class QuizScoreboard extends StatefulWidget {
  final List<dynamic> scoreboard;
  final String alias;

  const QuizScoreboard({
    super.key,
    required this.scoreboard,
    required this.alias,
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
        return Container(
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(0.0),
              border: (_scoreboard[index]["userAlias"] == widget.alias)
                  ? Border.all(
                      color: colors
                          .primary, // Change this color to whatever you want
                      width: 2.0, // Change this width to whatever you want
                    )
                  : Border.all(
                      color: Colors
                          .transparent, // No border when condition is not met
                    ),
            ),
            child: ListTile(
              leading: Text(
                _scoreboard[index]["rank"].toString(),
                style:
                    const TextStyle(fontWeight: FontWeight.bold, fontSize: 20),
              ),
              title: Text(_scoreboard[index]["userAlias"]),
              trailing: Text(
                _scoreboard[index]["score"].toString(),
                style:
                    const TextStyle(fontWeight: FontWeight.bold, fontSize: 20),
              ),
            ));
      },
    );
  }
}
