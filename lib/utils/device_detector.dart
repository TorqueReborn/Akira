import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

/// Service and utilities to detect if the app is currently running on Android TV / Leanback device.
class DeviceDetector {
  DeviceDetector._();

  static const MethodChannel _channel = MethodChannel('com.ghostreborn.akira/device_info');
  static bool? _cachedIsTv;

  /// Check via native Android API (UiModeManager & Leanback Feature) whether the device is TV.
  static Future<void> initialize() async {
    if (kIsWeb) {
      _cachedIsTv = false;
      return;
    }

    try {
      final bool? isTv = await _channel.invokeMethod<bool>('isTvDevice');
      _cachedIsTv = isTv ?? false;
    } catch (_) {
      _cachedIsTv = false;
    }
  }

  /// Synchronous getter after initialization.
  static bool get isTv => _cachedIsTv ?? false;

  /// Check if TV mode is active or if running on large landscape screen
  static bool isTvMode(BuildContext context) {
    if (_cachedIsTv != null && _cachedIsTv!) {
      return true;
    }
    final size = MediaQuery.of(context).size;
    // TV screens are wide landscape displays >= 960 width with aspect ratio >= 1.5
    if (size.width >= 960 && (size.width / size.height) >= 1.5) {
      _cachedIsTv = true;
      return true;
    }
    return false;
  }
}
