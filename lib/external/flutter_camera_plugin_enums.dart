enum CameraFlashMode { On, Off, Auto, Torch }

enum CameraFacing { Front, Back, External }

enum CameraResolutionPreset {
  /// 240p (320x240) on Android
  low,

  /// 480p (720x480) on Android
  medium,

  /// 720p (1280x720)
  high,

  /// 1080p (1920x1080)
  veryHigh,

  /// 2160p (3840x2160)
  ultraHigh,
}

getCameraFacingFromString(String facing) {
  switch (facing) {
    case 'Front':
      return CameraFacing.Front;
    case 'Back':
      return CameraFacing.Back;
    case 'External':
      return CameraFacing.External;
  }
}

getStringFromCameraFacing(CameraFacing facing) {
  switch (facing) {
    case CameraFacing.Back:
      return "Back";
    case CameraFacing.Front:
      return "Front";
    case CameraFacing.External:
      return "External";
  }
}

getCameraResolutionPreset(CameraResolutionPreset? cameraResolutionPreset) {
  switch (cameraResolutionPreset) {
    case CameraResolutionPreset.low:
      return "low";
    case CameraResolutionPreset.medium:
      return "medium";
    case CameraResolutionPreset.high:
      return "high";
    case CameraResolutionPreset.veryHigh:
      return "veryHigh";
    case CameraResolutionPreset.ultraHigh:
      return "ultraHigh";
    default:
      return "ultraHigh";
  }
}
