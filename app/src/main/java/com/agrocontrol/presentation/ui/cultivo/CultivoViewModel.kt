package com.agrocontrol.presentation.ui.cultivo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agrocontrol.data.repository.*
import com.agrocontrol.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CultivoUiState(
    val cultivoActivo: Cultivo? = null,
    val historial: List<HistorialEtapa> = emptyList(),
    val prediccion: PrediccionRendimiento? = null,
    val isLoadingPrediccion: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val isLoading: Boolean = false,
    val userId: Long = 0L
)

@HiltViewModel
class CultivoViewModel @Inject constructor(
    private val session: SessionManager,
    private val cultivoRepo: CultivoRepository,
    private val iaService: IAService
) : ViewModel() {

    private val _uiState = MutableStateFlow(CultivoUiState())
    val uiState: StateFlow<CultivoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            session.userId.filterNotNull().collect { uid ->
                _uiState.update { it.copy(userId = uid) }
                cultivoRepo.getCultivoActivo(uid).collect { cultivo ->
                    _uiState.update { it.copy(cultivoActivo = cultivo) }
                    cultivo?.let { loadHistorial(it.id) }
                }
            }
        }
    }

    private fun loadHistorial(cultivoId: Long) {
        viewModelScope.launch {
            cultivoRepo.getHistorialEtapas(cultivoId).collect { h ->
                _uiState.update { it.copy(historial = h) }
            }
        }
    }

    fun registrarCultivo(tipo: String, hectareas: String, region: String, fechaSiembra: Long) {
        val ha = hectareas.toDoubleOrNull()
        when {
            tipo.isBlank() -> _uiState.update { it.copy(error = "Selecciona el tipo de cultivo") }
            ha == null || ha <= 0 -> _uiState.update { it.copy(error = "Ingresa una cantidad válida de hectáreas") }
            region.isBlank() -> _uiState.update { it.copy(error = "Selecciona la región") }
            else -> viewModelScope.launch {
                val cultivo = Cultivo(
                    agricultorId = _uiState.value.userId,
                    tipoCultivo = tipo,
                    hectareas = ha,
                    fechaSiembra = fechaSiembra,
                    region = region
                )
                cultivoRepo.registrarCultivo(cultivo)
                _uiState.update { it.copy(success = true, error = null) }
            }
        }
    }

    fun actualizarEtapa(nuevaEtapa: EtapaCultivo, notas: String = "") {
        val cultivo = _uiState.value.cultivoActivo ?: return
        viewModelScope.launch {
            cultivoRepo.actualizarEtapa(cultivo.id, cultivo.agricultorId, nuevaEtapa, notas)
        }
    }

    fun actualizarUbicacion(latitude: Double, longitude: Double) {
        val cultivo = _uiState.value.cultivoActivo ?: return
        viewModelScope.launch {
            cultivoRepo.actualizarUbicacion(cultivo.agricultorId, latitude, longitude)
        }
    }

    fun predecirRendimiento() {
        val cultivo = _uiState.value.cultivoActivo ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingPrediccion = true) }
            iaService.predecirRendimiento(cultivo.tipoCultivo, cultivo.hectareas, cultivo.etapaActual).fold(
                onSuccess = { p -> _uiState.update { it.copy(prediccion = p, isLoadingPrediccion = false) } },
                onFailure = { e -> _uiState.update { it.copy(error = e.message, isLoadingPrediccion = false) } }
            )
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
    fun clearSuccess() = _uiState.update { it.copy(success = false) }
}
