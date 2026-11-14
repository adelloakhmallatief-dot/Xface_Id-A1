package com.xcoder.xfaceid

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xcoder.xfaceid.ui.PermissionScreen
import com.xcoder.xfaceid.ui.RegistrationScreen
import com.xcoder.xfaceid.ui.XFaceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // UI gelap penuh layar: ikon status bar dan navigasi harus terang.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        setContent {
            // XFaceTheme, PermissionScreen, dan RegistrationScreen didefinisikan per edisi:
            // app/src/standard (UI dinamis) dan app/src/lite (UI minimalis).
            XFaceTheme {
                CameraGate()
            }
        }
    }
}

/** Minta izin kamera, lalu tampilkan alur registrasi wajah. */
@Composable
private fun CameraGate() {
    val context = LocalContext.current
    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    var denied by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        granted = isGranted
        denied = !isGranted
    }

    if (granted) {
        RegistrationScreen(viewModel())
    } else {
        PermissionScreen(
            denied = denied,
            onRequest = { launcher.launch(Manifest.permission.CAMERA) },
        )
    }
}
