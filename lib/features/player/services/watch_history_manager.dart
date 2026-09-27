import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';

class AnimeProgress {
  final String animeId;
  final String animeTitle;
  final String episodeNumber;
  final int seekPositionMs;
  final int totalDurationMs;
  final String? thumbnail;
  final int lastWatchedTimestamp;

  const AnimeProgress({
    required this.animeId,
    required this.animeTitle,
    required this.episodeNumber,
    required this.seekPositionMs,
    required this.totalDurationMs,
    this.thumbnail,
    required this.lastWatchedTimestamp,
  });

  Map<String, dynamic> toJson() => {
        'animeId': animeId,
        'animeTitle': animeTitle,
        'episodeNumber': episodeNumber,
        'seekPositionMs': seekPositionMs,
        'totalDurationMs': totalDurationMs,
        'thumbnail': thumbnail,
        'lastWatchedTimestamp': lastWatchedTimestamp,
      };

  factory AnimeProgress.fromJson(Map<String, dynamic> json) => AnimeProgress(
        animeId: json['animeId']?.toString() ?? '',
        animeTitle: json['animeTitle']?.toString() ?? '',
        episodeNumber: json['episodeNumber']?.toString() ?? '',
        seekPositionMs: (json['seekPositionMs'] as num?)?.toInt() ?? 0,
        totalDurationMs: (json['totalDurationMs'] as num?)?.toInt() ?? 0,
        thumbnail: json['thumbnail']?.toString(),
        lastWatchedTimestamp: (json['lastWatchedTimestamp'] as num?)?.toInt() ??
            DateTime.now().millisecondsSinceEpoch,
      );
}

class WatchHistoryManager {
  static const String _keyHistory = 'anime_watch_progress_list';
  static SharedPreferences? _prefs;

  static Future<void> init() async {
    _prefs ??= await SharedPreferences.getInstance();
  }

  static Future<List<AnimeProgress>> getProgressList() async {
    final prefs = _prefs ?? await SharedPreferences.getInstance();
    final raw = prefs.getString(_keyHistory);
    if (raw == null || raw.isEmpty) return [];

    try {
      final dynamic decoded = jsonDecode(raw);
      if (decoded is List) {
        return decoded
            .whereType<Map<String, dynamic>>()
            .map((item) => AnimeProgress.fromJson(item))
            .toList();
      }
    } catch (_) {}
    return [];
  }

  static Future<AnimeProgress?> getProgress(String animeId) async {
    final list = await getProgressList();
    try {
      return list.firstWhere((p) => p.animeId == animeId);
    } catch (_) {
      return null;
    }
  }

  static Future<void> saveProgress({
    required String animeId,
    required String animeTitle,
    required String episodeNumber,
    required int seekPositionMs,
    required int totalDurationMs,
    String? thumbnail,
  }) async {
    if (animeId.trim().isEmpty) return;
    final prefs = _prefs ?? await SharedPreferences.getInstance();
    final list = await getProgressList();

    final existingIndex = list.indexWhere((p) => p.animeId == animeId);
    String? thumb = thumbnail;
    if (existingIndex >= 0 && (thumb == null || thumb.isEmpty)) {
      thumb = list[existingIndex].thumbnail;
    }

    final newProgress = AnimeProgress(
      animeId: animeId,
      animeTitle: animeTitle,
      episodeNumber: episodeNumber,
      seekPositionMs: seekPositionMs,
      totalDurationMs: totalDurationMs,
      thumbnail: thumb,
      lastWatchedTimestamp: DateTime.now().millisecondsSinceEpoch,
    );

    if (existingIndex >= 0) {
      list.removeAt(existingIndex);
    }
    list.insert(0, newProgress);

    // Limit to last 50 entries
    if (list.length > 50) {
      list.removeRange(50, list.length);
    }

    final jsonStr = jsonEncode(list.map((e) => e.toJson()).toList());
    await prefs.setString(_keyHistory, jsonStr);
  }
}
