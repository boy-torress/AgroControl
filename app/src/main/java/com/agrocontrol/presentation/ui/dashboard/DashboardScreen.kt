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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agrocontrol.domain.model.Cultivo
import com.agrocontrol.presentation.theme.*
import java.util.Calendar

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
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Hero Header ────────────────────────────────────────────────────────
        DashboardHero(
            userName   = state.userName,
            alertasCount = state.alertasCount,
            onAlertas  = onNavigateToAlertas,
            onPerfil   = onNavigateToPerfil
        )

        // ── Clima card dinámica ────────────────────────────────────────────────
        Box(Modifier.padding(horizontal = 16.dp)) {
            ClimaCardPremium(state = state, onClick = onNavigateToClima)
        }

        Spacer(Modifier.height(20.dp))

        // ── Cultivo activo ─────────────────────────────────────────────────────
        Column(Modifier.padding(horizontal = 16.dp)) {
            SectionLabel("🌾 Tu Cultivo")
            Spacer(Modifier.height(8.dp))
            CultivoActivoCardPremium(cultivo = state.cultivoActivo, onClick = onNavigateToCultivo)
        }

        Spacer(Modifier.height(20.dp))

        // ── Accesos rápidos ────────────────────────────────────────────────────
        Column(Modifier.padding(horizontal = 16.dp)) {
            SectionLabel("⚡ Accesos rápidos")
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickCard(
                    icon     = Icons.Outlined.Inventory2,
                    label    = "Inventario",
                    badge    = if (state.stockCriticoCount > 0) "${state.stockCriticoCount}" else null,
                    color    = AzulInfo,
                    modifier = Modifier.weight(1f),
                    onClick  = onNavigateToInventario
                )
                QuickCard(
                    icon     = Icons.Outlined.NotificationsActive,
                    label    = "Alertas",
                    badge    = if (state.alertasCount > 0) "${state.alertasCount}" else null,
                    color    = AmarilloAlert,
                    modifier = Modifier.weight(1f),
                    onClick  = onNavigateToAlertas
                )
                QuickCard(
                    icon     = Icons.Outlined.Calculate,
                    label    = "Calculadora",
                    badge    = null,
                    color    = VerdeAccent,
                    modifier = Modifier.weight(1f),
                    onClick  = onNavigateToCalculadora
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Offline banner now handled globally by MainScaffold

        Spacer(Modifier.height(32.dp))
    }
}

// ─── Hero Header ──────────────────────────────────────────────────────────────
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
    val gradiente = when {
        hour in 6..11  -> listOf(Color(0xFF1B4332), Verde60)          // Mañana verde
        hour in 12..17 -> listOf(Color(0xFF1E3A5F), AzulInfo)         // Tarde azul
        else            -> listOf(Color(0xFF0D1117), Color(0xFF1F2937)) // Noche oscuro
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(gradiente))
            .padding(top = 48.dp, bottom = 28.dp, start = 20.dp, end = 20.dp)
    ) {
        Column {
            Text(
                greeting,
                color = Blanco.copy(0.7f),
                fontSize = 14.sp,
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.Medium
            )
            Text(
                userName.split(" ").firstOrNull() ?: "Agricultor",
                color = Blanco,
                fontSize = 28.sp,
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.ExtraBold
            )
        }

        // Acciones top-right
        Row(
            modifier = Modifier.align(Alignment.TopEnd),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val ctx = androidx.compose.ui.platform.LocalContext.current
            
            // Botón Compartir PDF
            IconButton(onClick = { 
                // Esto es simulado como un Quick Export genérico de la sesión visible.
                com.agrocontrol.domain.util.PdfGenerator.generateAndShareCultivoReport(
                    context = ctx,
                    cultivo = com.agrocontrol.domain.model.Cultivo(
                        id = 0, agricultorId = 0, tipoCultivo = "Cultivo Activo", hectareas = 0.0, 
                        fechaSiembra = System.currentTimeMillis(), region = "Mi Región", 
                        etapaActual = com.agrocontrol.domain.model.EtapaCultivo.GERMINACION
                    ),
                    climaContexto = "Reporte climatológico extraído a las ${java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)}:00 hs.",
                    alertasActivas = alertasCount,
                    userName = userName
                )
            }) {
                Icon(Icons.Default.PictureAsPdf, null, tint = Blanco)
            }
            
            // Botón alertas con badge
            BadgedBox(
                badge = {
                    if (alertasCount > 0) Badge(containerColor = RojoAlert) {
                        Text("$alertasCount", fontSize = 10.sp)
                    }
                }
            ) {
                IconButton(onClick = onAlertas) {
                    Icon(Icons.Default.Notifications, null, tint = Blanco)
                }
            }
            // Botón perfil
            IconButton(onClick = onPerfil) {
                Box(
                    Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Blanco.copy(0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, null, tint = Blanco, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// ─── Clima card premium con gradiente dinámico ────────────────────────────────
@Composable
fun ClimaCardPremium(state: DashboardUiState, onClick: () -> Unit) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val gradiente = when {
        hour in 6..11  -> listOf(Color(0xFF1B6CA8), Color(0xFF2196F3))
        hour in 12..17 -> listOf(Color(0xFF0D47A1), Color(0xFF1565C0))
        else            -> listOf(Color(0xFF0D1117), Color(0xFF1E3A5F))
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(gradiente))
        ) {
            if (state.isLoadingClima && state.clima == null) {
                com.agrocontrol.presentation.ui.components.ShimmerClimaCard()
            } else {
                state.clima?.let { c ->
                    Row(
                        Modifier.fillMaxWidth().padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Clima ahora", color = Blanco.copy(0.7f),
                                style = MaterialTheme.typography.labelMedium,
                                fontFamily = PlusJakartaSansFamily)
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text("${c.temperatura}",
                                    color = Blanco, fontSize = 52.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = PlusJakartaSansFamily)
                                Text("°C", color = Blanco.copy(0.8f), fontSize = 22.sp,
                                    fontFamily = PlusJakartaSansFamily,
                                    modifier = Modifier.padding(bottom = 10.dp))
                            }
                            Text(c.descripcion, color = Blanco.copy(0.85f),
                                fontFamily = PlusJakartaSansFamily,
                                fontWeight = FontWeight.Medium)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(c.icono, fontSize = 48.sp)
                            Spacer(Modifier.height(8.dp))
                            ClimaChip("💧 ${c.humedad}%")
                            Spacer(Modifier.height(4.dp))
                            ClimaChip("💨 ${c.viento} km/h")
                        }
                    }
                } ?: Box(Modifier.fillMaxWidth().padding(20.dp)) {
                    Text(
                        "Registra un cultivo para ver el clima de tu región",
                        color = Blanco,
                        fontFamily = PlusJakartaSansFamily
                    )
                }
            }
        }
    }
}

@Composable
private fun ClimaChip(text: String) {
    Surface(shape = RoundedCornerShape(8.dp), color = Blanco.copy(0.2f)) {
        Text(text, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            color = Blanco, fontSize = 11.sp, fontFamily = PlusJakartaSansFamily)
    }
}

// ─── Cultivo Card Premium ─────────────────────────────────────────────────────
@Composable
fun CultivoActivoCardPremium(cultivo: Cultivo?, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        if (cultivo == null) {
            Row(
                Modifier.fillMaxWidth().padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(52.dp).clip(RoundedCornerShape(14.dp))
                        .background(Verde60.copy(0.1f)),
                    contentAlignment = Alignment.Center
                ) { Text("🌱", fontSize = 26.sp) }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Sin cultivo activo", fontWeight = FontWeight.SemiBold,
                        fontFamily = PlusJakartaSansFamily)
                    Text("Toca para registrar tu primer cultivo →",
                        style = MaterialTheme.typography.bodySmall,
                        color = Verde60, fontFamily = PlusJakartaSansFamily)
                }
                Icon(Icons.Default.ChevronRight, null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(0.3f))
            }
        } else {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(52.dp).clip(RoundedCornerShape(14.dp))
                            .background(Brush.radialGradient(listOf(VerdeAccent, Verde60))),
                        contentAlignment = Alignment.Center
                    ) { Text("🌾", fontSize = 26.sp) }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(cultivo.tipoCultivo, fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp, color = Verde60,
                            fontFamily = PlusJakartaSansFamily)
                        Text("${cultivo.region} · ${cultivo.hectareas} ha",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.6f),
                            fontFamily = PlusJakartaSansFamily)
                    }
                    Icon(Icons.Default.ChevronRight, null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(0.3f))
                }
                Spacer(Modifier.height(14.dp))
                // Barra de progreso de etapa
                val etapas = com.agrocontrol.domain.model.EtapaCultivo.values()
                val idx    = etapas.indexOf(cultivo.etapaActual)
                val progress by animateFloatAsState(
                    targetValue = (idx + 1).toFloat() / etapas.size,
                    animationSpec = tween(800, easing = FastOutSlowInEasing),
                    label = "etapa_progress"
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Etapa: ${cultivo.etapaActual.name}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.6f),
                        fontFamily = PlusJakartaSansFamily)
                    Text("${idx + 1}/${etapas.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Verde60, fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = Verde60,
                    trackColor = Verde60.copy(0.15f)
                )
            }
        }
    }
}

// ─── Quick access card ────────────────────────────────────────────────────────
@Composable
fun QuickCard(
    icon: ImageVector, label: String, badge: String?,
    color: Color, modifier: Modifier, onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            BadgedBox(badge = {
                if (badge != null) Badge(containerColor = RojoAlert) {
                    Text(badge, fontSize = 9.sp)
                }
            }) {
                Box(
                    Modifier.size(42.dp).clip(RoundedCornerShape(12.dp))
                        .background(color.copy(0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
                }
            }
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                fontFamily = PlusJakartaSansFamily,
                color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────
@Composable
fun SectionLabel(text: String) {
    Text(text, fontWeight = FontWeight.Bold, fontSize = 15.sp,
        fontFamily = PlusJakartaSansFamily,
        color = MaterialTheme.colorScheme.onBackground)
}

// Keep backwards compatibility aliases
@Composable
fun ClimaCard(state: DashboardUiState, onClick: () -> Unit, onRefresh: () -> Unit = {}) =
    ClimaCardPremium(state, onClick)

@Composable
fun CultivoActivoCard(cultivo: Cultivo?, onClick: () -> Unit) =
    CultivoActivoCardPremium(cultivo, onClick)

@Composable
fun InfoChip(text: String) {
    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer) {
        Text(text, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall, fontFamily = PlusJakartaSansFamily)
    }
}

@Composable
fun QuickAccessCard(
    icon: ImageVector, title: String, subtitle: String,
    hasAlert: Boolean, modifier: Modifier, onClick: () -> Unit
) = QuickCard(
    icon = icon, label = title,
    badge = if (hasAlert) "!" else null,
    color = if (hasAlert) RojoAlert else AzulInfo,
    modifier = modifier, onClick = onClick
)
