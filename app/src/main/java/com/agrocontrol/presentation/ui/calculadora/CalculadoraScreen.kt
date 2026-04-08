package com.agrocontrol.presentation.ui.calculadora

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agrocontrol.presentation.theme.*
import kotlin.math.roundToInt

@Composable
fun CalculadoraScreen() {
    var precioKg         by remember { mutableStateOf("") }
    var costoSemilla     by remember { mutableStateOf("") }
    var costoFertilizante by remember { mutableStateOf("") }
    var costoAgua        by remember { mutableStateOf("") }
    var costoManoObra    by remember { mutableStateOf("") }
    var rendimientoKgHa  by remember { mutableStateOf("") }
    var hectareas        by remember { mutableStateOf("") }

    val resultado = remember(precioKg, costoSemilla, costoFertilizante, costoAgua, costoManoObra, rendimientoKgHa, hectareas) {
        calcular(precioKg, costoSemilla, costoFertilizante, costoAgua, costoManoObra, rendimientoKgHa, hectareas)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(Brush.horizontalGradient(listOf(Color(0xFF7C2D12), NaranjaAmanecer))),
            contentAlignment = Alignment.CenterStart
        ) {
            Column(Modifier.padding(horizontal = 24.dp)) {
                Text("💰", fontSize = 32.sp)
                Text(
                    "Calculadora de ROI",
                    fontWeight = FontWeight.ExtraBold,
                    color = Blanco,
                    fontSize = 22.sp,
                    fontFamily = PlusJakartaSansFamily
                )
                Text(
                    "Estima la rentabilidad de tu cultivo",
                    color = Blanco.copy(0.8f),
                    fontSize = 13.sp,
                    fontFamily = PlusJakartaSansFamily
                )
            }
        }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Sección: Producción
            SectionTitle("📊 Producción", Icons.Default.Agriculture)
            InputCampo("Rendimiento estimado (kg/ha)", rendimientoKgHa, { rendimientoKgHa = it }, "Obtenlo de la predicción IA")
            InputCampo("Hectáreas sembradas", hectareas, { hectareas = it })
            InputCampo("Precio de venta (CLP/kg)", precioKg, { precioKg = it }, "Precio en el mercado local")

            Spacer(Modifier.height(4.dp))

            // Sección: Costos
            SectionTitle("💸 Costos (CLP / ha)", Icons.Default.MoneyOff)
            InputCampo("Semilla o plantines", costoSemilla, { costoSemilla = it })
            InputCampo("Fertilizantes", costoFertilizante, { costoFertilizante = it })
            InputCampo("Riego y agua", costoAgua, { costoAgua = it })
            InputCampo("Mano de obra", costoManoObra, { costoManoObra = it })

            Spacer(Modifier.height(8.dp))

            // Resultado
            AnimatedVisibility(
                visible = resultado != null,
                enter = slideInVertically { it / 2 } + fadeIn()
            ) {
                resultado?.let { r ->
                    ResultadoCard(r)
                }
            }

            if (resultado == null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, null, tint = GrisMedio)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Completa los campos para ver el resultado.",
                            color = GrisMedio,
                            fontFamily = PlusJakartaSansFamily,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

data class ResultadoROI(
    val ingresoTotal: Long,
    val costoTotal: Long,
    val utilidadNeta: Long,
    val roiPct: Int,
    val puntoCero: Double,
    val semilla: Double,
    val fert: Double,
    val agua: Double,
    val mo: Double
)

private fun calcular(
    precioKg: String, costoSemilla: String, costoFert: String,
    costoAgua: String, costoMO: String, rendimientoKgHa: String, ha: String
): ResultadoROI? {
    val precio     = precioKg.toDoubleOrNull()         ?: return null
    val semilla    = costoSemilla.toDoubleOrNull()      ?: 0.0
    val fert       = costoFert.toDoubleOrNull()         ?: 0.0
    val agua       = costoAgua.toDoubleOrNull()         ?: 0.0
    val mo         = costoMO.toDoubleOrNull()           ?: 0.0
    val rendim     = rendimientoKgHa.toDoubleOrNull()   ?: return null
    val hectareas  = ha.toDoubleOrNull()                ?: return null
    if (rendim <= 0 || hectareas <= 0 || precio <= 0) return null

    val costoXha   = semilla + fert + agua + mo
    val costoTotal = costoXha * hectareas
    val kgTotal    = rendim * hectareas
    val ingreso    = kgTotal * precio
    val utilidad   = ingreso - costoTotal
    val roi        = if (costoTotal > 0) ((utilidad / costoTotal) * 100).roundToInt() else 0
    val puntoCero  = if (precio > 0) costoTotal / precio else 0.0

    return ResultadoROI(ingreso.toLong(), costoTotal.toLong(), utilidad.toLong(), roi, puntoCero, semilla, fert, agua, mo)
}

@Composable
private fun ResultadoCard(r: ResultadoROI) {
    val esPositivo = r.utilidadNeta >= 0
    val fmtCLP = { v: Long -> "$ ${"%,d".format(v).replace(',', '.')}" }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (esPositivo) Verde60 else RojoAlert
        ),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                if (esPositivo) "✅ Cultivo rentable" else "⚠️ Cultivo en pérdidas",
                fontWeight = FontWeight.ExtraBold, color = Blanco,
                fontSize = 18.sp, fontFamily = PlusJakartaSansFamily
            )

            ResultRow("Ingreso total",   fmtCLP(r.ingresoTotal),  Blanco)
            ResultRow("Costo total",     fmtCLP(r.costoTotal),    Blanco.copy(0.8f))
            HorizontalDivider(color = Blanco.copy(0.3f))
            ResultRow("Utilidad neta",   fmtCLP(r.utilidadNeta),  Blanco, bold = true)
            ResultRow("ROI estimado",    "${r.roiPct}%",           Blanco, bold = true)
            ResultRow("Punto de equilibrio", "${"%,.0f".format(r.puntoCero)} kg totales", Blanco.copy(0.8f))
            
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Blanco.copy(0.3f))
            Text("Distribución de Costos", fontWeight = FontWeight.Bold, color = Blanco, fontSize = 15.sp, fontFamily = PlusJakartaSansFamily)
            
            val chartData = listOf(
                com.agrocontrol.presentation.ui.components.DonutChartData(r.semilla.toFloat(), NaranjaAmanecer, "Semillas"),
                com.agrocontrol.presentation.ui.components.DonutChartData(r.fert.toFloat(), AmarilloAlert, "Ferti."),
                com.agrocontrol.presentation.ui.components.DonutChartData(r.agua.toFloat(), AzulInfo, "Agua"),
                com.agrocontrol.presentation.ui.components.DonutChartData(r.mo.toFloat(), Color(0xFFF472B6), "M.O.")
            ).filter { it.value > 0f }

            if (chartData.isNotEmpty()) {
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    com.agrocontrol.presentation.ui.components.DonutChart(
                        data = chartData,
                        modifier = Modifier.size(100.dp),
                        thickness = 25f,
                        centerText = "Gastos"
                    )
                    Spacer(Modifier.width(24.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        chartData.forEach { d ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).background(d.color, shape = androidx.compose.foundation.shape.CircleShape))
                                Spacer(Modifier.width(8.dp))
                                Text(d.label, color = Blanco.copy(0.9f), fontSize = 12.sp, fontFamily = PlusJakartaSansFamily)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String, color: Color, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = color.copy(0.85f), fontFamily = PlusJakartaSansFamily, fontSize = 13.sp)
        Text(value, color = color, fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
            fontFamily = PlusJakartaSansFamily, fontSize = 14.sp)
    }
}

@Composable
private fun SectionTitle(text: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text, fontWeight = FontWeight.Bold, fontFamily = PlusJakartaSansFamily,
            fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
private fun InputCampo(
    label: String, value: String, onValueChange: (String) -> Unit,
    supportText: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontFamily = PlusJakartaSansFamily, fontSize = 13.sp) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        supportingText = if (supportText != null) {{ Text(supportText, fontSize = 11.sp) }} else null,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Verde60,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}
