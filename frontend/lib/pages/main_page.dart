import 'package:flutter/material.dart';
import 'package:frontend/auth_state.dart';
import 'package:frontend/tabs/courses_tab.dart';
import 'package:frontend/tabs/live_tab.dart';
import 'package:frontend/tabs/home_tab.dart';
import 'package:frontend/theme/assets.dart';

class MainPage extends StatefulWidget {
  const MainPage({super.key});

  @override
  State<MainPage> createState() => _MainPageState();
}

class _MainPageState extends AuthState<MainPage> {
  int _tabIndex = 0;
  Function? popFunction;

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
      resizeToAvoidBottomInset: false,
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.primary,
        title: Image.asset(htwgWhiteExtendedLogo, height: 50),
        centerTitle: true,
        leading: popFunction != null ? IconButton(
          icon: Icon(Icons.arrow_back, color: colors.onBackground),
          onPressed: () {
            popFunction!();
          },
        ) : null,
        actions: [
          Padding(
            padding: const EdgeInsets.all(8.0),
            child: IconButton(
              icon: const Icon(Icons.person, size: 30, color: Colors.black),
              onPressed: () {
                Navigator.pushNamed(context, '/profile');
              },
            ),
          )
        ]
      ),
      body: SafeArea(
        child: <Widget>[
          const HomeTab(title: "HTWG App"),
          CoursesTab(setPopFunction: (popFunction) {
            setState(() {
              this.popFunction = popFunction;
            });
          }),
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
            if (_tabIndex == index) return;
            setState(() {
              popFunction = null;
              _tabIndex = index;
            });
          },
          selectedIndex: _tabIndex),
    );
  }
}
