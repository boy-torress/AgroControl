package com.agrocontrol.presentation.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agrocontrol.presentation.theme.PlusJakartaSansFamily

data class DonutChartData(
    val value: Float,
    val color: Color,
    val label: String
)

@Composable
fun DonutChart(
    data: List<DonutChartData>,
    modifier: Modifier = Modifier,
    thickness: Float = 40f,
    centerText: String = "",
    animationDuration: Int = 1200
) {
    var animationPlayed by remember { mutableStateOf(false) }

    val total = data.map { it.value }.sum()
    val proportions = data.map { if (total > 0) it.value * 100 / total else 0f }
    val sweepAngles = proportions.map { it * 360f / 100f }

    LaunchedEffect(key1 = true) {
        animationPlayed = true
    }

    val animation = animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(
            durationMillis = animationDuration,
            delayMillis = 0,
            easing = FastOutSlowInEasing
        ),
        label = "DonutChartAnimation"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            var currentAngle = -90f

            for (i in sweepAngles.indices) {
                val sweepAngle = sweepAngles[i] * animation.value
                drawArc(
                    color = data[i].color,
                    startAngle = currentAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = thickness, cap = StrokeCap.Round)
                )
                currentAngle += sweepAngle
            }
        }
        
        if (centerText.isNotEmpty()) {
            Text(
                text = centerText,
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White
            )
        }
    }
}
