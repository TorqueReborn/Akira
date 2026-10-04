import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../../../theme/app_colors.dart';
import '../../../utils/image_utils.dart';
import '../models/anime_show.dart';

/// TV Optimized Non-Rounded Unified Top Hero Section covering the full screen,
/// combining the Top App Bar and Featured Banner Carousel edge-to-edge.
class TvFeaturedBanner extends StatefulWidget {
  final List<AnimeShow> featuredShows;
  final ValueChanged<AnimeShow> onTap;
  final FocusNode? focusNode;
  final FocusNode? nextFocusNode;

  final VoidCallback onToggleSearch;
  final bool isSearchActive;
  final TextEditingController searchController;
  final FocusNode searchInputFocusNode;
  final FocusNode searchButtonFocusNode;
  final ValueChanged<String> onSearchChanged;

  const TvFeaturedBanner({
    super.key,
    required this.featuredShows,
    required this.onTap,
    this.focusNode,
    this.nextFocusNode,
    required this.onToggleSearch,
    required this.isSearchActive,
    required this.searchController,
    required this.searchInputFocusNode,
    required this.searchButtonFocusNode,
    required this.onSearchChanged,
  });

  @override
  State<TvFeaturedBanner> createState() => _TvFeaturedBannerState();
}

class _TvFeaturedBannerState extends State<TvFeaturedBanner> {
  late final PageController _pageController;
  late final FocusNode _internalFocusNode;
  int _currentIndex = 0;
  bool _isFocused = false;
  Timer? _autoSlideTimer;

  FocusNode get _effectiveFocusNode => widget.focusNode ?? _internalFocusNode;

  @override
  void initState() {
    super.initState();
    _internalFocusNode = FocusNode();
    _pageController = PageController(viewportFraction: 1.0);
    _effectiveFocusNode.addListener(_onFocusChange);
    _startAutoSlide();
  }

  void _onFocusChange() {
    if (mounted) {
      setState(() {
        _isFocused = _effectiveFocusNode.hasFocus;
      });
      if (_isFocused) {
        _autoSlideTimer?.cancel();
      } else {
        _startAutoSlide();
      }
    }
  }

  void _startAutoSlide() {
    _autoSlideTimer?.cancel();
    if (widget.featuredShows.length <= 1) return;
    _autoSlideTimer = Timer.periodic(const Duration(seconds: 6), (_) {
      if (!mounted || !_pageController.hasClients || _isFocused) return;
      final nextIndex = (_currentIndex + 1) % widget.featuredShows.length;
      _pageController.animateToPage(
        nextIndex,
        duration: const Duration(milliseconds: 650),
        curve: Curves.easeInOutCubic,
      );
    });
  }

  void _goToPrevious() {
    if (_currentIndex > 0) {
      _pageController.previousPage(
        duration: const Duration(milliseconds: 350),
        curve: Curves.easeOutCubic,
      );
    } else {
      _pageController.animateToPage(
        widget.featuredShows.length - 1,
        duration: const Duration(milliseconds: 350),
        curve: Curves.easeOutCubic,
      );
    }
  }

  void _goToNext() {
    if (_currentIndex < widget.featuredShows.length - 1) {
      _pageController.nextPage(
        duration: const Duration(milliseconds: 350),
        curve: Curves.easeOutCubic,
      );
    } else {
      _pageController.animateToPage(
        0,
        duration: const Duration(milliseconds: 350),
        curve: Curves.easeOutCubic,
      );
    }
  }

  @override
  void dispose() {
    _autoSlideTimer?.cancel();
    _effectiveFocusNode.removeListener(_onFocusChange);
    _internalFocusNode.dispose();
    _pageController.dispose();
    super.dispose();
  }

  Widget _buildTopAppBar() {
    return Container(
      padding: const EdgeInsets.fromLTRB(40, 24, 40, 16),
      child: AnimatedSize(
        duration: const Duration(milliseconds: 260),
        curve: Curves.easeOutCubic,
        clipBehavior: Clip.none,
        child: AnimatedSwitcher(
          duration: const Duration(milliseconds: 220),
          switchInCurve: Curves.easeOutCubic,
          switchOutCurve: Curves.easeInCubic,
          layoutBuilder: (currentChild, previousChildren) {
            return Stack(
              clipBehavior: Clip.none,
              alignment: Alignment.centerLeft,
              children: [
                ...previousChildren,
                ?currentChild,
              ],
            );
          },
          transitionBuilder: (child, animation) {
            return FadeTransition(
              opacity: animation,
              child: child,
            );
          },
          child: widget.isSearchActive
              ? KeyedSubtree(
                  key: const ValueKey('banner_header_search_active'),
                  child: _buildActiveSearchBar(),
                )
              : KeyedSubtree(
                  key: const ValueKey('banner_header_search_collapsed'),
                  child: Row(
                    children: [
                      ShaderMask(
                        shaderCallback: (bounds) => AppColors.logoGradient.createShader(bounds),
                        child: const Text(
                          'AKIRA',
                          style: TextStyle(
                            color: Colors.white,
                            fontSize: 38,
                            fontWeight: FontWeight.w900,
                            letterSpacing: 6.0,
                          ),
                        ),
                      ),
                      const Spacer(),
                      _buildTvActionIcon(
                        focusNode: widget.searchButtonFocusNode,
                        icon: Icons.search_rounded,
                        onPressed: widget.onToggleSearch,
                        nextDownFocusNode: widget.focusNode,
                      ),
                    ],
                  ),
                ),
        ),
      ),
    );
  }

  Widget _buildTvActionIcon({
    required FocusNode focusNode,
    required IconData icon,
    required VoidCallback onPressed,
    FocusNode? nextLeftFocusNode,
    FocusNode? nextRightFocusNode,
    FocusNode? nextDownFocusNode,
  }) {
    return Focus(
      focusNode: focusNode,
      onKeyEvent: (node, event) {
        if (event is KeyDownEvent || event is KeyRepeatEvent) {
          if (event.logicalKey == LogicalKeyboardKey.select ||
              event.logicalKey == LogicalKeyboardKey.enter ||
              event.logicalKey == LogicalKeyboardKey.space ||
              event.logicalKey == LogicalKeyboardKey.gameButtonA) {
            onPressed();
            return KeyEventResult.handled;
          }
          if (event.logicalKey == LogicalKeyboardKey.arrowDown) {
            if (nextDownFocusNode != null) {
              nextDownFocusNode.requestFocus();
              return KeyEventResult.handled;
            }
          }
          if (event.logicalKey == LogicalKeyboardKey.arrowLeft && nextLeftFocusNode != null) {
            nextLeftFocusNode.requestFocus();
            return KeyEventResult.handled;
          }
          if (event.logicalKey == LogicalKeyboardKey.arrowRight && nextRightFocusNode != null) {
            nextRightFocusNode.requestFocus();
            return KeyEventResult.handled;
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
                    : Colors.black.withAlpha(80),
                border: Border.all(
                  color: isFocused ? Colors.white : Colors.white38,
                  width: isFocused ? 2.5 : 1.2,
                ),
                boxShadow: isFocused
                    ? [
                        BoxShadow(
                          color: const Color(0xFF8B5CF6).withAlpha(140),
                          blurRadius: 16,
                        ),
                      ]
                    : null,
              ),
              child: Material(
                color: Colors.transparent,
                child: InkWell(
                  canRequestFocus: false,
                  customBorder: const CircleBorder(),
                  onTap: onPressed,
                  child: Icon(
                    icon,
                    size: 26,
                    color: Colors.white,
                  ),
                ),
              ),
            ),
          );
        },
      ),
    );
  }

  Widget _buildActiveSearchBar() {
    final isInputFocused = widget.searchInputFocusNode.hasFocus;

    return Row(
      children: [
        // Brand Mark
        ShaderMask(
          shaderCallback: (bounds) => AppColors.logoGradient.createShader(bounds),
          child: const Text(
            'AKIRA',
            style: TextStyle(
              color: Colors.white,
              fontSize: 32,
              fontWeight: FontWeight.w900,
              letterSpacing: 4.0,
            ),
          ),
        ),
        const SizedBox(width: 32),

        // Light Surface Search Bar
        Expanded(
          child: AnimatedContainer(
            duration: const Duration(milliseconds: 200),
            height: 56,
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(16),
              border: Border.all(
                color: isInputFocused
                    ? const Color(0xFF7C3AED)
                    : const Color(0xFFE2E8F0),
                width: isInputFocused ? 2.0 : 1.2,
              ),
              boxShadow: isInputFocused
                  ? [
                      BoxShadow(
                        color: const Color(0xFF8B5CF6).withAlpha(60),
                        blurRadius: 18,
                        spreadRadius: 1,
                      ),
                      BoxShadow(
                        color: const Color(0xFF0F172A).withAlpha(15),
                        blurRadius: 8,
                        offset: const Offset(0, 3),
                      ),
                    ]
                  : [
                      BoxShadow(
                        color: const Color(0xFF0F172A).withAlpha(12),
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
                  size: 24,
                ),
                const SizedBox(width: 14),
                Expanded(
                  child: Focus(
                    onKeyEvent: (node, event) {
                      if (event is KeyDownEvent || event is KeyRepeatEvent) {
                        if (event.logicalKey == LogicalKeyboardKey.arrowDown) {
                          widget.searchInputFocusNode.unfocus();
                          if (widget.nextFocusNode != null) {
                            widget.nextFocusNode!.requestFocus();
                          } else if (widget.focusNode != null) {
                            widget.focusNode!.requestFocus();
                          }
                          return KeyEventResult.handled;
                        }
                        if (event.logicalKey == LogicalKeyboardKey.arrowRight &&
                            widget.searchController.selection.baseOffset == widget.searchController.text.length) {
                          widget.searchInputFocusNode.unfocus();
                          widget.searchButtonFocusNode.requestFocus();
                          return KeyEventResult.handled;
                        }
                      }
                      return KeyEventResult.ignored;
                    },
                    child: TextField(
                      controller: widget.searchController,
                      focusNode: widget.searchInputFocusNode,
                      autofocus: true,
                      cursorColor: const Color(0xFF7C3AED),
                      cursorWidth: 2.2,
                      onChanged: widget.onSearchChanged,
                      onSubmitted: (_) {
                        widget.searchInputFocusNode.unfocus();
                        if (widget.nextFocusNode != null) {
                          widget.nextFocusNode!.requestFocus();
                        } else if (widget.focusNode != null) {
                          widget.focusNode!.requestFocus();
                        }
                      },
                      onTapOutside: (_) {
                        widget.searchInputFocusNode.unfocus();
                      },
                      style: const TextStyle(
                        color: Color(0xFF1E1B2E),
                        fontSize: 16,
                        fontWeight: FontWeight.w600,
                        letterSpacing: 0.3,
                      ),
                      decoration: const InputDecoration(
                        hintText: 'Search anime by title, character, or studio...',
                        hintStyle: TextStyle(
                          color: Color(0xFF94A3B8),
                          fontSize: 15,
                          fontWeight: FontWeight.w400,
                        ),
                        border: InputBorder.none,
                        isDense: true,
                        contentPadding: EdgeInsets.symmetric(vertical: 16),
                      ),
                    ),
                  ),
                ),
                const SizedBox(width: 18),
              ],
            ),
          ),
        ),
        const SizedBox(width: 20),

        // Close Search Button
        _buildTvActionIcon(
          focusNode: widget.searchButtonFocusNode,
          icon: Icons.close_rounded,
          onPressed: widget.onToggleSearch,
          nextLeftFocusNode: widget.searchInputFocusNode,
          nextDownFocusNode: widget.nextFocusNode ?? widget.focusNode,
        ),
      ],
    );
  }

  Widget _buildSkeletonBanner() {
    return SizedBox(
      height: MediaQuery.of(context).size.height,
      child: Stack(
        children: [
          Container(
            color: const Color(0xFFF4EDFF),
            child: Stack(
              fit: StackFit.expand,
              children: [
                Container(color: const Color(0xFFEDE8F5)),
                Positioned(
                  bottom: 36,
                  left: 40,
                  right: 140,
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Container(
                        width: 120,
                        height: 18,
                        decoration: BoxDecoration(
                          color: const Color(0xFF7C3AED).withAlpha(30),
                          borderRadius: BorderRadius.circular(4),
                        ),
                      ),
                      const SizedBox(height: 12),
                      Container(
                        width: 380,
                        height: 40,
                        decoration: BoxDecoration(
                          color: const Color(0xFF1E1B2E).withAlpha(20),
                          borderRadius: BorderRadius.circular(8),
                        ),
                      ),
                      const SizedBox(height: 16),
                      Focus(
                        focusNode: _effectiveFocusNode,
                        onKeyEvent: (node, event) {
                          if (event is KeyDownEvent || event is KeyRepeatEvent) {
                            if (event.logicalKey == LogicalKeyboardKey.arrowUp) {
                              widget.searchButtonFocusNode.requestFocus();
                              return KeyEventResult.handled;
                            }
                            if (event.logicalKey == LogicalKeyboardKey.arrowDown) {
                              if (widget.nextFocusNode != null) {
                                widget.nextFocusNode!.requestFocus();
                              }
                              return KeyEventResult.handled;
                            }
                          }
                          return KeyEventResult.ignored;
                        },
                        child: Builder(
                          builder: (context) {
                            final isFocused = Focus.of(context).hasFocus;
                            return AnimatedScale(
                              scale: isFocused ? 1.08 : 1.0,
                              duration: const Duration(milliseconds: 150),
                              child: Container(
                                padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
                                decoration: BoxDecoration(
                                  gradient: AppColors.logoGradient,
                                  borderRadius: BorderRadius.circular(14),
                                  border: Border.all(
                                    color: isFocused ? Colors.white : Colors.transparent,
                                    width: isFocused ? 3.0 : 0,
                                  ),
                                ),
                                child: const Row(
                                  mainAxisSize: MainAxisSize.min,
                                  children: [
                                    Icon(Icons.play_arrow_rounded, color: Colors.white, size: 26),
                                    SizedBox(width: 8),
                                    Text(
                                      'Watch Now',
                                      style: TextStyle(
                                        color: Colors.white,
                                        fontSize: 17,
                                        fontWeight: FontWeight.w800,
                                        letterSpacing: 0.4,
                                      ),
                                    ),
                                  ],
                                ),
                              ),
                            );
                          },
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
          Positioned(
            top: 0,
            left: 0,
            right: 0,
            child: _buildTopAppBar(),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    if (widget.featuredShows.isEmpty) return _buildSkeletonBanner();

    final show = widget.featuredShows[_currentIndex.clamp(0, widget.featuredShows.length - 1)];
    final title = show.englishName ?? show.name;

    return SizedBox(
      height: MediaQuery.of(context).size.height,
      child: Stack(
        children: [
          // 1. Background Artwork Carousel
          PageView.builder(
            controller: _pageController,
            itemCount: widget.featuredShows.length,
            onPageChanged: (index) {
              setState(() => _currentIndex = index);
            },
            itemBuilder: (context, index) {
              final item = widget.featuredShows[index];
              final artwork = item.banner ?? item.thumbnail;

              return ClipRRect(
                borderRadius: BorderRadius.zero,
                child: Container(
                  color: const Color(0xFFF4EDFF),
                  child: Stack(
                    fit: StackFit.expand,
                    children: [
                      if (artwork != null && artwork.isNotEmpty)
                        Image.network(
                          artwork,
                          headers: ImageUtils.imageHeaders,
                          fit: BoxFit.cover,
                          cacheWidth: 1080,
                          errorBuilder: (context, error, stackTrace) =>
                              Container(color: const Color(0xFFEDE8F5)),
                        )
                      else
                        Container(color: const Color(0xFFEDE8F5)),

                      // Subtle dark vignette at bottom-left for maximum text legibility on all backgrounds
                      Positioned.fill(
                        child: Container(
                          decoration: BoxDecoration(
                            gradient: LinearGradient(
                              begin: Alignment.bottomLeft,
                              end: Alignment.topRight,
                              colors: [
                                const Color(0xFF0A0814).withAlpha(180),
                                const Color(0xFF0A0814).withAlpha(70),
                                Colors.transparent,
                              ],
                              stops: const [0.0, 0.40, 0.75],
                            ),
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              );
            },
          ),

          // 2. Persistent Hero Content Details (Stationary & Single Focus Tree Anchor)
          Positioned(
            bottom: 36,
            left: 40,
            right: 140,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              mainAxisSize: MainAxisSize.min,
              children: [
                if (show.type != null || show.seasonYear != null || (show.score != null && show.score! > 0)) ...[
                  Row(
                    children: [
                      // Score Badge
                      if (show.score != null && show.score! > 0) ...[
                        Container(
                          padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
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
                              const Icon(Icons.star_rounded, color: Color(0xFFF59E0B), size: 16),
                              const SizedBox(width: 4),
                              Text(
                                show.score!.toStringAsFixed(1),
                                style: const TextStyle(
                                  color: Color(0xFF92400E),
                                  fontSize: 13.5,
                                  fontWeight: FontWeight.w900,
                                ),
                              ),
                            ],
                          ),
                        ),
                        const SizedBox(width: 12),
                      ],
                      if (show.type != null) ...[
                        Container(
                          padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                          decoration: BoxDecoration(
                            color: const Color(0xFFF3E8FF),
                            borderRadius: BorderRadius.circular(8),
                            border: Border.all(
                              color: const Color(0xFF8B5CF6).withAlpha(100),
                              width: 1.0,
                            ),
                          ),
                          child: Text(
                            show.type!.toUpperCase(),
                            style: const TextStyle(
                              color: Color(0xFF6D28D9),
                              fontSize: 12.5,
                              fontWeight: FontWeight.w800,
                              letterSpacing: 0.6,
                            ),
                          ),
                        ),
                        const SizedBox(width: 12),
                      ],
                      if (show.seasonYear != null) ...[
                        Container(
                          padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                          decoration: BoxDecoration(
                            color: Colors.white,
                            borderRadius: BorderRadius.circular(8),
                            border: Border.all(color: const Color(0xFFEDE8F5), width: 1.2),
                            boxShadow: [
                              BoxShadow(
                                color: const Color(0xFF0F172A).withAlpha(10),
                                blurRadius: 6,
                                offset: const Offset(0, 2),
                              ),
                            ],
                          ),
                          child: Text(
                            '${show.seasonQuarter ?? ''} ${show.seasonYear}'.trim(),
                            style: const TextStyle(
                              color: AppColors.textSecondary,
                              fontSize: 12.5,
                              fontWeight: FontWeight.w700,
                            ),
                          ),
                        ),
                      ],
                    ],
                  ),
                  const SizedBox(height: 12),
                ],
                Text(
                  title,
                  key: ValueKey('hero_title_${show.id}_$_currentIndex'),
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(
                    color: Colors.white,
                    fontSize: 48,
                    fontWeight: FontWeight.w900,
                    height: 1.12,
                    letterSpacing: -0.6,
                    shadows: [
                      Shadow(
                        color: Colors.black,
                        blurRadius: 18,
                        offset: Offset(0, 3),
                      ),
                      Shadow(
                        color: Colors.black87,
                        blurRadius: 6,
                        offset: Offset(0, 1),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 18),
                Row(
                  children: [
                    Focus(
                      focusNode: _effectiveFocusNode,
                      onKeyEvent: (node, event) {
                        if (event is KeyDownEvent || event is KeyRepeatEvent) {
                          if (event.logicalKey == LogicalKeyboardKey.select ||
                              event.logicalKey == LogicalKeyboardKey.enter ||
                              event.logicalKey == LogicalKeyboardKey.space ||
                              event.logicalKey == LogicalKeyboardKey.gameButtonA) {
                            widget.onTap(show);
                            return KeyEventResult.handled;
                          }
                          if (event.logicalKey == LogicalKeyboardKey.arrowUp) {
                            widget.searchButtonFocusNode.requestFocus();
                            return KeyEventResult.handled;
                          }
                          if (event.logicalKey == LogicalKeyboardKey.arrowLeft) {
                            _goToPrevious();
                            return KeyEventResult.handled;
                          }
                          if (event.logicalKey == LogicalKeyboardKey.arrowRight) {
                            _goToNext();
                            return KeyEventResult.handled;
                          }
                          if (event.logicalKey == LogicalKeyboardKey.arrowDown) {
                            if (widget.nextFocusNode != null) {
                              widget.nextFocusNode!.requestFocus();
                            }
                            return KeyEventResult.handled;
                          }
                        }
                        return KeyEventResult.ignored;
                      },
                      child: Builder(
                        builder: (context) {
                          final isFocused = Focus.of(context).hasFocus;
                          return AnimatedScale(
                            scale: isFocused ? 1.08 : 1.0,
                            duration: const Duration(milliseconds: 150),
                            child: Material(
                              color: Colors.transparent,
                              borderRadius: BorderRadius.circular(16),
                              child: InkWell(
                                canRequestFocus: false,
                                onTap: () => widget.onTap(show),
                                borderRadius: BorderRadius.circular(16),
                                child: Container(
                                  padding: const EdgeInsets.symmetric(horizontal: 28, vertical: 14),
                                  decoration: BoxDecoration(
                                    gradient: AppColors.logoGradient,
                                    borderRadius: BorderRadius.circular(16),
                                    border: Border.all(
                                      color: isFocused ? Colors.white : Colors.transparent,
                                      width: isFocused ? 3.0 : 0,
                                    ),
                                    boxShadow: [
                                      BoxShadow(
                                        color: isFocused
                                            ? AppColors.primary.withAlpha(180)
                                            : AppColors.primaryDark.withAlpha(70),
                                        blurRadius: isFocused ? 24 : 12,
                                        offset: const Offset(0, 4),
                                      ),
                                    ],
                                  ),
                                  child: const Row(
                                    mainAxisSize: MainAxisSize.min,
                                    children: [
                                      Icon(Icons.play_arrow_rounded, color: Colors.white, size: 28),
                                      SizedBox(width: 8),
                                      Text(
                                        'Watch Now',
                                        style: TextStyle(
                                          color: Colors.white,
                                          fontSize: 17,
                                          fontWeight: FontWeight.w900,
                                          letterSpacing: 0.4,
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
              ],
            ),
          ),

          // 3. Top App Bar overlayed at the top
          Positioned(
            top: 0,
            left: 0,
            right: 0,
            child: _buildTopAppBar(),
          ),

          // 4. Slide indicator dots at the bottom right
          Positioned(
            bottom: 16,
            right: 36,
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: List.generate(widget.featuredShows.length, (i) {
                final isCurrent = _currentIndex == i;
                return AnimatedContainer(
                  duration: const Duration(milliseconds: 250),
                  margin: const EdgeInsets.symmetric(horizontal: 3.5),
                  height: 5,
                  width: isCurrent ? 22 : 6,
                  decoration: BoxDecoration(
                    gradient: isCurrent ? AppColors.logoGradient : null,
                    color: isCurrent ? null : const Color(0xFF9D97AD).withAlpha(120),
                    borderRadius: BorderRadius.circular(3),
                  ),
                );
              }),
            ),
          ),
        ],
      ),
    );
  }
}
