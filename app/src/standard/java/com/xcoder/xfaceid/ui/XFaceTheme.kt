package com.xcoder.xfaceid.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/** Palet XFace ID Standard. Cyan diambil dari warna sketsa logo. */
object XColors {
    val Ink = Color(0xFF060B14)
    val Panel = Color(0xFF0E1726)
    val Cyan = Color(0xFF86DDEB)
    val Violet = Color(0xFF8B7CFF)
    val Mint = Color(0xFF4DF0B0)
    val Rose = Color(0xFFFF6B7A)
    val TextPrimary = Color(0xFFEAF6FA)
    val TextSecondary = Color(0xFF9FB4BF)
}

@Composable
fun XFaceTheme(content: @Composable () -> Unit) {
    val scheme = darkColorScheme(
        primary = XColors.Cyan,
        onPrimary = XColors.Ink,
        secondary = XColors.Violet,
        tertiary = XColors.Mint,
        background = XColors.Ink,
        onBackground = XColors.TextPrimary,
        surface = XColors.Panel,
        onSurface = XColors.TextPrimary,
        error = XColors.Rose,
    )
    MaterialTheme(colorScheme = scheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = XColors.Ink,
            content = content,
        )
    }
}
