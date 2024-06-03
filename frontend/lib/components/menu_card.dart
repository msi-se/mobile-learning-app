import 'package:flutter/material.dart';
import 'package:flutter_svg/svg.dart';
import 'package:frontend/theme/assets.dart';
import 'package:rive/rive.dart';

class MenuCard extends StatefulWidget {
  final String title;
  final String description;
  final List<RowData> rowData;
  final String cardIcon;
  final RiveFile? riveFile;

  const MenuCard({
    Key? key,
    required this.title,
    required this.description,
    required this.rowData,
    required this.cardIcon,
    required this.riveFile,
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
    double iconWidth = widget.cardIcon == 'pescetarian' ? 10 : 25;
    double iconHeight = widget.cardIcon == 'pescetarian' ? 10 : 25;
    return FadeTransition(
      opacity: _opacityAnimation,
      child: Padding(
        padding: const EdgeInsets.all(2),
        child: Card(
          // Replace Container with Card
          color: Colors.white,
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
                  leading: Container(
                    width: 100,
                    height: 100,
                    child: RiveAnimation.direct(
                      widget.riveFile!,
                      fit: BoxFit.cover,
                      artboard: widget.title == 'Seezeit-Teller'
                          ? 'Seezeit'
                          : widget.title == 'hin&weg'
                              ? 'hin&weg'
                              : widget.title == 'KombinierBar'
                                  ? 'Kombinierbar'
                                  : 'Default',
                      stateMachines: ['State Machine'],
                    ),
                  ),
                  title: Padding(
                    padding: const EdgeInsets.all(5),
                    child: Wrap(
                      alignment: WrapAlignment.spaceBetween,
                      spacing: 40,
                      runSpacing: 20,
                      children: [
                        Text(
                          widget.title,
                          style: const TextStyle(
                            fontSize: 20,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        SvgPicture.asset(widget.cardIcon,
                            width: iconWidth, height: iconHeight),
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
              Padding(
                padding: const EdgeInsets.only(left: 10, right: 10, bottom: 10),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  crossAxisAlignment: CrossAxisAlignment.center,
                  children: widget.rowData.map((data) => data.build()).toList(),
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
        padding: const EdgeInsets.symmetric(horizontal: 5),
        child: Text(
          '$label $value€',
          style: const TextStyle(
            fontSize: 13,
            fontWeight: FontWeight.w500,
          ),
          overflow: TextOverflow.ellipsis,
        ),
      ),
    );
  }
}
