import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:frontend/theme/assets.dart';
import 'package:frontend/components/dashboard_statistics.dart';
import 'package:frontend/components/dashboard_card.dart';

class TestPage extends StatefulWidget {
  const TestPage({super.key});

  @override
  State<TestPage> createState() => _TestPageState();
}

class _TestPageState extends State<TestPage> with TickerProviderStateMixin {
  late AnimationController _controller;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      duration: const Duration(milliseconds: 600),
      vsync: this,
    )..forward();
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final pages = ['/test', '/menu', '/test', '/test'];
    final svgImages = [
      events,
      mensa,
      calendar,
      analytics,
    ];
    final texts = ['Events', 'Mensa', 'LSF', 'Noten'];

    return Scaffold(
      appBar: AppBar(
        title: const Text("Test Seite",
            style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
        centerTitle: true,
        backgroundColor: colors.primary,
      ),
      body: SafeArea(
        child: SingleChildScrollView(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Padding(
                padding: const EdgeInsets.all(16),
                child: GridView.builder(
                  gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                    crossAxisCount: 2,
                    crossAxisSpacing: 10,
                    mainAxisSpacing: 10,
                    childAspectRatio: 1,
                  ),
                  itemCount: 4,
                  primary: false,
                  shrinkWrap: true,
                  itemBuilder: (context, index) {
                    return DashboardCard(
                      svgImage: svgImages[index],
                      text: texts[index],
                      onTap: () => Navigator.pushNamed(context, pages[index]),
                    );
                  },
                ),
              ),
              const Padding(
                padding: EdgeInsets.all(16), 
                child: DashboardStatisticsWidget()
              ),
            ],
          ),
        ),
      ),
    );
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }
}
