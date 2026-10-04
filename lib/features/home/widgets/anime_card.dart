import 'package:flutter/material.dart';
import '../../../../theme/app_colors.dart';
import '../../../../utils/image_utils.dart';
import '../models/anime_show.dart';

class AnimeCard extends StatelessWidget {
  final AnimeShow anime;
  final VoidCallback? onTap;

  const AnimeCard({
    super.key,
    required this.anime,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(18),
        child: Container(
          decoration: BoxDecoration(
            color: const Color(0xFFEDE8F5),
            borderRadius: BorderRadius.circular(18),
            boxShadow: const [
              BoxShadow(
                color: AppColors.shadowColor,
                blurRadius: 14,
                offset: Offset(0, 5),
              ),
            ],
          ),
          clipBehavior: Clip.antiAlias,
          child: (anime.thumbnail != null && anime.thumbnail!.isNotEmpty)
              ? Image.network(
                  anime.thumbnail!,
                  headers: ImageUtils.imageHeaders,
                  fit: BoxFit.cover,
                  cacheWidth: 320,
                  frameBuilder: (context, child, frame, wasSynchronouslyLoaded) {
                    if (wasSynchronouslyLoaded || frame != null) {
                      return Stack(
                        fit: StackFit.expand,
                        children: [
                          child,
                          _buildDarkGradientsAndContent(),
                        ],
                      );
                    }
                    return _buildLoadingPlaceholder();
                  },
                  errorBuilder: (context, error, stackTrace) =>
                      _buildFallbackCover(),
                )
              : _buildFallbackCover(),
        ),
      ),
    );
  }

  Widget _buildLoadingPlaceholder() {
    return Container(
      color: const Color(0xFFEDE8F5),
      child: const Center(
        child: SizedBox(
          width: 22,
          height: 22,
          child: CircularProgressIndicator(
            strokeWidth: 2,
            color: AppColors.primary,
          ),
        ),
      ),
    );
  }

  Widget _buildDarkGradientsAndContent() {
    return Stack(
      fit: StackFit.expand,
      children: [
        // 1. Bottom 25% Gradient Overlay (ONLY shown over the loaded image)
        Positioned.fill(
          child: Container(
            decoration: BoxDecoration(
              gradient: LinearGradient(
                begin: Alignment.bottomCenter,
                end: Alignment.topCenter,
                colors: [
                  const Color(0xFF0F0C1B),                  // Opaque dark at the bottom
                  const Color(0xFF0F0C1B).withAlpha(220),   // Dark behind title
                  const Color(0xFF0F0C1B).withAlpha(120),   // Gentle fade
                  Colors.transparent,                       // Fully transparent from 25% up
                ],
                stops: const [0.0, 0.08, 0.16, 0.25],
              ),
            ),
          ),
        ),

        // 2. Subtle top vignette for top badge contrast
        Positioned(
          top: 0,
          left: 0,
          right: 0,
          height: 48,
          child: Container(
            decoration: BoxDecoration(
              gradient: LinearGradient(
                begin: Alignment.topCenter,
                end: Alignment.bottomCenter,
                colors: [
                  Colors.black.withAlpha(130),
                  Colors.transparent,
                ],
              ),
            ),
          ),
        ),

        // 3. Top Left: Type Badge (TV, Movie, ONA, etc.)
        if (anime.type != null && anime.type!.isNotEmpty)
          Positioned(
            top: 8,
            left: 8,
            child: Container(
              padding: const EdgeInsets.symmetric(
                horizontal: 7,
                vertical: 3.5,
              ),
              decoration: BoxDecoration(
                gradient: AppColors.primaryGradient,
                borderRadius: BorderRadius.circular(8),
                boxShadow: [
                  BoxShadow(
                    color: AppColors.primary.withAlpha(120),
                    blurRadius: 6,
                    offset: const Offset(0, 2),
                  ),
                ],
              ),
              child: Text(
                anime.type!,
                style: const TextStyle(
                  color: Colors.white,
                  fontSize: 10,
                  fontWeight: FontWeight.bold,
                  letterSpacing: 0.4,
                ),
              ),
            ),
          ),

        // 4. Top Right: Rating Score
        Positioned(
          top: 8,
          right: 8,
          child: Container(
            padding: const EdgeInsets.symmetric(
              horizontal: 6,
              vertical: 3.5,
            ),
            decoration: BoxDecoration(
              color: Colors.black.withAlpha(180),
              borderRadius: BorderRadius.circular(8),
              border: Border.all(
                color: Colors.white.withAlpha(25),
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
                  (anime.score != null && anime.score! > 0)
                      ? anime.score!.toStringAsFixed(1)
                      : 'N/A',
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

        // 5. Bottom Content (SUB/DUB badges + Anime Name)
        Positioned(
          bottom: 0,
          left: 0,
          right: 0,
          child: Padding(
            padding: const EdgeInsets.fromLTRB(10, 0, 10, 11),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              mainAxisSize: MainAxisSize.min,
              children: [
                if (_subLabel != null || _dubLabel != null) ...[
                  Row(
                    children: [
                      if (_subLabel != null) ...[
                        Container(
                          padding: const EdgeInsets.symmetric(
                            horizontal: 5,
                            vertical: 2,
                          ),
                          decoration: BoxDecoration(
                            color: const Color(0xFF6366F1),
                            borderRadius: BorderRadius.circular(5),
                          ),
                          child: Text(
                            _subLabel!,
                            style: const TextStyle(
                              color: Colors.white,
                              fontSize: 9.5,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                        ),
                        const SizedBox(width: 4),
                      ],
                      if (_dubLabel != null)
                        Container(
                          padding: const EdgeInsets.symmetric(
                            horizontal: 5,
                            vertical: 2,
                          ),
                          decoration: BoxDecoration(
                            color: const Color(0xFF10B981),
                            borderRadius: BorderRadius.circular(5),
                          ),
                          child: Text(
                            _dubLabel!,
                            style: const TextStyle(
                              color: Colors.white,
                              fontSize: 9.5,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                        ),
                    ],
                  ),
                  const SizedBox(height: 6),
                ],

                Text(
                  anime.englishName ?? anime.name,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(
                    color: Colors.white,
                    fontSize: 13,
                    fontWeight: FontWeight.w700,
                    letterSpacing: 0.1,
                    height: 1.2,
                  ),
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }

  String? get _subLabel {
    final ep = anime.lastEpisodeSub ??
        (anime.availableEpisodesSub > 0
            ? anime.availableEpisodesSub.toString()
            : null);
    return ep != null ? 'SUB $ep' : null;
  }

  String? get _dubLabel {
    final ep = anime.lastEpisodeDub ??
        (anime.availableEpisodesDub > 0
            ? anime.availableEpisodesDub.toString()
            : null);
    return ep != null ? 'DUB $ep' : null;
  }

  Widget _buildFallbackCover() {
    return Container(
      color: const Color(0xFFEDE8F5),
      padding: const EdgeInsets.all(12),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const Icon(
            Icons.movie_creation_outlined,
            color: AppColors.primary,
            size: 38,
          ),
          const SizedBox(height: 8),
          Text(
            anime.englishName ?? anime.name,
            maxLines: 2,
            textAlign: TextAlign.center,
            overflow: TextOverflow.ellipsis,
            style: const TextStyle(
              color: AppColors.textPrimary,
              fontSize: 12,
              fontWeight: FontWeight.w700,
            ),
          ),
        ],
      ),
    );
  }
}
