package com.agrocontrol.presentation.ui.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agrocontrol.presentation.theme.*

private val AdBg      = Color(0xFF070A0F)
private val AdSurface = Color(0xFF0C1018)
private val AdCard    = Color(0xFF0F1421)
private val AdBorder  = Color(0xFF192135)
private val AdBlue    = Color(0xFF60A5FA)
private val AdGreen   = Color(0xFF4ADE80)
private val AdRed     = Color(0xFFF87171)
private val AdAmber   = Color(0xFFFBBF24)
private val AdMuted   = Color(0xFF6B7280)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    onLogout  : () -> Unit,
    viewModel : AdminViewModel = hiltViewModel()
) {
    val state         by viewModel.uiState.collectAsState()
    var tabIndex      by remember { mutableStateOf(0) }
    var estadoDropdown by remember { mutableStateOf(false) }
    var tipoDropdown  by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = AdBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Panel Admin", color = Color.White, fontWeight = FontWeight.Black, fontSize = 19.sp, fontFamily = PlusJakartaSansFamily)
                        Text("Control total del sistema", color = AdMuted, fontSize = 11.sp, fontFamily = PlusJakartaSansFamily)
                    }
                },
                actions = {
                    IconButton(
                        onClick  = { viewModel.logout(); onLogout() },
                        modifier = Modifier.size(36.dp).background(AdRed.copy(0.1f), RoundedCornerShape(10.dp)).border(1.dp, AdRed.copy(0.2f), RoundedCornerShape(10.dp))
                    ) {
                        Icon(Icons.Default.Logout, null, tint = AdRed, modifier = Modifier.size(17.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AdBg)
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // ── Tabs personalizados ──────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Triple("Cultivos", Icons.Default.Agriculture, 0),
                    Triple("Usuarios", Icons.Default.People, 1)
                ).forEach { (label, icon, idx) ->
                    val isSelected = tabIndex == idx
                    Box(
                        modifier = Modifier.weight(1f).height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) AdBlue.copy(0.15f) else AdCard
                            )
                            .border(
                                1.dp,
                                if (isSelected) AdBlue.copy(0.35f) else AdBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { tabIndex = idx },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(icon, null, tint = if (isSelected) AdBlue else AdMuted, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(label, color = if (isSelected) AdBlue else AdMuted, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, fontFamily = PlusJakartaSansFamily, fontSize = 13.sp)
                        }
                    }
                }
            }

            when (tabIndex) {
                0 -> {
                    // ── Tab Cultivos ─────────────────────────────────────────
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Filtros
                        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Filtro estado
                            ExposedDropdownMenuBox(expanded = estadoDropdown, onExpandedChange = { estadoDropdown = it }, modifier = Modifier.weight(1f)) {
                                AdFilterField(
                                    value    = state.filtroEstado.replaceFirstChar { it.uppercase() },
                                    label    = "Estado",
                                    expanded = estadoDropdown,
                                    modifier = Modifier.menuAnchor()
                                )
                                ExposedDropdownMenu(expanded = estadoDropdown, onDismissRequest = { estadoDropdown = false }, containerColor = AdSurface) {
                                    listOf("todos", "activo", "inactivo").forEach { e ->
                                        DropdownMenuItem(text = { Text(e.replaceFirstChar { it.uppercase() }, color = Color.White, fontFamily = PlusJakartaSansFamily) },
                                            onClick = { viewModel.setFiltroEstado(e); estadoDropdown = false })
                                    }
                                }
                            }
                            // Filtro tipo
                            ExposedDropdownMenuBox(expanded = tipoDropdown, onExpandedChange = { tipoDropdown = it }, modifier = Modifier.weight(1f)) {
                                AdFilterField(
                                    value    = if (state.filtroTipo == "todos") "Todos" else state.filtroTipo,
                                    label    = "Tipo",
                                    expanded = tipoDropdown,
                                    modifier = Modifier.menuAnchor()
                                )
                                ExposedDropdownMenu(expanded = tipoDropdown, onDismissRequest = { tipoDropdown = false }, containerColor = AdSurface) {
                                    DropdownMenuItem(text = { Text("Todos", color = Color.White, fontFamily = PlusJakartaSansFamily) }, onClick = { viewModel.setFiltroTipo("todos"); tipoDropdown = false })
                                    state.tiposCultivo.forEach { t ->
                                        DropdownMenuItem(text = { Text(t, color = Color.White, fontFamily = PlusJakartaSansFamily) }, onClick = { viewModel.setFiltroTipo(t); tipoDropdown = false })
                                    }
                                }
                            }
                        }

                        // Contador
                        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("${state.cultivosFiltrados.size} registros", color = AdMuted, fontSize = 12.sp, fontFamily = PlusJakartaSansFamily, modifier = Modifier.weight(1f))
                            Box(
                                modifier = Modifier.background(AdBlue.copy(0.1f), RoundedCornerShape(8.dp)).border(1.dp, AdBlue.copy(0.2f), RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Download, null, tint = AdBlue, modifier = Modifier.size(13.dp))
                                    Spacer(Modifier.width(5.dp))
                                    Text("CSV", color = AdBlue, fontSize = 11.sp, fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (state.cultivosFiltrados.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Sin cultivos con los filtros seleccionados", color = AdMuted, fontFamily = PlusJakartaSansFamily, fontSize = 14.sp)
                            }
                        } else {
                            LazyColumn(contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(state.cultivosFiltrados) { cultivo ->
                                    Box(
                                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(AdCard).border(1.dp, AdBorder, RoundedCornerShape(14.dp)).padding(14.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier.size(40.dp).background(AdGreen.copy(0.1f), RoundedCornerShape(12.dp)).border(1.dp, AdGreen.copy(0.2f), RoundedCornerShape(12.dp)),
                                                contentAlignment = Alignment.Center
                                            ) { Text("🌾", fontSize = 18.sp) }
                                            Spacer(Modifier.width(12.dp))
                                            Column(Modifier.weight(1f)) {
                                                Text(cultivo.tipoCultivo, color = Color.White, fontWeight = FontWeight.Bold, fontFamily = PlusJakartaSansFamily, fontSize = 14.sp)
                                                Text("${cultivo.region} · ${cultivo.hectareas} ha", color = AdMuted, fontSize = 11.sp, fontFamily = PlusJakartaSansFamily)
                                                Text(cultivo.etapaActual.name, color = AdBlue.copy(0.8f), fontSize = 11.sp, fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Medium)
                                            }
                                            Box(
                                                modifier = Modifier.background(
                                                    if (cultivo.activo) AdGreen.copy(0.1f) else AdMuted.copy(0.1f),
                                                    RoundedCornerShape(8.dp)
                                                ).border(1.dp, if (cultivo.activo) AdGreen.copy(0.25f) else AdMuted.copy(0.2f), RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    if (cultivo.activo) "Activo" else "Inactivo",
                                                    color = if (cultivo.activo) AdGreen else AdMuted,
                                                    fontSize = 10.sp,
                                                    fontFamily = PlusJakartaSansFamily,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    letterSpacing = 0.3.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // ── Tab Usuarios ─────────────────────────────────────────
                    LazyColumn(contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Estadísticas por rol
                        item {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(
                                    Triple("AGRICULTOR", AdGreen, "🌾"),
                                    Triple("AGRONOMO", AdBlue, "👨‍🔬"),
                                    Triple("ADMINISTRADOR", AdAmber, "⚙️")
                                ).forEach { (rol, color, emoji) ->
                                    val count = state.usuarios.count { it.rol.name == rol }
                                    Box(
                                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(color.copy(0.07f)).border(1.dp, color.copy(0.2f), RoundedCornerShape(14.dp)).padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(emoji, fontSize = 18.sp)
                                            Spacer(Modifier.height(3.dp))
                                            Text("$count", color = color, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, fontFamily = PlusJakartaSansFamily)
                                            Text(rol.take(4), color = color.copy(0.7f), fontSize = 9.sp, fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // Lista usuarios
                        items(state.usuarios) { user ->
                            val rolColor = when (user.rol.name) {
                                "AGRICULTOR" -> AdGreen
                                "AGRONOMO"   -> AdBlue
                                else         -> AdAmber
                            }
                            Box(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(AdCard).border(1.dp, AdBorder, RoundedCornerShape(14.dp)).padding(14.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(44.dp).background(
                                            Brush.linearGradient(listOf(rolColor.copy(0.25f), rolColor.copy(0.08f))), CircleShape
                                        ).border(1.dp, rolColor.copy(0.3f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(user.nombre.first().toString(), color = rolColor, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, fontFamily = PlusJakartaSansFamily)
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(user.nombre, color = Color.White, fontWeight = FontWeight.SemiBold, fontFamily = PlusJakartaSansFamily, fontSize = 14.sp)
                                        Text(user.correo, color = AdMuted, fontSize = 11.sp, fontFamily = PlusJakartaSansFamily)
                                    }
                                    Box(
                                        modifier = Modifier.background(rolColor.copy(0.1f), RoundedCornerShape(8.dp)).border(1.dp, rolColor.copy(0.25f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(user.rol.name.take(3), color = rolColor, fontSize = 10.sp, fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdFilterField(value: String, label: String, expanded: Boolean, modifier: Modifier) {
    OutlinedTextField(
        value         = value,
        onValueChange = {},
        readOnly      = true,
        label         = { Text(label, fontFamily = PlusJakartaSansFamily, fontSize = 12.sp) },
        trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
        modifier      = Modifier.fillMaxWidth().then(modifier),
        singleLine    = true,
        shape         = RoundedCornerShape(12.dp),
        textStyle     = androidx.compose.ui.text.TextStyle(fontFamily = PlusJakartaSansFamily, fontSize = 13.sp),
        colors        = OutlinedTextFieldDefaults.colors(
            focusedBorderColor     = AdBlue,
            unfocusedBorderColor   = AdBorder,
            focusedTextColor       = Color.White,
            unfocusedTextColor     = Color.White,
            focusedLabelColor      = AdBlue,
            unfocusedLabelColor    = AdMuted,
            focusedContainerColor  = AdSurface,
            unfocusedContainerColor = AdBg
        )
    )
}
