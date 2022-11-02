import 'dart:async';
import 'package:open_app_file/src/common/open_result.dart';

import 'web.dart' as web;

class OpenAppFile {
  OpenAppFile._();

  static Future<OpenResult> open(String? filePath,
      {String? mimeType, String? uti}) async {
    final error = await web.open("file://$filePath");
    return OpenResult(error == null ? ResultType.done : ResultType.error,
        message:
            error == null ? "done" : "Error opening file $filePath: $error");
  }
}
