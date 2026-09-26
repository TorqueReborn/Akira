import 'package:flutter/material.dart';

import 'features/auth/screens/login_screen.dart';
import 'features/auth/services/token_manager.dart';
import 'features/home/screens/home_screen.dart';
import 'theme/app_theme.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await TokenManager.init();
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
      home: isLoggedIn ? const HomeScreen() : const LoginScreen(),
    );
  }
}
