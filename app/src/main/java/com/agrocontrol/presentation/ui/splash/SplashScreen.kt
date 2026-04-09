package com.agrocontrol.presentation.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agrocontrol.presentation.theme.*
import kotlinx.coroutines.delay
import kotlin.math.*

@Composable
fun SplashScreen(onFinished: () -> Unit) {

    var phase by remember { mutableStateOf(0) } // 0=idle, 1=animating, 2=done

    // Core animation values
    val infiniteTransition = rememberInfiniteTransition(label = "splash")

    val particleAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing)),
        label = "particleAngle"
    )

    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "glowPulse"
    )

    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -360f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing)),
        label = "ringRotation"
    )

    // Entry animations
    val logoScale by animateFloatAsState(
        targetValue = if (phase >= 1) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "logoScale"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (phase >= 1) 1f else 0f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "logoAlpha"
    )
    val titleAlpha by animateFloatAsState(
        targetValue = if (phase >= 1) 1f else 0f,
        animationSpec = tween(800, delayMillis = 300, easing = FastOutSlowInEasing),
        label = "titleAlpha"
    )
    val titleOffsetY by animateFloatAsState(
        targetValue = if (phase >= 1) 0f else 40f,
        animationSpec = tween(800, delayMillis = 300, easing = FastOutSlowInEasing),
        label = "titleOffsetY"
    )
    val taglineAlpha by animateFloatAsState(
        targetValue = if (phase >= 1) 1f else 0f,
        animationSpec = tween(700, delayMillis = 700, easing = FastOutSlowInEasing),
        label = "taglineAlpha"
    )
    val bottomAlpha by animateFloatAsState(
        targetValue = if (phase >= 1) 1f else 0f,
        animationSpec = tween(600, delayMillis = 1000, easing = FastOutSlowInEasing),
        label = "bottomAlpha"
    )

    LaunchedEffect(Unit) {
        delay(100)
        phase = 1
        delay(2500)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030F07)),
        contentAlignment = Alignment.Center
    ) {

        // Ambient glow canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawAmbientGlow(glowPulse, particleAngle, ringRotation)
        }

        // Central content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Logo container with orbital ring
            Box(
                modifier = Modifier.size(140.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer orbital ring canvas
                Canvas(modifier = Modifier.fillMaxSize().alpha(logoAlpha)) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.width / 2f - 4f

                    // Glow ring
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF4ADE80).copy(alpha = 0.25f * glowPulse),
                                Color.Transparent
                            ),
                            center = center,
                            radius = radius + 20f
                        ),
                        radius = radius + 20f,
                        center = center
                    )

                    // Dashed orbital ring
                    val dashCount = 24
                    repeat(dashCount) { i ->
                        val angle = Math.toRadians((ringRotation + i * (360f / dashCount)).toDouble())
                        val x = center.x + radius * cos(angle).toFloat()
                        val y = center.y + radius * sin(angle).toFloat()
                        drawCircle(
                            color = if (i % 3 == 0) Color(0xFF4ADE80).copy(alpha = 0.9f)
                                    else Color(0xFF4ADE80).copy(alpha = 0.3f),
                            radius = if (i % 3 == 0) 3.5f else 2f,
                            center = Offset(x, y)
                        )
                    }

                    // Inner glow circle
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF16A34A).copy(alpha = 0.4f),
                                Color(0xFF052E16).copy(alpha = 0.9f)
                            ),
                            center = center,
                            radius = radius * 0.8f
                        ),
                        radius = radius * 0.85f,
                        center = center
                    )
                }

                // Logo emoji
                Text(
                    text = "🌿",
                    fontSize = 56.sp,
                    modifier = Modifier
                        .scale(logoScale)
                        .alpha(logoAlpha)
                )
            }

            Spacer(Modifier.height(32.dp))

            // Brand name with letter spacing
            Text(
                text = "AGROCONTROL",
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 6.sp,
                fontFamily = PlusJakartaSansFamily,
                modifier = Modifier
                    .alpha(titleAlpha)
                    .offset(y = titleOffsetY.dp)
            )

            Spacer(Modifier.height(10.dp))

            // Separator line
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(2.dp)
                    .alpha(taglineAlpha)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, Color(0xFF4ADE80), Color.Transparent)
                        )
                    )
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Agricultura inteligente",
                fontSize = 13.sp,
                color = Color(0xFF86EFAC),
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.Light,
                letterSpacing = 2.sp,
                modifier = Modifier.alpha(taglineAlpha)
            )
        }

        // Bottom particles + version
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
                .alpha(bottomAlpha),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated dots loader
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(3) { i ->
                    val dotAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.2f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            keyframes {
                                durationMillis = 1200
                                0.2f at 0
                                1f at 300 + i * 180
                                0.2f at 900 + i * 60
                            }
                        ),
                        label = "dot$i"
                    )
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .alpha(dotAlpha)
                            .background(Color(0xFF4ADE80), shape = androidx.compose.foundation.shape.CircleShape)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "v1.0",
                color = Color.White.copy(alpha = 0.2f),
                fontSize = 11.sp,
                fontFamily = PlusJakartaSansFamily
            )
        }
    }
}

private fun DrawScope.drawAmbientGlow(glowPulse: Float, particleAngle: Float, ringRotation: Float) {
    val centerX = size.width / 2f
    val centerY = size.height / 2f

    // Large background glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF14532D).copy(alpha = 0.5f * glowPulse),
                Color.Transparent
            ),
            center = Offset(centerX, centerY),
            radius = size.width * 0.7f
        ),
        radius = size.width * 0.7f,
        center = Offset(centerX, centerY)
    )

    // Top-right accent
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF065F46).copy(alpha = 0.3f),
                Color.Transparent
            ),
            center = Offset(size.width * 0.85f, size.height * 0.15f),
            radius = size.width * 0.35f
        ),
        radius = size.width * 0.35f,
        center = Offset(size.width * 0.85f, size.height * 0.15f)
    )

    // Bottom-left accent
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF052E16).copy(alpha = 0.8f),
                Color.Transparent
            ),
            center = Offset(size.width * 0.1f, size.height * 0.85f),
            radius = size.width * 0.4f
        ),
        radius = size.width * 0.4f,
        center = Offset(size.width * 0.1f, size.height * 0.85f)
    )

    // Floating micro-particles
    val particlePositions = listOf(
        Offset(centerX + 200f, centerY - 100f),
        Offset(centerX - 180f, centerY + 80f),
        Offset(centerX + 80f, centerY - 220f),
        Offset(centerX - 100f, centerY + 200f),
        Offset(centerX + 150f, centerY + 160f),
        Offset(centerX - 220f, centerY - 140f),
    )
    particlePositions.forEachIndexed { i, base ->
        val angle = Math.toRadians((particleAngle + i * 60f).toDouble())
        val orbitRadius = 20f + i * 8f
        val x = base.x + (orbitRadius * cos(angle)).toFloat()
        val y = base.y + (orbitRadius * sin(angle)).toFloat()
        drawCircle(
            color = Color(0xFF4ADE80).copy(alpha = 0.15f + 0.1f * ((i % 3) / 3f)),
            radius = 2f + (i % 3).toFloat(),
            center = Offset(x, y)
        )
    }
}
