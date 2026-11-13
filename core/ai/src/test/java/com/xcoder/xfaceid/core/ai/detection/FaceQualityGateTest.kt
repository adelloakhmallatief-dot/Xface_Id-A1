package com.xcoder.xfaceid.core.ai.detection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FaceQualityGateTest {

    private val gate = FaceQualityGate()

    private fun face(
        left: Float = 0.30f,
        right: Float = 0.70f,
        top: Float = 0.30f,
        bottom: Float = 0.65f,
        score: Float = 0.9f,
        noseX: Float = 0.50f,
    ) = DetectedFace(
        box = FaceBox(left, top, right, bottom),
        score = score,
        rightEye = Landmark(0.42f, 0.40f),
        leftEye = Landmark(0.58f, 0.40f),
        nose = Landmark(noseX, 0.47f),
        mouth = Landmark(0.50f, 0.55f),
    )

    @Test
    fun goodFace_hasNoIssue() {
        val result = gate.evaluate(listOf(face()))
        assertNull(result.issue)
        assertEquals(true, result.isGood)
    }

    @Test
    fun noFace() {
        assertEquals(QualityIssue.NO_FACE, gate.evaluate(emptyList()).issue)
    }

    @Test
    fun multipleFaces() {
        assertEquals(QualityIssue.MULTIPLE_FACES, gate.evaluate(listOf(face(), face())).issue)
    }

    @Test
    fun lowConfidence() {
        assertEquals(QualityIssue.LOW_CONFIDENCE, gate.evaluate(listOf(face(score = 0.3f))).issue)
    }

    @Test
    fun tooFar() {
        assertEquals(
            QualityIssue.TOO_FAR,
            gate.evaluate(listOf(face(left = 0.45f, right = 0.55f))).issue,
        )
    }

    @Test
    fun tooClose() {
        assertEquals(
            QualityIssue.TOO_CLOSE,
            gate.evaluate(listOf(face(left = 0.05f, right = 0.95f))).issue,
        )
    }

    @Test
    fun offCenter() {
        assertEquals(
            QualityIssue.OFF_CENTER,
            gate.evaluate(listOf(face(left = 0.60f, right = 1.00f))).issue,
        )
    }

    @Test
    fun notFrontal_whenNoseFarFromEyeMidpoint() {
        assertEquals(
            QualityIssue.NOT_FRONTAL,
            gate.evaluate(listOf(face(noseX = 0.62f))).issue,
        )
    }
}
