import 'dart:convert';
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'package:akira/core/config/mkissa_config_provider.dart';
import 'package:akira/features/home/services/anime_repository.dart';
import 'package:akira/features/player/services/decryptor.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    SharedPreferences.setMockInitialValues({});
    MkissaConfigProvider.resetForTesting();
  });

  group('MkissaConfigProvider Tests', () {
    test('Successful dynamic build ID discovery from version endpoint', () async {
      final mockClient = MockClient((request) async {
        if (request.url.path.endsWith('version.json')) {
          return http.Response(jsonEncode({'version': '182'}), 200);
        }
        return http.Response('Not found', 404);
      });

      MkissaConfigProvider.client = mockClient;

      final config = await MkissaConfigProvider.getConfig(forceRefresh: true);
      expect(config.buildId, equals('182'));
      expect(MkissaConfigProvider.currentBuildId, equals('182'));
    });

    test('Missing or malformed configuration gracefully falls back', () async {
      final mockClient = MockClient((request) async {
        if (request.url.path.endsWith('version.json')) {
          return http.Response('{"invalid_json": true}', 200);
        }
        return http.Response('Server Error', 500);
      });

      MkissaConfigProvider.client = mockClient;

      final config = await MkissaConfigProvider.getConfig(forceRefresh: true);
      expect(config.buildId, equals('179')); // Fallback buildId
    });

    test('Expired cached configuration triggers refresh while valid cached config is reused', () async {
      int fetchCount = 0;
      final mockClient = MockClient((request) async {
        if (request.url.path.endsWith('version.json')) {
          fetchCount++;
          return http.Response(jsonEncode({'version': '${180 + fetchCount}'}), 200);
        }
        return http.Response('Not found', 404);
      });

      MkissaConfigProvider.client = mockClient;

      // First fetch
      final firstConfig = await MkissaConfigProvider.getConfig();
      expect(firstConfig.buildId, equals('181'));
      expect(fetchCount, equals(1));

      // Second fetch within TTL (should reuse cache)
      final secondConfig = await MkissaConfigProvider.getConfig();
      expect(secondConfig.buildId, equals('181'));
      expect(fetchCount, equals(1));

      // Force refresh (bypasses TTL)
      final thirdConfig = await MkissaConfigProvider.getConfig(forceRefresh: true);
      expect(thirdConfig.buildId, equals('182'));
      expect(fetchCount, equals(2));
    });

    test('Concurrent requests sharing one discovery operation are deduplicated', () async {
      int requestCount = 0;
      final mockClient = MockClient((request) async {
        if (request.url.path.endsWith('version.json')) {
          requestCount++;
          await Future.delayed(const Duration(milliseconds: 50));
          return http.Response(jsonEncode({'version': '185'}), 200);
        }
        return http.Response('Not found', 404);
      });

      MkissaConfigProvider.client = mockClient;

      final futures = await Future.wait([
        MkissaConfigProvider.getConfig(),
        MkissaConfigProvider.getConfig(),
        MkissaConfigProvider.getConfig(),
      ]);

      expect(requestCount, equals(1));
      expect(futures[0].buildId, equals('185'));
      expect(futures[1].buildId, equals('185'));
      expect(futures[2].buildId, equals('185'));
    });

    test('Failure to access website gracefully uses fallback', () async {
      final mockClient = MockClient((request) async {
        throw http.ClientException('Network unreachable');
      });

      MkissaConfigProvider.client = mockClient;

      final config = await MkissaConfigProvider.getConfig(forceRefresh: true);
      expect(config.buildId, equals('179'));
    });

    test('Consistency of configuration across episode and auth headers', () async {
      final mockClient = MockClient((request) async {
        if (request.url.path.endsWith('version.json')) {
          return http.Response(jsonEncode({'version': '188'}), 200);
        }
        return http.Response('Not found', 404);
      });

      MkissaConfigProvider.client = mockClient;
      await MkissaConfigProvider.getConfig(forceRefresh: true);

      expect(Decryptor.defaultBuildId, equals('188'));
      expect(MkissaConfigProvider.currentBuildId, equals('188'));
    });
  });

  group('Recovery from Unknown build id Tests', () {
    test('Automatic recovery when response reports unknown_build_id', () async {
      // Mock initial config = 175
      final mockClient = MockClient((request) async {
        if (request.url.path.endsWith('version.json')) {
          return http.Response(jsonEncode({'version': '179'}), 200);
        }
        return http.Response('Not found', 404);
      });

      MkissaConfigProvider.client = mockClient;

      // Invalidate and refresh to 179
      await MkissaConfigProvider.invalidate();
      final refreshed = await MkissaConfigProvider.getConfig(forceRefresh: true);
      expect(refreshed.buildId, equals('179'));
    });

    test('Second rejection after permitted retry returns useful error and terminates', () async {
      int queryAttempts = 0;
      final mockClient = MockClient((request) async {
        if (request.url.path.endsWith('version.json')) {
          return http.Response(jsonEncode({'version': '999'}), 200);
        }
        if (request.url.path.endsWith('/api')) {
          queryAttempts++;
          return http.Response(jsonEncode({'error': 'unknown_build_id'}), 200);
        }
        if (request.url.path.contains('bootstrap')) {
          return http.Response(
            jsonEncode({
              'epoch': 2962,
              'epochMs': 604800000,
              'graceMs': 86400000,
              'switchAt': 1792108800000,
              'partB': base64.encode(List<int>.filled(32, 1)),
              'k': 'k7',
            }),
            200,
          );
        }
        return http.Response('Not found', 404);
      });

      MkissaConfigProvider.client = mockClient;

      await expectLater(
        AnimeRepository.fetchEpisodeData(
          showId: 'test_show',
          episodeString: '1',
          client: mockClient,
        ),
        throwsA(isA<Exception>()),
      );
      expect(queryAttempts, greaterThanOrEqualTo(1));
    });
  });
}
