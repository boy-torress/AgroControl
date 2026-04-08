package com.agrocontrol.presentation.ui.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agrocontrol.data.repository.SessionManager
import com.agrocontrol.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PerfilUiState(
    val nombre: String = "",
    val correo: String = "",
    val rol: String = "",
    val editando: Boolean = false,
    val nombreEdit: String = "",
    val isSaving: Boolean = false,
    val success: Boolean = false
)

@HiltViewModel
class PerfilViewModel @Inject constructor(
    private val session: SessionManager,
    private val userRepo: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            session.userId.filterNotNull().collect { uid ->
                val user = userRepo.findById(uid) ?: return@collect
                _uiState.update { it.copy(
                    nombre = user.nombre,
                    correo = user.correo,
                    rol    = user.rol.name
                )}
            }
        }
    }

    fun startEdit() = _uiState.update { it.copy(editando = true, nombreEdit = it.nombre) }
    fun onNombreEdit(v: String) = _uiState.update { it.copy(nombreEdit = v) }
    fun cancelEdit() = _uiState.update { it.copy(editando = false) }

    fun guardar() {
        viewModelScope.launch {
            val nombre = _uiState.value.nombreEdit.trim()
            if (nombre.isBlank()) return@launch
            _uiState.update { it.copy(isSaving = true) }
            val uid = session.userId.firstOrNull() ?: return@launch
            userRepo.actualizarNombre(uid, nombre)
            _uiState.update { it.copy(nombre = nombre, editando = false, isSaving = false, success = true) }
        }
    }

    fun logout() = viewModelScope.launch { session.clear() }
    fun clearSuccess() = _uiState.update { it.copy(success = false) }
}
