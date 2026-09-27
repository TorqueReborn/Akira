import 'dart:convert';
import 'dart:developer' as developer;
import '../models/stream_source.dart';

class StreamParser {
  /// Known iframe / web embed domains that cannot be played directly by native VideoPlayer
  static final List<String> knownIframeHosts = [
    'filemoon',
    'ok.ru',
    'odnoklassniki',
    'vidguard',
    'mp4upload.com',
    'streamwish',
    'doodstream',
    'dood.',
    'voe.sx',
    'streamtape',
    'mixdrop',
  ];

  /// Parses decrypted JSON string into a prioritized list of [StreamSource] instances.
  /// Recognizes direct playable video sources (MP4, HLS, type == "player") vs iframe embeds.
  static List<StreamSource> parseStreams(String decryptedJson) {
    final trimmed = decryptedJson.trim();
    final streams = <StreamSource>[];

    try {
      if (trimmed.startsWith('[')) {
        final dynamic decoded = jsonDecode(trimmed);
        if (decoded is List) {
          for (final item in decoded) {
            if (item is Map<String, dynamic>) {
              streams.addAll(_parseStreamObject(item));
            }
          }
        }
      } else if (trimmed.startsWith('{')) {
        final dynamic decoded = jsonDecode(trimmed);
        if (decoded is Map<String, dynamic>) {
          final dynamic array = (decoded['episode'] is Map<String, dynamic>
                  ? decoded['episode']['sourceUrls']
                  : null) ??
              decoded['sourceUrls'] ??
              decoded['sources'] ??
              decoded['links'] ??
              decoded['data'];

          if (array is List) {
            for (final item in array) {
              if (item is Map<String, dynamic>) {
                streams.addAll(_parseStreamObject(item));
              }
            }
          } else {
            streams.addAll(_parseStreamObject(decoded));
          }
        }
      }
    } catch (e) {
      developer.log('[StreamParser] Error decoding JSON: $e', name: 'StreamParser');
      streams.addAll(_extractUrlsFallback(trimmed));
    }

    if (streams.isEmpty) {
      streams.addAll(_extractUrlsFallback(trimmed));
    }

    // Deduplicate by URL
    final seenUrls = <String>{};
    final unique = <StreamSource>[];
    for (final s in streams) {
      if (!seenUrls.contains(s.url)) {
        seenUrls.add(s.url);
        unique.add(s);
      }
    }

    // Sort priority order:
    // 1. Direct playable sources (type == "player", .mp4, .m3u8) before iframe sources
    // 2. Higher priority score first
    unique.sort((a, b) {
      if (a.isDirect != b.isDirect) {
        return a.isDirect ? -1 : 1;
      }
      return b.priority.compareTo(a.priority);
    });

    developer.log('[StreamParser] Parsed ${unique.length} streams (${unique.where((s) => s.isDirect).length} direct playable)', name: 'StreamParser');
    return unique;
  }

  static List<StreamSource> _parseStreamObject(Map<String, dynamic> obj) {
    final result = <StreamSource>[];

    final downloadsObj = obj['downloads'] as Map<String, dynamic>?;
    final downloadUrlRaw = downloadsObj?['downloadUrl']?.toString();
    final downloadName = downloadsObj?['sourceName']?.toString() ?? obj['sourceName']?.toString();

    final rawUrlCandidate = (obj['sourceUrl']?.toString().isNotEmpty == true
            ? obj['sourceUrl']?.toString()
            : obj['downloadUrl']?.toString().isNotEmpty == true
                ? obj['downloadUrl']?.toString()
                : obj['url']?.toString().isNotEmpty == true
                    ? obj['url']?.toString()
                    : obj['link']?.toString())
        ?.trim();

    final name = (obj['sourceName']?.toString().isNotEmpty == true
            ? obj['sourceName']?.toString()
            : obj['name']?.toString().isNotEmpty == true
                ? obj['name']?.toString()
                : 'Server') ??
        'Server';

    double rawPriority = 0.0;
    if (obj['priority'] != null) {
      if (obj['priority'] is num) {
        rawPriority = (obj['priority'] as num).toDouble();
      } else {
        rawPriority = double.tryParse(obj['priority'].toString()) ?? 0.0;
      }
    }

    final type = obj['type']?.toString().trim();
    final ext = (obj['fileExtenstion']?.toString() ?? obj['fileExtension']?.toString() ?? '').trim().toLowerCase();

    // 15. Normalize URL: //example.com/... -> https://example.com/...
    String? normalizeUrl(String? urlStr) {
      if (urlStr == null) return null;
      var u = urlStr.replaceAll(r'\/', '/').trim();
      if (u.startsWith('//')) {
        u = 'https:$u';
      }
      return u;
    }

    final rawUrl = normalizeUrl(rawUrlCandidate);
    final downloadUrl = normalizeUrl(downloadUrlRaw);

    bool checkIsDirect(String url, String? itemType, String itemExt) {
      final lower = url.toLowerCase();
      final uri = Uri.tryParse(url);
      final host = uri?.host.toLowerCase() ?? '';

      // Check if known iframe host
      for (final iframeHost in knownIframeHosts) {
        if (host.contains(iframeHost)) {
          // Unless it's an explicit direct player stream endpoint
          if (itemType != 'player' && !lower.endsWith('.mp4') && !lower.endsWith('.m3u8')) {
            return false;
          }
        }
      }

      if (itemType == 'player') return true;
      if (itemExt == 'mp4' || itemExt == 'm3u8') return true;
      if (lower.contains('.mp4') || lower.contains('.m3u8')) return true;

      // If type is iframe or embed, it's not direct
      if (itemType == 'iframe' || itemType == 'embed') return false;

      // Direct player server names (e.g., Yt-mp4)
      if (name.toLowerCase().contains('yt-mp4') || name.toLowerCase().contains('direct')) {
        return true;
      }

      return false;
    }

    if (rawUrl != null && rawUrl.isNotEmpty && (rawUrl.startsWith('http://') || rawUrl.startsWith('https://'))) {
      final isHls = rawUrl.toLowerCase().contains('.m3u8') || ext == 'm3u8';
      final isDirect = checkIsDirect(rawUrl, type, ext);

      // Boost direct playable sources so they are prioritized over iframes
      final effectivePriority = isDirect ? (rawPriority + 10.0) : rawPriority;

      result.add(
        StreamSource(
          sourceName: name,
          url: rawUrl,
          priority: effectivePriority,
          isHls: isHls,
          type: type,
          fileExtension: ext.isNotEmpty ? ext : (isHls ? 'm3u8' : (rawUrl.toLowerCase().contains('.mp4') ? 'mp4' : null)),
          isDirect: isDirect,
        ),
      );
    }

    if (downloadUrl != null &&
        downloadUrl.isNotEmpty &&
        (downloadUrl.startsWith('http://') || downloadUrl.startsWith('https://')) &&
        downloadUrl != rawUrl) {
      final isHls = downloadUrl.toLowerCase().contains('.m3u8');
      final isDirect = checkIsDirect(downloadUrl, 'player', 'mp4');

      result.add(
        StreamSource(
          sourceName: '${downloadName ?? "Download"} (Direct)',
          url: downloadUrl,
          priority: rawPriority + 5.0,
          isHls: isHls,
          type: 'player',
          fileExtension: isHls ? 'm3u8' : 'mp4',
          isDirect: isDirect,
        ),
      );
    }

    return result;
  }

  static List<StreamSource> _extractUrlsFallback(String text) {
    final regex = RegExp(r'https?://[^\s"<>\x27]+');
    final matches = regex.allMatches(text);
    final result = <StreamSource>[];
    int index = 1;
    for (final m in matches) {
      final url = m.group(0)!.replaceAll(r'\/', '/');
      final isHls = url.toLowerCase().contains('.m3u8');
      final isMp4 = url.toLowerCase().contains('.mp4');
      final isDirect = isHls || isMp4;
      result.add(
        StreamSource(
          sourceName: 'Source $index',
          url: url,
          priority: 0.0,
          isHls: isHls,
          fileExtension: isHls ? 'm3u8' : (isMp4 ? 'mp4' : null),
          isDirect: isDirect,
        ),
      );
      index++;
    }
    return result;
  }
}
