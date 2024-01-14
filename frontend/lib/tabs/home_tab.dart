import 'package:flutter/material.dart';
import 'package:frontend/components/dashboard_card.dart';
import 'package:frontend/theme/assets.dart';

Widget _buildColumn(String title, String value) {
  return Column(
    children: [
      SizedBox(height: 20),
      Text(
        title,
        style: const TextStyle(
          fontSize: 15,
          color: Colors.white,
        ),
        textAlign: TextAlign.center,
      ),
      Text(
        value,
        style: const TextStyle(fontSize: 30, color: Colors.grey),
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
    double screenWidth = MediaQuery.of(context).size.width;

    if (loading) {
      return const Center(
        child: CircularProgressIndicator(),
      );
    }

    return Scaffold(
      body: SafeArea(
        child: SingleChildScrollView(
          child: Column(
            children: [
              GridView.count(
                crossAxisCount: 2,
                crossAxisSpacing: 10,
                children: [
                  MyCard(
                      title: 'Events',
                      cardColor: Colors.grey,
                      imageName: events),
                  MyCard(
                      title: 'Mensa',
                      cardColor: Colors.yellow,
                      imageName: mensa),
                  MyCard(
                    title: 'LSF',
                    cardColor: Colors.orange,
                    imageName: calendar,
                  ),
                  MyCard(
                    title: 'Noten',
                    cardColor: Colors.green,
                    imageName: analytics,
                  ),
                ],
                shrinkWrap: true,
              ),
              Container(
                decoration: BoxDecoration(
                    color: colors.inverseSurface,
                    borderRadius: const BorderRadius.only(
                        topLeft: Radius.circular(20),
                        topRight: Radius.circular(20))),
                width: screenWidth,
                height: 200,
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                      children: [
                        _buildColumn('Gegebene Feedbacks', '12'),
                        _buildColumn('Absolvierte Quizze', '55'),
                      ],
                    ),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                      children: [
                        _buildColumn('Ø Quiz-Position', '4'),
                        _buildColumn('Alle Feedbacks ', '1923'),
                      ],
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
