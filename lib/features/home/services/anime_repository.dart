import 'dart:convert';
import 'dart:developer' as developer;
import 'package:http/http.dart' as http;
import '../../player/models/stream_source.dart';
import '../../player/services/decryptor.dart';
import '../../player/services/stream_parser.dart';
import '../models/anime_detail.dart';
import '../models/anime_show.dart';

class AnimeRepository {
  static const String baseUrl = 'https://api.mkissa.net/api';

  // SHA256 Hashes extracted from web client queries
  static const String browsePersistedQueryHash =
      '8b319a0fda488e4319f1b6d99093ba12802f3fc039572f39bcc02d5f9b9d9b02';
  static const String topRankedPersistedQueryHash =
      'c947693e2a04dfa9074df5ec01c6c5209fc9f9556f3e5d420dbca97f9d1b6d98';
  static const String communityPersistedQueryHash =
      '445e9bee7decbefb88df367d543174704a4dd5ee1259642d61e95baaa637d5b3';
  static const String detailPersistedQueryHash =
      'c6c067496f962fba87c7aaf6c215a40a6d3933c69012bd15e4d8949e58f3c010';
  static const String episodePersistedQueryHash =
      '670bbf38d0868f446e2346c1e956ca2c40c416e733ca248fd54e04f1c8b99145';

  // Persistent HTTP client reusing socket connections (avoids TLS handshake overhead)
  static final http.Client _client = http.Client();

  // In-memory cache for ultra-fast instant tab switching and zero lag
  static final Map<String, AnimePageResult> _cache = {};
  static final Map<String, AnimeDetail> _detailCache = {};
  static List<AnimeShow>? _topRankedCache;

  static Map<String, String> get _headers => {
        'User-Agent':
            'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/153.0.0.0 Safari/537.36 Edg/153.0.0.0',
        'Accept': '*/*',
        'Accept-Language': 'en-US,en;q=0.9',
        'Referer': 'https://mkissa.to/',
        'Origin': 'https://mkissa.to',
        'x-build-id': '176',
        'Sec-Fetch-Dest': 'empty',
        'Sec-Fetch-Mode': 'cors',
        'Sec-Fetch-Site': 'cross-site',
      };

  /// Fetches paginated anime list or search results.
  /// Supports listProfile ('browse'), sortBy ('Top', 'Popular', 'Latest_Update', 'Release_Year', etc.),
  /// translationType ('sub', 'dub'), and countryOrigin ('ALL', 'JP', 'KR', 'CN').
  static Future<AnimePageResult> fetchAnimeList({
    int page = 1,
    int limit = 26,
    String translationType = 'sub',
    String listProfile = 'browse',
    String? searchQuery,
    String? sortBy,
    String? countryOrigin,
    String? season,
    int? seasonYear,
  }) async {
    final searchObj = <String, dynamic>{
      'listProfile': listProfile,
    };

    final queryTrimmed = searchQuery?.trim();
    if (queryTrimmed != null && queryTrimmed.isNotEmpty) {
      searchObj['query'] = queryTrimmed;
      searchObj['sortBy'] = sortBy ?? 'Top';
    } else {
      if (sortBy != null && sortBy.isNotEmpty) {
        searchObj['sortBy'] = sortBy;
      }
      if (season != null && season.isNotEmpty) {
        searchObj['season'] = season;
      }
      if (seasonYear != null && seasonYear > 0) {
        searchObj['year'] = seasonYear;
      }
    }

    final variablesObj = <String, dynamic>{
      'search': searchObj,
      'limit': limit,
      'page': page,
      'translationType': translationType,
    };

    if (countryOrigin != null && countryOrigin.isNotEmpty && countryOrigin != 'ALL') {
      variablesObj['countryOrigin'] = countryOrigin;
    }

    final extensionsObj = <String, dynamic>{
      'persistedQuery': {
        'version': 1,
        'sha256Hash': browsePersistedQueryHash,
      },
    };

    final encodedVariables = Uri.encodeComponent(jsonEncode(variablesObj));
    final encodedExtensions = Uri.encodeComponent(jsonEncode(extensionsObj));
    final fullUrl = '$baseUrl?variables=$encodedVariables&extensions=$encodedExtensions';
    final cacheKey = fullUrl;
    if (page == 1 && (searchQuery == null || searchQuery.isEmpty)) {
      if (_cache.containsKey(cacheKey)) {
        return _cache[cacheKey]!;
      }
    }

    final response = await _client
        .get(Uri.parse(fullUrl), headers: _headers)
        .timeout(const Duration(seconds: 15));

    if (response.statusCode < 200 || response.statusCode >= 300) {
      throw Exception('Server error: HTTP ${response.statusCode}');
    }

    final result = _parseAnimeResponse(response.body, page);
    if (page == 1 && (searchQuery == null || searchQuery.isEmpty)) {
      _cache[cacheKey] = result;
    }
    return result;
  }

  /// Fetches the Trending / Top Ranked anime carousel data
  /// dateRange: 1 (Today), 7 (This Week), 30 (This Month)
  static Future<List<AnimeShow>> fetchTopRankedAnime({
    int dateRange = 1,
    int size = 15,
  }) async {
    if (_topRankedCache != null && _topRankedCache!.isNotEmpty) {
      return _topRankedCache!;
    }

    final variablesObj = <String, dynamic>{
      'type': 'anime',
      'size': size,
      'dateRange': dateRange,
      'page': 1,
      'allowAdult': false,
      'allowUnknown': false,
    };

    final extensionsObj = <String, dynamic>{
      'persistedQuery': {
        'version': 1,
        'sha256Hash': topRankedPersistedQueryHash,
      },
    };

    final encodedVariables = Uri.encodeComponent(jsonEncode(variablesObj));
    final encodedExtensions = Uri.encodeComponent(jsonEncode(extensionsObj));
    final fullUrl = '$baseUrl?variables=$encodedVariables&extensions=$encodedExtensions';

    final response = await _client
        .get(Uri.parse(fullUrl), headers: _headers)
        .timeout(const Duration(seconds: 15));

    if (response.statusCode < 200 || response.statusCode >= 300) {
      throw Exception('Server error: HTTP ${response.statusCode}');
    }

    final dynamic decoded = jsonDecode(response.body);
    final data = decoded['data'] as Map<String, dynamic>?;
    final top10Obj = data?['top10'] as Map<String, dynamic>?;
    final edges = top10Obj?['edges'] as List<dynamic>? ?? [];

    final list = <AnimeShow>[];
    for (final item in edges) {
      if (item is Map<String, dynamic>) {
        list.add(AnimeShow.fromRankedCard(item));
      }
    }
    _topRankedCache = list;
    return list;
  }

  static void clearCache() {
    _cache.clear();
    _topRankedCache = null;
    _detailCache.clear();
  }

  /// Fetches comprehensive details for a specific anime by ID
  static Future<AnimeDetail> fetchAnimeDetail(String animeId) async {
    if (_detailCache.containsKey(animeId)) {
      return _detailCache[animeId]!;
    }

    final variablesObj = <String, dynamic>{
      '_id': animeId,
      'search': {
        'allowAdult': false,
        'allowUnknown': false,
        'denyEcchi': false,
        'lite': false,
        'forMe': false,
      },
    };

    final extensionsObj = <String, dynamic>{
      'persistedQuery': {
        'version': 1,
        'sha256Hash': detailPersistedQueryHash,
      },
    };

    final encodedVariables = Uri.encodeComponent(jsonEncode(variablesObj));
    final encodedExtensions = Uri.encodeComponent(jsonEncode(extensionsObj));
    final fullUrl = '$baseUrl?variables=$encodedVariables&extensions=$encodedExtensions';

    final response = await _client
        .get(Uri.parse(fullUrl), headers: _headers)
        .timeout(const Duration(seconds: 15));

    if (response.statusCode < 200 || response.statusCode >= 300) {
      throw Exception('Server error: HTTP ${response.statusCode}');
    }

    final dynamic decoded = jsonDecode(response.body);
    if (decoded is! Map<String, dynamic>) {
      throw Exception('Invalid response format received from server.');
    }

    if (decoded.containsKey('errors')) {
      final errors = decoded['errors'] as List<dynamic>?;
      if (errors != null && errors.isNotEmpty) {
        final firstError = errors[0] as Map<String, dynamic>?;
        final msg = firstError?['message']?.toString() ?? 'Failed to load anime details';
        throw Exception(msg);
      }
    }

    final data = decoded['data'] as Map<String, dynamic>?;
    final showMap = data?['show'] as Map<String, dynamic>? ??
        data?['anime'] as Map<String, dynamic>?;

    if (showMap == null) {
      throw Exception('Anime details not found.');
    }

    final detail = AnimeDetail.fromJson(showMap);
    _detailCache[animeId] = detail;
    return detail;
  }

  /// Fetches community recommended rail (top voted picks)
  static Future<List<AnimeShow>> fetchCommunityPicks({int size = 15}) async {
    final variablesObj = <String, dynamic>{
      'size': size,
      'allowAdult': false,
      'denyEcchi': false,
    };

    final extensionsObj = <String, dynamic>{
      'persistedQuery': {
        'version': 1,
        'sha256Hash': communityPersistedQueryHash,
      },
    };

    final encodedVariables = Uri.encodeComponent(jsonEncode(variablesObj));
    final encodedExtensions = Uri.encodeComponent(jsonEncode(extensionsObj));
    final fullUrl = '$baseUrl?variables=$encodedVariables&extensions=$encodedExtensions';

    final response = await _client
        .get(Uri.parse(fullUrl), headers: _headers)
        .timeout(const Duration(seconds: 20));

    if (response.statusCode < 200 || response.statusCode >= 300) {
      return [];
    }

    final dynamic decoded = jsonDecode(response.body);
    final data = decoded['data'] as Map<String, dynamic>?;
    final rail = data?['communityRecommendRailUpdate'] as Map<String, dynamic>?;
    final edges = rail?['edges'] as List<dynamic>? ?? [];

    final list = <AnimeShow>[];
    for (final item in edges) {
      if (item is Map<String, dynamic> && item['card'] is Map<String, dynamic>) {
        list.add(AnimeShow.fromJson(item['card'] as Map<String, dynamic>));
      }
    }
    return list;
  }

  static AnimePageResult _parseAnimeResponse(String rawResponse, int page) {
    final dynamic decoded = jsonDecode(rawResponse);
    if (decoded is! Map<String, dynamic>) {
      throw Exception('Invalid response format received from server.');
    }

    if (decoded.containsKey('errors')) {
      final errors = decoded['errors'] as List<dynamic>?;
      if (errors != null && errors.isNotEmpty) {
        final firstError = errors[0] as Map<String, dynamic>?;
        final msg = firstError?['message']?.toString() ?? 'API query error';
        throw Exception(msg);
      }
    }

    final data = decoded['data'] as Map<String, dynamic>?;
    if (data == null) {
      throw Exception("Response missing 'data' object.");
    }

    final shows = data['shows'] as Map<String, dynamic>?;
    if (shows == null) {
      throw Exception("Response missing 'shows' object.");
    }

    final pageInfo = shows['pageInfo'] as Map<String, dynamic>?;
    final total = (pageInfo?['total'] as num?)?.toInt() ?? 0;

    final edges = shows['edges'] as List<dynamic>? ?? [];
    final showList = <AnimeShow>[];

    for (final edge in edges) {
      if (edge is Map<String, dynamic>) {
        showList.add(AnimeShow.fromJson(edge));
      }
    }

    return AnimePageResult(
      shows: showList,
      total: total,
      page: page,
    );
  }

  /// Fetches raw episode payload and authoritative CryptoBootstrap
  static Future<Map<String, dynamic>> fetchEpisodeData({
    required String showId,
    required String translationType,
    required String episodeString,
    String? authToken,
    String k = Decryptor.defaultLane,
    String? aaReq,
  }) async {
    assert(showId.isNotEmpty, 'Show ID cannot be empty.');
    assert(episodeString.isNotEmpty, 'Episode string cannot be empty.');

    Future<String> executeEpisodeQuery(CryptoBootstrap bootstrap, [String? explicitAaReq]) async {
      final effectiveAaReq = explicitAaReq ??
          aaReq ??
          Decryptor.generateAaReq(
            queryHash: episodePersistedQueryHash,
            bootstrap: bootstrap,
            buildId: Decryptor.defaultBuildId,
          );

      final variablesObj = <String, dynamic>{
        'showId': showId,
        'translationType': translationType.toLowerCase(),
        'episodeString': episodeString,
      };

      final extensionsObj = <String, dynamic>{
        'persistedQuery': {
          'version': 1,
          'sha256Hash': episodePersistedQueryHash,
        },
        'k': k,
        'aaReq': effectiveAaReq,
      };

      final encodedVariables = Uri.encodeComponent(jsonEncode(variablesObj));
      final encodedExtensions = Uri.encodeComponent(jsonEncode(extensionsObj));
      final fullUrl = '$baseUrl?variables=$encodedVariables&extensions=$encodedExtensions';

      final headers = Map<String, String>.from(_headers);
      if (authToken != null && authToken.trim().isNotEmpty) {
        headers['authorization'] =
            authToken.startsWith('Bearer ') ? authToken : 'Bearer $authToken';
      }

      developer.log('[AnimeRepository] Requesting episode: showId=$showId, ep=$episodeString, type=$translationType, buildId=${Decryptor.defaultBuildId}, lane=$k, epoch=${bootstrap.epoch}, qh=$episodePersistedQueryHash', name: 'AnimeRepository');

      final response = await _client
          .get(Uri.parse(fullUrl), headers: headers)
          .timeout(const Duration(seconds: 15));

      if (response.statusCode < 200 || response.statusCode >= 300) {
        throw Exception('API error HTTP ${response.statusCode}: ${response.body}');
      }
      return response.body;
    }

    // Step 1: Obtain authoritative bootstrap
    var bootstrap = await Decryptor.getBootstrap(lane: k, authToken: authToken);
    var responseStr = await executeEpisodeQuery(bootstrap);

    // Step 2: Handle AA_CRYPTO errors robustly
    bool checkCryptoError(String resp) {
      final upper = resp.toUpperCase();
      return upper.contains('AA_CRYPTO_STALE') ||
          upper.contains('AA_CRYPTO_EXPIRED') ||
          upper.contains('AA_CRYPTO_MISSING') ||
          upper.contains('AA_CRYPTO_MISSING_BUILD') ||
          upper.contains('AA_CRYPTO_MISSING_LANE') ||
          upper.contains('AA_CRYPTO_LANE_MISMATCH') ||
          upper.contains('AA_CRYPTO_QUERY_MISMATCH') ||
          upper.contains('AA_CRYPTO_BUILD_MISMATCH') ||
          upper.contains('AACRYPTO EXPIRED') ||
          upper.contains('AACRYPTO_EXPIRED');
    }

    if (checkCryptoError(responseStr)) {
      developer.log('[AnimeRepository] AA_CRYPTO error in episode response. Invalidating bootstrap cache and retrying with fresh bootstrap...', name: 'AnimeRepository');
      Decryptor.invalidateBootstrapCache();
      
      // Try with forceRefresh
      try {
        bootstrap = await Decryptor.getBootstrap(lane: k, authToken: authToken, forceRefresh: true);
        final freshAaReq = Decryptor.generateAaReq(
          queryHash: episodePersistedQueryHash,
          bootstrap: bootstrap,
          buildId: Decryptor.defaultBuildId,
        );
        responseStr = await executeEpisodeQuery(bootstrap, freshAaReq);
      } catch (e) {
        developer.log('[AnimeRepository] Retry failed: $e', name: 'AnimeRepository');
      }

      // If still error, try alternate candidate epochs
      if (checkCryptoError(responseStr)) {
        final now = DateTime.now().millisecondsSinceEpoch;
        final epochsToTry = [
          Decryptor.calculateCurrentEpoch(now),
          Decryptor.calculateTransitionEpoch(now),
          Decryptor.calculateCurrentEpoch(now) - 1,
          Decryptor.calculateCurrentEpoch(now) + 1,
        ];

        for (final ep in epochsToTry) {
          try {
            final xAaBoot = Decryptor.generateXAaBoot(
              buildId: Decryptor.defaultBuildId,
              lane: k,
              epoch: ep,
            );
            final url = Uri.parse('${Decryptor.bootstrapUrl}?buildId=${Decryptor.defaultBuildId}&k=$k');
            final headers = <String, String>{
              'Origin': 'https://mkissa.to',
              'Referer': 'https://mkissa.to/',
              'x-build-id': Decryptor.defaultBuildId,
              'x-aa-boot': xAaBoot,
            };
            if (authToken != null && authToken.trim().isNotEmpty) {
              headers['Authorization'] = authToken.startsWith('Bearer ') ? authToken : 'Bearer $authToken';
            }
            final bResp = await http.get(url, headers: headers).timeout(const Duration(seconds: 8));
            if (bResp.statusCode == 200) {
              final jsonMap = jsonDecode(bResp.body) as Map<String, dynamic>;
              final freshBoot = CryptoBootstrap.fromJson(jsonMap, defaultLane: k, currentTimeMs: now);
              final freshAaReq = Decryptor.generateAaReq(
                queryHash: episodePersistedQueryHash,
                bootstrap: freshBoot,
                buildId: Decryptor.defaultBuildId,
              );
              final altResp = await executeEpisodeQuery(freshBoot, freshAaReq);
              if (!checkCryptoError(altResp)) {
                bootstrap = freshBoot;
                responseStr = altResp;
                break;
              }
            }
          } catch (_) {}
        }
      }
    }

    return {
      'response': responseStr,
      'bootstrap': bootstrap,
    };
  }

  static Future<EpisodeFetchResult> fetchEpisodeStreams({
    required String showId,
    required String episodeString,
    String translationType = 'sub',
    String? authToken,
  }) async {
    final data = await fetchEpisodeData(
      showId: showId,
      translationType: translationType,
      episodeString: episodeString,
      authToken: authToken,
    );

    final rawJsonStr = data['response'] as String;
    final bootstrap = data['bootstrap'] as CryptoBootstrap;

    final dynamic root = jsonDecode(rawJsonStr);
    if (root is! Map<String, dynamic>) {
      throw Exception('Invalid response format');
    }

    if (root.containsKey('errors')) {
      final errors = root['errors'] as List<dynamic>?;
      if (errors != null && errors.isNotEmpty) {
        final first = errors[0] as Map<String, dynamic>?;
        final msg = first?['message']?.toString() ?? 'Episode query error';
        throw Exception(msg);
      }
    }

    final dataObj = root['data'] as Map<String, dynamic>?;
    final epObj = dataObj?['episode'] as Map<String, dynamic>?;

    // Prefer data.tobeparsed, then data.episode.tobeparsed, then data.episode.episodes
    final rawTobe = dataObj?['tobeparsed']?.toString();
    final epTobe = epObj?['tobeparsed']?.toString();
    final epEps = epObj?['episodes']?.toString();

    final tobeparsed = (rawTobe != null && rawTobe.trim().isNotEmpty)
        ? rawTobe
        : (epTobe != null && epTobe.trim().isNotEmpty)
            ? epTobe
            : epEps;

    if (tobeparsed == null || tobeparsed.trim().isEmpty) {
      throw Exception('No stream data found in episode response.');
    }

    developer.log('[AnimeRepository] Encrypted payload received: length=${tobeparsed.length}', name: 'AnimeRepository');

    final plainText = Decryptor.decrypt(
      payload: tobeparsed,
      partBBase64: bootstrap.partB,
      buildId: Decryptor.defaultBuildId,
    );

    developer.log('[AnimeRepository] Decrypted payload: length=${plainText.length}', name: 'AnimeRepository');

    final streams = StreamParser.parseStreams(plainText);
    if (streams.isEmpty) {
      throw Exception('No playable video stream sources could be extracted.');
    }

    final directSources = streams.where((s) => s.isDirect).toList();
    if (directSources.isNotEmpty) {
      final top = directSources.first;
      final uri = Uri.tryParse(top.url);
      developer.log('[AnimeRepository] Selected top playable source: name=${top.sourceName}, type=${top.type}, ext=${top.fileExtension}, host=${uri?.host}', name: 'AnimeRepository');
    }

    return EpisodeFetchResult(
      streams: streams,
      rawResponse: rawJsonStr,
      tobeparsed: tobeparsed,
      decryptedJson: plainText,
      bootstrapEpoch: bootstrap.epoch,
    );
  }
}

class EpisodeFetchResult {
  final List<StreamSource> streams;
  final String rawResponse;
  final String tobeparsed;
  final String decryptedJson;
  final int bootstrapEpoch;

  const EpisodeFetchResult({
    required this.streams,
    required this.rawResponse,
    required this.tobeparsed,
    required this.decryptedJson,
    required this.bootstrapEpoch,
  });
}

