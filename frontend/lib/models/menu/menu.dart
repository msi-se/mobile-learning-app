import 'package:frontend/models/menu/day.dart';

class Menu {
  final List<Day> days;

  Menu({required this.days});

  factory Menu.fromJson(Map<String, dynamic> json) {
    var daysFromJson = json['tag'] as List;
    List<Day> daysList = daysFromJson.map((day) => Day.fromJson(day)).toList();
    return Menu(days: daysList);
  }
}

