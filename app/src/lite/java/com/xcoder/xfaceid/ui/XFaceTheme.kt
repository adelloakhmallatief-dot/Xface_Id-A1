package com.xcoder.xfaceid.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/** Tema XFace ID Lite: gelap, datar, tanpa efek tambahan. */
@Composable
fun XFaceTheme(content: @Composable () -> Unit) {
    val ink = Color(0xFF060B14)
    val scheme = darkColorScheme(
        primary = Color(0xFF86DDEB),
        onPrimary = ink,
        background = ink,
        onBackground = Color(0xFFEAF6FA),
        surface = Color(0xFF0E1726),
        onSurface = Color(0xFFEAF6FA),
    )
    MaterialTheme(colorScheme = scheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = ink,
            content = content,
        )
    }
}
