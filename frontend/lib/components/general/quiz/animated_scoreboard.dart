import 'package:flutter/material.dart';

class AnimatedScoreboard extends StatefulWidget {
  final List<dynamic> scoreboard;

  const AnimatedScoreboard({
    super.key,
    required this.scoreboard,
  });

  @override
  State<AnimatedScoreboard> createState() => _AnimatedScoreboardState();
}

class _AnimatedScoreboardState extends State<AnimatedScoreboard> {
  late List<dynamic> _scoreboard;

  @override
  void initState() {
    super.initState();
    _scoreboard = [
      {"rank": 1, "userAlias": "Max Mustermann", "score": 100},
      {"rank": 2, "userAlias": "Max Mustermann", "score": 90},
      {"rank": 3, "userAlias": "Max Mustermann", "score": 80},
      {"rank": 4, "userAlias": "Max Mustermann", "score": 70},
      {"rank": 5, "userAlias": "Max Mustermann", "score": 60},
      {"rank": 6, "userAlias": "Max Mustermann", "score": 50},
      {"rank": 7, "userAlias": "Max Mustermann", "score": 40},
      {"rank": 8, "userAlias": "Max Mustermann", "score": 30},
      {"rank": 9, "userAlias": "Max Mustermann", "score": 20},
      {"rank": 10, "userAlias": "Max Mustermann", "score": 0},
      {"rank": 11, "userAlias": "Max Mustermann", "score": 0},
      {"rank": 12, "userAlias": "Max Mustermann", "score": 0},
      {"rank": 13, "userAlias": "Max Mustermann", "score": 0},
      {"rank": 14, "userAlias": "Max Mustermann", "score": 0},
      {"rank": 15, "userAlias": "Max Mustermann", "score": 0},
      {"rank": 16, "userAlias": "Max Mustermann", "score": 0},
      {"rank": 17, "userAlias": "Max Mustermann", "score": 0},
      {"rank": 18, "userAlias": "Max Mustermann", "score": 0},
      {"rank": 19, "userAlias": "Max Mustermann", "score": 0},
      {"rank": 20, "userAlias": "Max Mustermann", "score": 0},
      {"rank": 21, "userAlias": "Max Mustermann", "score": 0},
    ];
  }

  @override
  void didUpdateWidget(AnimatedScoreboard oldWidget) {
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

    return SingleChildScrollView(
      child: Padding(
        padding: const EdgeInsets.all(10.0),
        child: Card(
          borderOnForeground: true,
          color: Colors.white,
          shadowColor: Colors.grey[100],
          elevation: 4.0,
          child: Padding(
            padding: const EdgeInsets.all(10.0),
            child: Column(
              children: [
                const Padding(
                  padding: EdgeInsets.all(5.0),
                  child: SizedBox(
                    height: 30.0, // Set the height of the card
                    child: Row(
                      children: [
                        Expanded(
                          flex: 1,
                          child: Align(
                            alignment: Alignment.centerLeft,
                            child: Padding(
                              padding: EdgeInsets.only(left: 20.0),
                              child: FittedBox(
                                fit: BoxFit.none,
                                child: Text(
                                  'Platzierung',
                                  style: TextStyle(
                                      fontWeight: FontWeight.bold,
                                      fontSize: 20),
                                ),
                              ),
                            ),
                          ),
                        ),
                        Expanded(
                          flex: 2,
                          child: Center(
                            child: Text('Name',
                                style: TextStyle(
                                    fontWeight: FontWeight.bold, fontSize: 20)),
                          ),
                        ),
                        Expanded(
                          flex: 1,
                          child: Align(
                            alignment: Alignment.centerRight,
                            child: Padding(
                              padding: EdgeInsets.only(right: 20.0),
                              child: Text('Punkte',
                                  style: TextStyle(
                                      fontWeight: FontWeight.bold,
                                      fontSize: 20)),
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
                const Divider(),
                ListView.separated(
                  shrinkWrap: true,
                  itemCount: _scoreboard.length,
                  separatorBuilder: (BuildContext context, int index) =>
                      const Divider(),
                  itemBuilder: (BuildContext context, int index) {
                    return Padding(
                      padding: const EdgeInsets.all(5.0),
                      child: SizedBox(
                        height: 25.0, // Set the height of the card
                        child: Row(
                          children: [
                            Expanded(
                              flex: 1,
                              child: Align(
                                alignment: Alignment.centerLeft,
                                child: Padding(
                                  padding: const EdgeInsets.only(left: 20.0),
                                  child: Text(
                                    _scoreboard[index]["rank"].toString(),
                                    style: const TextStyle(
                                        fontWeight: FontWeight.bold,
                                        fontSize: 20),
                                  ),
                                ),
                              ),
                            ),
                            Expanded(
                              flex: 2,
                              child: Center(
                                child: Text(_scoreboard[index]["userAlias"]),
                              ),
                            ),
                            Expanded(
                              flex: 1,
                              child: Align(
                                alignment: Alignment.centerRight,
                                child: Padding(
                                  padding: const EdgeInsets.only(right: 20.0),
                                  child: Text(
                                    _scoreboard[index]["score"].toString(),
                                    style: const TextStyle(
                                        fontWeight: FontWeight.bold,
                                        fontSize: 20),
                                  ),
                                ),
                              ),
                            ),
                          ],
                        ),
                      ),
                    );
                  },
                )
              ],
            ),
          ),
        ),
      ),
    );
  }
}
