package com.senyumku.fluter_camera_plugin.flutter_camera_plugin

import android.content.Context
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

class CameraViewFactory(
    private val messenger: BinaryMessenger,
    private var flutterPluginBinding: FlutterPluginBinding,
    var plugin: FlutterCameraPlugin
) :
    PlatformViewFactory(StandardMessageCodec.INSTANCE) {
    override fun create(context: Context, id: Int, o: Any?): PlatformView {
        return FlutterCameraView(context, messenger, id, flutterPluginBinding, plugin)
    }

}