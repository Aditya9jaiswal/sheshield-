@file:Suppress("DEPRECATION")
package com.example.sheshield0.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera
import android.media.MediaRecorder
import android.os.Environment
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CameraUtils {

    private var camera: Camera? = null
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var currentVideoFile: File? = null
    private var recordingHandler: Handler? = null

    fun startCameraRecording(
        context: Context,
        duration: Long = 7000L,                       // default 7 seconds
        cameraFacing: Int = Camera.CameraInfo.CAMERA_FACING_FRONT,
        rotation: Int = 90,
        onComplete: (File?) -> Unit
    ) {
        // Check permissions
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            onComplete(null)
            return
        }

        // Cleanup previous resources if any
        cleanup()

        // Create video file
        currentVideoFile = createVideoFile(context) ?: run {
            onComplete(null)
            return
        }

        // Open camera
        camera = Camera.open(cameraFacing)
        camera?.let { cam ->
            val params = cam.parameters
            val supportedSizes = params.supportedPreviewSizes
            val optimalSize = supportedSizes?.maxByOrNull { it.width * it.height } ?: supportedSizes?.firstOrNull()
            optimalSize?.let {
                params.setPreviewSize(it.width, it.height)
                params.setRotation(rotation)
            }

            // Focus mode
            if (params.supportedFocusModes?.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO) == true) {
                params.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
            }

            cam.parameters = params
            cam.unlock()

            // MediaRecorder setup
            mediaRecorder = MediaRecorder().apply {
                setCamera(cam)
                setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
                setVideoSource(MediaRecorder.VideoSource.CAMERA)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                setOutputFile(currentVideoFile!!.absolutePath)
                setOrientationHint(rotation)
                setVideoSize(optimalSize?.width ?: 1280, optimalSize?.height ?: 720)
                setVideoFrameRate(30)
                setVideoEncodingBitRate(3000000)
                prepare()
                start()
                isRecording = true
            }

            // Stop recording after duration
            recordingHandler = Handler(Looper.getMainLooper())
            recordingHandler?.postDelayed({
                stopRecording(onComplete)
            }, duration)
        } ?: onComplete(null)
    }



    fun stopRecording(onComplete: (File?) -> Unit) {
        if (!isRecording) {
            onComplete(null)
            return
        }

        mediaRecorder?.stop()
        isRecording = false
        val file = currentVideoFile
        cleanup()
        onComplete(file?.takeIf { it.exists() && it.length() > 0 })
    }

    private fun createVideoFile(context: Context): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        storageDir?.mkdirs()
        return File.createTempFile("EMERGENCY_${timeStamp}_", ".mp4", storageDir)
    }

    private fun cleanup() {
        recordingHandler?.removeCallbacksAndMessages(null)
        recordingHandler = null

        mediaRecorder?.apply {
            if (isRecording) stop()
            reset()
            release()
        }
        mediaRecorder = null

        camera?.apply {
            lock()
            release()
        }
        camera = null

        isRecording = false
    }

    fun isRecording(): Boolean = isRecording
}
