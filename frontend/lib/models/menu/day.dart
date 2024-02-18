import 'package:intl/intl.dart';
import 'package:frontend/models/menu/item.dart';


class Day {
  final List<Item> items;
  final String timestamp;

  Day({required this.items, required this.timestamp});

  factory Day.fromJson(Map<String, dynamic> json) {
    return Day(
      items: List<Item>.from(json['item'].map((item) => Item.fromJson(item))),
      timestamp: json['timestamp'],
    );
  }

  String get formattedDate {
    final date = DateTime.fromMillisecondsSinceEpoch(int.parse(timestamp) * 1000);
    return DateFormat('EEE, d MMM, ''yy').format(date);
  }
}
