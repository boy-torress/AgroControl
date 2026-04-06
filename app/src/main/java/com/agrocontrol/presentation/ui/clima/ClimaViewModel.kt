package com.agrocontrol.presentation.ui.clima

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agrocontrol.data.repository.*
import com.agrocontrol.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class ClimaUiState(
    val climaActual: ClimaActual? = null,
    val pronostico: List<PronosticoDia> = emptyList(),
    val recomendaciones: List<RecomendacionCultivo> = emptyList(),
    val region: String = "Valparaíso",
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ClimaViewModel @Inject constructor(
    private val session: SessionManager,
    private val cultivoRepo: CultivoRepository,
    private val climaService: ClimaService,
    private val iaService: IAService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClimaUiState())
    val uiState: StateFlow<ClimaUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            session.userId.filterNotNull().collect { uid ->
                cultivoRepo.getCultivoActivo(uid).collect { cultivo ->
                    val region = cultivo?.region ?: "Valparaíso"
                    _uiState.update { it.copy(region = region) }
                    loadClima(region)
                    if (cultivo != null) loadRecomendaciones(region, cultivo.tipoCultivo)
                }
            }
        }
    }

    private fun loadClima(region: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            climaService.getClimaActual(region).fold(
                onSuccess = { c -> _uiState.update { it.copy(climaActual = c, isLoading = false) } },
                onFailure = { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
            )
            climaService.getPronostico7Dias(region).fold(
                onSuccess = { p -> _uiState.update { it.copy(pronostico = p) } },
                onFailure = {}
            )
        }
    }

    private fun loadRecomendaciones(region: String, cultivo: String) {
        viewModelScope.launch {
            iaService.recomendarCultivos(region, "Franco-arcilloso", Calendar.getInstance().get(Calendar.MONTH) + 1).fold(
                onSuccess = { r -> _uiState.update { it.copy(recomendaciones = r) } },
                onFailure = {}
            )
        }
    }

    fun refresh() {
        loadClima(_uiState.value.region)
    }
}
