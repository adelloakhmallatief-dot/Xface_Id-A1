package com.xcoder.xfaceid.registration

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xcoder.xfaceid.R
import com.xcoder.xfaceid.core.ai.detection.FaceDetectionEngine
import com.xcoder.xfaceid.core.ai.detection.FaceFrame
import com.xcoder.xfaceid.core.ai.detection.QualityIssue
import com.xcoder.xfaceid.core.camera.CameraPreview
import com.xcoder.xfaceid.core.camera.FaceFrameAnalyzer
import kotlin.math.max

@Composable
fun RegistrationScreen(viewModel: RegistrationViewModel) {
    val context = LocalContext.current

    // Gagal membuat engine biasanya berarti berkas model belum ada di assets.
    val engineResult = remember { runCatching { FaceDetectionEngine(context.applicationContext) } }
    val engine = engineResult.getOrNull()

    if (engine == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.model_load_failed),
                textAlign = TextAlign.Center,
            )
        }
        return
    }

    DisposableEffect(engine) {
        onDispose { engine.close() }
    }

    val analyzer = remember(engine, viewModel) {
        FaceFrameAnalyzer(engine, onError = { viewModel.onError(it) }) { frame, bitmap ->
            viewModel.onFrame(frame, bitmap)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(modifier = Modifier.fillMaxSize(), analyzer = analyzer)

        FaceOverlay(
            frame = viewModel.overlay,
            good = viewModel.issue == null,
            modifier = Modifier.fillMaxSize(),
        )

        // Panel debug sementara untuk uji coba. Dihapus sebelum rilis.
        Text(
            text = viewModel.debugInfo,
            color = Color.Yellow,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(6.dp),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when (viewModel.stage) {
                RegistrationStage.SCANNING -> {
                    Text(
                        text = stringResource(issueMessage(viewModel.issue)),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                    LinearProgressIndicator(
                        progress = {
                            viewModel.captured.toFloat() / RegistrationViewModel.TARGET_SAMPLES
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = stringResource(
                            R.string.registration_progress,
                            viewModel.captured,
                            RegistrationViewModel.TARGET_SAMPLES,
                        ),
                        color = Color.White,
                    )
                }

                RegistrationStage.DONE -> {
                    Text(
                        text = stringResource(R.string.registration_done),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                    Button(onClick = { viewModel.reset() }) {
                        Text(stringResource(R.string.registration_retry))
                    }
                }
            }
        }
    }
}

private fun issueMessage(issue: QualityIssue?): Int = when (issue) {
    null -> R.string.issue_hold_still
    QualityIssue.NO_FACE -> R.string.issue_no_face
    QualityIssue.MULTIPLE_FACES -> R.string.issue_multiple_faces
    QualityIssue.LOW_CONFIDENCE -> R.string.issue_low_confidence
    QualityIssue.TOO_FAR -> R.string.issue_too_far
    QualityIssue.TOO_CLOSE -> R.string.issue_too_close
    QualityIssue.OFF_CENTER -> R.string.issue_off_center
    QualityIssue.NOT_FRONTAL -> R.string.issue_not_frontal
}

/**
 * Menggambar kotak wajah di atas preview. PreviewView memakai FILL_CENTER (gambar di-scale
 * lalu dipotong di tengah) dan memirror kamera depan, jadi koordinat dipetakan sesuai itu.
 */
@Composable
private fun FaceOverlay(frame: FaceFrame?, good: Boolean, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val f = frame ?: return@Canvas
        if (f.imageWidth <= 0 || f.imageHeight <= 0) return@Canvas

        val imgW = f.imageWidth.toFloat()
        val imgH = f.imageHeight.toFloat()
        val scale = max(size.width / imgW, size.height / imgH)
        val offsetX = (size.width - imgW * scale) / 2f
        val offsetY = (size.height - imgH * scale) / 2f

        val color = if (good) Color(0xFF4CAF50) else Color.White
        val strokeWidth = 3.dp.toPx()

        for (face in f.faces) {
            val left = face.box.left * imgW * scale + offsetX
            val right = face.box.right * imgW * scale + offsetX
            val top = face.box.top * imgH * scale + offsetY
            val bottom = face.box.bottom * imgH * scale + offsetY

            // Mirror horizontal untuk kamera depan.
            val mirroredLeft = size.width - right
            val mirroredRight = size.width - left

            drawRect(
                color = color,
                topLeft = Offset(mirroredLeft, top),
                size = Size(mirroredRight - mirroredLeft, bottom - top),
                style = Stroke(width = strokeWidth),
            )
        }
    }
}
