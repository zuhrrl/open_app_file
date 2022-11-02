import 'dart:async';

// ignore: avoid_web_libraries_in_flutter
import 'dart:html';

Future<dynamic> open(String uri) async {
  try {
    await window.resolveLocalFileSystemUrl(uri);
    return null;
  } catch (e) {
    return e;
  }
}
