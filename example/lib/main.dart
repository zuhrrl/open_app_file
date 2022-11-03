import 'dart:io';
import 'dart:math';
import 'package:flutter/foundation.dart';

import 'package:open_app_file_example/native_utils.dart'
    if (dart.library.html) 'package:open_app_file_example/web_utils.dart';
import 'package:path_provider/path_provider.dart';
import 'package:http/http.dart';

import 'package:flutter/material.dart';
import 'dart:async';

import 'package:open_app_file/open_app_file.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  OpenResult? _openResult;
  final _fileNameController = TextEditingController(text: '/sdcard/test.txt');

  Future<String> _downloadFile(String url, String filename) async {
    if (kIsWeb) {
      // on the web there's no reason to download the file
      return url;
    }
    String dir = (await getTemporaryDirectory()).path;
    File targetFile = File('$dir/$filename');
    final httpClient = Client();
    var request = await httpClient.get(Uri.parse(url));
    var response = request.bodyBytes;
    await targetFile.writeAsBytes(response);
    return targetFile.path;
  }

  String _generateRandomString(int length) {
    var r = Random();
    return String.fromCharCodes(
        List.generate(length, (index) => r.nextInt(26) + 89));
  }

  Future<void> _openFile(String filePath) async {
    final result = await OpenAppFile.open(filePath);
    setState(() {
      _openResult = result;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: SingleChildScrollView(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 24.0),
                child: Text(
                  _openResult == null
                      ? 'Result: none'
                      : 'Result: ${_openResult?.type}\nMessage: ${_openResult?.message}',
                  textAlign: TextAlign.center,
                ),
              ),
              ElevatedButton(
                child: Text('Open generated file'),
                onPressed: () async {
                  _openFile(await createTextFile(_generateRandomString(30)));
                },
              ),
              SizedBox(
                height: 8.0,
              ),
              ElevatedButton(
                child: Text('Download and open image'),
                onPressed: () async {
                  _openFile(await _downloadFile(
                      'https://picsum.photos/200/300', 'test.jpg'));
                },
              ),
              SizedBox(
                height: 8.0,
              ),
              ElevatedButton(
                  child: Text('Download and open calendar event'),
                  onPressed: () async {
                    _openFile(await _downloadFile(
                        'https://raw.githubusercontent.com/yendoplan/open_app_file/master/example/files/test.ics',
                        'test.ics'));
                  }),
              SizedBox(
                height: 8.0,
              ),
              ElevatedButton(
                child: Text('Open non-existent file'),
                onPressed: () {
                  _openFile('asdf.qwert');
                },
              ),
              Padding(
                padding: const EdgeInsets.all(24.0),
                child: Text('To test external file access on Android:\n'
                    '1. Create or download a file to a restricted location (for example, "test.txt" in the root of external storage)\n'
                    '2. Set the file path in the field below and try to open it\n'
                    '3. Go to system settings and grant access to all files\n'
                    '4. Try to open the same file again\n'
                    'Use similar approach for media files: make an image like /sdcard/test.png and grant READ_EXTERNAL_STORAGE permission to the app'),
              ),
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 24.0),
                child: TextField(
                  controller: _fileNameController,
                ),
              ),
              ElevatedButton(
                child: Text('Open file'),
                onPressed: () async {
                  _openFile(_fileNameController.value.text);
                },
              ),
            ],
          ),
        ),
      ),
    );
  }
}
