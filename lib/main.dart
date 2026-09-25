import 'package:flutter/material.dart';

import 'features/auth/screens/login_screen.dart';
import 'theme/app_theme.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const AkiraApp());
}

class AkiraApp extends StatelessWidget {
  const AkiraApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(

      title: 'Akira',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.lightTheme,
      home: const LoginScreen(),
    );
  }
}
