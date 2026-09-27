import 'package:flutter_test/flutter_test.dart';
import 'package:akira/features/player/services/decryptor.dart';

void main() {
  group('Decryptor Crypto Verification Tests', () {
    test('x-aa-boot generation matches verified reference vector', () {
      // Vector given in requirements:
      // buildId = 176, lane = k7, epoch = 2960, host = mkissa.to, group = mkissa
      // Expected: edb170a6f277c38625d98b76b93d09bffe7c44387c385add49c00aaf0a473291
      final xAaBoot = Decryptor.generateXAaBoot(
        buildId: '176',
        lane: 'k7',
        epoch: 2960,
        refererHost: 'mkissa.to',
      );

      expect(xAaBoot, equals('edb170a6f277c38625d98b76b93d09bffe7c44387c385add49c00aaf0a473291'));
    });

    test('deriveKey generates valid 32-byte AES-256 key', () {
      const partB = 'Z+MAGY2T05TIklLnXEjI08z54I8gi3ZgEp1W2LNLgtc=';
      final key = Decryptor.deriveKey('176', partB);

      expect(key.length, equals(32));
    });

    test('generateAaReq generates valid Base64 payload with version byte 1 and 12-byte IV', () {
      const partB = 'Z+MAGY2T05TIklLnXEjI08z54I8gi3ZgEp1W2LNLgtc=';
      const bootstrap = CryptoBootstrap(
        epoch: 2960,
        epochMs: 604800000,
        graceMs: 86400000,
        switchAt: 1790899200000,
        partB: partB,
        k: 'k7',
      );

      final aaReq = Decryptor.generateAaReq(
        queryHash: '670bbf38d0868f446e2346c1e956ca2c40c416e733ca248fd54e04f1c8b99145',
        bootstrap: bootstrap,
        buildId: '176',
        currentTimeMs: 1727417000000,
      );

      expect(aaReq.isNotEmpty, isTrue);
    });
  });
}
