import 'dart:async';
import 'dart:developer' as developer;
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:video_player/video_player.dart';
import 'package:wakelock_plus/wakelock_plus.dart';

import '../../../../theme/app_colors.dart';
import '../models/stream_source.dart';
import '../services/watch_history_manager.dart';

/// TV Optimized Video Player Screen designed for D-Pad Remote Controls.
/// - D-Pad Left: Seek -10s
/// - D-Pad Right: Seek +10s
/// - D-Pad Select / Enter / Space / PlayPause: Toggle Play/Pause
/// - D-Pad Up / Down: Toggle / Navigate Overlay Controls
class TvPlayerScreen extends StatefulWidget {
  final String animeId;
  final String animeTitle;
  final String episodeNumber;
  final List<StreamSource> sources;
  final String? thumbnail;
  final int initialPositionMs;

  const TvPlayerScreen({
    super.key,
    required this.animeId,
    required this.animeTitle,
    required this.episodeNumber,
    required this.sources,
    this.thumbnail,
    this.initialPositionMs = 0,
  });

  @override
  State<TvPlayerScreen> createState() => _TvPlayerScreenState();
}

class _TvPlayerScreenState extends State<TvPlayerScreen> {
  VideoPlayerController? _controller;
  late List<StreamSource> _playableSources;
  int _currentSourceIndex = 0;
  bool _isLoading = true;
  String? _statusMessage;
  String? _errorMessage;

  bool _showControls = true;
  Timer? _controlsTimer;
  Timer? _progressSaveTimer;

  // Seeking OSD feedback state
  int? _seekFeedbackSeconds; // e.g. +10 or -10
  Timer? _seekFeedbackTimer;

  final FocusNode _mainPlayerFocusNode = FocusNode();
  final FocusNode _backButtonFocusNode = FocusNode();
  final FocusNode _sourceButtonFocusNode = FocusNode();

  @override
  void initState() {
    super.initState();
    WakelockPlus.enable();

    // Force TV landscape orientation
    SystemChrome.setEnabledSystemUIMode(SystemUiMode.immersiveSticky);
    SystemChrome.setPreferredOrientations([
      DeviceOrientation.landscapeLeft,
      DeviceOrientation.landscapeRight,
    ]);

    final directSources = widget.sources.where((s) => s.isDirect).toList();
    _playableSources = directSources.isNotEmpty ? directSources : widget.sources;

    if (_playableSources.isNotEmpty && _playableSources.any((s) => s.isDirect)) {
      _initSource(0, widget.initialPositionMs);
    } else {
      setState(() {
        _isLoading = false;
        _errorMessage = 'No direct playable video stream sources found for this episode.';
      });
    }

    _startControlsTimer();

    _progressSaveTimer = Timer.periodic(const Duration(seconds: 5), (_) {
      _saveCurrentProgress();
    });

    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (mounted) {
        _mainPlayerFocusNode.requestFocus();
      }
    });
  }

  @override
  void dispose() {
    _progressSaveTimer?.cancel();
    _controlsTimer?.cancel();
    _seekFeedbackTimer?.cancel();
    _saveCurrentProgress();

    _controller?.removeListener(_videoPlayerListener);
    _controller?.dispose();

    _mainPlayerFocusNode.dispose();
    _backButtonFocusNode.dispose();
    _sourceButtonFocusNode.dispose();

    WakelockPlus.disable();

    // Preserve landscape orientation when returning to TV screens
    SystemChrome.setEnabledSystemUIMode(SystemUiMode.edgeToEdge);
    SystemChrome.setPreferredOrientations([
      DeviceOrientation.landscapeLeft,
      DeviceOrientation.landscapeRight,
    ]);
    super.dispose();
  }

  void _startControlsTimer() {
    _controlsTimer?.cancel();
    _controlsTimer = Timer(const Duration(seconds: 4), () {
      if (mounted && _controller != null && _controller!.value.isPlaying) {
        setState(() {
          _showControls = false;
        });
      }
    });
  }

  void _showOverlayControls() {
    setState(() {
      _showControls = true;
    });
    _startControlsTimer();
  }

  void _togglePlayPause() {
    if (_controller == null || !_controller!.value.isInitialized) return;
    setState(() {
      if (_controller!.value.isPlaying) {
        _controller!.pause();
        _showControls = true;
      } else {
        _controller!.play();
        _startControlsTimer();
      }
    });
  }

  void _seekRelative(int seconds) {
    if (_controller == null || !_controller!.value.isInitialized) return;
    final currentPos = _controller!.value.position;
    final totalDuration = _controller!.value.duration;

    final newMs = (currentPos.inMilliseconds + seconds * 1000)
        .clamp(0, totalDuration.inMilliseconds);
    final targetPos = Duration(milliseconds: newMs);

    _controller!.seekTo(targetPos);

    // Show visual seek OSD (+10s / -10s)
    _seekFeedbackTimer?.cancel();
    setState(() {
      _seekFeedbackSeconds = seconds;
      _showControls = true;
    });

    _seekFeedbackTimer = Timer(const Duration(milliseconds: 1200), () {
      if (mounted) {
        setState(() {
          _seekFeedbackSeconds = null;
        });
      }
    });

    _startControlsTimer();
  }

  Future<void> _initSource(int index, [int startPositionMs = 0]) async {
    if (index < 0 || index >= _playableSources.length) {
      setState(() {
        _isLoading = false;
        _errorMessage = 'All available direct video sources failed to play.';
      });
      return;
    }

    final source = _playableSources[index];
    if (!source.isDirect) {
      if (index + 1 < _playableSources.length) {
        _initSource(index + 1, startPositionMs);
      } else {
        setState(() {
          _isLoading = false;
          _errorMessage = 'No direct playable sources available.';
        });
      }
      return;
    }

    setState(() {
      _currentSourceIndex = index;
      _isLoading = true;
      _errorMessage = null;
      _statusMessage = 'Loading ${source.sourceName}...';
    });

    final oldController = _controller;
    _controller = null;
    if (oldController != null) {
      await oldController.dispose();
    }

    try {
      final uri = Uri.parse(source.url);
      developer.log('[TvPlayerScreen] Initializing VideoPlayer: source=${source.sourceName}');

      final headers = <String, String>{
        'User-Agent':
            'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:155.0) Gecko/20100101 Firefox/155.0',
        'Referer': 'https://youtu-chan.com/',
        'Origin': 'https://youtu-chan.com',
        'Accept': '*/*',
        'Sec-Fetch-Dest': 'video',
        'Sec-Fetch-Mode': 'cors',
        'Sec-Fetch-Site': 'cross-site',
      };

      final controller = VideoPlayerController.networkUrl(
        uri,
        httpHeaders: headers,
      );

      await controller.initialize();

      if (startPositionMs > 0) {
        await controller.seekTo(Duration(milliseconds: startPositionMs));
      }

      await controller.play();
      controller.addListener(_videoPlayerListener);

      if (!mounted) return;
      setState(() {
        _controller = controller;
        _isLoading = false;
        _statusMessage = null;
        _errorMessage = null;
      });
    } catch (e) {
      developer.log('[TvPlayerScreen] Playback error on ${source.sourceName}: $e');
      if (!mounted) return;
      if (index + 1 < _playableSources.length) {
        final nextSource = _playableSources[index + 1];
        setState(() {
          _statusMessage =
              '${source.sourceName} failed. Switching to ${nextSource.sourceName}...';
        });
        _initSource(index + 1, startPositionMs);
      } else {
        setState(() {
          _isLoading = false;
          _errorMessage =
              'Playback failed on all available direct sources: ${e.toString()}';
        });
      }
    }
  }

  void _videoPlayerListener() {
    if (!mounted || _controller == null) return;
    final value = _controller!.value;

    if (value.hasError && _errorMessage == null) {
      final currentPos = value.position.inMilliseconds;
      if (_currentSourceIndex + 1 < _playableSources.length) {
        _initSource(_currentSourceIndex + 1, currentPos);
      } else {
        setState(() {
          _errorMessage = value.errorDescription ?? 'Playback error occurred.';
          _isLoading = false;
        });
      }
    } else {
      setState(() {});
    }
  }

  void _saveCurrentProgress() {
    if (_controller == null || !_controller!.value.isInitialized) return;
    final posMs = _controller!.value.position.inMilliseconds;
    final durMs = _controller!.value.duration.inMilliseconds;

    if (posMs > 0 && durMs > 0) {
      WatchHistoryManager.saveProgress(
        animeId: widget.animeId,
        animeTitle: widget.animeTitle,
        episodeNumber: widget.episodeNumber,
        seekPositionMs: posMs,
        totalDurationMs: durMs,
        thumbnail: widget.thumbnail,
      );
    }
  }

  String _formatDuration(Duration duration) {
    final hours = duration.inHours;
    final minutes = duration.inMinutes.remainder(60);
    final seconds = duration.inSeconds.remainder(60);
    if (hours > 0) {
      return '${hours.toString().padLeft(2, '0')}:${minutes.toString().padLeft(2, '0')}:${seconds.toString().padLeft(2, '0')}';
    } else {
      return '${minutes.toString().padLeft(2, '0')}:${seconds.toString().padLeft(2, '0')}';
    }
  }

  void _showSourcePickerDialog() {
    if (_playableSources.isEmpty) return;
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: const Color(0xFF181528),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
        title: const Text(
          'Select Video Stream Source',
          style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
        ),
        content: SizedBox(
          width: 320,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: _playableSources.asMap().entries.map((entry) {
              final idx = entry.key;
              final src = entry.value;
              final isCurrent = idx == _currentSourceIndex;

              return Container(
                margin: const EdgeInsets.symmetric(vertical: 4),
                child: ListTile(
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(12),
                  ),
                  tileColor: isCurrent
                      ? AppColors.primary.withAlpha(80)
                      : const Color(0xFF231E38),
                  leading: Icon(
                    isCurrent ? Icons.radio_button_checked : Icons.radio_button_unchecked,
                    color: isCurrent ? AppColors.primaryLight : Colors.white60,
                  ),
                  title: Text(
                    src.sourceName,
                    style: TextStyle(
                      color: isCurrent ? Colors.white : Colors.white70,
                      fontWeight: isCurrent ? FontWeight.bold : FontWeight.normal,
                    ),
                  ),
                  onTap: () {
                    Navigator.of(ctx).pop();
                    final currentMs = _controller?.value.position.inMilliseconds ?? 0;
                    _initSource(idx, currentMs);
                  },
                ),
              );
            }).toList(),
          ),
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final controller = _controller;
    final isInitialized = controller != null && controller.value.isInitialized;
    final isPlaying = isInitialized && controller.value.isPlaying;

    final currentPos = isInitialized ? controller.value.position : Duration.zero;
    final totalDur = isInitialized ? controller.value.duration : Duration.zero;
    final progressRatio = totalDur.inMilliseconds > 0
        ? (currentPos.inMilliseconds / totalDur.inMilliseconds).clamp(0.0, 1.0)
        : 0.0;

    return PopScope(
      canPop: true,
      onPopInvokedWithResult: (didPop, result) {
        _saveCurrentProgress();
      },
      child: Focus(
        focusNode: _mainPlayerFocusNode,
        autofocus: true,
        onKeyEvent: (node, event) {
          if (event is KeyDownEvent || event is KeyRepeatEvent) {
            final key = event.logicalKey;

            // Direct Seek Right (+10s)
            if (key == LogicalKeyboardKey.arrowRight) {
              _seekRelative(10);
              return KeyEventResult.handled;
            }

            // Direct Seek Left (-10s)
            if (key == LogicalKeyboardKey.arrowLeft) {
              _seekRelative(-10);
              return KeyEventResult.handled;
            }

            // Play / Pause Toggle
            if (key == LogicalKeyboardKey.select ||
                key == LogicalKeyboardKey.enter ||
                key == LogicalKeyboardKey.space ||
                key == LogicalKeyboardKey.gameButtonA ||
                key == LogicalKeyboardKey.mediaPlayPause ||
                key == LogicalKeyboardKey.mediaPlay ||
                key == LogicalKeyboardKey.mediaPause) {
              _togglePlayPause();
              return KeyEventResult.handled;
            }

            // Show controls and navigate
            if (key == LogicalKeyboardKey.arrowUp || key == LogicalKeyboardKey.arrowDown) {
              _showOverlayControls();
              if (key == LogicalKeyboardKey.arrowUp && _backButtonFocusNode.canRequestFocus) {
                _backButtonFocusNode.requestFocus();
              }
              return KeyEventResult.handled;
            }

            // Exit Player on Back button
            if (key == LogicalKeyboardKey.escape ||
                key == LogicalKeyboardKey.backspace ||
                key == LogicalKeyboardKey.gameButtonB) {
              _saveCurrentProgress();
              Navigator.of(context).maybePop();
              return KeyEventResult.handled;
            }
          }
          return KeyEventResult.ignored;
        },
        child: Scaffold(
          backgroundColor: Colors.black,
          body: GestureDetector(
            behavior: HitTestBehavior.opaque,
            onTap: () {
              _showOverlayControls();
              _togglePlayPause();
            },
            child: Stack(
              fit: StackFit.expand,
              children: [
                // 1. Video Canvas
                if (isInitialized)
                  Center(
                    child: AspectRatio(
                      aspectRatio: controller.value.aspectRatio > 0
                          ? controller.value.aspectRatio
                          : 16 / 9,
                      child: VideoPlayer(controller),
                    ),
                  )
                else
                  Container(color: Colors.black),

                // 2. Loading State / Status Indicator
                if (_isLoading)
                  Center(
                    child: Container(
                      padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 16),
                      decoration: BoxDecoration(
                        color: Colors.black.withAlpha(220),
                        borderRadius: BorderRadius.circular(16),
                        border: Border.all(color: Colors.white.withAlpha(30)),
                      ),
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          const SizedBox(
                            width: 42,
                            height: 48,
                            child: CircularProgressIndicator(
                              strokeWidth: 3.2,
                              color: AppColors.primary,
                            ),
                          ),
                          if (_statusMessage != null) ...[
                            const SizedBox(height: 14),
                            Text(
                              _statusMessage!,
                              style: const TextStyle(
                                color: Colors.white,
                                fontSize: 14,
                                fontWeight: FontWeight.w600,
                              ),
                            ),
                          ],
                        ],
                      ),
                    ),
                  ),

                // 3. Playback Error Overlay
                if (_errorMessage != null)
                  Center(
                    child: Container(
                      margin: const EdgeInsets.symmetric(horizontal: 32),
                      padding: const EdgeInsets.all(28),
                      decoration: BoxDecoration(
                        color: const Color(0xFF1E1015).withAlpha(240),
                        borderRadius: BorderRadius.circular(20),
                        border: Border.all(color: AppColors.primary.withAlpha(120), width: 1.5),
                      ),
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          const Icon(
                            Icons.error_outline_rounded,
                            color: AppColors.primary,
                            size: 48,
                          ),
                          const SizedBox(height: 14),
                          const Text(
                            'Playback Error',
                            style: TextStyle(
                              color: Colors.white,
                              fontSize: 20,
                              fontWeight: FontWeight.w800,
                            ),
                          ),
                          const SizedBox(height: 8),
                          Text(
                            _errorMessage!,
                            textAlign: TextAlign.center,
                            style: const TextStyle(
                              color: AppColors.textSecondary,
                              fontSize: 14,
                            ),
                          ),
                          const SizedBox(height: 20),
                          if (_playableSources.isNotEmpty)
                            Wrap(
                              spacing: 10,
                              runSpacing: 10,
                              children: _playableSources.asMap().entries.map((entry) {
                                final idx = entry.key;
                                final src = entry.value;
                                final isCurrent = idx == _currentSourceIndex;
                                return ActionChip(
                                  label: Text(
                                    src.sourceName,
                                    style: TextStyle(
                                      color: isCurrent ? Colors.white : AppColors.textPrimary,
                                      fontWeight: FontWeight.bold,
                                      fontSize: 13,
                                    ),
                                  ),
                                  backgroundColor: isCurrent
                                      ? AppColors.primary
                                      : const Color(0xFF2B2030),
                                  onPressed: () {
                                    _initSource(idx);
                                  },
                                );
                              }).toList(),
                            ),
                        ],
                      ),
                    ),
                  ),

                // 4. Center Seek Feedback Toast OSD (+10s / -10s)
                if (_seekFeedbackSeconds != null)
                  Center(
                    child: Transform.translate(
                      offset: const Offset(0, -85),
                      child: Container(
                        padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 14),
                        decoration: BoxDecoration(
                          color: Colors.black.withAlpha(210),
                          borderRadius: BorderRadius.circular(30),
                          border: Border.all(
                            color: AppColors.primary.withAlpha(160),
                            width: 1.5,
                          ),
                          boxShadow: [
                            BoxShadow(
                              color: AppColors.primary.withAlpha(100),
                              blurRadius: 20,
                            ),
                          ],
                        ),
                        child: Row(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            Icon(
                              _seekFeedbackSeconds! > 0
                                  ? Icons.fast_forward_rounded
                                  : Icons.fast_rewind_rounded,
                              color: Colors.white,
                              size: 28,
                            ),
                            const SizedBox(width: 8),
                            Text(
                              '${_seekFeedbackSeconds! > 0 ? '+' : ''}${_seekFeedbackSeconds!}s',
                              style: const TextStyle(
                                color: Colors.white,
                                fontSize: 20,
                                fontWeight: FontWeight.w900,
                                letterSpacing: 0.5,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ),
                  ),

                // 5. Full TV Remote Player Controls Overlay
                AnimatedOpacity(
                  opacity: _showControls ? 1.0 : 0.0,
                  duration: const Duration(milliseconds: 250),
                  child: IgnorePointer(
                    ignoring: !_showControls,
                    child: Stack(
                      children: [
                        // Top Bar Gradient Overlay
                        Positioned(
                          top: 0,
                          left: 0,
                          right: 0,
                          child: Container(
                            decoration: BoxDecoration(
                              gradient: LinearGradient(
                                begin: Alignment.topCenter,
                                end: Alignment.bottomCenter,
                                colors: [
                                  Colors.black.withAlpha(230),
                                  Colors.transparent,
                                ],
                              ),
                            ),
                            padding: const EdgeInsets.symmetric(horizontal: 28, vertical: 18),
                            child: SafeArea(
                              child: Row(
                                children: [
                                  // Focusable Back Button
                                  FocusableActionDetector(
                                    focusNode: _backButtonFocusNode,
                                    onShowFocusHighlight: (_) => setState(() {}),
                                    actions: <Type, Action<Intent>>{
                                      ActivateIntent: CallbackAction<ActivateIntent>(
                                        onInvoke: (_) {
                                          _saveCurrentProgress();
                                          Navigator.of(context).pop();
                                          return null;
                                        },
                                      ),
                                    },
                                    child: Builder(
                                      builder: (ctx) {
                                        final isFocused = Focus.of(ctx).hasFocus;
                                        return AnimatedScale(
                                          scale: isFocused ? 1.15 : 1.0,
                                          duration: const Duration(milliseconds: 140),
                                          child: Container(
                                            decoration: BoxDecoration(
                                              shape: BoxShape.circle,
                                              color: isFocused
                                                  ? AppColors.primary
                                                  : Colors.white.withAlpha(30),
                                              border: Border.all(
                                                color: isFocused ? Colors.white : Colors.transparent,
                                                width: isFocused ? 2.0 : 0,
                                              ),
                                            ),
                                            child: IconButton(
                                              icon: const Icon(
                                                Icons.arrow_back_rounded,
                                                color: Colors.white,
                                                size: 24,
                                              ),
                                              onPressed: () {
                                                _saveCurrentProgress();
                                                Navigator.of(context).pop();
                                              },
                                            ),
                                          ),
                                        );
                                      },
                                    ),
                                  ),
                                  const SizedBox(width: 16),
                                  Expanded(
                                    child: Column(
                                      crossAxisAlignment: CrossAxisAlignment.start,
                                      mainAxisSize: MainAxisSize.min,
                                      children: [
                                        Text(
                                          widget.animeTitle,
                                          maxLines: 1,
                                          overflow: TextOverflow.ellipsis,
                                          style: const TextStyle(
                                            color: Colors.white,
                                            fontSize: 18,
                                            fontWeight: FontWeight.w800,
                                          ),
                                        ),
                                        const SizedBox(height: 2),
                                        Text(
                                          'Episode ${widget.episodeNumber}',
                                          style: const TextStyle(
                                            color: Colors.white70,
                                            fontSize: 13,
                                            fontWeight: FontWeight.w600,
                                          ),
                                        ),
                                      ],
                                    ),
                                  ),
                                  // Source Picker Button
                                  FocusableActionDetector(
                                    focusNode: _sourceButtonFocusNode,
                                    onShowFocusHighlight: (_) => setState(() {}),
                                    actions: <Type, Action<Intent>>{
                                      ActivateIntent: CallbackAction<ActivateIntent>(
                                        onInvoke: (_) {
                                          _showSourcePickerDialog();
                                          return null;
                                        },
                                      ),
                                    },
                                    child: Builder(
                                      builder: (ctx) {
                                        final isFocused = Focus.of(ctx).hasFocus;
                                        final currentSource = _playableSources.isNotEmpty
                                            ? _playableSources[_currentSourceIndex]
                                            : null;

                                        return AnimatedScale(
                                          scale: isFocused ? 1.08 : 1.0,
                                          duration: const Duration(milliseconds: 140),
                                          child: Container(
                                            padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
                                            decoration: BoxDecoration(
                                              color: isFocused
                                                  ? AppColors.primary
                                                  : Colors.white.withAlpha(35),
                                              borderRadius: BorderRadius.circular(12),
                                              border: Border.all(
                                                color: isFocused ? Colors.white : Colors.white24,
                                                width: isFocused ? 2.0 : 1.0,
                                              ),
                                            ),
                                            child: Material(
                                              color: Colors.transparent,
                                              child: InkWell(
                                                onTap: _showSourcePickerDialog,
                                                borderRadius: BorderRadius.circular(12),
                                                child: Row(
                                                  mainAxisSize: MainAxisSize.min,
                                                  children: [
                                                    const Icon(Icons.tune_rounded, color: Colors.white, size: 16),
                                                    const SizedBox(width: 6),
                                                    Text(
                                                      currentSource?.sourceName ?? 'Source',
                                                      style: const TextStyle(
                                                        color: Colors.white,
                                                        fontSize: 13,
                                                        fontWeight: FontWeight.bold,
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
                            ),
                          ),
                        ),

                        // Center Play / Pause Action Button
                        if (isInitialized)
                          Center(
                            child: Material(
                              color: Colors.transparent,
                              child: InkWell(
                                onTap: _togglePlayPause,
                                borderRadius: BorderRadius.circular(40),
                                child: Container(
                                  width: 68,
                                  height: 68,
                                  decoration: BoxDecoration(
                                    shape: BoxShape.circle,
                                    gradient: AppColors.logoGradient,
                                    boxShadow: [
                                      BoxShadow(
                                        color: AppColors.primary.withAlpha(160),
                                        blurRadius: 20,
                                      ),
                                    ],
                                  ),
                                  child: Icon(
                                    isPlaying ? Icons.pause_rounded : Icons.play_arrow_rounded,
                                    color: Colors.white,
                                    size: 40,
                                  ),
                                ),
                              ),
                            ),
                          ),

                        // Bottom Scrubber Bar Overlay
                        Positioned(
                          bottom: 0,
                          left: 0,
                          right: 0,
                          child: Container(
                            decoration: BoxDecoration(
                              gradient: LinearGradient(
                                begin: Alignment.bottomCenter,
                                end: Alignment.topCenter,
                                colors: [
                                  Colors.black.withAlpha(230),
                                  Colors.transparent,
                                ],
                              ),
                            ),
                            padding: const EdgeInsets.fromLTRB(28, 20, 28, 20),
                            child: SafeArea(
                              child: Column(
                                mainAxisSize: MainAxisSize.min,
                                children: [
                                  // Time Progress Row
                                  Row(
                                    children: [
                                      Text(
                                        _formatDuration(currentPos),
                                        style: const TextStyle(
                                          color: Colors.white,
                                          fontSize: 14,
                                          fontWeight: FontWeight.bold,
                                        ),
                                      ),
                                      const SizedBox(width: 14),
                                      Expanded(
                                        child: ClipRRect(
                                          borderRadius: BorderRadius.circular(4),
                                          child: LinearProgressIndicator(
                                            value: progressRatio,
                                            minHeight: 6,
                                            backgroundColor: Colors.white.withAlpha(50),
                                            valueColor: const AlwaysStoppedAnimation<Color>(AppColors.primary),
                                          ),
                                        ),
                                      ),
                                      const SizedBox(width: 14),
                                      Text(
                                        _formatDuration(totalDur),
                                        style: const TextStyle(
                                          color: Colors.white70,
                                          fontSize: 14,
                                          fontWeight: FontWeight.bold,
                                        ),
                                      ),
                                    ],
                                  ),
                                ],
                              ),
                            ),
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
    );
  }
}
