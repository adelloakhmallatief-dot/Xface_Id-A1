package com.xcoder.xfaceid.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xcoder.xfaceid.R
import kotlin.math.cos
import kotlin.math.sin

/**
 * Latar "aurora": cahaya lembut yang bergerak pelan. Warnanya bergeser ke hijau saat wajah bagus.
 * Semua animasi dibaca di fase gambar, jadi tidak memicu recomposition.
 */
@Composable
fun AuroraBackground(positive: Boolean, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "aurora")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(16000, easing = LinearEasing), RepeatMode.Restart),
        label = "aurora-t",
    )
    val accentA by animateColorAsState(
        targetValue = if (positive) XColors.Mint else XColors.Cyan,
        animationSpec = tween(700),
        label = "accent-a",
    )
    val accentB by animateColorAsState(
        targetValue = if (positive) XColors.Cyan else XColors.Violet,
        animationSpec = tween(700),
        label = "accent-b",
    )

    Canvas(modifier) {
        drawRect(XColors.Ink)
        val w = size.width
        val h = size.height

        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(accentA.copy(alpha = 0.34f), accentA.copy(alpha = 0f)),
                center = Offset(w * (0.22f + 0.14f * sin(t)), h * (0.16f + 0.06f * cos(t))),
                radius = w * 0.95f,
            ),
        )
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(accentB.copy(alpha = 0.30f), accentB.copy(alpha = 0f)),
                center = Offset(w * (0.82f + 0.12f * cos(t)), h * (0.84f + 0.05f * sin(t))),
                radius = w * 1.0f,
            ),
        )
    }
}

/** Logo XFace ID yang "bernapas" pelan. [onLongPress] dipakai untuk membuka panel debug. */
@Composable
fun BreathingLogo(
    size: Dp,
    modifier: Modifier = Modifier,
    onLongPress: (() -> Unit)? = null,
) {
    val transition = rememberInfiniteTransition(label = "logo")
    val breathe by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            tween(2200, easing = FastOutSlowInEasing),
            RepeatMode.Reverse,
        ),
        label = "breathe",
    )
    val gestures = if (onLongPress != null) {
        Modifier.pointerInput(Unit) {
            detectTapGestures(onLongPress = { onLongPress() })
        }
    } else {
        Modifier
    }
    Image(
        painter = painterResource(R.drawable.ic_logo_mark),
        contentDescription = null,
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = breathe
                scaleY = breathe
            }
            .then(gestures),
    )
}

@Composable
fun GradientButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Brush.horizontalGradient(listOf(XColors.Cyan, XColors.Violet)))
            .clickable(onClick = onClick)
            .padding(horizontal = 30.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = XColors.Ink,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
        )
    }
}

val GradientTitleBrush: Brush
    get() = Brush.horizontalGradient(listOf(XColors.Cyan, XColors.Violet))
