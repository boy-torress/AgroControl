package com.agrocontrol.presentation.ui.alertas

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.agrocontrol.domain.model.*
import com.agrocontrol.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

private val AlertBase   = Color(0xFF030F07)
private val AlertCard   = Color(0xFF0C1E10)
private val AlertBorder = Color(0xFF1A3A1F)
private val GreenNeon   = Color(0xFF4ADE80)
private val TextPrimary = Color(0xFFF0FFF4)
private val TextSecond  = Color(0xFF86EFAC)
private val TextMuted   = Color(0xFF4B7160)
private val RedVivid    = Color(0xFFF87171)
private val AmberVivid  = Color(0xFFFBBF24)
private val BlueVivid   = Color(0xFF60A5FA)
private val IcyBlue     = Color(0xFFBAE6FD)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertasScreen(
    onBack: () -> Unit,
    viewModel: AlertasViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val noLeidas = state.alertas.count { !it.leida }

    Scaffold(
        containerColor = AlertBase,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Alertas climáticas",
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        if (noLeidas > 0) {
                            Text(
                                "$noLeidas sin leer",
                                fontSize = 11.sp,
                                color = RedVivid,
                                fontFamily = PlusJakartaSansFamily
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = TextSecond)
                    }
                },
                actions = {
                    if (noLeidas > 0) {
                        TextButton(onClick = { viewModel.marcarTodasLeidas() }) {
                            Text(
                                "Marcar todas",
                                color = GreenNeon,
                                fontFamily = PlusJakartaSansFamily,
                                fontSize = 13.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AlertBase)
            )
        }
    ) { padding ->
        if (state.alertas.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .background(GreenNeon.copy(0.08f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✅", fontSize = 40.sp)
                    }
                    Text(
                        "Sin alertas activas",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontFamily = PlusJakartaSansFamily
                    )
                    Text(
                        "Tu cultivo está en buenas condiciones",
                        color = TextMuted,
                        fontFamily = PlusJakartaSansFamily
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (noLeidas > 0) {
                    item {
                        // Summary bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(RedVivid.copy(0.08f))
                                .drawBehind {
                                    drawLine(RedVivid.copy(0.3f), Offset(0f, 0f), Offset(size.width, 0f), 1f)
                                }
                                .padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier.size(36.dp).background(RedVivid.copy(0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Warning, null, tint = RedVivid, modifier = Modifier.size(18.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    "$noLeidas alerta${if (noLeidas > 1) "s" else ""} pendiente${if (noLeidas > 1) "s" else ""}",
                                    color = RedVivid,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = PlusJakartaSansFamily
                                )
                            }
                        }
                    }
                }

                items(state.alertas, key = { it.id }) { alerta ->
                    AlertaCard(alerta = alerta, sdf = sdf, onMarcarLeida = { viewModel.marcarLeida(alerta.id) })
                }
            }
        }
    }
}

@Composable
fun AlertaCard(alerta: Alerta, sdf: SimpleDateFormat, onMarcarLeida: () -> Unit) {
    val (accentColor, emoji) = when (alerta.tipo) {
        TipoAlerta.HELADA         -> Pair(IcyBlue, "🌨️")
        TipoAlerta.LLUVIA_INTENSA -> Pair(BlueVivid, "🌧️")
        TipoAlerta.SEQUIA         -> Pair(AmberVivid, "☀️")
        TipoAlerta.STOCK_CRITICO  -> Pair(RedVivid, "📦")
    }
    val severidadColor = when (alerta.severidad) {
        SeveridadAlerta.ALTO  -> RedVivid
        SeveridadAlerta.MEDIO -> AmberVivid
        SeveridadAlerta.BAJO  -> GreenNeon
    }
    val dimmed = alerta.leida

    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically { it / 3 }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(if (dimmed) AlertCard.copy(alpha = 0.6f) else AlertCard)
                .drawBehind {
                    // Top border
                    drawLine(
                        color = if (!dimmed) accentColor.copy(0.4f) else AlertBorder,
                        start = Offset(0f, 0f), end = Offset(size.width, 0f),
                        strokeWidth = 1.5f
                    )
                    // Left accent bar
                    if (!dimmed) {
                        drawRect(
                            brush = Brush.verticalGradient(
                                listOf(accentColor.copy(0.8f), accentColor.copy(0.1f))
                            ),
                            topLeft = Offset(0f, 0f),
                            size = androidx.compose.ui.geometry.Size(3f, size.height)
                        )
                    }
                }
        ) {
            Column(Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Emoji icon container
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(accentColor.copy(0.1f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(emoji, fontSize = 22.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            alerta.tipo.name.replace("_", " "),
                            fontWeight = FontWeight.Bold,
                            color = if (dimmed) TextMuted else TextPrimary,
                            fontFamily = PlusJakartaSansFamily,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.height(3.dp))
                        Box(
                            Modifier.clip(RoundedCornerShape(6.dp)).background(severidadColor.copy(0.12f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "Severidad ${alerta.severidad.name}",
                                fontSize = 10.sp,
                                color = severidadColor,
                                fontWeight = FontWeight.Bold,
                                fontFamily = PlusJakartaSansFamily,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                    if (!alerta.leida) {
                        IconButton(
                            onClick = onMarcarLeida,
                            modifier = Modifier.size(36.dp).background(GreenNeon.copy(0.1f), CircleShape)
                        ) {
                            Icon(Icons.Default.Check, null, tint = GreenNeon, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    alerta.descripcion,
                    color = if (dimmed) TextMuted else TextSecond.copy(0.8f),
                    fontSize = 13.sp,
                    fontFamily = PlusJakartaSansFamily,
                    lineHeight = 20.sp
                )

                Spacer(Modifier.height(10.dp))

                // Recommendation box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(GreenNeon.copy(0.07f))
                        .drawBehind {
                            drawLine(GreenNeon.copy(0.2f), Offset(0f, 0f), Offset(size.width, 0f), 1f)
                        }
                        .padding(12.dp)
                ) {
                    Row {
                        Text("💡", fontSize = 13.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            alerta.recomendacion,
                            fontSize = 12.sp,
                            color = GreenNeon.copy(0.85f),
                            fontFamily = PlusJakartaSansFamily,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, null, tint = TextMuted, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Fecha estimada: ${sdf.format(Date(alerta.fechaEstimada))}",
                        fontSize = 10.sp,
                        color = TextMuted,
                        fontFamily = PlusJakartaSansFamily
                    )
                }
            }
        }
    }
}
