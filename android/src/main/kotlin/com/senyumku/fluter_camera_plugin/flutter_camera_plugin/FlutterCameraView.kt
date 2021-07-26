package com.senyumku.fluter_camera_plugin.flutter_camera_plugin

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.*
import android.media.AudioManager
import android.media.MediaActionSound
import android.os.Build
import android.os.Handler
import android.util.Rational
import android.util.Size
import android.view.View
import androidx.camera.core.*
import androidx.camera.core.Camera
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.senyumku.fluter_camera_plugin.flutter_camera_plugin.PluginConstants.capture_image_method_name
import com.senyumku.fluter_camera_plugin.flutter_camera_plugin.PluginConstants.channel_id
import com.senyumku.fluter_camera_plugin.flutter_camera_plugin.PluginConstants.initializeCamera
import com.senyumku.fluter_camera_plugin.flutter_camera_plugin.PluginConstants.play_sound_on_click_method_name
import com.senyumku.fluter_camera_plugin.flutter_camera_plugin.PluginConstants.set_flash_method_name
import com.senyumku.fluter_camera_plugin.flutter_camera_plugin.PluginConstants.set_lens_facing_method_name
import com.senyumku.fluter_camera_plugin.flutter_camera_plugin.PluginConstants.set_preview_aspect_ratio_method_name
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.platform.PlatformView
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import androidx.camera.core.ImageCapture
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import java.util.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import android.os.Looper
import android.util.Log
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.DisplayOrientedMeteringPointFactory
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import com.senyumku.fluter_camera_plugin.flutter_camera_plugin.PluginConstants.set_camera_resolution
import java.util.HashMap
import java.util.Objects


class FlutterCameraView internal constructor(
    context: Context, messenger: BinaryMessenger?, id: Int,
    flutterPluginBinding: FlutterPluginBinding, plugin: FlutterCameraPlugin
) : PlatformView, MethodCallHandler {
    private val methodChannel: MethodChannel =
        MethodChannel(messenger, channel_id + "_" + 0)
    private var mPreviewView: PreviewView
    private val executor: Executor = Executors.newSingleThreadExecutor()

    private lateinit var camera: Camera
    private var flashMode = ImageCapture.FLASH_MODE_OFF
    private var cameraResolution = Size(2160, 3840)
    private var imageCapture: ImageCapture? = null
    private var cameraId = 0
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var flutterPluginBinding: FlutterPluginBinding
    var plugin: FlutterCameraPlugin
    private var context: Context
    private var aspectRatio: Rational = Rational(16, 9)
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraRequestID = 513469796
    var playSoundOnClick: Boolean = false
    private var saveToFile = true
    private var torchMode = false
    private fun startCamera(
        context: Context,
        plugin: FlutterCameraPlugin
    ) {
        val cameraProviderFuture = ProcessCameraProvider
            .getInstance(context)
        cameraProviderFuture.addListener(Runnable {
            try {
                if (cameraProvider != null) {
                    return@Runnable
                }
                cameraProvider = cameraProviderFuture.get()
                bindPreview(cameraProvider, plugin)
            } catch (e: ExecutionException) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            } catch (e: InterruptedException) {
            }
        }, ContextCompat.getMainExecutor(context))
    }

    @SuppressLint("ClickableViewAccessibility")
    fun bindPreview(
        cameraProvider: ProcessCameraProvider?,
        plugin: FlutterCameraPlugin
    ) {

        val a: Int = Resources.getSystem().displayMetrics.widthPixels
        val previewBuilder: Preview.Builder = Preview.Builder()
        @SuppressLint("RestrictedApi") val preview: Preview = previewBuilder
            .setTargetResolution(
                Size(
                    a,
                    (a * 16.0 / 9.0).toInt()
                )
            ) // .setTargetAspectRatioCustom(new Rational(16,9))
            .build()
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(if (lensFacing == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_BACK else CameraSelector.LENS_FACING_FRONT)
            .build()
        val imageAnalysis = ImageAnalysis.Builder()
            .build()
        val builder = ImageCapture.Builder()
        imageCapture = plugin.activityPluginBinding?.activity?.windowManager?.defaultDisplay
            ?.rotation?.let {
                builder
                    .setTargetResolution(cameraResolution)
                    .setTargetRotation(
                        it
                    )
                    .build()
            }
        preview.setSurfaceProvider(mPreviewView.surfaceProvider)
        imageCapture!!.flashMode = flashMode
        cameraProvider?.unbindAll()
        if (cameraProvider != null) {
            camera = cameraProvider
                .bindToLifecycle(
                    plugin.activityPluginBinding?.activity as LifecycleOwner, cameraSelector,
                    preview, imageAnalysis, imageCapture
                )
        }
        camera.cameraControl.enableTorch(torchMode)
        val cameraControl: CameraControl = camera.cameraControl
        mPreviewView.setOnTouchListener { _, motionEvent ->
            val meteringPoint = DisplayOrientedMeteringPointFactory(
                mPreviewView.display,
                camera.cameraInfo, mPreviewView.width.toFloat(), mPreviewView.height.toFloat()
            )
                .createPoint(motionEvent.x, motionEvent.y)
            val action = FocusMeteringAction.Builder(meteringPoint).build()
            cameraControl.startFocusAndMetering(action)
            false
        }
        mPreviewView.setOnClickListener { Log.d("IsLog", "this log") }
    }

    private fun toBitmap(image: Image?): Bitmap {
        val planes: Array<Image.Plane> = image!!.planes
        val yBuffer: ByteBuffer = planes[0].buffer
        val uBuffer: ByteBuffer = planes[1].buffer
        val vBuffer: ByteBuffer = planes[2].buffer
        val ySize: Int = yBuffer.remaining()
        val uSize: Int = uBuffer.remaining()
        val vSize: Int = vBuffer.remaining()
        val nv21 = ByteArray(ySize + uSize + vSize)
        //U and V are swapped
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)
        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
        val imageBytes: ByteArray = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    private fun captureImage(path: String, result: MethodChannel.Result) {
        val file = File(path) //getDirectoryName(), mDateFormat.format(new Date())+ ".jpg");
        val outputFileOptions: ImageCapture.OutputFileOptions =
            ImageCapture.OutputFileOptions.Builder(file).build()
        imageCapture!!.flashMode = flashMode
        if (!saveToFile) {
            imageCapture!!.takePicture(executor, object : OnImageCapturedCallback() {
                @SuppressLint("UnsafeOptInUsageError")
                override fun onCaptureSuccess(image: ImageProxy) {
                    playClickSound()
                    plugin.activityPluginBinding?.activity?.runOnUiThread {
                        @SuppressLint("UnsafeExperimentalUsageError") val bim: Bitmap =
                            toBitmap(Objects.requireNonNull(image.image))
                        val size: Int = bim.rowBytes * bim.height
                        val b: ByteBuffer = ByteBuffer.allocate(size)
                        bim.copyPixelsToBuffer(b)
                        val bytes = ByteArray(size)
                        try {
                            b.get(bytes, 0, bytes.size)
                        } catch (e: BufferUnderflowException) {
                        }
                        result.success(bytes)
                    }
                    super.onCaptureSuccess(image)
                }

            })
        } else {
            imageCapture!!.takePicture(
                outputFileOptions,
                executor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        if (playSoundOnClick) {
                            playClickSound()
                        }
                        plugin.activityPluginBinding?.activity?.runOnUiThread {
                            val arguments2: HashMap<String, Any> = HashMap()
                            arguments2["pictureTaken"] = true
                            methodChannel.invokeMethod("pictureClicked", arguments2)
                            result.success(true)
                        }
                    }

                    override fun onError(error: ImageCaptureException) {
                        Handler(Looper.getMainLooper()).post {
                            error.printStackTrace()
                            result.error("-1", "error while capturing image", error.message)
                        }
                    }
                })
        }
    }

    fun playClickSound() {
        val audio: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        when (audio.ringerMode) {
            AudioManager.RINGER_MODE_NORMAL -> {
                val sound = MediaActionSound()
                sound.play(MediaActionSound.SHUTTER_CLICK)
            }
            AudioManager.RINGER_MODE_SILENT -> {
            }
            AudioManager.RINGER_MODE_VIBRATE -> {
            }
        }
    }

    private fun setFlashMode(mode: String?) {
        var flashMode = mode
        if (flashMode == "Torch") {
            setTorchMode(true)
            flashMode = "On"
        } else {
            setTorchMode(false)
        }
        this.flashMode = CameraUtils.getFlashModeFromString(flashMode)
        if (imageCapture != null) {
            imageCapture!!.flashMode = this.flashMode
        }
    }

    private fun setCameraResolution(resolution: String?){
        this.cameraResolution = CameraUtils.getCamaraResolution(resolution)
    }

    private fun setTorchMode(mode: Boolean) {
        torchMode = mode
        try {
            if (camera != null) {
                camera.cameraControl.enableTorch(mode)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setPlaySoundClick(value: Boolean) {
        playSoundOnClick = value
    }

    private fun setLensFacing(lensFacing: String?) {
        this.lensFacing = CameraUtils.getLensFacingFromString(lensFacing)
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method as String) {
            capture_image_method_name -> captureImage(
                call.argument<Any>("data") as String,
                result
            )
            set_flash_method_name -> {
                setFlashMode(call.argument<Any>("data") as String?)
                result.success(true)
            }
            set_lens_facing_method_name -> {
                setLensFacing(call.argument<Any>("data") as String?)
                result.success(true)
            }
            initializeCamera -> {
                setLensFacing(call.argument<Any>("lensFacing") as String?)
                setCameraResolution(call.argument<Any>("cameraResolution") as String?)
                if (call.argument<Any?>("saveToFile") != null && !(call.argument<Any>("saveToFile") as Boolean)) {
                    saveToFile = true
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    plugin.activityPluginBinding?.activity?.requestPermissions(
                        arrayOf(Manifest.permission.CAMERA),
                        513469796
                    )
                    plugin.activityPluginBinding?.addRequestPermissionsResultListener { requestCode, _, grantResults ->
                        if (grantResults.isNotEmpty() && requestCode == cameraRequestID && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                            startCamera(
                                context,
                                plugin
                            ) //start camera if permission has been granted by user
                        }
                        false
                    }
                }
            }
            set_preview_aspect_ratio_method_name -> try {
                aspectRatio =
                    Rational(call.argument<Any>("num") as Int, call.argument<Any>("denom") as Int)
                result.success(true)
            } catch (e: Exception) {
                result.error("-2", "Invalid Aspect Ratio", "Invalid Aspect Ratio")
            }
            play_sound_on_click_method_name -> {
                setPlaySoundClick((call.argument<Any>("data") as Boolean?)!!)
                result.notImplemented()
            }
            set_camera_resolution ->{
                setCameraResolution(call.argument<Any>("data") as String?)
                result.success(true)
            }
            else -> result.notImplemented()
        }
    }

    override fun getView(): View {
        return mPreviewView
    }

    @SuppressLint("RestrictedApi")
    override fun dispose() {
        if (cameraProvider != null) {
            cameraProvider!!.unbindAll()
            cameraProvider!!.shutdown()
        }
        imageCapture = null
    }

    init {

        cameraId = id
        this.context = context
        this.plugin = plugin
        this.flutterPluginBinding = flutterPluginBinding
        methodChannel.setMethodCallHandler(this)
        mPreviewView = PreviewView(context)
        mPreviewView.importantForAccessibility = 0
        mPreviewView.minimumHeight = 100
        mPreviewView.minimumWidth = 100
        mPreviewView.contentDescription = "Description Here"
    }
}
