import 'dart:async';
import 'package:open_app_file/src/common/open_result.dart';

import 'web.dart' as web;

class OpenAppFile {
  OpenAppFile._();

  /// Web [open] implementation generates a dynamic [AnchorElement]
  /// and tries to download the associated file. The downside is that
  /// it's not possible to have meaningful errors for requested files,
  /// so the result will always be [ResultType.done] regardless of the
  /// file availability and type.
  static Future<OpenResult> open(String filePath,
      {String? mimeType, String? uti}) async {
    web.open(filePath, filePath.split('/').last);
    return OpenResult(ResultType.done);
  }
}
