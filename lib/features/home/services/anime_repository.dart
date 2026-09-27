import 'dart:convert';
import 'package:http/http.dart' as http;
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
}
