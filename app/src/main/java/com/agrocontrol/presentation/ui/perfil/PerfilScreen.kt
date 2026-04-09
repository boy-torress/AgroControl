package com.agrocontrol.presentation.ui.perfil

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agrocontrol.presentation.theme.*
import kotlin.math.cos
import kotlin.math.sin

private val PBase     = Color(0xFF030F07)
private val PSurface  = Color(0xFF071409)
private val PCard     = Color(0xFF0C1E10)
private val PBorder   = Color(0xFF1A3A1F)
private val GreenNeon = Color(0xFF4ADE80)
private val GreenMid  = Color(0xFF16A34A)
private val GreenDeep = Color(0xFF052E16)
private val GoldAccent= Color(0xFFF59E0B)
private val TextPrim  = Color(0xFFF0FFF4)
private val TextSec   = Color(0xFF86EFAC)
private val TextMuted = Color(0xFF4B7160)
private val RedVivid  = Color(0xFFF87171)

@Composable
fun PerfilScreen(
    onLogout: () -> Unit,
    viewModel: PerfilViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.success) {
        if (state.success) viewModel.clearSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PBase)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Hero Header ─────────────────────────────────────────────────────
        PerfilHeroHeader(nombre = state.nombre, rol = state.rol)

        // ── Content ─────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .offset(y = (-28).dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Identity card
            IdentityCard(correo = state.correo, rol = state.rol)

            // Edit panel
            AnimatedEditPanel(
                editando   = state.editando,
                nombreEdit = state.nombreEdit,
                isSaving   = state.isSaving,
                onNombreEdit = viewModel::onNombreEdit,
                onStart    = viewModel::startEdit,
                onCancel   = viewModel::cancelEdit,
                onGuardar  = viewModel::guardar
            )

            // Account section
            PerfilSectionLabel("CUENTA")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PerfilActionRow(Icons.Outlined.Notifications, "Notificaciones", GreenNeon)
                PerfilActionRow(Icons.Outlined.Security, "Privacidad y seguridad", BlueColor)
                PerfilActionRow(Icons.Outlined.HelpOutline, "Soporte", GoldAccent)
            }

            Spacer(Modifier.height(8.dp))

            // Logout
            LogoutButton(onClick = { viewModel.logout(); onLogout() })

            // Version
            Text(
                "AgroControl v2.4.1 · Build 2025",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = TextMuted.copy(0.5f),
                fontSize = 11.sp,
                fontFamily = PlusJakartaSansFamily
            )
            Spacer(Modifier.height(40.dp))
        }
    }
}

private val BlueColor = Color(0xFF60A5FA)

// ─── Hero Header ──────────────────────────────────────────────────────────────
@Composable
private fun PerfilHeroHeader(nombre: String, rol: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "perfil")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "pulse"
    )
    val particleAngle by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(14000, easing = LinearEasing)),
        label = "particle"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .drawBehind {
                // Background gradient
                drawRect(Brush.verticalGradient(listOf(Color(0xFF071A0B), Color(0xFF040E06))))

                // Large ambient glow
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0xFF14532D).copy(0.6f), Color.Transparent),
                        center = Offset(size.width / 2f, size.height * 0.3f),
                        radius = size.width * 0.55f
                    ),
                    radius = size.width * 0.55f,
                    center = Offset(size.width / 2f, size.height * 0.3f)
                )

                // Decorative arc
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(
                            GreenNeon.copy(0.0f),
                            GreenNeon.copy(0.2f),
                            GreenNeon.copy(0.0f)
                        ),
                        center = Offset(size.width / 2f, size.height * 0.4f)
                    ),
                    startAngle = -180f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(size.width / 2f - 100f, size.height * 0.4f - 100f),
                    size = Size(200f, 200f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
                )

                // Orbiting particles
                val cx = size.width / 2f
                val cy = size.height * 0.42f
                val orbitR = 110f
                for (i in 0 until 6) {
                    val angle = Math.toRadians((particleAngle + i * 60f).toDouble())
                    val x = cx + (orbitR * cos(angle)).toFloat()
                    val y = cy + (orbitR * sin(angle)).toFloat()
                    drawCircle(
                        color = if (i % 2 == 0) GreenNeon.copy(0.6f) else GoldAccent.copy(0.4f),
                        radius = if (i % 2 == 0) 3.5f else 2.5f,
                        center = Offset(x, y)
                    )
                }

                // Bottom separator line
                drawLine(
                    Brush.horizontalGradient(listOf(Color.Transparent, PBorder, Color.Transparent)),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1f
                )
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(top = 44.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar with pulsing ring
            Box(contentAlignment = Alignment.Center) {
                // Outer pulsing ring
                Box(
                    modifier = Modifier
                        .size((76 * pulse).dp)
                        .border(
                            width = 2.dp,
                            brush = Brush.sweepGradient(listOf(GoldAccent, GreenNeon, GoldAccent)),
                            shape = CircleShape
                        )
                )
                // Avatar
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .background(
                            Brush.linearGradient(listOf(Color(0xFF166534), Color(0xFF15803D))),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        nombre.firstOrNull()?.uppercaseChar()?.toString() ?: "A",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontFamily = PlusJakartaSansFamily
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Text(
                nombre,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrim,
                fontFamily = PlusJakartaSansFamily
            )

            Spacer(Modifier.height(6.dp))

            // Rol badge
            Box(
                Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(GoldAccent.copy(0.1f))
                    .border(1.dp, GoldAccent.copy(0.4f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 5.dp)
            ) {
                Text(
                    rol.lowercase().replaceFirstChar { it.uppercase() },
                    color = GoldAccent,
                    fontSize = 12.sp,
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ─── Identity Card ─────────────────────────────────────────────────────────────
@Composable
private fun IdentityCard(correo: String, rol: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(PCard)
            .drawBehind {
                drawLine(PBorder, Offset(0f, 0f), Offset(size.width, 0f), 1f)
            }
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            IdentityInfoRow(Icons.Outlined.Email, "Correo electrónico", correo, BlueColor)
            Box(Modifier.fillMaxWidth().height(1.dp).background(PBorder))
            IdentityInfoRow(Icons.Outlined.Badge, "Rol en el sistema", rol, GoldAccent)
        }
    }
}

@Composable
private fun IdentityInfoRow(icon: ImageVector, label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(color.copy(0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(label, color = TextMuted, fontSize = 10.sp, fontFamily = PlusJakartaSansFamily, letterSpacing = 0.5.sp)
            Spacer(Modifier.height(2.dp))
            Text(value, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrim, fontFamily = PlusJakartaSansFamily)
        }
    }
}

// ─── Edit Panel ───────────────────────────────────────────────────────────────
@Composable
private fun AnimatedEditPanel(
    editando: Boolean,
    nombreEdit: String,
    isSaving: Boolean,
    onNombreEdit: (String) -> Unit,
    onStart: () -> Unit,
    onCancel: () -> Unit,
    onGuardar: () -> Unit
) {
    Column(modifier = Modifier.animateContentSize()) {
        if (editando) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nombreEdit,
                    onValueChange = onNombreEdit,
                    label = { Text("Nombre completo", color = TextMuted, fontFamily = PlusJakartaSansFamily, fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(color = TextPrim, fontFamily = PlusJakartaSansFamily),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenNeon.copy(0.6f),
                        unfocusedBorderColor = PBorder,
                        focusedContainerColor = Color(0xFF0D2010),
                        unfocusedContainerColor = Color(0xFF0A1A0D),
                        cursorColor = GreenNeon
                    )
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(PCard)
                            .drawBehind { drawLine(PBorder, Offset(0f,0f), Offset(size.width,0f), 1f) }
                            .then(Modifier.clickable { onCancel() }),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Cancelar", color = TextMuted, fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Medium)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (!isSaving) Brush.horizontalGradient(listOf(GreenMid, Color(0xFF15803D)))
                                else Brush.horizontalGradient(listOf(Color(0xFF166534), Color(0xFF166534)))
                            )
                            .then(if (!isSaving) Modifier.clickable { onGuardar() } else Modifier),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Guardar", color = Color.White, fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(PCard)
                    .drawBehind {
                        drawLine(PBorder, Offset(0f,0f), Offset(size.width,0f), 1f)
                        drawLine(GreenNeon.copy(0.3f), Offset(0f,0f), Offset(0f,size.height), 2f)
                    }
                    .then(Modifier.clickable { onStart() }),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Edit, null, tint = GreenNeon.copy(0.8f), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Editar nombre", color = GreenNeon.copy(0.8f), fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}



// ─── Section label ─────────────────────────────────────────────────────────────
@Composable
private fun PerfilSectionLabel(text: String) {
    Text(
        text,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = TextMuted,
        fontFamily = PlusJakartaSansFamily,
        letterSpacing = 2.sp
    )
}

// ─── Action row ───────────────────────────────────────────────────────────────
@Composable
private fun PerfilActionRow(icon: ImageVector, label: String, tint: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PCard)
            .drawBehind {
                drawLine(PBorder, Offset(0f, 0f), Offset(size.width, 0f), 1f)
            }
            .clickable {}
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(tint.copy(0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(17.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text(
                label,
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = TextPrim,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Default.ChevronRight, null, tint = TextMuted.copy(0.5f), modifier = Modifier.size(18.dp))
        }
    }
}

// ─── Logout Button ─────────────────────────────────────────────────────────────
@Composable
private fun LogoutButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(RedVivid.copy(0.08f))
            .drawBehind {
                drawLine(RedVivid.copy(0.3f), Offset(0f, 0f), Offset(size.width, 0f), 1.5f)
            }
            .then(Modifier.clickable(onClick = onClick)),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Logout, null, tint = RedVivid, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text(
                "Cerrar sesión",
                fontWeight = FontWeight.Bold,
                fontFamily = PlusJakartaSansFamily,
                color = RedVivid,
                fontSize = 15.sp
            )
        }
    }
}
