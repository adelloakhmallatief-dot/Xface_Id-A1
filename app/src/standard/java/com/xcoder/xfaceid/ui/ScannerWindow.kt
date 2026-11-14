package com.xcoder.xfaceid.ui

import androidx.camera.core.ImageAnalysis
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.xcoder.xfaceid.core.ai.detection.FaceFrame
import com.xcoder.xfaceid.core.camera.CameraPreview
import kotlin.math.max

/**
 * Jendela pemindai ala Face ID: kamera di dalam bingkai membulat, empat bracket pojok yang berpendar,
 * garis pemindai yang menyapu, dan penanda wajah. Berubah hijau saat wajah sudah pas.
 */
@Composable
fun ScannerWindow(
    positive: Boolean,
    done: Boolean,
    overlay: FaceFrame?,
    analyzer: ImageAnalysis.Analyzer,
    modifier: Modifier = Modifier,
) {
    val color by animateColorAsState(
        targetValue = if (positive || done) XColors.Mint else XColors.Cyan,
        animationSpec = tween(350),
        label = "frame-color",
    )
    val infinite = rememberInfiniteTransition(label = "scanner")
    val sweep by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(2400, easing = FastOutSlowInEasing),
            RepeatMode.Reverse,
        ),
        label = "sweep",
    )
    val pulse by infinite.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(1400, easing = FastOutSlowInEasing),
            RepeatMode.Reverse,
        ),
        label = "pulse",
    )
    val badge by animateFloatAsState(
        targetValue = if (done) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "badge",
    )

    Box(modifier) {
        Box(
            Modifier
                .matchParentSize()
                .padding(10.dp)
                .clip(RoundedCornerShape(36.dp))
                .background(Color.Black),
        ) {
            CameraPreview(modifier = Modifier.fillMaxSize(), analyzer = analyzer)

            Canvas(Modifier.fillMaxSize()) {
                if (!done) drawSweep(color, sweep)
                if (overlay != null) drawFaces(overlay, color)
            }
        }

        Canvas(Modifier.matchParentSize()) {
            drawBrackets(color, pulse)
        }

        if (badge > 0.01f) {
            Canvas(Modifier.matchParentSize()) {
                drawSuccessBadge(badge)
            }
        }
    }
}

private fun DrawScope.drawSweep(color: Color, progress: Float) {
    val y = size.height * (0.08f + 0.84f * progress)
    val band = size.height * 0.22f
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                color.copy(alpha = 0f),
                color.copy(alpha = 0.22f),
                color.copy(alpha = 0f),
            ),
            startY = y - band,
            endY = y + band,
        ),
        topLeft = Offset(0f, y - band),
        size = Size(size.width, band * 2f),
    )
    drawLine(
        color = color.copy(alpha = 0.85f),
        start = Offset(size.width * 0.12f, y),
        end = Offset(size.width * 0.88f, y),
        strokeWidth = 2.dp.toPx(),
        cap = StrokeCap.Round,
    )
}

/**
 * Penanda wajah. PreviewView memakai FILL_CENTER (gambar di-scale lalu dipotong di tengah) dan
 * memirror kamera depan, jadi koordinat dipetakan sesuai itu. Canvas ini seukuran area kamera.
 */
private fun DrawScope.drawFaces(frame: FaceFrame, color: Color) {
    if (frame.imageWidth <= 0 || frame.imageHeight <= 0) return

    val imgW = frame.imageWidth.toFloat()
    val imgH = frame.imageHeight.toFloat()
    val scale = max(size.width / imgW, size.height / imgH)
    val offsetX = (size.width - imgW * scale) / 2f
    val offsetY = (size.height - imgH * scale) / 2f

    for (face in frame.faces) {
        val left = face.box.left * imgW * scale + offsetX
        val right = face.box.right * imgW * scale + offsetX
        val top = face.box.top * imgH * scale + offsetY
        val bottom = face.box.bottom * imgH * scale + offsetY

        // Mirror horizontal untuk kamera depan.
        val mirroredLeft = size.width - right
        val mirroredRight = size.width - left

        drawRoundRect(
            color = color.copy(alpha = 0.9f),
            topLeft = Offset(mirroredLeft, top),
            size = Size(mirroredRight - mirroredLeft, bottom - top),
            cornerRadius = CornerRadius(20.dp.toPx()),
            style = Stroke(width = 2.dp.toPx()),
        )
    }
}

/** Empat bracket pojok dengan efek pendar (tiga lapis goresan, makin lebar makin samar). */
private fun DrawScope.drawBrackets(color: Color, glow: Float) {
    val w = size.width
    val h = size.height
    val inset = 5.dp.toPx()
    val r = 30.dp.toPx()
    val arm = 30.dp.toPx()
    val d = r * 2f

    fun paint(width: Float, c: Color) {
        val style = Stroke(width = width, cap = StrokeCap.Round)

        // Kiri atas
        drawArc(c, 180f, 90f, false, Offset(inset, inset), Size(d, d), style = style)
        drawLine(c, Offset(inset, inset + r), Offset(inset, inset + r + arm), width, StrokeCap.Round)
        drawLine(c, Offset(inset + r, inset), Offset(inset + r + arm, inset), width, StrokeCap.Round)

        // Kanan atas
        drawArc(c, 270f, 90f, false, Offset(w - inset - d, inset), Size(d, d), style = style)
        drawLine(c, Offset(w - inset - r, inset), Offset(w - inset - r - arm, inset), width, StrokeCap.Round)
        drawLine(c, Offset(w - inset, inset + r), Offset(w - inset, inset + r + arm), width, StrokeCap.Round)

        // Kanan bawah
        drawArc(c, 0f, 90f, false, Offset(w - inset - d, h - inset - d), Size(d, d), style = style)
        drawLine(c, Offset(w - inset, h - inset - r), Offset(w - inset, h - inset - r - arm), width, StrokeCap.Round)
        drawLine(c, Offset(w - inset - r, h - inset), Offset(w - inset - r - arm, h - inset), width, StrokeCap.Round)

        // Kiri bawah
        drawArc(c, 90f, 90f, false, Offset(inset, h - inset - d), Size(d, d), style = style)
        drawLine(c, Offset(inset + r, h - inset), Offset(inset + r + arm, h - inset), width, StrokeCap.Round)
        drawLine(c, Offset(inset, h - inset - r), Offset(inset, h - inset - r - arm), width, StrokeCap.Round)
    }

    paint(16.dp.toPx(), color.copy(alpha = 0.05f * glow))
    paint(9.dp.toPx(), color.copy(alpha = 0.10f * glow))
    paint(4.dp.toPx(), color)
}

private fun DrawScope.drawSuccessBadge(scale: Float) {
    val c = center
    val radius = 44.dp.toPx() * scale
    drawCircle(XColors.Mint.copy(alpha = 0.18f), radius = radius * 1.5f, center = c)
    drawCircle(XColors.Mint, radius = radius, center = c)

    val s = radius * 0.5f
    val p1 = Offset(c.x - s * 0.9f, c.y + s * 0.05f)
    val p2 = Offset(c.x - s * 0.2f, c.y + s * 0.7f)
    val p3 = Offset(c.x + s * 0.95f, c.y - s * 0.6f)
    val stroke = 6.dp.toPx() * scale
    drawLine(XColors.Ink, p1, p2, stroke, StrokeCap.Round)
    drawLine(XColors.Ink, p2, p3, stroke, StrokeCap.Round)
}
