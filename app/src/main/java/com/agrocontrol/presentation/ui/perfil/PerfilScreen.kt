package com.agrocontrol.presentation.ui.perfil

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agrocontrol.presentation.theme.*

// ─── Colores internos de diseño ──────────────────────────────────────────────
private val GreenDeep   = Color(0xFF14532D)
private val GreenMid    = Color(0xFF166534)
private val GreenBright = Color(0xFF22C55E)
private val GreenSoft   = Color(0xFFDCFCE7)
private val GoldAccent  = Color(0xFFF59E0B)
private val RedSoft     = Color(0xFFFEF2F2)
private val RedBorder   = Color(0xFFFCA5A5)
private val RedText     = Color(0xFFDC2626)
private val SlateMuted  = Color(0xFF64748B)
private val SlateSurface= Color(0xFFF8FAFC)
private val White       = Color.White

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
            .verticalScroll(rememberScrollState())
            .background(SlateSurface)
    ) {
        // ── Header orgánico con ondas ─────────────────────────────────────
        PerfilHeader(
            nombre        = state.nombre,
            nombreEdit    = state.nombreEdit,
            rol           = state.rol,
            editando      = state.editando,
            onNombreEdit  = viewModel::onNombreEdit
        )

        // ── Contenido principal ───────────────────────────────────────────
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .offset(y = (-24).dp)  // Overlap sobre el header
        ) {
            // Tarjeta de identidad flotante
            IdentityCard(correo = state.correo, rol = state.rol)

            Spacer(Modifier.height(20.dp))

            // Panel de edición
            AnimatedEditPanel(
                editando  = state.editando,
                isSaving  = state.isSaving,
                onStart   = viewModel::startEdit,
                onCancel  = viewModel::cancelEdit,
                onGuardar = viewModel::guardar
            )

            Spacer(Modifier.height(24.dp))

            // Sección configuración
            SectionLabel("Cuenta")
            Spacer(Modifier.height(8.dp))
            ActionRow(
                icon  = Icons.Outlined.Notifications,
                label = "Notificaciones",
                tint  = GreenBright
            )
            ActionRow(
                icon  = Icons.Outlined.Security,
                label = "Privacidad y seguridad",
                tint  = GreenBright
            )
            ActionRow(
                icon  = Icons.Outlined.HelpOutline,
                label = "Soporte",
                tint  = GreenBright
            )

            Spacer(Modifier.height(24.dp))

            // Cerrar sesión
            LogoutButton(onClick = { viewModel.logout(); onLogout() })

            // Versión
            Spacer(Modifier.height(20.dp))
            Text(
                "AgroControl v2.4.1 · Build 2025",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = SlateMuted.copy(alpha = 0.5f),
                fontSize = 11.sp,
                fontFamily = PlusJakartaSansFamily
            )
            Spacer(Modifier.height(40.dp))
        }
    }
}

// ─── Header con ondas decorativas ────────────────────────────────────────────
@Composable
private fun PerfilHeader(
    nombre: String,
    nombreEdit: String,
    rol: String,
    editando: Boolean,
    onNombreEdit: (String) -> Unit
) {
    // Pulso del anillo del avatar
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 1f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(1800, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "avatarPulse"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .drawBehind {
                // Gradiente base
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(GreenDeep, GreenMid),
                        start   = Offset.Zero,
                        end     = Offset(size.width, size.height)
                    )
                )
                // Onda decorativa 1
                val wavePath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, size.height * 0.72f)
                    cubicTo(
                        size.width * 0.25f, size.height * 0.60f,
                        size.width * 0.55f, size.height * 0.88f,
                        size.width,         size.height * 0.70f
                    )
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(wavePath, Color(0x22FFFFFF))

                // Onda 2 (más abajo)
                val wavePath2 = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, size.height * 0.82f)
                    cubicTo(
                        size.width * 0.30f, size.height * 0.74f,
                        size.width * 0.65f, size.height * 0.96f,
                        size.width,         size.height * 0.80f
                    )
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(wavePath2, Color(0x15FFFFFF))

                // Círculo decorativo esquina superior derecha
                drawCircle(
                    color  = Color(0x18FFFFFF),
                    radius = 120f,
                    center = Offset(size.width - 40f, -30f)
                )
                drawCircle(
                    color  = Color(0x0FFFFFFF),
                    radius = 70f,
                    center = Offset(size.width - 10f, 110f)
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Anillo exterior con pulso
            Box(
                modifier = Modifier
                    .size((90 * pulse).dp)
                    .border(
                        width = 2.dp,
                        brush = Brush.sweepGradient(listOf(GoldAccent, GreenBright, GoldAccent)),
                        shape = CircleShape
                    )
                    .padding(4.dp)
            ) {
                // Círculo avatar
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = nombre.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        fontSize   = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = White,
                        fontFamily = PlusJakartaSansFamily
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            if (editando) {
                OutlinedTextField(
                    value          = nombreEdit,
                    onValueChange  = onNombreEdit,
                    singleLine     = true,
                    colors         = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = GoldAccent,
                        unfocusedBorderColor = White.copy(0.5f),
                        focusedTextColor     = White,
                        unfocusedTextColor   = White,
                        cursorColor          = GoldAccent
                    ),
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 20.sp,
                        color      = White
                    ),
                    modifier = Modifier.width(230.dp)
                )
            } else {
                Text(
                    nombre,
                    fontSize   = 23.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = White,
                    fontFamily = PlusJakartaSansFamily
                )
            }

            Spacer(Modifier.height(6.dp))

            // Badge de rol con acento dorado
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent,
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = GoldAccent.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(20.dp)
                    )
            ) {
                Text(
                    rol.lowercase().replaceFirstChar { it.uppercase() },
                    modifier   = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                    color      = GoldAccent,
                    fontSize   = 12.sp,
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ─── Tarjeta de identidad flotante ───────────────────────────────────────────
@Composable
private fun IdentityCard(correo: String, rol: String) {
    Card(
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            InfoRow(Icons.Outlined.Email,  "Correo electrónico", correo)
            HorizontalDivider(color = GreenSoft)
            InfoRow(Icons.Outlined.Badge,  "Rol en el sistema",  rol)
        }
    }
}

// ─── Fila de info dentro de tarjeta ──────────────────────────────────────────
@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(listOf(GreenSoft, GreenBright.copy(alpha = 0.15f)))
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = GreenMid, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(
                label,
                color      = SlateMuted,
                fontSize   = 11.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = PlusJakartaSansFamily
            )
            Spacer(Modifier.height(2.dp))
            Text(
                value,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 15.sp,
                fontFamily = PlusJakartaSansFamily,
                color      = Color(0xFF0F172A)
            )
        }
    }
}

// ─── Panel animado edición/guardar ───────────────────────────────────────────
@Composable
private fun AnimatedEditPanel(
    editando: Boolean,
    isSaving: Boolean,
    onStart: () -> Unit,
    onCancel: () -> Unit,
    onGuardar: () -> Unit
) {
    Column(modifier = Modifier.animateContentSize()) {
        if (editando) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick  = onCancel,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape    = RoundedCornerShape(14.dp),
                    border   = androidx.compose.foundation.BorderStroke(1.dp, SlateMuted.copy(0.4f))
                ) {
                    Text("Cancelar", fontFamily = PlusJakartaSansFamily, color = SlateMuted)
                }
                Button(
                    onClick  = onGuardar,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape    = RoundedCornerShape(14.dp),
                    enabled  = !isSaving,
                    colors   = ButtonDefaults.buttonColors(containerColor = GreenMid)
                ) {
                    if (isSaving)
                        CircularProgressIndicator(Modifier.size(18.dp), color = White, strokeWidth = 2.dp)
                    else {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Guardar", fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        } else {
            OutlinedButton(
                onClick  = onStart,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape    = RoundedCornerShape(14.dp),
                border   = androidx.compose.foundation.BorderStroke(1.5.dp, GreenBright.copy(0.5f))
            ) {
                Icon(Icons.Outlined.Edit, null, tint = GreenMid, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(8.dp))
                Text("Editar nombre", color = GreenMid, fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ─── Etiqueta de sección ─────────────────────────────────────────────────────
@Composable
private fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        fontSize   = 11.sp,
        fontWeight = FontWeight.Bold,
        color      = SlateMuted,
        fontFamily = PlusJakartaSansFamily,
        letterSpacing = 1.5.sp
    )
}

// ─── Fila de acción genérica ──────────────────────────────────────────────────
@Composable
private fun ActionRow(icon: ImageVector, label: String, tint: Color) {
    Card(
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier  = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier            = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment   = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(tint.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text(label, fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            }
            Icon(Icons.Default.ChevronRight, null, tint = SlateMuted.copy(0.5f), modifier = Modifier.size(18.dp))
        }
    }
}

// ─── Botón logout dramático ───────────────────────────────────────────────────
@Composable
private fun LogoutButton(onClick: () -> Unit) {
    Button(
        onClick  = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .shadow(
                elevation    = 4.dp,
                shape        = RoundedCornerShape(16.dp),
                ambientColor = RedBorder,
                spotColor    = RedBorder
            ),
        shape  = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(listOf(Color(0xFFDC2626), Color(0xFFEF4444))),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Logout, null, tint = White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    "Cerrar sesión",
                    fontWeight = FontWeight.Bold,
                    fontFamily = PlusJakartaSansFamily,
                    color      = White,
                    fontSize   = 15.sp
                )
            }
        }
    }
}
