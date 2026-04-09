package com.agrocontrol.presentation.ui.clima

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agrocontrol.domain.model.PronosticoDia
import com.agrocontrol.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

private val ClimaBase    = Color(0xFF030F07)
private val ClimaCard    = Color(0xFF0C1E10)
private val ClimaBorder  = Color(0xFF1A3A1F)
private val GreenNeon    = Color(0xFF4ADE80)
private val TextPrimary  = Color(0xFFF0FFF4)
private val TextSecondary= Color(0xFF86EFAC)
private val TextMuted    = Color(0xFF4B7160)
private val BlueAccent   = Color(0xFF60A5FA)
private val AmberAccent  = Color(0xFFFBBF24)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClimaScreen(
    onBack: () -> Unit,
    viewModel: ClimaViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val sdf = remember { SimpleDateFormat("EEE dd", Locale("es", "CL")) }

    Scaffold(
        containerColor = ClimaBase,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Clima y pronóstico",
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = TextSecondary)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, null, tint = GreenNeon)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ClimaBase)
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = GreenNeon,
                        modifier = Modifier.size(40.dp),
                        strokeWidth = 3.dp
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("Obteniendo datos climáticos...", color = TextSecondary, fontFamily = PlusJakartaSansFamily)
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // ── Clima actual hero ──────────────────────────────────────────────
            state.climaActual?.let { clima ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .drawBehind {
                            drawRect(Brush.verticalGradient(listOf(Color(0xFF0D3B1C), Color(0xFF071409))))
                            // Atmospheric glow
                            drawCircle(
                                brush = Brush.radialGradient(
                                    listOf(Color(0xFF16A34A).copy(0.25f), Color.Transparent),
                                    center = Offset(size.width * 0.85f, size.height * 0.15f),
                                    radius = size.width * 0.5f
                                ),
                                radius = size.width * 0.5f,
                                center = Offset(size.width * 0.85f, size.height * 0.15f)
                            )
                            // Bottom glow
                            drawCircle(
                                brush = Brush.radialGradient(
                                    listOf(BlueAccent.copy(0.08f), Color.Transparent),
                                    center = Offset(size.width * 0.1f, size.height),
                                    radius = size.width * 0.4f
                                ),
                                radius = size.width * 0.4f,
                                center = Offset(size.width * 0.1f, size.height)
                            )
                        }
                ) {
                    Column(Modifier.padding(24.dp)) {
                        // Location
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = GreenNeon.copy(0.7f), modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                state.region,
                                color = TextSecondary.copy(0.7f),
                                fontSize = 12.sp,
                                fontFamily = PlusJakartaSansFamily,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.Top) {
                                    Text(
                                        "${clima.temperatura}",
                                        color = TextPrimary,
                                        fontSize = 80.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = PlusJakartaSansFamily,
                                        lineHeight = 80.sp
                                    )
                                    Text(
                                        "°C",
                                        color = GreenNeon,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Light,
                                        fontFamily = PlusJakartaSansFamily,
                                        modifier = Modifier.padding(top = 12.dp)
                                    )
                                }
                                Text(
                                    clima.descripcion,
                                    color = TextSecondary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = PlusJakartaSansFamily
                                )
                            }
                            Text(clima.icono, fontSize = 72.sp)
                        }

                        Spacer(Modifier.height(20.dp))

                        // Separator
                        Box(
                            Modifier.fillMaxWidth().height(1.dp)
                                .background(Brush.horizontalGradient(listOf(Color.Transparent, ClimaBorder, Color.Transparent)))
                        )

                        Spacer(Modifier.height(16.dp))

                        // Stats row
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ClimaStatItem("💧", "Humedad", "${clima.humedad}%", BlueAccent)
                            ClimaStatDivider()
                            ClimaStatItem("💨", "Viento", "${clima.viento} km/h", AmberAccent)
                            ClimaStatDivider()
                            ClimaStatItem("🌧️", "Precipit.", "${clima.precipitacion} mm", GreenNeon)
                        }

                        Spacer(Modifier.height(14.dp))

                        // Last update
                        val sdfUpdate = SimpleDateFormat("HH:mm", Locale.getDefault())
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(6.dp).background(GreenNeon, CircleShape))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Actualizado: ${sdfUpdate.format(Date(clima.ultimaActualizacion))} · Caché 1h",
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontFamily = PlusJakartaSansFamily
                            )
                        }
                    }
                }
            }

            // ── Pronóstico 7 días ──────────────────────────────────────────────
            if (state.pronostico.isNotEmpty()) {
                Column {
                    ClimaSection("📅 Pronóstico 7 días")
                    Spacer(Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(state.pronostico) { dia ->
                            PronosticoDiaCard(dia = dia, sdf = sdf)
                        }
                    }
                }
            }

            // ── Recomendaciones ────────────────────────────────────────────────
            if (state.recomendaciones.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ClimaSection("🤖 Recomendaciones para tu campo")
                    state.recomendaciones.forEach { rec ->
                        RecomendacionCard(rec = rec)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ClimaSection(title: String) {
    Text(
        title,
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = TextSecondary.copy(0.7f),
        letterSpacing = 0.3.sp
    )
}

@Composable
private fun ClimaStatItem(emoji: String, label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 18.sp)
        Spacer(Modifier.height(4.dp))
        Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 15.sp, fontFamily = PlusJakartaSansFamily)
        Text(label, color = TextMuted, fontSize = 10.sp, fontFamily = PlusJakartaSansFamily)
    }
}

@Composable
private fun ClimaStatDivider() {
    Box(Modifier.width(1.dp).height(40.dp).background(ClimaBorder))
}

@Composable
fun ClimaDataItem(emoji: String, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 20.sp)
        Text(value, color = TextPrimary, fontWeight = FontWeight.Bold)
        Text(label, color = TextMuted, style = MaterialTheme.typography.labelSmall, fontFamily = PlusJakartaSansFamily)
    }
}

@Composable
fun PronosticoDiaCard(dia: PronosticoDia, sdf: SimpleDateFormat) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(ClimaCard)
            .drawBehind {
                drawLine(color = ClimaBorder, start = Offset(0f, 0f), end = Offset(size.width, 0f), strokeWidth = 1f)
            }
    ) {
        Column(
            Modifier.padding(14.dp).width(72.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                sdf.format(Date(dia.fecha)).uppercase(),
                fontSize = 9.sp,
                color = TextMuted,
                fontFamily = PlusJakartaSansFamily,
                letterSpacing = 1.sp
            )
            Text(dia.icono, fontSize = 26.sp)
            Text("${dia.tempMax.toInt()}°", fontWeight = FontWeight.Bold, color = TextPrimary, fontFamily = PlusJakartaSansFamily)
            Text("${dia.tempMin.toInt()}°", fontSize = 12.sp, color = TextMuted, fontFamily = PlusJakartaSansFamily)
            if (dia.probabilidadLluvia > 20) {
                Box(
                    Modifier.clip(RoundedCornerShape(6.dp)).background(BlueAccent.copy(0.15f)).padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text("💧${dia.probabilidadLluvia}%", fontSize = 9.sp, color = BlueAccent, fontFamily = PlusJakartaSansFamily)
                }
            }
        }
    }
}

@Composable
fun RecomendacionCard(rec: com.agrocontrol.domain.model.RecomendacionCultivo) {
    val (colorAdecuacion, labelColor) = when (rec.nivelAdecuacion) {
        "alto"  -> Pair(GreenNeon, GreenNeon)
        "medio" -> Pair(AmberAccent, AmberAccent)
        else    -> Pair(Color(0xFFF87171), Color(0xFFF87171))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(ClimaCard)
            .drawBehind {
                drawLine(color = ClimaBorder, start = Offset(0f, 0f), end = Offset(size.width, 0f), strokeWidth = 1f)
                // Left accent bar
                drawRect(
                    brush = Brush.verticalGradient(listOf(colorAdecuacion.copy(0.7f), colorAdecuacion.copy(0.2f))),
                    topLeft = Offset(0f, 0f),
                    size = androidx.compose.ui.geometry.Size(3f, size.height)
                )
            }
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    rec.nombre,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    fontFamily = PlusJakartaSansFamily
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    "Rendimiento: ${rec.rendimientoEsperado}",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontFamily = PlusJakartaSansFamily
                )
                if (rec.riesgosClimaticos.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "⚠️ ${rec.riesgosClimaticos.joinToString(", ")}",
                        fontSize = 11.sp,
                        color = AmberAccent.copy(0.7f),
                        fontFamily = PlusJakartaSansFamily
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Box(
                Modifier.clip(RoundedCornerShape(10.dp)).background(colorAdecuacion.copy(0.12f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    rec.nivelAdecuacion.uppercase(),
                    color = labelColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = PlusJakartaSansFamily,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
