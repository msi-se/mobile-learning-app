import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';

class DashboardCard extends StatefulWidget {
  final String svgImage;
  final String text;
  final VoidCallback onTap;
  final bool enabled;

  const DashboardCard({
    Key? key,
    required this.svgImage,
    required this.text,
    required this.onTap,
    required this.enabled,
  }) : super(key: key);

  @override
  State<DashboardCard> createState() => _DashboardCardState();
}

class _DashboardCardState extends State<DashboardCard>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _animation;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      duration: const Duration(milliseconds: 400),
      vsync: this,
    );

    _animation = CurvedAnimation(
      parent: _controller,
      curve: Curves.easeOut,
    );

    _controller.forward();
  }

  void _displayDisabledDialog() {
    showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: const Text('Noch nicht verfügbar'),
          content: const Text('Diese Funktion ist noch in der Entwicklung.'),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(context).pop(),
              child: const Text('OK'),
            ),
          ],
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: widget.enabled ? widget.onTap : _displayDisabledDialog,
      child: ScaleTransition(
        scale: _animation,
        child: Container(
          decoration: BoxDecoration(
            color: widget.enabled ? const Color(0xffd9e5ec) : Colors.grey[300],
            borderRadius: BorderRadius.circular(24),
          ),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              SvgPicture.asset(
                widget.svgImage,
                height: 110,
                width: 110,
                colorFilter: widget.enabled
                    ? null
                    : ColorFilter.mode(Colors.grey[300]!, BlendMode.saturation),
              ),
              const SizedBox(height: 8),
              Text(
                widget.text,
                textAlign: TextAlign.center,
                style: const TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.bold,
                ),
              ),
              if (!widget.enabled)
                const Padding(
                  padding: EdgeInsets.all(8),
                  child: Text(
                    'Noch nicht verfügbar',
                    style: TextStyle(
                      color: Colors.black,
                      fontSize: 12,
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
    _controller.dispose();
    super.dispose();
  }
}
