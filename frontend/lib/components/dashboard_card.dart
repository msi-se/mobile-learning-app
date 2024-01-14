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
    //double screenWidth = MediaQuery.of(context).size.width;
    return InkWell(
      onTap: () {
        // Add functionality for on click here (redirect to other pages)
        // redirectPath
      },
      child: Container(
          margin: const EdgeInsets.all(10),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(20),
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
            children: [
              const SizedBox(height: 10),
              Text(
                title,
                style: const TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                  color: Colors.black,
                ),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 20),
              SvgPicture.asset(
                imageName,
                height: 80,
                width: 80,
              ),
            ],
          )),
    );
  }
}
