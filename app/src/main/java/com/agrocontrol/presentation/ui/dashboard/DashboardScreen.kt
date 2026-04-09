package com.agrocontrol.presentation.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agrocontrol.domain.model.Cultivo
import com.agrocontrol.presentation.theme.*
import java.util.Calendar

private val DashBg        = Color(0xFF030F07)
private val DashSurface   = Color(0xFF071409)
private val DashCard      = Color(0xFF0C1E10)
private val DashBorder    = Color(0xFF1A3A1F)
private val GreenNeon     = Color(0xFF4ADE80)
private val GreenDeep     = Color(0xFF166534)
private val TextOnDark    = Color(0xFFF0FFF4)
private val TextSubtle    = Color(0xFF86EFAC)
private val TextMuted     = Color(0xFF4B7160)
private val AmberAccent   = Color(0xFFF59E0B)
private val BlueAccent    = Color(0xFF38BDF8)
private val RedAccent     = Color(0xFFF87171)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToCultivo: () -> Unit,
    onNavigateToClima: () -> Unit,
    onNavigateToInventario: () -> Unit,
    onNavigateToAlertas: () -> Unit,
    onNavigateToCalculadora: () -> Unit = {},
    onNavigateToPerfil: () -> Unit = {},
    onLogout: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DashBg)
            .verticalScroll(rememberScrollState())
    ) {
        DashboardHero(
            userName      = state.userName,
            alertasCount  = state.alertasCount,
            onAlertas     = onNavigateToAlertas,
            onPerfil      = onNavigateToPerfil
        )

        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // Clima card
            ClimaCardPremium(state = state, onClick = onNavigateToClima)

            // Cultivo activo
            DashSectionLabel("🌾 Tu Cultivo Activo")
            Spacer(Modifier.height(-6.dp))
            CultivoActivoCardPremium(cultivo = state.cultivoActivo, onClick = onNavigateToCultivo)

            // Quick access grid
            DashSectionLabel("⚡ Accesos Rápidos")
            Spacer(Modifier.height(-6.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickCard(
                    icon    = Icons.Outlined.Inventory2,
                    label   = "Inventario",
                    badge   = if (state.stockCriticoCount > 0) "${state.stockCriticoCount}" else null,
                    color   = BlueAccent,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToInventario
                )
                QuickCard(
                    icon    = Icons.Outlined.NotificationsActive,
                    label   = "Alertas",
                    badge   = if (state.alertasCount > 0) "${state.alertasCount}" else null,
                    color   = AmberAccent,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToAlertas
                )
                QuickCard(
                    icon    = Icons.Outlined.Calculate,
                    label   = "ROI",
                    badge   = null,
                    color   = GreenNeon,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToCalculadora
                )
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

// ─── Hero Header ───────────────────────────────────────────────────────────────
@Composable
private fun DashboardHero(
    userName: String,
    alertasCount: Int,
    onAlertas: () -> Unit,
    onPerfil: () -> Unit
) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> "Buenos días"
        hour < 18 -> "Buenas tardes"
        else      -> "Buenas noches"
    }
    val greetingEmoji = when {
        hour < 12 -> "🌅"
        hour < 18 -> "☀️"
        else      -> "🌙"
    }

    val infiniteTransition = rememberInfiniteTransition(label = "hero")
    val shimmer by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3500, easing = LinearEasing)),
        label = "shimmer"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                // Deep forest background
                drawRect(Brush.verticalGradient(listOf(Color(0xFF071A0B), Color(0xFF040E06))))

                // Geometric accent lines
                drawLine(
                    Brush.horizontalGradient(listOf(Color.Transparent, GreenNeon.copy(0.25f), Color.Transparent)),
                    start = Offset(0f, size.height * 0.9f),
                    end   = Offset(size.width, size.height * 0.9f),
                    strokeWidth = 1f
                )

                // Ambient glow top-left
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0xFF14532D).copy(0.5f), Color.Transparent),
                        center = Offset(0f, 0f),
                        radius = size.width * 0.6f
                    ),
                    radius = size.width * 0.6f,
                    center = Offset(0f, 0f)
                )
            }
            .padding(top = 52.dp, bottom = 24.dp, start = 20.dp, end = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(greetingEmoji, fontSize = 16.sp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        greeting,
                        color = TextSubtle.copy(0.7f),
                        fontSize = 13.sp,
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    userName.split(" ").firstOrNull() ?: "Agricultor",
                    color = TextOnDark,
                    fontSize = 30.sp,
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                val ctx = androidx.compose.ui.platform.LocalContext.current

                IconButton(
                    onClick = {
                        com.agrocontrol.domain.util.PdfGenerator.generateAndShareCultivoReport(
                            context = ctx,
                            cultivo = com.agrocontrol.domain.model.Cultivo(
                                id = 0, agricultorId = 0, tipoCultivo = "Cultivo Activo",
                                hectareas = 0.0, fechaSiembra = System.currentTimeMillis(),
                                region = "Mi Región",
                                etapaActual = com.agrocontrol.domain.model.EtapaCultivo.GERMINACION
                            ),
                            climaContexto = "Reporte ${Calendar.getInstance().get(Calendar.HOUR_OF_DAY)}:00 hs.",
                            alertasActivas = alertasCount,
                            userName = userName
                        )
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF0F2714), CircleShape)
                ) {
                    Icon(Icons.Default.PictureAsPdf, null, tint = TextSubtle.copy(0.7f), modifier = Modifier.size(18.dp))
                }

                BadgedBox(
                    badge = {
                        if (alertasCount > 0) Badge(
                            containerColor = RedAccent,
                            contentColor = Color.White
                        ) { Text("$alertasCount", fontSize = 9.sp) }
                    }
                ) {
                    IconButton(
                        onClick = onAlertas,
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFF0F2714), CircleShape)
                    ) {
                        Icon(Icons.Default.Notifications, null, tint = AmberAccent, modifier = Modifier.size(20.dp))
                    }
                }

                IconButton(
                    onClick = onPerfil,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Brush.radialGradient(listOf(GreenDeep, Color(0xFF052E16))),
                            CircleShape
                        )
                ) {
                    Icon(Icons.Default.Person, null, tint = GreenNeon, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

// ─── Clima Card Premium ────────────────────────────────────────────────────────
@Composable
fun ClimaCardPremium(state: DashboardUiState, onClick: () -> Unit) {
    val c = state.clima

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .drawBehind {
                val hours = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                val grad = when {
                    hours in 6..11  -> listOf(Color(0xFF0D3B1C), Color(0xFF0A2F18))
                    hours in 12..17 -> listOf(Color(0xFF0B1E3B), Color(0xFF0A1A30))
                    else            -> listOf(Color(0xFF0A0D18), Color(0xFF080C15))
                }
                drawRect(Brush.verticalGradient(grad))
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(GreenNeon.copy(0.12f), Color.Transparent),
                        center = Offset(size.width * 0.1f, size.height * 0.3f),
                        radius = size.width * 0.4f
                    ),
                    radius = size.width * 0.4f,
                    center = Offset(size.width * 0.1f, size.height * 0.3f)
                )
            }
    ) {
        if (c != null) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = GreenNeon.copy(0.7f), modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(3.dp))
                        Text(
                            state.cultivoActivo?.region ?: "Sin Región",
                            color = TextSubtle.copy(0.7f),
                            fontSize = 11.sp,
                            fontFamily = PlusJakartaSansFamily,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            "${c.temperatura}",
                            color = TextOnDark,
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = PlusJakartaSansFamily,
                            lineHeight = 64.sp
                        )
                        Text(
                            "°C",
                            color = GreenNeon.copy(0.8f),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Light,
                            fontFamily = PlusJakartaSansFamily,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                    }
                    Text(
                        c.descripcion,
                        color = TextSubtle.copy(0.85f),
                        fontSize = 13.sp,
                        fontFamily = PlusJakartaSansFamily
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MiniClimaChip("💧", "${c.humedad}%")
                        MiniClimaChip("💨", "${c.viento} km/h")
                    }
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(c.icono, fontSize = 56.sp)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GreenNeon.copy(0.12f)
                    ) {
                        Row(
                            Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Ver más", color = GreenNeon, fontSize = 10.sp, fontFamily = PlusJakartaSansFamily)
                            Icon(Icons.Default.ChevronRight, null, tint = GreenNeon, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }
        } else {
            Row(
                Modifier.fillMaxWidth().padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🌤️", fontSize = 32.sp)
                Spacer(Modifier.width(14.dp))
                Text(
                    "Registra un cultivo para ver el clima de tu región",
                    color = TextSubtle.copy(0.7f),
                    fontFamily = PlusJakartaSansFamily,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun MiniClimaChip(emoji: String, value: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.White.copy(0.07f)
    ) {
        Text(
            "$emoji $value",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = TextOnDark.copy(0.85f),
            fontSize = 11.sp,
            fontFamily = PlusJakartaSansFamily
        )
    }
}

// ─── Cultivo Card ──────────────────────────────────────────────────────────────
@Composable
fun CultivoActivoCardPremium(cultivo: Cultivo?, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .background(DashCard)
            .drawBehind {
                drawLine(
                    color = DashBorder,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1f
                )
            }
    ) {
        if (cultivo == null) {
            Row(
                Modifier.fillMaxWidth().padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(GreenNeon.copy(0.08f)),
                    contentAlignment = Alignment.Center
                ) { Text("🌱", fontSize = 26.sp) }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Sin cultivo activo", fontWeight = FontWeight.SemiBold, color = TextOnDark, fontFamily = PlusJakartaSansFamily)
                    Text("Toca para registrar tu primer cultivo", fontSize = 12.sp, color = GreenNeon, fontFamily = PlusJakartaSansFamily)
                }
                Icon(Icons.Default.ChevronRight, null, tint = TextMuted)
            }
        } else {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(52.dp).clip(RoundedCornerShape(16.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFF166534), Color(0xFF15803D)))),
                        contentAlignment = Alignment.Center
                    ) { Text("🌾", fontSize = 24.sp) }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            cultivo.tipoCultivo,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = GreenNeon,
                            fontFamily = PlusJakartaSansFamily
                        )
                        Text(
                            "${cultivo.region} · ${cultivo.hectareas} ha",
                            fontSize = 12.sp,
                            color = TextMuted,
                            fontFamily = PlusJakartaSansFamily
                        )
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = TextMuted, modifier = Modifier.size(20.dp))
                }

                Spacer(Modifier.height(16.dp))

                val etapas = com.agrocontrol.domain.model.EtapaCultivo.values()
                val idx = etapas.indexOf(cultivo.etapaActual)
                val progress by animateFloatAsState(
                    targetValue = (idx + 1).toFloat() / etapas.size,
                    animationSpec = tween(900, easing = FastOutSlowInEasing),
                    label = "etapa"
                )

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        "Etapa: ${cultivo.etapaActual.name}",
                        fontSize = 11.sp,
                        color = TextMuted,
                        fontFamily = PlusJakartaSansFamily
                    )
                    Text(
                        "${idx + 1} / ${etapas.size}",
                        fontSize = 11.sp,
                        color = GreenNeon,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PlusJakartaSansFamily
                    )
                }
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape)
                        .background(GreenNeon.copy(0.1f))
                ) {
                    Box(
                        modifier = Modifier.fillMaxHeight().fillMaxWidth(progress).clip(CircleShape)
                            .background(Brush.horizontalGradient(listOf(Color(0xFF4ADE80), Color(0xFF22C55E))))
                    )
                }
            }
        }
    }
}

// ─── Quick Card ────────────────────────────────────────────────────────────────
@Composable
fun QuickCard(
    icon: ImageVector,
    label: String,
    badge: String?,
    color: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .background(DashCard)
            .drawBehind {
                drawLine(color = DashBorder, start = Offset(0f, 0f), end = Offset(size.width, 0f), strokeWidth = 1f)
            }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BadgedBox(badge = {
                if (badge != null) Badge(containerColor = RedAccent, contentColor = Color.White) {
                    Text(badge, fontSize = 8.sp, fontFamily = PlusJakartaSansFamily)
                }
            }) {
                Box(
                    Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(color.copy(0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
                }
            }
            Text(
                label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = PlusJakartaSansFamily,
                color = TextSubtle.copy(0.8f)
            )
        }
    }
}

// ─── Helpers ───────────────────────────────────────────────────────────────────
@Composable
fun DashSectionLabel(text: String) {
    Text(
        text,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        fontFamily = PlusJakartaSansFamily,
        color = TextSubtle.copy(0.7f),
        letterSpacing = 0.3.sp
    )
}

// Backwards-compat aliases
@Composable
fun SectionLabel(text: String) = DashSectionLabel(text)

@Composable
fun ClimaCard(state: DashboardUiState, onClick: () -> Unit, onRefresh: () -> Unit = {}) = ClimaCardPremium(state, onClick)

@Composable
fun CultivoActivoCard(cultivo: Cultivo?, onClick: () -> Unit) = CultivoActivoCardPremium(cultivo, onClick)

@Composable
fun InfoChip(text: String) {
    Surface(shape = RoundedCornerShape(8.dp), color = GreenNeon.copy(0.1f)) {
        Text(text, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall, fontFamily = PlusJakartaSansFamily, color = GreenNeon)
    }
}

@Composable
fun QuickAccessCard(icon: ImageVector, title: String, subtitle: String, hasAlert: Boolean, modifier: Modifier, onClick: () -> Unit) =
    QuickCard(icon = icon, label = title, badge = if (hasAlert) "!" else null,
        color = if (hasAlert) RedAccent else BlueAccent, modifier = modifier, onClick = onClick)
