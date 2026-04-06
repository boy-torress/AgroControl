package com.agrocontrol.presentation.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agrocontrol.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToCultivo: () -> Unit,
    onNavigateToClima: () -> Unit,
    onNavigateToInventario: () -> Unit,
    onNavigateToAlertas: () -> Unit,
    onLogout: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Estás seguro que deseas salir?") },
            confirmButton = {
                TextButton(onClick = { viewModel.logout(); onLogout() }) { Text("Salir") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Hola, ${state.userName.split(" ").firstOrNull() ?: "Agricultor"} 👋", fontWeight = FontWeight.Bold)
                        Text("Panel principal", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                },
                actions = {
                    if (state.alertasCount > 0) {
                        BadgedBox(badge = { Badge { Text("${state.alertasCount}") } }) {
                            IconButton(onClick = onNavigateToAlertas) { Icon(Icons.Default.Notifications, null) }
                        }
                    } else {
                        IconButton(onClick = onNavigateToAlertas) { Icon(Icons.Default.Notifications, null) }
                    }
                    IconButton(onClick = { showLogoutDialog = true }) { Icon(Icons.Default.Logout, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
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
            // Clima card
            ClimaCard(state = state, onClick = onNavigateToClima)

            // Cultivo activo
            CultivoActivoCard(cultivo = state.cultivoActivo, onClick = onNavigateToCultivo)

            // Accesos rápidos
            Text("Accesos rápidos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickAccessCard(
                    icon = Icons.Default.Inventory2,
                    title = "Inventario",
                    subtitle = if (state.stockCriticoCount > 0) "${state.stockCriticoCount} alertas" else "Sin alertas",
                    hasAlert = state.stockCriticoCount > 0,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToInventario
                )
                QuickAccessCard(
                    icon = Icons.Default.Warning,
                    title = "Alertas",
                    subtitle = if (state.alertasCount > 0) "${state.alertasCount} nuevas" else "Todo OK",
                    hasAlert = state.alertasCount > 0,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToAlertas
                )
            }

            if (state.sinConexion) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WifiOff, null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(Modifier.width(8.dp))
                        Text("Sin conexión · Datos del caché", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
fun ClimaCard(state: DashboardUiState, onClick: () -> Unit, onRefresh: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Verde60),
        shape = RoundedCornerShape(16.dp)
    ) {
        // Spinner solo cuando no hay NINGÚN dato previo
        if (state.isLoadingClima && state.clima == null) {
            Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(color = Blanco, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(12.dp))
                    Text("Cargando clima...", color = Blanco)
                }
            }
        } else {
            state.clima?.let { c ->
                Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Clima actual", color = Blanco.copy(alpha = 0.8f), style = MaterialTheme.typography.labelMedium)
                        Text("${c.temperatura}°C", color = Blanco, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                        Text(c.descripcion, color = Blanco.copy(alpha = 0.9f))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(c.icono, fontSize = 40.sp)
                        Text("💧 ${c.humedad}%", color = Blanco.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
                        Text("💨 ${c.viento} km/h", color = Blanco.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
                    }
                }
            } ?: run {
                Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                    Text("Registra un cultivo para ver el clima de tu región", color = Blanco, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun CultivoActivoCard(cultivo: com.agrocontrol.domain.model.Cultivo?, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Agriculture, null, tint = Verde60)
                Spacer(Modifier.width(8.dp))
                Text("Cultivo activo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            }
            Spacer(Modifier.height(12.dp))
            if (cultivo != null) {
                Text(cultivo.tipoCultivo, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Verde60)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    InfoChip("📍 ${cultivo.region}")
                    InfoChip("🌾 ${cultivo.hectareas} ha")
                    InfoChip("📊 ${cultivo.etapaActual.name}")
                }
            } else {
                Text("No tienes cultivos activos", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                Spacer(Modifier.height(4.dp))
                Text("Toca aquí para registrar tu primer cultivo →", style = MaterialTheme.typography.bodySmall, color = Verde60)
            }
        }
    }
}

@Composable
fun QuickAccessCard(icon: ImageVector, title: String, subtitle: String, hasAlert: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (hasAlert) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Icon(icon, null, tint = if (hasAlert) MaterialTheme.colorScheme.error else Verde60)
            Spacer(Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = if (hasAlert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}

@Composable
fun InfoChip(text: String) {
    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer) {
        Text(text, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall)
    }
}
