package com.xcoder.xfaceid.core.ai.detection

/**
 * Semua koordinat DINORMALISASI 0..1 relatif terhadap gambar tegak (setelah rotasi),
 * dan TIDAK dimirror. Mirror untuk kamera depan dilakukan di lapisan UI.
 */
data class FaceBox(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
    val centerX: Float get() = (left + right) / 2f
    val centerY: Float get() = (top + bottom) / 2f
}

data class Landmark(val x: Float, val y: Float)

data class DetectedFace(
    val box: FaceBox,
    val score: Float,
    /** Mata kanan subjek (tampak di sisi kiri gambar). */
    val rightEye: Landmark?,
    /** Mata kiri subjek (tampak di sisi kanan gambar). */
    val leftEye: Landmark?,
    val nose: Landmark?,
    val mouth: Landmark?,
)

/** Hasil deteksi satu frame beserta ukuran gambar tegak yang dianalisis. */
data class FaceFrame(
    val faces: List<DetectedFace>,
    val imageWidth: Int,
    val imageHeight: Int,
)
