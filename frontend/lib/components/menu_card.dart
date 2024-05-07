import 'package:flutter/material.dart';

class MenuCard extends StatefulWidget {
  final String title;
  final String description;
  final List<RowData> rowData;

  const MenuCard({
    Key? key,
    required this.title,
    required this.description,
    required this.rowData,
  }) : super(key: key);

  @override
  State<MenuCard> createState() => _MenuCardState();
}

class _MenuCardState extends State<MenuCard>
    with SingleTickerProviderStateMixin {
  late AnimationController _animationController;
  late Animation<double> _opacityAnimation;

  @override
  void initState() {
    super.initState();
    _animationController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 500),
    );
    _opacityAnimation =
        Tween<double>(begin: 0.0, end: 1.0).animate(CurvedAnimation(
      parent: _animationController,
      curve: Curves.easeIn,
    ));
    _animationController.forward();
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    return FadeTransition(
      opacity: _opacityAnimation,
      child: Padding(
        padding: const EdgeInsets.all(2),
        child: Card(
          // Replace Container with Card
          color: colors.onPrimary,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(20),
          ),
          elevation: 2,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Padding(
                padding: const EdgeInsets.all(5),
                child: ListTile(
                  leading: Icon(
                    widget.title == 'Seezeit-Teller'
                        ? Icons.restaurant
                        : widget.title == 'hin&weg'
                            ? Icons.arrow_circle_down
                            : widget.title == 'KombinierBar'
                                ? Icons.bar_chart
                                : widget.title == 'Pasta'
                                    ? Icons.paste
                                    : widget.title == 'Pasta vegetarisch'
                                        ? Icons.paste
                                        : widget.title == 'Sättigung 1'
                                            ? Icons.house
                                            : widget.title == 'Sättigung 2'
                                                ? Icons.house_siding_rounded
                                                : widget.title == 'Gemüse 1'
                                                    ? Icons.view_agenda
                                                    : widget.title == 'Salat 1'
                                                        ? Icons.save_alt_rounded
                                                        : Icons
                                                            .no_meals_rounded, // Default icon
                  ),
                  title: Padding(
                    padding: const EdgeInsets.all(5),
                    child: Text(
                      widget.title,
                      style: const TextStyle(
                        fontSize: 20,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
                  subtitle: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Padding(
                        padding: const EdgeInsets.all(5),
                        child: Text(widget.description),
                      ),
                      Padding(
                        padding: const EdgeInsets.all(5),
                        child: Row(
                          mainAxisSize: MainAxisSize.max,
                          children: widget.rowData
                              .map((data) => data.build())
                              .toList(),
                        ),
                      ),
                    ],
                  ),
                  trailing: InkWell(
                    onTap: () {
                      print("like");
                    },
                    child: const Icon(Icons.favorite_border),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  @override
  void dispose() {
    _animationController.dispose();
    super.dispose();
  }
}

class RowData {
  final String label;
  final String value;

  RowData({
    required this.label,
    required this.value,
  });

  Widget build() {
    return Flexible(
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 4.0),
        child: Text(
          '$label $value',
          style: const TextStyle(
            fontSize: 12,
            fontWeight: FontWeight.w300,
          ),
          overflow: TextOverflow.ellipsis,
        ),
      ),
    );
  }
}
