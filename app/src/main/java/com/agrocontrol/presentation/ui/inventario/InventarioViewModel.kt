package com.agrocontrol.presentation.ui.inventario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agrocontrol.data.repository.*
import com.agrocontrol.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InventarioUiState(
    val insumos: List<Insumo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddDialog: Boolean = false,
    val userId: Long = 0L
)

@HiltViewModel
class InventarioViewModel @Inject constructor(
    private val session: SessionManager,
    private val inventarioRepo: InventarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventarioUiState())
    val uiState: StateFlow<InventarioUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            session.userId.filterNotNull().collect { uid ->
                _uiState.update { it.copy(userId = uid) }
                inventarioRepo.getInsumos(uid).collect { lista ->
                    _uiState.update { it.copy(insumos = lista) }
                }
            }
        }
    }

    fun agregarInsumo(nombre: String, cantidad: String, unidad: String, minimo: String) {
        val cant = cantidad.toDoubleOrNull()
        val min = minimo.toDoubleOrNull()
        when {
            nombre.isBlank() -> _uiState.update { it.copy(error = "Ingresa el nombre del insumo") }
            cant == null || cant < 0 -> _uiState.update { it.copy(error = "Cantidad inválida") }
            unidad.isBlank() -> _uiState.update { it.copy(error = "Selecciona una unidad") }
            min == null || min < 0 -> _uiState.update { it.copy(error = "Cantidad mínima inválida") }
            else -> viewModelScope.launch {
                val insumo = Insumo(
                    agricultorId = _uiState.value.userId,
                    nombre = nombre,
                    cantidadActual = cant,
                    unidad = unidad,
                    cantidadMinima = min
                )
                inventarioRepo.agregarInsumo(insumo)
                _uiState.update { it.copy(showAddDialog = false, error = null) }
            }
        }
    }

    fun registrarMovimiento(insumoId: Long, cantidad: String, tipo: String) {
        val cant = cantidad.toDoubleOrNull() ?: return
        viewModelScope.launch {
            inventarioRepo.actualizarCantidad(insumoId, cant, tipo)
        }
    }

    fun eliminarInsumo(insumo: Insumo) = viewModelScope.launch { inventarioRepo.eliminarInsumo(insumo) }

    fun showAddDialog() = _uiState.update { it.copy(showAddDialog = true, error = null) }
    fun hideAddDialog() = _uiState.update { it.copy(showAddDialog = false, error = null) }
    fun clearError() = _uiState.update { it.copy(error = null) }
}
