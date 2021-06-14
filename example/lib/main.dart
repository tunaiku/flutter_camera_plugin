import 'package:flutter/material.dart';
import 'package:flutter_camera_plugin/external/flutter_camera_plugin_enums.dart';
import 'package:flutter_camera_plugin/flutter_camera_plugin.dart';
import 'package:flutter_camera_plugin/flutter_camera_plugin_descriptor.dart';
import 'package:path/path.dart';
import 'package:path_provider/path_provider.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  bool torchEnabled = false;

  @override
  void initState() {
    initializeCamera();
    super.initState();
  }

  initializeCamera() async {
    var cameras = await FlutterCameraPluginDescriptor.getAvailableCameras();
    print(cameras);
    _cameraController = FlutterCameraPluginController(cameras[0]);
    void callback() {
      print("Perform Action Here");
    }

    _cameraController.listenForPictureClick(callback);
    if (mounted) {
      setState(() {});
    }
  }

  FlutterCameraPluginController _cameraController;

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Container(
          child: SafeArea(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: <Widget>[
                Expanded(
                  flex: 1,
                  child: AspectRatio(
                    aspectRatio: 16 / 9,
                    child: _cameraController != null
                        ? FlutterCameraPluginPreview(
                            flutterCameraPluginController: _cameraController,
                          )
                        : Text("Loading"),
                  ),
                ),
                Align(
                  alignment: Alignment.bottomCenter,
                  child: Row(
                    children: <Widget>[
                      InkWell(
                        onTap: () async {
                          setState(() {
                            torchEnabled = !torchEnabled;
                          });

                          await _cameraController.setFlashMode(torchEnabled
                              ? CameraFlashMode.Torch
                              : CameraFlashMode.Auto);
                        },
                        child: Container(
                          height: 50,
                          child: Text(
                              torchEnabled ? "Disable Torch" : "Enable Torch"),
                        ),
                      ),
                      InkWell(
                        onTap: () async {
                          final path = join(
                              (await getTemporaryDirectory()).path,
                              '${DateTime.now()}.png');
                          await _cameraController.takePicture(path);
                        },
                        child: Container(
                          height: 50,
                          child: Text("Take Picture"),
                        ),
                      ),
                    ],
                  ),
                )
              ],
            ),
          ),
        ),
      ),
    );
  }
}
