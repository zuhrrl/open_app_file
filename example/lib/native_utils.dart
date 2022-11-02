import 'dart:io';

import 'package:path_provider/path_provider.dart';

Future<String> createTextFile(String data) async {
  final directory = await getApplicationDocumentsDirectory();
  final File file = File('${directory.path}/test.txt');
  await file.writeAsString(data);
  return file.path;
}
