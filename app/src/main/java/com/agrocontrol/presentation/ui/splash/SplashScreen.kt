package com.agrocontrol.presentation.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agrocontrol.presentation.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {

    var startAnim by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0.6f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "alpha"
    )
    val taglineAlpha by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(900, delayMillis = 400, easing = FastOutSlowInEasing),
        label = "taglineAlpha"
    )

    LaunchedEffect(Unit) {
        startAnim = true
        delay(2200)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Verde80, Verde60, Color(0xFF1B4332))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo emoji con escala animada
            Text(
                text = "🌿",
                fontSize = 80.sp,
                modifier = Modifier
                    .scale(scale)
                    .alpha(alpha)
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = "AgroControl",
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Blanco,
                fontFamily = PlusJakartaSansFamily,
                modifier = Modifier
                    .scale(scale)
                    .alpha(alpha)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Agricultura inteligente",
                fontSize = 15.sp,
                color = Blanco.copy(alpha = 0.7f),
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp,
                modifier = Modifier.alpha(taglineAlpha)
            )
        }

        // Versión en el fondo
        Text(
            text = "v1.0",
            color = Blanco.copy(alpha = 0.3f),
            fontSize = 11.sp,
            fontFamily = PlusJakartaSansFamily,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .alpha(taglineAlpha)
        )
    }
}
