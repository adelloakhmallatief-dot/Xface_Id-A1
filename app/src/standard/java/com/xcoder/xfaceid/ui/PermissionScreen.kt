package com.xcoder.xfaceid.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xcoder.xfaceid.R

@Composable
fun PermissionScreen(denied: Boolean, onRequest: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        AuroraBackground(positive = false, modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BreathingLogo(size = 128.dp)
            Spacer(Modifier.height(28.dp))
            Text(
                text = stringResource(R.string.camera_permission_title),
                style = TextStyle(
                    brush = GradientTitleBrush,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                ),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(
                    if (denied) R.string.camera_permission_denied else R.string.camera_permission_body,
                ),
                color = XColors.TextSecondary,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(28.dp))
            GradientButton(
                text = stringResource(R.string.camera_permission_button),
                onClick = onRequest,
            )
        }
    }
}
