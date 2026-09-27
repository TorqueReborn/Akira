import 'package:flutter/material.dart';

import 'features/auth/screens/login_screen.dart';
import 'features/auth/screens/tv_login_screen.dart';
import 'features/auth/services/token_manager.dart';
import 'features/home/screens/home_screen.dart';
import 'theme/app_theme.dart';
import 'utils/device_detector.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Future.wait([
    TokenManager.init(),
    DeviceDetector.initialize(),
  ]);
  runApp(const AkiraApp());
}

class AkiraApp extends StatelessWidget {
  const AkiraApp({super.key});

  @override
  Widget build(BuildContext context) {
    final isLoggedIn = TokenManager.isLoggedIn();

    return MaterialApp(
      title: 'Akira',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.lightTheme,
      home: Builder(
        builder: (ctx) {
          if (isLoggedIn) {
            return const HomeScreen();
          }
          final isTv = DeviceDetector.isTv || DeviceDetector.isTvMode(ctx);
          return isTv ? const TvLoginScreen() : const LoginScreen();
        },
      ),
    );
  }
}
