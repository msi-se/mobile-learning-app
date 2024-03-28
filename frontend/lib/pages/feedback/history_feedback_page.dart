import 'package:flutter/material.dart';
import 'package:frontend/auth_state.dart';

class HistoryFeedbackPage extends StatefulWidget {
  const HistoryFeedbackPage({super.key});

  @override
  _HistoryFeedbackPageState createState() => _HistoryFeedbackPageState();
}

class _HistoryFeedbackPageState extends AuthState<HistoryFeedbackPage> {
  final List<FeedbackItem> _feedbackItems = [
    FeedbackItem(text: 'Feedback AUME 23/24', label: '200'),
    FeedbackItem(text: 'Feedback DIMA 23/24', label: '15'),
    FeedbackItem(text: 'Feedback Teamprojekt 21/22', label: '5'),
  ];


  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.primary,
        title: const Text(
          "Feedback History", 
          style: TextStyle(
            color: Colors.white, 
            fontWeight: FontWeight.bold
          )
        ),
        leading: IconButton(
          icon: Icon(Icons.arrow_back),
          onPressed: () {
            Navigator.pushNamed(context, '/main');
          },
        ),
      ),
      body: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: <Widget>[
          Expanded(
            child: ListView.builder(
              itemCount: _feedbackItems.length,
              itemBuilder: (context, index) {
                final feedbackItem = _feedbackItems[index];
                return ListTile(
                  title: Text(feedbackItem.text),
                  trailing: Container(
                    padding: const EdgeInsets.all(8.0),
                    decoration: BoxDecoration(
                      shape: BoxShape.circle,
                      color: colors.primary,
                    ),
                    child: Text(
                      feedbackItem.label,
                      style: const TextStyle(color: Colors.white),
                    ),
                  ),
                  onTap: () {
                    // Navigate to the feedback detail page
                  },
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}

class FeedbackItem {
  final String text;
  final String label;

  FeedbackItem({required this.text, required this.label});
}
