import 'dart:async';
import 'package:flutter/material.dart';
import '../../../../theme/app_colors.dart';
import '../../../../utils/image_utils.dart';
import '../models/anime_show.dart';

class FeaturedAnimeBanner extends StatefulWidget {
  final List<AnimeShow> featuredShows;
  final ValueChanged<AnimeShow>? onTap;

  const FeaturedAnimeBanner({
    super.key,
    required this.featuredShows,
    this.onTap,
  });

  @override
  State<FeaturedAnimeBanner> createState() => _FeaturedAnimeBannerState();
}

class _FeaturedAnimeBannerState extends State<FeaturedAnimeBanner> {
  late final PageController _pageController;
  int _currentIndex = 0;
  Timer? _autoSlideTimer;

  @override
  void initState() {
    super.initState();
    _pageController = PageController(viewportFraction: 0.92);
    _startAutoSlide();
  }

  void _startAutoSlide() {
    _autoSlideTimer?.cancel();
    if (widget.featuredShows.length <= 1) return;
    _autoSlideTimer = Timer.periodic(const Duration(seconds: 5), (_) {
      if (!mounted || !_pageController.hasClients) return;
      final nextIndex = (_currentIndex + 1) % widget.featuredShows.length;
      _pageController.animateToPage(
        nextIndex,
        duration: const Duration(milliseconds: 650),
        curve: Curves.easeInOutCubic,
      );
    });
  }

  @override
  void didUpdateWidget(covariant FeaturedAnimeBanner oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.featuredShows != widget.featuredShows) {
      _startAutoSlide();
    }
  }

  @override
  void dispose() {
    _autoSlideTimer?.cancel();
    _pageController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (widget.featuredShows.isEmpty) return const SizedBox.shrink();

    return Column(
      children: [
        SizedBox(
          height: 220,
          child: PageView.builder(
            controller: _pageController,
            itemCount: widget.featuredShows.length,
            onPageChanged: (index) {
              setState(() => _currentIndex = index);
            },
            itemBuilder: (context, index) {
              final show = widget.featuredShows[index];
              final title = show.englishName ?? show.name;
              final bannerImage = show.thumbnail;

              return Padding(
                padding: const EdgeInsets.symmetric(horizontal: 6),
                child: Material(
                  color: Colors.transparent,
                  child: InkWell(
                    onTap: () => widget.onTap?.call(show),
                    borderRadius: BorderRadius.circular(22),
                    child: Container(
                      decoration: BoxDecoration(
                        color: const Color(0xFF13101E),
                        borderRadius: BorderRadius.circular(22),
                        boxShadow: [
                          BoxShadow(
                            color: AppColors.primary.withAlpha(45),
                            blurRadius: 18,
                            offset: const Offset(0, 8),
                          ),
                        ],
                      ),
                      clipBehavior: Clip.antiAlias,
                      child: Stack(
                        fit: StackFit.expand,
                        children: [
                          // Background Image
                          if (bannerImage != null && bannerImage.isNotEmpty)
                            Image.network(
                              bannerImage,
                              headers: ImageUtils.imageHeaders,
                              fit: BoxFit.cover,
                              cacheWidth: 720,
                              errorBuilder: (context, error, stackTrace) =>
                                  Container(color: const Color(0xFF1B1829)),
                            )
                          else
                            Container(color: const Color(0xFF1B1829)),

                          // Cinematic gradient overlays
                          // Dark gradient from bottom to top
                          Positioned.fill(
                            child: Container(
                              decoration: BoxDecoration(
                                gradient: LinearGradient(
                                  begin: Alignment.bottomCenter,
                                  end: Alignment.topCenter,
                                  colors: [
                                    const Color(0xFF0F0C1B).withAlpha(240),
                                    const Color(0xFF0F0C1B).withAlpha(150),
                                    Colors.transparent,
                                  ],
                                  stops: const [0.0, 0.45, 0.85],
                                ),
                              ),
                            ),
                          ),

                          // Left side shade for text readability
                          Positioned.fill(
                            child: Container(
                              decoration: BoxDecoration(
                                gradient: LinearGradient(
                                  begin: Alignment.centerLeft,
                                  end: Alignment.centerRight,
                                  colors: [
                                    const Color(0xFF0F0C1B).withAlpha(200),
                                    Colors.transparent,
                                  ],
                                  stops: const [0.0, 0.7],
                                ),
                              ),
                            ),
                          ),

                          // Top Badges (Rank / SPOTLIGHT & Score)
                          Positioned(
                            top: 14,
                            left: 16,
                            child: Container(
                              padding: const EdgeInsets.symmetric(
                                horizontal: 10,
                                vertical: 4,
                              ),
                              decoration: BoxDecoration(
                                gradient: AppColors.logoGradient,
                                borderRadius: BorderRadius.circular(20),
                                boxShadow: [
                                  BoxShadow(
                                    color: AppColors.primary.withAlpha(90),
                                    blurRadius: 8,
                                    offset: const Offset(0, 2),
                                  ),
                                ],
                              ),
                              child: Row(
                                mainAxisSize: MainAxisSize.min,
                                children: [
                                  const Icon(
                                    Icons.local_fire_department_rounded,
                                    color: Colors.white,
                                    size: 14,
                                  ),
                                  const SizedBox(width: 4),
                                  Text(
                                    '#${index + 1} TOP TRENDING',
                                    style: const TextStyle(
                                      color: Colors.white,
                                      fontSize: 10,
                                      fontWeight: FontWeight.w900,
                                      letterSpacing: 0.8,
                                    ),
                                  ),
                                ],
                              ),
                            ),
                          ),

                          if (show.score != null && show.score! > 0)
                            Positioned(
                              top: 14,
                              right: 16,
                              child: Container(
                                padding: const EdgeInsets.symmetric(
                                  horizontal: 8,
                                  vertical: 4,
                                ),
                                decoration: BoxDecoration(
                                  color: Colors.black.withAlpha(180),
                                  borderRadius: BorderRadius.circular(12),
                                  border: Border.all(
                                    color: Colors.amber.withAlpha(160),
                                    width: 1,
                                  ),
                                ),
                                child: Row(
                                  mainAxisSize: MainAxisSize.min,
                                  children: [
                                    const Icon(
                                      Icons.star_rounded,
                                      color: Colors.amber,
                                      size: 14,
                                    ),
                                    const SizedBox(width: 4),
                                    Text(
                                      show.score!.toStringAsFixed(1),
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

                          // Bottom details
                          Positioned(
                            bottom: 16,
                            left: 16,
                            right: 16,
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              mainAxisSize: MainAxisSize.min,
                              children: [
                                if (show.type != null ||
                                    show.seasonYear != null ||
                                    show.views != null) ...[
                                  Row(
                                    children: [
                                      if (show.type != null)
                                        Text(
                                          show.type!.toUpperCase(),
                                          style: const TextStyle(
                                            color: AppColors.primaryLight,
                                            fontSize: 11,
                                            fontWeight: FontWeight.w700,
                                            letterSpacing: 0.5,
                                          ),
                                        ),
                                      if (show.seasonYear != null) ...[
                                        const Text(
                                          ' • ',
                                          style: TextStyle(
                                            color: Colors.white54,
                                            fontSize: 11,
                                          ),
                                        ),
                                        Text(
                                          '${show.seasonQuarter ?? ''} ${show.seasonYear}'.trim(),
                                          style: const TextStyle(
                                            color: Colors.white70,
                                            fontSize: 11,
                                            fontWeight: FontWeight.w500,
                                          ),
                                        ),
                                      ],
                                      if (show.views != null) ...[
                                        const Text(
                                          ' • ',
                                          style: TextStyle(
                                            color: Colors.white54,
                                            fontSize: 11,
                                          ),
                                        ),
                                        const Icon(
                                          Icons.visibility_rounded,
                                          size: 12,
                                          color: Colors.white60,
                                        ),
                                        const SizedBox(width: 3),
                                        Text(
                                          show.views!,
                                          style: const TextStyle(
                                            color: Colors.white70,
                                            fontSize: 11,
                                            fontWeight: FontWeight.w500,
                                          ),
                                        ),
                                      ],
                                    ],
                                  ),
                                  const SizedBox(height: 4),
                                ],
                                Text(
                                  title,
                                  maxLines: 2,
                                  overflow: TextOverflow.ellipsis,
                                  style: const TextStyle(
                                    color: Colors.white,
                                    fontSize: 17,
                                    fontWeight: FontWeight.w800,
                                    height: 1.25,
                                    letterSpacing: -0.2,
                                  ),
                                ),
                                const SizedBox(height: 8),
                                Row(
                                  children: [
                                    Container(
                                      padding: const EdgeInsets.symmetric(
                                        horizontal: 10,
                                        vertical: 4,
                                      ),
                                      decoration: BoxDecoration(
                                        color: AppColors.primary,
                                        borderRadius: BorderRadius.circular(10),
                                      ),
                                      child: const Row(
                                        mainAxisSize: MainAxisSize.min,
                                        children: [
                                          Icon(
                                            Icons.play_arrow_rounded,
                                            color: Colors.white,
                                            size: 16,
                                          ),
                                          SizedBox(width: 3),
                                          Text(
                                            'Watch Now',
                                            style: TextStyle(
                                              color: Colors.white,
                                              fontSize: 11,
                                              fontWeight: FontWeight.bold,
                                            ),
                                          ),
                                        ],
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
                ),
              );
            },
          ),
        ),
        const SizedBox(height: 10),
        // Dots indicator
        Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: List.generate(
            widget.featuredShows.length,
            (index) {
              final isSelected = index == _currentIndex;
              return AnimatedContainer(
                duration: const Duration(milliseconds: 300),
                margin: const EdgeInsets.symmetric(horizontal: 3),
                height: 5,
                width: isSelected ? 20 : 6,
                decoration: BoxDecoration(
                  color: isSelected
                      ? AppColors.primary
                      : AppColors.primary.withAlpha(50),
                  borderRadius: BorderRadius.circular(4),
                ),
              );
            },
          ),
        ),
      ],
    );
  }
}
