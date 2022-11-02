import 'dart:io';
import 'dart:math';
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

  Future<String> _downloadFile(String url, String filename) async {
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

  Future<String> _generateRandomTextFile() async {
    final directory = await getApplicationDocumentsDirectory();
    final File file = File('${directory.path}/test.txt');
    await file.writeAsString(_generateRandomString(30));
    return file.path;
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
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Padding(
                padding: const EdgeInsets.all(8.0),
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
                  _openFile(await _generateRandomTextFile());
                },
              ),
              ElevatedButton(
                child: Text('Download and open image'),
                onPressed: () async {
                  _openFile(await _downloadFile(
                      'https://picsum.photos/200/300', 'test.jpg'));
                },
              ),
              ElevatedButton(
                child: Text('Download and open calendar event'),
                onPressed: () async {
                  _openFile(await _downloadFile(
                      'https://raw.githubusercontent.com/yendoplan/open_app_file/master/example/files/test.ics',
                      'test.ics'));
                },
              ),
              ElevatedButton(
                child: Text('Open non-existent file'),
                onPressed: () async {
                  _openFile('/sdcard/asdf.qwert');
                },
              ),
            ],
          ),
        ),
      ),
    );
  }
}
