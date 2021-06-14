import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_camera_plugin/external/flutter_camera_plugin_constants.dart';
import 'package:flutter_camera_plugin/flutter_camera_plugin_controller.dart';
import 'package:flutter_camera_plugin/flutter_camera_plugin_method_channel.dart';
export 'package:flutter_camera_plugin/external/flutter_camera_plugin_constants.dart';
export 'package:flutter_camera_plugin/flutter_camera_plugin_controller.dart';
export 'package:flutter_camera_plugin/flutter_camera_plugin_method_channel.dart';


class FlutterCameraPlugin {
  static final MethodChannel _channel =
      FlutterCameraPluginMethodChannel.channel;

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}

//typedef void CameraXCreatedCallback(CameraXController controller);

class FlutterCameraPluginPreview extends StatefulWidget {
  const FlutterCameraPluginPreview({
    Key key,
    this.flutterCameraPluginController,
  }) : super(key: key);

  final FlutterCameraPluginController flutterCameraPluginController;

  @override
  _FlutterCameraPluginPreviewState createState() =>
      _FlutterCameraPluginPreviewState();
}

class _FlutterCameraPluginPreviewState
    extends State<FlutterCameraPluginPreview> {
  @override
  Widget build(BuildContext context) {
    return AndroidView(
      viewType: FlutterCameraPluginConstants.previewViewType,
      onPlatformViewCreated: _onPlatformViewCreated,
    );
  }

  void _onPlatformViewCreated(int id) {
    if (widget.flutterCameraPluginController == null) {
      return;
    }
    widget.flutterCameraPluginController.initialize();
  }
}
