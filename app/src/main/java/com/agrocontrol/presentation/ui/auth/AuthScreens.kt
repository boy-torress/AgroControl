package com.agrocontrol.presentation.ui.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agrocontrol.presentation.theme.*
import kotlin.math.sin

private val DarkBase     = Color(0xFF030F07)
private val DarkSurface  = Color(0xFF0A1F0F)
private val DarkCard     = Color(0xFF0F2714)
private val GreenNeon    = Color(0xFF4ADE80)
private val GreenMuted   = Color(0xFF16A34A)
private val GreenSubtle  = Color(0xFF14532D)
private val InputBorder  = Color(0xFF1F4A28)
private val InputFocus   = Color(0xFF4ADE80)
private val TextPrimary  = Color(0xFFF0FFF4)
private val TextSecondary= Color(0xFF86EFAC)
private val TextMuted    = Color(0xFF4B7160)

// ─── Login Screen ─────────────────────────────────────────────────────────────
@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "auth_bg")
    val bgShift by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing)),
        label = "bgShift"
    )

    LaunchedEffect(uiState.successRol) {
        uiState.successRol?.let { onLoginSuccess(it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBase)
    ) {
        // Animated background canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF14532D).copy(alpha = 0.4f), Color.Transparent),
                    center = Offset(size.width * 0.15f, size.height * 0.1f),
                    radius = size.width * 0.6f
                ),
                radius = size.width * 0.6f,
                center = Offset(size.width * 0.15f, size.height * 0.1f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF052E16).copy(alpha = 0.6f), Color.Transparent),
                    center = Offset(size.width * 0.9f, size.height * 0.85f),
                    radius = size.width * 0.5f
                ),
                radius = size.width * 0.5f,
                center = Offset(size.width * 0.9f, size.height * 0.85f)
            )
            // Subtle grid
            val gridStep = 60f
            val gridAlpha = 0.04f
            var x = 0f
            while (x < size.width) {
                drawLine(
                    color = Color.White.copy(alpha = gridAlpha),
                    start = Offset(x, 0f), end = Offset(x, size.height), strokeWidth = 0.5f
                )
                x += gridStep
            }
            var y = 0f
            while (y < size.height) {
                drawLine(
                    color = Color.White.copy(alpha = gridAlpha),
                    start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = 0.5f
                )
                y += gridStep
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(40.dp))

            // Logo area
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            Brush.radialGradient(listOf(Color(0xFF166534), Color(0xFF052E16))),
                            RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🌿", fontSize = 40.sp)
                }
                Spacer(Modifier.height(20.dp))
                Text(
                    "AgroControl",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    fontFamily = PlusJakartaSansFamily,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Gestión agrícola inteligente",
                    fontSize = 14.sp,
                    color = TextSecondary.copy(alpha = 0.7f),
                    fontFamily = PlusJakartaSansFamily
                )
            }

            Spacer(Modifier.height(44.dp))

            // Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(DarkCard)
                    .then(
                        Modifier.padding(1.dp) // border hack
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(DarkCard)
                        .padding(28.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        "Iniciar sesión",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontFamily = PlusJakartaSansFamily
                    )

                    DarkTextField(
                        value = correo,
                        onValueChange = { correo = it; viewModel.clearError() },
                        label = "Correo electrónico",
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = GreenNeon.copy(0.6f), modifier = Modifier.size(18.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    DarkTextField(
                        value = password,
                        onValueChange = { password = it; viewModel.clearError() },
                        label = "Contraseña",
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = GreenNeon.copy(0.6f), modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
                    )

                    uiState.error?.let {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF7F1D1D).copy(alpha = 0.4f))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, null, tint = Color(0xFFFCA5A5), modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(it, color = Color(0xFFFCA5A5), fontSize = 13.sp, fontFamily = PlusJakartaSansFamily)
                            }
                        }
                    }

                    // Primary button with gradient
                    Button(
                        onClick = { viewModel.login(correo, password) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        enabled = !uiState.isLoading,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    if (!uiState.isLoading)
                                        Brush.horizontalGradient(listOf(Color(0xFF16A34A), Color(0xFF15803D)))
                                    else Brush.horizontalGradient(listOf(Color(0xFF166534), Color(0xFF166534))),
                                    RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text(
                                    "Ingresar",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White,
                                    fontFamily = PlusJakartaSansFamily,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "¿Sin cuenta?",
                    color = TextMuted,
                    fontSize = 14.sp,
                    fontFamily = PlusJakartaSansFamily
                )
                TextButton(onClick = onNavigateToRegister) {
                    Text(
                        "Regístrate aquí",
                        color = GreenNeon,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        fontFamily = PlusJakartaSansFamily
                    )
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

// ─── Register Screen ──────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.successRol) {
        if (uiState.successRol != null) onRegisterSuccess()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Crear cuenta",
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBase)
            )
        },
        containerColor = DarkBase
    ) { padding ->

        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF14532D).copy(alpha = 0.3f), Color.Transparent),
                        center = Offset(size.width * 0.8f, size.height * 0.1f),
                        radius = size.width * 0.55f
                    ),
                    radius = size.width * 0.55f,
                    center = Offset(size.width * 0.8f, size.height * 0.1f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Únete a AgroControl",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        fontFamily = PlusJakartaSansFamily
                    )
                    Text(
                        "Completa tus datos para comenzar",
                        color = TextMuted,
                        fontSize = 14.sp,
                        fontFamily = PlusJakartaSansFamily
                    )
                }

                Spacer(Modifier.height(8.dp))

                DarkTextField(
                    value = nombre,
                    onValueChange = { nombre = it; viewModel.clearError() },
                    label = "Nombre completo",
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = GreenNeon.copy(0.6f), modifier = Modifier.size(18.dp)) }
                )

                DarkTextField(
                    value = correo,
                    onValueChange = { correo = it; viewModel.clearError() },
                    label = "Correo electrónico",
                    leadingIcon = { Icon(Icons.Default.Email, null, tint = GreenNeon.copy(0.6f), modifier = Modifier.size(18.dp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                DarkTextField(
                    value = password,
                    onValueChange = { password = it; viewModel.clearError() },
                    label = "Contraseña",
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = GreenNeon.copy(0.6f), modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                null, tint = TextMuted, modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    supportText = "Mín. 8 caracteres, una mayúscula y un número"
                )

                DarkTextField(
                    value = confirmar,
                    onValueChange = { confirmar = it; viewModel.clearError() },
                    label = "Confirmar contraseña",
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = GreenNeon.copy(0.6f), modifier = Modifier.size(18.dp)) },
                    visualTransformation = PasswordVisualTransformation()
                )

                uiState.error?.let {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF7F1D1D).copy(alpha = 0.4f))
                            .padding(14.dp)
                    ) {
                        Text(it, color = Color(0xFFFCA5A5), fontSize = 13.sp, fontFamily = PlusJakartaSansFamily)
                    }
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.register(nombre, correo, password, confirmar) },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    enabled = !uiState.isLoading,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                if (!uiState.isLoading)
                                    Brush.horizontalGradient(listOf(Color(0xFF16A34A), Color(0xFF15803D)))
                                else Brush.horizontalGradient(listOf(Color(0xFF166534), Color(0xFF166534))),
                                RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text(
                                "Crear cuenta",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White,
                                fontFamily = PlusJakartaSansFamily
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ─── Shared dark-theme text field ─────────────────────────────────────────────
@Composable
private fun DarkTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    supportText: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                label,
                fontFamily = PlusJakartaSansFamily,
                fontSize = 13.sp,
                color = TextMuted
            )
        },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        supportingText = supportText?.let { { Text(it, color = TextMuted, fontSize = 11.sp, fontFamily = PlusJakartaSansFamily) } },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary.copy(alpha = 0.9f),
            focusedBorderColor = InputFocus,
            unfocusedBorderColor = InputBorder,
            focusedContainerColor = Color(0xFF0D2010),
            unfocusedContainerColor = Color(0xFF0A1A0D),
            cursorColor = GreenNeon,
            focusedLabelColor = GreenNeon.copy(0.8f)
        )
    )
}

private val KeyboardType = androidx.compose.ui.text.input.KeyboardType
