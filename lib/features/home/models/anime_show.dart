import '../../../utils/image_utils.dart';

class AnimeShow {
  final String id;
  final String name;
  final String? englishName;
  final String? nativeName;
  final String? thumbnail;
  final String? banner;
  final String? type;
  final String? seasonQuarter;
  final int? seasonYear;
  final double? score;
  final int availableEpisodesSub;
  final int availableEpisodesDub;
  final String? lastEpisodeSub;
  final String? lastEpisodeDub;
  final String? views;

  const AnimeShow({
    required this.id,
    required this.name,
    this.englishName,
    this.nativeName,
    this.thumbnail,
    this.banner,
    this.type,
    this.seasonQuarter,
    this.seasonYear,
    this.score,
    this.availableEpisodesSub = 0,
    this.availableEpisodesDub = 0,
    this.lastEpisodeSub,
    this.lastEpisodeDub,
    this.views,
  });

  factory AnimeShow.fromJson(Map<String, dynamic> json) {
    final id = json['_id']?.toString() ?? '';
    final name = json['name']?.toString() ?? json['title']?.toString() ?? 'Unknown';
    final englishName = json['englishName']?.toString();
    final nativeName = json['nativeName']?.toString();
    
    // Resolve thumbnail: check thumbnail, or fallback to cover
    String? thumb = json['thumbnail']?.toString() ?? json['cover']?.toString();
    thumb = ImageUtils.resolveUrl(thumb);

    // Resolve banner if available in response
    String? banner = json['banner']?.toString() ?? json['bannerImage']?.toString() ?? json['bannerUrl']?.toString();
    banner = ImageUtils.resolveUrl(banner);

    final type = json['type']?.toString() ?? json['format']?.toString();

    final seasonObj = json['season'] as Map<String, dynamic>?;
    final seasonQuarter = seasonObj?['quarter']?.toString();
    final seasonYear = (seasonObj?['year'] as num?)?.toInt();

    final score = (json['score'] as num?)?.toDouble();

    final availableEpisodesObj =
        json['availableEpisodes'] as Map<String, dynamic>?;
    final availableEpisodesSub =
        (availableEpisodesObj?['sub'] as num?)?.toInt() ?? 0;
    final availableEpisodesDub =
        (availableEpisodesObj?['dub'] as num?)?.toInt() ?? 0;

    final lastEpisodeInfoObj = json['lastEpisodeInfo'] as Map<String, dynamic>?;
    final subInfo = lastEpisodeInfoObj?['sub'] as Map<String, dynamic>?;
    final dubInfo = lastEpisodeInfoObj?['dub'] as Map<String, dynamic>?;
    final lastEpisodeSub = subInfo?['episodeString']?.toString();
    final lastEpisodeDub = dubInfo?['episodeString']?.toString();

    final pageStatusObj = json['pageStatus'] as Map<String, dynamic>?;
    final views = pageStatusObj?['rangeViews']?.toString() ??
        pageStatusObj?['views']?.toString() ??
        json['views']?.toString();

    return AnimeShow(
      id: id,
      name: name,
      englishName: (englishName != null && englishName.trim().isNotEmpty)
          ? englishName
          : null,
      nativeName: (nativeName != null && nativeName.trim().isNotEmpty)
          ? nativeName
          : null,
      thumbnail: thumb,
      banner: banner,
      type: (type != null && type.trim().isNotEmpty) ? type : null,
      seasonQuarter:
          (seasonQuarter != null && seasonQuarter.trim().isNotEmpty)
              ? seasonQuarter
              : null,
      seasonYear: seasonYear != null && seasonYear > 0 ? seasonYear : null,
      score: score,
      availableEpisodesSub: availableEpisodesSub,
      availableEpisodesDub: availableEpisodesDub,
      lastEpisodeSub: (lastEpisodeSub != null && lastEpisodeSub.trim().isNotEmpty)
          ? lastEpisodeSub
          : null,
      lastEpisodeDub: (lastEpisodeDub != null && lastEpisodeDub.trim().isNotEmpty)
          ? lastEpisodeDub
          : null,
      views: views,
    );
  }

  /// Parses Top 10 cards that come in the {anyCard: {...}, pageStatus: {...}} format
  factory AnimeShow.fromRankedCard(Map<String, dynamic> json) {
    final anyCard = json['anyCard'] as Map<String, dynamic>? ?? json;
    final pageStatus = json['pageStatus'] as Map<String, dynamic>?;

    final id = anyCard['_id']?.toString() ?? '';
    final name = anyCard['name']?.toString() ?? anyCard['title']?.toString() ?? 'Unknown';
    final englishName = anyCard['englishName']?.toString();
    final nativeName = anyCard['nativeName']?.toString();
    String? thumbnail = anyCard['thumbnail']?.toString() ?? anyCard['cover']?.toString();
    thumbnail = ImageUtils.resolveUrl(thumbnail);

    String? banner = anyCard['banner']?.toString() ?? anyCard['bannerImage']?.toString() ?? anyCard['bannerUrl']?.toString();
    banner = ImageUtils.resolveUrl(banner);

    final score = (anyCard['score'] as num?)?.toDouble();

    final availableEpisodesObj =
        anyCard['availableEpisodes'] as Map<String, dynamic>?;
    final availableEpisodesSub =
        (availableEpisodesObj?['sub'] as num?)?.toInt() ?? 0;
    final availableEpisodesDub =
        (availableEpisodesObj?['dub'] as num?)?.toInt() ?? 0;

    final airedStart = anyCard['airedStart'] as Map<String, dynamic>?;
    final seasonYear = (airedStart?['year'] as num?)?.toInt();

    final views = pageStatus?['rangeViews']?.toString() ?? pageStatus?['views']?.toString();

    return AnimeShow(
      id: id,
      name: name,
      englishName: (englishName != null && englishName.trim().isNotEmpty)
          ? englishName
          : null,
      nativeName: (nativeName != null && nativeName.trim().isNotEmpty)
          ? nativeName
          : null,
      thumbnail: thumbnail,
      banner: banner,
      type: 'TV',
      seasonQuarter: null,
      seasonYear: seasonYear != null && seasonYear > 0 ? seasonYear : null,
      score: score,
      availableEpisodesSub: availableEpisodesSub,
      availableEpisodesDub: availableEpisodesDub,
      views: views,
    );
  }
}

class AnimePageResult {
  final List<AnimeShow> shows;
  final int total;
  final int page;

  const AnimePageResult({
    required this.shows,
    required this.total,
    required this.page,
  });
}
