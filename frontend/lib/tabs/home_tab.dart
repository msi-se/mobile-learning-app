import 'package:flutter/material.dart';
import 'package:frontend/components/dashboard_card.dart';
import 'package:frontend/components/dashboard_statistics.dart';
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

class _HomeTabState extends State<HomeTab> with TickerProviderStateMixin {
  bool _loading = true;
  bool _isSmallPhone = false;
  bool _isTablet = false;

  @override
  void initState() {
    super.initState();
    setState(() {
      _loading = false;
    });
    
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
}
