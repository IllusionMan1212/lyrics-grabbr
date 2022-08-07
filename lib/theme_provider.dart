import 'package:flutter/material.dart';

class ThemeProvider extends ChangeNotifier {
  ThemeMode themeMode;

  ThemeProvider(this.themeMode);

  bool get isDarkMode => themeMode == ThemeMode.dark;

  void changeTheme(ThemeMode mode) {
    themeMode = mode;

    notifyListeners();
  }
}

class MyThemes {
  static final darkTheme = ThemeData(
    primarySwatch: Colors.deepPurple,
    dialogTheme: const DialogTheme(backgroundColor: Color(0xFF202020)),
    unselectedWidgetColor: Colors.white,
    canvasColor: Colors.black,
    bottomSheetTheme: const BottomSheetThemeData(backgroundColor: Color(0xFF202020)),
    dividerColor: Colors.white,
    iconTheme: const IconThemeData(color: Colors.white),
    textTheme: const TextTheme(
        bodyText1: TextStyle(color: Colors.white, fontSize: 16),
        bodyText2: TextStyle(color: Colors.white),
        subtitle1: TextStyle(color: Colors.grey),
        subtitle2: TextStyle(color: Colors.white),
        headline6: TextStyle(color: Colors.white),
    ),
  );

  static final lightTheme = ThemeData(
    primarySwatch: Colors.purple,
    dividerColor: Colors.black,
    textTheme: const TextTheme(
        bodyText1: TextStyle(fontSize: 16),
        subtitle1: TextStyle(color: Colors.black54),
    ),
  );
}
