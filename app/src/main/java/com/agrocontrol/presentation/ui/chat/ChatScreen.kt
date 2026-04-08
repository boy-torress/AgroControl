package com.agrocontrol.presentation.ui.chat

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agrocontrol.presentation.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Auto-scroll al último mensaje
    LaunchedEffect(state.mensajes.size) {
        if (state.mensajes.isNotEmpty()) {
            listState.animateScrollToItem(state.mensajes.lastIndex)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp), // Cancela insets duplicados 
        topBar = {
            TopAppBar(
                modifier = Modifier.height(48.dp), // Minimum touch target height
                windowInsets = WindowInsets(0.dp), // Cancela el relleno automático del Notch/Estado
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(listOf(VerdeAccent, Verde60))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🤖", fontSize = 18.sp)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(verticalArrangement = Arrangement.Center) {
                            Text(
                                "AgroBot",
                                fontWeight = FontWeight.Bold,
                                fontFamily = PlusJakartaSansFamily,
                                fontSize = 16.sp
                            )
                            Text(
                                "Asistente agrícola IA",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.5f),
                                fontFamily = PlusJakartaSansFamily
                            )
                        }
                    }
                },
                actions = {
                    if (state.mensajes.isNotEmpty()) {
                        IconButton(onClick = { viewModel.limpiarChat() }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Limpiar chat")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            ChatInputBar(
                value     = state.inputText,
                isLoading = state.isLoading,
                onValueChange = viewModel::onInputChange,
                onSend = {
                    viewModel.enviarMensaje()
                    scope.launch { listState.animateScrollToItem(state.mensajes.size) }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Lista de mensajes
            if (state.mensajes.isEmpty()) {
                EmptyChatState(
                    modifier = Modifier.weight(1f),
                    onSuggestionClick = { viewModel.onInputChange(it); viewModel.enviarMensaje() }
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.mensajes, key = { it.id }) { msg ->
                        ChatBubble(msg = msg)
                    }
                }
            }

            // Error banner
            AnimatedVisibility(visible = state.error != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            state.error ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = viewModel::clearError, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(msg: ChatMensajeUi) {
    val isUser = msg.rol == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            Box(
                Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(VerdeAccent, Verde60))),
                contentAlignment = Alignment.Center
            ) { Text("🤖", fontSize = 16.sp) }
            Spacer(Modifier.width(8.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd   = if (isUser) 4.dp  else 18.dp
            ),
            color = if (isUser) Verde60 else MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 1.dp,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            if (msg.sCargando) {
                TypingIndicator(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
            } else {
                Text(
                    text = msg.contenido,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    color = if (isUser) Blanco else MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontFamily = PlusJakartaSansFamily,
                    lineHeight = 20.sp
                )
            }
        }

        if (isUser) {
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) { Text("👤", fontSize = 16.sp) }
        }
    }
}

@Composable
private fun TypingIndicator(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    val dots = (0..2).map { i ->
        animateFloatAsState(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 900
                    0.3f at 0; 1f at 300 + i * 100; 0.3f at 700
                }
            ),
            label = "dot$i"
        ).value
    }
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically) {
        dots.forEach { alpha ->
            Box(
                Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha))
            )
        }
    }
}

@Composable
private fun EmptyChatState(modifier: Modifier, onSuggestionClick: (String) -> Unit) {
    val suggestions = listOf(
        "¿Cómo debo regar mi cultivo esta semana?",
        "¿Cuándo es buen momento para cosechar?",
        "¿Qué fertilizante me recomiendas?"
    )

    Column(
        modifier = modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🤖", fontSize = 64.sp)
        Spacer(Modifier.height(16.dp))
        Text("AgroBot listo para ayudarte", fontWeight = FontWeight.Bold,
            fontFamily = PlusJakartaSansFamily, fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            "Pregúntame sobre clima, plagas, riego, fertilización o cualquier tema agrícola.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(0.6f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            fontFamily = PlusJakartaSansFamily
        )
        Spacer(Modifier.height(28.dp))
        suggestions.forEach { s ->
            OutlinedButton(
                onClick = { onSuggestionClick(s) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Verde60.copy(0.4f))
            ) {
                Text(s, fontFamily = PlusJakartaSansFamily, fontSize = 13.sp, color = Verde60)
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    value: String,
    isLoading: Boolean,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Surface(
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .imePadding(),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text("Escribe tu pregunta...", fontFamily = PlusJakartaSansFamily,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.4f))
                },
                shape = RoundedCornerShape(24.dp),
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { if (!isLoading) onSend() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Verde60,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            Spacer(Modifier.width(8.dp))
            FilledIconButton(
                onClick = { if (!isLoading) onSend() },
                modifier = Modifier.size(48.dp),
                enabled = value.isNotBlank() && !isLoading,
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Verde60)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Blanco, strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Send, contentDescription = "Enviar", tint = Blanco)
                }
            }
        }
    }
}
