package com.xcoder.xfaceid.core.camera

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.UseCase
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.concurrent.Executors

private const val TAG = "XFaceCamera"

/**
 * Preview kamera berbasis CameraX di dalam Compose.
 *
 * Jika [analyzer] diberikan, ImageAnalysis ikut di-bind (RGBA_8888, hanya frame terbaru
 * yang diproses) dan dijalankan di satu thread latar. Frame TIDAK pernah disimpan ke disk.
 * Berikan [analyzer] yang stabil (dibungkus remember), karena perubahan instance
 * akan membuka ulang kamera.
 *
 * Izin CAMERA harus sudah diberikan sebelum composable ini dipanggil.
 */
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    lensFacing: Int = CameraSelector.LENS_FACING_FRONT,
    analyzer: ImageAnalysis.Analyzer? = null,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    DisposableEffect(lifecycleOwner, lensFacing, analyzer) {
        val analysisExecutor = if (analyzer != null) Executors.newSingleThreadExecutor() else null
        val providerFuture = ProcessCameraProvider.getInstance(context)
        var cameraProvider: ProcessCameraProvider? = null

        providerFuture.addListener(
            {
                try {
                    val provider = providerFuture.get()
                    cameraProvider = provider

                    val preview = Preview.Builder().build()
                    preview.setSurfaceProvider(previewView.surfaceProvider)

                    val useCases = mutableListOf<UseCase>(preview)
                    if (analyzer != null && analysisExecutor != null) {
                        val analysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                            .build()
                        analysis.setAnalyzer(analysisExecutor, analyzer)
                        useCases += analysis
                    }

                    val selector = CameraSelector.Builder()
                        .requireLensFacing(lensFacing)
                        .build()

                    provider.unbindAll()
                    provider.bindToLifecycle(lifecycleOwner, selector, *useCases.toTypedArray())
                } catch (e: Exception) {
                    // Contoh: perangkat tanpa kamera depan, atau kamera sedang dipakai app lain.
                    Log.e(TAG, "Gagal membuka kamera", e)
                }
            },
            ContextCompat.getMainExecutor(context),
        )

        onDispose {
            cameraProvider?.unbindAll()
            analysisExecutor?.shutdown()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier,
    )
}
