import 'dart:ui';

import 'package:flutter/material.dart';

class SliverLayout extends StatelessWidget {
  final Widget Function(double) title;
  final Widget background;
  final Widget body;

  final double expandedTitleScale;
  final double headerHeight;

  const SliverLayout(
      {super.key,
      required this.title,
      required this.background,
      required this.body,
      this.headerHeight = 140,
      this.expandedTitleScale = 2});

  @override
  Widget build(BuildContext context) {
    double heightWithoutappBarNavBar = MediaQuery.of(context).size.height -
        (kBottomNavigationBarHeight + Scaffold.of(context).appBarMaxHeight!);

    final colors = Theme.of(context).colorScheme;

    return Scaffold(
      body: CustomScrollView(
        slivers: [
          sliverHeader(colors),
          sliverList(heightWithoutappBarNavBar, colors),
        ],
      ),
    );
  }

  SliverAppBar sliverHeader(ColorScheme colors) {
    return SliverAppBar(
      surfaceTintColor: colors.background,
      expandedHeight: headerHeight,
      pinned: true,
      flexibleSpace: FlexibleSpaceBar(
        expandedTitleScale: expandedTitleScale,
        titlePadding: const EdgeInsetsDirectional.only(start: 16),
        title: LayoutBuilder(
          builder: (context, constraints) {
            double percentage = ((constraints.biggest.height - kToolbarHeight) /
                    (headerHeight - kToolbarHeight))
                .clamp(0.0, 1.0);
            return title(percentage);
          },
        ),
        background: background,
      ),
    );
  }

  SliverList sliverList(double heightWithoutappBarNavBar, ColorScheme colors) {
    return SliverList(
      delegate: SliverChildListDelegate([
        ConstrainedBox(
          constraints: BoxConstraints(
            minHeight: heightWithoutappBarNavBar - 80,
          ),
          child: Card(
            shape: const RoundedRectangleBorder(
              borderRadius: BorderRadius.only(
                  topLeft: Radius.circular(25), topRight: Radius.circular(25)),
            ),
            color: colors.background,
            margin: const EdgeInsets.only(top: 0),
            child: body,
          ),
        ),
      ]),
    );
  }
}
