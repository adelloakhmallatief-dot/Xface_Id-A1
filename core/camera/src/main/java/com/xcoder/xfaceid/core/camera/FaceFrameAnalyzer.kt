package com.xcoder.xfaceid.core.camera

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.xcoder.xfaceid.core.ai.detection.FaceDetectionEngine
import com.xcoder.xfaceid.core.ai.detection.FaceFrame

private const val TAG = "XFaceAnalyzer"

/**
 * Jembatan kamera -> deteksi: mengubah frame menjadi bitmap tegak, menjalankan detektor,
 * lalu memanggil [onFrame].
 *
 * [onFrame] dipanggil di thread analisis, bukan thread utama. Bitmap yang diberikan hanya
 * berlaku untuk pemanggilan itu: salin atau potong bila perlu disimpan, dan jangan tulis ke disk.
 *
 * Catatan performa: konversi + rotasi bitmap per frame sengaja dibuat sederhana di Fase 2.
 * Optimasi (reuse buffer, rotasi di dalam MediaPipe) dikerjakan di fase Lite.
 */
class FaceFrameAnalyzer(
    private val engine: FaceDetectionEngine,
    private val onError: (Throwable) -> Unit = {},
    private val onFrame: (frame: FaceFrame, bitmap: Bitmap) -> Unit,
) : ImageAnalysis.Analyzer {

    override fun analyze(image: ImageProxy) {
        try {
            val rotation = image.imageInfo.rotationDegrees
            val raw = image.toBitmap()
            val upright = if (rotation == 0) {
                raw
            } else {
                val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
                Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, matrix, true)
            }
            onFrame(engine.detect(upright), upright)
        } catch (e: Exception) {
            Log.e(TAG, "Gagal menganalisis frame", e)
            onError(e)
        } finally {
            image.close()
        }
    }
}
