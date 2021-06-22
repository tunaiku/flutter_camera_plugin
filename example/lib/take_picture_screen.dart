import 'package:flutter/material.dart';
import 'package:flutter_camera_plugin/flutter_camera_plugin.dart';
import 'package:flutter_camera_plugin_example/result_picture_screen.dart';
import 'package:path/path.dart';
import 'package:path_provider/path_provider.dart';

class TakePictureScreen extends StatefulWidget {
  const TakePictureScreen({Key? key}) : super(key: key);

  @override
  _TakePictureScreenState createState() => _TakePictureScreenState();
}

class _TakePictureScreenState extends State<TakePictureScreen> {
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

    _cameraController!.listenForPictureClick(callback);
    if (mounted) {
      setState(() {});
    }
  }

  FlutterCameraPluginController? _cameraController;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
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

                        await _cameraController!.setFlashMode(torchEnabled
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
                        final imagePath = join(
                            (await getTemporaryDirectory()).path,
                            '${DateTime.now()}.png');
                        await _cameraController!.takePicture(imagePath);
                        if (imagePath.isNotEmpty) {
                          Navigator.push(
                            context,
                            MaterialPageRoute(
                              builder: (BuildContext context) =>
                                  ResultPictureScreen(
                                imagePath: imagePath,
                              ),
                            ),
                          );
                        }
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
    );
  }
}
