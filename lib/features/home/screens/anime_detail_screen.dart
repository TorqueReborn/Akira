import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../../../../theme/app_colors.dart';
import '../../../../utils/image_utils.dart';
import '../../auth/services/token_manager.dart';
import '../../player/screens/player_screen.dart';
import '../../player/services/watch_history_manager.dart';
import '../models/anime_detail.dart';
import '../services/anime_repository.dart';

/// Screen presenting comprehensive anime details, artwork banner, metadata pills,
/// synopsis, genres, episode status, studio credits, characters and voice actors.
class AnimeDetailScreen extends StatefulWidget {
  final String animeId;
  final String? initialTitle;
  final String? initialPoster;

  const AnimeDetailScreen({
    super.key,
    required this.animeId,
    this.initialTitle,
    this.initialPoster,
  });

  @override
  State<AnimeDetailScreen> createState() => _AnimeDetailScreenState();
}

class _AnimeDetailScreenState extends State<AnimeDetailScreen> {
  AnimeDetail? _detail;
  bool _isLoading = true;
  String? _errorMessage;
  bool _isSynopsisExpanded = false;

  // Episode selection & playback state
  String _selectedTab = 'SUB'; // 'SUB', 'DUB', 'RAW'
  String? _loadingEpisodeString;
  AnimeProgress? _currentProgress;

  @override
  void initState() {
    super.initState();
    _fetchDetail();
    _loadProgress();
  }

  Future<void> _loadProgress() async {
    final progress = await WatchHistoryManager.getProgress(widget.animeId);
    if (mounted) {
      setState(() {
        _currentProgress = progress;
      });
    }
  }

  Future<void> _fetchDetail() async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final detail = await AnimeRepository.fetchAnimeDetail(widget.animeId);
      if (!mounted) return;
      setState(() {
        _detail = detail;
        _isLoading = false;
        if (detail.episodesDetail.sub.isNotEmpty) {
          _selectedTab = 'SUB';
        } else if (detail.episodesDetail.dub.isNotEmpty) {
          _selectedTab = 'DUB';
        } else if (detail.episodesDetail.raw.isNotEmpty) {
          _selectedTab = 'RAW';
        }
      });
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _isLoading = false;
        _errorMessage = e.toString().replaceFirst('Exception: ', '');
      });
    }
  }

  Future<void> _playEpisode(String episodeString, {int startPositionMs = 0}) async {
    if (_loadingEpisodeString != null) return;

    setState(() {
      _loadingEpisodeString = episodeString;
    });

    try {
      final token = TokenManager.getAccessToken();
      final result = await AnimeRepository.fetchEpisodeStreams(
        showId: widget.animeId,
        episodeString: episodeString,
        translationType: _selectedTab.toLowerCase(),
        authToken: token,
      );

      if (!mounted) return;
      setState(() {
        _loadingEpisodeString = null;
      });

      final title = _detail?.englishName ?? _detail?.name ?? widget.initialTitle ?? 'Anime';
      final thumb = _detail?.thumbnail ?? widget.initialPoster;

      await Navigator.of(context).push(
        MaterialPageRoute(
          builder: (_) => PlayerScreen(
            animeId: widget.animeId,
            animeTitle: title,
            episodeNumber: episodeString,
            sources: result.streams,
            thumbnail: thumb,
            initialPositionMs: startPositionMs,
          ),
        ),
      );

      // Refresh progress upon returning
      _loadProgress();
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _loadingEpisodeString = null;
      });
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Failed to load episode $episodeString: ${e.toString().replaceFirst("Exception: ", "")}'),
          backgroundColor: const Color(0xFFE50914),
          behavior: SnackBarBehavior.floating,
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return AnnotatedRegion<SystemUiOverlayStyle>(
      value: const SystemUiOverlayStyle(
        statusBarColor: Colors.transparent,
        statusBarIconBrightness: Brightness.light, // Forces crisp, white clock & icons over backdrop
        statusBarBrightness: Brightness.dark,      // iOS dark header status style
      ),
      child: Scaffold(
        backgroundColor: AppColors.background,
        body: Stack(
          children: [
          // 1. Ambient Background glow matching Akira design language
          Positioned(
            top: -80,
            right: -80,
            child: Container(
              width: 320,
              height: 320,
              decoration: const BoxDecoration(
                shape: BoxShape.circle,
                gradient: RadialGradient(
                  colors: [
                    AppColors.ambientGlow,
                    Colors.transparent,
                  ],
                ),
              ),
            ),
          ),
          Positioned(
            bottom: 60,
            left: -100,
            child: Container(
              width: 300,
              height: 300,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                gradient: RadialGradient(
                  colors: [
                    AppColors.primaryGlow.withAlpha(20),
                    Colors.transparent,
                  ],
                ),
              ),
            ),
          ),

          // 2. Main Content
          if (_isLoading && _detail == null)
            _buildLoadingState()
          else if (_errorMessage != null && _detail == null)
            _buildErrorState()
          else
            _buildDetailContent(),

          // 3. Floating top bar (back button + share / bookmark)
          SafeArea(
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              child: Row(
                children: [
                  _buildGlassCircleButton(
                    icon: Icons.arrow_back_rounded,
                    onPressed: () => Navigator.of(context).pop(),
                  ),
                  const Spacer(),
                  _buildGlassCircleButton(
                    icon: Icons.bookmark_border_rounded,
                    onPressed: () {
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(
                          content: Text('Added to bookmarks!'),
                          duration: Duration(seconds: 1),
                          behavior: SnackBarBehavior.floating,
                        ),
                      );
                    },
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    ),
  );
}

  Widget _buildGlassCircleButton({
    required IconData icon,
    required VoidCallback onPressed,
  }) {
    return Material(
      color: Colors.black.withAlpha(120),
      shape: const CircleBorder(),
      child: InkWell(
        customBorder: const CircleBorder(),
        onTap: onPressed,
        child: Container(
          width: 40,
          height: 40,
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            border: Border.all(
              color: Colors.white.withAlpha(40),
              width: 1,
            ),
          ),
          child: Icon(icon, color: Colors.white, size: 20),
        ),
      ),
    );
  }

  Widget _buildLoadingState() {
    return Stack(
      children: [
        if (widget.initialPoster != null)
          Positioned(
            top: 0,
            left: 0,
            right: 0,
            height: 340,
            child: Stack(
              fit: StackFit.expand,
              children: [
                Image.network(
                  ImageUtils.resolveUrl(widget.initialPoster)!,
                  headers: ImageUtils.imageHeaders,
                  fit: BoxFit.cover,
                ),
                Container(
                  decoration: BoxDecoration(
                    gradient: LinearGradient(
                      begin: Alignment.topCenter,
                      end: Alignment.bottomCenter,
                      colors: [
                        Colors.black.withAlpha(100),
                        AppColors.background.withAlpha(200),
                        AppColors.background,
                      ],
                    ),
                  ),
                ),
              ],
            ),
          ),
        Center(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const SizedBox(
                width: 44,
                height: 44,
                child: CircularProgressIndicator(
                  strokeWidth: 3,
                  valueColor: AlwaysStoppedAnimation<Color>(AppColors.primary),
                ),
              ),
              const SizedBox(height: 16),
              if (widget.initialTitle != null)
                Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 32),
                  child: Text(
                    widget.initialTitle!,
                    textAlign: TextAlign.center,
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(
                      color: AppColors.textPrimary,
                      fontSize: 16,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
              const SizedBox(height: 6),
              const Text(
                'Loading details...',
                style: TextStyle(
                  color: AppColors.textSecondary,
                  fontSize: 13,
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildErrorState() {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: Colors.red.withAlpha(20),
                shape: BoxShape.circle,
              ),
              child: const Icon(
                Icons.error_outline_rounded,
                color: Colors.red,
                size: 42,
              ),
            ),
            const SizedBox(height: 16),
            const Text(
              'Failed to load details',
              style: TextStyle(
                color: AppColors.textPrimary,
                fontSize: 18,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              _errorMessage ?? 'An unexpected error occurred.',
              textAlign: TextAlign.center,
              style: const TextStyle(
                color: AppColors.textSecondary,
                fontSize: 13,
              ),
            ),
            const SizedBox(height: 24),
            ElevatedButton.icon(
              onPressed: _fetchDetail,
              style: ElevatedButton.styleFrom(
                backgroundColor: AppColors.primary,
                padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(14),
                ),
              ),
              icon: const Icon(Icons.refresh_rounded, color: Colors.white, size: 20),
              label: const Text('Try Again',
                  style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildDetailContent() {
    final detail = _detail!;
    final displayTitle = detail.englishName ?? detail.name;
    final bannerImage = detail.banner ?? detail.thumbnail ?? widget.initialPoster;

    return SingleChildScrollView(
      physics: const BouncingScrollPhysics(),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // 1. Hero Artwork Header with Backdrop & Poster
          _buildHeroHeader(detail, bannerImage, displayTitle),

          // 2. Body Details Content Container
          Padding(
            padding: const EdgeInsets.fromLTRB(18, 16, 18, 40),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Quick Info Badges (Type, Season, Episodes, Rating)
                _buildQuickInfoRow(detail),
                const SizedBox(height: 16),

                // Episode status bar (SUB / DUB count and latest released)
                _buildEpisodeStatusBar(detail),
                const SizedBox(height: 18),

                // Watch / Play action button
                _buildPrimaryActionButton(detail),
                const SizedBox(height: 22),

                // Genre Tags
                if (detail.genres.isNotEmpty) ...[
                  _buildGenreTags(detail.genres),
                  const SizedBox(height: 22),
                ],

                // Synopsis with expand/collapse
                if (detail.description != null && detail.description!.isNotEmpty) ...[
                  _buildSynopsisSection(detail.description!),
                  const SizedBox(height: 24),
                ],

                // Information Grid (Studio, Format, Status, Country, Duration)
                _buildInfoGrid(detail),
                const SizedBox(height: 24),

                // Alternative Names (if available)
                if (detail.altNames.isNotEmpty) ...[
                  _buildAltNamesSection(detail.altNames),
                  const SizedBox(height: 24),
                ],

                // Characters & Voice Cast rail
                if (detail.characters.isNotEmpty) ...[
                  _buildCharactersSection(detail.characters),
                  const SizedBox(height: 16),
                ],
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildHeroHeader(
    AnimeDetail detail,
    String? bannerImage,
    String displayTitle,
  ) {
    final posterImage = detail.thumbnail ?? widget.initialPoster;

    return Stack(
      children: [
        // Backdrop Image
        SizedBox(
          height: 320,
          width: double.infinity,
          child: bannerImage != null && bannerImage.isNotEmpty
              ? Image.network(
                  bannerImage,
                  headers: ImageUtils.imageHeaders,
                  fit: BoxFit.cover,
                  errorBuilder: (context, error, stackTrace) => Container(
                    color: const Color(0xFFEDE8F5),
                  ),
                )
              : Container(
                  decoration: const BoxDecoration(
                    gradient: AppColors.primaryGradient,
                  ),
                ),
        ),

        // Gradient fading downwards seamlessly into the light background
        Positioned.fill(
          child: Container(
            decoration: BoxDecoration(
              gradient: LinearGradient(
                begin: Alignment.topCenter,
                end: Alignment.bottomCenter,
                colors: [
                  Colors.black.withAlpha(150),
                  Colors.black.withAlpha(70),
                  AppColors.background.withAlpha(180),
                  AppColors.background,
                ],
                stops: const [0.0, 0.4, 0.78, 1.0],
              ),
            ),
          ),
        ),

        // Dedicated top status-bar contrast scrim to make white clock and battery perfectly clear
        Positioned(
          top: 0,
          left: 0,
          right: 0,
          height: 60,
          child: Container(
            decoration: BoxDecoration(
              gradient: LinearGradient(
                begin: Alignment.topCenter,
                end: Alignment.bottomCenter,
                colors: [
                  Colors.black.withAlpha(170),
                  Colors.transparent,
                ],
              ),
            ),
          ),
        ),

        // Foreground Poster + Title + Score Info
        Positioned(
          left: 18,
          right: 18,
          bottom: 12,
          child: Row(
            crossAxisAlignment: CrossAxisAlignment.end,
            children: [
              // Poster Card with Elevated Shadow + Top-Right Star Rating
              Container(
                width: 110,
                height: 160,
                decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(16),
                  color: const Color(0xFF1E1B2E),
                  boxShadow: [
                    BoxShadow(
                      color: AppColors.primaryDark.withAlpha(80),
                      blurRadius: 18,
                      offset: const Offset(0, 8),
                    ),
                  ],
                  border: Border.all(
                    color: Colors.white.withAlpha(40),
                    width: 1.5,
                  ),
                ),
                clipBehavior: Clip.antiAlias,
                child: Stack(
                  fit: StackFit.expand,
                  children: [
                    // Poster image
                    if (posterImage != null && posterImage.isNotEmpty)
                      Image.network(
                        posterImage,
                        headers: ImageUtils.imageHeaders,
                        fit: BoxFit.cover,
                        errorBuilder: (context, error, stackTrace) =>
                            const Center(
                          child: Icon(
                            Icons.movie_creation_outlined,
                            color: AppColors.primaryLight,
                            size: 36,
                          ),
                        ),
                      )
                    else
                      const Center(
                        child: Icon(
                          Icons.movie_creation_outlined,
                          color: AppColors.primaryLight,
                          size: 36,
                        ),
                      ),

                    // Top-right Score Badge directly on Poster Card
                    if (detail.score != null && detail.score! > 0)
                      Positioned(
                        top: 7,
                        right: 7,
                        child: Container(
                          padding: const EdgeInsets.symmetric(
                            horizontal: 6,
                            vertical: 3.5,
                          ),
                          decoration: BoxDecoration(
                            color: Colors.black.withAlpha(190),
                            borderRadius: BorderRadius.circular(8),
                            border: Border.all(
                              color: Colors.white.withAlpha(35),
                              width: 0.8,
                            ),
                          ),
                          child: Row(
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              const Icon(
                                Icons.star_rounded,
                                color: Color(0xFFFFB800),
                                size: 13,
                              ),
                              const SizedBox(width: 3),
                              Text(
                                detail.score!.toStringAsFixed(1),
                                style: const TextStyle(
                                  color: Colors.white,
                                  fontSize: 11,
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                            ],
                          ),
                        ),
                      ),
                  ],
                ),
              ),

              const SizedBox(width: 16),

              // Title and Secondary Details
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    // Main Title
                    Text(
                      displayTitle,
                      maxLines: 3,
                      overflow: TextOverflow.ellipsis,
                      style: const TextStyle(
                        color: AppColors.textPrimary,
                        fontSize: 20,
                        fontWeight: FontWeight.w800,
                        height: 1.25,
                        letterSpacing: -0.2,
                      ),
                    ),
                    const SizedBox(height: 6),

                    // Native / Japanese title if different
                    if (detail.nativeName != null &&
                        detail.nativeName != displayTitle) ...[
                      Text(
                        detail.nativeName!,
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                        style: const TextStyle(
                          color: AppColors.textSecondary,
                          fontSize: 12.5,
                          fontWeight: FontWeight.w500,
                        ),
                      ),
                      const SizedBox(height: 8),
                    ],

                    // Clean inline rating: 5 star indicator + score + like emoji
                    if ((detail.score != null && detail.score! > 0) ||
                        (detail.averageScore != null && detail.averageScore! > 0))
                      Row(
                        children: [
                          // 5 Stars visual indicator (only stars, no numeric value)
                          if (detail.score != null && detail.score! > 0)
                            Row(
                              mainAxisSize: MainAxisSize.min,
                              children: List.generate(5, (index) {
                                final starRating = (detail.score! / 2.0); // convert 10-scale to 5-scale
                                if (starRating >= index + 0.8) {
                                  return const Icon(
                                    Icons.star_rounded,
                                    color: Color(0xFFFFB800),
                                    size: 17,
                                  );
                                } else if (starRating >= index + 0.3) {
                                  return const Icon(
                                    Icons.star_half_rounded,
                                    color: Color(0xFFFFB800),
                                    size: 17,
                                  );
                                } else {
                                  return Icon(
                                    Icons.star_outline_rounded,
                                    color: Colors.amber.withAlpha(120),
                                    size: 17,
                                  );
                                }
                              }),
                            ),

                          // Dot separator
                          if (detail.score != null &&
                              detail.score! > 0 &&
                              detail.averageScore != null &&
                              detail.averageScore! > 0) ...[
                            const SizedBox(width: 8),
                            const Text(
                              '•',
                              style: TextStyle(
                                color: AppColors.textHint,
                                fontSize: 12,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                            const SizedBox(width: 8),
                          ],

                          // Reaction gauge with emoji (e.g. 🔥 Highly Liked / 👍 Well Liked)
                          if (detail.averageScore != null &&
                              detail.averageScore! > 0)
                            _buildReactionGauge(detail.averageScore!),
                        ],
                      ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildReactionGauge(int averageScore) {
    final String emoji;
    final String label;
    final Color color;

    if (averageScore >= 80) {
      emoji = '🔥';
      label = 'Must Watch';
      color = const Color(0xFFEF4444); // Vibrant Red/Orange
    } else if (averageScore >= 70) {
      emoji = '✨';
      label = 'Highly Rated';
      color = const Color(0xFF8B5CF6); // Akira Purple
    } else if (averageScore >= 60) {
      emoji = '👍';
      label = 'Well Liked';
      color = const Color(0xFF10B981); // Emerald Green
    } else {
      emoji = '👀';
      label = 'Casual Pick';
      color = AppColors.textSecondary;
    }

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 7, vertical: 3),
      decoration: BoxDecoration(
        color: color.withAlpha(20),
        borderRadius: BorderRadius.circular(8),
        border: Border.all(
          color: color.withAlpha(45),
          width: 0.9,
        ),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(
            emoji,
            style: const TextStyle(fontSize: 12),
          ),
          const SizedBox(width: 4),
          Text(
            label,
            style: TextStyle(
              color: color,
              fontSize: 11,
              fontWeight: FontWeight.w700,
              letterSpacing: 0.2,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildQuickInfoRow(AnimeDetail detail) {
    final seasonStr = [
      if (detail.seasonQuarter != null) detail.seasonQuarter,
      if (detail.seasonYear != null) detail.seasonYear.toString(),
    ].join(' ');

    return SingleChildScrollView(
      scrollDirection: Axis.horizontal,
      physics: const BouncingScrollPhysics(),
      child: Row(
        children: [
          if (detail.type != null && detail.type!.isNotEmpty)
            _buildBadgePill(
              icon: Icons.tv_rounded,
              text: detail.type!,
              color: AppColors.primary,
            ),
          if (detail.status != null && detail.status!.isNotEmpty) ...[
            const SizedBox(width: 8),
            _buildBadgePill(
              icon: Icons.stream_rounded,
              text: detail.status!,
              color: detail.status?.toLowerCase() == 'finished'
                  ? const Color(0xFF10B981)
                  : const Color(0xFF3B82F6),
            ),
          ],
          if (seasonStr.isNotEmpty) ...[
            const SizedBox(width: 8),
            _buildBadgePill(
              icon: Icons.calendar_month_rounded,
              text: seasonStr,
              color: const Color(0xFF8B5CF6),
            ),
          ],
          if (detail.rating != null && detail.rating!.isNotEmpty) ...[
            const SizedBox(width: 8),
            _buildBadgePill(
              icon: Icons.shield_outlined,
              text: detail.rating!,
              color: const Color(0xFFF97316),
            ),
          ],
          if (detail.episodeDurationMinutes != null) ...[
            const SizedBox(width: 8),
            _buildBadgePill(
              icon: Icons.schedule_rounded,
              text: '${detail.episodeDurationMinutes}m / ep',
              color: AppColors.textSecondary,
            ),
          ],
        ],
      ),
    );
  }

  Widget _buildBadgePill({
    required IconData icon,
    required String text,
    required Color color,
  }) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
      decoration: BoxDecoration(
        color: color.withAlpha(22),
        borderRadius: BorderRadius.circular(10),
        border: Border.all(
          color: color.withAlpha(60),
          width: 1,
        ),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, size: 14, color: color),
          const SizedBox(width: 5),
          Text(
            text,
            style: TextStyle(
              color: color,
              fontSize: 12,
              fontWeight: FontWeight.w700,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildEpisodeStatusBar(AnimeDetail detail) {
    final subText = detail.lastEpisodeSub != null
        ? 'Ep ${detail.lastEpisodeSub}'
        : (detail.availableEpisodesSub > 0
            ? '${detail.availableEpisodesSub} Eps'
            : 'None');

    final dubText = detail.lastEpisodeDub != null
        ? 'Ep ${detail.lastEpisodeDub}'
        : (detail.availableEpisodesDub > 0
            ? '${detail.availableEpisodesDub} Eps'
            : 'None');

    final int? totalCount = (detail.episodeCount != null && detail.episodeCount! > 0)
        ? detail.episodeCount
        : (detail.type?.toLowerCase() == 'movie'
            ? 1
            : (detail.availableEpisodesSub > 0
                ? detail.availableEpisodesSub
                : (detail.availableEpisodesDub > 0
                    ? detail.availableEpisodesDub
                    : null)));

    final String totalText =
        totalCount != null ? '$totalCount Eps' : 'N/A';

    return Row(
      children: [
        // 1. SUB Card
        Expanded(
          child: _buildAvailabilityStatCard(
            label: 'SUB',
            value: subText,
            themeColor: const Color(0xFF6366F1),
            icon: Icons.subtitles_rounded,
          ),
        ),
        const SizedBox(width: 10),

        // 2. DUB Card
        Expanded(
          child: _buildAvailabilityStatCard(
            label: 'DUB',
            value: dubText,
            themeColor: const Color(0xFF10B981),
            icon: Icons.mic_rounded,
          ),
        ),
        const SizedBox(width: 10),

        // 3. TOTAL Card (Always visible)
        Expanded(
          child: _buildAvailabilityStatCard(
            label: 'TOTAL',
            value: totalText,
            themeColor: AppColors.primary,
            icon: Icons.video_library_rounded,
          ),
        ),
      ],
    );
  }

  Widget _buildAvailabilityStatCard({
    required String label,
    required String value,
    required Color themeColor,
    required IconData icon,
  }) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(
          color: const Color(0xFFEDE8F5),
          width: 1,
        ),
        boxShadow: [
          BoxShadow(
            color: const Color(0xFF0F172A).withAlpha(8),
            blurRadius: 8,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                decoration: BoxDecoration(
                  color: themeColor.withAlpha(25),
                  borderRadius: BorderRadius.circular(6),
                ),
                child: Text(
                  label,
                  style: TextStyle(
                    color: themeColor,
                    fontSize: 10,
                    fontWeight: FontWeight.w800,
                    letterSpacing: 0.5,
                  ),
                ),
              ),
              const Spacer(),
              Icon(
                icon,
                size: 14,
                color: themeColor.withAlpha(160),
              ),
            ],
          ),
          const SizedBox(height: 6),
          Text(
            value,
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
            style: const TextStyle(
              color: AppColors.textPrimary,
              fontSize: 13.5,
              fontWeight: FontWeight.w800,
            ),
          ),
        ],
      ),
    );
  }



  Widget _buildPrimaryActionButton(AnimeDetail detail) {
    final isLoadingThis = _loadingEpisodeString != null;
    String targetEp = '1';

    // Helper to get episodes list for a tab
    List<String> getTabEpisodes(String tab) {
      if (tab == 'SUB') {
        return detail.episodesDetail.sub.isNotEmpty
            ? detail.episodesDetail.sub
            : (detail.availableEpisodesSub > 0
                ? List.generate(detail.availableEpisodesSub, (i) => '${i + 1}')
                : <String>[]);
      } else if (tab == 'DUB') {
        return detail.episodesDetail.dub.isNotEmpty
            ? detail.episodesDetail.dub
            : (detail.availableEpisodesDub > 0
                ? List.generate(detail.availableEpisodesDub, (i) => '${i + 1}')
                : <String>[]);
      } else {
        return detail.episodesDetail.raw;
      }
    }

    final activeEpisodes = getTabEpisodes(_selectedTab).isNotEmpty
        ? getTabEpisodes(_selectedTab)
        : (getTabEpisodes('SUB').isNotEmpty ? getTabEpisodes('SUB') : getTabEpisodes('DUB'));

    if (_currentProgress != null) {
      targetEp = _currentProgress!.episodeNumber;
    } else if (activeEpisodes.isNotEmpty) {
      targetEp = activeEpisodes.first;
    }

    return Row(
      children: [
        // 1. Primary Action: Watch / Resume Episode Button
        Expanded(
          child: Container(
            height: 52,
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(16),
              gradient: AppColors.logoGradient,
              boxShadow: [
                BoxShadow(
                  color: AppColors.primary.withAlpha(70),
                  blurRadius: 14,
                  offset: const Offset(0, 4),
                ),
              ],
            ),
            child: Material(
              color: Colors.transparent,
              child: InkWell(
                onTap: () {
                  final startMs = _currentProgress != null && _currentProgress!.episodeNumber == targetEp
                      ? _currentProgress!.seekPositionMs
                      : 0;
                  _playEpisode(targetEp, startPositionMs: startMs);
                },
                borderRadius: BorderRadius.circular(16),
                splashColor: Colors.white.withAlpha(50),
                child: Center(
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      if (isLoadingThis) ...[
                        const SizedBox(
                          width: 20,
                          height: 20,
                          child: CircularProgressIndicator(
                            strokeWidth: 2.5,
                            color: Colors.white,
                          ),
                        ),
                        const SizedBox(width: 8),
                        Text(
                          'Loading Ep $targetEp...',
                          style: const TextStyle(
                            color: Colors.white,
                            fontSize: 15,
                            fontWeight: FontWeight.w800,
                          ),
                        ),
                      ] else ...[
                        const Icon(
                          Icons.play_arrow_rounded,
                          color: Colors.white,
                          size: 26,
                        ),
                        const SizedBox(width: 6),
                        Text(
                          _currentProgress != null ? 'Resume Episode $targetEp' : 'Watch Episode $targetEp',
                          style: const TextStyle(
                            color: Colors.white,
                            fontSize: 15,
                            fontWeight: FontWeight.w800,
                            letterSpacing: 0.3,
                          ),
                        ),
                      ],
                    ],
                  ),
                ),
              ),
            ),
          ),
        ),
        const SizedBox(width: 10),

        // 2. Secondary Action: Icon-Only Episodes Button
        Container(
          width: 52,
          height: 52,
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(16),
            border: Border.all(
              color: const Color(0xFFEDE8F5),
              width: 1.2,
            ),
            boxShadow: [
              BoxShadow(
                color: const Color(0xFF0F172A).withAlpha(8),
                blurRadius: 10,
                offset: const Offset(0, 3),
              ),
            ],
          ),
          child: Material(
            color: Colors.transparent,
            child: InkWell(
              onTap: () => _showEpisodesBottomSheet(detail),
              borderRadius: BorderRadius.circular(16),
              child: const Center(
                child: Icon(
                  Icons.format_list_numbered_rounded,
                  color: AppColors.primary,
                  size: 24,
                ),
              ),
            ),
          ),
        ),
      ],
    );
  }



  void _showEpisodesBottomSheet(AnimeDetail detail) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (ctx) {
        return _EpisodesBottomSheet(
          detail: detail,
          initialTab: _selectedTab,
          currentEpisode: _currentProgress?.episodeNumber,
          loadingEpisode: _loadingEpisodeString,
          onTabChanged: (newTab) {
            setState(() {
              _selectedTab = newTab;
            });
          },
          onSelectEpisode: (ep) {
            Navigator.of(ctx).pop();
            _playEpisode(ep);
          },
        );
      },
    );
  }

  Widget _buildGenreTags(List<String> genres) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          'Genres',
          style: TextStyle(
            color: AppColors.textPrimary,
            fontSize: 16,
            fontWeight: FontWeight.w800,
            letterSpacing: -0.2,
          ),
        ),
        const SizedBox(height: 10),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: genres.map((genre) {
            return Container(
              padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 7),
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(20),
                border: Border.all(
                  color: const Color(0xFFE2E8F0),
                  width: 1,
                ),
                boxShadow: [
                  BoxShadow(
                    color: const Color(0xFF0F172A).withAlpha(8),
                    blurRadius: 4,
                    offset: const Offset(0, 1),
                  ),
                ],
              ),
              child: Text(
                genre,
                style: const TextStyle(
                  color: AppColors.textPrimary,
                  fontSize: 12.5,
                  fontWeight: FontWeight.w600,
                ),
              ),
            );
          }).toList(),
        ),
      ],
    );
  }

  Widget _buildSynopsisSection(String description) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          'Synopsis',
          style: TextStyle(
            color: AppColors.textPrimary,
            fontSize: 16,
            fontWeight: FontWeight.w800,
            letterSpacing: -0.2,
          ),
        ),
        const SizedBox(height: 8),
        Text(
          description,
          maxLines: _isSynopsisExpanded ? null : 4,
          overflow: _isSynopsisExpanded ? null : TextOverflow.ellipsis,
          style: const TextStyle(
            color: AppColors.textSecondary,
            fontSize: 14,
            height: 1.55,
          ),
        ),
        if (description.length > 200) ...[
          const SizedBox(height: 4),
          GestureDetector(
            onTap: () {
              setState(() {
                _isSynopsisExpanded = !_isSynopsisExpanded;
              });
            },
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                Text(
                  _isSynopsisExpanded ? 'Show less' : 'Read more',
                  style: const TextStyle(
                    color: AppColors.primary,
                    fontSize: 13,
                    fontWeight: FontWeight.w700,
                  ),
                ),
                Icon(
                  _isSynopsisExpanded
                      ? Icons.keyboard_arrow_up_rounded
                      : Icons.keyboard_arrow_down_rounded,
                  color: AppColors.primary,
                  size: 18,
                ),
              ],
            ),
          ),
        ],
      ],
    );
  }

  Widget _buildInfoGrid(AnimeDetail detail) {
    final items = <Map<String, String>>[
      if (detail.studios.isNotEmpty)
        {'label': 'Studios', 'value': detail.studios.join(', ')},
      if (detail.countryOfOrigin != null)
        {'label': 'Origin', 'value': detail.countryOfOrigin!},
      if (detail.type != null)
        {'label': 'Format', 'value': detail.type!},
      if (detail.status != null)
        {'label': 'Status', 'value': detail.status!},
      if (detail.episodeCount != null)
        {'label': 'Episodes', 'value': detail.episodeCount.toString()},
      if (detail.views != null && detail.views!.isNotEmpty)
        {'label': 'Views', 'value': detail.views!},
    ];

    if (items.isEmpty) return const SizedBox.shrink();

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          'Information',
          style: TextStyle(
            color: AppColors.textPrimary,
            fontSize: 16,
            fontWeight: FontWeight.w800,
            letterSpacing: -0.2,
          ),
        ),
        const SizedBox(height: 12),
        Container(
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(16),
            border: Border.all(
              color: const Color(0xFFE2E8F0),
              width: 1,
            ),
            boxShadow: [
              BoxShadow(
                color: const Color(0xFF0F172A).withAlpha(8),
                blurRadius: 6,
                offset: const Offset(0, 2),
              ),
            ],
          ),
          child: Column(
            children: items.asMap().entries.map((entry) {
              final idx = entry.key;
              final item = entry.value;
              final isLast = idx == items.length - 1;

              return Column(
                children: [
                  Row(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      SizedBox(
                        width: 90,
                        child: Text(
                          item['label']!,
                          style: const TextStyle(
                            color: AppColors.textSecondary,
                            fontSize: 13,
                            fontWeight: FontWeight.w500,
                          ),
                        ),
                      ),
                      Expanded(
                        child: Text(
                          item['value']!,
                          style: const TextStyle(
                            color: AppColors.textPrimary,
                            fontSize: 13,
                            fontWeight: FontWeight.w700,
                          ),
                        ),
                      ),
                    ],
                  ),
                  if (!isLast) ...[
                    const SizedBox(height: 10),
                    const Divider(height: 1, color: Color(0xFFF1F5F9)),
                    const SizedBox(height: 10),
                  ],
                ],
              );
            }).toList(),
          ),
        ),
      ],
    );
  }

  Widget _buildAltNamesSection(List<String> altNames) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          'Alternative Titles',
          style: TextStyle(
            color: AppColors.textPrimary,
            fontSize: 16,
            fontWeight: FontWeight.w800,
            letterSpacing: -0.2,
          ),
        ),
        const SizedBox(height: 8),
        Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: altNames.take(4).map((name) {
            return Padding(
              padding: const EdgeInsets.only(bottom: 4),
              child: Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text('• ',
                      style: TextStyle(color: AppColors.primary, fontWeight: FontWeight.bold)),
                  Expanded(
                    child: Text(
                      name,
                      style: const TextStyle(
                        color: AppColors.textSecondary,
                        fontSize: 13,
                      ),
                    ),
                  ),
                ],
              ),
            );
          }).toList(),
        ),
      ],
    );
  }

  Widget _buildCharactersSection(List<AnimeCharacter> characters) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            const Text(
              'Characters & Cast',
              style: TextStyle(
                color: AppColors.textPrimary,
                fontSize: 16,
                fontWeight: FontWeight.w800,
                letterSpacing: -0.2,
              ),
            ),
            const Spacer(),
            Text(
              '${characters.length}',
              style: const TextStyle(
                color: AppColors.textHint,
                fontSize: 13,
                fontWeight: FontWeight.w600,
              ),
            ),
          ],
        ),
        const SizedBox(height: 12),
        SizedBox(
          height: 140,
          child: ListView.separated(
            scrollDirection: Axis.horizontal,
            physics: const BouncingScrollPhysics(),
            itemCount: characters.length,
            separatorBuilder: (context, index) => const SizedBox(width: 12),
            itemBuilder: (context, index) {
              final character = characters[index];
              return SizedBox(
                width: 90,
                child: Column(
                  children: [
                    // Character Avatar
                    Container(
                      width: 74,
                      height: 74,
                      decoration: BoxDecoration(
                        shape: BoxShape.circle,
                        color: const Color(0xFF1E1B2E),
                        border: Border.all(
                          color: character.role.toLowerCase() == 'main'
                              ? AppColors.primary
                              : const Color(0xFFE2E8F0),
                          width: character.role.toLowerCase() == 'main' ? 2 : 1,
                        ),
                        boxShadow: [
                          BoxShadow(
                            color: const Color(0xFF0F172A).withAlpha(15),
                            blurRadius: 6,
                            offset: const Offset(0, 2),
                          ),
                        ],
                      ),
                      child: character.imageUrl != null &&
                              character.imageUrl!.isNotEmpty
                          ? Image.network(
                              character.imageUrl!,
                              headers: ImageUtils.imageHeaders,
                              fit: BoxFit.cover,
                              errorBuilder: (context, error, stackTrace) =>
                                  const Center(
                                child: Icon(
                                  Icons.person_outline_rounded,
                                  color: AppColors.textHint,
                                ),
                              ),
                            )
                          : const Center(
                              child: Icon(
                                Icons.person_outline_rounded,
                                color: AppColors.textHint,
                              ),
                            ),
                    ),
                    const SizedBox(height: 6),

                    // Character Name
                    Text(
                      character.name,
                      maxLines: 2,
                      textAlign: TextAlign.center,
                      overflow: TextOverflow.ellipsis,
                      style: const TextStyle(
                        color: AppColors.textPrimary,
                        fontSize: 11.5,
                        fontWeight: FontWeight.w700,
                        height: 1.15,
                      ),
                    ),
                    const SizedBox(height: 2),

                    // Role pill / text
                    Text(
                      character.role,
                      maxLines: 1,
                      style: TextStyle(
                        color: character.role.toLowerCase() == 'main'
                            ? AppColors.primary
                            : AppColors.textHint,
                        fontSize: 10,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ],
                ),
              );
            },
          ),
        ),
      ],
    );
  }
}

class _EpisodesBottomSheet extends StatefulWidget {
  final AnimeDetail detail;
  final String initialTab;
  final String? currentEpisode;
  final String? loadingEpisode;
  final ValueChanged<String> onTabChanged;
  final ValueChanged<String> onSelectEpisode;

  const _EpisodesBottomSheet({
    required this.detail,
    required this.initialTab,
    this.currentEpisode,
    this.loadingEpisode,
    required this.onTabChanged,
    required this.onSelectEpisode,
  });

  @override
  State<_EpisodesBottomSheet> createState() => _EpisodesBottomSheetState();
}

class _EpisodesBottomSheetState extends State<_EpisodesBottomSheet> {
  final TextEditingController _searchController = TextEditingController();
  late String _activeTab;
  int _selectedChunkIndex = 0;
  static const int _chunkSize = 100;

  @override
  void initState() {
    super.initState();
    _activeTab = widget.initialTab;
  }

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  List<String> _getEpisodesForTab(String tab) {
    if (tab == 'SUB') {
      return widget.detail.episodesDetail.sub.isNotEmpty
          ? widget.detail.episodesDetail.sub
          : (widget.detail.availableEpisodesSub > 0
              ? List.generate(widget.detail.availableEpisodesSub, (i) => '${i + 1}')
              : <String>[]);
    } else if (tab == 'DUB') {
      return widget.detail.episodesDetail.dub.isNotEmpty
          ? widget.detail.episodesDetail.dub
          : (widget.detail.availableEpisodesDub > 0
              ? List.generate(widget.detail.availableEpisodesDub, (i) => '${i + 1}')
              : <String>[]);
    } else {
      return widget.detail.episodesDetail.raw;
    }
  }

  List<String> _getAvailableTabs() {
    final tabs = <String>[];
    if (_getEpisodesForTab('SUB').isNotEmpty) tabs.add('SUB');
    if (_getEpisodesForTab('DUB').isNotEmpty) tabs.add('DUB');
    if (_getEpisodesForTab('RAW').isNotEmpty) tabs.add('RAW');
    return tabs.isNotEmpty ? tabs : ['SUB'];
  }

  @override
  Widget build(BuildContext context) {
    final availableTabs = _getAvailableTabs();
    if (!availableTabs.contains(_activeTab)) {
      _activeTab = availableTabs.first;
    }

    final episodes = _getEpisodesForTab(_activeTab);
    final query = _searchController.text.trim();
    final totalEpisodes = episodes.length;
    final totalChunks = (totalEpisodes / _chunkSize).ceil();

    List<String> displayedEpisodes;
    if (query.isNotEmpty) {
      displayedEpisodes = episodes
          .where((ep) => ep.toLowerCase().contains(query.toLowerCase()))
          .toList();
    } else if (totalChunks > 1) {
      final start = _selectedChunkIndex * _chunkSize;
      final end = (start + _chunkSize).clamp(0, totalEpisodes);
      displayedEpisodes = episodes.sublist(start, end);
    } else {
      displayedEpisodes = episodes;
    }

    return Container(
      height: MediaQuery.of(context).size.height * 0.82,
      decoration: const BoxDecoration(
        color: AppColors.background,
        borderRadius: BorderRadius.vertical(top: Radius.circular(28)),
      ),
      child: Column(
        children: [
          // Drag handle
          Container(
            margin: const EdgeInsets.only(top: 12, bottom: 6),
            width: 42,
            height: 4.5,
            decoration: BoxDecoration(
              color: Colors.black.withAlpha(25),
              borderRadius: BorderRadius.circular(3),
            ),
          ),

          // Header with Title & Close Button
          Padding(
            padding: const EdgeInsets.fromLTRB(20, 8, 14, 6),
            child: Row(
              children: [
                const Text(
                  'Episodes',
                  style: TextStyle(
                    color: AppColors.textPrimary,
                    fontSize: 20,
                    fontWeight: FontWeight.w800,
                    letterSpacing: -0.3,
                  ),
                ),
                const SizedBox(width: 8),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2.5),
                  decoration: BoxDecoration(
                    color: AppColors.primary.withAlpha(20),
                    borderRadius: BorderRadius.circular(10),
                  ),
                  child: Text(
                    '$totalEpisodes ${_activeTab.toLowerCase()}',
                    style: const TextStyle(
                      color: AppColors.primary,
                      fontSize: 11.5,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                ),
                const Spacer(),
                IconButton(
                  icon: const Icon(Icons.close_rounded, color: AppColors.textSecondary, size: 22),
                  onPressed: () => Navigator.of(context).pop(),
                ),
              ],
            ),
          ),

          // SUB / DUB / RAW Pill Switcher (if multiple tabs exist)
          if (availableTabs.length > 1)
            Padding(
              padding: const EdgeInsets.fromLTRB(20, 0, 20, 10),
              child: Container(
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(16),
                  border: Border.all(color: const Color(0xFFEDE8F5), width: 1),
                  boxShadow: [
                    BoxShadow(
                      color: const Color(0xFF0F172A).withAlpha(6),
                      blurRadius: 6,
                      offset: const Offset(0, 2),
                    ),
                  ],
                ),
                padding: const EdgeInsets.all(4),
                child: Row(
                  children: availableTabs.map((tab) {
                    final isSel = tab == _activeTab;
                    final count = _getEpisodesForTab(tab).length;
                    return Expanded(
                      child: GestureDetector(
                        onTap: () {
                          setState(() {
                            _activeTab = tab;
                            _selectedChunkIndex = 0;
                            _searchController.clear();
                          });
                          widget.onTabChanged(tab);
                        },
                        child: AnimatedContainer(
                          duration: const Duration(milliseconds: 200),
                          padding: const EdgeInsets.symmetric(vertical: 8),
                          decoration: BoxDecoration(
                            gradient: isSel ? AppColors.logoGradient : null,
                            color: isSel ? null : Colors.transparent,
                            borderRadius: BorderRadius.circular(12),
                            boxShadow: isSel
                                ? [
                                    BoxShadow(
                                      color: AppColors.primary.withAlpha(50),
                                      blurRadius: 8,
                                      offset: const Offset(0, 2),
                                    ),
                                  ]
                                : null,
                          ),
                          alignment: Alignment.center,
                          child: Row(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              Icon(
                                tab == 'SUB'
                                    ? Icons.subtitles_rounded
                                    : (tab == 'DUB' ? Icons.mic_rounded : Icons.fiber_manual_record_rounded),
                                size: 14,
                                color: isSel ? Colors.white : AppColors.textSecondary,
                              ),
                              const SizedBox(width: 5),
                              Text(
                                '$tab ($count)',
                                style: TextStyle(
                                  color: isSel ? Colors.white : AppColors.textSecondary,
                                  fontSize: 12.5,
                                  fontWeight: isSel ? FontWeight.w800 : FontWeight.w600,
                                ),
                              ),
                            ],
                          ),
                        ),
                      ),
                    );
                  }).toList(),
                ),
              ),
            ),

          // Search Bar
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 4),
            child: TextField(
              controller: _searchController,
              onChanged: (_) => setState(() {}),
              style: const TextStyle(color: AppColors.textPrimary, fontSize: 14),
              decoration: InputDecoration(
                hintText: 'Jump to episode (e.g. 12, 105)...',
                hintStyle: const TextStyle(color: AppColors.textHint, fontSize: 13),
                prefixIcon: const Icon(Icons.search_rounded, color: AppColors.primary, size: 20),
                suffixIcon: query.isNotEmpty
                    ? IconButton(
                        icon: const Icon(Icons.clear_rounded, color: AppColors.textSecondary, size: 18),
                        onPressed: () {
                          _searchController.clear();
                          setState(() {});
                        },
                      )
                    : null,
                filled: true,
                fillColor: Colors.white,
                contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
                enabledBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(14),
                  borderSide: const BorderSide(color: Color(0xFFEDE8F5), width: 1),
                ),
                focusedBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(14),
                  borderSide: const BorderSide(color: AppColors.primary, width: 1.5),
                ),
              ),
            ),
          ),

          // Chunk Range Chips (e.g. 1-100, 101-200)
          if (query.isEmpty && totalChunks > 1) ...[
            const SizedBox(height: 8),
            SizedBox(
              height: 34,
              child: ListView.separated(
                padding: const EdgeInsets.symmetric(horizontal: 20),
                scrollDirection: Axis.horizontal,
                physics: const BouncingScrollPhysics(),
                itemCount: totalChunks,
                separatorBuilder: (_, _) => const SizedBox(width: 8),
                itemBuilder: (context, idx) {
                  final start = idx * _chunkSize + 1;
                  final end = ((idx + 1) * _chunkSize).clamp(1, totalEpisodes);
                  final isSelected = idx == _selectedChunkIndex;

                  return ChoiceChip(
                    label: Text(
                      '$start - $end',
                      style: TextStyle(
                        fontSize: 11.5,
                        fontWeight: FontWeight.w700,
                        color: isSelected ? Colors.white : AppColors.textSecondary,
                      ),
                    ),
                    selected: isSelected,
                    selectedColor: AppColors.primary,
                    backgroundColor: Colors.white,
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(18),
                      side: BorderSide(
                        color: isSelected ? AppColors.primary : const Color(0xFFEDE8F5),
                      ),
                    ),
                    onSelected: (val) {
                      if (val) {
                        setState(() {
                          _selectedChunkIndex = idx;
                        });
                      }
                    },
                  );
                },
              ),
            ),
          ],

          const SizedBox(height: 12),

          // Episodes Grid
          Expanded(
            child: displayedEpisodes.isEmpty
                ? Center(
                    child: Text(
                      'No episodes found for "$query"',
                      style: const TextStyle(color: AppColors.textSecondary),
                    ),
                  )
                : GridView.builder(
                    padding: const EdgeInsets.fromLTRB(20, 0, 20, 24),
                    physics: const BouncingScrollPhysics(),
                    gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                      crossAxisCount: 5,
                      mainAxisSpacing: 10,
                      crossAxisSpacing: 10,
                      childAspectRatio: 1.45,
                    ),
                    itemCount: displayedEpisodes.length,
                    itemBuilder: (context, index) {
                      final ep = displayedEpisodes[index];
                      final isCurrentProgress = widget.currentEpisode == ep;
                      final isCurrentlyLoading = widget.loadingEpisode == ep;

                      return Material(
                        color: Colors.transparent,
                        child: InkWell(
                          borderRadius: BorderRadius.circular(12),
                          onTap: () => widget.onSelectEpisode(ep),
                          child: AnimatedContainer(
                            duration: const Duration(milliseconds: 150),
                            decoration: BoxDecoration(
                              color: isCurrentProgress
                                  ? AppColors.primary.withAlpha(20)
                                  : Colors.white,
                              borderRadius: BorderRadius.circular(12),
                              border: Border.all(
                                color: isCurrentProgress
                                    ? AppColors.primary
                                    : const Color(0xFFEDE8F5),
                                width: isCurrentProgress ? 1.6 : 1,
                              ),
                              boxShadow: [
                                BoxShadow(
                                  color: isCurrentProgress
                                      ? AppColors.primary.withAlpha(20)
                                      : const Color(0xFF0F172A).withAlpha(6),
                                  blurRadius: isCurrentProgress ? 6 : 4,
                                  offset: const Offset(0, 1.5),
                                ),
                              ],
                            ),
                            child: Center(
                              child: isCurrentlyLoading
                                  ? const SizedBox(
                                      width: 16,
                                      height: 16,
                                      child: CircularProgressIndicator(
                                        strokeWidth: 2,
                                        color: AppColors.primary,
                                      ),
                                    )
                                  : Row(
                                      mainAxisSize: MainAxisSize.min,
                                      children: [
                                        Icon(
                                          isCurrentProgress
                                              ? Icons.play_arrow_rounded
                                              : Icons.play_arrow_outlined,
                                          size: 13,
                                          color: isCurrentProgress
                                              ? AppColors.primary
                                              : AppColors.textSecondary.withAlpha(160),
                                        ),
                                        const SizedBox(width: 2),
                                        Text(
                                          ep,
                                          style: TextStyle(
                                            color: isCurrentProgress
                                                ? AppColors.primary
                                                : AppColors.textPrimary,
                                            fontSize: 12.5,
                                            fontWeight: isCurrentProgress
                                                ? FontWeight.w800
                                                : FontWeight.w600,
                                          ),
                                        ),
                                      ],
                                    ),
                            ),
                          ),
                        ),
                      );
                    },
                  ),
          ),
        ],
      ),
    );
  }
}
