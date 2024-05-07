import 'package:flutter/material.dart';

class MenuCard extends StatefulWidget {
  final String title;
  final String description;
  final List<RowData> rowData;
  final Color cardColor;

  const MenuCard({
    Key? key,
    required this.title,
    required this.description,
    required this.rowData,
    required this.cardColor,
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
          color: widget.cardColor,
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
                            ? Icons.texture
                            : widget.title == 'KombinierBar'
                                ? Icons.join_full
                                : widget.title == 'Pasta'
                                    ? Icons.rice_bowl
                                    : widget.title == 'Pasta vegetarisch'
                                        ? Icons.rice_bowl
                                        : widget.title == 'Sättigung I'
                                            ? Icons.fastfood
                                            : widget.title == 'Sättigung II'
                                                ? Icons.fastfood
                                                : widget.title == 'Gemüse I'
                                                    ? Icons.restaurant_menu
                                                    : widget.title == 'Salat I'
                                                        ? Icons.restaurant_menu
                                                        : widget.title ==
                                                                'Salat II'
                                                            ? Icons
                                                                .restaurant_menu
                                                            : widget.title ==
                                                                    'Dessert I'
                                                                ? Icons
                                                                    .restaurant_menu
                                                                : Icons
                                                                    .no_meals_rounded, // Default icon
                  ),
                  title: Padding(
                    padding: const EdgeInsets.all(5),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text(
                          widget.title,
                          style: const TextStyle(
                            fontSize: 20,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        Icon(Icons.favorite_border),
                      ],
                    ),
                  ),

                  subtitle: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Padding(
                        padding: const EdgeInsets.all(5),
                        child: Text(
                          widget.description,
                          style: TextStyle(fontWeight: FontWeight.w600),
                        ),
                      ),
                      Padding(
                        padding: const EdgeInsets.all(0),
                        child: Row(
                          mainAxisSize: MainAxisSize.max,
                          children: widget.rowData
                              .map((data) => data.build())
                              .toList(),
                        ),
                      ),
                    ],
                  ),
                  // trailing: InkWell(
                  //   onTap: () {
                  //     print("like");
                  //   },
                  //   child: const Icon(Icons.favorite_border),
                  // ),
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
        padding: const EdgeInsets.symmetric(horizontal: 2),
        child: Text(
          '$label $value',
          style: const TextStyle(
            fontSize: 12,
            fontWeight: FontWeight.w500,
          ),
          overflow: TextOverflow.ellipsis,
        ),
      ),
    );
  }
}
