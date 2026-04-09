package com.agrocontrol.presentation.ui.agronomo

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
import com.agrocontrol.domain.model.User
import com.agrocontrol.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

private val AgBg      = Color(0xFF07080F)
private val AgSurface = Color(0xFF0D0F1A)
private val AgCard    = Color(0xFF101320)
private val AgBorder  = Color(0xFF1A1E33)
private val AgPurple  = Color(0xFFA78BFA)
private val AgGreen   = Color(0xFF4ADE80)
private val AgRed     = Color(0xFFF87171)
private val AgMuted   = Color(0xFF6B7280)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgronomoScreen(
    onLogout  : () -> Unit,
    viewModel : AgronomoViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val sdf    = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    if (state.selectedAgricultor != null) {
        val ag     = state.selectedAgricultor!!
        val cultivo = state.cultivosPorAgricultor[ag.id]

        Scaffold(
            containerColor = AgBg,
            topBar = {
                TopAppBar(
                    title = {
                        Text(ag.nombre, color = Color.White, fontWeight = FontWeight.ExtraBold, fontFamily = PlusJakartaSansFamily, fontSize = 18.sp)
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.selectAgricultor(null) }) {
                            Box(
                                modifier = Modifier.size(36.dp).background(Color.White.copy(0.06f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) { Icon(Icons.Default.ArrowBack, null, tint = Color.White, modifier = Modifier.size(18.dp)) }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = AgBg)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(padding).padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ── Info agricultor ──────────────────────────────────────────
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                        .background(AgCard).border(1.dp, AgBorder, RoundedCornerShape(18.dp)).padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(52.dp)
                                .background(Brush.linearGradient(listOf(AgPurple.copy(0.3f), AgPurple.copy(0.1f))), CircleShape)
                                .border(1.dp, AgPurple.copy(0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(ag.nombre.first().toString(), color = AgPurple, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, fontFamily = PlusJakartaSansFamily)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(ag.nombre, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, fontFamily = PlusJakartaSansFamily)
                            Text(ag.correo, color = AgMuted, fontSize = 12.sp, fontFamily = PlusJakartaSansFamily)
                            Spacer(Modifier.height(5.dp))
                            Box(
                                modifier = Modifier.background(AgPurple.copy(0.12f), RoundedCornerShape(6.dp)).border(1.dp, AgPurple.copy(0.25f), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Visibility, null, tint = AgPurple, modifier = Modifier.size(10.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Solo lectura", color = AgPurple, fontSize = 10.sp, fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp)
                                }
                            }
                        }
                    }
                }

                // ── Cultivo activo ───────────────────────────────────────────
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                        .background(AgCard).border(1.dp, AgBorder, RoundedCornerShape(18.dp)).padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Cultivo activo", color = AgMuted, fontSize = 11.sp, fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        if (cultivo != null) {
                            Text(cultivo.tipoCultivo, color = AgGreen, fontWeight = FontWeight.Black, fontSize = 24.sp, fontFamily = PlusJakartaSansFamily)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AgInfoChip(Icons.Default.AccountTree, cultivo.etapaActual.name)
                                AgInfoChip(Icons.Default.LocationOn, cultivo.region)
                                AgInfoChip(Icons.Default.SquareFoot, "${cultivo.hectareas} ha")
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, null, tint = AgMuted, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Sin cultivo activo", color = AgMuted, fontFamily = PlusJakartaSansFamily, fontSize = 14.sp)
                            }
                        }
                    }
                }

                // ── Historial de etapas ──────────────────────────────────────
                if (state.historialSeleccionado.isNotEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                            .background(AgCard).border(1.dp, AgBorder, RoundedCornerShape(18.dp)).padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Historial de etapas", color = AgMuted, fontSize = 11.sp, fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            state.historialSeleccionado.forEachIndexed { idx, h ->
                                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    // Timeline dot
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(Modifier.size(8.dp).background(if (idx == 0) AgGreen else AgPurple.copy(0.5f), CircleShape))
                                        if (idx < state.historialSeleccionado.lastIndex) {
                                            Box(Modifier.width(1.dp).height(20.dp).background(AgBorder))
                                        }
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Text(h.etapa.name, color = Color.White, fontWeight = FontWeight.Medium, fontFamily = PlusJakartaSansFamily, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                    Text(sdf.format(Date(h.fechaCambio)), color = AgMuted, fontSize = 11.sp, fontFamily = PlusJakartaSansFamily)
                                }
                            }
                        }
                    }
                }
            }
        }
        return
    }

    Scaffold(
        containerColor = AgBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Panel Agrónomo", color = Color.White, fontWeight = FontWeight.Black, fontSize = 19.sp, fontFamily = PlusJakartaSansFamily)
                        Text("Vista de solo lectura", color = AgMuted, fontSize = 11.sp, fontFamily = PlusJakartaSansFamily)
                    }
                },
                actions = {
                    IconButton(
                        onClick  = { viewModel.logout(); onLogout() },
                        modifier = Modifier.size(36.dp).background(AgRed.copy(0.1f), RoundedCornerShape(10.dp)).border(1.dp, AgRed.copy(0.2f), RoundedCornerShape(10.dp))
                    ) {
                        Icon(Icons.Default.Logout, null, tint = AgRed, modifier = Modifier.size(17.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AgBg)
            )
        }
    ) { padding ->
        if (state.agricultores.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier.size(80.dp).background(AgPurple.copy(0.08f), CircleShape).border(1.dp, AgPurple.copy(0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) { Text("👨‍🌾", fontSize = 34.sp) }
                    Text("Sin agricultores asignados", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp, fontFamily = PlusJakartaSansFamily)
                    Text("Contacta al administrador", color = AgMuted, fontSize = 13.sp, fontFamily = PlusJakartaSansFamily)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Mis agricultores", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp, fontFamily = PlusJakartaSansFamily)
                        Spacer(Modifier.width(8.dp))
                        Box(Modifier.background(AgPurple.copy(0.12f), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                            Text("${state.agricultores.size}", color = AgPurple, fontSize = 12.sp, fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
                items(state.agricultores) { ag ->
                    AgricultorCard(
                        agricultor   = ag,
                        cultivo      = state.cultivosPorAgricultor[ag.id],
                        alertasCount = state.alertasPorAgricultor[ag.id] ?: 0,
                        onClick      = { viewModel.selectAgricultor(ag) }
                    )
                }
            }
        }
    }
}

@Composable
fun AgricultorCard(
    agricultor   : User,
    cultivo      : com.agrocontrol.domain.model.Cultivo?,
    alertasCount : Int,
    onClick      : () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(AgCard).border(1.dp, AgBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick).padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp)
                    .background(Brush.linearGradient(listOf(AgPurple.copy(0.25f), AgPurple.copy(0.08f))), CircleShape)
                    .border(1.dp, AgPurple.copy(0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(agricultor.nombre.first().toString(), color = AgPurple, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, fontFamily = PlusJakartaSansFamily)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(agricultor.nombre, color = Color.White, fontWeight = FontWeight.SemiBold, fontFamily = PlusJakartaSansFamily, fontSize = 15.sp)
                Text(
                    cultivo?.let { "${it.tipoCultivo} · ${it.etapaActual.name}" } ?: "Sin cultivo activo",
                    color = if (cultivo != null) AgGreen.copy(0.8f) else AgMuted,
                    fontSize = 12.sp,
                    fontFamily = PlusJakartaSansFamily
                )
            }
            if (alertasCount > 0) {
                Box(
                    modifier = Modifier.size(24.dp).background(AgRed, CircleShape),
                    contentAlignment = Alignment.Center
                ) { Text("$alertasCount", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, fontFamily = PlusJakartaSansFamily) }
                Spacer(Modifier.width(8.dp))
            }
            Icon(Icons.Default.ChevronRight, null, tint = AgMuted.copy(0.4f), modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun AgInfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Box(
        modifier = Modifier.background(AgGreen.copy(0.08f), RoundedCornerShape(8.dp)).border(1.dp, AgGreen.copy(0.18f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = AgGreen, modifier = Modifier.size(10.dp))
            Spacer(Modifier.width(4.dp))
            Text(text, color = AgGreen.copy(0.9f), fontSize = 10.sp, fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Medium)
        }
    }
}
