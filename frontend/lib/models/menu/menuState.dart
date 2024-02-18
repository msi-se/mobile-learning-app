import 'package:frontend/models/menu/menu.dart';

class MenuState {
  final DateTime timestamp;
  final Menu menu;

  MenuState({required this.timestamp, required this.menu});

  factory MenuState.fromJson(Map<String, dynamic> json) {
    return MenuState(
      timestamp: DateTime.parse(json['timestamp']),
      menu: Menu.fromJson(json['menu']),
    );
  }
}
