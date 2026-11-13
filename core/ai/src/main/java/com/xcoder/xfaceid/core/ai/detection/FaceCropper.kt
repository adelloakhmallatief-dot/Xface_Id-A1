package com.xcoder.xfaceid.core.ai.detection

import android.graphics.Bitmap

object FaceCropper {

    /**
     * Memotong area wajah dari [source] (gambar tegak, tidak dimirror) dengan [margin]
     * (proporsi dari lebar/tinggi kotak). Hasil selalu bitmap baru yang berdiri sendiri.
     * Bitmap hanya hidup di memori; jangan ditulis ke disk.
     */
    fun crop(source: Bitmap, box: FaceBox, margin: Float = 0.25f): Bitmap? {
        val w = source.width
        val h = source.height
        if (w < 2 || h < 2) return null

        val mx = box.width * margin
        val my = box.height * margin

        val left = ((box.left - mx) * w).toInt().coerceIn(0, w - 1)
        val top = ((box.top - my) * h).toInt().coerceIn(0, h - 1)
        val right = ((box.right + mx) * w).toInt().coerceIn(left + 1, w)
        val bottom = ((box.bottom + my) * h).toInt().coerceIn(top + 1, h)

        val out = Bitmap.createBitmap(source, left, top, right - left, bottom - top)
        return if (out === source) {
            source.copy(source.config ?: Bitmap.Config.ARGB_8888, false)
        } else {
            out
        }
    }
}
