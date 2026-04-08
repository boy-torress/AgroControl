package com.agrocontrol.presentation.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agrocontrol.data.repository.*
import com.agrocontrol.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminUiState(
    val usuarios: List<User> = emptyList(),
    val cultivos: List<Cultivo> = emptyList(),
    val cultivosFiltrados: List<Cultivo> = emptyList(),
    val filtroEstado: String = "todos",
    val filtroTipo: String = "todos",
    val tiposCultivo: List<String> = emptyList()
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val session: SessionManager,
    private val authRepo: AuthRepository,
    private val cultivoRepo: CultivoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepo.getAllUsers().collect { users -> _uiState.update { it.copy(usuarios = users) } }
        }
        viewModelScope.launch {
            cultivoRepo.getAllCultivos().collect { cultivos ->
                val tipos = cultivos.map { it.tipoCultivo }.distinct()
                _uiState.update { it.copy(cultivos = cultivos, tiposCultivo = tipos) }
                aplicarFiltros()
            }
        }
    }

    fun setFiltroEstado(estado: String) {
        _uiState.update { it.copy(filtroEstado = estado) }
        aplicarFiltros()
    }

    fun setFiltroTipo(tipo: String) {
        _uiState.update { it.copy(filtroTipo = tipo) }
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        val estado = _uiState.value.filtroEstado
        val tipo = _uiState.value.filtroTipo
        val filtrados = _uiState.value.cultivos.filter { c ->
            val passEstado = when (estado) {
                "activo" -> c.activo
                "inactivo" -> !c.activo
                else -> true
            }
            val passTipo = tipo == "todos" || c.tipoCultivo == tipo
            passEstado && passTipo
        }
        _uiState.update { it.copy(cultivosFiltrados = filtrados) }
    }

    fun exportarCSV(): String {
        val header = "ID,Agricultor ID,Tipo,Hectáreas,Región,Etapa,Estado\n"
        val filas = _uiState.value.cultivosFiltrados.joinToString("\n") { c ->
            "${c.id},${c.agricultorId},${c.tipoCultivo},${c.hectareas},${c.region},${c.etapaActual.name},${if (c.activo) "activo" else "inactivo"}"
        }
        return header + filas
    }

    fun logout() = viewModelScope.launch { session.clear() }
}
