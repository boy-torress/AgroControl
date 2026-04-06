package com.agrocontrol.presentation.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agrocontrol.data.repository.*
import com.agrocontrol.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val userName: String = "",
    val cultivoActivo: Cultivo? = null,
    val clima: ClimaActual? = null,
    val alertasCount: Int = 0,
    val stockCriticoCount: Int = 0,
    // Solo true cuando NO tenemos datos de clima todavía (primera carga)
    val isLoadingClima: Boolean = false,
    val sinConexion: Boolean = false,
    val userId: Long = 0L
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val session: SessionManager,
    private val cultivoRepo: CultivoRepository,
    private val alertaRepo: AlertaRepository,
    private val inventarioRepo: InventarioRepository,
    private val climaService: ClimaService
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    // Región del último fetch de clima, para evitar llamadas redundantes
    private var lastClimaRegion: String? = null

    init {
        viewModelScope.launch {
            session.userId.filterNotNull().collect { uid ->
                _uiState.update { it.copy(userId = uid) }
                loadData(uid)
            }
        }
        viewModelScope.launch {
            session.userName.filterNotNull().collect { name ->
                _uiState.update { it.copy(userName = name) }
            }
        }
    }

    private fun loadData(uid: Long) {
        viewModelScope.launch {
            cultivoRepo.getCultivoActivo(uid)
                // Solo reaccionar cuando cambia la región (no en cada emit de Room)
                .distinctUntilChanged { old, new -> old?.region == new?.region }
                .collect { cultivo ->
                    _uiState.update { it.copy(cultivoActivo = cultivo) }
                    val region = cultivo?.region
                    // Solo cargar clima si la región es nueva o cambió
                    if (region != null && region != lastClimaRegion) {
                        lastClimaRegion = region
                        loadClima(region)
                    }
                }
        }
        viewModelScope.launch {
            // Actualizar cultivoActivo con todas las emisiones (no solo cambios de región)
            cultivoRepo.getCultivoActivo(uid).collect { cultivo ->
                _uiState.update { it.copy(cultivoActivo = cultivo) }
            }
        }
        viewModelScope.launch {
            alertaRepo.countAlertasNoLeidas(uid).collect { count ->
                _uiState.update { it.copy(alertasCount = count) }
            }
        }
        viewModelScope.launch {
            inventarioRepo.countStockCritico(uid).collect { count ->
                _uiState.update { it.copy(stockCriticoCount = count) }
            }
        }
    }

    private fun loadClima(region: String) {
        viewModelScope.launch {
            // Solo mostrar spinner si no tenemos datos de clima previos
            if (_uiState.value.clima == null) {
                _uiState.update { it.copy(isLoadingClima = true) }
            }
            climaService.getClimaActual(region).fold(
                onSuccess = { c ->
                    _uiState.update { it.copy(clima = c, isLoadingClima = false, sinConexion = false) }
                },
                onFailure = {
                    _uiState.update { it.copy(isLoadingClima = false, sinConexion = true) }
                }
            )
        }
    }

    fun refreshClima() {
        val region = lastClimaRegion ?: return
        loadClima(region)
    }

    fun logout() = viewModelScope.launch { session.clearSession() }
}
