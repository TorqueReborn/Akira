import 'package:flutter/material.dart';

import '../theme/app_colors.dart';

/// Akira Brand Name & Tagline Header Component.
class AkiraBranding extends StatelessWidget {
  const AkiraBranding({super.key});

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        // Brand Name - AKIRA with Gradient Styling
        ShaderMask(
          shaderCallback: (bounds) => AppColors.logoGradient.createShader(
            Rect.fromLTWH(0, 0, bounds.width, bounds.height),
          ),
          child: const Text(
            'AKIRA',
            style: TextStyle(
              fontSize: 36,
              fontWeight: FontWeight.w900,
              letterSpacing: 6.0,
              color: Colors.white, // Masked by shader
            ),
          ),
        ),
        const SizedBox(height: 6),

        // Tagline
        const Text(
          'Your gateway to the anime world.',
          style: TextStyle(
            color: AppColors.textSecondary,
            fontSize: 14,
            fontWeight: FontWeight.w500,
            letterSpacing: 0.2,
          ),
          textAlign: TextAlign.center,
        ),
      ],
    );
  }
}
