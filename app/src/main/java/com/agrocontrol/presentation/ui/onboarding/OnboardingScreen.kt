package com.agrocontrol.presentation.ui.onboarding

import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agrocontrol.presentation.theme.*
import kotlinx.coroutines.launch

data class OnboardingPage(
    val emoji: String,
    val titulo: String,
    val descripcion: String,
    val gradiente: List<Color>
)

private val pages = listOf(
    OnboardingPage(
        emoji = "🌱",
        titulo = "Bienvenido a AgroControl",
        descripcion = "Tu asistente agrícola inteligente. Gestiona tus cultivos, monitorea el clima y toma decisiones basadas en datos reales.",
        gradiente = listOf(Color(0xFF1B4332), Color(0xFF2D6A4F))
    ),
    OnboardingPage(
        emoji = "🌤️",
        titulo = "Clima en tiempo real",
        descripcion = "Conectado a Open-Meteo: temperatura, lluvia y pronóstico de 7 días para tu región. Sin costo, sin API key.",
        gradiente = listOf(Color(0xFF1E3A5F), Color(0xFF2563EB))
    ),
    OnboardingPage(
        emoji = "🤖",
        titulo = "IA Agrícola con Groq",
        descripcion = "Predicción de rendimiento, recomendaciones de cultivo y alertas climáticas generadas por inteligencia artificial. Pregúntale cualquier cosa a tu asistente.",
        gradiente = listOf(Color(0xFF3B0764), Color(0xFF7C3AED))
    ),
    OnboardingPage(
        emoji = "📦",
        titulo = "Gestión de inventario",
        descripcion = "Lleva el control de fertilizantes, pesticidas y semillas. Recibe alertas cuando el stock esté por agotarse.",
        gradiente = listOf(Color(0xFF7C2D12), Color(0xFFF97316))
    ),
    OnboardingPage(
        emoji = "🚀",
        titulo = "Listo para empezar",
        descripcion = "Crea tu cuenta o inicia sesión. Tu campo, tu data, tu cosecha.",
        gradiente = listOf(Color(0xFF1B4332), Color(0xFF40916C))
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutine()
    val isLastPage = pagerState.currentPage == pages.lastIndex

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { pageIdx ->
            val page = pages[pageIdx]
            OnboardingPage(page = page)
        }

        // Bottom controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            // Indicadores de página
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(pages.size) { idx ->
                    val isSelected = pagerState.currentPage == idx
                    val width by animateDpAsState(
                        targetValue = if (isSelected) 28.dp else 8.dp,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "dot_width"
                    )
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(if (isSelected) Blanco else Blanco.copy(alpha = 0.4f))
                    )
                }
            }

            // Botón principal
            Button(
                onClick = {
                    if (isLastPage) onFinished()
                    else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Blanco,
                    contentColor   = Verde80
                )
            ) {
                Text(
                    text = if (isLastPage) "¡Comenzar ahora!" else "Siguiente",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    fontFamily = PlusJakartaSansFamily
                )
            }

            // Saltar (excepto última página)
            if (!isLastPage) {
                TextButton(onClick = onFinished) {
                    Text(
                        "Saltar",
                        color = Blanco.copy(alpha = 0.6f),
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingPage(page: OnboardingPage) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = page.gradiente)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp)
                .padding(bottom = 180.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = page.emoji,
                fontSize = 96.sp
            )

            Text(
                text = page.titulo,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Blanco,
                fontFamily = PlusJakartaSansFamily,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )

            Text(
                text = page.descripcion,
                fontSize = 15.sp,
                color = Blanco.copy(alpha = 0.82f),
                fontFamily = PlusJakartaSansFamily,
                textAlign = TextAlign.Center,
                lineHeight = 23.sp
            )
        }
    }
}

// Helper para el scope dentro de composable
@Composable
private fun rememberCoroutine() = rememberCoroutineScope()
