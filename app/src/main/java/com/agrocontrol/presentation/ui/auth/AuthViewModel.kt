package com.agrocontrol.presentation.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agrocontrol.data.repository.AuthRepository
import com.agrocontrol.data.repository.SessionManager
import com.agrocontrol.domain.model.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successRol: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepo: AuthRepository,
    private val session: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(correo: String, password: String) {
        if (correo.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = "Completa todos los campos") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepo.login(correo.trim(), password).fold(
                onSuccess = { user ->
                    session.saveSession(user.id, user.rol.name, user.nombre)
                    _uiState.update { it.copy(isLoading = false, successRol = user.rol.name) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun register(nombre: String, correo: String, password: String, confirmar: String) {
        when {
            nombre.isBlank() || correo.isBlank() || password.isBlank() ->
                _uiState.update { it.copy(error = "Completa todos los campos") }
            password != confirmar ->
                _uiState.update { it.copy(error = "Las contraseñas no coinciden") }
            password.length < 8 ->
                _uiState.update { it.copy(error = "La contraseña debe tener mínimo 8 caracteres") }
            !password.any { it.isUpperCase() } ->
                _uiState.update { it.copy(error = "La contraseña debe tener al menos una mayúscula") }
            !password.any { it.isDigit() } ->
                _uiState.update { it.copy(error = "La contraseña debe tener al menos un número") }
            else -> viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, error = null) }
                authRepo.registrar(nombre.trim(), correo.trim(), password, UserRole.AGRICULTOR).fold(
                    onSuccess = { user ->
                        session.saveSession(user.id, user.rol.name, user.nombre)
                        _uiState.update { it.copy(isLoading = false, successRol = user.rol.name) }
                    },
                    onFailure = { e ->
                        _uiState.update { it.copy(isLoading = false, error = e.message) }
                    }
                )
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
