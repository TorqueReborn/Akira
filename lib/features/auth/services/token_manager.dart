import 'package:shared_preferences/shared_preferences.dart';

class TokenManager {
  static const String _keyAccessToken = 'access_token';
  static const String _keyRefreshToken = 'refresh_token';
  static const String _keySessionId = 'session_id';
  static const String _keyUsername = 'username';
  static const String _keyDisplayName = 'display_name';
  static const String _keyPicture = 'picture';
  static const String _keyUserId = 'user_id';
  static const String _keyEmail = 'email';
  static const String _keyEmailVerified = 'email_verified';

  static SharedPreferences? _prefs;

  static Future<void> init() async {
    _prefs ??= await SharedPreferences.getInstance();
  }

  static Future<void> saveAuthData({
    required String accessToken,
    required String refreshToken,
    required String sessionId,
    required String username,
    String? displayName,
    String? picture,
    String? userId,
    String? email,
    bool isEmailVerified = false,
  }) async {
    final prefs = _prefs ?? await SharedPreferences.getInstance();
    await prefs.setString(_keyAccessToken, accessToken);
    await prefs.setString(_keyRefreshToken, refreshToken);
    await prefs.setString(_keySessionId, sessionId);
    await prefs.setString(_keyUsername, username);
    if (displayName != null) {
      await prefs.setString(_keyDisplayName, displayName);
    } else {
      await prefs.remove(_keyDisplayName);
    }
    if (picture != null) {
      await prefs.setString(_keyPicture, picture);
    } else {
      await prefs.remove(_keyPicture);
    }
    if (userId != null) {
      await prefs.setString(_keyUserId, userId);
    } else {
      await prefs.remove(_keyUserId);
    }
    if (email != null) {
      await prefs.setString(_keyEmail, email);
    } else {
      await prefs.remove(_keyEmail);
    }
    await prefs.setBool(_keyEmailVerified, isEmailVerified);
  }

  static String? getAccessToken() => _prefs?.getString(_keyAccessToken);
  static String? getRefreshToken() => _prefs?.getString(_keyRefreshToken);
  static String? getSessionId() => _prefs?.getString(_keySessionId);
  static String? getUsername() => _prefs?.getString(_keyUsername);
  static String? getDisplayName() => _prefs?.getString(_keyDisplayName);
  static String? getPicture() => _prefs?.getString(_keyPicture);
  static String? getUserId() => _prefs?.getString(_keyUserId);
  static String? getEmail() => _prefs?.getString(_keyEmail);
  static bool isEmailVerified() => _prefs?.getBool(_keyEmailVerified) ?? false;

  static bool isLoggedIn() {
    final token = getAccessToken();
    return token != null && token.trim().isNotEmpty;
  }

  static Future<void> clear() async {
    final prefs = _prefs ?? await SharedPreferences.getInstance();
    await prefs.clear();
  }
}
