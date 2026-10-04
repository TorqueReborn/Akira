import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../../../../utils/image_utils.dart';
import '../models/anime_show.dart';

/// TV Optimized Anime Card with focus glow, scale elevation, D-Pad select handling, and TV 10-foot legibility.
class TvAnimeCard extends StatefulWidget {
  final AnimeShow anime;
  final VoidCallback onTap;
  final bool autofocus;
  final ValueChanged<bool>? onFocusChange;
  final FocusNode? focusNode;
  final VoidCallback? onArrowUp;
  final VoidCallback? onArrowDown;
  final VoidCallback? onArrowRight;

  const TvAnimeCard({
    super.key,
    required this.anime,
    required this.onTap,
    this.autofocus = false,
    this.onFocusChange,
    this.focusNode,
    this.onArrowUp,
    this.onArrowDown,
    this.onArrowRight,
  });

  @override
  State<TvAnimeCard> createState() => _TvAnimeCardState();
}

class _TvAnimeCardState extends State<TvAnimeCard> {
  late final FocusNode _internalFocusNode;
  bool _isFocused = false;

  FocusNode get _effectiveFocusNode => widget.focusNode ?? _internalFocusNode;

  @override
  void initState() {
    super.initState();
    _internalFocusNode = FocusNode();
    _effectiveFocusNode.addListener(_onFocusChange);
  }

  @override
  void didUpdateWidget(TvAnimeCard oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.focusNode != widget.focusNode) {
      (oldWidget.focusNode ?? _internalFocusNode).removeListener(_onFocusChange);
      _effectiveFocusNode.addListener(_onFocusChange);
    }
  }

  void _onFocusChange() {
    if (mounted) {
      setState(() {
        _isFocused = _effectiveFocusNode.hasFocus;
      });
      widget.onFocusChange?.call(_isFocused);
    }
  }

  @override
  void dispose() {
    _effectiveFocusNode.removeListener(_onFocusChange);
    _internalFocusNode.dispose();
    super.dispose();
  }

  Widget _buildFallbackCover() {
    return Container(
      color: const Color(0xFFF1EDF8),
      alignment: Alignment.center,
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const Icon(
            Icons.movie_creation_outlined,
            size: 38,
            color: Color(0xFF9D97AD),
          ),
          const SizedBox(height: 8),
          Text(
            widget.anime.englishName ?? widget.anime.name,
            textAlign: TextAlign.center,
            maxLines: 2,
            overflow: TextOverflow.ellipsis,
            style: const TextStyle(
              fontSize: 12,
              fontWeight: FontWeight.w600,
              color: Color(0xFF6E6B7B),
            ),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Focus(
      focusNode: _effectiveFocusNode,
      autofocus: widget.autofocus,
      onKeyEvent: (node, event) {
        if (event is KeyDownEvent) {
          if (event.logicalKey == LogicalKeyboardKey.select ||
              event.logicalKey == LogicalKeyboardKey.enter ||
              event.logicalKey == LogicalKeyboardKey.space ||
              event.logicalKey == LogicalKeyboardKey.gameButtonA) {
            widget.onTap();
            return KeyEventResult.handled;
          }
          if (event.logicalKey == LogicalKeyboardKey.arrowUp && widget.onArrowUp != null) {
            widget.onArrowUp!();
            return KeyEventResult.handled;
          }
          if (event.logicalKey == LogicalKeyboardKey.arrowDown && widget.onArrowDown != null) {
            widget.onArrowDown!();
            return KeyEventResult.handled;
          }
          if (event.logicalKey == LogicalKeyboardKey.arrowRight && widget.onArrowRight != null) {
            widget.onArrowRight!();
            return KeyEventResult.handled;
          }
        }
        return KeyEventResult.ignored;
      },
      child: GestureDetector(
        onTap: widget.onTap,
        child: AnimatedScale(
          scale: _isFocused ? 1.08 : 1.0,
          duration: const Duration(milliseconds: 180),
          curve: Curves.easeOutCubic,
          child: AnimatedOpacity(
            opacity: _isFocused ? 1.0 : 0.90,
            duration: const Duration(milliseconds: 180),
            child: AnimatedContainer(
              margin: const EdgeInsets.symmetric(vertical: 6, horizontal: 3),
              duration: const Duration(milliseconds: 180),
              curve: Curves.easeOutCubic,
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(18),
                border: Border.all(
                  color: _isFocused ? const Color(0xFF7C3AED) : Colors.black.withAlpha(12),
                  width: _isFocused ? 3.2 : 1.0,
                ),
                boxShadow: [
                  if (_isFocused) ...[
                    BoxShadow(
                      color: const Color(0xFF8B5CF6).withAlpha(140),
                      blurRadius: 22,
                      offset: const Offset(0, 8),
                    ),
                    BoxShadow(
                      color: const Color(0xFF0F172A).withAlpha(35),
                      blurRadius: 12,
                      offset: const Offset(0, 4),
                    ),
                  ] else ...[
                    BoxShadow(
                      color: const Color(0xFF0F172A).withAlpha(18),
                      blurRadius: 8,
                      offset: const Offset(0, 3),
                    ),
                  ],
                ],
              ),
              child: ClipRRect(
                borderRadius: BorderRadius.circular(14.5),
                child: Container(
                  color: const Color(0xFFF1EDF8),
                  child: Stack(
                    fit: StackFit.expand,
                    children: [
                      if (widget.anime.thumbnail != null && widget.anime.thumbnail!.isNotEmpty)
                        Image.network(
                          widget.anime.thumbnail!,
                          headers: ImageUtils.imageHeaders,
                          fit: BoxFit.cover,
                          errorBuilder: (context, error, stackTrace) => _buildFallbackCover(),
                        )
                      else
                        _buildFallbackCover(),

                      // Subtle gradient overlay at bottom
                      Positioned(
                        bottom: 0,
                        left: 0,
                        right: 0,
                        height: 60,
                        child: Container(
                          decoration: BoxDecoration(
                            gradient: LinearGradient(
                              begin: Alignment.topCenter,
                              end: Alignment.bottomCenter,
                              colors: [
                                Colors.transparent,
                                Colors.black.withAlpha(140),
                              ],
                            ),
                          ),
                        ),
                      ),

                      // Star Score badge on top right of card
                      if (widget.anime.score != null && widget.anime.score! > 0)
                        Positioned(
                          top: 8,
                          right: 8,
                          child: Container(
                            padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 3),
                            decoration: BoxDecoration(
                              color: const Color(0xFF0F0C1B).withAlpha(220),
                              borderRadius: BorderRadius.circular(8),
                              border: Border.all(
                                color: const Color(0xFFFBBF24).withAlpha(140),
                                width: 1.0,
                              ),
                            ),
                            child: Row(
                              mainAxisSize: MainAxisSize.min,
                              children: [
                                const Icon(Icons.star_rounded, color: Color(0xFFFBBF24), size: 12),
                                const SizedBox(width: 3),
                                Text(
                                  widget.anime.score!.toStringAsFixed(1),
                                  style: const TextStyle(
                                    color: Colors.white,
                                    fontSize: 11,
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
              ),
            ),
          ),
        ),
      ),
    );
  }
}
