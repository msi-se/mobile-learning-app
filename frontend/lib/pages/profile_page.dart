import 'package:flutter/material.dart';
import 'package:frontend/auth_state.dart';
import 'package:frontend/components/basicButton.dart';
import 'package:frontend/pages/privacy_page.dart';
import 'package:frontend/global.dart';

class ProfilePage extends StatefulWidget {
  const ProfilePage({super.key});

  @override
  State<ProfilePage> createState() => _ProfilePage();
}

class _ProfilePage extends AuthState<ProfilePage> {
  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;

    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.primary,
        title: const Text("Profilseite"),
      ),
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.only(top: 150.0),
          child: Center(
            child: Column(
              children: <Widget>[
                // const CircleAvatar(
                //   radius: 50,
                //   backgroundImage: NetworkImage('https://via.placeholder.com/150'),
                // ),
                Icon(Icons.account_circle, size: 100, color: colors.primary),
                Text(
                  getSession()!.fullName,
                  style: const TextStyle(
                    fontSize: 30,
                    color: Colors.black,
                    fontWeight: FontWeight.w500,
                  ),
                ),
                Text(
                  getSession()!.username,
                  style: const TextStyle(
                    fontSize: 20,
                    color: Colors.black54,
                    fontWeight: FontWeight.w500,
                  ),
                ),
                const SizedBox(height: 10),
                TextButton(
                  onPressed: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(builder: (context) => PrivacyPage()),
                    );
                  },
                  child: const Text(
                    'Datenschutzerklärung',
                    style: TextStyle(fontSize: 16),
                  ),
                ),
                const SizedBox(height: 10),
                BasicButton(
                  type: ButtonType.cancel,
                  text: "Logout",
                  onPressed: () {
                    clearSession();
                    Navigator.pushReplacementNamed(context, "/login");
                  },
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
