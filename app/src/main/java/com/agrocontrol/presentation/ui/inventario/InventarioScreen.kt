package com.agrocontrol.presentation.ui.inventario

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agrocontrol.domain.model.Insumo
import com.agrocontrol.presentation.theme.RojoAlert
import com.agrocontrol.presentation.theme.Verde60

val UNIDADES = listOf("kg", "litros", "sacos", "unidades", "toneladas", "gramos")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventarioScreen(
    onBack: () -> Unit,
    viewModel: InventarioViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var nombreInsumo by remember { mutableStateOf("") }
    var cantidadInsumo by remember { mutableStateOf("") }
    var unidadInsumo by remember { mutableStateOf("kg") }
    var minimoInsumo by remember { mutableStateOf("") }
    var unidadDropdown by remember { mutableStateOf(false) }

    if (state.showAddDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideAddDialog(); nombreInsumo = ""; cantidadInsumo = ""; minimoInsumo = "" },
            title = { Text("Agregar insumo") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = nombreInsumo, onValueChange = { nombreInsumo = it; viewModel.clearError() },
                        label = { Text("Nombre del insumo *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                    OutlinedTextField(value = cantidadInsumo, onValueChange = { cantidadInsumo = it },
                        label = { Text("Cantidad actual *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(), singleLine = true)

                    ExposedDropdownMenuBox(expanded = unidadDropdown, onExpandedChange = { unidadDropdown = it }) {
                        OutlinedTextField(value = unidadInsumo, onValueChange = {}, readOnly = true,
                            label = { Text("Unidad *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(unidadDropdown) },
                            modifier = Modifier.fillMaxWidth().menuAnchor())
                        ExposedDropdownMenu(expanded = unidadDropdown, onDismissRequest = { unidadDropdown = false }) {
                            UNIDADES.forEach { u ->
                                DropdownMenuItem(text = { Text(u) }, onClick = { unidadInsumo = u; unidadDropdown = false })
                            }
                        }
                    }

                    OutlinedTextField(value = minimoInsumo, onValueChange = { minimoInsumo = it },
                        label = { Text("Cantidad mínima de alerta *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        supportingText = { Text("Se alertará cuando baje de este nivel") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true)

                    state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.agregarInsumo(nombreInsumo, cantidadInsumo, unidadInsumo, minimoInsumo) }) { Text("Agregar") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideAddDialog(); nombreInsumo = ""; cantidadInsumo = ""; minimoInsumo = "" }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventario de insumos") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddDialog() }, containerColor = Verde60) {
                Icon(Icons.Default.Add, null, tint = androidx.compose.ui.graphics.Color.White)
            }
        }
    ) { padding ->
        if (state.insumos.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📦", style = MaterialTheme.typography.displayMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("Sin insumos registrados", style = MaterialTheme.typography.titleMedium)
                    Text("Agrega fertilizantes, pesticidas o semillas", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val criticos = state.insumos.filter { it.enStockCritico }
                if (criticos.isNotEmpty()) {
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(12.dp)) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, null, tint = RojoAlert)
                                Spacer(Modifier.width(8.dp))
                                Text("${criticos.size} insumo(s) en stock crítico", color = RojoAlert, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                items(state.insumos) { insumo ->
                    InsumoCard(insumo = insumo, onMovimiento = { id, cant, tipo -> viewModel.registrarMovimiento(id, cant, tipo) },
                        onEliminar = { viewModel.eliminarInsumo(insumo) })
                }
            }
        }
    }
}

@Composable
fun InsumoCard(insumo: Insumo, onMovimiento: (Long, String, String) -> Unit, onEliminar: () -> Unit) {
    var showMovimientoDialog by remember { mutableStateOf(false) }
    var tipoMovimiento by remember { mutableStateOf("entrada") }
    var cantidadMov by remember { mutableStateOf("") }

    if (showMovimientoDialog) {
        AlertDialog(
            onDismissRequest = { showMovimientoDialog = false; cantidadMov = "" },
            title = { Text(if (tipoMovimiento == "entrada") "Registrar entrada" else "Registrar salida") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row {
                        FilterChip(selected = tipoMovimiento == "entrada", onClick = { tipoMovimiento = "entrada" }, label = { Text("Entrada") })
                        Spacer(Modifier.width(8.dp))
                        FilterChip(selected = tipoMovimiento == "salida", onClick = { tipoMovimiento = "salida" }, label = { Text("Salida") })
                    }
                    OutlinedTextField(value = cantidadMov, onValueChange = { cantidadMov = it },
                        label = { Text("Cantidad (${insumo.unidad})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
            },
            confirmButton = {
                Button(onClick = { onMovimiento(insumo.id, cantidadMov, tipoMovimiento); showMovimientoDialog = false; cantidadMov = "" }) {
                    Text("Registrar")
                }
            },
            dismissButton = { TextButton(onClick = { showMovimientoDialog = false }) { Text("Cancelar") } }
        )
    }

    val isCritico = insumo.enStockCritico
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (isCritico) MaterialTheme.colorScheme.errorContainer.copy(0.5f) else MaterialTheme.colorScheme.surface),
        border = if (isCritico) androidx.compose.foundation.BorderStroke(1.dp, RojoAlert.copy(0.4f)) else null
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(insumo.nombre, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                        if (isCritico) {
                            Spacer(Modifier.width(6.dp))
                            Icon(Icons.Default.Warning, null, tint = RojoAlert, modifier = Modifier.size(16.dp))
                        }
                    }
                    Text("${insumo.cantidadActual} ${insumo.unidad}", style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold, color = if (isCritico) RojoAlert else Verde60)
                    Text("Mínimo: ${insumo.cantidadMinima} ${insumo.unidad}", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                }
                Row {
                    IconButton(onClick = { showMovimientoDialog = true }) { Icon(Icons.Default.SwapVert, null, tint = Verde60) }
                    IconButton(onClick = onEliminar) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                }
            }
            if (isCritico) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (insumo.cantidadActual / insumo.cantidadMinima).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                    color = RojoAlert
                )
            }
        }
    }
}
