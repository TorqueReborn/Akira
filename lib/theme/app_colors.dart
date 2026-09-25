import 'package:flutter/material.dart';

/// AppColors defines the Solo Leveling inspired Light Mode color palette
/// for the Akira anime application.
class AppColors {
  AppColors._();

  // Backgrounds
  static const Color background = Color(0xFFFAF9FE);
  static const Color backgroundGradientStart = Color(0xFFFFFFFF);
  static const Color backgroundGradientEnd = Color(0xFFF4EDFF);

  // Surface & Cards
  static const Color surface = Color(0xFFFFFFFF);
  static const Color surfaceBorder = Color(0xFFEDE8F5);
  static const Color surfaceBorderFocused = Color(0xFF8B5CF6);

  // Primary & Accents (Purple / Violet)
  static const Color primary = Color(0xFF7C3AED); // Modern Electric Violet
  static const Color primaryLight = Color(0xFF8B5CF6);
  static const Color primaryDark = Color(0xFF6D28D9);
  static const Color accentSubtle = Color(0xFFF3E8FF);

  // Text Colors
  static const Color textPrimary = Color(0xFF1E1B2E);
  static const Color textSecondary = Color(0xFF6E6B7B);
  static const Color textHint = Color(0xFFA39EB0);

  // Icons
  static const Color iconPrimary = Color(0xFF7C3AED);
  static const Color iconMuted = Color(0xFF9D97AD);

  // Glow & Shadows
  static const Color shadowColor = Color(0x1A6D28D9);
  static const Color primaryGlow = Color(0x408B5CF6);
  static const Color focusGlow = Color(0x338B5CF6);
  static const Color ambientGlow = Color(0x1F8B5CF6);

  // Gradients
  static const LinearGradient primaryGradient = LinearGradient(
    colors: [Color(0xFF8B5CF6), Color(0xFF6D28D9)],
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
  );

  // Logo color extracted gradient matching the vibrant blue, purple, and pink tones of the logo
  static const LinearGradient logoGradient = LinearGradient(
    colors: [
      Color(0xFF3B82F6), // Vibrant Blue from logo
      Color(0xFF8B5CF6), // Rich Purple from logo
      Color(0xFFEC4899), // Vibrant Pink from logo
    ],
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
  );
}
