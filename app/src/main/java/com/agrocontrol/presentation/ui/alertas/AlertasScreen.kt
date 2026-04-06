package com.agrocontrol.presentation.ui.alertas

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
import com.agrocontrol.domain.model.*
import com.agrocontrol.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

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
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Alertas climáticas")
                        if (noLeidas > 0)
                            Text("$noLeidas sin leer", style = MaterialTheme.typography.labelSmall, color = RojoAlert)
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    if (noLeidas > 0) {
                        TextButton(onClick = { viewModel.marcarTodasLeidas() }) { Text("Marcar todas") }
                    }
                }
            )
        }
    ) { padding ->
        if (state.alertas.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("✅", style = MaterialTheme.typography.displayMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("Sin alertas activas", style = MaterialTheme.typography.titleMedium)
                    Text("Tu cultivo está en buenas condiciones", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.alertas, key = { it.id }) { alerta ->
                    AlertaCard(alerta = alerta, sdf = sdf, onMarcarLeida = { viewModel.marcarLeida(alerta.id) })
                }
            }
        }
    }
}

@Composable
fun AlertaCard(alerta: Alerta, sdf: SimpleDateFormat, onMarcarLeida: () -> Unit) {
    val (color, emoji) = when (alerta.tipo) {
        TipoAlerta.HELADA         -> Pair(Color(0xFF90CAF9), "🌨️")
        TipoAlerta.LLUVIA_INTENSA -> Pair(AzulInfo, "🌧️")
        TipoAlerta.SEQUIA         -> Pair(AmarilloAlert, "☀️")
        TipoAlerta.STOCK_CRITICO  -> Pair(RojoAlert, "📦")
    }
    val severidadColor = when (alerta.severidad) {
        SeveridadAlerta.ALTO  -> RojoAlert
        SeveridadAlerta.MEDIO -> AmarilloAlert
        SeveridadAlerta.BAJO  -> Verde60
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!alerta.leida) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (!alerta.leida) androidx.compose.foundation.BorderStroke(1.dp, severidadColor.copy(0.4f)) else null
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(emoji, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(alerta.tipo.name.replace("_", " "), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                    Surface(shape = RoundedCornerShape(4.dp), color = severidadColor.copy(0.15f)) {
                        Text(
                            "Severidad: ${alerta.severidad.name}",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = severidadColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                if (!alerta.leida) {
                    IconButton(onClick = onMarcarLeida, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.CheckCircle, null, tint = Verde60, modifier = Modifier.size(20.dp))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(alerta.descripcion, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(6.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Verde60.copy(0.1f)), shape = RoundedCornerShape(8.dp)) {
                Text(
                    "💡 ${alerta.recomendacion}",
                    modifier = Modifier.padding(10.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Verde60
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "Fecha estimada: ${sdf.format(Date(alerta.fechaEstimada))}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
            )
        }
    }
}
