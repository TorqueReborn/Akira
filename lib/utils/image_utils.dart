/// Utility class for resolving image URLs and providing required network headers.
class ImageUtils {
  ImageUtils._();

  static const String baseHost = 'https://aln.youtube-anime.com';

  /// Standard HTTP headers required by the thumbnail CDN server.
  static const Map<String, String> imageHeaders = {
    'Referer': 'https://mkissa.to/',
    'User-Agent':
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/154.0.0.0 Safari/537.36 Edg/154.0.0.0',
    'sec-ch-ua-platform': '"Windows"',
    'sec-ch-ua':
        '"Chromium";v="154", "Microsoft Edge";v="154", "Not A(Brand";v="99"',
    'sec-ch-ua-mobile': '?0',
  };

  /// Resolves partial image paths by prepending the host if needed.
  static String? resolveUrl(String? url) {
    if (url == null) return null;
    final trimmed = url.trim();
    if (trimmed.isEmpty) return null;

    if (trimmed.startsWith('http://') || trimmed.startsWith('https://')) {
      return trimmed;
    }

    if (trimmed.startsWith('/')) {
      return '$baseHost$trimmed';
    }

    return '$baseHost/$trimmed';
  }
}
