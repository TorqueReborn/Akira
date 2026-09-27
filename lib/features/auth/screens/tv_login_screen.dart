import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../../../theme/app_colors.dart';
import '../../home/screens/home_screen.dart';
import '../services/auth_repository.dart';
import '../services/token_manager.dart';
import '../widgets/cloudflare_verification_dialog.dart';
import '../widgets/tv_button.dart';
import '../widgets/tv_input_field.dart';

/// Akira TV Optimized Login Screen tailored for 10-foot TV experience, D-Pad navigation,
/// beautiful landscape 2-column layout, and remote controller focus transitions.
class TvLoginScreen extends StatefulWidget {
  const TvLoginScreen({super.key});

  @override
  State<TvLoginScreen> createState() => _TvLoginScreenState();
}

class _TvLoginScreenState extends State<TvLoginScreen> {
  final TextEditingController _emailController = TextEditingController();
  final TextEditingController _passwordController = TextEditingController();

  final FocusNode _emailFocusNode = FocusNode();
  final FocusNode _passwordFocusNode = FocusNode();
  final FocusNode _loginButtonFocusNode = FocusNode();

  bool _isLoading = false;
  String? _errorMessage;

  final FocusScopeNode _screenFocusScopeNode = FocusScopeNode();
  bool _hasInitialFocusOccurred = false;

  @override
  void initState() {
    super.initState();
    // Do not automatically focus any field initially
  }

  @override
  void dispose() {
    _emailController.dispose();
    _passwordController.dispose();
    _emailFocusNode.dispose();
    _passwordFocusNode.dispose();
    _loginButtonFocusNode.dispose();
    _screenFocusScopeNode.dispose();
    super.dispose();
  }

  void _showError(String message) {
    setState(() {
      _errorMessage = message;
      _isLoading = false;
    });
    ScaffoldMessenger.of(context).clearSnackBars();
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Row(
          children: [
            const Icon(Icons.error_outline_rounded, color: Colors.white, size: 24),
            const SizedBox(width: 14),
            Expanded(
              child: Text(
                message,
                style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 16),
              ),
            ),
          ],
        ),
        backgroundColor: Colors.redAccent.shade700,
        behavior: SnackBarBehavior.floating,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
        margin: const EdgeInsets.symmetric(horizontal: 48, vertical: 24),
        duration: const Duration(seconds: 5),
      ),
    );
  }

  void _handleLoginClicked() {
    final email = _emailController.text.trim();
    final password = _passwordController.text;

    if (email.isEmpty) {
      _showError('Please enter your email or username.');
      _emailFocusNode.requestFocus();
      return;
    }
    if (password.isEmpty) {
      _showError('Please enter your password.');
      _passwordFocusNode.requestFocus();
      return;
    }

    setState(() {
      _errorMessage = null;
    });

    // Open Cloudflare verification dialog to acquire token
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (dialogCtx) => CloudflareVerificationDialog(
        onTokenExtracted: (token) {
          Navigator.of(dialogCtx).pop();
          _performLogin(token);
        },
        onDismiss: () {
          Navigator.of(dialogCtx).pop();
          _loginButtonFocusNode.requestFocus();
        },
      ),
    );
  }

  Future<void> _performLogin(String turnstileToken) async {
    final email = _emailController.text.trim();
    final password = _passwordController.text;

    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final authResult = await AuthRepository.authenticate(
        usernameOrEmail: email,
        rawPassword: password,
        recaptchCode: turnstileToken,
      );

      final detectedEmail = email.contains('@') ? email : authResult.email;

      await TokenManager.saveAuthData(
        accessToken: authResult.accessToken,
        refreshToken: authResult.refreshToken,
        sessionId: authResult.sessionId,
        username: authResult.username,
        displayName: authResult.displayName,
        picture: authResult.picture,
        userId: authResult.userId,
        email: detectedEmail,
        isEmailVerified: authResult.isEmailVerified,
      );

      if (!mounted) return;

      Navigator.of(context).pushReplacement(
        MaterialPageRoute(builder: (_) => const HomeScreen()),
      );
    } catch (e) {
      if (!mounted) return;
      final rawMsg = e.toString().replaceFirst('Exception: ', '');
      _showError(rawMsg);
      _loginButtonFocusNode.requestFocus();
    } finally {
      if (mounted) {
        setState(() {
          _isLoading = false;
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Focus(
      autofocus: true,
      onKeyEvent: (node, event) {
        if (!_hasInitialFocusOccurred && event is KeyDownEvent) {
          final isDpadKey = event.logicalKey == LogicalKeyboardKey.arrowDown ||
              event.logicalKey == LogicalKeyboardKey.arrowUp ||
              event.logicalKey == LogicalKeyboardKey.arrowLeft ||
              event.logicalKey == LogicalKeyboardKey.arrowRight ||
              event.logicalKey == LogicalKeyboardKey.select ||
              event.logicalKey == LogicalKeyboardKey.enter ||
              event.logicalKey == LogicalKeyboardKey.space ||
              event.logicalKey == LogicalKeyboardKey.gameButtonA;

          if (isDpadKey) {
            _hasInitialFocusOccurred = true;
            _emailFocusNode.requestFocus();
            return KeyEventResult.handled;
          }
        }
        return KeyEventResult.ignored;
      },
      child: Scaffold(
        backgroundColor: AppColors.background,
        body: Stack(
        children: [
          // Background Ambient Glow Circles
          Positioned(
            top: -120,
            left: -80,
            child: Container(
              width: 450,
              height: 450,
              decoration: const BoxDecoration(
                shape: BoxShape.circle,
                gradient: RadialGradient(
                  colors: [AppColors.ambientGlow, Colors.transparent],
                ),
              ),
            ),
          ),
          Positioned(
            bottom: -150,
            right: -100,
            child: Container(
              width: 500,
              height: 500,
              decoration: const BoxDecoration(
                shape: BoxShape.circle,
                gradient: RadialGradient(
                  colors: [AppColors.ambientGlow, Colors.transparent],
                ),
              ),
            ),
          ),

          // Main TV 2-Column Wide Landscape Layout
          SafeArea(
            child: Center(
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 72, vertical: 36),
                child: IntrinsicHeight(
                  child: Row(
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      // Left Column: TV Hero & Akira Branding Showcase
                      Expanded(
                        flex: 5,
                        child: Padding(
                          padding: const EdgeInsets.symmetric(vertical: 8, horizontal: 12),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              // Akira Title & Gradient
                              ShaderMask(
                                shaderCallback: (bounds) =>
                                    AppColors.logoGradient.createShader(
                                  Rect.fromLTWH(0, 0, bounds.width, bounds.height),
                                ),
                                child: const Text(
                                  'AKIRA',
                                  style: TextStyle(
                                    fontSize: 56,
                                    fontWeight: FontWeight.w900,
                                    letterSpacing: 8.0,
                                    color: Colors.white,
                                  ),
                                ),
                              ),
                              const SizedBox(height: 14),
                              const Text(
                                'Stream your favorite anime in ultra clarity on the big screen.',
                                style: TextStyle(
                                  color: AppColors.textSecondary,
                                  fontSize: 18,
                                  height: 1.5,
                                  fontWeight: FontWeight.w500,
                                ),
                              ),
                            ],
                          ),
                        ),
                      ),

                      const SizedBox(width: 64),

                      // Right Column: Focusable TV Login Section
                      Expanded(
                        flex: 5,
                        child: Padding(
                          padding: const EdgeInsets.symmetric(vertical: 8, horizontal: 12),
                          child: FocusTraversalGroup(
                            policy: OrderedTraversalPolicy(),
                            child: Column(
                              mainAxisSize: MainAxisSize.min,
                              crossAxisAlignment: CrossAxisAlignment.stretch,
                              mainAxisAlignment: MainAxisAlignment.center,
                              children: [
                                const Text(
                                  'Sign In to Your Account',
                                  style: TextStyle(
                                    color: AppColors.textPrimary,
                                    fontSize: 24,
                                    fontWeight: FontWeight.w800,
                                    letterSpacing: -0.3,
                                  ),
                                ),
                                const SizedBox(height: 6),
                                const Text(
                                  'Use your remote control to navigate between fields',
                                  style: TextStyle(
                                    color: AppColors.textSecondary,
                                    fontSize: 14,
                                  ),
                                ),
                              const SizedBox(height: 24),

                              // Email Input
                              TvInputField(
                                label: 'Email or Username',
                                hintText: 'Enter your email or username',
                                prefixIcon: Icons.mail_outline_rounded,
                                controller: _emailController,
                                focusNode: _emailFocusNode,
                                nextFocusNode: _passwordFocusNode,
                                keyboardType: TextInputType.emailAddress,
                              ),
                              const SizedBox(height: 16),

                              // Password Input
                              TvInputField(
                                label: 'Password',
                                hintText: 'Enter your account password',
                                prefixIcon: Icons.lock_outline_rounded,
                                controller: _passwordController,
                                focusNode: _passwordFocusNode,
                                previousFocusNode: _emailFocusNode,
                                nextFocusNode: _loginButtonFocusNode,
                                isPassword: true,
                                keyboardType: TextInputType.visiblePassword,
                                onSubmitted: (_) => _handleLoginClicked(),
                              ),

                              if (_errorMessage != null) ...[
                                const SizedBox(height: 14),
                                Container(
                                  padding: const EdgeInsets.symmetric(
                                    horizontal: 14,
                                    vertical: 10,
                                  ),
                                  decoration: BoxDecoration(
                                    color: Colors.red.withAlpha(25),
                                    borderRadius: BorderRadius.circular(12),
                                    border: Border.all(
                                      color: Colors.redAccent.withAlpha(90),
                                    ),
                                  ),
                                  child: Row(
                                    children: [
                                      const Icon(
                                        Icons.error_outline_rounded,
                                        color: Colors.redAccent,
                                        size: 20,
                                      ),
                                      const SizedBox(width: 10),
                                      Expanded(
                                        child: Text(
                                          _errorMessage!,
                                          style: const TextStyle(
                                            color: Colors.redAccent,
                                            fontSize: 13,
                                            fontWeight: FontWeight.w600,
                                          ),
                                        ),
                                      ),
                                    ],
                                  ),
                                ),
                              ],

                              const SizedBox(height: 28),

                              // Login Submit Button
                              TvButton(
                                text: 'SIGN IN',
                                icon: Icons.arrow_forward_rounded,
                                isLoading: _isLoading,
                                focusNode: _loginButtonFocusNode,
                                previousFocusNode: _passwordFocusNode,
                                onPressed: _handleLoginClicked,
                              ),
                            ],
                          ),
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ),
        ],
      ),
    ),
    );
  }
}
