package com.agrocontrol.presentation.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agrocontrol.presentation.theme.Verde60

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    onLogout: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var tabIndex by remember { mutableStateOf(0) }
    var estadoDropdown by remember { mutableStateOf(false) }
    var tipoDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel Administrador") },
                actions = {
                    IconButton(onClick = { viewModel.logout(); onLogout() }) { Icon(Icons.Default.Logout, null) }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = tabIndex) {
                Tab(selected = tabIndex == 0, onClick = { tabIndex = 0 }, text = { Text("Cultivos") },
                    icon = { Icon(Icons.Default.Agriculture, null) })
                Tab(selected = tabIndex == 1, onClick = { tabIndex = 1 }, text = { Text("Usuarios") },
                    icon = { Icon(Icons.Default.People, null) })
            }

            when (tabIndex) {
                0 -> {
                    // Filtros
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Filtro estado
                        ExposedDropdownMenuBox(expanded = estadoDropdown, onExpandedChange = { estadoDropdown = it },
                            modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = state.filtroEstado.replaceFirstChar { it.uppercase() },
                                onValueChange = {}, readOnly = true, label = { Text("Estado") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(estadoDropdown) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(), textStyle = MaterialTheme.typography.bodySmall
                            )
                            ExposedDropdownMenu(expanded = estadoDropdown, onDismissRequest = { estadoDropdown = false }) {
                                listOf("todos", "activo", "inactivo").forEach { e ->
                                    DropdownMenuItem(text = { Text(e.replaceFirstChar { it.uppercase() }) },
                                        onClick = { viewModel.setFiltroEstado(e); estadoDropdown = false })
                                }
                            }
                        }
                        // Filtro tipo
                        ExposedDropdownMenuBox(expanded = tipoDropdown, onExpandedChange = { tipoDropdown = it },
                            modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = if (state.filtroTipo == "todos") "Todos" else state.filtroTipo,
                                onValueChange = {}, readOnly = true, label = { Text("Tipo") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(tipoDropdown) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(), textStyle = MaterialTheme.typography.bodySmall
                            )
                            ExposedDropdownMenu(expanded = tipoDropdown, onDismissRequest = { tipoDropdown = false }) {
                                DropdownMenuItem(text = { Text("Todos") }, onClick = { viewModel.setFiltroTipo("todos"); tipoDropdown = false })
                                state.tiposCultivo.forEach { t ->
                                    DropdownMenuItem(text = { Text(t) }, onClick = { viewModel.setFiltroTipo(t); tipoDropdown = false })
                                }
                            }
                        }
                    }

                    // Contador y export
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("${state.cultivosFiltrados.size} registros", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.6f), modifier = Modifier.weight(1f))
                        OutlinedButton(onClick = {}, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                            Icon(Icons.Default.Download, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("CSV", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    if (state.cultivosFiltrados.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Sin cultivos con los filtros seleccionados", color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                        }
                    } else {
                        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(state.cultivosFiltrados) { cultivo ->
                                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Column(Modifier.weight(1f)) {
                                            Text(cultivo.tipoCultivo, fontWeight = FontWeight.SemiBold)
                                            Text("ID Agricultor: ${cultivo.agricultorId} · ${cultivo.region}",
                                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                                            Text("${cultivo.hectareas} ha · ${cultivo.etapaActual.name}",
                                                style = MaterialTheme.typography.bodySmall)
                                        }
                                        Surface(shape = RoundedCornerShape(6.dp),
                                            color = if (cultivo.activo) Verde60.copy(0.15f) else Color.Gray.copy(0.15f)) {
                                            Text(
                                                if (cultivo.activo) "Activo" else "Inactivo",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (cultivo.activo) Verde60 else Color.Gray,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Usuarios
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            Row(Modifier.fillMaxWidth()) {
                                listOf("AGRICULTOR", "AGRONOMO", "ADMINISTRADOR").forEach { rol ->
                                    val count = state.usuarios.count { it.rol.name == rol }
                                    Card(modifier = Modifier.weight(1f).padding(4.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                        Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("$count", fontWeight = FontWeight.Bold, color = Verde60,
                                                style = MaterialTheme.typography.titleLarge)
                                            Text(rol.take(5), style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }
                        items(state.usuarios) { user ->
                            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Surface(shape = androidx.compose.foundation.shape.CircleShape,
                                        color = Verde60, modifier = Modifier.size(40.dp)) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(user.nombre.first().toString(), color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(user.nombre, fontWeight = FontWeight.SemiBold)
                                        Text(user.correo, style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                                    }
                                    Surface(shape = RoundedCornerShape(6.dp), color = Verde60.copy(0.12f)) {
                                        Text(user.rol.name.take(3), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall, color = Verde60, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
