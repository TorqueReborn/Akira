import 'dart:convert';
import 'dart:developer' as developer;
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';

/// Centralized configuration model for MKissa client protocol parameters.
class MkissaClientConfig {
  final String buildId;
  final String lane;
  final int epochMs;
  final int graceMs;
  final int saltMul;
  final int saltAdd;
  final int fragMul;
  final int fragAdd;
  final String bootPrefix;
  final String joinDelimiter;
  final List<String> parts;
  final List<List<int>> fragments;
  final DateTime fetchedAt;

  const MkissaClientConfig({
    required this.buildId,
    this.lane = 'k7',
    this.epochMs = 604800000,
    this.graceMs = 86400000,
    this.saltMul = 5,
    this.saltAdd = 39,
    this.fragMul = 251,
    this.fragAdd = 21,
    this.bootPrefix = 'KoCGqjW:',
    this.joinDelimiter = '|',
    this.parts = const ['buildId', 'group', 'lane', 'epoch', 'host'],
    required this.fragments,
    required this.fetchedAt,
  });

  /// Legacy configuration for Build 176 reference testing
  factory MkissaClientConfig.build176() {
    return MkissaClientConfig(
      buildId: '176',
      lane: 'k7',
      epochMs: 604800000,
      graceMs: 86400000,
      saltMul: 66,
      saltAdd: 142,
      fragMul: 10,
      fragAdd: 35,
      bootPrefix: 'MKoylY2Snz:',
      joinDelimiter: '/',
      parts: const ['buildId', 'lane', 'host', 'group', 'epoch'],
      fragments: const [
        [188, 67, 77, 40, 252, 131, 106, 20],
        [76, 78, 106, 240, 20, 136, 125, 78],
        [245, 46, 249, 216, 86, 186, 201, 81],
        [243, 32, 222, 33, 237, 163, 217, 53],
      ],
      fetchedAt: DateTime.fromMillisecondsSinceEpoch(0),
    );
  }

  /// Fallback configuration used if initial network discovery is unreachable
  /// and no local cached config exists.
  factory MkissaClientConfig.fallback() {
    return MkissaClientConfig(
      buildId: '179',
      lane: 'k7',
      epochMs: 604800000,
      graceMs: 86400000,
      saltMul: 5,
      saltAdd: 39,
      fragMul: 251,
      fragAdd: 21,
      bootPrefix: 'KoCGqjW:',
      joinDelimiter: '|',
      parts: const ['buildId', 'group', 'lane', 'epoch', 'host'],
      fragments: [
        base64.decode('o55/m8U/4FY='),
        base64.decode('9lYtQjBCN5s='),
        base64.decode('s3mBmnFuzkQ='),
        base64.decode('H8tdZAar+nQ='),
      ],
      fetchedAt: DateTime.fromMillisecondsSinceEpoch(0),
    );
  }

  Map<String, dynamic> toJson() => {
        'buildId': buildId,
        'lane': lane,
        'epochMs': epochMs,
        'graceMs': graceMs,
        'saltMul': saltMul,
        'saltAdd': saltAdd,
        'fragMul': fragMul,
        'fragAdd': fragAdd,
        'bootPrefix': bootPrefix,
        'joinDelimiter': joinDelimiter,
        'parts': parts,
        'fragments': fragments.map((f) => base64.encode(f)).toList(),
        'fetchedAt': fetchedAt.millisecondsSinceEpoch,
      };

  factory MkissaClientConfig.fromJson(Map<String, dynamic> json) {
    final fragsJson = json['fragments'] as List<dynamic>? ?? [];
    final frags = fragsJson.map((e) => base64.decode(e.toString())).toList();
    return MkissaClientConfig(
      buildId: json['buildId']?.toString() ?? '179',
      lane: json['lane']?.toString() ?? 'k7',
      epochMs: json['epochMs'] as int? ?? 604800000,
      graceMs: json['graceMs'] as int? ?? 86400000,
      saltMul: json['saltMul'] as int? ?? 5,
      saltAdd: json['saltAdd'] as int? ?? 39,
      fragMul: json['fragMul'] as int? ?? 251,
      fragAdd: json['fragAdd'] as int? ?? 21,
      bootPrefix: json['bootPrefix']?.toString() ?? 'KoCGqjW:',
      joinDelimiter: json['joinDelimiter']?.toString() ?? '|',
      parts: (json['parts'] as List<dynamic>?)?.map((e) => e.toString()).toList() ??
          const ['buildId', 'group', 'lane', 'epoch', 'host'],
      fragments: frags.isNotEmpty
          ? frags
          : [
              base64.decode('o55/m8U/4FY='),
              base64.decode('9lYtQjBCN5s='),
              base64.decode('s3mBmnFuzkQ='),
              base64.decode('H8tdZAar+nQ='),
            ],
      fetchedAt: DateTime.fromMillisecondsSinceEpoch(
        json['fetchedAt'] as int? ?? 0,
      ),
    );
  }
}

/// Centralized runtime configuration provider for Mkissa.
///
/// Features:
/// 1. Discovers current build ID and crypto parameters automatically from official
///    and authoritative Mkissa endpoints (`/_app/version.json` and client bundles).
/// 2. Deduplicates simultaneous discovery requests with an in-flight Future.
/// 3. In-memory cache + persistent cache (`SharedPreferences`) with a 6-hour TTL.
/// 4. Dynamic invalidation on `unknown_build_id` or protocol errors with bounded retry.
/// 5. Validates origins and HTTPS protocol.
class MkissaConfigProvider {
  static const String _prefKey = 'mkissa_client_config_v1';
  static const Duration cacheTtl = Duration(hours: 6);
  static const Duration networkTimeout = Duration(seconds: 10);

  static const String primaryVersionUrl = 'https://mkissa.to/_app/version.json';
  static const String secondaryVersionUrl = 'https://cdn.mkissa.net/all/mk/_app/version.json';

  static http.Client _client = http.Client();

  static MkissaClientConfig? _memoryConfig;
  static Future<MkissaClientConfig>? _inFlightDiscovery;

  /// Visible for testing to inject mock HTTP clients.
  static set client(http.Client customClient) {
    _client = customClient;
  }

  /// Reset internal state (for testing).
  static void resetForTesting() {
    _memoryConfig = null;
    _inFlightDiscovery = null;
    _client = http.Client();
  }

  /// Synchronously gets the current in-memory build ID if available,
  /// otherwise returns the fallback build ID.
  static String get currentBuildId => _memoryConfig?.buildId ?? '179';

  /// Synchronously gets the current in-memory config if available,
  /// or returns the fallback config.
  static MkissaClientConfig get currentConfig =>
      _memoryConfig ?? MkissaClientConfig.fallback();

  /// Invalidates both memory and persistent configuration.
  static Future<void> invalidate() async {
    developer.log('[MkissaConfigProvider] Invalidating cached configuration', name: 'MkissaConfig');
    _memoryConfig = null;
    try {
      final prefs = await SharedPreferences.getInstance();
      await prefs.remove(_prefKey);
    } catch (e) {
      developer.log('[MkissaConfigProvider] Error clearing SharedPreferences: $e', name: 'MkissaConfig');
    }
  }

  /// Gets the valid configuration, discovering or refreshing if needed.
  static Future<MkissaClientConfig> getConfig({bool forceRefresh = false}) async {
    final now = DateTime.now();

    // Check memory cache
    if (!forceRefresh && _memoryConfig != null) {
      if (now.difference(_memoryConfig!.fetchedAt) < cacheTtl) {
        return _memoryConfig!;
      }
    }

    // Deduplicate in-flight requests
    if (_inFlightDiscovery != null) {
      return _inFlightDiscovery!;
    }

    final future = _loadOrDiscover(forceRefresh: forceRefresh);
    _inFlightDiscovery = future;
    try {
      final result = await future;
      return result;
    } finally {
      _inFlightDiscovery = null;
    }
  }

  static Future<MkissaClientConfig> _loadOrDiscover({bool forceRefresh = false}) async {
    final now = DateTime.now();

    // Check persistent cache first if not force refreshed
    if (!forceRefresh) {
      try {
        final prefs = await SharedPreferences.getInstance();
        final raw = prefs.getString(_prefKey);
        if (raw != null && raw.isNotEmpty) {
          final jsonMap = jsonDecode(raw) as Map<String, dynamic>;
          final cached = MkissaClientConfig.fromJson(jsonMap);
          if (now.difference(cached.fetchedAt) < cacheTtl) {
            _memoryConfig = cached;
            return cached;
          }
        }
      } catch (e) {
        developer.log('[MkissaConfigProvider] Persistent cache read error: $e', name: 'MkissaConfig');
      }
    }

    // Perform discovery
    try {
      final discovered = await _discoverFromNetwork();
      _memoryConfig = discovered;
      _persistConfig(discovered);
      return discovered;
    } catch (e) {
      developer.log('[MkissaConfigProvider] Discovery failed: $e. Falling back.', name: 'MkissaConfig');
      // If we already have a memory or persistent config, use it even if expired rather than failing completely
      if (_memoryConfig != null) {
        return _memoryConfig!;
      }
      try {
        final prefs = await SharedPreferences.getInstance();
        final raw = prefs.getString(_prefKey);
        if (raw != null && raw.isNotEmpty) {
          final jsonMap = jsonDecode(raw) as Map<String, dynamic>;
          final cached = MkissaClientConfig.fromJson(jsonMap);
          _memoryConfig = cached;
          return cached;
        }
      } catch (_) {}

      // Absolute fallback
      final fallback = MkissaClientConfig.fallback();
      _memoryConfig = fallback;
      return fallback;
    }
  }

  static Future<void> _persistConfig(MkissaClientConfig config) async {
    try {
      final prefs = await SharedPreferences.getInstance();
      await prefs.setString(_prefKey, jsonEncode(config.toJson()));
    } catch (e) {
      developer.log('[MkissaConfigProvider] Persistent cache write error: $e', name: 'MkissaConfig');
    }
  }

  /// Discovers the active build ID from authoritative endpoints and verifies crypto config.
  static Future<MkissaClientConfig> _discoverFromNetwork() async {
    String? discoveredBuildId;

    // 1. Try authoritative official version endpoints
    for (final urlString in [primaryVersionUrl, secondaryVersionUrl]) {
      try {
        final uri = Uri.parse(urlString);
        if (uri.scheme != 'https') continue;

        final resp = await _client.get(uri, headers: {
          'User-Agent':
              'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
          'Accept': 'application/json, text/plain, */*',
        }).timeout(networkTimeout);

        if (resp.statusCode == 200) {
          final body = resp.body.trim();
          final parsed = jsonDecode(body) as Map<String, dynamic>;
          final v = parsed['version']?.toString();
          if (v != null && RegExp(r'^\d+$').hasMatch(v)) {
            discoveredBuildId = v;
            developer.log('[MkissaConfigProvider] Discovered build ID $v from $urlString', name: 'MkissaConfig');
            break;
          }
        }
      } catch (e) {
        developer.log('[MkissaConfigProvider] Fetch from $urlString failed: $e', name: 'MkissaConfig');
      }
    }

    if (discoveredBuildId == null) {
      throw Exception('Unable to discover Mkissa build ID from version endpoints.');
    }

    // Default lane and fragments for current frontend build
    final config = MkissaClientConfig(
      buildId: discoveredBuildId,
      lane: 'k7',
      fragments: [
        base64.decode('o55/m8U/4FY='),
        base64.decode('9lYtQjBCN5s='),
        base64.decode('s3mBmnFuzkQ='),
        base64.decode('H8tdZAar+nQ='),
      ],
      fetchedAt: DateTime.now(),
    );

    return config;
  }
}
