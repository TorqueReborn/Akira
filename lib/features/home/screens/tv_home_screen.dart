import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../../../theme/app_colors.dart';
import '../models/anime_show.dart';
import '../services/anime_repository.dart';
import '../widgets/tv_anime_card.dart';
import '../widgets/tv_featured_banner.dart';
import 'tv_anime_detail_screen.dart';

/// TV Optimized Home Screen featuring a full-screen spotlight banner
/// showing top anime (limited to 5) at the top, followed by an infinite list row
/// with automatic full-screen focus scrolling and generous 10-foot TV spacing.
class TvHomeScreen extends StatefulWidget {
  const TvHomeScreen({super.key});

  @override
  State<TvHomeScreen> createState() => _TvHomeScreenState();
}

class _TvCategorySection {
  final String title;
  final String sortBy;
  final ScrollController scrollController = ScrollController();
  final Map<int, FocusNode> focusNodes = {};
  List<AnimeShow> shows = [];
  AnimeShow? selectedAnime;
  int currentPage = 1;
  bool isLoadingMore = false;
  bool hasMore = true;
  int lastFocusedIndex = 0;

  _TvCategorySection({required this.title, required this.sortBy});

  FocusNode getFocusNode(int index, VoidCallback onFocused) {
    return focusNodes.putIfAbsent(index, () {
      final node = FocusNode();
      node.addListener(() {
        if (node.hasFocus) onFocused();
      });
      return node;
    });
  }

  void dispose() {
    scrollController.dispose();
    for (final node in focusNodes.values) {
      node.dispose();
    }
  }
}

class _TvHomeScreenState extends State<TvHomeScreen> {
  bool _isLoading = true;
  String? _errorMessage;

  List<AnimeShow> _featuredShows = [];
  
  // Main Page Scroll Controller for TV navigation scrolling
  final ScrollController _mainScrollController = ScrollController();

  // 4 TV Category Sections
  late final List<_TvCategorySection> _sections;

  // Search & Focus state
  bool _isSearchActive = false;
  final TextEditingController _searchController = TextEditingController();
  final FocusNode _searchInputFocusNode = FocusNode();
  final FocusNode _searchButtonFocusNode = FocusNode();
  final FocusNode _bannerFocusNode = FocusNode();
  List<AnimeShow> _searchResults = [];
  final Map<int, FocusNode> _searchResultFocusNodes = {};
  bool _isSearching = false;
  Timer? _debounceTimer;

  int _activeRowIndex = 0;

  @override
  void initState() {
    super.initState();
    _sections = [
      _TvCategorySection(title: 'Latest Updates', sortBy: 'Latest_Update'),
      _TvCategorySection(title: 'Top Rated', sortBy: 'Top'),
      _TvCategorySection(title: 'Popular', sortBy: 'Popular'),
      _TvCategorySection(title: 'New Releases', sortBy: 'Release_Year'),
    ];

    _loadData();
    _bannerFocusNode.addListener(_onBannerFocusChange);
    _searchButtonFocusNode.addListener(_onBannerFocusChange);
  }

  @override
  void dispose() {
    _mainScrollController.dispose();
    for (final section in _sections) {
      section.dispose();
    }
    for (final node in _searchResultFocusNodes.values) {
      node.dispose();
    }
    _searchController.dispose();
    _searchInputFocusNode.dispose();
    _searchButtonFocusNode.dispose();
    _bannerFocusNode.dispose();
    _debounceTimer?.cancel();
    super.dispose();
  }

  void _onBannerFocusChange() {
    if ((_bannerFocusNode.hasFocus || _searchButtonFocusNode.hasFocus) &&
        _mainScrollController.hasClients) {
      _mainScrollController.animateTo(
        0.0,
        duration: const Duration(milliseconds: 350),
        curve: Curves.easeInOutCubic,
      );
    }
  }

  void _scrollToRowIndex(int rowIndex) {
    if (!_mainScrollController.hasClients) return;
    _activeRowIndex = rowIndex;
    final screenHeight = MediaQuery.of(context).size.height;
    
    // Each row block is: Section header (~48px) + gap (16px) + Container (362px) + bottom margin (48px) = ~474px
    const double rowHeight = 474.0;
    
    // We want the section header and container to sit comfortably in the viewport, slightly below top edge
    final double target = (screenHeight + (rowIndex * rowHeight) - 30.0)
        .clamp(0.0, _mainScrollController.position.maxScrollExtent);

    _mainScrollController.animateTo(
      target,
      duration: const Duration(milliseconds: 320),
      curve: Curves.easeOutCubic,
    );
  }

  Future<void> _loadData() async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final results = await Future.wait([
        AnimeRepository.fetchTopRankedAnime(dateRange: 1, size: 5).catchError((_) => <AnimeShow>[]),
        AnimeRepository.fetchAnimeList(page: 1, sortBy: 'Latest_Update').catchError((_) => const AnimePageResult(shows: [], total: 0, page: 1)),
        AnimeRepository.fetchAnimeList(page: 1, sortBy: 'Top').catchError((_) => const AnimePageResult(shows: [], total: 0, page: 1)),
        AnimeRepository.fetchAnimeList(page: 1, sortBy: 'Popular').catchError((_) => const AnimePageResult(shows: [], total: 0, page: 1)),
        AnimeRepository.fetchAnimeList(page: 1, sortBy: 'Release_Year').catchError((_) => const AnimePageResult(shows: [], total: 0, page: 1)),
      ]);

      if (!mounted) return;

      final topRanked = results[0] as List<AnimeShow>;
      final latestResult = results[1] as AnimePageResult;
      final topResult = results[2] as AnimePageResult;
      final popResult = results[3] as AnimePageResult;
      final newResult = results[4] as AnimePageResult;

      final spotlightShows = topRanked.isNotEmpty ? topRanked : latestResult.shows;

      setState(() {
        _featuredShows = spotlightShows.take(5).toList();

        // 1. Latest Updates
        _sections[0].shows = latestResult.shows;
        _sections[0].selectedAnime = latestResult.shows.isNotEmpty ? latestResult.shows.first : null;
        _sections[0].hasMore = latestResult.shows.isNotEmpty;

        // 2. Top Rated
        _sections[1].shows = topResult.shows;
        _sections[1].selectedAnime = topResult.shows.isNotEmpty ? topResult.shows.first : null;
        _sections[1].hasMore = topResult.shows.isNotEmpty;

        // 3. Popular
        _sections[2].shows = popResult.shows;
        _sections[2].selectedAnime = popResult.shows.isNotEmpty ? popResult.shows.first : null;
        _sections[2].hasMore = popResult.shows.isNotEmpty;

        // 4. New Releases
        _sections[3].shows = newResult.shows;
        _sections[3].selectedAnime = newResult.shows.isNotEmpty ? newResult.shows.first : null;
        _sections[3].hasMore = newResult.shows.isNotEmpty;

        _isLoading = false;
      });
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _errorMessage = e.toString();
        _isLoading = false;
      });
    }
  }

  Future<void> _loadMoreForSection(_TvCategorySection section) async {
    if (section.isLoadingMore || !section.hasMore) return;
    setState(() => section.isLoadingMore = true);
    try {
      final nextPage = section.currentPage + 1;
      final result = await AnimeRepository.fetchAnimeList(page: nextPage, sortBy: section.sortBy);
      if (!mounted) return;
      setState(() {
        section.shows.addAll(result.shows);
        section.currentPage = nextPage;
        section.hasMore = result.shows.isNotEmpty;
        section.isLoadingMore = false;
      });
    } catch (_) {
      if (!mounted) return;
      setState(() => section.isLoadingMore = false);
    }
  }

  void _navigateToDetail(AnimeShow anime) {
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (_) => TvAnimeDetailScreen(
          animeId: anime.id,
          initialTitle: anime.englishName ?? anime.name,
          initialPoster: anime.thumbnail,
        ),
      ),
    );
  }

  FocusNode _getSearchResultFocusNode(int index) {
    return _searchResultFocusNodes.putIfAbsent(index, () => FocusNode());
  }

  void _closeSearch() {
    if (!_isSearchActive) return;
    _searchInputFocusNode.unfocus();
    FocusManager.instance.primaryFocus?.unfocus();
    setState(() {
      _isSearchActive = false;
      _searchController.clear();
      _searchResults.clear();
      _isSearching = false;
    });
    Future.microtask(() {
      if (mounted) {
        _searchButtonFocusNode.requestFocus();
      }
    });
  }

  void _toggleSearch() {
    if (_isSearchActive) {
      _closeSearch();
    } else {
      setState(() {
        _isSearchActive = true;
      });
      Future.delayed(const Duration(milliseconds: 100), () {
        if (mounted) _searchInputFocusNode.requestFocus();
      });
    }
  }

  void _onSearchChanged(String query) {
    _debounceTimer?.cancel();
    if (query.trim().isEmpty) {
      setState(() {
        _searchResults = [];
        _isSearching = false;
      });
      return;
    }

    setState(() => _isSearching = true);
    _debounceTimer = Timer(const Duration(milliseconds: 400), () async {
      try {
        final result = await AnimeRepository.fetchAnimeList(page: 1, searchQuery: query);
        if (!mounted) return;
        setState(() {
          _searchResults = result.shows;
          _isSearching = false;
        });
      } catch (_) {
        if (!mounted) return;
        setState(() => _isSearching = false);
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading) {
      return const Scaffold(
        backgroundColor: AppColors.background,
        body: Center(
          child: CircularProgressIndicator(color: AppColors.primary),
        ),
      );
    }

    if (_errorMessage != null) {
      return Scaffold(
        backgroundColor: AppColors.background,
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(Icons.error_outline_rounded, color: Colors.redAccent, size: 64),
              const SizedBox(height: 16),
              const Text(
                'Failed to load anime catalog',
                style: TextStyle(color: AppColors.textPrimary, fontSize: 22, fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 8),
              Text(_errorMessage!, style: const TextStyle(color: AppColors.textSecondary, fontSize: 14)),
              const SizedBox(height: 24),
              ElevatedButton(
                onPressed: _loadData,
                child: const Text('Retry'),
              ),
            ],
          ),
        ),
      );
    }

    final firstSection = _sections.isNotEmpty ? _sections[0] : null;
    final initialCardFocus = firstSection?.getFocusNode(
      firstSection.lastFocusedIndex,
      () => _scrollToRowIndex(0),
    );

    return PopScope(
      canPop: !_isSearchActive,
      onPopInvokedWithResult: (didPop, result) {
        if (didPop) return;
        if (_isSearchActive) {
          _closeSearch();
        }
      },
      child: Scaffold(
        backgroundColor: AppColors.background,
        body: AnimatedSwitcher(
          duration: const Duration(milliseconds: 280),
          switchInCurve: Curves.easeOutCubic,
          switchOutCurve: Curves.easeInCubic,
          transitionBuilder: (child, animation) {
            return FadeTransition(
              opacity: animation,
              child: child,
            );
          },
          child: _isSearchActive && (_searchController.text.trim().isNotEmpty || _searchResults.isNotEmpty)
              ? KeyedSubtree(
                  key: const ValueKey('search_results_active_view'),
                  child: _buildSearchResultsView(),
                )
              : KeyedSubtree(
                  key: const ValueKey('home_main_feed_view'),
                  child: SingleChildScrollView(
                    controller: _mainScrollController,
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        // Full screen covering spotlight showing anime (100% screen height)
                        TvFeaturedBanner(
                          featuredShows: _featuredShows,
                          onTap: _navigateToDetail,
                          focusNode: _bannerFocusNode,
                          nextFocusNode: initialCardFocus,
                          onToggleSearch: _toggleSearch,
                          isSearchActive: _isSearchActive,
                          searchController: _searchController,
                          searchInputFocusNode: _searchInputFocusNode,
                          searchButtonFocusNode: _searchButtonFocusNode,
                          onSearchChanged: _onSearchChanged,
                        ),
                        const SizedBox(height: 32),

                        // Dynamic list of TV Sections (Latest Updates, Top Rated, Popular, New Releases)
                        ..._sections.asMap().entries.map((entry) {
                          final sectionIndex = entry.key;
                          final section = entry.value;
                          return Padding(
                            padding: const EdgeInsets.only(bottom: 48.0),
                            child: _buildTvCategoryRow(sectionIndex, section),
                          );
                        }),

                        const SizedBox(height: 140),
                      ],
                    ),
                  ),
                ),
        ),
      ),
    );
  }

  void _onCardFocusChange(int sectionIndex, int cardIndex, bool isFocused, AnimeShow anime) {
    if (isFocused) {
      final section = _sections[sectionIndex];
      section.lastFocusedIndex = cardIndex;
      setState(() {
        section.selectedAnime = anime;
      });
      _scrollToRowIndex(sectionIndex);
      _scrollCardToFixedPosition(section, cardIndex);

      // Proactively prefetch next batch
      if (cardIndex >= section.shows.length - 6 && !section.isLoadingMore && section.hasMore) {
        _loadMoreForSection(section);
      }
    }
  }

  void _scrollCardToFixedPosition(_TvCategorySection section, int cardIndex) {
    if (!section.scrollController.hasClients) return;

    // Card width (200) + horizontal margins (10 left + 10 right = 20) = 220
    const double itemWidth = 220.0;
    final double targetOffset = cardIndex * itemWidth;

    final double maxScroll = section.scrollController.position.maxScrollExtent;
    final double clampedOffset = targetOffset.clamp(0.0, maxScroll);

    section.scrollController.animateTo(
      clampedOffset,
      duration: const Duration(milliseconds: 280),
      curve: Curves.easeOutCubic,
    );
  }

  Widget _buildTvCategoryRow(int sectionIndex, _TvCategorySection section) {
    final activeAnime = section.selectedAnime ?? (section.shows.isNotEmpty ? section.shows.first : null);
    final activeTitle = activeAnime?.englishName ?? activeAnime?.name ?? '';
    final isSectionActive = _activeRowIndex == sectionIndex;

    return AnimatedOpacity(
      opacity: isSectionActive ? 1.0 : 0.78,
      duration: const Duration(milliseconds: 250),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Section Header: Pure Title
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 28),
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                ShaderMask(
                  shaderCallback: (bounds) => isSectionActive
                      ? AppColors.logoGradient.createShader(bounds)
                      : const LinearGradient(colors: [Color(0xFF1E1B2E), Color(0xFF475569)]).createShader(bounds),
                  child: Text(
                    section.title,
                    style: TextStyle(
                      color: AppColors.textPrimary,
                      fontSize: isSectionActive ? 32 : 28,
                      fontWeight: FontWeight.w900,
                      letterSpacing: 0.8,
                    ),
                  ),
                ),

                if (section.isLoadingMore) ...[
                  const SizedBox(width: 14),
                  const SizedBox(
                    width: 20,
                    height: 20,
                    child: CircularProgressIndicator(
                      strokeWidth: 2.2,
                      color: AppColors.primary,
                    ),
                  ),
                ],
              ],
            ),
          ),
          const SizedBox(height: 16),

          // Unified Shelf: Full-Width Light Surface Ribbon Container
          AnimatedContainer(
            duration: const Duration(milliseconds: 250),
            margin: EdgeInsets.zero,
            padding: const EdgeInsets.symmetric(vertical: 18),
            decoration: BoxDecoration(
              gradient: LinearGradient(
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
                colors: isSectionActive
                    ? const [
                        Color(0xFFFFFFFF),
                        Color(0xFFF9F6FF),
                        Color(0xFFF3EBFF),
                      ]
                    : const [
                        Color(0xFFFCFBFF),
                        Color(0xFFF7F3FD),
                        Color(0xFFF0E8FA),
                      ],
              ),
              boxShadow: [
                BoxShadow(
                  color: isSectionActive
                      ? const Color(0xFF7C3AED).withAlpha(22)
                      : const Color(0xFF0F172A).withAlpha(10),
                  blurRadius: isSectionActive ? 24 : 12,
                  offset: const Offset(0, 6),
                ),
              ],
            ),
            child: SizedBox(
              height: 330,
              child: Row(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  // Left Stage: Clean, balanced, TV broadcast info layout
                  Container(
                    width: 380,
                    margin: const EdgeInsets.only(left: 28, right: 20),
                    child: activeAnime != null
                        ? AnimatedSwitcher(
                            duration: const Duration(milliseconds: 220),
                            child: Column(
                              key: ValueKey(activeAnime.id),
                              crossAxisAlignment: CrossAxisAlignment.start,
                              mainAxisAlignment: MainAxisAlignment.center,
                              children: [
                                // 1. Anime Title (At Top)
                                Text(
                                  activeTitle,
                                  maxLines: 4,
                                  overflow: TextOverflow.ellipsis,
                                  style: const TextStyle(
                                    color: AppColors.textPrimary,
                                    fontSize: 28,
                                    fontWeight: FontWeight.w900,
                                    height: 1.18,
                                    letterSpacing: -0.3,
                                  ),
                                ),
                                const SizedBox(height: 14),

                                // 2. Metadata Row: Star Rating Pill + Format Badge + Season/Year
                                Row(
                                  crossAxisAlignment: CrossAxisAlignment.center,
                                  children: [
                                    Container(
                                      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
                                      decoration: BoxDecoration(
                                        color: const Color(0xFFFFFBEB),
                                        borderRadius: BorderRadius.circular(8),
                                        border: Border.all(
                                          color: const Color(0xFFF59E0B),
                                          width: 1.2,
                                        ),
                                        boxShadow: [
                                          BoxShadow(
                                            color: const Color(0xFFF59E0B).withAlpha(30),
                                            blurRadius: 8,
                                          ),
                                        ],
                                      ),
                                      child: Row(
                                        mainAxisSize: MainAxisSize.min,
                                        children: [
                                          const Icon(
                                            Icons.star_rounded,
                                            color: Color(0xFFF59E0B),
                                            size: 16,
                                          ),
                                          const SizedBox(width: 4),
                                          Text(
                                            (activeAnime.score != null && activeAnime.score! > 0)
                                                ? activeAnime.score!.toStringAsFixed(1)
                                                : 'N/A',
                                            style: const TextStyle(
                                              color: Color(0xFF92400E),
                                              fontSize: 13.5,
                                              fontWeight: FontWeight.w900,
                                            ),
                                          ),
                                        ],
                                      ),
                                    ),
                                    const SizedBox(width: 10),
                                    if (activeAnime.type != null && activeAnime.type!.isNotEmpty) ...[
                                      Container(
                                        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
                                        decoration: BoxDecoration(
                                          color: const Color(0xFFF3E8FF),
                                          borderRadius: BorderRadius.circular(8),
                                          border: Border.all(
                                            color: const Color(0xFF8B5CF6).withAlpha(100),
                                            width: 1.0,
                                          ),
                                        ),
                                        child: Text(
                                          activeAnime.type!.toUpperCase(),
                                          style: const TextStyle(
                                            color: Color(0xFF6D28D9),
                                            fontSize: 12,
                                            fontWeight: FontWeight.w800,
                                            letterSpacing: 0.8,
                                          ),
                                        ),
                                      ),
                                      const SizedBox(width: 10),
                                    ],
                                    if (activeAnime.availableEpisodesSub > 0 || activeAnime.availableEpisodesDub > 0) ...[
                                      Container(
                                        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
                                        decoration: BoxDecoration(
                                          color: const Color(0xFFEDE9FE),
                                          borderRadius: BorderRadius.circular(8),
                                          border: Border.all(
                                            color: const Color(0xFF7C3AED).withAlpha(120),
                                            width: 1.0,
                                          ),
                                        ),
                                        child: Text(
                                          activeAnime.availableEpisodesSub > 0
                                              ? '${activeAnime.availableEpisodesSub} EPS'
                                              : '${activeAnime.availableEpisodesDub} EPS',
                                          style: const TextStyle(
                                            color: Color(0xFF5B21B6),
                                            fontSize: 12,
                                            fontWeight: FontWeight.w800,
                                            letterSpacing: 0.6,
                                          ),
                                        ),
                                      ),
                                      const SizedBox(width: 10),
                                    ],
                                    if (activeAnime.seasonYear != null) ...[
                                      Expanded(
                                        child: Text(
                                          '${activeAnime.seasonQuarter != null ? '${activeAnime.seasonQuarter} ' : ''}${activeAnime.seasonYear}',
                                          maxLines: 1,
                                          overflow: TextOverflow.ellipsis,
                                          style: const TextStyle(
                                            color: AppColors.textSecondary,
                                            fontSize: 14,
                                            fontWeight: FontWeight.w600,
                                          ),
                                        ),
                                      ),
                                    ],
                                  ],
                                ),
                                const SizedBox(height: 18),

                                // 3. Primary CTA Button: Watch Now
                                InkWell(
                                  onTap: () => _navigateToDetail(activeAnime),
                                  borderRadius: BorderRadius.circular(12),
                                  child: Container(
                                    padding: const EdgeInsets.symmetric(horizontal: 22, vertical: 10),
                                    decoration: BoxDecoration(
                                      gradient: AppColors.logoGradient,
                                      borderRadius: BorderRadius.circular(12),
                                      boxShadow: [
                                        BoxShadow(
                                          color: AppColors.primary.withAlpha(80),
                                          blurRadius: 12,
                                          offset: const Offset(0, 3),
                                        ),
                                      ],
                                    ),
                                    child: Row(
                                      mainAxisSize: MainAxisSize.min,
                                      children: [
                                        const Icon(
                                          Icons.play_arrow_rounded,
                                          color: Colors.white,
                                          size: 20,
                                        ),
                                        const SizedBox(width: 6),
                                        const Text(
                                          'Watch Now',
                                          style: TextStyle(
                                            color: Colors.white,
                                            fontSize: 13.5,
                                            fontWeight: FontWeight.w800,
                                            letterSpacing: 0.3,
                                          ),
                                        ),
                                      ],
                                    ),
                                  ),
                                ),
                              ],
                            ),
                          )
                        : const SizedBox.shrink(),
                  ),

                  // Architectural Luminous Divider
                  Container(
                    width: 1.5,
                    margin: const EdgeInsets.symmetric(vertical: 22),
                    decoration: BoxDecoration(
                      gradient: LinearGradient(
                        begin: Alignment.topCenter,
                        end: Alignment.bottomCenter,
                        colors: [
                          Colors.transparent,
                          isSectionActive
                              ? const Color(0xFF8B5CF6).withAlpha(160)
                              : const Color(0xFFCBD5E1).withAlpha(140),
                          Colors.transparent,
                        ],
                        stops: const [0.0, 0.5, 1.0],
                      ),
                    ),
                  ),
                  const SizedBox(width: 12),

                  // Right-side horizontal poster card list
                  Expanded(
                    child: ListView.builder(
                      controller: section.scrollController,
                      scrollDirection: Axis.horizontal,
                      cacheExtent: 1400.0,
                      padding: const EdgeInsets.only(right: 28, top: 4, bottom: 4),
                      itemCount: section.shows.length + (section.isLoadingMore ? 1 : 0),
                      itemBuilder: (context, cardIndex) {
                        if (cardIndex >= section.shows.length) {
                          return Container(
                            width: 140,
                            alignment: Alignment.center,
                            child: const CircularProgressIndicator(color: AppColors.primary),
                          );
                        }
                        final show = section.shows[cardIndex];
                        return Container(
                          width: 200,
                          margin: const EdgeInsets.symmetric(horizontal: 10),
                          child: TvAnimeCard(
                            anime: show,
                            onTap: () => _navigateToDetail(show),
                            focusNode: section.getFocusNode(
                              cardIndex,
                              () => _scrollToRowIndex(sectionIndex),
                            ),
                            onFocusChange: (isFocused) =>
                                _onCardFocusChange(sectionIndex, cardIndex, isFocused, show),
                            onArrowUp: () {
                              if (sectionIndex == 0) {
                                _bannerFocusNode.requestFocus();
                              } else {
                                final prevSection = _sections[sectionIndex - 1];
                                final targetIndex = prevSection.lastFocusedIndex.clamp(0, prevSection.shows.isEmpty ? 0 : prevSection.shows.length - 1);
                                prevSection.getFocusNode(targetIndex, () => _scrollToRowIndex(sectionIndex - 1)).requestFocus();
                              }
                            },
                            onArrowDown: () {
                              if (sectionIndex < _sections.length - 1) {
                                final nextSection = _sections[sectionIndex + 1];
                                final targetIndex = nextSection.lastFocusedIndex.clamp(0, nextSection.shows.isEmpty ? 0 : nextSection.shows.length - 1);
                                nextSection.getFocusNode(targetIndex, () => _scrollToRowIndex(sectionIndex + 1)).requestFocus();
                              }
                            },
                            onArrowRight: () {
                              if (cardIndex < section.shows.length - 1) {
                                section.getFocusNode(cardIndex + 1, () => _scrollToRowIndex(sectionIndex)).requestFocus();
                              } else if (section.hasMore && !section.isLoadingMore) {
                                _loadMoreForSection(section);
                              }
                            },
                          ),
                        );
                      },
                    ),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSearchResultsView() {
    final isInputFocused = _searchInputFocusNode.hasFocus;

    return Column(
      children: [
        Container(
          padding: const EdgeInsets.fromLTRB(40, 24, 40, 16),
          child: Row(
            children: [
              ShaderMask(
                shaderCallback: (bounds) => AppColors.logoGradient.createShader(bounds),
                child: const Text(
                  'AKIRA',
                  style: TextStyle(
                    color: Colors.white,
                    fontSize: 34,
                    fontWeight: FontWeight.w900,
                    letterSpacing: 4.0,
                  ),
                ),
              ),
              const SizedBox(width: 32),
              Expanded(
                child: AnimatedContainer(
                  duration: const Duration(milliseconds: 200),
                  height: 58,
                  decoration: BoxDecoration(
                    color: Colors.white,
                    borderRadius: BorderRadius.circular(16),
                    border: Border.all(
                      color: isInputFocused
                          ? const Color(0xFF7C3AED)
                          : const Color(0xFFE2E8F0),
                      width: isInputFocused ? 2.2 : 1.2,
                    ),
                    boxShadow: isInputFocused
                        ? [
                            BoxShadow(
                              color: const Color(0xFF8B5CF6).withAlpha(50),
                              blurRadius: 18,
                              spreadRadius: 1,
                            ),
                            BoxShadow(
                              color: const Color(0xFF0F172A).withAlpha(12),
                              blurRadius: 8,
                              offset: const Offset(0, 3),
                            ),
                          ]
                        : [
                            BoxShadow(
                              color: const Color(0xFF0F172A).withAlpha(10),
                              blurRadius: 8,
                              offset: const Offset(0, 3),
                            ),
                          ],
                  ),
                  child: Row(
                    children: [
                      const SizedBox(width: 18),
                      Icon(
                        Icons.search_rounded,
                        color: isInputFocused ? const Color(0xFF7C3AED) : const Color(0xFF94A3B8),
                        size: 26,
                      ),
                      const SizedBox(width: 14),
                      Expanded(
                        child: Focus(
                          onKeyEvent: (node, event) {
                            if (event is KeyDownEvent || event is KeyRepeatEvent) {
                              if (event.logicalKey == LogicalKeyboardKey.arrowDown) {
                                _searchInputFocusNode.unfocus();
                                if (_searchResults.isNotEmpty) {
                                  _getSearchResultFocusNode(0).requestFocus();
                                } else {
                                  _searchButtonFocusNode.requestFocus();
                                }
                                return KeyEventResult.handled;
                              }
                              if (event.logicalKey == LogicalKeyboardKey.arrowRight &&
                                  _searchController.selection.baseOffset == _searchController.text.length) {
                                _searchInputFocusNode.unfocus();
                                _searchButtonFocusNode.requestFocus();
                                return KeyEventResult.handled;
                              }
                            }
                            return KeyEventResult.ignored;
                          },
                          child: TextField(
                            controller: _searchController,
                            focusNode: _searchInputFocusNode,
                            cursorColor: const Color(0xFF7C3AED),
                            cursorWidth: 2.2,
                            onChanged: _onSearchChanged,
                            onSubmitted: (_) {
                              _searchInputFocusNode.unfocus();
                              if (_searchResults.isNotEmpty) {
                                _getSearchResultFocusNode(0).requestFocus();
                              } else {
                                _searchButtonFocusNode.requestFocus();
                              }
                            },
                            onTapOutside: (_) {
                              _searchInputFocusNode.unfocus();
                            },
                            style: const TextStyle(
                              color: Color(0xFF1E1B2E),
                              fontSize: 16.5,
                              fontWeight: FontWeight.w600,
                              letterSpacing: 0.3,
                            ),
                            decoration: const InputDecoration(
                              hintText: 'Search anime by title, character, or studio...',
                              hintStyle: TextStyle(
                                color: Color(0xFF94A3B8),
                                fontSize: 15.5,
                                fontWeight: FontWeight.w400,
                              ),
                              border: InputBorder.none,
                              isDense: true,
                              contentPadding: EdgeInsets.symmetric(vertical: 16),
                            ),
                          ),
                        ),
                      ),
                      if (_searchController.text.isNotEmpty)
                        Padding(
                          padding: const EdgeInsets.only(right: 12),
                          child: GestureDetector(
                            onTap: () {
                              _searchController.clear();
                              _onSearchChanged('');
                            },
                            child: const Icon(
                              Icons.cancel_rounded,
                              color: Color(0xFF94A3B8),
                              size: 20,
                            ),
                          ),
                        ),
                    ],
                  ),
                ),
              ),
              const SizedBox(width: 20),
              Focus(
                focusNode: _searchButtonFocusNode,
                onKeyEvent: (node, event) {
                  if (event is KeyDownEvent || event is KeyRepeatEvent) {
                    if (event.logicalKey == LogicalKeyboardKey.select ||
                        event.logicalKey == LogicalKeyboardKey.enter ||
                        event.logicalKey == LogicalKeyboardKey.space ||
                        event.logicalKey == LogicalKeyboardKey.gameButtonA) {
                      _closeSearch();
                      return KeyEventResult.handled;
                    }
                    if (event.logicalKey == LogicalKeyboardKey.arrowLeft) {
                      _searchInputFocusNode.requestFocus();
                      return KeyEventResult.handled;
                    }
                    if (event.logicalKey == LogicalKeyboardKey.arrowDown) {
                      if (_searchResults.isNotEmpty) {
                        final targetIndex = (_searchResults.length > 5) ? 5 : _searchResults.length - 1;
                        _getSearchResultFocusNode(targetIndex).requestFocus();
                        return KeyEventResult.handled;
                      }
                    }
                  }
                  return KeyEventResult.ignored;
                },
                child: Builder(
                  builder: (context) {
                    final isFocused = Focus.of(context).hasFocus;
                    return AnimatedScale(
                      scale: isFocused ? 1.15 : 1.0,
                      duration: const Duration(milliseconds: 150),
                      child: Container(
                        width: 52,
                        height: 52,
                        decoration: BoxDecoration(
                          shape: BoxShape.circle,
                          color: isFocused
                              ? AppColors.primary
                              : Colors.white,
                          border: Border.all(
                            color: isFocused ? AppColors.primaryLight : const Color(0xFFE2E8F0),
                            width: isFocused ? 2.5 : 1.2,
                          ),
                          boxShadow: [
                            if (isFocused)
                              BoxShadow(
                                color: const Color(0xFF8B5CF6).withAlpha(100),
                                blurRadius: 16,
                              )
                            else
                              BoxShadow(
                                color: const Color(0xFF0F172A).withAlpha(12),
                                blurRadius: 8,
                                offset: const Offset(0, 2),
                              ),
                          ],
                        ),
                        child: Material(
                          color: Colors.transparent,
                          child: InkWell(
                            canRequestFocus: false,
                            customBorder: const CircleBorder(),
                            onTap: _closeSearch,
                            child: Icon(
                              Icons.close_rounded,
                              size: 26,
                              color: isFocused ? Colors.white : AppColors.textPrimary,
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
        ),
        Expanded(
          child: _isSearching
              ? const Center(child: CircularProgressIndicator(color: AppColors.primary))
              : _searchResults.isEmpty
                  ? const Center(
                      child: Text(
                        'No anime found matching your search.',
                        style: TextStyle(color: AppColors.textSecondary, fontSize: 18),
                      ),
                    )
                  : GridView.builder(
                      padding: const EdgeInsets.fromLTRB(40, 10, 40, 40),
                      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                        crossAxisCount: 6,
                        childAspectRatio: 0.65,
                        crossAxisSpacing: 16,
                        mainAxisSpacing: 20,
                      ),
                      itemCount: _searchResults.length,
                      itemBuilder: (context, index) {
                        final show = _searchResults[index];
                        return TvAnimeCard(
                          anime: show,
                          focusNode: _getSearchResultFocusNode(index),
                          onTap: () => _navigateToDetail(show),
                          onArrowUp: () {
                            if (index < 6) {
                              _searchInputFocusNode.requestFocus();
                            } else {
                              _getSearchResultFocusNode(index - 6).requestFocus();
                            }
                          },
                          onArrowDown: () {
                            if (index + 6 < _searchResults.length) {
                              _getSearchResultFocusNode(index + 6).requestFocus();
                            }
                          },
                          onArrowRight: () {
                            if (index + 1 < _searchResults.length) {
                              _getSearchResultFocusNode(index + 1).requestFocus();
                            }
                          },
                        );
                      },
                    ),
        ),
      ],
    );
  }
}
