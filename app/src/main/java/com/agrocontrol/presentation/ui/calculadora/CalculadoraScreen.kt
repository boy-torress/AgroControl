package com.agrocontrol.presentation.ui.calculadora

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agrocontrol.presentation.theme.*
import kotlin.math.roundToInt

private val CalcBase   = Color(0xFF030F07)
private val CalcCard   = Color(0xFF0C1E10)
private val CalcBorder = Color(0xFF1A3A1F)
private val GreenNeon  = Color(0xFF4ADE80)
private val TextPrim   = Color(0xFFF0FFF4)
private val TextSec    = Color(0xFF86EFAC)
private val TextMuted  = Color(0xFF4B7160)
private val GoldAccent = Color(0xFFFBBF24)
private val RedVivid   = Color(0xFFF87171)
private val BlueAccent = Color(0xFF60A5FA)
private val PinkAccent = Color(0xFFF472B6)

@Composable
fun CalculadoraScreen() {
    var precioKg          by remember { mutableStateOf("") }
    var costoSemilla      by remember { mutableStateOf("") }
    var costoFertilizante by remember { mutableStateOf("") }
    var costoAgua         by remember { mutableStateOf("") }
    var costoManoObra     by remember { mutableStateOf("") }
    var rendimientoKgHa   by remember { mutableStateOf("") }
    var hectareas         by remember { mutableStateOf("") }

    val resultado = remember(precioKg, costoSemilla, costoFertilizante, costoAgua, costoManoObra, rendimientoKgHa, hectareas) {
        calcular(precioKg, costoSemilla, costoFertilizante, costoAgua, costoManoObra, rendimientoKgHa, hectareas)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CalcBase)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Header ──────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRect(Brush.verticalGradient(listOf(Color(0xFF0D1F0A), CalcBase)))
                    // Decorative circle
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(GoldAccent.copy(0.15f), Color.Transparent),
                            center = Offset(size.width * 0.9f, size.height * 0.2f),
                            radius = size.width * 0.4f
                        ),
                        radius = size.width * 0.4f,
                        center = Offset(size.width * 0.9f, size.height * 0.2f)
                    )
                }
                .padding(top = 52.dp, start = 20.dp, end = 20.dp, bottom = 24.dp)
        ) {
            Column {
                Box(
                    Modifier.size(56.dp).clip(RoundedCornerShape(18.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF7C2D12), Color(0xFFC2410C)))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💰", fontSize = 28.sp)
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    "Calculadora de ROI",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrim,
                    fontFamily = PlusJakartaSansFamily,
                    letterSpacing = (-0.5).sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Estima la rentabilidad de tu cultivo",
                    color = TextMuted,
                    fontFamily = PlusJakartaSansFamily,
                    fontSize = 14.sp
                )
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // ── Sección Producción ────────────────────────────────────────────
            CalcSection("📊 Producción")
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CalcInputField("Rendimiento estimado (kg/ha)", rendimientoKgHa, { rendimientoKgHa = it }, "Obtenlo de la predicción IA", BlueAccent)
                CalcInputField("Hectáreas sembradas", hectareas, { hectareas = it }, color = GreenNeon)
                CalcInputField("Precio de venta (CLP/kg)", precioKg, { precioKg = it }, "Precio en el mercado local", GoldAccent)
            }

            // Divider
            Box(Modifier.fillMaxWidth().height(1.dp).background(
                Brush.horizontalGradient(listOf(Color.Transparent, CalcBorder, Color.Transparent))
            ))

            // ── Sección Costos ────────────────────────────────────────────────
            CalcSection("💸 Costos (CLP / ha)")
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CalcInputField("Semilla o plantines", costoSemilla, { costoSemilla = it }, color = GoldAccent)
                CalcInputField("Fertilizantes", costoFertilizante, { costoFertilizante = it }, color = GreenNeon)
                CalcInputField("Riego y agua", costoAgua, { costoAgua = it }, color = BlueAccent)
                CalcInputField("Mano de obra", costoManoObra, { costoManoObra = it }, color = PinkAccent)
            }

            // ── Resultado ─────────────────────────────────────────────────────
            AnimatedVisibility(
                visible = resultado != null,
                enter = slideInVertically { it / 2 } + fadeIn()
            ) {
                resultado?.let { r -> ResultadoCard(r) }
            }

            if (resultado == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(CalcCard)
                        .drawBehind {
                            drawLine(CalcBorder, Offset(0f,0f), Offset(size.width,0f), 1f)
                        }
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Info, null, tint = TextMuted, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Completa los campos de producción y costos\npara calcular el retorno de inversión.",
                            color = TextMuted,
                            fontFamily = PlusJakartaSansFamily,
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun CalcSection(title: String) {
    Text(
        title,
        fontWeight = FontWeight.Bold,
        fontFamily = PlusJakartaSansFamily,
        fontSize = 14.sp,
        color = TextSec.copy(0.7f),
        letterSpacing = 0.3.sp
    )
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
    val precio    = precioKg.toDoubleOrNull()       ?: return null
    val semilla   = costoSemilla.toDoubleOrNull()   ?: 0.0
    val fert      = costoFert.toDoubleOrNull()      ?: 0.0
    val agua      = costoAgua.toDoubleOrNull()      ?: 0.0
    val mo        = costoMO.toDoubleOrNull()        ?: 0.0
    val rendim    = rendimientoKgHa.toDoubleOrNull() ?: return null
    val hectareas = ha.toDoubleOrNull()             ?: return null
    if (rendim <= 0 || hectareas <= 0 || precio <= 0) return null

    val costoXha  = semilla + fert + agua + mo
    val costoTotal = costoXha * hectareas
    val kgTotal   = rendim * hectareas
    val ingreso   = kgTotal * precio
    val utilidad  = ingreso - costoTotal
    val roi       = if (costoTotal > 0) ((utilidad / costoTotal) * 100).roundToInt() else 0
    val puntoCero = if (precio > 0) costoTotal / precio else 0.0

    return ResultadoROI(ingreso.toLong(), costoTotal.toLong(), utilidad.toLong(), roi, puntoCero, semilla, fert, agua, mo)
}

@Composable
private fun ResultadoCard(r: ResultadoROI) {
    val esPositivo = r.utilidadNeta >= 0
    val mainColor  = if (esPositivo) GreenNeon else RedVivid
    val fmtCLP = { v: Long -> "$ ${"%,d".format(v).replace(',', '.')}" }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CalcCard)
            .drawBehind {
                drawLine(mainColor.copy(0.5f), Offset(0f,0f), Offset(size.width,0f), 2f)
                drawRect(
                    brush = Brush.verticalGradient(listOf(mainColor.copy(0.08f), Color.Transparent)),
                    topLeft = Offset(0f, 0f),
                    size = Size(size.width, size.height * 0.3f)
                )
            }
    ) {
        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {

            // Status badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(40.dp).background(mainColor.copy(0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (esPositivo) "✅" else "⚠️", fontSize = 18.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        if (esPositivo) "Cultivo rentable" else "Cultivo en pérdidas",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = mainColor,
                        fontFamily = PlusJakartaSansFamily
                    )
                    Text(
                        "ROI: ${r.roiPct}%",
                        color = mainColor.copy(0.7f),
                        fontSize = 13.sp,
                        fontFamily = PlusJakartaSansFamily
                    )
                }
            }

            // Divider
            Box(Modifier.fillMaxWidth().height(1.dp).background(CalcBorder))

            // Key metrics
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MetricBox("Ingreso total", fmtCLP(r.ingresoTotal), GreenNeon, Modifier.weight(1f))
                Spacer(Modifier.width(10.dp))
                MetricBox("Costo total", fmtCLP(r.costoTotal), RedVivid, Modifier.weight(1f))
            }

            Spacer(Modifier.height(2.dp))

            // Utilidad neta
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(mainColor.copy(0.08f))
                    .padding(14.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Utilidad neta", color = TextSec, fontFamily = PlusJakartaSansFamily, fontSize = 13.sp)
                    Text(
                        fmtCLP(r.utilidadNeta),
                        color = mainColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        fontFamily = PlusJakartaSansFamily
                    )
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Punto de equilibrio", color = TextMuted, fontFamily = PlusJakartaSansFamily, fontSize = 12.sp)
                Text("${"%,.0f".format(r.puntoCero)} kg", color = GoldAccent, fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            // Cost breakdown
            Box(Modifier.fillMaxWidth().height(1.dp).background(CalcBorder))

            Text("Distribución de costos", fontWeight = FontWeight.SemiBold, color = TextSec, fontFamily = PlusJakartaSansFamily, fontSize = 13.sp)

            val chartData = listOf(
                Triple(r.semilla.toFloat(), GoldAccent, "Semillas"),
                Triple(r.fert.toFloat(),  GreenNeon,  "Fertiliz."),
                Triple(r.agua.toFloat(),  BlueAccent, "Agua"),
                Triple(r.mo.toFloat(),    PinkAccent, "M. Obra")
            ).filter { it.first > 0f }

            if (chartData.isNotEmpty()) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    // Mini donut chart via Canvas
                    MiniDonutChart(chartData, Modifier.size(88.dp))
                    Spacer(Modifier.width(20.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        val total = chartData.sumOf { it.first.toDouble() }
                        chartData.forEach { (value, color, label) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(8.dp).background(color, CircleShape))
                                Spacer(Modifier.width(8.dp))
                                Text(label, color = TextSec.copy(0.8f), fontSize = 12.sp, fontFamily = PlusJakartaSansFamily, modifier = Modifier.weight(1f))
                                Text(
                                    "${((value / total) * 100).roundToInt()}%",
                                    color = color,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = PlusJakartaSansFamily
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricBox(label: String, value: String, color: Color, modifier: Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(0.07f))
            .padding(12.dp)
    ) {
        Column {
            Text(label, color = TextMuted, fontSize = 10.sp, fontFamily = PlusJakartaSansFamily)
            Spacer(Modifier.height(3.dp))
            Text(value, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = PlusJakartaSansFamily)
        }
    }
}

@Composable
private fun MiniDonutChart(data: List<Triple<Float, Color, String>>, modifier: Modifier) {
    val total = data.sumOf { it.first.toDouble() }.toFloat()
    if (total == 0f) return

    androidx.compose.foundation.Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val outerRadius = size.minDimension / 2f
        val strokeWidth = outerRadius * 0.35f
        val radius = outerRadius - strokeWidth / 2f
        var startAngle = -90f

        data.forEach { (value, color, _) ->
            val sweep = (value / total) * 360f
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweep - 2f,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                ),
                topLeft = Offset(cx - radius, cy - radius),
                size = Size(radius * 2, radius * 2)
            )
            startAngle += sweep
        }
    }
}

@Composable
private fun CalcInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    supportText: String? = null,
    color: Color = GreenNeon
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontFamily = PlusJakartaSansFamily, fontSize = 12.sp, color = TextMuted) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        supportingText = supportText?.let { { Text(it, fontSize = 10.sp, color = TextMuted.copy(0.7f), fontFamily = PlusJakartaSansFamily) } },
        shape = RoundedCornerShape(14.dp),
        textStyle = androidx.compose.ui.text.TextStyle(color = TextPrim, fontFamily = PlusJakartaSansFamily),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = color.copy(0.7f),
            unfocusedBorderColor = CalcBorder,
            focusedContainerColor = Color(0xFF0D2010),
            unfocusedContainerColor = Color(0xFF0A1A0D),
            cursorColor = color,
            focusedLabelColor = color.copy(0.8f)
        )
    )
}
