class AnimeCharacter {
  final String? id;
  final String role;
  final String name;
  final String? nativeName;
  final String? imageUrl;
  final List<String> voiceActors;

  const AnimeCharacter({
    this.id,
    required this.role,
    required this.name,
    this.nativeName,
    this.imageUrl,
    this.voiceActors = const [],
  });

  factory AnimeCharacter.fromJson(Map<String, dynamic> json) {
    final role = json['role']?.toString() ?? 'Supporting';
    final nameObj = json['name'] as Map<String, dynamic>?;
    final name = nameObj?['full']?.toString() ??
        nameObj?['name']?.toString() ??
        'Unknown';
    final native = nameObj?['native']?.toString();

    final imgObj = json['image'] as Map<String, dynamic>?;
    final imgUrl = imgObj?['large']?.toString() ?? imgObj?['medium']?.toString();

    final vaList = <String>[];
    final vas = json['voiceActors'] as List<dynamic>?;
    if (vas != null) {
      for (final va in vas) {
        if (va is Map<String, dynamic>) {
          final lang = va['language']?.toString();
          if (lang != null && lang.isNotEmpty) {
            vaList.add(lang);
          }
        }
      }
    }

    return AnimeCharacter(
      id: json['aniListId']?.toString(),
      role: role,
      name: name,
      nativeName: native,
      imageUrl: imgUrl,
      voiceActors: vaList,
    );
  }
}

class AnimeDetail {
  final String id;
  final String name;
  final String? englishName;
  final String? nativeName;
  final String? thumbnail;
  final String? banner;
  final String? description;
  final String? type;
  final String? status;
  final String? rating;
  final double? score;
  final int? averageScore;
  final int? episodeCount;
  final int? episodeDurationMinutes;
  final String? seasonQuarter;
  final int? seasonYear;
  final String? countryOfOrigin;
  final List<String> studios;
  final List<String> genres;
  final List<String> altNames;
  final int availableEpisodesSub;
  final int availableEpisodesDub;
  final String? lastEpisodeSub;
  final String? lastEpisodeDub;
  final String? views;
  final String? trailerVideoId;
  final List<AnimeCharacter> characters;
  final Map<String, dynamic>? siteRanks;

  const AnimeDetail({
    required this.id,
    required this.name,
    this.englishName,
    this.nativeName,
    this.thumbnail,
    this.banner,
    this.description,
    this.type,
    this.status,
    this.rating,
    this.score,
    this.averageScore,
    this.episodeCount,
    this.episodeDurationMinutes,
    this.seasonQuarter,
    this.seasonYear,
    this.countryOfOrigin,
    this.studios = const [],
    this.genres = const [],
    this.altNames = const [],
    this.availableEpisodesSub = 0,
    this.availableEpisodesDub = 0,
    this.lastEpisodeSub,
    this.lastEpisodeDub,
    this.views,
    this.trailerVideoId,
    this.characters = const [],
    this.siteRanks,
  });

  /// Clean HTML tags (such as <i>, <br>, etc.) from API descriptions
  static String cleanDescription(String? raw) {
    if (raw == null || raw.isEmpty) return '';
    return raw
        .replaceAll(RegExp(r'<br\s*/?>', caseSensitive: false), '\n')
        .replaceAll(RegExp(r'</?i>', caseSensitive: false), '')
        .replaceAll(RegExp(r'</?b>', caseSensitive: false), '')
        .replaceAll(RegExp(r'</?p>', caseSensitive: false), '\n')
        .replaceAll(RegExp(r'<[^>]*>'), '')
        .replaceAll('&amp;', '&')
        .replaceAll('&quot;', '"')
        .replaceAll('&apos;', "'")
        .replaceAll('&lt;', '<')
        .replaceAll('&gt;', '>')
        .trim();
  }

  factory AnimeDetail.fromJson(Map<String, dynamic> json) {
    final id = json['_id']?.toString() ?? '';
    final name = json['name']?.toString() ??
        json['title']?.toString() ??
        'Unknown';
    final englishName = json['englishName']?.toString();
    final nativeName = json['nativeName']?.toString();
    final thumbnail = json['thumbnail']?.toString() ?? json['cover']?.toString();
    final banner = json['banner']?.toString();
    final description = cleanDescription(json['description']?.toString());
    final type = json['type']?.toString();
    final status = json['status']?.toString();
    final rating = json['rating']?.toString();
    double? parseDouble(dynamic val) {
      if (val == null) return null;
      if (val is num) return val.toDouble();
      return double.tryParse(val.toString());
    }

    int? parseInt(dynamic val) {
      if (val == null) return null;
      if (val is num) return val.toInt();
      return int.tryParse(val.toString());
    }

    final score = parseDouble(json['score']);
    final averageScore = parseInt(json['averageScore']);
    final episodeCount = parseInt(json['episodeCount']);

    // Episode duration comes in milliseconds (e.g. 1440000 ms = 24 mins)
    int? durationMins;
    final durMs = parseInt(json['episodeDuration']);
    if (durMs != null && durMs > 0) {
      durationMins = (durMs / 60000).round();
    }

    final seasonObj = json['season'] as Map<String, dynamic>?;
    final seasonQuarter = seasonObj?['quarter']?.toString();
    final airedStartObj = json['airedStart'] as Map<String, dynamic>?;
    final seasonYear = parseInt(seasonObj?['year']) ?? parseInt(airedStartObj?['year']);

    final countryOfOrigin = json['countryOfOrigin']?.toString();

    final studios = <String>[];
    if (json['studios'] is List) {
      for (final s in json['studios']) {
        if (s != null && s.toString().trim().isNotEmpty) {
          studios.add(s.toString().trim());
        }
      }
    }

    final genres = <String>[];
    if (json['genres'] is List) {
      for (final g in json['genres']) {
        if (g != null && g.toString().trim().isNotEmpty) {
          genres.add(g.toString().trim());
        }
      }
    }

    final altNames = <String>[];
    if (json['altNames'] is List) {
      for (final a in json['altNames']) {
        if (a != null && a.toString().trim().isNotEmpty) {
          altNames.add(a.toString().trim());
        }
      }
    }

    final availableEpisodesObj =
        json['availableEpisodes'] as Map<String, dynamic>?;
    final availableEpisodesSub =
        parseInt(availableEpisodesObj?['sub']) ?? 0;
    final availableEpisodesDub =
        parseInt(availableEpisodesObj?['dub']) ?? 0;

    final lastEpisodeInfoObj = json['lastEpisodeInfo'] as Map<String, dynamic>?;
    final subInfo = lastEpisodeInfoObj?['sub'] as Map<String, dynamic>?;
    final dubInfo = lastEpisodeInfoObj?['dub'] as Map<String, dynamic>?;
    final lastEpisodeSub = subInfo?['episodeString']?.toString();
    final lastEpisodeDub = dubInfo?['episodeString']?.toString();

    final pageStatusObj = json['pageStatus'] as Map<String, dynamic>?;
    final views = pageStatusObj?['rangeViews']?.toString() ??
        pageStatusObj?['views']?.toString() ??
        json['views']?.toString();

    // Trailer video
    String? trailerId;
    if (json['prevideos'] is List && (json['prevideos'] as List).isNotEmpty) {
      trailerId = (json['prevideos'] as List)[0]?.toString();
    }

    // Characters
    final characters = <AnimeCharacter>[];
    if (json['characters'] is List) {
      for (final c in json['characters']) {
        if (c is Map<String, dynamic>) {
          characters.add(AnimeCharacter.fromJson(c));
        }
      }
    }

    final siteRanks = json['siteRanks'] as Map<String, dynamic>?;

    return AnimeDetail(
      id: id,
      name: name,
      englishName: (englishName != null && englishName.trim().isNotEmpty)
          ? englishName
          : null,
      nativeName: (nativeName != null && nativeName.trim().isNotEmpty)
          ? nativeName
          : null,
      thumbnail: (thumbnail != null && thumbnail.trim().isNotEmpty)
          ? thumbnail
          : null,
      banner: (banner != null && banner.trim().isNotEmpty) ? banner : null,
      description: description.isNotEmpty ? description : null,
      type: type,
      status: status,
      rating: rating,
      score: score,
      averageScore: averageScore,
      episodeCount: episodeCount,
      episodeDurationMinutes: durationMins,
      seasonQuarter: seasonQuarter,
      seasonYear: seasonYear,
      countryOfOrigin: countryOfOrigin,
      studios: studios,
      genres: genres,
      altNames: altNames,
      availableEpisodesSub: availableEpisodesSub,
      availableEpisodesDub: availableEpisodesDub,
      lastEpisodeSub: lastEpisodeSub,
      lastEpisodeDub: lastEpisodeDub,
      views: views,
      trailerVideoId: trailerId,
      characters: characters,
      siteRanks: siteRanks,
    );
  }
}
