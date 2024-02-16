import 'package:flutter/material.dart';
import 'package:frontend/theme/assets.dart';
import 'package:flutter_svg/flutter_svg.dart';

class MyCard extends StatelessWidget {
  final Color cardColor;
  final String title;
  final String imageName;

  const MyCard({
    Key? key,
    required this.title,
    required this.cardColor,
    required this.imageName,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: () {
        // Add functionality for on click here (redirect to other pages)
        // redirectPath
      },
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 15, vertical: 15),
        child: Container(
            margin: const EdgeInsets.all(10),
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(25),
              color: cardColor,
              boxShadow: const [
                BoxShadow(
                  color: Colors.grey,
                  spreadRadius: 1,
                  blurRadius: 3,
                )
              ],
            ),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Text(
                  title,
                  style: const TextStyle(
                    fontSize: 20,
                    fontWeight: FontWeight.bold,
                    color: Colors.black,
                  ),
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 5),
                Align(
                    alignment: Alignment.center,
                    child: SvgPicture.asset(
                      imageName,
                      height: 80,
                      width: 80,
                    ))
              ],
            )),
      ),
    );
  }
}
