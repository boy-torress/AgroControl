package com.agrocontrol.presentation.ui.cultivo

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agrocontrol.domain.model.EtapaCultivo
import com.agrocontrol.presentation.theme.PlusJakartaSansFamily
import com.agrocontrol.presentation.theme.Verde60
import java.text.SimpleDateFormat
import java.util.*

// ─── Design tokens (mismo sistema que AlertasScreen / DashboardScreen) ──────
private val CultBase    = Color(0xFF030F07)
private val CultCard    = Color(0xFF0C1E10)
private val CultBorder  = Color(0xFF1A3A1F)
private val GreenNeon   = Color(0xFF4ADE80)
private val TextPrimary = Color(0xFFF0FFF4)
private val TextSecond  = Color(0xFF86EFAC)
private val TextMuted   = Color(0xFF4B7160)
private val RedVivid    = Color(0xFFF87171)
private val AmberVivid  = Color(0xFFFBBF24)
private val SurfaceCard = Color(0xFF111F14)

val TIPOS_CULTIVO = listOf(
    "Trigo", "Maíz", "Papa", "Tomate", "Lechuga",
    "Cebolla", "Uva", "Manzana", "Palta", "Arándano", "Otro"
)
val REGIONES_CHILE = listOf(
    "Arica y Parinacota", "Tarapacá", "Antofagasta", "Atacama",
    "Coquimbo", "Valparaíso", "Metropolitana", "O'Higgins",
    "Maule", "Ñuble", "Biobío", "La Araucanía",
    "Los Ríos", "Los Lagos", "Aysén", "Magallanes"
)

data class AtajoCultivo(val icono: String, val titulo: String, val tipo: String, val ha: String, val region: String)

// ─── Registro Cultivo Screen ──────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroCultivoScreen(
    onSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: CultivoViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var tipoCultivo       by remember { mutableStateOf("") }
    var hectareas         by remember { mutableStateOf("") }
    var region            by remember { mutableStateOf("") }
    var tipoDropdownOpen  by remember { mutableStateOf(false) }
    var regionDropdownOpen by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val sugerencias = remember {
        listOf(
            AtajoCultivo("🌾", "Trigo Maule",     "Trigo",   "10.0", "Maule"),
            AtajoCultivo("🌽", "Maíz O'Higgins",  "Maíz",    "5.5",  "O'Higgins"),
            AtajoCultivo("🥔", "Papas Sur",        "Papa",    "8.0",  "Los Lagos"),
            AtajoCultivo("🍎", "Manzanas Biobío",  "Manzana", "3.2",  "Biobío")
        )
    }

    LaunchedEffect(state.success) {
        if (state.success) { viewModel.clearSuccess(); onSuccess() }
    }

    // Confirm replace dialog
    if (showConfirmDialog && state.cultivoActivo != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            containerColor = CultCard,
            titleContentColor = TextPrimary,
            textContentColor = TextSecond,
            title = { Text("Reemplazar cultivo activo", fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Bold) },
            text  = {
                Text(
                    "Ya tienes un cultivo activo (${state.cultivoActivo!!.tipoCultivo}). ¿Deseas reemplazarlo?",
                    fontFamily = PlusJakartaSansFamily
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    viewModel.registrarCultivo(tipoCultivo, hectareas, region, System.currentTimeMillis())
                }) { Text("Sí, reemplazar", color = RedVivid, fontFamily = PlusJakartaSansFamily) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancelar", color = TextMuted, fontFamily = PlusJakartaSansFamily)
                }
            }
        )
    }

    Scaffold(
        containerColor = CultBase,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Registrar cultivo",
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = TextSecond)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CultBase)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ── Sugerencias rápidas ──────────────────────────────────────────
            SectionLabel("⚡ Sugerencias rápidas")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(sugerencias.size) { i ->
                    val s = sugerencias[i]
                    SugerenciaChip(s) {
                        tipoCultivo = s.tipo
                        hectareas   = s.ha
                        region      = s.region
                    }
                }
            }

            // ── Formulario ───────────────────────────────────────────────────
            SectionLabel("🌱 Información del cultivo")
            DarkCard {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

                    // Tipo de cultivo
                    ExposedDropdownMenuBox(
                        expanded = tipoDropdownOpen,
                        onExpandedChange = { tipoDropdownOpen = it }
                    ) {
                        DarkOutlinedTextField(
                            value    = tipoCultivo,
                            label    = "Tipo de cultivo *",
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(tipoDropdownOpen) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = tipoDropdownOpen,
                            onDismissRequest = { tipoDropdownOpen = false },
                            modifier = Modifier.background(CultCard)
                        ) {
                            TIPOS_CULTIVO.forEach { tipo ->
                                DropdownMenuItem(
                                    text = { Text(tipo, color = TextPrimary, fontFamily = PlusJakartaSansFamily) },
                                    onClick = { tipoCultivo = tipo; tipoDropdownOpen = false }
                                )
                            }
                        }
                    }

                    // Hectáreas
                    DarkOutlinedTextField(
                        value    = hectareas,
                        label    = "Superficie sembrada (hectáreas) *",
                        leadingIcon = { Icon(Icons.Default.SquareFoot, null, tint = TextMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        onValueChange = { hectareas = it; viewModel.clearError() },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Región
                    ExposedDropdownMenuBox(
                        expanded = regionDropdownOpen,
                        onExpandedChange = { regionDropdownOpen = it }
                    ) {
                        DarkOutlinedTextField(
                            value    = region,
                            label    = "Región geográfica *",
                            readOnly = true,
                            leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = TextMuted) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(regionDropdownOpen) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = regionDropdownOpen,
                            onDismissRequest = { regionDropdownOpen = false },
                            modifier = Modifier.background(CultCard)
                        ) {
                            REGIONES_CHILE.forEach { reg ->
                                DropdownMenuItem(
                                    text = { Text(reg, color = TextPrimary, fontFamily = PlusJakartaSansFamily) },
                                    onClick = { region = reg; regionDropdownOpen = false }
                                )
                            }
                        }
                    }
                }
            }

            // Error
            state.error?.let { err ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(RedVivid.copy(0.1f))
                        .drawBehind {
                            drawLine(RedVivid.copy(0.4f), Offset(0f, 0f), Offset(size.width, 0f), 1f)
                        }
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null, tint = RedVivid, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(err, color = RedVivid, fontFamily = PlusJakartaSansFamily, fontSize = 13.sp)
                    }
                }
            }

            // CTA
            Button(
                onClick = {
                    if (state.cultivoActivo != null) showConfirmDialog = true
                    else viewModel.registrarCultivo(tipoCultivo, hectareas, region, System.currentTimeMillis())
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenNeon, contentColor = Color.Black)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Agriculture, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Registrar cultivo", fontWeight = FontWeight.Bold, fontFamily = PlusJakartaSansFamily)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─── Cultivo Detail Screen ────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CultivoScreen(
    onNavigateToRegistro: () -> Unit,
    onNavigateToMapa: () -> Unit,
    onBack: () -> Unit,
    viewModel: CultivoViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showEtapaDialog  by remember { mutableStateOf(false) }
    var notasEtapa       by remember { mutableStateOf("") }
    var etapaSeleccionada by remember { mutableStateOf<EtapaCultivo?>(null) }
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    // Etapa update dialog
    if (showEtapaDialog) {
        AlertDialog(
            onDismissRequest = { showEtapaDialog = false },
            containerColor   = CultCard,
            titleContentColor = TextPrimary,
            title = { Text("Actualizar etapa", fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Bold) },
            text  = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    EtapaCultivo.values().forEach { etapa ->
                        val selected = etapaSeleccionada == etapa
                        FilterChip(
                            selected = selected,
                            onClick  = { etapaSeleccionada = etapa },
                            label    = {
                                Text(etapa.name, fontFamily = PlusJakartaSansFamily,
                                    color = if (selected) Color.Black else TextSecond)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor  = GreenNeon,
                                containerColor          = CultBorder
                            )
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    DarkOutlinedTextField(
                        value    = notasEtapa,
                        label    = "Notas opcionales",
                        onValueChange = { notasEtapa = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    etapaSeleccionada?.let { viewModel.actualizarEtapa(it, notasEtapa) }
                    showEtapaDialog = false
                }) { Text("Actualizar", color = GreenNeon, fontFamily = PlusJakartaSansFamily) }
            },
            dismissButton = {
                TextButton(onClick = { showEtapaDialog = false }) {
                    Text("Cancelar", color = TextMuted, fontFamily = PlusJakartaSansFamily)
                }
            }
        )
    }

    Scaffold(
        containerColor = CultBase,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mi cultivo",
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = TextSecond)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToRegistro) {
                        Icon(Icons.Default.Add, null, tint = GreenNeon)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CultBase)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val cultivo = state.cultivoActivo

            if (cultivo == null) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(CultCard)
                        .drawBehind {
                            drawLine(GreenNeon.copy(0.2f), Offset(0f, 0f), Offset(size.width, 0f), 1.5f)
                        }
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            Modifier.size(80.dp).background(GreenNeon.copy(0.08f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) { Text("🌱", fontSize = 36.sp) }
                        Text(
                            "Sin cultivo activo",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontFamily = PlusJakartaSansFamily
                        )
                        Text(
                            "Registra tu primer cultivo para comenzar",
                            color = TextMuted,
                            fontFamily = PlusJakartaSansFamily,
                            fontSize = 13.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Button(
                            onClick = onNavigateToRegistro,
                            colors = ButtonDefaults.buttonColors(containerColor = GreenNeon, contentColor = Color.Black),
                            shape  = RoundedCornerShape(12.dp)
                        ) {
                            Text("Registrar cultivo", fontWeight = FontWeight.Bold, fontFamily = PlusJakartaSansFamily)
                        }
                    }
                }
            } else {

                // ── Hero card ──────────────────────────────────────────────
                AnimatedVisibility(visible = true, enter = fadeIn() + slideInVertically { it / 3 }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF0D2E14), Color(0xFF061209))
                                )
                            )
                            .drawBehind {
                                drawLine(GreenNeon.copy(0.5f), Offset(0f, 0f), Offset(size.width, 0f), 2f)
                                drawRect(
                                    brush = Brush.verticalGradient(listOf(GreenNeon.copy(0.7f), GreenNeon.copy(0.1f))),
                                    topLeft = Offset(0f, 0f),
                                    size = androidx.compose.ui.geometry.Size(3f, size.height)
                                )
                            }
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier
                                        .size(52.dp)
                                        .background(GreenNeon.copy(0.12f), RoundedCornerShape(14.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        when (cultivo.tipoCultivo.lowercase()) {
                                            "trigo"    -> "🌾"
                                            "maíz"     -> "🌽"
                                            "papa"     -> "🥔"
                                            "tomate"   -> "🍅"
                                            "uva"      -> "🍇"
                                            "manzana"  -> "🍎"
                                            "palta"    -> "🥑"
                                            "arándano" -> "🫐"
                                            else       -> "🌿"
                                        },
                                        fontSize = 26.sp
                                    )
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        cultivo.tipoCultivo,
                                        fontFamily = PlusJakartaSansFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize   = 20.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        "Etapa: ${cultivo.etapaActual.name}",
                                        color = TextSecond,
                                        fontFamily = PlusJakartaSansFamily,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Spacer(Modifier.height(14.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                InfoPill("📍", cultivo.region)
                                InfoPill("🌾", "${cultivo.hectareas} ha")
                            }
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = onNavigateToMapa,
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                                shape  = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GreenNeon, contentColor = Color.Black)
                            ) {
                                Icon(Icons.Default.Map, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Ver en el mapa", fontWeight = FontWeight.Bold, fontFamily = PlusJakartaSansFamily)
                            }
                        }
                    }
                }

                // ── Ciclo del cultivo ──────────────────────────────────────
                DarkCard {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Ciclo del cultivo",
                                fontFamily = PlusJakartaSansFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                fontSize = 15.sp
                            )
                            TextButton(onClick = {
                                showEtapaDialog  = true
                                etapaSeleccionada = cultivo.etapaActual
                            }) {
                                Text("Actualizar", color = GreenNeon, fontFamily = PlusJakartaSansFamily, fontSize = 13.sp)
                            }
                        }
                        Spacer(Modifier.height(10.dp))

                        val etapas      = EtapaCultivo.values()
                        val currentIdx  = etapas.indexOf(cultivo.etapaActual)
                        val progress    = (currentIdx + 1).toFloat() / etapas.size

                        LinearProgressIndicator(
                            progress  = { progress },
                            modifier  = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(4.dp)),
                            color     = GreenNeon,
                            trackColor = CultBorder
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            etapas.forEach { etapa ->
                                Text(
                                    etapa.name.take(3),
                                    fontSize = 9.sp,
                                    fontFamily = PlusJakartaSansFamily,
                                    color = if (etapa.ordinal <= currentIdx) GreenNeon else TextMuted,
                                    fontWeight = if (etapa == cultivo.etapaActual) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // ── Predicción IA ──────────────────────────────────────────
                DarkCard {
                    Column {
                        Text(
                            "🤖 Predicción de rendimiento",
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            fontSize = 15.sp
                        )
                        Spacer(Modifier.height(14.dp))

                        when {
                            state.isLoadingPrediccion -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(Modifier.size(20.dp), color = GreenNeon, strokeWidth = 2.dp)
                                    Spacer(Modifier.width(12.dp))
                                    Text("Calculando predicción...", color = TextSecond, fontFamily = PlusJakartaSansFamily)
                                }
                            }
                            state.prediccion != null -> {
                                val p = state.prediccion!!
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        "${p.kgPorHectarea}",
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Black,
                                        color = GreenNeon,
                                        fontFamily = PlusJakartaSansFamily
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        "kg/ha",
                                        fontSize = 14.sp,
                                        color = TextSecond,
                                        fontFamily = PlusJakartaSansFamily,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                }
                                // Confianza badge
                                Spacer(Modifier.height(6.dp))
                                Box(
                                    Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(GreenNeon.copy(0.1f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        "Confianza ${p.confianzaPorcentaje}%",
                                        fontSize = 11.sp,
                                        color = GreenNeon,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = PlusJakartaSansFamily
                                    )
                                }
                                Spacer(Modifier.height(12.dp))
                                // Factores
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(GreenNeon.copy(0.06f))
                                        .padding(12.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        p.factoresInfluyentes.forEach { factor ->
                                            Row {
                                                Text("•", color = GreenNeon, fontFamily = PlusJakartaSansFamily)
                                                Spacer(Modifier.width(6.dp))
                                                Text(factor, fontSize = 12.sp, color = TextSecond, fontFamily = PlusJakartaSansFamily)
                                            }
                                        }
                                    }
                                }
                            }
                            else -> {
                                Button(
                                    onClick  = { viewModel.predecirRendimiento() },
                                    modifier = Modifier.fillMaxWidth().height(46.dp),
                                    shape    = RoundedCornerShape(12.dp),
                                    colors   = ButtonDefaults.buttonColors(containerColor = GreenNeon, contentColor = Color.Black)
                                ) {
                                    Text("Calcular predicción", fontWeight = FontWeight.Bold, fontFamily = PlusJakartaSansFamily)
                                }
                            }
                        }
                    }
                }

                // ── Gráfica histórica ──────────────────────────────────────
                RendimientoChart()

                // ── Historial de etapas ────────────────────────────────────
                if (state.historial.isNotEmpty()) {
                    DarkCard {
                        Column {
                            Text(
                                "Historial de etapas",
                                fontFamily = PlusJakartaSansFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                fontSize = 15.sp
                            )
                            Spacer(Modifier.height(12.dp))
                            state.historial.forEachIndexed { idx, h ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        Modifier
                                            .size(8.dp)
                                            .background(GreenNeon.copy(0.7f), CircleShape)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        h.etapa.name,
                                        Modifier.weight(1f),
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary,
                                        fontFamily = PlusJakartaSansFamily,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        sdf.format(Date(h.fechaCambio)),
                                        fontSize = 10.sp,
                                        color = TextMuted,
                                        fontFamily = PlusJakartaSansFamily
                                    )
                                }
                                if (h.notas.isNotBlank()) {
                                    Text(
                                        h.notas,
                                        fontSize = 12.sp,
                                        color = TextMuted,
                                        fontFamily = PlusJakartaSansFamily,
                                        modifier = Modifier.padding(start = 18.dp, bottom = 4.dp)
                                    )
                                }
                                if (idx < state.historial.lastIndex) {
                                    HorizontalDivider(color = CultBorder, thickness = 0.5.dp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─── Shared private composables ───────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextMuted,
        fontFamily = PlusJakartaSansFamily,
        letterSpacing = 0.5.sp
    )
}

@Composable
private fun DarkCard(content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CultCard)
            .drawBehind {
                drawLine(CultBorder, Offset(0f, 0f), Offset(size.width, 0f), 1f)
            }
            .padding(18.dp)
    ) {
        Column(content = content)
    }
}

@Composable
private fun SugerenciaChip(s: AtajoCultivo, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CultCard)
            .drawBehind {
                drawLine(CultBorder, Offset(0f, 0f), Offset(size.width, 0f), 1f)
                drawLine(CultBorder, Offset(0f, size.height), Offset(size.width, size.height), 1f)
                drawLine(CultBorder, Offset(0f, 0f), Offset(0f, size.height), 1f)
                drawLine(CultBorder, Offset(size.width, 0f), Offset(size.width, size.height), 1f)
            }
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(s.icono, fontSize = 22.sp)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(s.titulo, fontWeight = FontWeight.Bold, color = TextPrimary, fontFamily = PlusJakartaSansFamily, fontSize = 13.sp)
                    Text("${s.ha} ha · ${s.region}", fontSize = 11.sp, color = TextMuted, fontFamily = PlusJakartaSansFamily)
                }
            }
    }
}

@Composable
private fun InfoPill(emoji: String, text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(0.08f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text("$emoji $text", color = TextSecond, fontFamily = PlusJakartaSansFamily, fontSize = 12.sp)
    }
}

@Composable
private fun DarkOutlinedTextField(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onValueChange: (String) -> Unit = {}
) {
    OutlinedTextField(
        value          = value,
        onValueChange  = onValueChange,
        readOnly       = readOnly,
        label          = { Text(label, fontFamily = PlusJakartaSansFamily, color = TextMuted) },
        leadingIcon    = leadingIcon,
        trailingIcon   = trailingIcon,
        keyboardOptions = keyboardOptions,
        modifier       = modifier,
        singleLine     = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor   = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedBorderColor = GreenNeon,
            unfocusedBorderColor = CultBorder,
            cursorColor        = GreenNeon,
            focusedContainerColor   = CultCard,
            unfocusedContainerColor = CultCard
        )
    )
}

// Keep the public InfoBadge for any external usages
@Composable
fun InfoBadge(emoji: String, text: String) {
    InfoPill(emoji, text)
}
