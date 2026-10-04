import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../../../theme/app_colors.dart';
import '../../home/screens/tv_home_screen.dart';
import '../services/auth_repository.dart';
import '../services/token_manager.dart';
import '../widgets/tv_cloudflare_verification_dialog.dart';
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

  final ScrollController _scrollController = ScrollController();
  bool _isLoading = false;
  String? _errorMessage;
  Timer? _errorDismissTimer;

  final FocusScopeNode _screenFocusScopeNode = FocusScopeNode();
  bool _hasInitialFocusOccurred = false;
  String? _currentlyEditingField; // 'email' | 'password' | null

  @override
  void initState() {
    super.initState();
    // Do not automatically focus any field initially
  }

  @override
  void dispose() {
    _errorDismissTimer?.cancel();
    _scrollController.dispose();
    _emailController.dispose();
    _passwordController.dispose();
    _emailFocusNode.dispose();
    _passwordFocusNode.dispose();
    _loginButtonFocusNode.dispose();
    _screenFocusScopeNode.dispose();
    super.dispose();
  }

  void _showError(String message) {
    _errorDismissTimer?.cancel();

    final cleanMessage = message.trim().isEmpty
        ? 'Invalid Credentials'
        : message.trim();

    setState(() {
      _errorMessage = cleanMessage;
      _isLoading = false;
    });

    // Auto-dismiss the error message after 2 seconds
    _errorDismissTimer = Timer(const Duration(seconds: 2), () {
      if (mounted) {
        setState(() {
          _errorMessage = null;
        });
      }
    });
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

    // Open TV Cloudflare verification dialog to acquire token
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (dialogCtx) => TvCloudflareVerificationDialog(
        autoCloseOnSuccess: true,
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
        MaterialPageRoute(builder: (_) => const TvHomeScreen()),
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
            // Soft Light Ambient Gradient & Subtle Glowing Orbs
            Positioned.fill(
              child: Container(
                decoration: const BoxDecoration(
                  gradient: LinearGradient(
                    colors: [
                      AppColors.backgroundGradientStart,
                      AppColors.backgroundGradientEnd,
                    ],
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                  ),
                ),
              ),
            ),
            Positioned(
              top: -120,
              left: -80,
              child: Container(
                width: 500,
                height: 500,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  gradient: RadialGradient(
                    colors: [
                      const Color(0xFF8B5CF6).withAlpha(35),
                      Colors.transparent,
                    ],
                  ),
                ),
              ),
            ),
            Positioned(
              bottom: -120,
              right: -80,
              child: Container(
                width: 520,
                height: 520,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  gradient: RadialGradient(
                    colors: [
                      const Color(0xFFEC4899).withAlpha(25),
                      Colors.transparent,
                    ],
                  ),
                ),
              ),
            ),

            // Main TV 2-Column Wide Landscape Layout (1080p, 720p, 4k resilient)
            SafeArea(
              child: LayoutBuilder(
                builder: (context, constraints) {
                  final keyboardHeight = MediaQuery.of(context).viewInsets.bottom;
                  final isKeyboardOpen = keyboardHeight > 50;
                  final isCompactHeight = constraints.maxHeight < 600;
                  final vPadding = isCompactHeight ? 16.0 : 32.0;
                  final hPadding = constraints.maxWidth < 900 ? 36.0 : 64.0;
                  final formGap = isCompactHeight ? 10.0 : 16.0;

                  // Shift amount: on TV, virtual keyboard is an overlay window.
                  double yOffset = 0.0;
                  if (isKeyboardOpen) {
                    yOffset = -keyboardHeight * 0.55;
                  } else if (_currentlyEditingField == 'password') {
                    yOffset = -120.0;
                  } else if (_currentlyEditingField == 'email') {
                    yOffset = -50.0;
                  }

                  return Center(
                    child: SingleChildScrollView(
                      controller: _scrollController,
                      padding: EdgeInsets.symmetric(
                        horizontal: hPadding,
                        vertical: vPadding,
                      ),
                      child: AnimatedContainer(
                        duration: const Duration(milliseconds: 250),
                        curve: Curves.easeOutCubic,
                        transform: Matrix4.translationValues(0.0, yOffset, 0.0),
                        child: Row(
                          crossAxisAlignment: CrossAxisAlignment.center,
                          children: [
                            // Left Column: Cinematic Akira Brand Showcase
                            Expanded(
                              flex: 5,
                              child: Padding(
                                padding: const EdgeInsets.symmetric(horizontal: 16),
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
                                      child: Text(
                                        'AKIRA',
                                        style: TextStyle(
                                          fontSize: isCompactHeight ? 48 : 62,
                                          fontWeight: FontWeight.w900,
                                          letterSpacing: 8.0,
                                          color: Colors.white,
                                          height: 1.05,
                                        ),
                                      ),
                                    ),
                                    const SizedBox(height: 14),
                                    Text(
                                      'Stream your favorite anime in ultra clarity on the big screen with synced watch history, seamless episode playback, and real-time bookmarks.',
                                      style: TextStyle(
                                        color: AppColors.textSecondary,
                                        fontSize: isCompactHeight ? 14 : 16,
                                        height: 1.5,
                                        fontWeight: FontWeight.w500,
                                      ),
                                    ),
                                  ],
                                ),
                              ),
                            ),

                            SizedBox(width: constraints.maxWidth < 900 ? 32 : 56),

                            // Right Column: Focusable TV Login Form (Flat / Non-cardized)
                            Expanded(
                              flex: 5,
                              child: Padding(
                                padding: const EdgeInsets.symmetric(horizontal: 16),
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
                                          fontSize: 22,
                                          fontWeight: FontWeight.w800,
                                          letterSpacing: -0.3,
                                        ),
                                      ),
                                      const SizedBox(height: 6),
                                      const Text(
                                        'Use your remote control to navigate between fields',
                                        style: TextStyle(
                                          color: AppColors.textSecondary,
                                          fontSize: 13,
                                        ),
                                      ),
                                      SizedBox(height: formGap * 1.2),

                                      // Email Input
                                      TvInputField(
                                        label: 'Email or Username',
                                        hintText: 'Enter your email or username',
                                        prefixIcon: Icons.mail_outline_rounded,
                                        controller: _emailController,
                                        focusNode: _emailFocusNode,
                                        nextFocusNode: _passwordFocusNode,
                                        keyboardType: TextInputType.emailAddress,
                                        onEditingChanged: (isEditing) {
                                          setState(() {
                                            _currentlyEditingField = isEditing ? 'email' : null;
                                          });
                                        },
                                      ),
                                      SizedBox(height: formGap),

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
                                        onEditingChanged: (isEditing) {
                                          setState(() {
                                            _currentlyEditingField = isEditing ? 'password' : null;
                                          });
                                        },
                                        onSubmitted: (_) => _handleLoginClicked(),
                                      ),

                                      AnimatedSize(
                                        duration: const Duration(milliseconds: 200),
                                        curve: Curves.easeInOut,
                                        child: (_errorMessage != null && _errorMessage!.trim().isNotEmpty)
                                            ? Padding(
                                                padding: const EdgeInsets.only(top: 12),
                                                child: Container(
                                                  padding: const EdgeInsets.symmetric(
                                                    horizontal: 14,
                                                    vertical: 10,
                                                  ),
                                                  decoration: BoxDecoration(
                                                    color: Colors.red.withAlpha(20),
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
                                              )
                                            : const SizedBox.shrink(),
                                      ),

                                      SizedBox(height: isCompactHeight ? 16 : 22),

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
                  );
                },
              ),
            ),
          ],
        ),
      ),
    );
  }
}
