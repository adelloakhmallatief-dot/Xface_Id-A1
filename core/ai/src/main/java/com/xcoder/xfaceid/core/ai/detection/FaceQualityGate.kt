package com.xcoder.xfaceid.core.ai.detection

import kotlin.math.abs
import kotlin.math.atan2

enum class QualityIssue {
    NO_FACE,
    MULTIPLE_FACES,
    LOW_CONFIDENCE,
    TOO_FAR,
    TOO_CLOSE,
    OFF_CENTER,
    NOT_FRONTAL,
}

data class QualityResult(
    /** null = kualitas baik. */
    val issue: QualityIssue?,
    val face: DetectedFace?,
) {
    val isGood: Boolean get() = issue == null
}

/** Ambang batas. Nilai awal dibuat longgar (prioritas kenyamanan), tuning di Fase 8 dengan data nyata. */
data class QualityConfig(
    val minScore: Float = 0.6f,
    val minFaceWidth: Float = 0.30f,
    val maxFaceWidth: Float = 0.80f,
    val maxCenterOffsetX: Float = 0.20f,
    val maxCenterOffsetY: Float = 0.25f,
    val maxYawRatio: Float = 0.35f,
    val maxRollDegrees: Float = 20f,
)

/**
 * Gerbang kualitas untuk registrasi: tepat satu wajah, cukup besar, di tengah, menghadap depan.
 * Kotlin murni tanpa dependensi Android supaya mudah diuji unit.
 */
class FaceQualityGate(private val config: QualityConfig = QualityConfig()) {

    fun evaluate(faces: List<DetectedFace>): QualityResult {
        if (faces.isEmpty()) return QualityResult(QualityIssue.NO_FACE, null)
        if (faces.size > 1) return QualityResult(QualityIssue.MULTIPLE_FACES, null)

        val face = faces.first()
        val box = face.box

        val issue = when {
            face.score < config.minScore -> QualityIssue.LOW_CONFIDENCE
            box.width < config.minFaceWidth -> QualityIssue.TOO_FAR
            box.width > config.maxFaceWidth -> QualityIssue.TOO_CLOSE
            abs(box.centerX - 0.5f) > config.maxCenterOffsetX ||
                abs(box.centerY - 0.5f) > config.maxCenterOffsetY -> QualityIssue.OFF_CENTER
            !isFrontal(face) -> QualityIssue.NOT_FRONTAL
            else -> null
        }
        return QualityResult(issue, face)
    }

    /** Perkiraan menghadap depan dari mata dan hidung. Jika landmark tidak lengkap, dianggap tidak frontal. */
    private fun isFrontal(face: DetectedFace): Boolean {
        val (yawRatio, rollDegrees) = yawAndRoll(face) ?: return false
        return yawRatio <= config.maxYawRatio && rollDegrees <= config.maxRollDegrees
    }

    /** Pasangan (rasio yaw, derajat roll), atau null bila landmark tidak lengkap. */
    private fun yawAndRoll(face: DetectedFace): Pair<Float, Float>? {
        val re = face.rightEye ?: return null
        val le = face.leftEye ?: return null
        val nose = face.nose ?: return null

        val eyeDist = abs(le.x - re.x)
        if (eyeDist < 1e-3f) return null

        val eyeMidX = (le.x + re.x) / 2f
        val yawRatio = abs(nose.x - eyeMidX) / eyeDist

        val rollDegrees = Math.toDegrees(
            atan2((le.y - re.y).toDouble(), eyeDist.toDouble()),
        ).let { abs(it).toFloat() }

        return yawRatio to rollDegrees
    }

    /** Ringkasan angka untuk panel debug di layar (sementara, untuk uji coba). */
    fun describe(faces: List<DetectedFace>): String {
        if (faces.isEmpty()) return "wajah=0"
        if (faces.size > 1) return "wajah=${faces.size}"
        val f = faces.first()
        val yr = yawAndRoll(f)
        val yaw = yr?.first?.let { "%.2f".format(java.util.Locale.US, it) } ?: "-"
        val roll = yr?.second?.let { "%.0f".format(java.util.Locale.US, it) } ?: "-"
        return "skor=%.2f lebar=%.2f x=%.2f y=%.2f yaw=%s roll=%s".format(
            java.util.Locale.US, f.score, f.box.width, f.box.centerX, f.box.centerY, yaw, roll,
        )
    }
}
