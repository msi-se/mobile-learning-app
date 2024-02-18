import 'package:flutter/material.dart';
import 'package:frontend/components/dashboard_card.dart';
import 'package:frontend/theme/assets.dart';

Widget _buildColumn(String title, String value, bool smallDevice) {
  return Column(
    children: [
      SizedBox(height: smallDevice ? 10 : 20),
      Text(
        title,
        style: TextStyle(
          fontSize: smallDevice ? 10 : 17,
          color: Colors.black,
        ),
        textAlign: TextAlign.center,
      ),
      Text(
        value,
        style: TextStyle(fontSize: smallDevice ? 15 : 30, color: Colors.white),
        textAlign: TextAlign.center,
      ),
    ],
  );
}

class HomeTab extends StatefulWidget {
  const HomeTab({super.key, required this.title});

  final String title;

  @override
  State<HomeTab> createState() => _HomeTabState();
}

class _HomeTabState extends State<HomeTab> {
  bool loading = true;

  @override
  void initState() {
    super.initState();
    setState(() {
      loading = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    if (loading) {
      return const Center(
        child: CircularProgressIndicator(),
      );
    }
    return Scaffold(
      body: Center(
        child: LayoutBuilder(
          builder: (context, constraints) {
            if (constraints.maxWidth < 600) {
              // mobile version
              final isSmallPhone = constraints.maxHeight < 570;
              return Column(
                children: [               
                  GridView.count(
                    crossAxisCount: 2,
                    crossAxisSpacing: 10,
                    shrinkWrap: true,
                    children: [
                      MyCard(
                          title: 'Events',
                          cardColor: colors.surfaceVariant,
                          imageName: events),
                      MyCard(
                          title: 'Mensa',
                          cardColor: colors.surfaceVariant,
                          imageName: mensa),
                      MyCard(
                        title: 'LSF',
                        cardColor: colors.surfaceVariant,
                        imageName: calendar,
                      ),
                      MyCard(
                        title: 'Noten',
                        cardColor: colors.surfaceVariant,
                        imageName: analytics,
                      ),
                    ],
                  Expanded(
                    flex: isSmallPhone ? 6 : 3,
                    child: GridView.count(
                        crossAxisCount: 2,
                        shrinkWrap: true,
                        children: [
                          MyCard(
                              title: 'Events',
                              cardColor: colors.surfaceVariant,
                              imageName: events),
                          MyCard(
                              title: 'Mensa',
                              cardColor: colors.surfaceVariant,
                              imageName: mensa),
                          MyCard(
                            title: 'LSF',
                            cardColor: colors.surfaceVariant,
                            imageName: calendar,
                          ),
                          MyCard(
                            title: 'Noten',
                            cardColor: colors.surfaceVariant,
                            imageName: analytics,
                          ),
                        ]),
                  ),
                  const SizedBox(height: 10),
                  Expanded(
                    flex: 2,
                    child: Container(
                      decoration: BoxDecoration(
                          color: colors.outlineVariant,
                          borderRadius: const BorderRadius.only(
                              topLeft: Radius.circular(10),
                              topRight: Radius.circular(10))),
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                        children: [
                          Row(
                            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                            children: [
                              _buildColumn(
                                  'Gegebene Feedbacks', '12', isSmallPhone),
                              _buildColumn(
                                  'Absolvierte Quizze', '55', isSmallPhone),
                            ],
                          ),
                          Row(
                            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                            children: [
                              _buildColumn(
                                  'Ø Quiz-Position', '4', isSmallPhone),
                              _buildColumn(
                                  'Alle Feedbacks ', '1923', isSmallPhone),
                            ],
                          ),
                        ],
                      ),
                    ),
                  ),
                ],
              );
            } else {
              //desktop version
              return Row(
                children: [
                  Expanded(
                    flex: 1,
                    child: GridView.count(
                        crossAxisCount: 2,
                        childAspectRatio: 2,
                        children: [
                          MyCard(
                              title: 'Events',
                              cardColor: colors.surfaceVariant,
                              imageName: events),
                          MyCard(
                              title: 'Mensa',
                              cardColor: colors.surfaceVariant,
                              imageName: mensa),
                          MyCard(
                            title: 'LSF',
                            cardColor: colors.surfaceVariant,
                            imageName: calendar,
                          ),
                          MyCard(
                            title: 'Noten',
                            cardColor: colors.surfaceVariant,
                            imageName: analytics,
                          ),
                        ]),
                  ),
                  const SizedBox(width: 40),
                  Expanded(
                    flex: 1,
                    child: Container(
                      decoration: BoxDecoration(
                          color: colors.outlineVariant,
                          borderRadius: const BorderRadius.only(
                              topLeft: Radius.circular(20),
                              bottomLeft: Radius.circular(20))),
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                        children: [
                          Row(
                            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                            children: [
                              _buildColumn('Gegebene Feedbacks', '12', false),
                              _buildColumn('Absolvierte Quizze', '55', false),
                            ],
                          ),
                          Row(
                            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                            children: [
                              _buildColumn('Ø Quiz-Position', '4', false),
                              _buildColumn('Alle Feedbacks ', '1923', false),
                            ],
                          ),
                        ],
                      ),
                    ),
                  ),
                ],
              );
            }
          },
        ),
      ),
    );
  }
}
