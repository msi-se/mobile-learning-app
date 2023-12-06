import 'package:flutter/material.dart';
import 'package:frontend/tabs/feedback_tab.dart';
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
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: const Text("Mobile App"),
      ),
      body: SafeArea(
        child: <Widget>[
          const HomeTab(title: "Home"),
          const FeedbackTab(),
        ][_tabIndex],
      ),
      bottomNavigationBar: NavigationBar(
        destinations: const [
          NavigationDestination(
            icon: Icon(Icons.home),
            label: 'Home',
          ),
          NavigationDestination(
            icon: Icon(Icons.map),
            label: 'Feedback',
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
