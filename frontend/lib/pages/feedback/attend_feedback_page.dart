import 'package:flutter/material.dart';
import 'package:frontend/components/feedback/elements/slider_feedback.dart';
import 'package:frontend/components/feedback/elements/star_feedback.dart';

class AttendFeedbackPage extends StatefulWidget {
  const AttendFeedbackPage({super.key});

  @override
  State<AttendFeedbackPage> createState() => _AttendFeedbackPageState();
}

class _AttendFeedbackPageState extends State<AttendFeedbackPage> {
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
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            const Text("Feedbackbogen"),
            StarFeedback(
              rating: 3,
              onRatingChanged: (newRating) {
                print(newRating);
              },
            ),
            SliderFeedback(onFeedbackChanged: (newFeedback) {
              print(newFeedback);
            }),
          ],
        ),
      ),
    );
  }
}
// 