import 'package:flutter/material.dart';
import 'package:frontend/components/dashboard_card.dart';
import 'package:frontend/components/dashboard_statistics.dart';
import 'package:frontend/theme/assets.dart';

class DashboardItem {
  final String page;
  final String svgImage;
  final String text;
  final bool enabled;

  DashboardItem({
    required this.page,
    required this.svgImage,
    required this.text,
    required this.enabled,
  });
}

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

class _HomeTabState extends State<HomeTab> with TickerProviderStateMixin {
  bool _loading = true;

  @override
  void initState() {
    super.initState();
    setState(() {
      _loading = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    final dashboardItems = [
      DashboardItem(
          page: '/test', svgImage: events, text: 'Events', enabled: false),
      DashboardItem(
          page: '/menu', svgImage: mensa, text: 'Mensa', enabled: true),
      DashboardItem(
          page: '/test', svgImage: calendar, text: 'LSF', enabled: false),
      DashboardItem(
          page: '/test', svgImage: analytics, text: 'Noten', enabled: false),
    ];

    var screenWidth = MediaQuery.of(context).size.width;
    int crossAxisCount = screenWidth > 800 ? 4 : 2;

    if (_loading) {
      return const Center(
        child: CircularProgressIndicator(),
      );
    }
    return Scaffold(
      body: SafeArea(
        child: SingleChildScrollView(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Padding(
                padding: const EdgeInsets.all(16),
                child: GridView.builder(
                  gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
                    crossAxisCount: crossAxisCount,
                    crossAxisSpacing: 10,
                    mainAxisSpacing: 10,
                    childAspectRatio: 1,
                  ),
                  itemCount: dashboardItems.length,
                  primary: false,
                  shrinkWrap: true,
                  itemBuilder: (context, index) {
                    var dashboardItem = dashboardItems[index];
                    return DashboardCard(
                      svgImage: dashboardItem.svgImage,
                      text: dashboardItem.text,
                      onTap: () => Navigator.pushNamed(context, dashboardItem.page),
                      enabled: dashboardItem.enabled,
                    );
                  },
                ),
              ),
              const Padding(
                  padding: EdgeInsets.all(16),
                  child: DashboardStatisticsWidget()),
            ],
          ),
        ),
      ),
    );
  }
}
