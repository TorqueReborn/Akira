class AuthResult {
  final String accessToken;
  final String refreshToken;
  final String sessionId;
  final String username;
  final String? displayName;
  final String? picture;
  final String? userId;
  final String? email;
  final bool isEmailVerified;
  final String rawResponseJson;

  AuthResult({
    required this.accessToken,
    required this.refreshToken,
    required this.sessionId,
    required this.username,
    this.displayName,
    this.picture,
    this.userId,
    this.email,
    this.isEmailVerified = false,
    this.rawResponseJson = '',
  });
}
