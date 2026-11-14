package com.xcoder.xfaceid.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xcoder.xfaceid.R
import com.xcoder.xfaceid.core.ai.detection.FaceDetectionEngine
import com.xcoder.xfaceid.core.camera.FaceFrameAnalyzer
import com.xcoder.xfaceid.registration.RegistrationStage
import com.xcoder.xfaceid.registration.RegistrationViewModel
import com.xcoder.xfaceid.registration.issueMessageRes
import kotlinx.coroutines.delay

/** Panel debug sementara untuk uji coba. Tersembunyi secara default; tahan lama logo untuk menampilkannya. */
private const val SHOW_DEBUG_DEFAULT = false

/** Jeda singkat agar animasi centang berhasil terlihat sebelum masuk. */
private const val SUCCESS_HOLD_MS = 1000L

@Composable
fun RegistrationScreen(viewModel: RegistrationViewModel) {
    val context = LocalContext.current
    // Gagal membuat engine biasanya berarti berkas model belum ada di assets.
    val engine = remember { runCatching { FaceDetectionEngine(context.applicationContext) }.getOrNull() }

    val done = viewModel.stage == RegistrationStage.DONE
    val failed = viewModel.stage == RegistrationStage.FAILED
    val positive = (viewModel.issue == null || done) && !failed

    Box(Modifier.fillMaxSize()) {
        AuroraBackground(positive = positive, modifier = Modifier.fillMaxSize())

        if (engine == null) {
            ModelMissing()
        } else {
            RegistrationContent(viewModel, engine, done, failed, positive)
        }
    }
}

@Composable
private fun RegistrationContent(
    viewModel: RegistrationViewModel,
    engine: FaceDetectionEngine,
    done: Boolean,
    failed: Boolean,
    positive: Boolean,
) {
    DisposableEffect(engine) {
        onDispose { engine.close() }
    }

    // Berhasil: beri waktu sebentar untuk animasi centang, lalu langsung masuk.
    LaunchedEffect(done) {
        if (done) {
            delay(SUCCESS_HOLD_MS)
            viewModel.unlock()
        }
    }

    val analyzer = remember(engine, viewModel) {
        FaceFrameAnalyzer(engine, onError = { viewModel.onError(it) }) { frame, bitmap ->
            viewModel.onFrame(frame, bitmap)
        }
    }

    var showDebug by remember { mutableStateOf(SHOW_DEBUG_DEFAULT) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Header(onLogoLongPress = { showDebug = !showDebug })

        if (showDebug) {
            Spacer(Modifier.height(8.dp))
            DebugPanel(viewModel.debugInfo)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ScannerWindow(
                positive = positive,
                done = done,
                overlay = viewModel.overlay,
                analyzer = analyzer,
                modifier = Modifier
                    .fillMaxWidth(0.84f)
                    .aspectRatio(0.8f),
            )
            Spacer(Modifier.height(20.dp))
            StatusCard(
                messageRes = when {
                    done -> R.string.scan_success
                    failed -> R.string.scan_failed
                    else -> issueMessageRes(viewModel.issue)
                },
                failed = failed,
                onRetry = { viewModel.reset() },
            )
        }
    }
}

@Composable
private fun Header(onLogoLongPress: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BreathingLogo(size = 52.dp, onLongPress = onLogoLongPress)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = stringResource(R.string.app_name),
                style = TextStyle(
                    brush = GradientTitleBrush,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                ),
            )
            Text(
                text = stringResource(R.string.app_tagline),
                color = XColors.TextSecondary,
                fontSize = 11.sp,
                letterSpacing = 1.2.sp,
            )
        }
    }
}

@Composable
private fun DebugPanel(text: String) {
    Text(
        text = text,
        color = Color.Yellow,
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Black.copy(alpha = 0.55f))
            .padding(8.dp),
    )
}

/** Kartu kaca (glass) berisi pesan arahan. Tombol "Coba lagi" hanya muncul saat pindai gagal. */
@Composable
private fun StatusCard(messageRes: Int, failed: Boolean, onRetry: () -> Unit) {
    val shape = RoundedCornerShape(26.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.12f), shape)
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Crossfade(targetState = messageRes, label = "message") { res ->
            Text(
                text = stringResource(res),
                color = XColors.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
        }

        if (failed) {
            GradientButton(
                text = stringResource(R.string.registration_retry),
                onClick = onRetry,
            )
        }
    }
}

@Composable
private fun ModelMissing() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.model_load_failed),
            color = XColors.TextPrimary,
            textAlign = TextAlign.Center,
        )
    }
}
