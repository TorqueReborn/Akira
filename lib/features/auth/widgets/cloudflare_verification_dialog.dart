import 'package:flutter/material.dart';
import 'package:webview_flutter/webview_flutter.dart';

class CloudflareVerificationDialog extends StatefulWidget {
  final ValueChanged<String> onTokenExtracted;
  final VoidCallback onDismiss;

  const CloudflareVerificationDialog({
    super.key,
    required this.onTokenExtracted,
    required this.onDismiss,
  });

  @override
  State<CloudflareVerificationDialog> createState() =>
      _CloudflareVerificationDialogState();
}

class _CloudflareVerificationDialogState
    extends State<CloudflareVerificationDialog> {
  late final WebViewController _controller;
  bool _isLoading = true;
  bool _tokenDelivered = false;

  @override
  void initState() {
    super.initState();
    _initWebView();
  }

  void _onTokenReceived(String token) {
    if (_tokenDelivered) return;
    _tokenDelivered = true;
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
  style.innerHTML = 'body, html { background: transparent !important; display: flex !important; justify-content: center !important; align-items: center !important; min-height: 100vh !important; margin: 0 !important; padding: 0 !important; overflow: hidden !important; }';
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
    var input = document.querySelector('input[name="cf-turnstile-response"]');
    if (input && input.value && !window.__tokenExtracted) {
      window.__tokenExtracted = true;
      sendToFlutter(input.value);
    }
  }, 1000);
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
  }

  @override
  Widget build(BuildContext context) {
    return Dialog(
      backgroundColor: Colors.transparent,
      elevation: 0,
      insetPadding: const EdgeInsets.symmetric(horizontal: 24, vertical: 24),
      child: Center(
        child: Container(
          constraints: const BoxConstraints(maxWidth: 380),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(28),
            // Gradient border matching the Akira logo & button
            gradient: const LinearGradient(
              colors: [
                Color(0xFF3B82F6), // Vibrant Blue
                Color(0xFF8B5CF6), // Rich Purple
                Color(0xFFEC4899), // Vibrant Pink
              ],
              begin: Alignment.topLeft,
              end: Alignment.bottomRight,
            ),
            boxShadow: [
              BoxShadow(
                color: const Color(0xFF8B5CF6).withAlpha(45),
                blurRadius: 28,
                offset: const Offset(0, 12),
              ),
            ],
          ),
          padding: const EdgeInsets.all(1.5), // Gradient border stroke thickness
          child: Container(
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(26.5),
            ),
            padding: const EdgeInsets.fromLTRB(22, 22, 22, 24),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                // Header: Title + Close Icon
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  crossAxisAlignment: CrossAxisAlignment.center,
                  children: [
                    const Text(
                      'Verify you are human',
                      style: TextStyle(
                        color: Color(0xFF1E1B2E),
                        fontSize: 18,
                        fontWeight: FontWeight.w700,
                        letterSpacing: -0.2,
                      ),
                    ),
                    Material(
                      color: Colors.transparent,
                      child: InkWell(
                        onTap: widget.onDismiss,
                        borderRadius: BorderRadius.circular(20),
                        child: Container(
                          padding: const EdgeInsets.all(6),
                          decoration: const BoxDecoration(
                            color: Color(0xFFF1F5F9),
                            shape: BoxShape.circle,
                          ),
                          child: const Icon(
                            Icons.close_rounded,
                            color: Color(0xFF64748B),
                            size: 18,
                          ),
                        ),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 18),

                // Turnstile Challenge Container
                ClipRRect(
                  borderRadius: BorderRadius.circular(16),
                  child: Container(
                    height: 200,
                    width: double.infinity,
                    decoration: BoxDecoration(
                      color: const Color(0xFFF8FAFC),
                      borderRadius: BorderRadius.circular(16),
                      border: Border.all(
                        color: const Color(0xFFE2E8F0),
                        width: 1.0,
                      ),
                    ),
                    child: Stack(
                      alignment: Alignment.center,
                      children: [
                        WebViewWidget(controller: _controller),
                        if (_isLoading)
                          const Center(
                            child: SizedBox(
                              width: 28,
                              height: 28,
                              child: CircularProgressIndicator(
                                strokeWidth: 2.5,
                                valueColor: AlwaysStoppedAnimation<Color>(
                                  Color(0xFF8B5CF6),
                                ),
                              ),
                            ),
                          ),
                      ],
                    ),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
