import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:frontend/auth_state.dart';
import 'package:intl/intl.dart';
import 'package:frontend/components/menu_card.dart';
import 'package:frontend/global.dart';
import 'package:frontend/models/menu/menuState.dart';
import 'package:frontend/models/menu/day.dart';
import 'package:frontend/utils.dart';
import 'package:http/http.dart' as http;

class MenuPage extends StatefulWidget {
  const MenuPage({super.key});

  @override
  State<MenuPage> createState() => _MenuPageState();
}

class _MenuPageState extends AuthState<MenuPage> with TickerProviderStateMixin {
  late Future<MenuState> menuStateFuture;
  TabController? _tabController;
  int? initialTabIndex;

  List<String> iconsVegan = ['24'];
  List<String> iconsPescetarian = ['50'];
  List<String> iconsVegetarian = ['51'];
  List<String> iconsNotVegetarian = ['45', '46', '49', '23'];
  List<String> iconsToTest = [];

  @override
  void initState() {
    super.initState();
    menuStateFuture = fetchMenuState();
  }

  Future<MenuState> fetchMenuState() async {
    final response = await http.get(
      Uri.parse("${getBackendUrl()}/external/menu"),
      headers: {
        "Content-Type": "application/json",
        "Authorization": "Bearer ${getSession()!.jwt}",
      },
    );

    if (response.statusCode == 200) {
      final menuState = MenuState.fromJson(json.decode(response.body));
      initialTabIndex = _findIndexOfCurrentDay(menuState.menu.days);
      _initTabController(menuState.menu.days.length, initialTabIndex ?? 0);
      return menuState;
    } else {
      throw Exception('Failed to load menu');
    }
  }

  int _findIndexOfCurrentDay(List<Day> days) {
    final now = DateTime.now();
    DateTime adjustedNow = now;
    if (now.weekday == DateTime.saturday) {
      adjustedNow = now.add(const Duration(days: 2));
    } else if (now.weekday == DateTime.sunday) {
      adjustedNow = now.add(const Duration(days: 1));
    }

    for (int i = 0; i < days.length; i++) {
      final dayDate = DateTime.fromMillisecondsSinceEpoch(
          int.parse(days[i].timestamp) * 1000);
      if (DateFormat('yyyy-MM-dd').format(adjustedNow) ==
          DateFormat('yyyy-MM-dd').format(dayDate)) {
        return i;
      }
    }
    return 0;
  }

  void _initTabController(int length, int initialIndex) {
    _tabController =
        TabController(length: length, vsync: this, initialIndex: initialIndex);
  }

  Color getColorBasedOnIcon(String icon) {
    iconsToTest = icon.split(',');
    print(iconsToTest);
    for (var i in iconsToTest) {
      if (iconsVegan.contains(i)) {
        return Colors.green;
      } else if (iconsPescetarian.contains(i)) {
        return Colors.blue;
      } else if (iconsVegetarian.contains(i)) {
        return Colors.orange;
      } else if (iconsNotVegetarian.contains(i)) {
        return Colors.red;
      }
      return Theme.of(context).colorScheme.onPrimary;
    }
    return Theme.of(context).colorScheme.onPrimary;
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    return Scaffold(
      appBar: AppBar(
        title: const Text("Mensa Speiseplan",
            style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
        centerTitle: true,
        backgroundColor: Theme.of(context).colorScheme.primary,
      ),
      body: FutureBuilder<MenuState>(
        future: menuStateFuture,
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.done &&
              snapshot.hasData) {
            if (_tabController == null ||
                _tabController!.length != snapshot.data!.menu.days.length) {
              setState(() {
                initialTabIndex =
                    _findIndexOfCurrentDay(snapshot.data!.menu.days);
                _tabController = TabController(
                    vsync: this,
                    length: snapshot.data!.menu.days.length,
                    initialIndex: initialTabIndex!);
              });
            }
            return Column(
              children: [
                TabBar(
                  controller: _tabController,
                  isScrollable: true,
                  tabs: snapshot.data!.menu.days.map<Tab>((day) {
                    return Tab(text: day.formattedDate);
                  }).toList(),
                  indicatorColor: Theme.of(context).colorScheme.primary,
                  labelColor: Colors.black,
                ),
                Container(
                  padding: EdgeInsets.all(8.0),
                  child: const Row(
                    mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                    children: [
                      LegendItem(
                        color: Colors.green,
                        description: 'Vegan',
                      ),
                      LegendItem(
                        color: Colors.orange,
                        description: 'Vegetarisch',
                      ),
                      LegendItem(
                        color: Colors.red,
                        description: 'Nicht vegetarisch',
                      ),
                      LegendItem(
                        color: Colors.blue,
                        description: 'Pesketarisch',
                      ),
                    ],
                  ),
                ),
                Expanded(
                  child: TabBarView(
                    controller: _tabController,
                    children: snapshot.data!.menu.days.map<Widget>((day) {
                      return ListView.builder(
                        itemCount: day.items.length,
                        itemBuilder: (context, index) {
                          final item = day.items[index];
                          return MenuCard(
                            cardColor: getColorBasedOnIcon(item.icons),
                            title: item.category,
                            description: item.title,
                            rowData: [
                              RowData(
                                  label: 'Studierende:', value: item.preis1),
                              RowData(
                                  label: 'Mitarbeiter:', value: item.preis2),
                              RowData(label: 'Gäste:', value: item.preis3),
                            ],
                          );
                        },
                      );
                    }).toList(),
                  ),
                ),
              ],
            );
          } else if (snapshot.hasError) {
            return Center(child: Text("Error: ${snapshot.error}"));
          } else {
            return const Center(child: CircularProgressIndicator());
          }
        },
      ),
    );
  }

  @override
  void dispose() {
    _tabController?.dispose();
    super.dispose();
  }
}

class LegendItem extends StatelessWidget {
  final Color color;
  final String description;

  const LegendItem({
    required this.color,
    required this.description,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Container(
          width: 20,
          height: 20,
          color: color,
        ),
        SizedBox(width: 5),
        Text(
          description,
          style: TextStyle(fontSize: 10),
        ),
        SizedBox(width: 15),
      ],
    );
  }
}
