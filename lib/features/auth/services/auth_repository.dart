import 'dart:convert';
import 'package:crypto/crypto.dart';
import 'package:http/http.dart' as http;
import '../models/auth_result.dart';

class AuthRepository {
  static const String apiUrl = 'https://api.mkissa.net/api';

  static const String _authenticateMutation = r'''
mutation(
  $username: String
  $email: String
  $password: String!
  $recaptchCode: String
  $captchaProvider: String
) {
  authenticate(
    serviceName: "password"
    params: {
      user: { username: $username, email: $email }
      password: $password
      recaptchCode: $recaptchCode
      captchaProvider: $captchaProvider
    }
  ) {
    sessionId
    tokens {
      refreshToken
      accessToken
    }
    user {
      _id
      username
      displayName
      picture
      emails {
        verified
        address
      }
    }
  }
}
''';

  static const String _logoutMutation = r'''
mutation {
  logout
}
''';

  static String hashPasswordToSha256(String password) {
    final bytes = utf8.encode(password);
    final digest = sha256.convert(bytes);
    return digest.toString();
  }

  static Future<AuthResult> authenticate({
    required String usernameOrEmail,
    required String rawPassword,
    required String recaptchCode,
    String captchaProvider = 'turnstile',
  }) async {
    if (usernameOrEmail.trim().isEmpty) {
      throw Exception('Username or email is required.');
    }
    if (rawPassword.trim().isEmpty) {
      throw Exception('Password is required.');
    }
    if (recaptchCode.trim().isEmpty) {
      throw Exception('Cloudflare Turnstile token is required.');
    }

    final isSha256 = rawPassword.length == 64 &&
        RegExp(r'^[0-9a-fA-F]+$').hasMatch(rawPassword);
    final hashedPassword =
        isSha256 ? rawPassword.toLowerCase() : hashPasswordToSha256(rawPassword);

    final isEmail = usernameOrEmail.contains('@');
    final variables = <String, dynamic>{
      if (isEmail) 'email': usernameOrEmail.trim() else 'username': usernameOrEmail.trim(),
      'password': hashedPassword,
      'recaptchCode': recaptchCode.trim(),
      'captchaProvider': captchaProvider,
    };

    final requestBody = jsonEncode({
      'query': _authenticateMutation.trim(),
      'variables': variables,
    });

    final headers = <String, String>{
      'Content-Type': 'application/json; charset=UTF-8',
      'User-Agent':
          'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:155.0) Gecko/20100101 Firefox/155.0',
      'Accept': '*/*',
      'Accept-Language': 'en-US,en;q=0.9',
      'Origin': 'https://youtu-chan.com',
      'Referer': 'https://youtu-chan.com/',
      'x-build-id': '168',
      'Host': 'api.mkissa.net',
      'Sec-Fetch-Dest': 'empty',
      'Sec-Fetch-Mode': 'cors',
      'Sec-Fetch-Site': 'cross-site',
      'Priority': 'u=4',
    };

    final response = await http
        .post(
          Uri.parse(apiUrl),
          headers: headers,
          body: requestBody,
        )
        .timeout(const Duration(seconds: 20));

    final rawResponse = response.body;
    return _parseAuthenticateResponse(rawResponse, usernameOrEmail);
  }

  static Future<bool> logout(String? accessToken) async {
    try {
      final requestBody = jsonEncode({
        'query': _logoutMutation.trim(),
      });

      final headers = <String, String>{
        'Content-Type': 'application/json; charset=UTF-8',
        'User-Agent':
            'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:155.0) Gecko/20100101 Firefox/155.0',
        'Origin': 'https://youtu-chan.com',
        'Referer': 'https://youtu-chan.com/',
        'x-build-id': '168',
        'Host': 'api.mkissa.net',
      };

      if (accessToken != null && accessToken.isNotEmpty) {
        final authHeader = accessToken.toLowerCase().startsWith('bearer ')
            ? accessToken
            : 'Bearer $accessToken';
        headers['authorization'] = authHeader;
      }

      await http
          .post(
            Uri.parse(apiUrl),
            headers: headers,
            body: requestBody,
          )
          .timeout(const Duration(seconds: 15));

      return true;
    } catch (_) {
      return true;
    }
  }

  static AuthResult _parseAuthenticateResponse(
      String rawResponse, String inputIdentifier) {
    final trimmed = rawResponse.trim();
    if (trimmed.startsWith('<!DOCTYPE') ||
        trimmed.startsWith('<html') ||
        trimmed.startsWith('<?xml')) {
      final titleMatch =
          RegExp(r'<title>(.*?)</title>', caseSensitive: false).firstMatch(trimmed);
      final title = titleMatch?.group(1)?.trim() ?? '';
      throw Exception(
          'Server returned HTML ($title). Cloudflare verification failed or expired. Please retry.');
    }

    final dynamic decoded = jsonDecode(trimmed);
    if (decoded is! Map<String, dynamic>) {
      throw Exception('Invalid server response format.');
    }

    if (decoded.containsKey('errors')) {
      final errors = decoded['errors'] as List<dynamic>?;
      if (errors != null && errors.isNotEmpty) {
        final firstError = errors[0] as Map<String, dynamic>?;
        final errMsg = firstError?['message']?.toString() ?? 'Authentication error';
        throw Exception(errMsg);
      }
    }

    final data = decoded['data'] as Map<String, dynamic>?;
    if (data == null) {
      throw Exception("Response missing 'data' object.");
    }

    final authenticate = data['authenticate'] as Map<String, dynamic>?;
    if (authenticate == null) {
      throw Exception(
          'Authentication failed. Invalid credentials or expired token.');
    }

    final sessionId = authenticate['sessionId']?.toString() ?? '';
    final tokens = authenticate['tokens'] as Map<String, dynamic>?;
    if (tokens == null) {
      throw Exception("Response missing 'tokens' object.");
    }

    final accessToken = tokens['accessToken']?.toString() ?? '';
    final refreshToken = tokens['refreshToken']?.toString() ?? '';

    if (accessToken.isEmpty || refreshToken.isEmpty) {
      throw Exception('Failed to retrieve access/refresh tokens from server.');
    }

    final user = authenticate['user'] as Map<String, dynamic>?;
    final username = user?['username']?.toString() ?? inputIdentifier;
    final displayName = user?['displayName']?.toString();
    final picture = user?['picture']?.toString();
    final userId = user?['_id']?.toString();

    String? email;
    bool isEmailVerified = false;
    final emails = user?['emails'] as List<dynamic>?;
    if (emails != null && emails.isNotEmpty) {
      final firstEmail = emails[0] as Map<String, dynamic>?;
      email = firstEmail?['address']?.toString();
      isEmailVerified = firstEmail?['verified'] == true;
    }

    return AuthResult(
      accessToken: accessToken,
      refreshToken: refreshToken,
      sessionId: sessionId,
      username: username,
      displayName: displayName,
      picture: picture,
      userId: userId,
      email: email,
      isEmailVerified: isEmailVerified,
      rawResponseJson: trimmed,
    );
  }
}
