import 'dart:convert';
import 'dart:developer' as developer;
import 'dart:typed_data';
import 'package:crypto/crypto.dart';
import 'package:pointycastle/export.dart';
import 'package:http/http.dart' as http;
import '../../../core/config/mkissa_config_provider.dart';

class CryptoBootstrap {
  final int epoch;
  final int epochMs;
  final int graceMs;
  final int switchAt;
  final String partB;
  final String k;

  const CryptoBootstrap({
    required this.epoch,
    required this.epochMs,
    required this.graceMs,
    required this.switchAt,
    required this.partB,
    required this.k,
  });

  factory CryptoBootstrap.fromJson(
    Map<String, dynamic> json, {
    String defaultLane = 'k7',
    int? currentTimeMs,
    MkissaClientConfig? config,
  }) {
    final effectiveConfig = config ?? MkissaConfigProvider.currentConfig;
    final now = currentTimeMs ?? DateTime.now().millisecondsSinceEpoch;
    final epoch = json['epoch'] as int? ?? -1;
    if (epoch == -1) throw Exception("Missing 'epoch' in bootstrap response.");

    final epochMs = json['epochMs'] as int? ?? effectiveConfig.epochMs;
    final graceMs = json['graceMs'] as int? ?? effectiveConfig.graceMs;
    final switchAt = json['switchAt'] as int? ?? (now + epochMs);
    final partB = json['partB']?.toString() ?? '';
    if (partB.trim().isEmpty) throw Exception("Missing or blank 'partB' in bootstrap response.");

    final k = json['k']?.toString() ?? defaultLane;

    return CryptoBootstrap(
      epoch: epoch,
      epochMs: epochMs,
      graceMs: graceMs,
      switchAt: switchAt,
      partB: partB,
      k: k,
    );
  }
}

class Decryptor {
  static const String bootstrapUrl = 'https://api.mkissa.net/client-crypto/v1/bootstrap';

  static CryptoBootstrap? _cachedBootstrap;

  /// Default build ID dynamically queried from the provider.
  static String get defaultBuildId => MkissaConfigProvider.currentBuildId;

  /// Default lane dynamically queried from current config.
  static String get defaultLane => MkissaConfigProvider.currentConfig.lane;

  static void invalidateBootstrapCache() {
    _cachedBootstrap = null;
  }

  static CryptoBootstrap? get cachedBootstrap => _cachedBootstrap;

  /// Reconstructs seed generation:
  /// seed[i] = buildId.charCodeAt(i % buildId.length) XOR ((i * saltMul + saltAdd) & 0xFF)
  static Uint8List deriveSeed(String buildId, {MkissaClientConfig? config}) {
    if (buildId.isEmpty) throw ArgumentError('buildId cannot be empty');
    final cfg = config ?? MkissaConfigProvider.currentConfig;
    final seed = Uint8List(32);
    for (int i = 0; i < 32; i++) {
      final charCode = buildId.codeUnitAt(i % buildId.length);
      seed[i] = (charCode ^ ((i * cfg.saltMul + cfg.saltAdd) & 0xFF)) & 0xFF;
    }
    return seed;
  }

  /// Reconstructs 32-byte mask generation:
  /// mask[fragmentOffset + position] = fragmentByte XOR seed[fragmentOffset + position] XOR ((lane * fragMul + position * fragAdd) & 0xFF)
  static Uint8List deriveMask(String buildId, {MkissaClientConfig? config}) {
    final cfg = config ?? MkissaConfigProvider.currentConfig;
    final seed = deriveSeed(buildId, config: cfg);
    final mask = Uint8List(32);
    final frags = cfg.fragments;
    for (int lane = 0; lane < 4; lane++) {
      final fragment = frags[lane];
      final offset = lane * 8;
      for (int pos = 0; pos < 8; pos++) {
        final mix = (lane * cfg.fragMul + pos * cfg.fragAdd) & 0xFF;
        final fragmentByte = fragment[pos] & 0xFF;
        final seedByte = seed[offset + pos] & 0xFF;
        mask[offset + pos] = (fragmentByte ^ seedByte ^ mix) & 0xFF;
      }
    }
    return mask;
  }

  static int calculateCurrentEpoch([int? currentTimeMs, int? epochMs]) {
    final effectiveEpochMs = epochMs ?? MkissaConfigProvider.currentConfig.epochMs;
    final now = currentTimeMs ?? DateTime.now().millisecondsSinceEpoch;
    return now ~/ effectiveEpochMs;
  }

  static int calculateTransitionEpoch([int? currentTimeMs, int? epochMs, int? graceMs]) {
    final cfg = MkissaConfigProvider.currentConfig;
    final effectiveEpochMs = epochMs ?? cfg.epochMs;
    final effectiveGraceMs = graceMs ?? cfg.graceMs;
    final now = currentTimeMs ?? DateTime.now().millisecondsSinceEpoch;
    final epoch = calculateCurrentEpoch(now, effectiveEpochMs);
    if (now - epoch * effectiveEpochMs < effectiveGraceMs && epoch > 0) {
      return epoch - 1;
    } else {
      return epoch;
    }
  }

  /// Key-group resolution:
  /// mkissa.to -> mkissa
  static String resolveKeyGroup(String refererHost) {
    final host = refererHost.trim().toLowerCase().replaceFirst('www.', '');
    if (host == 'mkissa.to' || host.contains('mkissa')) return 'mkissa';
    if (host.startsWith('192.168.')) return '192.168.';
    if (host == 'localhost' || host == '127.0.0.1') return 'mirror';
    return host;
  }

  /// x-aa-boot generation:
  /// Stage 1: HMAC-SHA256(key = derivedMask, message = bootPrefix + buildId)
  /// Dynamic Context matching `cfg.parts` joined by `cfg.joinDelimiter`
  /// Stage 2: HMAC-SHA256(key = firstHmac, message = context)
  /// Return lowercase hex string.
  static String generateXAaBoot({
    required String buildId,
    required String lane,
    int? epoch,
    String refererHost = 'mkissa.to',
    MkissaClientConfig? config,
  }) {
    final cfg = config ?? MkissaConfigProvider.currentConfig;
    final mask = deriveMask(buildId, config: cfg);
    final keyGroup = resolveKeyGroup(refererHost);
    final currentEpoch = epoch ?? calculateCurrentEpoch();

    // Stage 1 HMAC
    final hmac1 = Hmac(sha256, mask);
    final stage1Message = utf8.encode('${cfg.bootPrefix}$buildId');
    final intermediateKey = hmac1.convert(stage1Message).bytes;

    // Stage 2 HMAC: order of parts dynamically constructed
    final partValues = <String, String>{
      'buildId': buildId,
      'group': keyGroup,
      'lane': lane,
      'epoch': currentEpoch.toString(),
      'host': refererHost,
    };
    final context = cfg.parts.map((p) => partValues[p] ?? '').join(cfg.joinDelimiter);

    final hmac2 = Hmac(sha256, intermediateKey);
    final stage2Message = utf8.encode(context);
    final finalDigest = hmac2.convert(stage2Message).bytes;

    return finalDigest.map((b) => b.toRadixString(16).padLeft(2, '0')).join();
  }

  /// AES key derivation:
  /// decodedPartB[0..31] XOR derivedMask[0..31] -> 32 bytes (AES-256)
  static Uint8List deriveKey(String buildId, String partBBase64, {MkissaClientConfig? config}) {
    final trimmed = partBBase64.trim();
    final partB = base64.decode(trimmed);
    if (partB.length < 32) {
      throw Exception('partB decoded length (${partB.length}) is less than 32 bytes.');
    }
    final mask = deriveMask(buildId, config: config);
    final keyBytes = Uint8List(32);
    for (int i = 0; i < 32; i++) {
      keyBytes[i] = (partB[i] ^ mask[i]) & 0xFF;
    }
    return keyBytes;
  }

  /// Bootstrap request & cache rules:
  /// Stale once now >= switchAt (do NOT add graceMs).
  static Future<CryptoBootstrap> getBootstrap({
    String? lane,
    String? buildId,
    String? authToken,
    bool forceRefresh = false,
    MkissaClientConfig? config,
    http.Client? httpClient,
  }) async {
    final effectiveConfig = config ?? await MkissaConfigProvider.getConfig(forceRefresh: forceRefresh);
    final effectiveLane = lane ?? effectiveConfig.lane;
    final effectiveBuildId = buildId ?? effectiveConfig.buildId;
    final client = httpClient ?? http.Client();

    final now = DateTime.now().millisecondsSinceEpoch;
    final cached = _cachedBootstrap;
    if (!forceRefresh && cached != null && cached.k == effectiveLane && now < cached.switchAt) {
      return cached;
    }

    final epochCandidates = [
      calculateTransitionEpoch(now, effectiveConfig.epochMs, effectiveConfig.graceMs),
      calculateCurrentEpoch(now, effectiveConfig.epochMs),
    ];

    Exception? lastError;
    for (final candidateEpoch in epochCandidates) {
      try {
        final xAaBoot = generateXAaBoot(
          buildId: effectiveBuildId,
          lane: effectiveLane,
          epoch: candidateEpoch,
          config: effectiveConfig,
        );
        final url = Uri.parse('$bootstrapUrl?buildId=$effectiveBuildId&k=$effectiveLane');

        final headers = <String, String>{
          'Origin': 'https://mkissa.to',
          'Referer': 'https://mkissa.to/',
          'x-build-id': effectiveBuildId,
          'x-aa-boot': xAaBoot,
        };
        if (authToken != null && authToken.trim().isNotEmpty) {
          headers['Authorization'] = authToken.startsWith('Bearer ') ? authToken : 'Bearer $authToken';
        }

        developer.log(
          '[Decryptor] Requesting bootstrap: buildId=$effectiveBuildId, lane=$effectiveLane, epoch=$candidateEpoch, xAaBoot=$xAaBoot',
          name: 'Decryptor',
        );

        final response = await client.get(url, headers: headers).timeout(const Duration(seconds: 10));
        if (response.statusCode != 200) {
          throw Exception('Bootstrap HTTP ${response.statusCode}: ${response.body}');
        }

        final jsonMap = jsonDecode(response.body) as Map<String, dynamic>;
        final bootstrap = CryptoBootstrap.fromJson(
          jsonMap,
          defaultLane: effectiveLane,
          currentTimeMs: now,
          config: effectiveConfig,
        );
        _cachedBootstrap = bootstrap;

        developer.log(
          '[Decryptor] Bootstrap received: epoch=${bootstrap.epoch}, lane=${bootstrap.k}, switchAt=${bootstrap.switchAt}',
          name: 'Decryptor',
        );
        return bootstrap;
      } catch (e) {
        lastError = Exception(e.toString());
      }
    }

    throw lastError ?? Exception('Bootstrap fetch failed for all candidate epochs.');
  }

  /// aaReq generation:
  /// Exact JSON structure & property insertion order:
  /// {
  ///   "v": 1,
  ///   "ts": `5-min aligned ts`,
  ///   "epoch": `bootstrap epoch`,
  ///   "buildId": ...,
  ///   "qh": "`queryHash`",
  ///   "k": "k7"
  /// }
  /// IV: first 12 bytes of SHA-256(epoch + ":" + buildId + ":" + queryHash + ":" + ts + ":" + lane)
  /// Encrypted AES-256-GCM (128-bit tag), byte 0 = 0x01, bytes 1..12 = IV, rest = ciphertext + tag
  static String generateAaReq({
    required String queryHash,
    required CryptoBootstrap bootstrap,
    String? buildId,
    int? currentTimeMs,
    MkissaClientConfig? config,
  }) {
    final effectiveBuildId = buildId ?? MkissaConfigProvider.currentBuildId;
    final now = currentTimeMs ?? DateTime.now().millisecondsSinceEpoch;
    final ts = (now ~/ 300000) * 300000;
    final epoch = bootstrap.epoch;

    final ivSource = '$epoch:$effectiveBuildId:$queryHash:$ts:${bootstrap.k}';
    final fullHash = sha256.convert(utf8.encode(ivSource)).bytes;
    final iv = Uint8List.fromList(fullHash.sublist(0, 12));

    // LinkedHashMap preserves exact insertion order: v, ts, epoch, buildId, qh, k
    final payloadMap = <String, dynamic>{
      'v': 1,
      'ts': ts,
      'epoch': epoch,
      'buildId': effectiveBuildId,
      'qh': queryHash,
      'k': bootstrap.k,
    };
    final plaintextJson = jsonEncode(payloadMap);

    final keyBytes = deriveKey(effectiveBuildId, bootstrap.partB, config: config);

    // Encrypt AES-256-GCM (32-byte key, 12-byte IV, 128-bit / 16-byte tag, no AAD)
    final cipher = GCMBlockCipher(AESEngine());
    final params = AEADParameters(KeyParameter(keyBytes), 128, iv, Uint8List(0));
    cipher.init(true, params);

    final input = Uint8List.fromList(utf8.encode(plaintextJson));
    final encryptedWithTag = cipher.process(input);

    final output = Uint8List(1 + 12 + encryptedWithTag.length);
    output[0] = 0x01;
    output.setRange(1, 13, iv);
    output.setRange(13, output.length, encryptedWithTag);

    final finalAaReq = base64.encode(output);
    developer.log(
      '[Decryptor] Generated aaReq (length ${finalAaReq.length}): buildId=$effectiveBuildId, epoch=$epoch, ts=$ts, lane=${bootstrap.k}',
      name: 'Decryptor',
    );
    return finalAaReq;
  }

  /// AES-256-GCM decryption:
  /// Base64 decode -> byte 0 == 0x01, bytes 1..12 == IV, remaining == ciphertext + 16-byte GCM tag
  static String decrypt({
    required String payload,
    required String partBBase64,
    String? buildId,
    MkissaClientConfig? config,
  }) {
    final effectiveBuildId = buildId ?? MkissaConfigProvider.currentBuildId;
    final trimmed = payload.trim();
    if (trimmed.isEmpty) throw ArgumentError('Payload cannot be empty');

    final raw = base64.decode(trimmed);
    if (raw.isEmpty) throw Exception('Decoded payload is empty');
    if (raw[0] != 1) throw Exception('Unsupported payload version (${raw[0]}). Only version 1 is supported.');
    if (raw.length < 1 + 12 + 16) throw Exception('Payload buffer too short (${raw.length} bytes).');

    final iv = Uint8List.fromList(raw.sublist(1, 13));
    final encryptedWithTag = Uint8List.fromList(raw.sublist(13));

    final keyBytes = deriveKey(effectiveBuildId, partBBase64, config: config);

    final cipher = GCMBlockCipher(AESEngine());
    final params = AEADParameters(KeyParameter(keyBytes), 128, iv, Uint8List(0));
    cipher.init(false, params);

    final plaintextBytes = cipher.process(encryptedWithTag);
    return utf8.decode(plaintextBytes);
  }
}
