package com.agrocontrol.presentation.ui.inventario

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.agrocontrol.domain.model.Insumo
import com.agrocontrol.presentation.theme.*

val UNIDADES = listOf("kg", "litros", "sacos", "unidades", "toneladas", "gramos")

private val InvBase    = Color(0xFF030F07)
private val InvCard    = Color(0xFF0C1E10)
private val InvBorder  = Color(0xFF1A3A1F)
private val GreenNeon  = Color(0xFF4ADE80)
private val TextPrim   = Color(0xFFF0FFF4)
private val TextSec    = Color(0xFF86EFAC)
private val TextMuted  = Color(0xFF4B7160)
private val RedVivid   = Color(0xFFF87171)
private val AmberVivid = Color(0xFFFBBF24)
private val BlueVivid  = Color(0xFF60A5FA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventarioScreen(
    onBack: () -> Unit,
    viewModel: InventarioViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var nombreInsumo    by remember { mutableStateOf("") }
    var cantidadInsumo  by remember { mutableStateOf("") }
    var unidadInsumo    by remember { mutableStateOf("kg") }
    var minimoInsumo    by remember { mutableStateOf("") }
    var unidadDropdown  by remember { mutableStateOf(false) }

    if (state.showAddDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideAddDialog(); nombreInsumo = ""; cantidadInsumo = ""; minimoInsumo = "" },
            title = {
                Text(
                    "Agregar insumo",
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.Bold,
                    color = TextPrim
                )
            },
            containerColor = Color(0xFF0F2714),
            titleContentColor = TextPrim,
            textContentColor = TextSec,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    InvTextField(nombreInsumo, { nombreInsumo = it; viewModel.clearError() }, "Nombre del insumo *")
                    InvTextField(cantidadInsumo, { cantidadInsumo = it }, "Cantidad actual *", KeyboardType.Decimal)

                    ExposedDropdownMenuBox(expanded = unidadDropdown, onExpandedChange = { unidadDropdown = it }) {
                        InvTextField(unidadInsumo, {}, "Unidad *",
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(unidadDropdown) },
                            modifier = Modifier.menuAnchor(), readOnly = true
                        )
                        ExposedDropdownMenu(
                            expanded = unidadDropdown,
                            onDismissRequest = { unidadDropdown = false },
                            modifier = Modifier.background(Color(0xFF0F2714))
                        ) {
                            UNIDADES.forEach { u ->
                                DropdownMenuItem(
                                    text = { Text(u, color = TextPrim, fontFamily = PlusJakartaSansFamily) },
                                    onClick = { unidadInsumo = u; unidadDropdown = false }
                                )
                            }
                        }
                    }

                    InvTextField(minimoInsumo, { minimoInsumo = it }, "Cantidad mínima de alerta *", KeyboardType.Decimal,
                        supportText = "Se alertará cuando baje de este nivel")

                    state.error?.let {
                        Text(it, color = RedVivid, fontSize = 12.sp, fontFamily = PlusJakartaSansFamily)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.agregarInsumo(nombreInsumo, cantidadInsumo, unidadInsumo, minimoInsumo) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenNeon, contentColor = Color.Black)
                ) {
                    Text("Agregar", fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideAddDialog(); nombreInsumo = ""; cantidadInsumo = ""; minimoInsumo = "" }) {
                    Text("Cancelar", color = TextMuted, fontFamily = PlusJakartaSansFamily)
                }
            }
        )
    }

    Scaffold(
        containerColor = InvBase,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Inventario",
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        color = TextPrim
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = TextSec)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = InvBase)
            )
        },
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFF16A34A), Color(0xFF15803D))))
                    .clickable { viewModel.showAddDialog() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }
    ) { padding ->
        if (state.insumos.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        Modifier.size(88.dp).background(BlueVivid.copy(0.08f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) { Text("📦", fontSize = 40.sp) }
                    Text("Sin insumos registrados", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrim, fontFamily = PlusJakartaSansFamily)
                    Text("Agrega fertilizantes, pesticidas\no semillas a tu inventario", color = TextMuted, fontFamily = PlusJakartaSansFamily, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val criticos = state.insumos.filter { it.enStockCritico }
                if (criticos.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(RedVivid.copy(0.08f))
                                .drawBehind {
                                    drawLine(RedVivid.copy(0.35f), Offset(0f, 0f), Offset(size.width, 0f), 1.5f)
                                }
                                .padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier.size(36.dp).background(RedVivid.copy(0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Warning, null, tint = RedVivid, modifier = Modifier.size(18.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "${criticos.size} insumo${if (criticos.size > 1) "s" else ""} en stock crítico",
                                        color = RedVivid,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = PlusJakartaSansFamily
                                    )
                                    Text(
                                        criticos.joinToString(", ") { it.nombre },
                                        color = RedVivid.copy(0.6f),
                                        fontSize = 11.sp,
                                        fontFamily = PlusJakartaSansFamily
                                    )
                                }
                            }
                        }
                    }
                }

                // Header stat row
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        InvStatBox("Total", "${state.insumos.size}", BlueVivid, Modifier.weight(1f))
                        InvStatBox("Críticos", "${criticos.size}", RedVivid, Modifier.weight(1f))
                        InvStatBox("OK", "${state.insumos.size - criticos.size}", GreenNeon, Modifier.weight(1f))
                    }
                }

                items(state.insumos) { insumo ->
                    InsumoCard(
                        insumo = insumo,
                        onMovimiento = { id, cant, tipo -> viewModel.registrarMovimiento(id, cant, tipo) },
                        onEliminar = { viewModel.eliminarInsumo(insumo) }
                    )
                }
            }
        }
    }
}

@Composable
private fun InvStatBox(label: String, value: String, color: Color, modifier: Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(InvCard)
            .drawBehind {
                drawLine(color.copy(0.3f), Offset(0f, 0f), Offset(size.width, 0f), 1.5f)
            }
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = color, fontSize = 22.sp, fontWeight = FontWeight.Black, fontFamily = PlusJakartaSansFamily)
            Text(label, color = TextMuted, fontSize = 11.sp, fontFamily = PlusJakartaSansFamily)
        }
    }
}

@Composable
fun InsumoCard(insumo: Insumo, onMovimiento: (Long, String, String) -> Unit, onEliminar: () -> Unit) {
    var showMovimientoDialog by remember { mutableStateOf(false) }
    var tipoMovimiento       by remember { mutableStateOf("entrada") }
    var cantidadMov          by remember { mutableStateOf("") }

    if (showMovimientoDialog) {
        AlertDialog(
            onDismissRequest = { showMovimientoDialog = false; cantidadMov = "" },
            containerColor = Color(0xFF0F2714),
            titleContentColor = TextPrim,
            title = { Text(if (tipoMovimiento == "entrada") "Registrar entrada" else "Registrar salida", fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val entradaSelected = tipoMovimiento == "entrada"
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (entradaSelected) GreenNeon.copy(0.2f) else InvCard)
                                .clickable { tipoMovimiento = "entrada" }
                                .drawBehind { if (entradaSelected) drawLine(GreenNeon.copy(0.5f), Offset(0f,0f), Offset(size.width,0f), 1f) }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text("📈 Entrada", color = if (entradaSelected) GreenNeon else TextMuted, fontSize = 13.sp, fontFamily = PlusJakartaSansFamily)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (!entradaSelected) RedVivid.copy(0.2f) else InvCard)
                                .clickable { tipoMovimiento = "salida" }
                                .drawBehind { if (!entradaSelected) drawLine(RedVivid.copy(0.5f), Offset(0f,0f), Offset(size.width,0f), 1f) }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text("📉 Salida", color = if (!entradaSelected) RedVivid else TextMuted, fontSize = 13.sp, fontFamily = PlusJakartaSansFamily)
                        }
                    }
                    InvTextField(cantidadMov, { cantidadMov = it }, "Cantidad (${insumo.unidad})", KeyboardType.Decimal)
                }
            },
            confirmButton = {
                Button(
                    onClick = { onMovimiento(insumo.id, cantidadMov, tipoMovimiento); showMovimientoDialog = false; cantidadMov = "" },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenNeon, contentColor = Color.Black)
                ) { Text("Registrar", fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showMovimientoDialog = false }) {
                    Text("Cancelar", color = TextMuted, fontFamily = PlusJakartaSansFamily)
                }
            }
        )
    }

    val isCritico = insumo.enStockCritico
    val levelColor = if (isCritico) RedVivid else GreenNeon
    val progress = if (insumo.cantidadMinima > 0)
        (insumo.cantidadActual / (insumo.cantidadMinima * 3f)).toFloat().coerceIn(0f, 1f)
    else 1f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(InvCard)
            .drawBehind {
                drawLine(if (isCritico) RedVivid.copy(0.4f) else InvBorder, Offset(0f,0f), Offset(size.width,0f), 1.5f)
                // Left bar
                drawRect(
                    brush = Brush.verticalGradient(listOf(levelColor.copy(0.8f), levelColor.copy(0.1f))),
                    topLeft = Offset(0f, 0f),
                    size = Size(3f, size.height)
                )
            }
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(levelColor.copy(0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (isCritico) "⚠️" else "📦", fontSize = 20.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(insumo.nombre, fontWeight = FontWeight.Bold, color = TextPrim, fontFamily = PlusJakartaSansFamily)
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "${insumo.cantidadActual}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = levelColor,
                            fontFamily = PlusJakartaSansFamily
                        )
                        Text(
                            insumo.unidad,
                            fontSize = 13.sp,
                            color = TextMuted,
                            fontFamily = PlusJakartaSansFamily,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                    Text(
                        "Mín: ${insumo.cantidadMinima} ${insumo.unidad}",
                        fontSize = 10.sp,
                        color = TextMuted,
                        fontFamily = PlusJakartaSansFamily
                    )
                }
                Row {
                    IconButton(
                        onClick = { showMovimientoDialog = true },
                        modifier = Modifier.size(36.dp).background(GreenNeon.copy(0.1f), CircleShape)
                    ) {
                        Icon(Icons.Default.SwapVert, null, tint = GreenNeon, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(6.dp))
                    IconButton(
                        onClick = onEliminar,
                        modifier = Modifier.size(36.dp).background(RedVivid.copy(0.1f), CircleShape)
                    ) {
                        Icon(Icons.Default.Delete, null, tint = RedVivid, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Progress bar
            Column {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Nivel de stock", fontSize = 10.sp, color = TextMuted, fontFamily = PlusJakartaSansFamily)
                    Text("${(progress * 100).toInt()}%", fontSize = 10.sp, color = levelColor, fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(5.dp))
                Box(Modifier.fillMaxWidth().height(5.dp).clip(CircleShape).background(levelColor.copy(0.1f))) {
                    Box(Modifier.fillMaxHeight().fillMaxWidth(progress).clip(CircleShape).background(
                        Brush.horizontalGradient(listOf(levelColor, levelColor.copy(0.6f)))
                    ))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InvTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    supportText: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontFamily = PlusJakartaSansFamily, fontSize = 13.sp, color = TextMuted) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        readOnly = readOnly,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        supportingText = supportText?.let { { Text(it, fontSize = 10.sp, color = TextMuted, fontFamily = PlusJakartaSansFamily) } },
        trailingIcon = trailingIcon,
        shape = RoundedCornerShape(12.dp),
        textStyle = androidx.compose.ui.text.TextStyle(color = TextPrim, fontFamily = PlusJakartaSansFamily),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GreenNeon.copy(0.6f),
            unfocusedBorderColor = InvBorder,
            focusedContainerColor = Color(0xFF0D2010),
            unfocusedContainerColor = Color(0xFF0A1A0D),
            cursorColor = GreenNeon,
            focusedLabelColor = GreenNeon.copy(0.8f)
        )
    )
}

private val KeyboardType = androidx.compose.ui.text.input.KeyboardType
