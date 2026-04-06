package com.agrocontrol.presentation.ui.alertas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agrocontrol.data.repository.*
import com.agrocontrol.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlertasUiState(
    val alertas: List<Alerta> = emptyList(),
    val isLoading: Boolean = false,
    val isGenerando: Boolean = false,
    val userId: Long = 0L,
    val error: String? = null
)

@HiltViewModel
class AlertasViewModel @Inject constructor(
    private val session: SessionManager,
    private val alertaRepo: AlertaRepository,
    private val climaService: ClimaService,
    private val iaService: IAService,
    private val cultivoRepo: CultivoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlertasUiState())
    val uiState: StateFlow<AlertasUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            session.userId.filterNotNull().collect { uid ->
                _uiState.update { it.copy(userId = uid) }
                // Observar alertas guardadas en DB
                launch {
                    alertaRepo.getAlertas(uid).collect { lista ->
                        _uiState.update { it.copy(alertas = lista) }
                    }
                }
                // Generar alertas inteligentes con Groq + clima real
                generarAlertasInteligentes(uid)
            }
        }
    }

    private suspend fun generarAlertasInteligentes(uid: Long) {
        _uiState.update { it.copy(isGenerando = true) }
        try {
            val cultivo = cultivoRepo.getCultivoActivo(uid).firstOrNull() ?: return
            val region  = cultivo.region

            // Obtener clima real de Open-Meteo
            val climaResult    = climaService.getClimaActual(region)
            val pronosticoResult = climaService.getPronostico7Dias(region)

            val clima      = climaResult.getOrNull()     ?: return
            val pronostico = pronosticoResult.getOrNull() ?: emptyList()

            // Llamar a Groq para generar alertas basadas en datos reales
            iaService.generarAlertasInteligentes(
                cultivo      = cultivo.tipoCultivo,
                region       = region,
                temperatura  = clima.temperatura,
                humedad      = clima.humedad,
                precipitacion = clima.precipitacion,
                viento       = clima.viento,
                pronostico   = pronostico
            ).onSuccess { alertasGroq ->
                alertasGroq.forEach { ag ->
                    val tipo = runCatching { TipoAlerta.valueOf(ag.tipo) }.getOrNull() ?: return@forEach
                    val sev  = runCatching { SeveridadAlerta.valueOf(ag.severidad) }.getOrNull() ?: return@forEach

                    alertaRepo.crearAlerta(
                        Alerta(
                            agricultorId  = uid,
                            tipo          = tipo,
                            severidad     = sev,
                            descripcion   = ag.descripcion,
                            recomendacion = ag.recomendacion,
                            fechaEstimada = System.currentTimeMillis() + (ag.horasHastaEvento * 3_600_000L)
                        )
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message) }
            }
        } finally {
            _uiState.update { it.copy(isGenerando = false) }
        }
    }

    fun marcarLeida(alertaId: Long) = viewModelScope.launch { alertaRepo.marcarLeida(alertaId) }
    fun marcarTodasLeidas() = viewModelScope.launch { alertaRepo.marcarTodasLeidas(_uiState.value.userId) }
    fun clearError() = _uiState.update { it.copy(error = null) }
}
