import 'dart:convert';
import 'dart:developer' as developer;
import 'dart:typed_data';
import 'package:crypto/crypto.dart';
import 'package:pointycastle/export.dart';
import 'package:http/http.dart' as http;

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

  factory CryptoBootstrap.fromJson(Map<String, dynamic> json, {String defaultLane = 'k7', int? currentTimeMs}) {
    final now = currentTimeMs ?? DateTime.now().millisecondsSinceEpoch;
    final epoch = json['epoch'] as int? ?? -1;
    if (epoch == -1) throw Exception("Missing 'epoch' in bootstrap response.");

    final epochMs = json['epochMs'] as int? ?? Decryptor.defaultEpochMs;
    final graceMs = json['graceMs'] as int? ?? Decryptor.defaultGraceMs;
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
  // 1. Correct MKissa build/lane configuration - single source of truth: 176 / k7
  static const String defaultBuildId = '176';
  static const String defaultLane = 'k7';
  static const String bootstrapUrl = 'https://api.mkissa.net/client-crypto/v1/bootstrap';

  static const int defaultEpochMs = 604800000;
  static const int defaultGraceMs = 86400000;

  // 3. Exact crypto constants from current MKissa web client
  static const int saltMul = 66;
  static const int saltAdd = 142;
  static const int fragMul = 10;
  static const int fragAdd = 35;
  static const String bootPrefix = 'MKoylY2Snz:';
  static const String joinDelimiter = '/';

  static const List<List<int>> fragments = [
    [188, 67, 77, 40, 252, 131, 106, 20],
    [76, 78, 106, 240, 20, 136, 125, 78],
    [245, 46, 249, 216, 86, 186, 201, 81],
    [243, 32, 222, 33, 237, 163, 217, 53],
  ];

  static CryptoBootstrap? _cachedBootstrap;

  static void invalidateBootstrapCache() {
    _cachedBootstrap = null;
  }

  static CryptoBootstrap? get cachedBootstrap => _cachedBootstrap;

  /// Reconstructs seed generation:
  /// seed[i] = buildId.charCodeAt(i % buildId.length) XOR ((i * 66 + 142) & 0xFF)
  static Uint8List deriveSeed(String buildId) {
    if (buildId.isEmpty) throw ArgumentError('buildId cannot be empty');
    final seed = Uint8List(32);
    for (int i = 0; i < 32; i++) {
      final charCode = buildId.codeUnitAt(i % buildId.length);
      seed[i] = (charCode ^ ((i * saltMul + saltAdd) & 0xFF)) & 0xFF;
    }
    return seed;
  }

  /// Reconstructs 32-byte mask generation:
  /// mask[fragmentOffset + position] = fragmentByte XOR seed[fragmentOffset + position] XOR ((lane * 10 + position * 35) & 0xFF)
  static Uint8List deriveMask(String buildId) {
    final seed = deriveSeed(buildId);
    final mask = Uint8List(32);
    for (int lane = 0; lane < 4; lane++) {
      final fragment = fragments[lane];
      final offset = lane * 8;
      for (int pos = 0; pos < 8; pos++) {
        final mix = (lane * fragMul + pos * fragAdd) & 0xFF;
        final fragmentByte = fragment[pos] & 0xFF;
        final seedByte = seed[offset + pos] & 0xFF;
        mask[offset + pos] = (fragmentByte ^ seedByte ^ mix) & 0xFF;
      }
    }
    return mask;
  }

  static int calculateCurrentEpoch([int? currentTimeMs, int epochMs = defaultEpochMs]) {
    final now = currentTimeMs ?? DateTime.now().millisecondsSinceEpoch;
    return now ~/ epochMs;
  }

  static int calculateTransitionEpoch([int? currentTimeMs, int epochMs = defaultEpochMs, int graceMs = defaultGraceMs]) {
    final now = currentTimeMs ?? DateTime.now().millisecondsSinceEpoch;
    final epoch = calculateCurrentEpoch(now, epochMs);
    if (now - epoch * epochMs < graceMs && epoch > 0) {
      return epoch - 1;
    } else {
      return epoch;
    }
  }

  /// 4. Correct key-group resolution:
  /// mkissa.to -> mkissa
  static String resolveKeyGroup(String refererHost) {
    final host = refererHost.trim().toLowerCase().replaceFirst('www.', '');
    if (host == 'mkissa.to' || host.contains('mkissa')) return 'mkissa';
    if (host.startsWith('192.168.')) return '192.168.';
    if (host == 'localhost' || host == '127.0.0.1') return 'mirror';
    return host;
  }

  /// 5. Correct x-aa-boot generation:
  /// Stage 1: HMAC-SHA256(key = derivedMask, message = "MKoylY2Snz:" + buildId)
  /// Context order: buildId / lane / host / group / epoch
  /// Stage 2: HMAC-SHA256(key = firstHmac, message = context)
  /// Return lowercase hex string.
  static String generateXAaBoot({
    required String buildId,
    required String lane,
    int epoch = 2960,
    String refererHost = 'mkissa.to',
  }) {
    final mask = deriveMask(buildId);
    final keyGroup = resolveKeyGroup(refererHost);

    // Stage 1 HMAC
    final hmac1 = Hmac(sha256, mask);
    final stage1Message = utf8.encode('$bootPrefix$buildId');
    final intermediateKey = hmac1.convert(stage1Message).bytes;

    // Stage 2 HMAC: buildId / lane / host / group / epoch
    final context = [buildId, lane, refererHost, keyGroup, epoch.toString()].join(joinDelimiter);
    final hmac2 = Hmac(sha256, intermediateKey);
    final stage2Message = utf8.encode(context);
    final finalDigest = hmac2.convert(stage2Message).bytes;

    return finalDigest.map((b) => b.toRadixString(16).padLeft(2, '0')).join();
  }

  /// 8. Correct AES key derivation:
  /// decodedPartB[0..31] XOR derivedMask[0..31] -> 32 bytes (AES-256)
  static Uint8List deriveKey(String buildId, String partBBase64) {
    final trimmed = partBBase64.trim();
    final partB = base64.decode(trimmed);
    if (partB.length < 32) {
      throw Exception('partB decoded length (${partB.length}) is less than 32 bytes.');
    }
    final mask = deriveMask(buildId);
    final keyBytes = Uint8List(32);
    for (int i = 0; i < 32; i++) {
      keyBytes[i] = (partB[i] ^ mask[i]) & 0xFF;
    }
    return keyBytes;
  }

  /// 6 & 7. Bootstrap request & cache rules:
  /// Stale once now >= switchAt (do NOT add graceMs).
  static Future<CryptoBootstrap> getBootstrap({
    String lane = defaultLane,
    String buildId = defaultBuildId,
    String? authToken,
    bool forceRefresh = false,
  }) async {
    final now = DateTime.now().millisecondsSinceEpoch;
    final cached = _cachedBootstrap;
    if (!forceRefresh && cached != null && cached.k == lane && now < cached.switchAt) {
      return cached;
    }

    final epochCandidates = [
      calculateTransitionEpoch(now),
      calculateCurrentEpoch(now),
    ];

    Exception? lastError;
    for (final candidateEpoch in epochCandidates) {
      try {
        final xAaBoot = generateXAaBoot(
          buildId: buildId,
          lane: lane,
          epoch: candidateEpoch,
        );
        final url = Uri.parse('$bootstrapUrl?buildId=$buildId&k=$lane');

        final headers = <String, String>{
          'Origin': 'https://mkissa.to',
          'Referer': 'https://mkissa.to/',
          'x-build-id': buildId,
          'x-aa-boot': xAaBoot,
        };
        if (authToken != null && authToken.trim().isNotEmpty) {
          headers['Authorization'] = authToken.startsWith('Bearer ') ? authToken : 'Bearer $authToken';
        }

        developer.log('[Decryptor] Requesting bootstrap: buildId=$buildId, lane=$lane, epoch=$candidateEpoch, xAaBoot=$xAaBoot', name: 'Decryptor');

        final response = await http.get(url, headers: headers).timeout(const Duration(seconds: 10));
        if (response.statusCode != 200) {
          throw Exception('Bootstrap HTTP ${response.statusCode}: ${response.body}');
        }

        final jsonMap = jsonDecode(response.body) as Map<String, dynamic>;
        final bootstrap = CryptoBootstrap.fromJson(jsonMap, defaultLane: lane, currentTimeMs: now);
        _cachedBootstrap = bootstrap;

        developer.log('[Decryptor] Bootstrap received: epoch=${bootstrap.epoch}, lane=${bootstrap.k}, switchAt=${bootstrap.switchAt}', name: 'Decryptor');
        return bootstrap;
      } catch (e) {
        lastError = Exception(e.toString());
      }
    }

    throw lastError ?? Exception('Bootstrap fetch failed for all candidate epochs.');
  }

  /// 9. Correct aaReq generation:
  /// Exact JSON structure & property insertion order:
  /// {
  ///   "v": 1,
  ///   "ts": `5-min aligned ts`,
  ///   "epoch": `bootstrap epoch`,
  ///   "buildId": "176",
  ///   "qh": "`queryHash`",
  ///   "k": "k7"
  /// }
  /// IV: first 12 bytes of SHA-256(epoch + ":" + buildId + ":" + queryHash + ":" + ts + ":" + lane)
  /// Encrypted AES-256-GCM (128-bit tag), byte 0 = 0x01, bytes 1..12 = IV, rest = ciphertext + tag
  static String generateAaReq({
    required String queryHash,
    required CryptoBootstrap bootstrap,
    String buildId = defaultBuildId,
    int? currentTimeMs,
  }) {
    final now = currentTimeMs ?? DateTime.now().millisecondsSinceEpoch;
    final ts = (now ~/ 300000) * 300000;
    final epoch = bootstrap.epoch;

    final ivSource = '$epoch:$buildId:$queryHash:$ts:${bootstrap.k}';
    final fullHash = sha256.convert(utf8.encode(ivSource)).bytes;
    final iv = Uint8List.fromList(fullHash.sublist(0, 12));

    // LinkedHashMap preserves exact insertion order: v, ts, epoch, buildId, qh, k
    final payloadMap = <String, dynamic>{
      'v': 1,
      'ts': ts,
      'epoch': epoch,
      'buildId': buildId,
      'qh': queryHash,
      'k': bootstrap.k,
    };
    final plaintextJson = jsonEncode(payloadMap);

    final keyBytes = deriveKey(buildId, bootstrap.partB);

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
    developer.log('[Decryptor] Generated aaReq (length ${finalAaReq.length}): buildId=$buildId, epoch=$epoch, ts=$ts, lane=${bootstrap.k}', name: 'Decryptor');
    return finalAaReq;
  }

  /// 13. Correct AES-256-GCM decryption:
  /// Base64 decode -> byte 0 == 0x01, bytes 1..12 == IV, remaining == ciphertext + 16-byte GCM tag
  static String decrypt({
    required String payload,
    required String partBBase64,
    String buildId = defaultBuildId,
  }) {
    final trimmed = payload.trim();
    if (trimmed.isEmpty) throw ArgumentError('Payload cannot be empty');

    final raw = base64.decode(trimmed);
    if (raw.isEmpty) throw Exception('Decoded payload is empty');
    if (raw[0] != 1) throw Exception('Unsupported payload version (${raw[0]}). Only version 1 is supported.');
    if (raw.length < 1 + 12 + 16) throw Exception('Payload buffer too short (${raw.length} bytes).');

    final iv = Uint8List.fromList(raw.sublist(1, 13));
    final encryptedWithTag = Uint8List.fromList(raw.sublist(13));

    final keyBytes = deriveKey(buildId, partBBase64);

    final cipher = GCMBlockCipher(AESEngine());
    final params = AEADParameters(KeyParameter(keyBytes), 128, iv, Uint8List(0));
    cipher.init(false, params);

    final plaintextBytes = cipher.process(encryptedWithTag);
    return utf8.decode(plaintextBytes);
  }
}
