import '../../../utils/image_utils.dart';

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
    String? imgUrl = imgObj?['large']?.toString() ?? imgObj?['medium']?.toString();
    imgUrl = ImageUtils.resolveUrl(imgUrl);

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

class AnimeMusic {
  final String type; // 'Opening', 'Ending', etc.
  final String title;

  const AnimeMusic({
    required this.type,
    required this.title,
  });

  factory AnimeMusic.fromJson(Map<String, dynamic> json) {
    return AnimeMusic(
      type: json['type']?.toString() ?? 'Theme',
      title: json['title']?.toString() ?? '',
    );
  }
}

class AnimeRelatedItem {
  final String relation; // 'adaptation', 'prequel', 'sequel', etc.
  final String? showId;
  final String? mangaId;

  const AnimeRelatedItem({
    required this.relation,
    this.showId,
    this.mangaId,
  });

  factory AnimeRelatedItem.fromJson(Map<String, dynamic> json) {
    return AnimeRelatedItem(
      relation: json['relation']?.toString() ?? 'Related',
      showId: json['showId']?.toString(),
      mangaId: json['mangaId']?.toString(),
    );
  }
}

class AnimeCommunityStats {
  final String views;
  final String likes;
  final String comments;
  final String reviews;
  final int? bookmarkRank;
  final int? bookmarkScore;

  const AnimeCommunityStats({
    this.views = '0',
    this.likes = '0',
    this.comments = '0',
    this.reviews = '0',
    this.bookmarkRank,
    this.bookmarkScore,
  });

  factory AnimeCommunityStats.fromShow(Map<String, dynamic> json) {
    final pageStatus = json['pageStatus'] as Map<String, dynamic>?;
    final siteRanks = json['siteRanks'] as Map<String, dynamic>?;

    final views = pageStatus?['views']?.toString() ??
        pageStatus?['rangeViews']?.toString() ??
        json['views']?.toString() ??
        '0';
    final likes = pageStatus?['likesCount']?.toString() ?? '0';
    final comments = pageStatus?['commentCount']?.toString() ?? '0';
    final reviews = pageStatus?['reviewCount']?.toString() ?? '0';

    int? bmRank;
    int? bmScore;
    final bookmarked = siteRanks?['bookmarked'] as Map<String, dynamic>?;
    if (bookmarked != null) {
      final s = bookmarked['score'];
      if (s is num) {
        bmScore = s.toInt();
      } else if (s != null) {
        bmScore = int.tryParse(s.toString());
      }
    }

    final entries = siteRanks?['entries'] as List<dynamic>?;
    if (entries != null) {
      for (final e in entries) {
        if (e is Map<String, dynamic> && e['key'] == 'bookmarked') {
          final pos = e['position'];
          if (pos is num) {
            bmRank = pos.toInt();
          } else if (pos != null) {
            bmRank = int.tryParse(pos.toString());
          }
        }
      }
    }

    return AnimeCommunityStats(
      views: views,
      likes: likes,
      comments: comments,
      reviews: reviews,
      bookmarkRank: bmRank,
      bookmarkScore: bmScore,
    );
  }
}

class AnimeEpisodeDetail {
  final List<String> sub;
  final List<String> dub;
  final List<String> raw;

  const AnimeEpisodeDetail({
    this.sub = const [],
    this.dub = const [],
    this.raw = const [],
  });

  factory AnimeEpisodeDetail.fromJson(Map<String, dynamic>? json) {
    if (json == null) return const AnimeEpisodeDetail();
    List<String> parseList(dynamic val) {
      if (val is List) {
        return val.map((e) => e.toString()).toList().reversed.toList();
      }
      return const [];
    }

    return AnimeEpisodeDetail(
      sub: parseList(json['sub']),
      dub: parseList(json['dub']),
      raw: parseList(json['raw']),
    );
  }
}

class AnimeDetail {
  final String id;
  final String name;
  final String? englishName;
  final String? nativeName;
  final String? thumbnail;
  final List<String> thumbnails;
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
  final List<String> tags;
  final List<String> altNames;
  final int availableEpisodesSub;
  final int availableEpisodesDub;
  final String? lastEpisodeSub;
  final String? lastEpisodeDub;
  final AnimeEpisodeDetail episodesDetail;
  final String? views;
  final String? trailerVideoId;
  final List<String> prevideos;
  final List<AnimeCharacter> characters;
  final List<AnimeMusic> musics;
  final List<AnimeRelatedItem> relatedShows;
  final List<AnimeRelatedItem> relatedMangas;
  final AnimeCommunityStats communityStats;
  final String? malId;
  final String? aniListId;
  final String? airedStartFormatted;
  final String? broadcastIntervalDays;
  final Map<String, dynamic>? siteRanks;

  const AnimeDetail({
    required this.id,
    required this.name,
    this.englishName,
    this.nativeName,
    this.thumbnail,
    this.thumbnails = const [],
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
    this.tags = const [],
    this.altNames = const [],
    this.availableEpisodesSub = 0,
    this.availableEpisodesDub = 0,
    this.lastEpisodeSub,
    this.lastEpisodeDub,
    this.episodesDetail = const AnimeEpisodeDetail(),
    this.views,
    this.trailerVideoId,
    this.prevideos = const [],
    this.characters = const [],
    this.musics = const [],
    this.relatedShows = const [],
    this.relatedMangas = const [],
    this.communityStats = const AnimeCommunityStats(),
    this.malId,
    this.aniListId,
    this.airedStartFormatted,
    this.broadcastIntervalDays,
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
    String? thumbnail = json['thumbnail']?.toString() ?? json['cover']?.toString();
    thumbnail = ImageUtils.resolveUrl(thumbnail);

    final thumbnailsList = <String>[];
    if (json['thumbnails'] is List) {
      for (final t in json['thumbnails']) {
        final resolved = ImageUtils.resolveUrl(t?.toString());
        if (resolved != null && resolved.isNotEmpty) {
          thumbnailsList.add(resolved);
        }
      }
    }

    String? banner = json['banner']?.toString();
    banner = ImageUtils.resolveUrl(banner);
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

    // Episode duration in minutes
    int? durationMins;
    final durMs = parseInt(json['episodeDuration']);
    if (durMs != null && durMs > 0) {
      durationMins = (durMs / 60000).round();
    }

    final seasonObj = json['season'] as Map<String, dynamic>?;
    final seasonQuarter = seasonObj?['quarter']?.toString();
    final airedStartObj = json['airedStart'] as Map<String, dynamic>?;
    final seasonYear = parseInt(seasonObj?['year']) ?? parseInt(airedStartObj?['year']);

    // Format Aired Start date (e.g. "Sep 5, 2026")
    String? airedStartFormatted;
    if (airedStartObj != null && airedStartObj['year'] != null) {
      final y = airedStartObj['year'];
      final m = airedStartObj['month'];
      final d = airedStartObj['date'] ?? airedStartObj['day'];
      const monthNames = [
        '', 'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
        'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'
      ];
      final mStr = (m is int && m >= 1 && m <= 12) ? monthNames[m] : (m != null ? 'M$m' : '');
      if (mStr.isNotEmpty && d != null) {
        airedStartFormatted = '$mStr $d, $y';
      } else if (mStr.isNotEmpty) {
        airedStartFormatted = '$mStr $y';
      } else {
        airedStartFormatted = '$y';
      }
    }

    // Broadcast interval
    String? broadcastIntervalDays;
    final intervalMs = parseInt(json['broadcastInterval']);
    if (intervalMs != null && intervalMs > 0) {
      final days = (intervalMs / (1000 * 60 * 60 * 24)).round();
      if (days == 7) {
        broadcastIntervalDays = 'Weekly';
      } else if (days == 1) {
        broadcastIntervalDays = 'Daily';
      } else {
        broadcastIntervalDays = 'Every $days days';
      }
    }

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

    final tags = <String>[];
    if (json['tags'] is List) {
      for (final t in json['tags']) {
        if (t != null && t.toString().trim().isNotEmpty) {
          String cleanTag = t.toString().trim();
          if (cleanTag.startsWith('theme:')) {
            cleanTag = cleanTag.substring(6);
          }
          if (cleanTag.isNotEmpty) {
            tags.add(cleanTag[0].toUpperCase() + cleanTag.substring(1));
          }
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

    // Trailer video(s)
    final prevideos = <String>[];
    if (json['prevideos'] is List) {
      for (final p in json['prevideos']) {
        if (p != null && p.toString().trim().isNotEmpty) {
          prevideos.add(p.toString().trim());
        }
      }
    }
    final trailerId = prevideos.isNotEmpty ? prevideos.first : null;

    // Characters
    final characters = <AnimeCharacter>[];
    if (json['characters'] is List) {
      for (final c in json['characters']) {
        if (c is Map<String, dynamic>) {
          characters.add(AnimeCharacter.fromJson(c));
        }
      }
    }

    // Musics (OP / ED themes)
    final musics = <AnimeMusic>[];
    if (json['musics'] is List) {
      for (final m in json['musics']) {
        if (m is Map<String, dynamic>) {
          musics.add(AnimeMusic.fromJson(m));
        }
      }
    }

    // Related Shows & Mangas
    final relatedShows = <AnimeRelatedItem>[];
    if (json['relatedShows'] is List) {
      for (final r in json['relatedShows']) {
        if (r is Map<String, dynamic>) {
          relatedShows.add(AnimeRelatedItem.fromJson(r));
        }
      }
    }

    final relatedMangas = <AnimeRelatedItem>[];
    if (json['relatedMangas'] is List) {
      for (final r in json['relatedMangas']) {
        if (r is Map<String, dynamic>) {
          relatedMangas.add(AnimeRelatedItem.fromJson(r));
        }
      }
    }

    // Community Stats
    final communityStats = AnimeCommunityStats.fromShow(json);

    final malId = json['malId']?.toString();
    final aniListId = json['aniListId']?.toString();

    final siteRanks = json['siteRanks'] as Map<String, dynamic>?;
    final epDetailObj = json['availableEpisodesDetail'] as Map<String, dynamic>?;
    final episodesDetail = AnimeEpisodeDetail.fromJson(epDetailObj);

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
      thumbnails: thumbnailsList,
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
      tags: tags,
      altNames: altNames,
      availableEpisodesSub: availableEpisodesSub,
      availableEpisodesDub: availableEpisodesDub,
      lastEpisodeSub: lastEpisodeSub,
      lastEpisodeDub: lastEpisodeDub,
      episodesDetail: episodesDetail,
      views: views,
      trailerVideoId: trailerId,
      prevideos: prevideos,
      characters: characters,
      musics: musics,
      relatedShows: relatedShows,
      relatedMangas: relatedMangas,
      communityStats: communityStats,
      malId: malId,
      aniListId: aniListId,
      airedStartFormatted: airedStartFormatted,
      broadcastIntervalDays: broadcastIntervalDays,
      siteRanks: siteRanks,
    );
  }
}
