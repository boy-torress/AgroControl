package com.agrocontrol.presentation.ui.agronomo

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agrocontrol.domain.model.User
import com.agrocontrol.presentation.theme.RojoAlert
import com.agrocontrol.presentation.theme.Verde60
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgronomoScreen(
    onLogout: () -> Unit,
    viewModel: AgronomoViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    if (state.selectedAgricultor != null) {
        val ag = state.selectedAgricultor!!
        val cultivo = state.cultivosPorAgricultor[ag.id]

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(ag.nombre) },
                    navigationIcon = { IconButton(onClick = { viewModel.selectAgricultor(null) }) { Icon(Icons.Default.ArrowBack, null) } }
                )
            }
        ) { padding ->
            Column(
                Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Info agricultor
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Agricultor", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Text(ag.nombre, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(ag.correo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                        Spacer(Modifier.height(4.dp))
                        Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                            Text("Solo lectura", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                // Cultivo activo
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Cultivo activo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        if (cultivo != null) {
                            Text(cultivo.tipoCultivo, style = MaterialTheme.typography.headlineSmall, color = Verde60, fontWeight = FontWeight.Bold)
                            Text("Etapa: ${cultivo.etapaActual.name}", style = MaterialTheme.typography.bodyMedium)
                            Text("Región: ${cultivo.region} · ${cultivo.hectareas} ha", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                        } else {
                            Text("Sin cultivo activo", color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                        }
                    }
                }

                // Historial
                if (state.historialSeleccionado.isNotEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Historial de etapas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(8.dp))
                            state.historialSeleccionado.forEach { h ->
                                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                    Text("• ${h.etapa.name}", Modifier.weight(1f), fontWeight = FontWeight.Medium)
                                    Text(sdf.format(Date(h.fechaCambio)), style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                                }
                                Divider(Modifier.padding(vertical = 2.dp))
                            }
                        }
                    }
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel Agrónomo") },
                actions = {
                    IconButton(onClick = { viewModel.logout(); onLogout() }) { Icon(Icons.Default.Logout, null) }
                }
            )
        }
    ) { padding ->
        if (state.agricultores.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("👨‍🌾", style = MaterialTheme.typography.displayMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("Sin agricultores asignados", style = MaterialTheme.typography.titleMedium)
                    Text("Contacta al administrador para que te asigne agricultores",
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                }
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text("Mis agricultores (${state.agricultores.size})",
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                items(state.agricultores) { ag ->
                    AgricultorCard(
                        agricultor = ag,
                        cultivo = state.cultivosPorAgricultor[ag.id],
                        alertasCount = state.alertasPorAgricultor[ag.id] ?: 0,
                        onClick = { viewModel.selectAgricultor(ag) }
                    )
                }
            }
        }
    }
}

@Composable
fun AgricultorCard(agricultor: User, cultivo: com.agrocontrol.domain.model.Cultivo?, alertasCount: Int, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(12.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = androidx.compose.foundation.shape.CircleShape, color = Verde60, modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(agricultor.nombre.first().toString(), color = androidx.compose.ui.graphics.Color.White,
                        fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(agricultor.nombre, fontWeight = FontWeight.SemiBold)
                Text(cultivo?.let { "${it.tipoCultivo} · ${it.etapaActual.name}" } ?: "Sin cultivo activo",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
            }
            if (alertasCount > 0) {
                Badge(containerColor = RojoAlert) { Text("$alertasCount") }
                Spacer(Modifier.width(8.dp))
            }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurface.copy(0.3f))
        }
    }
}
