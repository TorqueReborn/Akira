import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../../../theme/app_colors.dart';
import '../../auth/screens/login_screen.dart';
import '../../auth/services/auth_repository.dart';
import '../../auth/services/token_manager.dart';
import '../models/anime_show.dart';
import '../services/anime_repository.dart';
import '../widgets/anime_card.dart';
import '../widgets/anime_card_skeleton.dart';
import '../widgets/featured_anime_banner.dart';
import 'anime_detail_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final ScrollController _scrollController = ScrollController();
  final TextEditingController _searchController = TextEditingController();

  void _navigateToDetail(AnimeShow anime) {
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (_) => AnimeDetailScreen(
          animeId: anime.id,
          initialTitle: anime.englishName ?? anime.name,
          initialPoster: anime.thumbnail,
        ),
      ),
    );
  }

  List<AnimeShow> _shows = [];
  List<AnimeShow> _topRankedToday = [];
  bool _isLoading = false;
  bool _isLoadingMore = false;
  String? _errorMessage;
  int _currentPage = 1;
  bool _hasMore = true;

  bool _isSearchActive = false;
  Timer? _debounceTimer;

  // Translation Type: 'sub' vs 'dub'
  String _translationType = 'sub';

  // Country Origin: 'ALL', 'JP', 'KR', 'CN'
  String _selectedCountry = 'ALL';
  final List<Map<String, String>> _countryOptions = [
    {'code': 'ALL', 'label': 'All'},
    {'code': 'JP', 'label': 'Japan'},
    {'code': 'KR', 'label': 'Korea'},
    {'code': 'CN', 'label': 'China'},
  ];

  // Category filter state
  int _selectedCategoryIndex = 0;
  final List<Map<String, dynamic>> _categories = [
    {'label': 'Latest Updates', 'sortBy': 'Latest_Update'},
    {'label': 'Top Rated', 'sortBy': 'Top'},
    {'label': 'Popular', 'sortBy': 'Popular'},
    {'label': 'New Releases', 'sortBy': 'Release_Year'},
  ];

  @override
  void initState() {
    super.initState();
    _loadInitialData();
    _scrollController.addListener(_onScroll);
  }

  @override
  void dispose() {
    _scrollController.dispose();
    _searchController.dispose();
    _debounceTimer?.cancel();
    super.dispose();
  }

  Future<void> _loadInitialData() async {
    // Fire both requests in parallel immediately
    Future.wait([
      AnimeRepository.fetchTopRankedAnime(dateRange: 1, size: 10).then((ranked) {
        if (mounted && ranked.isNotEmpty) {
          setState(() {
            _topRankedToday = ranked;
          });
        }
      }).catchError((_) {}),
      _fetchShows(page: 1),
    ]);
  }

  void _onScroll() {
    if (!_scrollController.hasClients) return;
    final maxScroll = _scrollController.position.maxScrollExtent;
    final currentScroll = _scrollController.position.pixels;

    if (currentScroll >= maxScroll - 400 &&
        !_isLoading &&
        !_isLoadingMore &&
        _hasMore) {
      _loadMore();
    }
  }

  Future<void> _fetchShows({
    required int page,
    String? query,
    bool isRefresh = false,
  }) async {
    if (page == 1) {
      setState(() {
        _isLoading = true;
        _errorMessage = null;
        if (isRefresh) {
          AnimeRepository.clearCache();
        }
      });
    } else {
      setState(() {
        _isLoadingMore = true;
      });
    }

    try {
      final activeCategory = _categories[_selectedCategoryIndex];
      final sortBy = activeCategory['sortBy'] as String?;

      final result = await AnimeRepository.fetchAnimeList(
        page: page,
        searchQuery: query?.trim().isEmpty ?? true ? null : query!.trim(),
        sortBy: sortBy,
        translationType: _translationType,
        countryOrigin: _selectedCountry,
      );

      if (!mounted) return;

      setState(() {
        if (page == 1) {
          _shows = result.shows;
          // If top ranked failed or is empty, fallback featured banner to top catalog shows
          if (_topRankedToday.isEmpty && _shows.isNotEmpty) {
            _topRankedToday = _shows
                .where((s) => s.thumbnail != null && s.thumbnail!.isNotEmpty)
                .take(6)
                .toList();
          }
        } else {
          _shows.addAll(result.shows);
        }
        _currentPage = page;
        _hasMore = result.shows.isNotEmpty;
        _isLoading = false;
        _isLoadingMore = false;
      });
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _isLoading = false;
        _isLoadingMore = false;
        if (page == 1) {
          _errorMessage = e.toString().replaceFirst('Exception: ', '');
        }
      });
    }
  }

  void _loadMore() {
    _fetchShows(
      page: _currentPage + 1,
      query: _searchController.text,
    );
  }

  void _onSearchChanged(String val) {
    _debounceTimer?.cancel();
    _debounceTimer = Timer(const Duration(milliseconds: 400), () {
      _fetchShows(page: 1, query: val);
    });
  }

  void _toggleSearch() {
    setState(() {
      _isSearchActive = !_isSearchActive;
      if (!_isSearchActive) {
        _searchController.clear();
        _fetchShows(page: 1);
      }
    });
  }

  void _selectCategory(int index) {
    if (_selectedCategoryIndex == index) return;
    setState(() {
      _selectedCategoryIndex = index;
    });
    _fetchShows(page: 1, query: _searchController.text);
  }

  void _toggleTranslationType(String type) {
    if (_translationType == type) return;
    setState(() {
      _translationType = type;
    });
    _fetchShows(page: 1, query: _searchController.text);
  }

  void _selectCountry(String code) {
    if (_selectedCountry == code) return;
    setState(() {
      _selectedCountry = code;
    });
    _fetchShows(page: 1, query: _searchController.text);
  }

  Future<void> _handleLogout() async {
    final confirm = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: AppColors.surface,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
        title: const Text(
          'Log Out',
          style: TextStyle(
            color: AppColors.textPrimary,
            fontWeight: FontWeight.bold,
          ),
        ),
        content: const Text(
          'Are you sure you want to log out from Akira?',
          style: TextStyle(color: AppColors.textSecondary),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(ctx).pop(false),
            child: const Text('Cancel',
                style: TextStyle(color: AppColors.textSecondary)),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(
              backgroundColor: AppColors.primary,
              shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(12)),
            ),
            onPressed: () => Navigator.of(ctx).pop(true),
            child: const Text('Log Out', style: TextStyle(color: Colors.white)),
          ),
        ],
      ),
    );

    if (confirm != true) return;

    final accessToken = TokenManager.getAccessToken();
    await AuthRepository.logout(accessToken);
    await TokenManager.clear();

    if (!mounted) return;

    Navigator.of(context).pushAndRemoveUntil(
      MaterialPageRoute(builder: (_) => const LoginScreen()),
      (route) => false,
    );
  }

  @override
  Widget build(BuildContext context) {
    return AnnotatedRegion<SystemUiOverlayStyle>(
      value: const SystemUiOverlayStyle(
        statusBarColor: Colors.transparent,
        statusBarIconBrightness: Brightness.dark, // Crisp dark status bar icons over light background
        statusBarBrightness: Brightness.light,    // iOS status style
      ),
      child: Scaffold(
        backgroundColor: AppColors.background,
        body: Stack(
          children: [
            // 1. Ambient Background Glow matching the login screen aesthetic
            Positioned(
              top: -120,
              right: -100,
              child: Container(
                width: 380,
                height: 380,
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
              bottom: 40,
              left: -120,
              child: Container(
                width: 320,
                height: 320,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  gradient: RadialGradient(
                    colors: [
                      AppColors.primaryGlow.withAlpha(25),
                      Colors.transparent,
                    ],
                  ),
                ),
              ),
            ),

            // 2. Main Content
            SafeArea(
              bottom: false,
              child: _buildBody(),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildTopAppBar() {
    return Container(
      padding: const EdgeInsets.fromLTRB(18, 12, 18, 10),
      child: _isSearchActive
          ? _buildActiveSearchBar()
          : Row(
              children: [
                // Akira Brand Logo with exact login gradient
                ShaderMask(
                  shaderCallback: (bounds) =>
                      AppColors.logoGradient.createShader(bounds),
                  child: const Text(
                    'AKIRA',
                    style: TextStyle(
                      color: Colors.white,
                      fontSize: 26,
                      fontWeight: FontWeight.w900,
                      letterSpacing: 4.0,
                    ),
                  ),
                ),
                const Spacer(),

                // Sub / Dub segmented toggle
                _buildSubDubToggle(),

                const SizedBox(width: 10),

                // Search Icon button
                _buildCircularAction(
                  icon: Icons.search_rounded,
                  onPressed: _toggleSearch,
                  tooltip: 'Search anime',
                ),

                const SizedBox(width: 8),

                // Logout action button
                _buildCircularAction(
                  icon: Icons.logout_rounded,
                  onPressed: _handleLogout,
                  tooltip: 'Log Out',
                ),
              ],
            ),
    );
  }

  Widget _buildSubDubToggle() {
    return Container(
      height: 34,
      padding: const EdgeInsets.all(3),
      decoration: BoxDecoration(
        color: const Color(0xFFF1EEF8),
        borderRadius: BorderRadius.circular(18),
        border: Border.all(
          color: const Color(0xFFE2E8F0),
          width: 1,
        ),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          _buildToggleOption(
            title: 'SUB',
            isSelected: _translationType == 'sub',
            onTap: () => _toggleTranslationType('sub'),
          ),
          _buildToggleOption(
            title: 'DUB',
            isSelected: _translationType == 'dub',
            onTap: () => _toggleTranslationType('dub'),
          ),
        ],
      ),
    );
  }

  Widget _buildToggleOption({
    required String title,
    required bool isSelected,
    required VoidCallback onTap,
  }) {
    return GestureDetector(
      onTap: onTap,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 200),
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
        decoration: BoxDecoration(
          gradient: isSelected ? AppColors.logoGradient : null,
          color: isSelected ? null : Colors.transparent,
          borderRadius: BorderRadius.circular(14),
          boxShadow: isSelected
              ? [
                  BoxShadow(
                    color: AppColors.primary.withAlpha(60),
                    blurRadius: 4,
                    offset: const Offset(0, 2),
                  ),
                ]
              : null,
        ),
        child: Text(
          title,
          style: TextStyle(
            color: isSelected ? Colors.white : AppColors.textSecondary,
            fontSize: 11,
            fontWeight: FontWeight.w800,
            letterSpacing: 0.5,
          ),
        ),
      ),
    );
  }

  Widget _buildCircularAction({
    required IconData icon,
    required VoidCallback onPressed,
    required String tooltip,
  }) {
    return Material(
      color: Colors.white,
      shape: const CircleBorder(),
      elevation: 0,
      child: InkWell(
        customBorder: const CircleBorder(),
        onTap: onPressed,
        child: Container(
          width: 38,
          height: 38,
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            border: Border.all(
              color: const Color(0xFFE2E8F0),
              width: 1,
            ),
            boxShadow: [
              BoxShadow(
                color: const Color(0xFF0F172A).withAlpha(10),
                blurRadius: 6,
                offset: const Offset(0, 2),
              ),
            ],
          ),
          child: Icon(
            icon,
            size: 19,
            color: AppColors.textPrimary,
          ),
        ),
      ),
    );
  }

  Widget _buildActiveSearchBar() {
    return Row(
      children: [
        Expanded(
          child: Container(
            height: 46,
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(16),
              border: Border.all(
                color: AppColors.primary,
                width: 1.5,
              ),
              boxShadow: [
                BoxShadow(
                  color: AppColors.primary.withAlpha(35),
                  blurRadius: 12,
                  offset: const Offset(0, 3),
                ),
              ],
            ),
            child: TextField(
              controller: _searchController,
              autofocus: true,
              onChanged: _onSearchChanged,
              style: const TextStyle(
                color: AppColors.textPrimary,
                fontSize: 14,
                fontWeight: FontWeight.w500,
              ),
              decoration: const InputDecoration(
                hintText: 'Search anime by title...',
                hintStyle: TextStyle(
                  color: AppColors.textHint,
                  fontSize: 14,
                ),
                prefixIcon: Icon(
                  Icons.search_rounded,
                  color: AppColors.primary,
                  size: 20,
                ),
                border: InputBorder.none,
                contentPadding: EdgeInsets.symmetric(vertical: 12),
              ),
            ),
          ),
        ),
        const SizedBox(width: 8),
        IconButton(
          onPressed: _toggleSearch,
          icon: const Icon(Icons.close_rounded, color: AppColors.textPrimary),
          tooltip: 'Cancel Search',
        ),
      ],
    );
  }

  Widget _buildCategoryPills() {
    return SizedBox(
      height: 38,
      child: ListView.separated(
        padding: const EdgeInsets.symmetric(horizontal: 18),
        scrollDirection: Axis.horizontal,
        physics: const BouncingScrollPhysics(),
        itemCount: _categories.length,
        separatorBuilder: (_, _) => const SizedBox(width: 8),
        itemBuilder: (context, index) {
          final isSelected = _selectedCategoryIndex == index;
          final cat = _categories[index];
          return Material(
            color: Colors.transparent,
            child: InkWell(
              onTap: () => _selectCategory(index),
              borderRadius: BorderRadius.circular(20),
              child: AnimatedContainer(
                duration: const Duration(milliseconds: 200),
                padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                decoration: BoxDecoration(
                  gradient: isSelected ? AppColors.logoGradient : null,
                  color: isSelected ? null : Colors.white,
                  borderRadius: BorderRadius.circular(20),
                  border: Border.all(
                    color: isSelected
                        ? Colors.transparent
                        : const Color(0xFFE2E8F0),
                    width: 1,
                  ),
                  boxShadow: [
                    if (isSelected)
                      BoxShadow(
                        color: AppColors.primary.withAlpha(70),
                        blurRadius: 8,
                        offset: const Offset(0, 3),
                      )
                    else
                      BoxShadow(
                        color: const Color(0xFF0F172A).withAlpha(8),
                        blurRadius: 4,
                        offset: const Offset(0, 1),
                      ),
                  ],
                ),
                alignment: Alignment.center,
                child: Text(
                  cat['label'] as String,
                  style: TextStyle(
                    color: isSelected ? Colors.white : AppColors.textSecondary,
                    fontSize: 13,
                    fontWeight: isSelected ? FontWeight.w700 : FontWeight.w500,
                  ),
                ),
              ),
            ),
          );
        },
      ),
    );
  }

  Widget _buildCountryFilterBar() {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 18),
      child: Container(
        height: 38,
        padding: const EdgeInsets.all(3.5),
        decoration: BoxDecoration(
          color: const Color(0xFFF1EEF8),
          borderRadius: BorderRadius.circular(20),
          border: Border.all(
            color: const Color(0xFFE2E8F0),
            width: 1,
          ),
        ),
        child: Row(
          children: _countryOptions.map((item) {
            final code = item['code']!;
            final label = item['label']!;
            final isSelected = _selectedCountry == code;

            return Expanded(
              child: GestureDetector(
                onTap: () => _selectCountry(code),
                behavior: HitTestBehavior.opaque,
                child: AnimatedContainer(
                  duration: const Duration(milliseconds: 220),
                  curve: Curves.easeInOut,
                  alignment: Alignment.center,
                  decoration: BoxDecoration(
                    color: isSelected ? Colors.white : Colors.transparent,
                    borderRadius: BorderRadius.circular(16),
                    boxShadow: isSelected
                        ? [
                            BoxShadow(
                              color: AppColors.primary.withAlpha(35),
                              blurRadius: 8,
                              offset: const Offset(0, 2),
                            ),
                          ]
                        : null,
                  ),
                  child: Text(
                    label,
                    style: TextStyle(
                      color: isSelected
                          ? AppColors.primary
                          : AppColors.textSecondary,
                      fontSize: 12.5,
                      fontWeight:
                          isSelected ? FontWeight.w800 : FontWeight.w600,
                    ),
                  ),
                ),
              ),
            );
          }).toList(),
        ),
      ),
    );
  }

  Widget _buildBody() {

    if (_errorMessage != null && _shows.isEmpty) {
      return Center(
        child: Padding(
          padding: const EdgeInsets.all(32),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
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
                'Failed to load anime',
                style: TextStyle(
                  color: AppColors.textPrimary,
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(height: 8),
              Text(
                _errorMessage!,
                textAlign: TextAlign.center,
                style: const TextStyle(
                  color: AppColors.textSecondary,
                  fontSize: 13,
                ),
              ),
              const SizedBox(height: 24),
              ElevatedButton.icon(
                onPressed: () =>
                    _fetchShows(page: 1, query: _searchController.text),
                style: ElevatedButton.styleFrom(
                  backgroundColor: AppColors.primary,
                  padding:
                      const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
                  shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(14)),
                ),
                icon: const Icon(Icons.refresh_rounded,
                    color: Colors.white, size: 20),
                label: const Text('Try Again',
                    style: TextStyle(
                        color: Colors.white, fontWeight: FontWeight.bold)),
              ),
            ],
          ),
        ),
      );
    }

    return RefreshIndicator(
      onRefresh: () async {
        await _loadInitialData();
      },
      color: AppColors.primary,
      backgroundColor: AppColors.surface,
      child: LayoutBuilder(
        builder: (context, constraints) {
          final crossAxisCount = constraints.maxWidth > 900
              ? 5
              : constraints.maxWidth > 600
                  ? 3
                  : 2;

          final isSearching = _searchController.text.trim().isNotEmpty;
          final currentCatLabel =
              _categories[_selectedCategoryIndex]['label'] as String;

          return CustomScrollView(
            controller: _scrollController,
            physics: const AlwaysScrollableScrollPhysics(),
            slivers: [
              // Top Bar
              SliverToBoxAdapter(
                child: _buildTopAppBar(),
              ),

              // Hero Top 10 Featured Carousel (only when not searching)
              if (!isSearching && _topRankedToday.isNotEmpty) ...[
                const SliverToBoxAdapter(
                  child: SizedBox(height: 6),
                ),
                SliverToBoxAdapter(
                  child: FeaturedAnimeBanner(
                    featuredShows: _topRankedToday,
                    onTap: (anime) => _navigateToDetail(anime),
                  ),
                ),
                const SliverToBoxAdapter(
                  child: SizedBox(height: 18),
                ),
              ],

              // Category Pills (only when not searching)
              if (!isSearching) ...[
                SliverToBoxAdapter(
                  child: _buildCategoryPills(),
                ),
                const SliverToBoxAdapter(
                  child: SizedBox(height: 10),
                ),
                // Origin country filter bar (JP, KR, CN, ALL)
                SliverToBoxAdapter(
                  child: _buildCountryFilterBar(),
                ),
                const SliverToBoxAdapter(
                  child: SizedBox(height: 14),
                ),
              ],

              // Section Header
              SliverToBoxAdapter(
                child: Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 18),
                  child: Row(
                    children: [
                      Text(
                        isSearching
                            ? 'Search Results for "${_searchController.text.trim()}"'
                            : currentCatLabel,
                        style: const TextStyle(
                          color: AppColors.textPrimary,
                          fontSize: 18,
                          fontWeight: FontWeight.w800,
                          letterSpacing: -0.2,
                        ),
                      ),
                      const SizedBox(width: 8),
                      Container(
                        padding: const EdgeInsets.symmetric(
                            horizontal: 7, vertical: 2),
                        decoration: BoxDecoration(
                          color: AppColors.primary.withAlpha(20),
                          borderRadius: BorderRadius.circular(6),
                        ),
                        child: Text(
                          _translationType.toUpperCase(),
                          style: const TextStyle(
                            color: AppColors.primary,
                            fontSize: 10,
                            fontWeight: FontWeight.w800,
                          ),
                        ),
                      ),
                      const Spacer(),
                      if (!isSearching && _shows.isNotEmpty)
                        Text(
                          '${_shows.length} shows',
                          style: const TextStyle(
                            color: AppColors.textHint,
                            fontSize: 12,
                            fontWeight: FontWeight.w500,
                          ),
                        ),
                    ],
                  ),
                ),
              ),

              // Skeleton Loading Grid (shown immediately when loading first time)
              if (_isLoading && _shows.isEmpty)
                SliverPadding(
                  padding: const EdgeInsets.fromLTRB(16, 12, 16, 16),
                  sliver: SliverGrid(
                    gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
                      crossAxisCount: crossAxisCount,
                      crossAxisSpacing: 14,
                      mainAxisSpacing: 16,
                      childAspectRatio: 0.70,
                    ),
                    delegate: SliverChildBuilderDelegate(
                      (context, index) => const AnimeCardSkeleton(),
                      childCount: 8,
                    ),
                  ),
                ),

              // Empty Search Result fallback (only when NOT loading)
              if (!_isLoading && _shows.isEmpty)
                SliverToBoxAdapter(
                  child: Padding(
                    padding: const EdgeInsets.symmetric(vertical: 60),
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Icon(
                          Icons.movie_filter_outlined,
                          size: 56,
                          color: AppColors.iconMuted.withAlpha(150),
                        ),
                        const SizedBox(height: 14),
                        const Text(
                          'No anime found',
                          style: TextStyle(
                            color: AppColors.textPrimary,
                            fontSize: 18,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        const SizedBox(height: 6),
                        const Text(
                          'Try searching with a different keyword or filter',
                          style: TextStyle(
                            color: AppColors.textSecondary,
                            fontSize: 13,
                          ),
                        ),
                      ],
                    ),
                  ),
                ),

              // Grid of Anime
              if (_shows.isNotEmpty)
                SliverPadding(
                  padding: const EdgeInsets.fromLTRB(16, 12, 16, 16),
                  sliver: SliverGrid(
                    gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
                      crossAxisCount: crossAxisCount,
                      crossAxisSpacing: 14,
                      mainAxisSpacing: 16,
                      childAspectRatio: 0.70,
                    ),
                    delegate: SliverChildBuilderDelegate(
                      (context, index) {
                        final anime = _shows[index];
                        return AnimeCard(
                          anime: anime,
                          onTap: () => _navigateToDetail(anime),
                        );
                      },
                      childCount: _shows.length,
                    ),
                  ),
                ),

              // Loading more indicator
              if (_isLoadingMore)
                const SliverToBoxAdapter(
                  child: Padding(
                    padding: EdgeInsets.symmetric(vertical: 24),
                    child: Center(
                      child: SizedBox(
                        width: 28,
                        height: 28,
                        child: CircularProgressIndicator(
                          color: AppColors.primary,
                          strokeWidth: 2.5,
                        ),
                      ),
                    ),
                  ),
                ),

              // Bottom safe spacing
              const SliverToBoxAdapter(
                child: SizedBox(height: 24),
              ),
            ],
          );
        },
      ),
    );
  }
}

