import 'dart:io';
import 'dart:ui';
import 'package:flutter/foundation.dart' show kIsWeb;

import 'package:flutter/material.dart';

class SliverLayout extends StatelessWidget {
  final Widget Function(double) title;
  final Widget? background;
  final Widget body;

  final double expandedTitleScale;
  final double headerHeight;
  final bool collapsable;
  final double navBarHeight;

  const SliverLayout(
      {super.key,
      required this.title,
      this.background,
      required this.body,
      this.headerHeight = 140,
      this.expandedTitleScale = 2,
      this.collapsable = false,
      this.navBarHeight = 80});

  @override
  Widget build(BuildContext context) {
    final bool isIphoneWithHomeBar =
        !kIsWeb && Platform.isIOS && MediaQuery.of(context).size.height >= 812.0;
    int homeBarHeight = 0;
    if (isIphoneWithHomeBar && navBarHeight > 0) {
      homeBarHeight = 34;
    }

    double heightWithoutAppBarNavBar = MediaQuery.of(context).size.height -
        Scaffold.of(context).appBarMaxHeight! -
        (collapsable ? kToolbarHeight : headerHeight) -
        navBarHeight -
        homeBarHeight;

    final colors = Theme.of(context).colorScheme;

    return Scaffold(
      body: CustomScrollView(
        slivers: [
          sliverHeader(colors),
          sliverList(heightWithoutAppBarNavBar, colors),
        ],
      ),
    );
  }

  SliverAppBar sliverHeader(ColorScheme colors) {
    return SliverAppBar(
      surfaceTintColor: colors.background,
      expandedHeight: headerHeight,
      collapsedHeight: collapsable ? kToolbarHeight : headerHeight,
      pinned: true,
      automaticallyImplyLeading: false,
      flexibleSpace: FlexibleSpaceBar(
        expandedTitleScale: expandedTitleScale,
        titlePadding: const EdgeInsetsDirectional.only(start: 0),
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

  SliverList sliverList(double heightWithoutAppBarNavBar, ColorScheme colors) {
    return SliverList(
      delegate: SliverChildListDelegate([
        ConstrainedBox(
          constraints: BoxConstraints(
            minHeight: collapsable
                ? heightWithoutAppBarNavBar
                : heightWithoutAppBarNavBar - headerHeight + kToolbarHeight,
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
