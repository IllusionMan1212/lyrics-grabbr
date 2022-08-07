// ignore_for_file: constant_identifier_names

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'theme_provider.dart';

enum AppTheme {
  Dark,
  Light,
  System
}

class Setting extends StatelessWidget {
  final String title;
  final String? desc;
  final void Function() onTap;

  const Setting({
    Key? key,
    required this.title,
    this.desc,
    required this.onTap,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Material(
      child: InkWell(
        onTap: onTap,
        child: Ink(
          child: Container(
            padding: const EdgeInsets.all(16),
            child: Row(
              children: [
                Expanded(
                  child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(title, style: const TextStyle(fontSize: 16)),
                        if (desc != null)
                          Text(desc!,
                              style: const TextStyle(color: Colors.grey)),
                      ]),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

class SettingsPage extends StatefulWidget {
  const SettingsPage({Key? key}) : super(key: key);

  @override
  State<SettingsPage> createState() => _SettingsPageState();
}

class _SettingsPageState extends State<SettingsPage> {
  final Future<SharedPreferences> _prefs = SharedPreferences.getInstance();
  late SharedPreferences prefs;
  String? activeTheme;

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  Future<void> initPlatformState() async {
    prefs = await _prefs;

    setState(() {
      activeTheme = prefs.getString("theme") ?? AppTheme.System.name;
    });
  }

  Widget _buildSettingsGroup(String groupTitle, List<Setting> items) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16),
          child: Text(groupTitle,
              style: const TextStyle(color: Colors.deepPurple)),
        ),
        ...items,
      ],
    );
  }

  List<Widget> _buildSettingsList(BuildContext context) {
    return [
      _buildSettingsGroup("Customization", [
        Setting(
            title: "Theme",
            desc: activeTheme,
            onTap: () {
              final provider = Provider.of<ThemeProvider>(context, listen: false);

              showDialog(
                  context: context,
                  builder: (BuildContext buildCtx) {
                    return Dialog(
                      shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(2)),
                      child: Container(
                        padding: const EdgeInsets.symmetric(vertical: 16),
                        child: Column(
                          mainAxisSize: MainAxisSize.min,
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Padding(
                              padding:
                                  const EdgeInsets.symmetric(horizontal: 20),
                              child: Text("Theme",
                                  style: Theme.of(context).textTheme.headline6),
                            ),
                            Padding(
                              padding: const EdgeInsets.symmetric(vertical: 8),
                              child: Column(
                                children: [
                                  RadioListTile(
                                      value: AppTheme.System.name,
                                      groupValue: activeTheme,
                                      title: Text("Use System Default", style: Theme.of(context).textTheme.bodyText1),
                                      onChanged: (String? value) => setState(() {
                                        provider.changeTheme(ThemeMode.system);
                                        activeTheme = AppTheme.System.name;
                                        prefs.setString("theme", AppTheme.System.name);
                                        Navigator.pop(context);
                                      })),
                                  RadioListTile(
                                      value: AppTheme.Dark.name,
                                      groupValue: activeTheme,
                                      title: Text("Dark", style: Theme.of(context).textTheme.bodyText1),
                                      onChanged: (String? value) => setState(() {
                                        provider.changeTheme(ThemeMode.dark);
                                        activeTheme = AppTheme.Dark.name;
                                        prefs.setString("theme", AppTheme.Dark.name);
                                        Navigator.pop(context);
                                      })),
                                  RadioListTile(
                                      value: AppTheme.Light.name,
                                      groupValue: activeTheme,
                                      title: Text("Light", style: Theme.of(context).textTheme.bodyText1),
                                      onChanged: (String? value) => setState(() {
                                        provider.changeTheme(ThemeMode.light);
                                        activeTheme = AppTheme.Light.name;
                                        prefs.setString("theme", AppTheme.Light.name);
                                        Navigator.pop(context);
                                      })),
                                ],
                              ),
                            ),
                          ],
                        ),
                      ),
                    );
                  });
            })
      ])
    ];
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Settings"),
      ),
      body: ListView.separated(
        itemBuilder: (_, i) {
          return _buildSettingsList(context)[i];
        },
        separatorBuilder: (_, __) {
          return const SizedBox(height: 15);
        },
        itemCount: 1,
        padding: const EdgeInsets.symmetric(vertical: 20),
      ),
    );
  }
}
