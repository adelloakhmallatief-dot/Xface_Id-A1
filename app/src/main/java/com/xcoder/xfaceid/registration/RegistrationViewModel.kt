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

/** SCANNING: mencari wajah. DONE: berhasil. FAILED: waktu habis tanpa wajah yang jelas, tampilkan "coba lagi". */
enum class RegistrationStage { SCANNING, DONE, FAILED }

/**
 * Alur pindai satu kali: begitu satu potongan wajah berkualitas baik didapat, langsung berhasil
 * (lalu app membuka layar utama). Jika [SCAN_TIMEOUT_MS] habis tanpa hasil, gagal dan pengguna bisa mengulang.
 *
 * Potongan hanya disimpan di MEMORI. Fase 3 akan mengubahnya menjadi embedding
 * (MobileFaceNet) lalu mencocokkannya dengan wajah terdaftar.
 * CATATAN: sebelum Fase 3, "berhasil" hanya berarti ada wajah yang jelas, BUKAN verifikasi identitas.
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

    /** Teks diagnostik sementara untuk uji coba di HP (fps, ukuran frame, angka kualitas, error). */
    var debugInfo by mutableStateOf("menunggu frame pertama...")
        private set

    /** True setelah pindai berhasil: app menampilkan layar utama. */
    var unlocked by mutableStateOf(false)
        private set

    private val gate = FaceQualityGate()
    private val samples = mutableListOf<Bitmap>()
    private var goodStreak = 0
    private var lastCaptureMs = 0L
    private var scanStartMs = 0L

    private var frameCount = 0
    private var fpsWindowStart = 0L
    private var fps = 0
    private var lastError: String? = null

    /** Dipanggil analyzer bila satu frame gagal diproses. */
    @Synchronized
    fun onError(t: Throwable) {
        lastError = "${t.javaClass.simpleName}: ${t.message}".take(140)
    }

    @Synchronized
    fun onFrame(frame: FaceFrame, bitmap: Bitmap) {
        if (stage != RegistrationStage.SCANNING) return

        val now = SystemClock.elapsedRealtime()
        if (scanStartMs == 0L) scanStartMs = now
        if (now - scanStartMs > SCAN_TIMEOUT_MS) {
            stage = RegistrationStage.FAILED
            return
        }
        frameCount++
        if (now - fpsWindowStart >= 1000L) {
            fps = frameCount
            frameCount = 0
            fpsWindowStart = now
        }

        overlay = frame
        val quality = gate.evaluate(frame.faces)
        issue = quality.issue

        debugInfo = buildString {
            append("fps=$fps  ${frame.imageWidth}x${frame.imageHeight}  streak=$goodStreak\n")
            append(gate.describe(frame.faces)).append('\n')
            append("masalah=${quality.issue ?: "tidak ada"}")
            lastError?.let { append("\nerror: $it") }
        }

        val face = quality.face
        if (!quality.isGood || face == null) {
            goodStreak = 0
            return
        }

        goodStreak++
        if (goodStreak < STEADY_FRAMES || now - lastCaptureMs < MIN_INTERVAL_MS) return

        val crop = try {
            FaceCropper.crop(bitmap, face.box)
        } catch (e: Exception) {
            lastError = "crop: ${e.javaClass.simpleName}: ${e.message}".take(140)
            null
        }
        if (crop == null) {
            // Jangan mengulang tanpa henti dengan streak penuh; mulai hitung lagi.
            goodStreak = 0
            return
        }

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
        scanStartMs = 0L
        overlay = null
        issue = QualityIssue.NO_FACE
        lastError = null
        stage = RegistrationStage.SCANNING
    }

    /** Dipanggil UI setelah animasi berhasil selesai. */
    fun unlock() {
        if (stage == RegistrationStage.DONE) unlocked = true
    }

    /** Kembali ke pindai wajah (tombol "Kunci lagi"). */
    fun lockAgain() {
        unlocked = false
        reset()
    }

    /** Dipakai Fase 3 untuk membuat embedding. Salinan daftar; bitmap tetap milik ViewModel sampai reset. */
    @Synchronized
    fun samplesSnapshot(): List<Bitmap> = samples.toList()

    override fun onCleared() {
        samples.clear()
    }

    companion object {
        /** Cukup satu pindai yang jelas. */
        const val TARGET_SAMPLES = 1
        const val SCAN_TIMEOUT_MS = 15_000L
        private const val STEADY_FRAMES = 3
        private const val MIN_INTERVAL_MS = 500L
    }
}
