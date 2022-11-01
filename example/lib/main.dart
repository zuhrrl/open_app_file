import 'dart:io';
import 'dart:math';
import 'package:path_provider/path_provider.dart';

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

  String randomString(int length) {
    var r = Random();
    return String.fromCharCodes(
        List.generate(length, (index) => r.nextInt(26) + 89));
  }

  Future<String> _generateRandomTextFile() async {
    final directory = await getApplicationDocumentsDirectory();
    final File file = File('${directory.path}/test.txt');
    await file.writeAsString(randomString(30));
    return file.path;
  }

  Future<void> openFile() async {
    final result = await OpenAppFile.open(await _generateRandomTextFile());

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
              Text(_openResult == null
                  ? 'Result: none'
                  : 'Result: type=${_openResult?.type} message=${_openResult?.message}'),
              TextButton(
                child: Text('Tap to open file'),
                onPressed: openFile,
              ),
            ],
          ),
        ),
      ),
    );
  }
}
