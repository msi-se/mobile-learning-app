import 'package:flutter/material.dart';
import 'package:frontend/components/basicButton.dart';
import 'package:frontend/global.dart';
import 'package:http/http.dart' as http;
import 'package:frontend/utils.dart';

typedef AliasSubmittedCallback = void Function(String alias);

class ChooseAlias extends StatefulWidget {
  final AliasSubmittedCallback onAliasSubmitted;

  const ChooseAlias({Key? key, required this.onAliasSubmitted})
      : super(key: key);

  @override
  State<ChooseAlias> createState() => _ChooseAliasState();
}

class _ChooseAliasState extends State<ChooseAlias> {
  final TextEditingController _aliasController = TextEditingController();

  @override
  void initState() {
    super.initState();
    _getRandomAlias();
  }

  Future<void> _getRandomAlias() async {
    try {
      final response =
          await http.get(Uri.parse('${getBackendUrl()}/funnyalias/'));
      if (response.statusCode == 200) {
        setState(() {
          _aliasController.text = response.body;
        });
      } else {
        print('Failed to load random alias');
      }
    } catch (e) {
      print('Error occurred while fetching random alias: $e');
    }
  }

  void _onShufflePressed() {
    // Call the random alias fetching function again
    _getRandomAlias();
  }

  void _onRealNamePressed() {
    // get the real name of the user from the session
    // and set it as the alias
    _aliasController.text = getSession()!.fullName;
  }

  void _onSubmit() {
    if (_aliasController.text.isNotEmpty) {
      widget.onAliasSubmitted(_aliasController.text);
    }
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;

    return Center(
      child: Card(
        margin: EdgeInsets.all(16.0),
        child: Padding(
          padding: EdgeInsets.symmetric(vertical: 32.0, horizontal: 20.0),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: <Widget>[
              Text(
                'Wie ist dein Nickname?',
                style: Theme.of(context).textTheme.titleLarge,
              ),
              const SizedBox(height: 40),
              Container(
                decoration: BoxDecoration(
                  border: Border.all(
                    color: colors.onSurface.withOpacity(0.5),
                  ),
                  borderRadius: BorderRadius.circular(32),
                ),
                child: Row(
                  children: [
                    Expanded(
                      child: TextFormField(
                        controller: _aliasController,
                        decoration: const InputDecoration(
                          hintText: 'Your nickname',
                          border: InputBorder.none,
                          contentPadding: EdgeInsets.symmetric(horizontal: 20),
                        ),
                      ),
                    ),
                    SizedBox(
                      width: 50,
                      height: 50,
                      child: ElevatedButton(
                        onPressed: () {
                          _onSubmit();
                        },
                        style: ElevatedButton.styleFrom(
                          padding: EdgeInsets.zero,
                          shape: const CircleBorder(),
                          fixedSize: const Size(50, 50),
                          backgroundColor: colors.primary,
                        ),
                        child: Icon(
                          Icons.arrow_forward,
                          color: colors.background,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 20),
              BasicButton(
                type: ButtonType.primary,
                text: "Zufälliger Alias",
                onPressed: _onShufflePressed,
              ),
              const SizedBox(height: 20),
              BasicButton(
                type: ButtonType.primary,
                text: "Nimm meinen richtigen Namen",
                onPressed: _onRealNamePressed,
              ),
            ],
          ),
        ),
      ),
    );
  }
}
