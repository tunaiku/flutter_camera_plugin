import 'package:flutter_camera_plugin/external/flutter_camera_plugin_constants.dart';
import 'package:flutter_camera_plugin/external/flutter_camera_plugin_enums.dart';
import 'package:flutter_camera_plugin/flutter_camera_plugin_method_channel.dart';

class FlutterCameraPluginDescriptor {
  FlutterCameraPluginDescriptor(
      {this.name, this.lensFacing, this.sensorOrientation});

  final String name;
  final CameraFacing lensFacing;

  /// Clockwise angle through which the output image needs to be rotated to be upright on the device screen in its native orientation.
  final int sensorOrientation;

  static getAvailableCameras() async {
    final List<Map<dynamic, dynamic>> cameras =
        await FlutterCameraPluginMethodChannel.channel
            .invokeListMethod<Map<dynamic, dynamic>>(
                FlutterCameraPluginConstants.getAvailableCamerasMethodName);
    return cameras.map((Map<dynamic, dynamic> camera) {
      return FlutterCameraPluginDescriptor(
        name: camera['name'],
        lensFacing: getCameraFacingFromString(camera['lensFacing']),
        sensorOrientation: camera['sensorOrientation'],
      );
    }).toList();
  }

  @override
  String toString() {
    return '$runtimeType($name, $lensFacing, $sensorOrientation)';
  }
}
