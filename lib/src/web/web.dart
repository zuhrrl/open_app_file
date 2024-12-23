// ignore: avoid_web_libraries_in_flutter
import 'dart:html';

void open(String uri, String filename) {
  AnchorElement(
    href: uri,
  )
    ..setAttribute('download', filename)
    ..setAttribute('target', '_blank')
    ..click();
}
