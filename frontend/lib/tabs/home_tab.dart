import 'package:flutter/material.dart';
import 'package:frontend/theme/assets.dart';

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

    if (loading) {
      return const Scaffold(
        body: Center(
          child: CircularProgressIndicator(),
        ),
      );
    }

    return Scaffold(
      body: Center(
        child: SizedBox(
          height: 300.0,
          child: Center(
            child: Image.asset(htwgLogo),
          ),
        ),
      ),
    );
  }
}
// 