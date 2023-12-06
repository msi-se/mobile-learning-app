import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:frontend/theme/assets.dart';
import 'package:frontend/utils.dart';
import 'package:http/http.dart' as http;

class HomeTab extends StatefulWidget {
  const HomeTab({super.key, required this.title});

  final String title;

  @override
  State<HomeTab> createState() => _HomeTabState();
}

class _HomeTabState extends State<HomeTab> {
  int _counter = 0;
  String _testData = "";

  @override
  void initState() {
    super.initState();
    // fetch test data from the backend
    Future fetchTestData() async {
      try {
        final response = await http.get(Uri.parse("${getBackendUrl()}/hello"));
        if (response.statusCode == 200) {
          setState(() {
            _testData = response.body; // response.body is a String
          });
        }
      } on SocketException catch (_) {
        setState(() {
          _testData = "Backend not reachable";
        });
      }
    }

    fetchTestData();
  }

  void _incrementCounter() {
    setState(() {
      _counter++;
    });
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;

    return Scaffold(
      extendBodyBehindAppBar: true,
      appBar: AppBar(
        backgroundColor: colors.background.withOpacity(0),
        elevation: 0,
        leading: IconButton(
          onPressed: () {},
          icon: SvgPicture.asset(
            hamburger,
            alignment: Alignment.center,
            width: 24,
          ),
          color: colors.primary,
        ),
      ),
      body: Stack(
        children: <Widget>[
          SvgPicture.asset(
            onboardingLight,
            alignment: Alignment.center,
            fit: BoxFit.cover,
          ),
          Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: <Widget>[
                Container(height: 130),
                const Text(
                  'You have pushed the button this many times:',
                ),
                Text(
                  '$_counter',
                  style: Theme.of(context).textTheme.headlineMedium,
                ),
                Text(
                  _testData,
                  style: Theme.of(context).textTheme.headlineMedium,
                ),
              ],
            ),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: _incrementCounter,
        tooltip: 'Increment',
        child: const Icon(Icons.add),
      ),
    );
  }
}
