package com.agrocontrol.presentation.ui.cultivo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Layers
import kotlinx.coroutines.launch
import com.agrocontrol.presentation.theme.PlusJakartaSansFamily
import com.agrocontrol.presentation.theme.Verde60
import com.agrocontrol.presentation.theme.VerdeNeon
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PolygonAnnotation
import com.mapbox.maps.extension.style.style
import androidx.hilt.navigation.compose.hiltViewModel
import com.agrocontrol.presentation.ui.cultivo.CultivoViewModel

@OptIn(ExperimentalMaterial3Api::class, MapboxExperimental::class)
@Composable
fun MapaParcelasScreen(
    onNavigateBack: () -> Unit,
    viewModel: CultivoViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val cultivo = state.cultivoActivo
    val context = androidx.compose.ui.platform.LocalContext.current

    var dynamicMapStyle by remember { mutableStateOf(Style.SATELLITE_STREETS) }

    val baseLng = cultivo?.longitude ?: getCoordenadasRegion(cultivo?.region).first
    val baseLat = cultivo?.latitude ?: getCoordenadasRegion(cultivo?.region).second

    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            center(Point.fromLngLat(baseLng, baseLat))
            zoom(6.0)
            pitch(0.0)
        }
    }

    LaunchedEffect(Unit) {
        val cameraOptions = com.mapbox.maps.CameraOptions.Builder()
            .center(Point.fromLngLat(baseLng, baseLat))
            .zoom(16.5)
            .pitch(60.0)
            .build()
        val animationOptions = com.mapbox.maps.plugin.animation.MapAnimationOptions.Builder()
            .duration(3500)
            .build()
        mapViewportState.flyTo(cameraOptions, animationOptions)
    }

    // Calculamos un tamaño de polígono aproximado basado en hectáreas (Simple simulación)
    val ha = cultivo?.hectareas ?: 4.3
    val offset = (ha * 0.0001).coerceAtMost(0.01) // Simulador de expansión

    val polygonColor = remember(state.prediccion) {
        when {
            state.prediccion == null -> androidx.compose.ui.graphics.Color(0xFF4ADE80)
            state.prediccion!!.kgPorHectarea >= 5000 -> androidx.compose.ui.graphics.Color(0xFF4ADE80) // Buen rendimiento
            else -> androidx.compose.ui.graphics.Color(0xFFF97316) // Riesgo / Alerta
        }
    }

    val polygonPoints = remember(baseLng, baseLat, offset) {
        listOf(
            listOf(
                Point.fromLngLat(baseLng - offset, baseLat - offset),
                Point.fromLngLat(baseLng + offset, baseLat - offset),
                Point.fromLngLat(baseLng + offset, baseLat + offset),
                Point.fromLngLat(baseLng - offset, baseLat + offset),
                Point.fromLngLat(baseLng - offset, baseLat - offset)
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("Mis Parcelas", fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Bold) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(androidx.compose.material.icons.Icons.Default.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            
            MapboxMap(
                modifier = Modifier.fillMaxSize(),
                mapViewportState = mapViewportState,
                style = {
                    MapEffect(dynamicMapStyle) { mapView ->
                        mapView.mapboxMap.loadStyle(dynamicMapStyle)
                    }
                }
            ) {
                PolygonAnnotation(
                    points = polygonPoints
                ) {
                    fillColor = polygonColor
                    fillOpacity = 0.35
                    fillOutlineColor = androidx.compose.ui.graphics.Color(0xFF1B4332)
                }
            }

            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Center",
                tint = Color.White.copy(0.7f),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(36.dp)
            )

            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FloatingActionButton(
                    onClick = {
                        dynamicMapStyle = if (dynamicMapStyle == Style.SATELLITE_STREETS) Style.DARK else Style.SATELLITE_STREETS
                    },
                    containerColor = Color(0xE6161B22),
                    contentColor = VerdeNeon,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Layers, "Cambiar Capa")
                }
                
                FloatingActionButton(
                    onClick = {
                        val center = mapViewportState.cameraState?.center
                        if (center != null) {
                            viewModel.actualizarUbicacion(center.latitude(), center.longitude())
                            android.widget.Toast.makeText(context, "Ubicación real fijada exitosamente", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    containerColor = Color(0xE6161B22),
                    contentColor = VerdeNeon,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.GpsFixed, "Fijar GPS")
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xE6161B22))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Verde60),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🌾", fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            cultivo?.let { "Parcela de ${it.tipoCultivo}" } ?: "Sin Cultivo", 
                            color = Color.White, 
                            fontWeight = FontWeight.Bold, 
                            fontFamily = PlusJakartaSansFamily,
                            fontSize = 16.sp
                        )
                        Text(
                            cultivo?.let { "Estado: ${it.etapaActual.name.lowercase().replaceFirstChar { char -> char.uppercase() }} · ${it.hectareas} Ha" } ?: "Registra un cultivo", 
                            color = VerdeNeon, 
                            fontWeight = FontWeight.Medium,
                            fontFamily = PlusJakartaSansFamily,
                            fontSize = 12.sp
                        )
                    }
                    var analisisIniciado by remember { mutableStateOf(false) }

                    LaunchedEffect(state.isLoadingPrediccion) {
                        if (analisisIniciado && !state.isLoadingPrediccion) {
                            if (state.prediccion != null) {
                                android.widget.Toast.makeText(context, "Análisis IA finalizado", android.widget.Toast.LENGTH_SHORT).show()
                                // Se quitó onNavigateBack() para evitar el error interno de destrucción de Mapbox
                            } else if (state.error != null) {
                                android.widget.Toast.makeText(context, "Error: ${state.error}", android.widget.Toast.LENGTH_LONG).show()
                            }
                            analisisIniciado = false
                        }
                    }

                    Button(
                        onClick = { 
                            if (cultivo != null && !state.isLoadingPrediccion && state.prediccion == null) {
                                analisisIniciado = true
                                viewModel.predecirRendimiento()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VerdeNeon, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        enabled = cultivo != null && !state.isLoadingPrediccion
                    ) {
                        if (state.isLoadingPrediccion && analisisIniciado) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Calculando...", fontWeight = FontWeight.Bold, fontFamily = PlusJakartaSansFamily)
                        } else if (state.prediccion != null) {
                            Text("${state.prediccion!!.kgPorHectarea} kg/ha ✨", fontWeight = FontWeight.Bold, fontFamily = PlusJakartaSansFamily)
                        } else {
                            Text("Analizar IA", fontWeight = FontWeight.Bold, fontFamily = PlusJakartaSansFamily)
                        }
                    }
                }
            }
        }
    }
}

// Generador dinámico de coordenadas agrícolas referenciales
private fun getCoordenadasRegion(region: String?): Pair<Double, Double> {
    return when (region) {
        "Arica y Parinacota" -> Pair(-70.25, -18.48) // Valle de Lluta
        "Tarapacá" -> Pair(-69.58, -20.25) // Pica
        "Antofagasta" -> Pair(-68.20, -22.91) // San Pedro de Atacama
        "Atacama" -> Pair(-70.26, -27.38) // Valle del Copiapó
        "Coquimbo" -> Pair(-71.25, -30.60) // Ovalle
        "Valparaíso" -> Pair(-71.22, -32.74) // La Ligua / Quillota
        "Metropolitana" -> Pair(-70.93, -33.68) // Melipilla
        "O'Higgins" -> Pair(-71.00, -34.58) // San Fernando
        "Maule" -> Pair(-71.55, -35.48) // Talca
        "Ñuble" -> Pair(-72.10, -36.60) // Chillán
        "Biobío" -> Pair(-72.35, -37.46) // Los Ángeles
        "La Araucanía" -> Pair(-72.58, -38.73) // Temuco
        "Los Ríos" -> Pair(-73.24, -39.81) // Valdivia
        "Los Lagos" -> Pair(-73.10, -40.57) // Osorno
        "Aysén" -> Pair(-72.06, -45.57) // Coyhaique
        "Magallanes" -> Pair(-70.93, -53.16) // Punta Arenas
        else -> Pair(-70.93, -33.68) // Default (Metropolitana)
    }
}
