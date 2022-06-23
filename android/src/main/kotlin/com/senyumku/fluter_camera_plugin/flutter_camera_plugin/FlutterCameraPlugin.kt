package com.senyumku.fluter_camera_plugin.flutter_camera_plugin

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata.*
import android.os.Build
import com.senyumku.fluter_camera_plugin.flutter_camera_plugin.PluginConstants.channel_id
import com.senyumku.fluter_camera_plugin.flutter_camera_plugin.PluginConstants.get_available_cameras_method_name
import com.senyumku.fluter_camera_plugin.flutter_camera_plugin.PluginConstants.previewViewType
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterPluginBinding
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result


/** FlutterCameraPlugin  */
class FlutterCameraPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private var channel: MethodChannel? = null
    private var flutterPluginBinding: FlutterPluginBinding? = null
    var activityPluginBinding: ActivityPluginBinding? = null
    override fun onAttachedToEngine(flutterPluginBinding: FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, channel_id)
        channel!!.setMethodCallHandler(this)
        this.flutterPluginBinding = flutterPluginBinding
        flutterPluginBinding.platformViewRegistry.registerViewFactory(
            previewViewType,
            CameraViewFactory(flutterPluginBinding.applicationContext, flutterPluginBinding.binaryMessenger, flutterPluginBinding, this)
        )
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        if (call.method == "getPlatformVersion") {
            result.success("Android " + Build.VERSION.RELEASE)
        } else if (call.method == get_available_cameras_method_name) {
            try {
                result.success(cameras)
            } catch (e: CameraAccessException) {
                e.printStackTrace()
                result.error("-1", "Error getting info", "Error getting camera info")
            }
        } else {
            result.notImplemented()
        }
    }

    @get:Throws(CameraAccessException::class)
    val cameras: List<Map<String, Any>>
        get() {
            val cameraManager: CameraManager =
                activityPluginBinding!!.activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraNames: Array<String> = cameraManager.cameraIdList
            val cameras: MutableList<Map<String, Any>> = ArrayList()
            for (cameraName in cameraNames) {
                val details: HashMap<String, Any> = HashMap()
                val characteristics: CameraCharacteristics =
                    cameraManager.getCameraCharacteristics(cameraName)
                details["name"] = cameraName
                val sensorOrientation: Int =
                    characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)!!
                details["sensorOrientation"] = sensorOrientation
                when (characteristics.get(CameraCharacteristics.LENS_FACING)) {
                    LENS_FACING_FRONT -> details["lensFacing"] = "Front"
                    LENS_FACING_BACK -> details["lensFacing"] = "Back"
                    LENS_FACING_EXTERNAL -> details["lensFacing"] = "External"
                }
                cameras.add(details)
            }
            return cameras
        }

    override fun onDetachedFromEngine(binding: FlutterPluginBinding) {
        channel!!.setMethodCallHandler(null)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activityPluginBinding = binding
    }

    override fun onDetachedFromActivityForConfigChanges() {}
    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {}
    override fun onDetachedFromActivity() {}
}
