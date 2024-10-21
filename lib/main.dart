import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:logger/logger.dart';

void main() {
  runApp(const CameraApp());
}

class CameraApp extends StatefulWidget {
  const CameraApp({super.key});

  @override
  CameraAppState createState() => CameraAppState();
}

class CameraAppState extends State<CameraApp> {
  static const platform = MethodChannel('com.example.home_assignment/camera');
  String? _capturedImagePath;
  int? viewId;
  final Logger logger = Logger();

  @override
  void initState() {
    super.initState();
  }

  Future<void> _openCamera(int? viewId) async {
    try {
      if (viewId != null) {
        await platform.invokeMethod('openCamera', {'viewId': viewId}); //android
      }
    } on PlatformException catch (e) {
      logger.e("Failed to open camera: ${e.message}");
    }
  }

  Future<void> toggleFlash() async {
    try {
      await platform.invokeMethod('toggleFlash'); //android
    } on PlatformException catch (e) {
      logger.e("Failed to toggle flash: ${e.message}");
    }
  }

  Future<void> _takePicture() async {
    try {
      final String imagePath = await platform.invokeMethod('takePicture'); //android
      setState(() {
        _capturedImagePath = imagePath;
      });
    } on PlatformException catch (e) {
      logger.d("Failed to take picture: ${e.message}");
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Camera'),
        ),
        body: Column(
          children: [
            Expanded(
              child: AndroidView(
                viewType: 'CameraXPreview',
                onPlatformViewCreated: (int id) {
                  viewId = id; // Save the ID when the view is created
                  _openCamera(viewId);
                },
              ),
            ),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                Center(
                  child: _capturedImagePath == null
                      ? Container(width: 50,)
                      : Image.file(
                    File(_capturedImagePath!),
                    width: 50,
                    height: 50,
                    fit: BoxFit.cover,
                  ),
                ),
                ElevatedButton(
                  onPressed: _takePicture,
                  child: const Text('Take Picture'),
                ),
                ElevatedButton(
                  onPressed: toggleFlash,
                  child: const Text('Torchlight'),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
