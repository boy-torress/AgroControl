package com.agrocontrol.presentation.ui.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agrocontrol.presentation.theme.*
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

data class OnboardingPage(
    val emoji: String,
    val titulo: String,
    val descripcion: String,
    val gradiente: List<Color>,
    val accentColor: Color
)

private val pages = listOf(
    OnboardingPage(
        emoji = "🌱",
        titulo = "Bienvenido a AgroControl",
        descripcion = "Tu asistente agrícola inteligente. Gestiona tus cultivos, monitorea el clima y toma decisiones basadas en datos reales.",
        gradiente = listOf(Color(0xFF052E16), Color(0xFF14532D), Color(0xFF166534)),
        accentColor = Color(0xFF4ADE80)
    ),
    OnboardingPage(
        emoji = "🌤️",
        titulo = "Clima en tiempo real",
        descripcion = "Conectado a Open-Meteo: temperatura, lluvia y pronóstico de 7 días para tu región. Sin costo, sin API key.",
        gradiente = listOf(Color(0xFF0C1A2E), Color(0xFF1E3A5F), Color(0xFF1D4ED8)),
        accentColor = Color(0xFF60A5FA)
    ),
    OnboardingPage(
        emoji = "🤖",
        titulo = "IA Agrícola con Groq",
        descripcion = "Predicción de rendimiento, recomendaciones de cultivo y alertas climáticas generadas por inteligencia artificial.",
        gradiente = listOf(Color(0xFF1C0533), Color(0xFF3B0764), Color(0xFF6D28D9)),
        accentColor = Color(0xFFA78BFA)
    ),
    OnboardingPage(
        emoji = "📦",
        titulo = "Gestión de inventario",
        descripcion = "Lleva el control de fertilizantes, pesticidas y semillas. Recibe alertas cuando el stock esté por agotarse.",
        gradiente = listOf(Color(0xFF431407), Color(0xFF7C2D12), Color(0xFFC2410C)),
        accentColor = Color(0xFFFB923C)
    ),
    OnboardingPage(
        emoji = "🚀",
        titulo = "Listo para empezar",
        descripcion = "Crea tu cuenta o inicia sesión. Tu campo, tu data, tu cosecha.",
        gradiente = listOf(Color(0xFF052E16), Color(0xFF14532D), Color(0xFF15803D)),
        accentColor = Color(0xFF4ADE80)
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.lastIndex

    val infiniteTransition = rememberInfiniteTransition(label = "onboard")
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -10f, targetValue = 10f,
        animationSpec = infiniteRepeatable(tween(2800, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "float"
    )
    val particleRotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(18000, easing = LinearEasing)),
        label = "particleRot"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { pageIdx ->
            val page = pages[pageIdx]
            PageContent(
                page = page,
                floatOffset = floatAnim,
                particleRotation = particleRotation
            )
        }

        // Bottom controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 28.dp, vertical = 52.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Page indicator dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pages.size) { idx ->
                    val isSelected = pagerState.currentPage == idx
                    val width by animateDpAsState(
                        targetValue = if (isSelected) 32.dp else 6.dp,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "dot_width"
                    )
                    val alpha by animateFloatAsState(
                        targetValue = if (isSelected) 1f else 0.35f,
                        label = "dot_alpha"
                    )
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(width)
                            .clip(CircleShape)
                            .alpha(alpha)
                            .background(Color.White)
                    )
                }
            }

            // CTA button
            Button(
                onClick = {
                    if (isLastPage) onFinished()
                    else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = pages[pagerState.currentPage].gradiente.last()
                ),
                elevation = ButtonDefaults.buttonElevation(8.dp)
            ) {
                Text(
                    text = if (isLastPage) "¡Comenzar ahora!" else "Continuar",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    fontFamily = PlusJakartaSansFamily,
                    letterSpacing = 0.5.sp
                )
            }

            if (!isLastPage) {
                TextButton(onClick = onFinished) {
                    Text(
                        "Omitir",
                        color = Color.White.copy(alpha = 0.5f),
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )
                }
            } else {
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun PageContent(
    page: OnboardingPage,
    floatOffset: Float,
    particleRotation: Float
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = page.gradiente)),
        contentAlignment = Alignment.Center
    ) {
        // Decorative particle canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height * 0.35f

            // Ambient glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(page.accentColor.copy(alpha = 0.18f), Color.Transparent),
                    center = Offset(cx, cy),
                    radius = size.width * 0.55f
                ),
                radius = size.width * 0.55f,
                center = Offset(cx, cy)
            )

            // Orbiting particles
            val orbitRadius = size.width * 0.32f
            val particleCount = 8
            repeat(particleCount) { i ->
                val angle = Math.toRadians((particleRotation + i * (360f / particleCount)).toDouble())
                val x = cx + (orbitRadius * cos(angle)).toFloat()
                val y = cy + (orbitRadius * sin(angle)).toFloat()
                val isAccent = i % 2 == 0
                drawCircle(
                    color = if (isAccent) page.accentColor.copy(alpha = 0.7f)
                            else Color.White.copy(alpha = 0.25f),
                    radius = if (isAccent) 4f else 2.5f,
                    center = Offset(x, y)
                )
            }

            // Subtle horizontal line
            drawLine(
                brush = Brush.horizontalGradient(
                    listOf(Color.Transparent, page.accentColor.copy(alpha = 0.3f), Color.Transparent)
                ),
                start = Offset(0f, cy + 90f),
                end = Offset(size.width, cy + 90f),
                strokeWidth = 1f
            )
        }

        // Page content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 36.dp)
                .padding(bottom = 220.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            // Floating emoji container
            Box(
                modifier = Modifier
                    .offset(y = floatOffset.dp)
                    .size(120.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                page.accentColor.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(page.emoji, fontSize = 64.sp)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = page.titulo,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontFamily = PlusJakartaSansFamily,
                    textAlign = TextAlign.Center,
                    lineHeight = 34.sp
                )

                // Accent underline
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(3.dp)
                        .clip(CircleShape)
                        .background(page.accentColor)
                )

                Text(
                    text = page.descripcion,
                    fontSize = 15.sp,
                    color = Color.White.copy(alpha = 0.75f),
                    fontFamily = PlusJakartaSansFamily,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
            }
        }
    }
}

// Helper
@Composable
private fun rememberCoroutine() = rememberCoroutineScope()
