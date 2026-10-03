import 'dart:async';
import 'dart:developer' as developer;
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:video_player/video_player.dart';
import 'package:wakelock_plus/wakelock_plus.dart';
import '../../../../theme/app_colors.dart';
import '../../../../utils/device_detector.dart';
import '../models/stream_source.dart';
import '../services/watch_history_manager.dart';

class PlayerScreen extends StatefulWidget {
  final String animeId;
  final String animeTitle;
  final String episodeNumber;
  final List<StreamSource> sources;
  final String? thumbnail;
  final int initialPositionMs;

  const PlayerScreen({
    super.key,
    required this.animeId,
    required this.animeTitle,
    required this.episodeNumber,
    required this.sources,
    this.thumbnail,
    this.initialPositionMs = 0,
  });

  @override
  State<PlayerScreen> createState() => _PlayerScreenState();
}

class _PlayerScreenState extends State<PlayerScreen> {
  VideoPlayerController? _controller;
  int _currentSourceIndex = 0;
  bool _isLoading = true;
  String? _statusMessage;
  String? _errorMessage;

  bool _showControls = true;
  Timer? _controlsTimer;

  bool _isDraggingSlider = false;
  double _dragSliderProgress = 0.0;

  bool _isFullScreen = true;
  bool _isTv = false;
  Timer? _progressSaveTimer;

  @override
  void initState() {
    super.initState();
    // Keep screen awake during playback
    WakelockPlus.enable();

    // Default to landscape orientation for premium immersive playback
    SystemChrome.setEnabledSystemUIMode(SystemUiMode.immersiveSticky);
    SystemChrome.setPreferredOrientations([
      DeviceOrientation.landscapeLeft,
      DeviceOrientation.landscapeRight,
    ]);

    // Filter for direct playable sources only; do not feed iframe URLs to VideoPlayerController
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

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    _isTv = DeviceDetector.isTv || DeviceDetector.isTvMode(context);
  }

    // Periodically save progress every 5 seconds
    _progressSaveTimer = Timer.periodic(const Duration(seconds: 5), (_) {
      _saveCurrentProgress();
    });
  }

  late List<StreamSource> _playableSources;

  void _startControlsTimer() {
    _controlsTimer?.cancel();
    _controlsTimer = Timer(const Duration(seconds: 4), () {
      if (mounted && _controller != null && _controller!.value.isPlaying && !_isDraggingSlider) {
        setState(() {
          _showControls = false;
        });
      }
    });
  }

  void _toggleControls() {
    setState(() {
      _showControls = !_showControls;
    });
    if (_showControls) {
      _startControlsTimer();
    } else {
      _controlsTimer?.cancel();
    }
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
      // Do not attempt to play non-direct / iframe URLs in VideoPlayerController
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
      developer.log('[PlayerScreen] Initializing VideoPlayer: source=${source.sourceName}, host=${uri.host}');

      // Playback headers matching original app in main.zip
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
      developer.log('[PlayerScreen] Playback started: ${source.sourceName}');
    } catch (e) {
      developer.log('[PlayerScreen] Playback error on ${source.sourceName}: $e');
      if (!mounted) return;
      // Auto-fallback to next direct source, preserving seek position
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

  void _toggleFullScreen() {
    setState(() {
      _isFullScreen = !_isFullScreen;
    });
    if (_isFullScreen || _isTv) {
      SystemChrome.setEnabledSystemUIMode(SystemUiMode.immersiveSticky);
      SystemChrome.setPreferredOrientations([
        DeviceOrientation.landscapeLeft,
        DeviceOrientation.landscapeRight,
      ]);
    } else {
      SystemChrome.setEnabledSystemUIMode(SystemUiMode.edgeToEdge);
      SystemChrome.setPreferredOrientations([
        DeviceOrientation.portraitUp,
      ]);
    }
  }

  @override
  void dispose() {
    _progressSaveTimer?.cancel();
    _controlsTimer?.cancel();
    _saveCurrentProgress();

    _controller?.removeListener(_videoPlayerListener);
    _controller?.dispose();

    WakelockPlus.disable();

    // Restore orientations and system bars
    SystemChrome.setEnabledSystemUIMode(SystemUiMode.edgeToEdge);
    if (_isTv || DeviceDetector.isTv) {
      SystemChrome.setPreferredOrientations([
        DeviceOrientation.landscapeLeft,
        DeviceOrientation.landscapeRight,
      ]);
    } else {
      SystemChrome.setPreferredOrientations([
        DeviceOrientation.portraitUp,
        DeviceOrientation.portraitDown,
        DeviceOrientation.landscapeLeft,
        DeviceOrientation.landscapeRight,
      ]);
    }
    super.dispose();
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

  @override
  Widget build(BuildContext context) {
    final controller = _controller;
    final isInitialized = controller != null && controller.value.isInitialized;
    final isPlaying = isInitialized && controller.value.isPlaying;

    final currentPos = isInitialized ? controller.value.position : Duration.zero;
    final totalDur = isInitialized ? controller.value.duration : Duration.zero;

    return PopScope(
      canPop: true,
      onPopInvokedWithResult: (didPop, result) {
        _saveCurrentProgress();
      },
      child: Scaffold(
        backgroundColor: Colors.black,
        body: GestureDetector(
          behavior: HitTestBehavior.opaque,
          onTap: _toggleControls,
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

              // 2. Loading State / Status Message
              if (_isLoading)
                Center(
                  child: Container(
                    padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 14),
                    decoration: BoxDecoration(
                      color: Colors.black.withAlpha(200),
                      borderRadius: BorderRadius.circular(14),
                      border: Border.all(color: Colors.white.withAlpha(30)),
                    ),
                    child: Column(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        const SizedBox(
                          width: 36,
                          height: 36,
                          child: CircularProgressIndicator(
                            strokeWidth: 3,
                            color: AppColors.primary,
                          ),
                        ),
                        if (_statusMessage != null) ...[
                          const SizedBox(height: 12),
                          Text(
                            _statusMessage!,
                            style: const TextStyle(
                              color: Colors.white,
                              fontSize: 13,
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                        ],
                      ],
                    ),
                  ),
                ),

              // 3. Error Overlay
              if (_errorMessage != null)
                Center(
                  child: Container(
                    margin: const EdgeInsets.symmetric(horizontal: 32),
                    padding: const EdgeInsets.all(24),
                    decoration: BoxDecoration(
                      color: const Color(0xFF1E1015).withAlpha(230),
                      borderRadius: BorderRadius.circular(18),
                      border: Border.all(color: AppColors.primary.withAlpha(100), width: 1.5),
                    ),
                    child: Column(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        const Icon(
                          Icons.error_outline_rounded,
                          color: AppColors.primary,
                          size: 46,
                        ),
                        const SizedBox(height: 12),
                        const Text(
                          'Playback Error',
                          style: TextStyle(
                            color: Colors.white,
                            fontSize: 18,
                            fontWeight: FontWeight.w800,
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
                        const SizedBox(height: 18),
                        if (_playableSources.isNotEmpty)
                          Wrap(
                            spacing: 8,
                            runSpacing: 8,
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
                                    fontSize: 12,
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

              // 4. Player Controls Overlay
              AnimatedOpacity(
                opacity: _showControls ? 1.0 : 0.0,
                duration: const Duration(milliseconds: 250),
                child: IgnorePointer(
                  ignoring: !_showControls,
                  child: Stack(
                    children: [
                      // Top Bar Gradient
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
                                Colors.black.withAlpha(220),
                                Colors.transparent,
                              ],
                            ),
                          ),
                          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                          child: SafeArea(
                            child: Row(
                              children: [
                                IconButton(
                                  icon: const Icon(
                                    Icons.arrow_back_rounded,
                                    color: Colors.white,
                                    size: 26,
                                  ),
                                  onPressed: () {
                                    _saveCurrentProgress();
                                    Navigator.of(context).pop();
                                  },
                                ),
                                const SizedBox(width: 8),
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
                                          fontSize: 15,
                                          fontWeight: FontWeight.w700,
                                        ),
                                      ),
                                      Text(
                                        'Episode ${widget.episodeNumber}',
                                        style: TextStyle(
                                          color: Colors.white.withAlpha(180),
                                          fontSize: 12,
                                          fontWeight: FontWeight.w500,
                                        ),
                                      ),
                                    ],
                                  ),
                                ),

                                // Source Selection Menu
                                if (_playableSources.isNotEmpty)
                                  PopupMenuButton<int>(
                                    initialValue: _currentSourceIndex,
                                    onSelected: (index) {
                                      if (index != _currentSourceIndex) {
                                        final pos = _controller?.value.position.inMilliseconds ?? 0;
                                        _initSource(index, pos);
                                      }
                                    },
                                    color: const Color(0xFF1E1B2E),
                                    shape: RoundedRectangleBorder(
                                      borderRadius: BorderRadius.circular(12),
                                    ),
                                    itemBuilder: (context) {
                                      return _playableSources.asMap().entries.map((entry) {
                                        final idx = entry.key;
                                        final src = entry.value;
                                        return PopupMenuItem<int>(
                                          value: idx,
                                          child: Row(
                                            children: [
                                              if (idx == _currentSourceIndex)
                                                const Icon(
                                                  Icons.check_rounded,
                                                  color: AppColors.primary,
                                                  size: 16,
                                                )
                                              else
                                                const SizedBox(width: 16),
                                              const SizedBox(width: 8),
                                              Text(
                                                src.sourceName,
                                                style: TextStyle(
                                                  color: idx == _currentSourceIndex
                                                      ? AppColors.primary
                                                      : Colors.white,
                                                  fontWeight: idx == _currentSourceIndex
                                                      ? FontWeight.bold
                                                      : FontWeight.normal,
                                                  fontSize: 13,
                                                ),
                                              ),
                                              if (src.isHls) ...[
                                                const SizedBox(width: 6),
                                                Container(
                                                  padding: const EdgeInsets.symmetric(
                                                      horizontal: 5, vertical: 2),
                                                  decoration: BoxDecoration(
                                                    color: AppColors.primary.withAlpha(40),
                                                    borderRadius: BorderRadius.circular(4),
                                                  ),
                                                  child: const Text(
                                                    'HLS',
                                                    style: TextStyle(
                                                      color: AppColors.primary,
                                                      fontSize: 9,
                                                      fontWeight: FontWeight.bold,
                                                    ),
                                                  ),
                                                ),
                                              ],
                                            ],
                                          ),
                                        );
                                      }).toList();
                                    },
                                    child: Container(
                                      padding: const EdgeInsets.symmetric(
                                          horizontal: 12, vertical: 6),
                                      decoration: BoxDecoration(
                                        color: Colors.white.withAlpha(40),
                                        borderRadius: BorderRadius.circular(20),
                                        border: Border.all(
                                            color: Colors.white.withAlpha(50), width: 1),
                                      ),
                                      child: Row(
                                        mainAxisSize: MainAxisSize.min,
                                        children: [
                                          const Icon(
                                            Icons.tune_rounded,
                                            color: Colors.white,
                                            size: 14,
                                          ),
                                          const SizedBox(width: 6),
                                          Text(
                                            _playableSources.isNotEmpty && _currentSourceIndex < _playableSources.length
                                                ? _playableSources[_currentSourceIndex].sourceName
                                                : 'Source',
                                            style: const TextStyle(
                                              color: Colors.white,
                                              fontSize: 12,
                                              fontWeight: FontWeight.w600,
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

                      // Center Playback Buttons (Seek backward 10s, Play/Pause, Seek forward 10s)
                      Center(
                        child: Row(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            // Replay 10s
                            IconButton(
                              iconSize: 42,
                              icon: const Icon(
                                Icons.replay_10_rounded,
                                color: Colors.white,
                              ),
                              onPressed: () {
                                if (controller != null && isInitialized) {
                                  final newPos = controller.value.position - const Duration(seconds: 10);
                                  controller.seekTo(newPos > Duration.zero ? newPos : Duration.zero);
                                  _startControlsTimer();
                                }
                              },
                            ),
                            const SizedBox(width: 36),

                            // Main Play / Pause Button
                            GestureDetector(
                              onTap: () {
                                if (controller != null && isInitialized) {
                                  if (isPlaying) {
                                    controller.pause();
                                  } else {
                                    controller.play();
                                  }
                                  _startControlsTimer();
                                }
                              },
                              child: Container(
                                width: 68,
                                height: 68,
                                decoration: BoxDecoration(
                                  shape: BoxShape.circle,
                                  color: AppColors.primary,
                                  boxShadow: [
                                    BoxShadow(
                                      color: AppColors.primary.withAlpha(120),
                                      blurRadius: 20,
                                      offset: const Offset(0, 4),
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
                            const SizedBox(width: 36),

                            // Forward 10s
                            IconButton(
                              iconSize: 42,
                              icon: const Icon(
                                Icons.forward_10_rounded,
                                color: Colors.white,
                              ),
                              onPressed: () {
                                if (controller != null && isInitialized) {
                                  final newPos = controller.value.position + const Duration(seconds: 10);
                                  final maxPos = controller.value.duration;
                                  controller.seekTo(newPos < maxPos ? newPos : maxPos);
                                  _startControlsTimer();
                                }
                              },
                            ),
                          ],
                        ),
                      ),

                      // Bottom Scrub Bar & Timer
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
                          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                          child: SafeArea(
                            child: Column(
                              mainAxisSize: MainAxisSize.min,
                              children: [
                                Row(
                                  children: [
                                    Text(
                                      _formatDuration(currentPos),
                                      style: const TextStyle(
                                        color: Colors.white,
                                        fontSize: 12,
                                        fontFamily: 'monospace',
                                        fontWeight: FontWeight.w600,
                                      ),
                                    ),
                                    Expanded(
                                      child: SliderTheme(
                                        data: SliderTheme.of(context).copyWith(
                                          thumbColor: AppColors.primary,
                                          activeTrackColor: AppColors.primary,
                                          inactiveTrackColor: Colors.white.withAlpha(60),
                                          trackHeight: 3.5,
                                          thumbShape: const RoundSliderThumbShape(
                                            enabledThumbRadius: 6.5,
                                          ),
                                          overlayShape: const RoundSliderOverlayShape(
                                            overlayRadius: 14,
                                          ),
                                        ),
                                        child: Slider(
                                          value: _isDraggingSlider
                                              ? _dragSliderProgress
                                              : (totalDur.inMilliseconds > 0
                                                  ? (currentPos.inMilliseconds /
                                                          totalDur.inMilliseconds)
                                                      .clamp(0.0, 1.0)
                                                  : 0.0),
                                          onChanged: (val) {
                                            setState(() {
                                              _isDraggingSlider = true;
                                              _dragSliderProgress = val;
                                            });
                                            _startControlsTimer();
                                          },
                                          onChangeEnd: (val) {
                                            if (controller != null && isInitialized) {
                                              final targetMs = (val * totalDur.inMilliseconds).toInt();
                                              controller.seekTo(Duration(milliseconds: targetMs));
                                            }
                                            setState(() {
                                              _isDraggingSlider = false;
                                            });
                                            _startControlsTimer();
                                          },
                                        ),
                                      ),
                                    ),
                                    Text(
                                      _formatDuration(totalDur),
                                      style: const TextStyle(
                                        color: Colors.white,
                                        fontSize: 12,
                                        fontFamily: 'monospace',
                                        fontWeight: FontWeight.w600,
                                      ),
                                    ),
                                    const SizedBox(width: 8),
                                    IconButton(
                                      icon: Icon(
                                        _isFullScreen
                                            ? Icons.fullscreen_exit_rounded
                                            : Icons.fullscreen_rounded,
                                        color: Colors.white,
                                        size: 26,
                                      ),
                                      onPressed: _toggleFullScreen,
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
    );
  }
}
