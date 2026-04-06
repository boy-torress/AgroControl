package com.agrocontrol.presentation.ui.agronomo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agrocontrol.data.repository.*
import com.agrocontrol.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AgronomoUiState(
    val agricultores: List<User> = emptyList(),
    val cultivosPorAgricultor: Map<Long, Cultivo?> = emptyMap(),
    val historialSeleccionado: List<HistorialEtapa> = emptyList(),
    val alertasPorAgricultor: Map<Long, Int> = emptyMap(),
    val selectedAgricultor: User? = null,
    val userId: Long = 0L
)

@HiltViewModel
class AgronomoViewModel @Inject constructor(
    private val session: SessionManager,
    private val authRepo: AuthRepository,
    private val cultivoRepo: CultivoRepository,
    private val alertaRepo: AlertaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgronomoUiState())
    val uiState: StateFlow<AgronomoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            session.userId.filterNotNull().collect { uid ->
                _uiState.update { it.copy(userId = uid) }
                authRepo.getAgricultoresByAgronomo(uid).collect { lista ->
                    _uiState.update { it.copy(agricultores = lista) }
                    lista.forEach { ag ->
                        cultivoRepo.getCultivoActivo(ag.id).collect { cultivo ->
                            _uiState.update { s ->
                                s.copy(cultivosPorAgricultor = s.cultivosPorAgricultor + (ag.id to cultivo))
                            }
                        }
                        alertaRepo.countAlertasNoLeidas(ag.id).collect { count ->
                            _uiState.update { s ->
                                s.copy(alertasPorAgricultor = s.alertasPorAgricultor + (ag.id to count))
                            }
                        }
                    }
                }
            }
        }
    }

    fun selectAgricultor(ag: User?) {
        _uiState.update { it.copy(selectedAgricultor = ag) }
        if (ag != null) {
            viewModelScope.launch {
                val cultivo = _uiState.value.cultivosPorAgricultor[ag.id]
                if (cultivo != null) {
                    cultivoRepo.getHistorialEtapas(cultivo.id).collect { historial ->
                        _uiState.update { it.copy(historialSeleccionado = historial) }
                    }
                }
            }
        }
    }

    fun logout() = viewModelScope.launch { session.clearSession() }
}
