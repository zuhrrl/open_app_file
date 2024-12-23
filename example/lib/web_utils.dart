import 'dart:html';

Future<String> createTextFile(String data) async {
  return Url.createObjectUrlFromBlob(Blob([data], 'plain/text', 'native'))
      .toString();
}
