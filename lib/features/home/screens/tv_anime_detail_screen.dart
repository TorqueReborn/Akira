import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../../../theme/app_colors.dart';
import '../../auth/services/token_manager.dart';
import '../../player/screens/tv_player_screen.dart';
import '../../player/services/watch_history_manager.dart';
import '../models/anime_detail.dart';
import '../services/anime_repository.dart';

/// 100% Remote D-Pad Accessible Anime Detail Screen designed specifically for Android TV.
/// Features a 2-column landscape layout (Poster/Actions on Left, Info/Episodes/Cast on Right),
/// luminous focused state outlines, scale micro-animations, and fast episode navigation.
class TvAnimeDetailScreen extends StatefulWidget {
  final String animeId;
  final String? initialTitle;
  final String? initialPoster;

  const TvAnimeDetailScreen({
    super.key,
    required this.animeId,
    this.initialTitle,
    this.initialPoster,
  });

  @override
  State<TvAnimeDetailScreen> createState() => _TvAnimeDetailScreenState();
}

class _TvAnimeDetailScreenState extends State<TvAnimeDetailScreen> {
  AnimeDetail? _detail;
  bool _isLoading = true;
  String? _errorMessage;

  // Selected audio tab
  String _selectedTab = 'SUB'; // 'SUB', 'DUB', 'RAW'
  String? _loadingEpisodeString;
  AnimeProgress? _currentProgress;

  // Focus nodes
  final FocusNode _backButtonFocusNode = FocusNode();
  final FocusNode _watchButtonFocusNode = FocusNode();
  final FocusNode _bookmarkButtonFocusNode = FocusNode();
  final FocusNode _retryButtonFocusNode = FocusNode();

  // Tab focus nodes
  final FocusNode _subTabFocusNode = FocusNode();
  final FocusNode _dubTabFocusNode = FocusNode();
  final FocusNode _rawTabFocusNode = FocusNode();

  // Chunk selector focus nodes
  final List<FocusNode> _chunkFocusNodes = [];
  // Episode grid focus nodes
  final List<FocusNode> _episodeFocusNodes = [];

  // Episode pagination chunk index (1-100, 101-200, etc.)
  int _selectedChunkIndex = 0;
  static const int _chunkSize = 100;

  final ScrollController _rightPanelScrollController = ScrollController();

  @override
  void initState() {
    super.initState();
    _fetchDetail();
    _loadProgress();
  }

  @override
  void dispose() {
    _backButtonFocusNode.dispose();
    _watchButtonFocusNode.dispose();
    _bookmarkButtonFocusNode.dispose();
    _retryButtonFocusNode.dispose();
    _subTabFocusNode.dispose();
    _dubTabFocusNode.dispose();
    _rawTabFocusNode.dispose();
    for (final node in _chunkFocusNodes) {
      node.dispose();
    }
    for (final node in _episodeFocusNodes) {
      node.dispose();
    }
    _rightPanelScrollController.dispose();
    super.dispose();
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

      // Auto-focus primary watch action once loaded
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (mounted && _watchButtonFocusNode.canRequestFocus) {
          _watchButtonFocusNode.requestFocus();
        }
      });
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _isLoading = false;
        _errorMessage = e.toString().replaceFirst('Exception: ', '');
      });
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (mounted && _retryButtonFocusNode.canRequestFocus) {
          _retryButtonFocusNode.requestFocus();
        }
      });
    }
  }

  List<String> _getEpisodesForTab(String tab) {
    if (_detail == null) return const [];
    if (tab == 'SUB') {
      return _detail!.episodesDetail.sub.isNotEmpty
          ? _detail!.episodesDetail.sub
          : (_detail!.availableEpisodesSub > 0
              ? List.generate(_detail!.availableEpisodesSub, (i) => '${i + 1}')
              : <String>[]);
    } else if (tab == 'DUB') {
      return _detail!.episodesDetail.dub.isNotEmpty
          ? _detail!.episodesDetail.dub
          : (_detail!.availableEpisodesDub > 0
              ? List.generate(_detail!.availableEpisodesDub, (i) => '${i + 1}')
              : <String>[]);
    } else {
      return _detail!.episodesDetail.raw;
    }
  }

  List<String> _getAvailableTabs() {
    final tabs = <String>[];
    if (_getEpisodesForTab('SUB').isNotEmpty) tabs.add('SUB');
    if (_getEpisodesForTab('DUB').isNotEmpty) tabs.add('DUB');
    if (_getEpisodesForTab('RAW').isNotEmpty) tabs.add('RAW');
    return tabs.isNotEmpty ? tabs : ['SUB'];
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
          builder: (_) => TvPlayerScreen(
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
    return Actions(
      actions: <Type, Action<Intent>>{
        _TvPopIntent: CallbackAction<_TvPopIntent>(
          onInvoke: (intent) => Navigator.of(context).maybePop(),
        ),
      },
      child: Shortcuts(
        shortcuts: <LogicalKeySet, Intent>{
          LogicalKeySet(LogicalKeyboardKey.escape): const _TvPopIntent(),
          LogicalKeySet(LogicalKeyboardKey.gameButtonB): const _TvPopIntent(),
          LogicalKeySet(LogicalKeyboardKey.backspace): const _TvPopIntent(),
        },
        child: Scaffold(
          backgroundColor: const Color(0xFFFAF9FE),
          body: Stack(
            children: [
              // 1. Ambient Background Artwork & Lighting
              _buildBackdropGlow(),

              // 2. Main Content
              if (_isLoading && _detail == null)
                _buildLoadingState()
              else if (_errorMessage != null && _detail == null)
                _buildErrorState()
              else
                _buildTvDetailsLayout(),

              // 3. Floating Back Button (Top Left)
              Positioned(
                top: 24,
                left: 28,
                child: _buildFocusableCircleButton(
                  focusNode: _backButtonFocusNode,
                  icon: Icons.arrow_back_rounded,
                  tooltip: 'Back',
                  onPressed: () => Navigator.of(context).maybePop(),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildBackdropGlow() {
    final bannerImage = _detail?.banner ?? _detail?.thumbnail ?? widget.initialPoster;

    return Stack(
      fit: StackFit.expand,
      children: [
        if (bannerImage != null && bannerImage.isNotEmpty)
          Positioned.fill(
            child: Image.network(
              bannerImage,
              fit: BoxFit.cover,
              errorBuilder: (context, error, stackTrace) => const SizedBox.shrink(),
            ),
          ),
        // Ambient Light Theme Color Gradients & Subtle Vignette
        Positioned.fill(
          child: Container(
            decoration: const BoxDecoration(
              gradient: LinearGradient(
                begin: Alignment.topCenter,
                end: Alignment.bottomCenter,
                colors: [
                  Color(0xCCFAF9FE),
                  Color(0xF0FAF9FE),
                  Color(0xFFFAF9FE),
                ],
                stops: [0.0, 0.45, 1.0],
              ),
            ),
          ),
        ),
        Positioned.fill(
          child: Container(
            decoration: const BoxDecoration(
              gradient: LinearGradient(
                begin: Alignment.centerLeft,
                end: Alignment.centerRight,
                colors: [
                  Color(0xFFFAF9FE),
                  Color(0xF2FAF9FE),
                  Color(0xD0FAF9FE),
                  Color(0xF8FAF9FE),
                ],
                stops: [0.0, 0.35, 0.70, 1.0],
              ),
            ),
          ),
        ),
        // Vibrant Violet Accent Orb (Top Left)
        Positioned(
          top: -120,
          left: -80,
          child: Container(
            width: 550,
            height: 550,
            decoration: const BoxDecoration(
              shape: BoxShape.circle,
              gradient: RadialGradient(
                colors: [
                  Color(0x257C3AED),
                  Colors.transparent,
                ],
              ),
            ),
          ),
        ),
        // Rose Glow Orb (Bottom Right)
        Positioned(
          bottom: -160,
          right: -80,
          child: Container(
            width: 600,
            height: 600,
            decoration: const BoxDecoration(
              shape: BoxShape.circle,
              gradient: RadialGradient(
                colors: [
                  Color(0x18EC4899),
                  Colors.transparent,
                ],
              ),
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildLoadingState() {
    return Center(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            width: 68,
            height: 68,
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              color: Colors.white,
              border: Border.all(color: const Color(0xFFE2E8F0), width: 1.5),
              boxShadow: [
                BoxShadow(
                  color: const Color(0xFF7C3AED).withAlpha(35),
                  blurRadius: 20,
                ),
              ],
            ),
            child: const CircularProgressIndicator(
              strokeWidth: 3.0,
              valueColor: AlwaysStoppedAnimation<Color>(AppColors.primary),
            ),
          ),
          const SizedBox(height: 22),
          Text(
            widget.initialTitle != null ? 'Loading "${widget.initialTitle}"...' : 'Loading Anime Details...',
            style: const TextStyle(
              color: Color(0xFF1E1B2E),
              fontSize: 18,
              fontWeight: FontWeight.w700,
              letterSpacing: 0.3,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildErrorState() {
    return Center(
      child: Container(
        constraints: const BoxConstraints(maxWidth: 520),
        padding: const EdgeInsets.symmetric(horizontal: 40, vertical: 36),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(24),
          border: Border.all(color: const Color(0xFFFECACA), width: 1.2),
          boxShadow: [
            BoxShadow(
              color: const Color(0xFF0F172A).withAlpha(20),
              blurRadius: 24,
              offset: const Offset(0, 8),
            ),
          ],
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              width: 72,
              height: 72,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                color: const Color(0xFFEF4444).withAlpha(25),
                border: Border.all(color: const Color(0xFFEF4444).withAlpha(80), width: 1.5),
              ),
              child: const Icon(Icons.error_outline_rounded, size: 40, color: Color(0xFFEF4444)),
            ),
            const SizedBox(height: 20),
            const Text(
              'Failed to Load Details',
              style: TextStyle(color: Color(0xFF1E1B2E), fontSize: 24, fontWeight: FontWeight.w900),
            ),
            const SizedBox(height: 10),
            Text(
              _errorMessage ?? 'An unexpected network error occurred.',
              textAlign: TextAlign.center,
              style: const TextStyle(color: Color(0xFF64748B), fontSize: 14, height: 1.4),
            ),
            const SizedBox(height: 28),
            _buildTvPrimaryButton(
              focusNode: _retryButtonFocusNode,
              label: 'Retry Connection',
              icon: Icons.refresh_rounded,
              onPressed: _fetchDetail,
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildTvDetailsLayout() {
    final detail = _detail!;
    final posterImage = detail.thumbnail ?? widget.initialPoster;

    return Padding(
      padding: const EdgeInsets.fromLTRB(44, 28, 44, 24),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // ----------------------------------------------------
          // LEFT COLUMN: Poster, Score badge, Main Play & Bookmark actions
          // ----------------------------------------------------
          SizedBox(
            width: 270,
            child: SingleChildScrollView(
              physics: const ClampingScrollPhysics(),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.center,
                children: [
                  const SizedBox(height: 52), // Spacing for top back button
                  // Poster Card with Light Surface Styling
                  Container(
                    width: 230,
                    height: 326,
                    decoration: BoxDecoration(
                      borderRadius: BorderRadius.circular(22),
                      color: Colors.white,
                      boxShadow: [
                        BoxShadow(
                          color: const Color(0xFF7C3AED).withAlpha(35),
                          blurRadius: 32,
                          offset: const Offset(0, 12),
                        ),
                        BoxShadow(
                          color: const Color(0xFF0F172A).withAlpha(15),
                          blurRadius: 20,
                          offset: const Offset(0, 6),
                        ),
                      ],
                      border: Border.all(
                        color: const Color(0xFFE2E8F0),
                        width: 1.5,
                      ),
                    ),
                    clipBehavior: Clip.antiAlias,
                    child: Stack(
                      fit: StackFit.expand,
                      children: [
                        if (posterImage != null && posterImage.isNotEmpty)
                          Image.network(
                            posterImage,
                            fit: BoxFit.cover,
                            errorBuilder: (context, error, stackTrace) => const Center(
                              child: Icon(Icons.movie_filter_rounded, color: Color(0xFF94A3B8), size: 52),
                            ),
                          )
                        else
                          const Center(
                            child: Icon(Icons.movie_filter_rounded, color: Color(0xFF94A3B8), size: 52),
                          ),

                        // Subtle bottom gradient inside poster
                        Positioned(
                          left: 0,
                          right: 0,
                          bottom: 0,
                          height: 80,
                          child: Container(
                            decoration: BoxDecoration(
                              gradient: LinearGradient(
                                begin: Alignment.topCenter,
                                end: Alignment.bottomCenter,
                                colors: [
                                  Colors.transparent,
                                  Colors.black.withAlpha(120),
                                ],
                              ),
                            ),
                          ),
                        ),

                        // Score overlay on poster top right
                        if (detail.score != null && detail.score! > 0)
                          Positioned(
                            top: 12,
                            right: 12,
                            child: Container(
                              padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
                              decoration: BoxDecoration(
                                color: const Color(0xFFFFFBEB),
                                borderRadius: BorderRadius.circular(12),
                                border: Border.all(
                                  color: const Color(0xFFF59E0B),
                                  width: 1.2,
                                ),
                                boxShadow: [
                                  BoxShadow(
                                    color: const Color(0xFF0F172A).withAlpha(12),
                                    blurRadius: 8,
                                    offset: const Offset(0, 2),
                                  ),
                                ],
                              ),
                              child: Row(
                                mainAxisSize: MainAxisSize.min,
                                children: [
                                  const Icon(Icons.star_rounded, color: Color(0xFFF59E0B), size: 16),
                                  const SizedBox(width: 4),
                                  Text(
                                    detail.score!.toStringAsFixed(1),
                                    style: const TextStyle(
                                      color: Color(0xFF92400E),
                                      fontSize: 13.5,
                                      fontWeight: FontWeight.w900,
                                    ),
                                  ),
                                ],
                              ),
                            ),
                          ),
                      ],
                    ),
                  ),

                  const SizedBox(height: 22),

                  // Watch / Resume Action Button
                  _buildPrimaryWatchButton(detail),

                  const SizedBox(height: 18),

                  // Studio & Format Quick Stats Box
                  if (detail.studios.isNotEmpty || detail.status != null)
                    Container(
                      width: 230,
                      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                      decoration: BoxDecoration(
                        color: Colors.white,
                        borderRadius: BorderRadius.circular(16),
                        border: Border.all(color: const Color(0xFFE2E8F0), width: 1.2),
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
                          if (detail.studios.isNotEmpty) ...[
                            Row(
                              children: [
                                const Icon(Icons.movie_creation_outlined, color: Color(0xFF64748B), size: 14),
                                const SizedBox(width: 6),
                                Expanded(
                                  child: Text(
                                    detail.studios.first.toUpperCase(),
                                    maxLines: 1,
                                    overflow: TextOverflow.ellipsis,
                                    style: const TextStyle(
                                      color: Color(0xFF334155),
                                      fontSize: 12,
                                      fontWeight: FontWeight.w800,
                                      letterSpacing: 0.6,
                                    ),
                                  ),
                                ),
                              ],
                            ),
                            const SizedBox(height: 8),
                          ],
                          if (detail.status != null)
                            Row(
                              children: [
                                Container(
                                  width: 7,
                                  height: 7,
                                  decoration: BoxDecoration(
                                    shape: BoxShape.circle,
                                    color: detail.status?.toLowerCase() == 'finished'
                                        ? const Color(0xFF10B981)
                                        : const Color(0xFF3B82F6),
                                  ),
                                ),
                                const SizedBox(width: 8),
                                Text(
                                  detail.status!.toUpperCase(),
                                  style: TextStyle(
                                    color: detail.status?.toLowerCase() == 'finished'
                                        ? const Color(0xFF059669)
                                        : const Color(0xFF2563EB),
                                    fontSize: 12,
                                    fontWeight: FontWeight.w800,
                                    letterSpacing: 0.8,
                                  ),
                                ),
                              ],
                            ),
                        ],
                      ),
                    ),
                ],
              ),
            ),
          ),

          const SizedBox(width: 44),

          // ----------------------------------------------------
          // RIGHT COLUMN: Title, Badges, Synopsis, Sub/Dub Switcher, Episode Grid
          // ----------------------------------------------------
          Expanded(
            child: SingleChildScrollView(
              controller: _rightPanelScrollController,
              physics: const BouncingScrollPhysics(),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const SizedBox(height: 8),
                  // Title & Native Name
                  Text(
                    detail.englishName ?? detail.name,
                    style: const TextStyle(
                      color: Color(0xFF1E1B2E),
                      fontSize: 36,
                      fontWeight: FontWeight.w900,
                      letterSpacing: -0.6,
                      height: 1.14,
                    ),
                  ),

                  if (detail.nativeName != null && detail.nativeName!.isNotEmpty) ...[
                    const SizedBox(height: 6),
                    Text(
                      detail.nativeName!,
                      style: const TextStyle(
                        color: Color(0xFF64748B),
                        fontSize: 17,
                        fontWeight: FontWeight.w500,
                        letterSpacing: 0.2,
                      ),
                    ),
                  ],

                  const SizedBox(height: 20),

                  // Metadata Badges (Rating, Season, Format, Duration, Reaction Gauge)
                  _buildTvMetadataRow(detail),

                  const SizedBox(height: 16),

                  // Genres Wrap
                  if (detail.genres.isNotEmpty) ...[
                    _buildTvGenresRow(detail.genres),
                    const SizedBox(height: 18),
                  ],

                  // Synopsis
                  if (detail.description != null && detail.description!.isNotEmpty) ...[
                    Text(
                      detail.description!,
                      maxLines: 4,
                      overflow: TextOverflow.ellipsis,
                      style: const TextStyle(
                        color: Color(0xFF334155),
                        fontSize: 15,
                        height: 1.55,
                        letterSpacing: 0.1,
                      ),
                    ),
                    const SizedBox(height: 26),
                  ],

                  // Gradient Divider
                  Container(
                    height: 1.5,
                    decoration: BoxDecoration(
                      gradient: LinearGradient(
                        colors: [
                          const Color(0xFF7C3AED).withAlpha(100),
                          const Color(0xFFE2E8F0),
                          Colors.transparent,
                        ],
                      ),
                    ),
                    margin: const EdgeInsets.only(bottom: 26),
                  ),

                  // Episode Section Header + SUB / DUB / RAW Audio Selector
                  _buildTvEpisodesHeader(detail),

                  const SizedBox(height: 18),

                  // Episode Chunk selector (if > 100 episodes)
                  _buildTvChunkSelector(detail),

                  // TV Grid of Focusable Episode Cards
                  _buildTvEpisodesGrid(detail),

                  const SizedBox(height: 36),

                  // Characters / Voice Cast (if available)
                  if (detail.characters.isNotEmpty) ...[
                    const Text(
                      'Characters & Voice Cast',
                      style: TextStyle(
                        color: Color(0xFF1E1B2E),
                        fontSize: 22,
                        fontWeight: FontWeight.w900,
                        letterSpacing: 0.3,
                      ),
                    ),
                    const SizedBox(height: 16),
                    _buildTvCastRail(detail.characters),
                    const SizedBox(height: 48),
                  ],
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildTvMetadataRow(AnimeDetail detail) {
    final seasonStr = [
      if (detail.seasonQuarter != null) detail.seasonQuarter,
      if (detail.seasonYear != null) detail.seasonYear.toString(),
    ].join(' ');

    return Wrap(
      spacing: 12,
      runSpacing: 10,
      crossAxisAlignment: WrapCrossAlignment.center,
      children: [
        // Star score
        if (detail.score != null && detail.score! > 0)
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
            decoration: BoxDecoration(
              color: const Color(0xFFFFFBEB),
              borderRadius: BorderRadius.circular(10),
              border: Border.all(color: const Color(0xFFF59E0B), width: 1.2),
              boxShadow: [
                BoxShadow(
                  color: const Color(0xFFF59E0B).withAlpha(25),
                  blurRadius: 8,
                ),
              ],
            ),
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                const Icon(Icons.star_rounded, color: Color(0xFFF59E0B), size: 16),
                const SizedBox(width: 5),
                Text(
                  '${detail.score!.toStringAsFixed(1)} / 10',
                  style: const TextStyle(
                    color: Color(0xFF92400E),
                    fontWeight: FontWeight.w900,
                    fontSize: 13.5,
                  ),
                ),
              ],
            ),
          ),

        // Reaction Tag
        if (detail.averageScore != null && detail.averageScore! > 0)
          _buildReactionBadge(detail.averageScore!),

        // Format
        if (detail.type != null && detail.type!.isNotEmpty)
          _buildTvPill(Icons.tv_rounded, detail.type!.toUpperCase(), const Color(0xFF6D28D9)),

        // Season
        if (seasonStr.isNotEmpty)
          _buildTvPill(Icons.calendar_month_rounded, seasonStr, const Color(0xFF2563EB)),

        // Duration
        if (detail.episodeDurationMinutes != null)
          _buildTvPill(Icons.schedule_rounded, '${detail.episodeDurationMinutes}m / ep', const Color(0xFF475569)),

        // Total episodes
        if (detail.episodeCount != null)
          _buildTvPill(Icons.video_library_outlined, '${detail.episodeCount} Episodes', const Color(0xFF475569)),
      ],
    );
  }

  Widget _buildReactionBadge(int score) {
    final String emoji;
    final String label;
    final Color color;

    if (score >= 80) {
      emoji = '🔥';
      label = 'Must Watch';
      color = const Color(0xFFDC2626);
    } else if (score >= 70) {
      emoji = '✨';
      label = 'Highly Rated';
      color = const Color(0xFF7C3AED);
    } else if (score >= 60) {
      emoji = '👍';
      label = 'Well Liked';
      color = const Color(0xFF059669);
    } else {
      emoji = '👀';
      label = 'Casual Pick';
      color = const Color(0xFF64748B);
    }

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      decoration: BoxDecoration(
        color: color.withAlpha(20),
        borderRadius: BorderRadius.circular(10),
        border: Border.all(color: color.withAlpha(80), width: 1.2),
        boxShadow: [
          BoxShadow(
            color: color.withAlpha(20),
            blurRadius: 6,
          ),
        ],
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(emoji, style: const TextStyle(fontSize: 13.5)),
          const SizedBox(width: 5),
          Text(
            label,
            style: TextStyle(color: color, fontSize: 13, fontWeight: FontWeight.w800),
          ),
        ],
      ),
    );
  }

  Widget _buildTvPill(IconData icon, String text, Color color) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(10),
        border: Border.all(color: const Color(0xFFE2E8F0), width: 1.0),
        boxShadow: [
          BoxShadow(
            color: const Color(0xFF0F172A).withAlpha(8),
            blurRadius: 6,
            offset: const Offset(0, 1),
          ),
        ],
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, size: 15, color: color),
          const SizedBox(width: 6),
          Text(
            text,
            style: TextStyle(
              color: color,
              fontSize: 13,
              fontWeight: FontWeight.w700,
              letterSpacing: 0.3,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildTvGenresRow(List<String> genres) {
    return Wrap(
      spacing: 10,
      runSpacing: 8,
      children: genres.map((genre) {
        return Container(
          padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 5),
          decoration: BoxDecoration(
            color: const Color(0xFFF3E8FF),
            borderRadius: BorderRadius.circular(16),
            border: Border.all(color: const Color(0xFF8B5CF6).withAlpha(60), width: 1.0),
          ),
          child: Text(
            genre,
            style: const TextStyle(
              color: Color(0xFF6D28D9),
              fontSize: 12.5,
              fontWeight: FontWeight.w700,
              letterSpacing: 0.2,
            ),
          ),
        );
      }).toList(),
    );
  }

  Widget _buildPrimaryWatchButton(AnimeDetail detail) {
    final activeEpisodes = _getEpisodesForTab(_selectedTab).isNotEmpty
        ? _getEpisodesForTab(_selectedTab)
        : (_getEpisodesForTab('SUB').isNotEmpty ? _getEpisodesForTab('SUB') : _getEpisodesForTab('DUB'));

    String targetEp = '1';
    if (_currentProgress != null) {
      targetEp = _currentProgress!.episodeNumber;
    } else if (activeEpisodes.isNotEmpty) {
      targetEp = activeEpisodes.first;
    }

    final isResume = _currentProgress != null;
    final isLoading = _loadingEpisodeString == targetEp;

    return FocusableActionDetector(
      focusNode: _watchButtonFocusNode,
      onShowFocusHighlight: (_) => setState(() {}),
      actions: <Type, Action<Intent>>{
        ActivateIntent: CallbackAction<ActivateIntent>(
          onInvoke: (intent) {
            final startMs = isResume && _currentProgress!.episodeNumber == targetEp
                ? _currentProgress!.seekPositionMs
                : 0;
            _playEpisode(targetEp, startPositionMs: startMs);
            return null;
          },
        ),
      },
      child: Builder(
        builder: (context) {
          final isFocused = Focus.of(context).hasFocus;

          return AnimatedScale(
            scale: isFocused ? 1.08 : 1.0,
            duration: const Duration(milliseconds: 160),
            child: Container(
              width: 230,
              height: 56,
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(16),
                gradient: isFocused ? AppColors.logoGradient : AppColors.primaryGradient,
                border: Border.all(
                  color: isFocused ? Colors.white : Colors.transparent,
                  width: isFocused ? 3.0 : 0,
                ),
                boxShadow: [
                  BoxShadow(
                    color: isFocused
                        ? AppColors.primaryLight.withAlpha(180)
                        : AppColors.primaryDark.withAlpha(80),
                    blurRadius: isFocused ? 24 : 12,
                    offset: const Offset(0, 4),
                  ),
                ],
              ),
              child: Material(
                color: Colors.transparent,
                child: InkWell(
                  onTap: () {
                    final startMs = isResume && _currentProgress!.episodeNumber == targetEp
                        ? _currentProgress!.seekPositionMs
                        : 0;
                    _playEpisode(targetEp, startPositionMs: startMs);
                  },
                  borderRadius: BorderRadius.circular(16),
                  child: Center(
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        if (isLoading) ...[
                          const SizedBox(
                            width: 22,
                            height: 22,
                            child: CircularProgressIndicator(
                              strokeWidth: 2.5,
                              valueColor: AlwaysStoppedAnimation<Color>(Colors.white),
                            ),
                          ),
                          const SizedBox(width: 10),
                          const Text(
                            'Loading...',
                            style: TextStyle(
                              color: Colors.white,
                              fontSize: 15.5,
                              fontWeight: FontWeight.w800,
                            ),
                          ),
                        ] else ...[
                          Icon(
                            isResume ? Icons.play_circle_filled_rounded : Icons.play_arrow_rounded,
                            color: Colors.white,
                            size: 28,
                          ),
                          const SizedBox(width: 8),
                          Flexible(
                            child: Text(
                              isResume ? 'Resume Ep $targetEp' : 'Watch Ep $targetEp',
                              maxLines: 1,
                              overflow: TextOverflow.ellipsis,
                              style: const TextStyle(
                                color: Colors.white,
                                fontSize: 16,
                                fontWeight: FontWeight.w900,
                                letterSpacing: 0.4,
                              ),
                            ),
                          ),
                        ],
                      ],
                    ),
                  ),
                ),
              ),
            ),
          );
        },
      ),
    );
  }

  Widget _buildTvSecondaryButton({
    required FocusNode focusNode,
    required IconData icon,
    required String label,
    required VoidCallback onPressed,
  }) {
    return FocusableActionDetector(
      focusNode: focusNode,
      onShowFocusHighlight: (_) => setState(() {}),
      actions: <Type, Action<Intent>>{
        ActivateIntent: CallbackAction<ActivateIntent>(
          onInvoke: (intent) {
            onPressed();
            return null;
          },
        ),
      },
      child: Builder(
        builder: (context) {
          final isFocused = Focus.of(context).hasFocus;

          return AnimatedScale(
            scale: isFocused ? 1.06 : 1.0,
            duration: const Duration(milliseconds: 160),
            child: Container(
              width: 230,
              height: 48,
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(16),
                color: isFocused ? const Color(0xFFF3E8FF) : Colors.white,
                border: Border.all(
                  color: isFocused ? const Color(0xFF7C3AED) : const Color(0xFFE2E8F0),
                  width: isFocused ? 2.5 : 1.0,
                ),
                boxShadow: [
                  if (isFocused)
                    BoxShadow(
                      color: AppColors.primary.withAlpha(80),
                      blurRadius: 16,
                      offset: const Offset(0, 4),
                    )
                  else
                    BoxShadow(
                      color: const Color(0xFF0F172A).withAlpha(10),
                      blurRadius: 6,
                      offset: const Offset(0, 2),
                    ),
                ],
              ),
              child: Material(
                color: Colors.transparent,
                child: InkWell(
                  onTap: onPressed,
                  borderRadius: BorderRadius.circular(16),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Icon(
                        icon,
                        color: isFocused ? const Color(0xFF6D28D9) : const Color(0xFF1E1B2E),
                        size: 20,
                      ),
                      const SizedBox(width: 8),
                      Text(
                        label,
                        style: TextStyle(
                          color: isFocused ? const Color(0xFF6D28D9) : const Color(0xFF1E1B2E),
                          fontSize: 14,
                          fontWeight: FontWeight.w800,
                          letterSpacing: 0.3,
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
    );
  }

  Widget _buildTvEpisodesHeader(AnimeDetail detail) {
    final availableTabs = _getAvailableTabs();
    if (!availableTabs.contains(_selectedTab)) {
      _selectedTab = availableTabs.first;
    }

    final episodes = _getEpisodesForTab(_selectedTab);

    return Row(
      children: [
        ShaderMask(
          shaderCallback: (bounds) => AppColors.logoGradient.createShader(bounds),
          child: const Icon(Icons.video_library_rounded, color: Colors.white, size: 24),
        ),
        const SizedBox(width: 12),
        Text(
          'Episodes (${episodes.length})',
          style: const TextStyle(
            color: Color(0xFF1E1B2E),
            fontSize: 22,
            fontWeight: FontWeight.w900,
            letterSpacing: 0.2,
          ),
        ),
        const Spacer(),
        // Audio selector tabs (SUB, DUB, RAW)
        Container(
          padding: const EdgeInsets.all(4),
          decoration: BoxDecoration(
            color: const Color(0xFFF1EEF8),
            borderRadius: BorderRadius.circular(16),
            border: Border.all(color: const Color(0xFFE2E8F0), width: 1.2),
          ),
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              if (availableTabs.contains('SUB'))
                _buildTvTabItem(
                  focusNode: _subTabFocusNode,
                  label: 'SUB (${_getEpisodesForTab('SUB').length})',
                  tabId: 'SUB',
                ),
              if (availableTabs.contains('DUB'))
                _buildTvTabItem(
                  focusNode: _dubTabFocusNode,
                  label: 'DUB (${_getEpisodesForTab('DUB').length})',
                  tabId: 'DUB',
                ),
              if (availableTabs.contains('RAW'))
                _buildTvTabItem(
                  focusNode: _rawTabFocusNode,
                  label: 'RAW (${_getEpisodesForTab('RAW').length})',
                  tabId: 'RAW',
                ),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildTvTabItem({
    required FocusNode focusNode,
    required String label,
    required String tabId,
  }) {
    final isSelected = _selectedTab == tabId;

    return FocusableActionDetector(
      focusNode: focusNode,
      onShowFocusHighlight: (_) => setState(() {}),
      actions: <Type, Action<Intent>>{
        ActivateIntent: CallbackAction<ActivateIntent>(
          onInvoke: (intent) {
            setState(() {
              _selectedTab = tabId;
              _selectedChunkIndex = 0;
            });
            return null;
          },
        ),
      },
      child: Builder(
        builder: (context) {
          final isFocused = Focus.of(context).hasFocus;

          return AnimatedScale(
            scale: isFocused ? 1.05 : 1.0,
            duration: const Duration(milliseconds: 140),
            child: Container(
              margin: const EdgeInsets.symmetric(horizontal: 2),
              decoration: BoxDecoration(
                gradient: isSelected
                    ? AppColors.logoGradient
                    : (isFocused ? LinearGradient(colors: [AppColors.primary.withAlpha(50), AppColors.primary.withAlpha(25)]) : null),
                borderRadius: BorderRadius.circular(12),
                border: Border.all(
                  color: isFocused ? const Color(0xFF7C3AED) : Colors.transparent,
                  width: isFocused ? 2.0 : 0,
                ),
                boxShadow: [
                  if (isSelected || isFocused)
                    BoxShadow(
                      color: AppColors.primary.withAlpha(100),
                      blurRadius: 10,
                    ),
                ],
              ),
              child: Material(
                color: Colors.transparent,
                child: InkWell(
                  onTap: () {
                    setState(() {
                      _selectedTab = tabId;
                      _selectedChunkIndex = 0;
                    });
                  },
                  borderRadius: BorderRadius.circular(12),
                  child: Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                    child: Text(
                      label,
                      style: TextStyle(
                        color: isSelected || isFocused ? Colors.white : Colors.white60,
                        fontSize: 13,
                        fontWeight: isSelected ? FontWeight.w900 : FontWeight.w700,
                        letterSpacing: 0.3,
                      ),
                    ),
                  ),
                ),
              ),
            ),
          );
        },
      ),
    );
  }

  Widget _buildTvChunkSelector(AnimeDetail detail) {
    final episodes = _getEpisodesForTab(_selectedTab);
    final totalEpisodes = episodes.length;
    final totalChunks = (totalEpisodes / _chunkSize).ceil();

    if (totalChunks <= 1) return const SizedBox.shrink();

    // Ensure focus node list size
    while (_chunkFocusNodes.length < totalChunks) {
      _chunkFocusNodes.add(FocusNode());
    }

    return Container(
      margin: const EdgeInsets.only(bottom: 16),
      height: 40,
      child: ListView.builder(
        scrollDirection: Axis.horizontal,
        itemCount: totalChunks,
        itemBuilder: (context, idx) {
          final start = idx * _chunkSize + 1;
          final end = ((idx + 1) * _chunkSize).clamp(1, totalEpisodes);
          final isSelected = _selectedChunkIndex == idx;
          final node = _chunkFocusNodes[idx];

          return FocusableActionDetector(
            focusNode: node,
            onShowFocusHighlight: (_) => setState(() {}),
            actions: <Type, Action<Intent>>{
              ActivateIntent: CallbackAction<ActivateIntent>(
                onInvoke: (intent) {
                  setState(() {
                    _selectedChunkIndex = idx;
                  });
                  return null;
                },
              ),
            },
            child: Builder(
              builder: (ctx) {
                final isFocused = Focus.of(ctx).hasFocus;

                return AnimatedScale(
                  scale: isFocused ? 1.06 : 1.0,
                  duration: const Duration(milliseconds: 140),
                  child: Container(
                    margin: const EdgeInsets.only(right: 10),
                    decoration: BoxDecoration(
                      gradient: isSelected
                          ? AppColors.logoGradient
                          : null,
                      color: isSelected
                          ? null
                          : (isFocused ? const Color(0xFFF3E8FF) : Colors.white),
                      borderRadius: BorderRadius.circular(12),
                      border: Border.all(
                        color: isFocused ? const Color(0xFF7C3AED) : (isSelected ? Colors.transparent : const Color(0xFFE2E8F0)),
                        width: isFocused ? 2.0 : 1.0,
                      ),
                      boxShadow: [
                        BoxShadow(
                          color: const Color(0xFF0F172A).withAlpha(8),
                          blurRadius: 6,
                          offset: const Offset(0, 1),
                        ),
                      ],
                    ),
                    child: Material(
                      color: Colors.transparent,
                      child: InkWell(
                        onTap: () {
                          setState(() {
                            _selectedChunkIndex = idx;
                          });
                        },
                        borderRadius: BorderRadius.circular(12),
                        child: Padding(
                          padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
                          child: Text(
                            '$start-$end',
                            style: TextStyle(
                              color: isSelected ? Colors.white : (isFocused ? const Color(0xFF6D28D9) : const Color(0xFF1E1B2E)),
                              fontSize: 13,
                              fontWeight: FontWeight.w800,
                            ),
                          ),
                        ),
                      ),
                    ),
                  ),
                );
              },
            ),
          );
        },
      ),
    );
  }

  Widget _buildTvEpisodesGrid(AnimeDetail detail) {
    final episodes = _getEpisodesForTab(_selectedTab);
    if (episodes.isEmpty) {
      return Container(
        padding: const EdgeInsets.all(32),
        alignment: Alignment.center,
        child: const Text(
          'No episodes found for this audio option.',
          style: TextStyle(color: Color(0xFF64748B), fontSize: 15),
        ),
      );
    }

    final totalEpisodes = episodes.length;
    final start = _selectedChunkIndex * _chunkSize;
    final end = (start + _chunkSize).clamp(0, totalEpisodes);
    final displayedEpisodes = episodes.sublist(start, end);

    // Keep focus nodes count aligned
    while (_episodeFocusNodes.length < displayedEpisodes.length) {
      _episodeFocusNodes.add(FocusNode());
    }

    return GridView.builder(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      itemCount: displayedEpisodes.length,
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 6,
        mainAxisSpacing: 12,
        crossAxisSpacing: 12,
        childAspectRatio: 1.65,
      ),
      itemBuilder: (context, index) {
        final epNum = displayedEpisodes[index];
        final node = _episodeFocusNodes[index];
        final isCurrent = _currentProgress?.episodeNumber == epNum;
        final isLoading = _loadingEpisodeString == epNum;

        return _TvEpisodeTile(
          focusNode: node,
          episodeNumber: epNum,
          isCurrentWatched: isCurrent,
          isLoading: isLoading,
          onTap: () {
            final startMs = isCurrent ? (_currentProgress?.seekPositionMs ?? 0) : 0;
            _playEpisode(epNum, startPositionMs: startMs);
          },
        );
      },
    );
  }

  Widget _buildTvCastRail(List<AnimeCharacter> characters) {
    return SizedBox(
      height: 135,
      child: ListView.builder(
        scrollDirection: Axis.horizontal,
        physics: const BouncingScrollPhysics(),
        itemCount: characters.length,
        itemBuilder: (context, index) {
          final char = characters[index];
          return Container(
            width: 96,
            margin: const EdgeInsets.only(right: 16),
            child: Column(
              children: [
                Container(
                  width: 70,
                  height: 70,
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    color: Colors.white,
                    border: Border.all(color: const Color(0xFFE2E8F0), width: 1.8),
                    boxShadow: [
                      BoxShadow(
                        color: const Color(0xFF0F172A).withAlpha(12),
                        blurRadius: 8,
                        offset: const Offset(0, 3),
                      ),
                    ],
                  ),
                  clipBehavior: Clip.antiAlias,
                  child: char.imageUrl != null && char.imageUrl!.isNotEmpty
                      ? Image.network(
                          char.imageUrl!,
                          fit: BoxFit.cover,
                          errorBuilder: (context, error, stackTrace) => const Icon(Icons.person, color: Color(0xFF94A3B8)),
                        )
                      : const Icon(Icons.person, color: Color(0xFF94A3B8)),
                ),
                const SizedBox(height: 8),
                Text(
                  char.name,
                  maxLines: 1,
                  textAlign: TextAlign.center,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(
                    color: Color(0xFF1E1B2E),
                    fontSize: 12,
                    fontWeight: FontWeight.w700,
                  ),
                ),
                const SizedBox(height: 2),
                Text(
                  char.role,
                  maxLines: 1,
                  style: TextStyle(
                    color: char.role.toLowerCase() == 'main'
                        ? const Color(0xFF7C3AED)
                        : const Color(0xFF64748B),
                    fontSize: 10.5,
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ],
            ),
          );
        },
      ),
    );
  }

  Widget _buildFocusableCircleButton({
    required FocusNode focusNode,
    required IconData icon,
    required String tooltip,
    required VoidCallback onPressed,
  }) {
    return FocusableActionDetector(
      focusNode: focusNode,
      onShowFocusHighlight: (_) => setState(() {}),
      actions: <Type, Action<Intent>>{
        ActivateIntent: CallbackAction<ActivateIntent>(
          onInvoke: (intent) {
            onPressed();
            return null;
          },
        ),
      },
      child: Builder(
        builder: (context) {
          final isFocused = Focus.of(context).hasFocus;

          return AnimatedScale(
            scale: isFocused ? 1.18 : 1.0,
            duration: const Duration(milliseconds: 150),
            child: Container(
              width: 48,
              height: 48,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                color: isFocused ? AppColors.primary : Colors.white,
                border: Border.all(
                  color: isFocused ? Colors.transparent : const Color(0xFFE2E8F0),
                  width: isFocused ? 2.5 : 1.2,
                ),
                boxShadow: [
                  BoxShadow(
                    color: isFocused ? AppColors.primary.withAlpha(140) : const Color(0xFF0F172A).withAlpha(15),
                    blurRadius: isFocused ? 16 : 8,
                  ),
                ],
              ),
              child: Material(
                color: Colors.transparent,
                child: InkWell(
                  customBorder: const CircleBorder(),
                  onTap: onPressed,
                  child: Icon(
                    icon,
                    size: 22,
                    color: isFocused ? Colors.white : const Color(0xFF1E1B2E),
                  ),
                ),
              ),
            ),
          );
        },
      ),
    );
  }

  Widget _buildTvPrimaryButton({
    required FocusNode focusNode,
    required String label,
    required IconData icon,
    required VoidCallback onPressed,
  }) {
    return FocusableActionDetector(
      focusNode: focusNode,
      onShowFocusHighlight: (_) => setState(() {}),
      actions: <Type, Action<Intent>>{
        ActivateIntent: CallbackAction<ActivateIntent>(
          onInvoke: (intent) {
            onPressed();
            return null;
          },
        ),
      },
      child: Builder(
        builder: (context) {
          final isFocused = Focus.of(context).hasFocus;

          return AnimatedScale(
            scale: isFocused ? 1.05 : 1.0,
            duration: const Duration(milliseconds: 150),
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 26, vertical: 14),
              decoration: BoxDecoration(
                gradient: isFocused ? AppColors.logoGradient : AppColors.primaryGradient,
                borderRadius: BorderRadius.circular(16),
                border: Border.all(
                  color: isFocused ? Colors.white : Colors.transparent,
                  width: isFocused ? 2.5 : 0,
                ),
                boxShadow: [
                  if (isFocused)
                    BoxShadow(
                      color: AppColors.primary.withAlpha(160),
                      blurRadius: 18,
                    ),
                ],
              ),
              child: Material(
                color: Colors.transparent,
                child: InkWell(
                  onTap: onPressed,
                  borderRadius: BorderRadius.circular(16),
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Icon(icon, color: Colors.white, size: 22),
                      const SizedBox(width: 8),
                      Text(
                        label,
                        style: const TextStyle(
                          color: Colors.white,
                          fontWeight: FontWeight.w900,
                          fontSize: 15,
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
    );
  }
}

class _TvEpisodeTile extends StatelessWidget {
  final FocusNode focusNode;
  final String episodeNumber;
  final bool isCurrentWatched;
  final bool isLoading;
  final VoidCallback onTap;

  const _TvEpisodeTile({
    required this.focusNode,
    required this.episodeNumber,
    required this.isCurrentWatched,
    required this.isLoading,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return FocusableActionDetector(
      focusNode: focusNode,
      actions: <Type, Action<Intent>>{
        ActivateIntent: CallbackAction<ActivateIntent>(
          onInvoke: (intent) {
            onTap();
            return null;
          },
        ),
      },
      child: Builder(
        builder: (context) {
          final isFocused = Focus.of(context).hasFocus;

          return AnimatedScale(
            scale: isFocused ? 1.08 : 1.0,
            duration: const Duration(milliseconds: 140),
            child: Container(
              decoration: BoxDecoration(
                gradient: isFocused
                    ? AppColors.logoGradient
                    : null,
                color: isFocused
                    ? null
                    : (isCurrentWatched ? const Color(0xFFF3E8FF) : Colors.white),
                borderRadius: BorderRadius.circular(14),
                border: Border.all(
                  color: isFocused
                      ? Colors.white
                      : (isCurrentWatched ? const Color(0xFF8B5CF6).withAlpha(120) : const Color(0xFFE2E8F0)),
                  width: isFocused ? 2.5 : (isCurrentWatched ? 1.6 : 1.0),
                ),
                boxShadow: [
                  if (isFocused)
                    BoxShadow(
                      color: AppColors.primary.withAlpha(140),
                      blurRadius: 16,
                      offset: const Offset(0, 4),
                    )
                  else if (isCurrentWatched)
                    BoxShadow(
                      color: const Color(0xFF8B5CF6).withAlpha(35),
                      blurRadius: 8,
                    )
                  else
                    BoxShadow(
                      color: const Color(0xFF0F172A).withAlpha(10),
                      blurRadius: 4,
                      offset: const Offset(0, 2),
                    ),
                ],
              ),
              child: Material(
                color: Colors.transparent,
                child: InkWell(
                  onTap: onTap,
                  borderRadius: BorderRadius.circular(14),
                  child: Stack(
                    alignment: Alignment.center,
                    children: [
                      if (isLoading)
                        const SizedBox(
                          width: 20,
                          height: 20,
                          child: CircularProgressIndicator(
                            strokeWidth: 2.2,
                            valueColor: AlwaysStoppedAnimation<Color>(AppColors.primary),
                          ),
                        )
                      else
                        Row(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            if (isCurrentWatched) ...[
                              Icon(
                                Icons.play_circle_fill_rounded,
                                size: 16,
                                color: isFocused ? Colors.white : const Color(0xFF6D28D9),
                              ),
                              const SizedBox(width: 5),
                            ],
                            Text(
                              'EP $episodeNumber',
                              style: TextStyle(
                                color: isFocused ? Colors.white : (isCurrentWatched ? const Color(0xFF6D28D9) : const Color(0xFF1E1B2E)),
                                fontSize: 13.5,
                                fontWeight: isFocused || isCurrentWatched
                                    ? FontWeight.w900
                                    : FontWeight.w700,
                              ),
                            ),
                          ],
                        ),

                      if (isCurrentWatched && !isFocused)
                        Positioned(
                          bottom: 0,
                          left: 14,
                          right: 14,
                          child: Container(
                            height: 3,
                            decoration: BoxDecoration(
                              gradient: AppColors.logoGradient,
                              borderRadius: BorderRadius.circular(2),
                            ),
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
    );
  }
}

class _TvPopIntent extends Intent {
  const _TvPopIntent();
}
