package com.agrocontrol.presentation.ui.cultivo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agrocontrol.presentation.theme.Verde60
import java.text.SimpleDateFormat
import java.util.*

val TIPOS_CULTIVO = listOf("Trigo", "Maíz", "Papa", "Tomate", "Lechuga", "Cebolla", "Uva", "Manzana", "Palta", "Arándano", "Otro")
val REGIONES_CHILE = listOf("Arica y Parinacota", "Tarapacá", "Antofagasta", "Atacama", "Coquimbo", "Valparaíso", "Metropolitana", "O'Higgins", "Maule", "Ñuble", "Biobío", "La Araucanía", "Los Ríos", "Los Lagos", "Aysén", "Magallanes")

data class AtajoCultivo(val icono: String, val titulo: String, val tipo: String, val ha: String, val region: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroCultivoScreen(
    onSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: CultivoViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var tipoCultivo by remember { mutableStateOf("") }
    var hectareas by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var tipoDropdownOpen by remember { mutableStateOf(false) }
    var regionDropdownOpen by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val sugerencias = remember {
        listOf(
            AtajoCultivo("🌾", "Trigo Maule", "Trigo", "10.0", "Maule"),
            AtajoCultivo("🌽", "Maíz O'Higgins", "Maíz", "5.5", "O'Higgins"),
            AtajoCultivo("🥔", "Papas Sur", "Papa", "8.0", "Los Lagos"),
            AtajoCultivo("🍎", "Manzanas", "Manzana", "3.2", "Biobío")
        )
    }

    LaunchedEffect(state.success) {
        if (state.success) { viewModel.clearSuccess(); onSuccess() }
    }

    if (showConfirmDialog && state.cultivoActivo != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Reemplazar cultivo activo") },
            text = { Text("Ya tienes un cultivo activo (${state.cultivoActivo!!.tipoCultivo}). ¿Deseas reemplazarlo con el nuevo cultivo?") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    viewModel.registrarCultivo(tipoCultivo, hectareas, region, System.currentTimeMillis())
                }) { Text("Sí, reemplazar") }
            },
            dismissButton = { TextButton(onClick = { showConfirmDialog = false }) { Text("Cancelar") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar cultivo") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // Sugerencias rápidas (Atajos)
            Text("Sugerencias rápidas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(sugerencias.size) { index ->
                    val s = sugerencias[index]
                    Card(
                        onClick = {
                            tipoCultivo = s.tipo
                            hectareas = s.ha
                            region = s.region
                        },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Text(s.icono, fontSize = 24.sp)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(s.titulo, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("${s.ha} ha · ${s.region}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            Card(shape = RoundedCornerShape(12.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Información del cultivo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

                    // Tipo de cultivo
                    ExposedDropdownMenuBox(expanded = tipoDropdownOpen, onExpandedChange = { tipoDropdownOpen = it }) {
                        OutlinedTextField(
                            value = tipoCultivo,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tipo de cultivo *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(tipoDropdownOpen) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = tipoDropdownOpen, onDismissRequest = { tipoDropdownOpen = false }) {
                            TIPOS_CULTIVO.forEach { tipo ->
                                DropdownMenuItem(text = { Text(tipo) }, onClick = { tipoCultivo = tipo; tipoDropdownOpen = false })
                            }
                        }
                    }

                    // Hectáreas
                    OutlinedTextField(
                        value = hectareas,
                        onValueChange = { hectareas = it; viewModel.clearError() },
                        label = { Text("Superficie sembrada (hectáreas) *") },
                        leadingIcon = { Icon(Icons.Default.SquareFoot, null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Región
                    ExposedDropdownMenuBox(expanded = regionDropdownOpen, onExpandedChange = { regionDropdownOpen = it }) {
                        OutlinedTextField(
                            value = region,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Región geográfica *") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(regionDropdownOpen) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = regionDropdownOpen, onDismissRequest = { regionDropdownOpen = false }) {
                            REGIONES_CHILE.forEach { reg ->
                                DropdownMenuItem(text = { Text(reg) }, onClick = { region = reg; regionDropdownOpen = false })
                            }
                        }
                    }
                }
            }

            state.error?.let {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(it, modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.error)
                }
            }

            Button(
                onClick = {
                    if (state.cultivoActivo != null) showConfirmDialog = true
                    else viewModel.registrarCultivo(tipoCultivo, hectareas, region, System.currentTimeMillis())
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Agriculture, null)
                Spacer(Modifier.width(8.dp))
                Text("Registrar cultivo", fontWeight = FontWeight.SemiBold)
            }
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
    var showEtapaDialog by remember { mutableStateOf(false) }
    var notasEtapa by remember { mutableStateOf("") }
    var etapaSeleccionada by remember { mutableStateOf<com.agrocontrol.domain.model.EtapaCultivo?>(null) }
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    if (showEtapaDialog) {
        AlertDialog(
            onDismissRequest = { showEtapaDialog = false },
            title = { Text("Actualizar etapa") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    com.agrocontrol.domain.model.EtapaCultivo.values().forEach { etapa ->
                        FilterChip(
                            selected = etapaSeleccionada == etapa,
                            onClick = { etapaSeleccionada = etapa },
                            label = { Text(etapa.name) }
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notasEtapa,
                        onValueChange = { notasEtapa = it },
                        label = { Text("Notas opcionales") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    etapaSeleccionada?.let { viewModel.actualizarEtapa(it, notasEtapa) }
                    showEtapaDialog = false
                }) { Text("Actualizar") }
            },
            dismissButton = { TextButton(onClick = { showEtapaDialog = false }) { Text("Cancelar") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi cultivo") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = onNavigateToRegistro) { Icon(Icons.Default.Add, null) }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val cultivo = state.cultivoActivo
            if (cultivo == null) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(24.dp), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                        Text("🌱", style = MaterialTheme.typography.displaySmall)
                        Spacer(Modifier.height(8.dp))
                        Text("Sin cultivo activo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("Registra tu primer cultivo para comenzar", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onNavigateToRegistro) { Text("Registrar cultivo") }
                    }
                }
            } else {
                // Info card
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Verde60)) {
                    Column(Modifier.padding(20.dp)) {
                        Text(cultivo.tipoCultivo, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Blanco)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            InfoBadge("📍", cultivo.region)
                            InfoBadge("🌾", "${cultivo.hectareas} ha")
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("Etapa: ${cultivo.etapaActual.name}", color = Blanco.copy(0.9f), fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = onNavigateToMapa,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = Verde60)
                        ) {
                            Icon(Icons.Default.Map, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Ver en el mapa", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Etapa progress
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(20.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Text("Ciclo del cultivo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            TextButton(onClick = { showEtapaDialog = true; etapaSeleccionada = cultivo.etapaActual }) {
                                Text("Actualizar")
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        val etapas = com.agrocontrol.domain.model.EtapaCultivo.values()
                        val currentIndex = etapas.indexOf(cultivo.etapaActual)
                        LinearProgressIndicator(
                            progress = { (currentIndex + 1).toFloat() / etapas.size },
                            modifier = Modifier.fillMaxWidth(),
                            color = Verde60
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            etapas.forEach { etapa ->
                                Text(
                                    etapa.name.take(3),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (etapa.ordinal <= currentIndex) Verde60 else MaterialTheme.colorScheme.onSurface.copy(0.4f),
                                    fontWeight = if (etapa == cultivo.etapaActual) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // Predicción IA
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(20.dp)) {
                        Text("🤖 Predicción de rendimiento", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(12.dp))
                        if (state.isLoadingPrediccion) {
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                CircularProgressIndicator(Modifier.size(20.dp), color = Verde60, strokeWidth = 2.dp)
                                Spacer(Modifier.width(12.dp))
                                Text("Calculando predicción...")
                            }
                        } else if (state.prediccion != null) {
                            val p = state.prediccion!!
                            Text("${p.kgPorHectarea} kg/ha", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Verde60)
                            Text("Confianza: ${p.confianzaPorcentaje}%", style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(8.dp))
                            p.factoresInfluyentes.forEach { factor ->
                                Text("• $factor", style = MaterialTheme.typography.bodySmall)
                            }
                        } else {
                            Button(onClick = { viewModel.predecirRendimiento() }, modifier = Modifier.fillMaxWidth()) {
                                Text("Calcular predicción")
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                // Gráfica de evolución histórica (Vico M3)
                com.agrocontrol.presentation.ui.cultivo.RendimientoChart()

                // Historial
                if (state.historial.isNotEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                        Column(Modifier.padding(20.dp)) {
                            Text("Historial de etapas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(12.dp))
                            state.historial.forEach { h ->
                                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                    Text("• ${h.etapa.name}", modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                                    Text(sdf.format(Date(h.fechaCambio)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                                }
                                if (h.notas.isNotBlank()) Text("  ${h.notas}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                                Divider(modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoBadge(emoji: String, text: String) {
    Surface(shape = RoundedCornerShape(8.dp), color = Blanco.copy(alpha = 0.2f)) {
        Text("$emoji $text", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = Blanco, style = MaterialTheme.typography.labelSmall)
    }
}

private val Blanco = androidx.compose.ui.graphics.Color.White
