import 'package:flutter/material.dart';
import 'package:frontend/tabs/courses_tab.dart';
import 'package:frontend/tabs/live_tab.dart';
import 'package:frontend/tabs/home_tab.dart';

class MainPage extends StatefulWidget {
  const MainPage({super.key});

  @override
  State<MainPage> createState() => _MainPageState();
}

class _MainPageState extends State<MainPage> {
  int _tabIndex = 0;

  @override
  void initState() {
    super.initState();

    setState(() {
      _tabIndex = 0;
    });
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;

    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.primary,
        title: const Text(
          "HTWG App", 
          style: TextStyle(
            color: Colors.white, 
            fontWeight: FontWeight.bold
          )
        ),
      ),
      body: SafeArea(
        child: <Widget>[
          const HomeTab(title: "HTWG App"),
          const CoursesTab(),
          const LiveTab(),
        ][_tabIndex],
      ),
      bottomNavigationBar: NavigationBar(
          destinations: [
            NavigationDestination(
              icon: Icon(Icons.home, color: colors.secondary),
              label: 'Home',
            ),
            NavigationDestination(
              icon: Icon(Icons.school, color: colors.secondary),
              label: 'Kurse',
            ),
            NavigationDestination(
              icon: Icon(Icons.sensors, color: colors.secondary),
              label: 'Live',
            ),
          ],
          onDestinationSelected: (index) {
            setState(() {
              _tabIndex = index;
            });
          },
          selectedIndex: _tabIndex),
    );
  }
}
