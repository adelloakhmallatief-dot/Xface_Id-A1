package com.xcoder.xfaceid.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xcoder.xfaceid.R
import com.xcoder.xfaceid.core.ai.detection.FaceDetectionEngine
import com.xcoder.xfaceid.core.camera.CameraPreview
import com.xcoder.xfaceid.core.camera.FaceFrameAnalyzer
import com.xcoder.xfaceid.registration.RegistrationStage
import com.xcoder.xfaceid.registration.RegistrationViewModel
import com.xcoder.xfaceid.registration.issueMessageRes

/**
 * Registrasi versi Lite: preview kamera + teks + bilah progres. Tanpa animasi, tanpa overlay gambar,
 * supaya hemat RAM dan baterai. Mesin deteksi dan ambang kualitas sama persis dengan edisi Standard.
 */
@Composable
fun RegistrationScreen(viewModel: RegistrationViewModel) {
    val context = LocalContext.current
    // Gagal membuat engine biasanya berarti berkas model belum ada di assets.
    val engine = remember { runCatching { FaceDetectionEngine(context.applicationContext) }.getOrNull() }

    if (engine == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = stringResource(R.string.model_load_failed), textAlign = TextAlign.Center)
        }
    } else {
        RegistrationContent(viewModel, engine)
    }
}

@Composable
private fun RegistrationContent(viewModel: RegistrationViewModel, engine: FaceDetectionEngine) {
    DisposableEffect(engine) {
        onDispose { engine.close() }
    }

    val analyzer = remember(engine, viewModel) {
        FaceFrameAnalyzer(engine, onError = { viewModel.onError(it) }) { frame, bitmap ->
            viewModel.onFrame(frame, bitmap)
        }
    }

    val done = viewModel.stage == RegistrationStage.DONE

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleMedium,
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .clip(RoundedCornerShape(12.dp)),
        ) {
            CameraPreview(modifier = Modifier.fillMaxSize(), analyzer = analyzer)
        }

        Text(
            text = stringResource(
                if (done) R.string.registration_done else issueMessageRes(viewModel.issue),
            ),
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.Center,
        )

        LinearProgressIndicator(
            progress = { viewModel.captured.toFloat() / RegistrationViewModel.TARGET_SAMPLES },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
        )
        Text(
            text = stringResource(
                R.string.registration_progress,
                viewModel.captured,
                RegistrationViewModel.TARGET_SAMPLES,
            ),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp),
        )

        if (done) {
            Button(
                onClick = { viewModel.reset() },
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text(stringResource(R.string.registration_retry))
            }
        }

        // Diagnostik sementara untuk uji coba (sama dengan panel debug di Standard). Hapus sebelum rilis.
        Text(
            text = viewModel.debugInfo,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
