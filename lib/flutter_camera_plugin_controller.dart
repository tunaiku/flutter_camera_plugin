import 'package:flutter/services.dart';
import 'package:flutter_camera_plugin/external/flutter_camera_plugin_constants.dart';
import 'package:flutter_camera_plugin/external/flutter_camera_plugin_enums.dart';
import 'package:flutter_camera_plugin/flutter_camera_plugin_descriptor.dart';

class FlutterCameraPluginController {
  FlutterCameraPluginDescriptor? cameraDescriptor;
  bool? saveToFile;

  FlutterCameraPluginController(this.cameraDescriptor, {this.saveToFile = true});

  MethodChannel _channel = MethodChannel('${FlutterCameraPluginConstants.channelId}_0');

  listenForPictureClick(var callback) {
    try {
      Future<dynamic> handleMethodCall(MethodCall call) async {
        if (call.method == "pictureClicked") {
          callback();
        }
      }

      _channel.setMethodCallHandler(handleMethodCall);
    } catch (e) {
      print(e);
    }
  }

  Future<void> setFlashMode(CameraFlashMode mode) async {
    if (mode == CameraFlashMode.On)
      return _channel.invokeMethod(FlutterCameraPluginConstants.setFlashMethodName, {"data": "On"});
    if (mode == CameraFlashMode.Off)
      return _channel
          .invokeMethod(FlutterCameraPluginConstants.setFlashMethodName, {"data": "Off"});
    if (mode == CameraFlashMode.Auto)
      return _channel
          .invokeMethod(FlutterCameraPluginConstants.setFlashMethodName, {"data": "Auto"});
    if (mode == CameraFlashMode.Torch)
      return _channel
          .invokeMethod(FlutterCameraPluginConstants.setFlashMethodName, {"data": "Torch"});
  }

  Future<void> initialize() async {
    print("before Initializing camera here");

    if (cameraDescriptor == null) return;
    print("Initializing camera here");
    _channel.invokeMethod("initializeCamera", {
      "lensFacing": getStringFromCameraFacing(cameraDescriptor!.lensFacing),
      "saveToFile": saveToFile
    });
  }

  Future takePicture(String path) async {
    var image = await _channel
        .invokeMethod(FlutterCameraPluginConstants.captureImageMethodName, {"data": path});
    return image;
  }

  Future<void> setAspectRatio(int num, int denom) {
    return _channel.invokeMethod(
        FlutterCameraPluginConstants.setPreviewAspectRatioMethodName, {"num": num, "denom": denom});
  }

  Future enableClickSound(bool val) {
    return _channel
        .invokeMethod(FlutterCameraPluginConstants.playSoundOnClickMethodName, {"data": val});
  }
}
