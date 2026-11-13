package com.xcoder.xfaceid.core.ai.detection

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facedetector.FaceDetector
import java.io.Closeable

/**
 * Pembungkus MediaPipe Face Detector (BlazeFace short-range), 100% on-device.
 *
 * Memakai RunningMode.IMAGE: satu frame masuk, satu hasil keluar, sinkron.
 * Panggil dari satu thread analisis saja. Model dimuat dari assets
 * (lihat task downloadFaceDetectorModel di build.gradle.kts modul ini).
 */
class FaceDetectionEngine(
    context: Context,
    minDetectionConfidence: Float = 0.5f,
) : Closeable {

    private val detector: FaceDetector

    init {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath(MODEL_ASSET)
            .build()
        val options = FaceDetector.FaceDetectorOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.IMAGE)
            .setMinDetectionConfidence(minDetectionConfidence)
            .build()
        detector = FaceDetector.createFromOptions(context, options)
    }

    /** [upright] harus sudah tegak (sudah dirotasi sesuai orientasi sensor). */
    fun detect(upright: Bitmap): FaceFrame {
        val w = upright.width
        val h = upright.height
        val result = detector.detect(BitmapImageBuilder(upright).build())

        val faces = result.detections().map { d ->
            val bb = d.boundingBox()
            val score = d.categories().firstOrNull()?.score() ?: 0f
            val kps = d.keypoints().orElse(emptyList())

            // Urutan keypoint MediaPipe: mata kanan, mata kiri, ujung hidung,
            // tengah mulut, telinga kanan, telinga kiri.
            fun kp(i: Int): Landmark? = kps.getOrNull(i)?.let { Landmark(it.x(), it.y()) }

            DetectedFace(
                box = FaceBox(
                    left = bb.left / w,
                    top = bb.top / h,
                    right = bb.right / w,
                    bottom = bb.bottom / h,
                ),
                score = score,
                rightEye = kp(0),
                leftEye = kp(1),
                nose = kp(2),
                mouth = kp(3),
            )
        }
        return FaceFrame(faces = faces, imageWidth = w, imageHeight = h)
    }

    override fun close() {
        detector.close()
    }

    companion object {
        const val MODEL_ASSET = "blaze_face_short_range.tflite"
    }
}
