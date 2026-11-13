package com.xcoder.xfaceid.registration

import android.graphics.Bitmap
import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.xcoder.xfaceid.core.ai.detection.FaceCropper
import com.xcoder.xfaceid.core.ai.detection.FaceFrame
import com.xcoder.xfaceid.core.ai.detection.FaceQualityGate
import com.xcoder.xfaceid.core.ai.detection.QualityIssue

enum class RegistrationStage { SCANNING, DONE }

/**
 * Alur registrasi Fase 2: kumpulkan [TARGET_SAMPLES] potongan wajah berkualitas baik.
 *
 * Potongan hanya disimpan di MEMORI. Fase 3 akan mengubahnya menjadi embedding
 * (MobileFaceNet), menyimpan embedding terenkripsi, lalu membuang potongan ini.
 *
 * onFrame dipanggil dari thread analisis; state Compose aman ditulis dari thread lain.
 */
class RegistrationViewModel : ViewModel() {

    var stage by mutableStateOf(RegistrationStage.SCANNING)
        private set
    var overlay by mutableStateOf<FaceFrame?>(null)
        private set
    var issue by mutableStateOf<QualityIssue?>(QualityIssue.NO_FACE)
        private set
    var captured by mutableIntStateOf(0)
        private set

    private val gate = FaceQualityGate()
    private val samples = mutableListOf<Bitmap>()
    private var goodStreak = 0
    private var lastCaptureMs = 0L

    @Synchronized
    fun onFrame(frame: FaceFrame, bitmap: Bitmap) {
        if (stage == RegistrationStage.DONE) return

        overlay = frame
        val quality = gate.evaluate(frame.faces)
        issue = quality.issue

        val face = quality.face
        if (!quality.isGood || face == null) {
            goodStreak = 0
            return
        }

        goodStreak++
        val now = SystemClock.elapsedRealtime()
        if (goodStreak < STEADY_FRAMES || now - lastCaptureMs < MIN_INTERVAL_MS) return

        val crop = FaceCropper.crop(bitmap, face.box) ?: return
        samples += crop
        captured = samples.size
        lastCaptureMs = now
        goodStreak = 0

        if (samples.size >= TARGET_SAMPLES) {
            stage = RegistrationStage.DONE
        }
    }

    @Synchronized
    fun reset() {
        samples.clear()
        captured = 0
        goodStreak = 0
        lastCaptureMs = 0L
        overlay = null
        issue = QualityIssue.NO_FACE
        stage = RegistrationStage.SCANNING
    }

    /** Dipakai Fase 3 untuk membuat embedding. Salinan daftar; bitmap tetap milik ViewModel sampai reset. */
    @Synchronized
    fun samplesSnapshot(): List<Bitmap> = samples.toList()

    override fun onCleared() {
        samples.clear()
    }

    companion object {
        const val TARGET_SAMPLES = 5
        private const val STEADY_FRAMES = 3
        private const val MIN_INTERVAL_MS = 500L
    }
}
