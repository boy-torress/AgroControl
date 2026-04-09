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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agrocontrol.presentation.theme.*
import kotlinx.coroutines.launch

private val ChatBase     = Color(0xFF030F07)
private val ChatSurface  = Color(0xFF071409)
private val ChatCard     = Color(0xFF0C1E10)
private val ChatBorder   = Color(0xFF1A3A1F)
private val GreenNeon    = Color(0xFF4ADE80)
private val GreenDeep    = Color(0xFF166534)
private val TextPrimary  = Color(0xFFF0FFF4)
private val TextSecond   = Color(0xFF86EFAC)
private val TextMuted    = Color(0xFF4B7160)
private val UserBubble   = Color(0xFF15803D)
private val BotBubble    = Color(0xFF0D2210)
private val InputBg      = Color(0xFF0A1A0D)
private val InputBorder  = Color(0xFF1F4A28)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.mensajes.size) {
        if (state.mensajes.isNotEmpty()) {
            listState.animateScrollToItem(state.mensajes.lastIndex)
        }
    }

    Scaffold(
        containerColor = ChatBase,
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        drawRect(ChatSurface)
                        drawLine(ChatBorder, Offset(0f, size.height), Offset(size.width, size.height), 1f)
                    }
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Bot avatar
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                Brush.linearGradient(listOf(Color(0xFF166534), Color(0xFF15803D))),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🤖", fontSize = 20.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "AgroBot",
                            fontWeight = FontWeight.Bold,
                            fontFamily = PlusJakartaSansFamily,
                            color = TextPrimary,
                            fontSize = 16.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(6.dp).background(GreenNeon, CircleShape))
                            Spacer(Modifier.width(5.dp))
                            Text(
                                "Asistente agrícola IA",
                                fontSize = 11.sp,
                                color = TextMuted,
                                fontFamily = PlusJakartaSansFamily
                            )
                        }
                    }
                    if (state.mensajes.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.limpiarChat() },
                            modifier = Modifier.size(36.dp).background(ChatCard, CircleShape)
                        ) {
                            Icon(Icons.Default.DeleteSweep, null, tint = TextMuted, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        },
        bottomBar = {
            ChatInputBar(
                value = state.inputText,
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
            if (state.mensajes.isEmpty()) {
                EmptyChatState(
                    modifier = Modifier.weight(1f),
                    onSuggestionClick = { viewModel.onInputChange(it); viewModel.enviarMensaje() }
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.mensajes, key = { it.id }) { msg ->
                        ChatBubble(msg = msg)
                    }
                }
            }

            // Error banner
            AnimatedVisibility(visible = state.error != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF7F1D1D).copy(0.5f))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null, tint = Color(0xFFFCA5A5), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            state.error ?: "",
                            fontSize = 12.sp,
                            color = Color(0xFFFCA5A5),
                            fontFamily = PlusJakartaSansFamily,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = viewModel::clearError, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp), tint = Color(0xFFFCA5A5))
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
                Modifier.size(32.dp)
                    .background(Brush.linearGradient(listOf(Color(0xFF166534), Color(0xFF15803D))), CircleShape),
                contentAlignment = Alignment.Center
            ) { Text("🤖", fontSize = 15.sp) }
            Spacer(Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 20.dp, topEnd = 20.dp,
                        bottomStart = if (isUser) 20.dp else 5.dp,
                        bottomEnd = if (isUser) 5.dp else 20.dp
                    )
                )
                .background(
                    if (isUser)
                        Brush.linearGradient(listOf(UserBubble, Color(0xFF16A34A)))
                    else
                        Brush.linearGradient(listOf(BotBubble, ChatCard))
                )
                .widthIn(max = 280.dp)
                .drawBehind {
                    if (!isUser) drawLine(ChatBorder, Offset(0f, 0f), Offset(size.width, 0f), 1f)
                }
        ) {
            if (msg.sCargando) {
                TypingIndicator(modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp))
            } else {
                Text(
                    text = msg.contenido,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                    color = if (isUser) Color.White else TextSecond.copy(0.9f),
                    fontSize = 14.sp,
                    fontFamily = PlusJakartaSansFamily,
                    lineHeight = 21.sp
                )
            }
        }

        if (isUser) {
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier.size(32.dp).background(ChatCard, CircleShape),
                contentAlignment = Alignment.Center
            ) { Text("👤", fontSize = 15.sp) }
        }
    }
}

@Composable
private fun TypingIndicator(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(3) { i ->
            val dotAlpha by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    keyframes { durationMillis = 1000; 0.2f at 0; 1f at 300 + i * 120; 0.2f at 750 }
                ),
                label = "dot$i"
            )
            val dotSize by infiniteTransition.animateFloat(
                initialValue = 5f,
                targetValue = 7f,
                animationSpec = infiniteRepeatable(
                    keyframes { durationMillis = 1000; 5f at 0; 7f at 300 + i * 120; 5f at 750 }
                ),
                label = "dotSize$i"
            )
            Box(
                Modifier.size(dotSize.dp).background(GreenNeon.copy(alpha = dotAlpha), CircleShape)
            )
        }
    }
}

@Composable
private fun EmptyChatState(modifier: Modifier, onSuggestionClick: (String) -> Unit) {
    val suggestions = listOf(
        "🌱 ¿Cómo debo regar mi cultivo esta semana?",
        "🌾 ¿Cuándo es buen momento para cosechar?",
        "💊 ¿Qué fertilizante me recomiendas para trigo?"
    )

    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Large bot avatar
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(
                    Brush.radialGradient(listOf(Color(0xFF166534), Color(0xFF052E16))),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("🤖", fontSize = 48.sp)
        }

        Spacer(Modifier.height(20.dp))

        Text(
            "AgroBot listo para ayudarte",
            fontWeight = FontWeight.ExtraBold,
            fontFamily = PlusJakartaSansFamily,
            fontSize = 20.sp,
            color = TextPrimary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Pregúntame sobre clima, plagas,\nriego, fertilización o cualquier tema agrícola",
            color = TextMuted,
            fontFamily = PlusJakartaSansFamily,
            fontSize = 14.sp,
            lineHeight = 21.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            suggestions.forEach { s ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSuggestionClick(s.substringAfter(" ")) }
                        .background(ChatCard)
                        .drawBehind {
                            drawLine(ChatBorder, Offset(0f, 0f), Offset(size.width, 0f), 1f)
                            drawLine(GreenNeon.copy(0.3f), Offset(0f, 0f), Offset(0f, size.height), 2f)
                        }
                        .padding(14.dp)
                ) {
                    Text(
                        s,
                        color = TextSecond.copy(0.85f),
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 13.sp
                    )
                }
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(ChatSurface)
            .drawBehind {
                drawLine(ChatBorder, Offset(0f, 0f), Offset(size.width, 0f), 1f)
            }
            .imePadding()
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        "Escribe tu pregunta...",
                        fontFamily = PlusJakartaSansFamily,
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                },
                shape = RoundedCornerShape(20.dp),
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { if (!isLoading) onSend() }),
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = TextPrimary,
                    fontFamily = PlusJakartaSansFamily,
                    fontSize = 14.sp
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenNeon.copy(0.6f),
                    unfocusedBorderColor = InputBorder,
                    focusedContainerColor = InputBg,
                    unfocusedContainerColor = InputBg,
                    cursorColor = GreenNeon
                )
            )

            Spacer(Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (value.isNotBlank() && !isLoading)
                            Brush.linearGradient(listOf(Color(0xFF16A34A), Color(0xFF15803D)))
                        else Brush.linearGradient(listOf(ChatCard, ChatCard))
                    )
                    .clickable(enabled = value.isNotBlank() && !isLoading) { onSend() },
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(Modifier.size(20.dp), color = GreenNeon, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Send, null, tint = if (value.isNotBlank()) Color.White else TextMuted, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
