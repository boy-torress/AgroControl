package com.agrocontrol.presentation.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agrocontrol.data.local.dao.ChatDao
import com.agrocontrol.data.local.entities.ChatMensajeEntity
import com.agrocontrol.data.repository.*
import com.agrocontrol.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatMensajeUi(
    val id: Long = 0,
    val rol: String,       // "user" | "assistant"
    val contenido: String,
    val timestamp: Long = System.currentTimeMillis(),
    val sCargando: Boolean = false
)

data class ChatUiState(
    val mensajes: List<ChatMensajeUi> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val cultivoContexto: String = "",
    val climaContexto: String = ""
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val session: SessionManager,
    private val chatDao: ChatDao,
    private val iaService: IAService,
    private val cultivoRepo: CultivoRepository,
    private val climaService: ClimaService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var userId = 0L

    init {
        viewModelScope.launch {
            session.userId.filterNotNull().collect { uid ->
                userId = uid
                // Cargar historial guardado
                chatDao.getMensajesByUsuario(uid).collect { entities ->
                    val msgs = entities.map {
                        ChatMensajeUi(it.id, it.rol, it.contenido, it.timestamp)
                    }
                    _uiState.update { s -> s.copy(mensajes = msgs) }
                }
            }
        }
        viewModelScope.launch {
            session.userId.filterNotNull().collect { uid ->
                // Construir contexto de cultivo y clima para el sistema prompt
                cultivoRepo.getCultivoActivo(uid).collect { cultivo ->
                    if (cultivo != null) {
                        val ctx = "Cultivo activo: ${cultivo.tipoCultivo}, ${cultivo.hectareas} ha, región ${cultivo.region}, etapa ${cultivo.etapaActual.name}."
                        _uiState.update { it.copy(cultivoContexto = ctx) }
                        climaService.getClimaActual(cultivo.region).onSuccess { c ->
                            val climaCtx = "Clima actual: ${c.temperatura}°C, ${c.descripcion}, humedad ${c.humedad}%."
                            _uiState.update { it.copy(climaContexto = climaCtx) }
                        }
                    }
                }
            }
        }
    }

    fun onInputChange(text: String) = _uiState.update { it.copy(inputText = text) }

    fun enviarMensaje() {
        val texto = _uiState.value.inputText.trim()
        if (texto.isBlank() || _uiState.value.isLoading) return

        viewModelScope.launch {
            // 1. Agregar mensaje del usuario
            val userMsg = ChatMensajeUi(rol = "user", contenido = texto)
            _uiState.update { it.copy(
                mensajes = it.mensajes + userMsg,
                inputText = "",
                isLoading = true,
                error = null
            )}
            chatDao.insert(ChatMensajeEntity(usuarioId = userId, rol = "user", contenido = texto))

            // 2. Placeholder de carga
            val loadingMsg = ChatMensajeUi(rol = "assistant", contenido = "...", sCargando = true)
            _uiState.update { it.copy(mensajes = it.mensajes + loadingMsg) }

            // 3. Llamar a Groq
            val state = _uiState.value
            val systemPrompt = buildString {
                append("Eres AgroBot, un asistente agrónomo experto para agricultores chilenos. ")
                append("Responde siempre en español, de forma clara, práctica y concisa. ")
                append("Contexto del agricultor: ")
                if (state.cultivoContexto.isNotBlank()) append(state.cultivoContexto + " ")
                if (state.climaContexto.isNotBlank()) append(state.climaContexto)
            }

            // Historial reciente (últimos 10 mensajes)
            val historial = state.mensajes
                .filter { !it.sCargando }
                .takeLast(10)
                .map { GroqMessage(it.rol, it.contenido) }

            iaService.chat(systemPrompt, historial, texto).fold(
                onSuccess = { respuesta ->
                    chatDao.insert(ChatMensajeEntity(usuarioId = userId, rol = "assistant", contenido = respuesta))
                    val assistantMsg = ChatMensajeUi(rol = "assistant", contenido = respuesta)
                    _uiState.update { s ->
                        s.copy(
                            mensajes = s.mensajes.dropLast(1) + assistantMsg,
                            isLoading = false
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { s ->
                        s.copy(
                            mensajes = s.mensajes.dropLast(1),
                            isLoading = false,
                            error = "Error al conectar con el asistente: ${e.message}"
                        )
                    }
                }
            )
        }
    }

    fun limpiarChat() = viewModelScope.launch {
        chatDao.limpiarChat(userId)
        _uiState.update { it.copy(mensajes = emptyList()) }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
