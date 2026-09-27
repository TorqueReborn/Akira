import 'dart:async';
import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:webview_flutter/webview_flutter.dart';

import '../../../theme/app_colors.dart';

/// Premium TV Cloudflare Verification Dialog matching Akira's typography & cards,
/// with instant default D-Pad focus on the close button.
class TvCloudflareVerificationDialog extends StatefulWidget {
  final ValueChanged<String> onTokenExtracted;
  final VoidCallback onDismiss;
  final bool autoCloseOnSuccess;

  const TvCloudflareVerificationDialog({
    super.key,
    required this.onTokenExtracted,
    required this.onDismiss,
    this.autoCloseOnSuccess = true,
  });

  @override
  State<TvCloudflareVerificationDialog> createState() =>
      _TvCloudflareVerificationDialogState();
}

class _TvCloudflareVerificationDialogState
    extends State<TvCloudflareVerificationDialog> {
  late final WebViewController _controller;
  final FocusNode _closeButtonFocusNode = FocusNode();

  bool _isLoading = true;
  bool _tokenDelivered = false;
  bool _needsManualInteraction = false;
  Timer? _interactionCheckTimer;

  bool _isCloseFocused = false; // Not focused initially until user navigates with D-Pad
  bool _hasInitialFocusOccurred = false;

  @override
  void initState() {
    super.initState();
    _closeButtonFocusNode.addListener(_onFocusChange);
    _initWebView();
  }

  void _onFocusChange() {
    if (mounted) {
      setState(() {
        _isCloseFocused = _closeButtonFocusNode.hasFocus;
      });
    }
  }

  @override
  void dispose() {
    _interactionCheckTimer?.cancel();
    _closeButtonFocusNode.removeListener(_onFocusChange);
    _closeButtonFocusNode.dispose();
    super.dispose();
  }

  void _onTokenReceived(String token) {
    if (_tokenDelivered) return;
    _tokenDelivered = true;
    _interactionCheckTimer?.cancel();
    widget.onTokenExtracted(token);
  }

  void _initWebView() {
    final controller = WebViewController()
      ..setJavaScriptMode(JavaScriptMode.unrestricted)
      ..setBackgroundColor(Colors.transparent)
      ..setUserAgent(
        'Mozilla/5.0 (Linux; Android 14; 2312DRA50I Build/CP2A.260605.016; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/130.0.0.0 Mobile Safari/537.36',
      )
      ..addJavaScriptChannel(
        'CaptchaChannel',
        onMessageReceived: (JavaScriptMessage message) {
          final token = message.message.trim();
          if (token.isNotEmpty) {
            _onTokenReceived(token);
          }
        },
      )
      ..setNavigationDelegate(
        NavigationDelegate(
          onPageFinished: (String url) {
            if (mounted) {
              setState(() {
                _isLoading = false;
              });
            }

            const js = '''
(function() {
  var style = document.createElement('style');
  style.innerHTML = 'body, html { background: transparent !important; display: flex !important; justify-content: center !important; align-items: center !important; min-height: 100vh !important; margin: 0 !important; padding: 0 !important; overflow: hidden !important; width: 100vw !important; height: 100vh !important; }';
  document.head.appendChild(style);

  function sendToFlutter(token) {
    if (token && window.CaptchaChannel) {
      window.CaptchaChannel.postMessage(token);
    }
  }

  var oldCallback = window.captchaCallback;
  window.captchaCallback = function(token) {
    sendToFlutter(token);
    if (oldCallback) oldCallback(token);
  };

  setInterval(function() {
    var input = document.querySelector('input[name="cf-turnstile-response"]') || 
                document.querySelector('textarea[name="g-recaptcha-response"]') ||
                document.querySelector('input[name="g-recaptcha-response"]');
    if (input && input.value && !window.__tokenExtracted) {
      window.__tokenExtracted = true;
      sendToFlutter(input.value);
    }
  }, 400);
})();
''';
            _controller.runJavaScript(js);
          },
          onWebResourceError: (WebResourceError error) {
            debugPrint('Turnstile WebView error: ${error.description}');
          },
        ),
      );

    _controller = controller;

    _controller.loadRequest(
      Uri.parse('https://api.allanime.day/captcha/turnstile'),
      headers: const {
        'Referer': 'https://allmanga.to',
        'sec-ch-ua': '"Not=A?Brand";v="99", "Android WebView";v="130", "Chromium";v="130"',
        'sec-ch-ua-mobile': '?1',
        'sec-ch-ua-platform': '"Android"',
        'upgrade-insecure-requests': '1',
        'accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8',
        'x-requested-with': 'com.allanime.animechicken',
        'accept-language': 'en-US,en;q=0.9',
      },
    );

    // If Cloudflare doesn't autocomplete within 2 seconds, reveal header instruction & mouse prompt
    _interactionCheckTimer = Timer(const Duration(milliseconds: 2000), () {
      if (!_tokenDelivered && mounted) {
        setState(() {
          _needsManualInteraction = true;
        });
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Focus(
      autofocus: true,
      onKeyEvent: (node, event) {
        if (event is KeyDownEvent) {
          if (event.logicalKey == LogicalKeyboardKey.escape ||
              event.logicalKey == LogicalKeyboardKey.backspace ||
              event.logicalKey == LogicalKeyboardKey.gameButtonB) {
            widget.onDismiss();
            return KeyEventResult.handled;
          }

          if (!_hasInitialFocusOccurred) {
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
              _closeButtonFocusNode.requestFocus();
              return KeyEventResult.handled;
            }
          }
        }
        return KeyEventResult.ignored;
      },
      child: Dialog(
        backgroundColor: Colors.transparent,
        elevation: 0,
        insetPadding: const EdgeInsets.symmetric(horizontal: 48, vertical: 24),
        child: Center(
          child: Container(
            constraints: BoxConstraints(
              maxWidth: _needsManualInteraction ? 440 : 380,
            ),
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(28),
              gradient: AppColors.logoGradient,
              boxShadow: [
                BoxShadow(
                  color: const Color(0xFF8B5CF6).withAlpha(45),
                  blurRadius: 32,
                  offset: const Offset(0, 12),
                ),
              ],
            ),
            padding: const EdgeInsets.all(1.5),
            child: Container(
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(26.5),
              ),
              padding: _needsManualInteraction
                  ? const EdgeInsets.fromLTRB(22, 20, 22, 22)
                  : const EdgeInsets.all(16),
              child: AnimatedSize(
                duration: const Duration(milliseconds: 250),
                curve: Curves.easeInOut,
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    // Only show header with mouse hint and close button if it didn't autocomplete
                    if (_needsManualInteraction) ...[
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          const Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                'Security Verification',
                                style: TextStyle(
                                  color: AppColors.textPrimary,
                                  fontSize: 18,
                                  fontWeight: FontWeight.w800,
                                  letterSpacing: -0.3,
                                ),
                              ),
                              SizedBox(height: 4),
                              Text(
                                'Connect mouse & click the checkbox',
                                style: TextStyle(
                                  color: AppColors.textSecondary,
                                  fontSize: 13,
                                  fontWeight: FontWeight.w500,
                                ),
                              ),
                            ],
                          ),
                          // D-Pad Focusable Close Button
                          Focus(
                            focusNode: _closeButtonFocusNode,
                            autofocus: false,
                            onKeyEvent: (node, event) {
                              if (event is KeyDownEvent) {
                                if (event.logicalKey == LogicalKeyboardKey.select ||
                                    event.logicalKey == LogicalKeyboardKey.enter ||
                                    event.logicalKey == LogicalKeyboardKey.space ||
                                    event.logicalKey == LogicalKeyboardKey.gameButtonA ||
                                    event.logicalKey == LogicalKeyboardKey.escape ||
                                    event.logicalKey == LogicalKeyboardKey.backspace ||
                                    event.logicalKey == LogicalKeyboardKey.gameButtonB) {
                                  widget.onDismiss();
                                  return KeyEventResult.handled;
                                }
                              }
                              return KeyEventResult.ignored;
                            },
                            child: AnimatedScale(
                              scale: _isCloseFocused ? 1.15 : 1.0,
                              duration: const Duration(milliseconds: 150),
                              child: Container(
                                decoration: BoxDecoration(
                                  color: _isCloseFocused
                                      ? AppColors.primary
                                      : const Color(0xFFF1F5F9),
                                  shape: BoxShape.circle,
                                  border: Border.all(
                                    color: _isCloseFocused
                                        ? AppColors.primaryLight
                                        : Colors.transparent,
                                    width: 2,
                                  ),
                                ),
                                child: Material(
                                  color: Colors.transparent,
                                  child: InkWell(
                                    onTap: widget.onDismiss,
                                    customBorder: const CircleBorder(),
                                    child: Padding(
                                      padding: const EdgeInsets.all(6.0),
                                      child: Icon(
                                        Icons.close_rounded,
                                        color: _isCloseFocused
                                            ? Colors.white
                                            : AppColors.textSecondary,
                                        size: 16,
                                      ),
                                    ),
                                  ),
                                ),
                              ),
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 18),
                    ],

                    // Clean WebView Challenge Area
                    SizedBox(
                      height: 100,
                      width: double.infinity,
                      child: Stack(
                        alignment: Alignment.center,
                        children: [
                          WebViewWidget(
                            controller: _controller,
                            gestureRecognizers: const <Factory<OneSequenceGestureRecognizer>>{
                              Factory<OneSequenceGestureRecognizer>(
                                EagerGestureRecognizer.new,
                              ),
                            },
                          ),
                          if (_isLoading)
                            const Center(
                              child: SizedBox(
                                width: 24,
                                height: 24,
                                child: CircularProgressIndicator(
                                  strokeWidth: 2.5,
                                  valueColor: AlwaysStoppedAnimation<Color>(
                                    AppColors.primary,
                                  ),
                                ),
                              ),
                            ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}
