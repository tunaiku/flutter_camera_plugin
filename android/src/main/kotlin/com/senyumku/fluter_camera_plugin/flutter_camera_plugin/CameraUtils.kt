package com.senyumku.fluter_camera_plugin.flutter_camera_plugin

import android.util.Size
import androidx.camera.core.CameraSelector

import androidx.camera.core.ImageCapture

object CameraUtils {
    fun getFlashModeFromString(mode: String?): Int {
        when (mode) {
            "Auto" -> return ImageCapture.FLASH_MODE_AUTO
            "On" -> return ImageCapture.FLASH_MODE_ON
            "Off" -> return ImageCapture.FLASH_MODE_OFF
        }
        return 0
    }

    fun getLensFacingFromString(mode: String?): Int {
        when (mode) {
            "Front" -> return CameraSelector.LENS_FACING_FRONT
            "Back" -> return CameraSelector.LENS_FACING_BACK
        }
        return 0
    }

    fun getCamaraResolution(resolution: String?): Size {
        when (resolution) {
            "low" -> return Size(240, 320)
            "medium" -> return Size(480, 720)
            "high" -> return Size(720, 1280)
            "veryHigh" -> return Size(1080, 1920)
            "ultraHigh" -> return Size(2160, 3840)
        }
        return Size(2160, 3840)
    }
}