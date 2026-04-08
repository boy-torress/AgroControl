package com.agrocontrol.presentation.ui.cultivo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agrocontrol.presentation.theme.PlusJakartaSansFamily
import com.agrocontrol.presentation.theme.Verde60
import com.agrocontrol.presentation.theme.VerdeNeon
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf

@Composable
fun RendimientoChart() {
    // Datos simulados (Ejemplo de evolución de predicciones de rendimiento kg/ha durante las últimas semanas)
    // En una iteración futura esto vendría de un listado "HistorialPredicciones" de Room.
    val chartEntryModel = remember {
        entryModelOf(
            0 to 5200f,
            1 to 5350f,
            2 to 5500f,
            3 to 5800f,
            4 to 5750f,
            5 to 6100f
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth().height(260.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Evolución de Rendimiento Esperado",
                fontWeight = FontWeight.Bold,
                fontFamily = PlusJakartaSansFamily,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Kg/ha proyectados por semana cruzando IA y Clima",
                fontSize = 12.sp,
                fontFamily = PlusJakartaSansFamily,
                color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Chart(
                chart = lineChart(
                    lines = listOf(
                        com.patrykandpatrick.vico.compose.chart.line.lineSpec(
                            lineColor = VerdeNeon,
                            lineBackgroundShader = com.patrykandpatrick.vico.compose.component.shape.shader.verticalGradient(
                                arrayOf(VerdeNeon.copy(alpha = 0.6f), Color.Transparent)
                            )
                        )
                    )
                ),
                model = chartEntryModel,
                startAxis = rememberStartAxis(
                    valueFormatter = { value, _ -> "${value.toInt()}" }
                ),
                bottomAxis = rememberBottomAxis(
                    valueFormatter = { value, _ -> "${value.toInt() + 1}" }
                ),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
