class StreamSource {
  final String sourceName;
  final String url;
  final double priority;
  final bool isHls;
  final String? type;
  final String? fileExtension;
  final bool isDirect;

  const StreamSource({
    required this.sourceName,
    required this.url,
    this.priority = 0.0,
    this.isHls = false,
    this.type,
    this.fileExtension,
    this.isDirect = true,
  });

  @override
  String toString() =>
      'StreamSource(name: $sourceName, direct: $isDirect, isHls: $isHls, type: $type, ext: $fileExtension, priority: $priority, url: $url)';
}
