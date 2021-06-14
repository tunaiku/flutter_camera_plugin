import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:flutter_camera_plugin/external/flutter_camera_plugin_constants.dart';

@protected
class FlutterCameraPluginMethodChannel {
  static final MethodChannel channel = const MethodChannel(
    FlutterCameraPluginConstants.channelId,
  );
}