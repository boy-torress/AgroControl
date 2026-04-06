package com.agrocontrol.presentation.ui.clima

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agrocontrol.domain.model.PronosticoDia
import com.agrocontrol.presentation.theme.AzulInfo
import com.agrocontrol.presentation.theme.Verde60
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClimaScreen(
    onBack: () -> Unit,
    viewModel: ClimaViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val sdf = remember { SimpleDateFormat("EEE dd", Locale("es", "CL")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clima y pronóstico") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) { Icon(Icons.Default.Refresh, null) }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Verde60)
                    Spacer(Modifier.height(12.dp))
                    Text("Obteniendo datos climáticos...")
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Clima actual
            state.climaActual?.let { clima ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AzulInfo)
                ) {
                    Column(Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = Color.White)
                            Spacer(Modifier.width(4.dp))
                            Text(state.region, color = Color.White.copy(0.85f), style = MaterialTheme.typography.labelLarge)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("${clima.temperatura}°", color = Color.White, fontSize = 72.sp, fontWeight = FontWeight.Bold)
                            Text("C", color = Color.White.copy(0.7f), fontSize = 28.sp, modifier = Modifier.padding(bottom = 12.dp))
                        }
                        Text(clima.descripcion, color = Color.White, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            ClimaDataItem("💧", "Humedad", "${clima.humedad}%")
                            ClimaDataItem("💨", "Viento", "${clima.viento} km/h")
                            ClimaDataItem("🌧️", "Precipit.", "${clima.precipitacion} mm")
                        }
                        Spacer(Modifier.height(8.dp))
                        val sdfUpdate = SimpleDateFormat("HH:mm", Locale.getDefault())
                        Text(
                            "Actualizado: ${sdfUpdate.format(Date(clima.ultimaActualizacion))} · Los datos se cachean por 1 hora",
                            color = Color.White.copy(0.6f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            // Pronóstico 7 días
            if (state.pronostico.isNotEmpty()) {
                Text("Pronóstico 7 días", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.pronostico) { dia ->
                        PronosticoDiaCard(dia = dia, sdf = sdf)
                    }
                }
            }

            // Recomendaciones de cultivo
            if (state.recomendaciones.isNotEmpty()) {
                Text("🤖 Recomendaciones para tu campo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                state.recomendaciones.forEach { rec ->
                    RecomendacionCard(rec = rec)
                }
            }
        }
    }
}

@Composable
fun ClimaDataItem(emoji: String, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 20.sp)
        Text(value, color = Color.White, fontWeight = FontWeight.Bold)
        Text(label, color = Color.White.copy(0.7f), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun PronosticoDiaCard(dia: PronosticoDia, sdf: SimpleDateFormat) {
    Card(shape = RoundedCornerShape(12.dp)) {
        Column(
            Modifier.padding(12.dp).width(80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(sdf.format(Date(dia.fecha)).uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
            Text(dia.icono, fontSize = 28.sp)
            Text("${dia.tempMax.toInt()}°", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
            Text("${dia.tempMin.toInt()}°", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
            if (dia.probabilidadLluvia > 20) {
                Text("💧 ${dia.probabilidadLluvia}%", style = MaterialTheme.typography.labelSmall, color = AzulInfo)
            }
        }
    }
}

@Composable
fun RecomendacionCard(rec: com.agrocontrol.domain.model.RecomendacionCultivo) {
    val colorAdecuacion = when (rec.nivelAdecuacion) {
        "alto" -> Verde60
        "medio" -> androidx.compose.ui.graphics.Color(0xFFF4A261)
        else -> MaterialTheme.colorScheme.error
    }
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(rec.nombre, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                Text("Rendimiento: ${rec.rendimientoEsperado}", style = MaterialTheme.typography.bodySmall)
                if (rec.riesgosClimaticos.isNotEmpty()) {
                    Text("⚠️ ${rec.riesgosClimaticos.joinToString(", ")}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                }
            }
            Spacer(Modifier.width(12.dp))
            Surface(shape = RoundedCornerShape(8.dp), color = colorAdecuacion.copy(0.15f)) {
                Text(
                    rec.nivelAdecuacion.uppercase(),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    color = colorAdecuacion,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
