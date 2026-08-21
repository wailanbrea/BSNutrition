package com.bsnutrition.app.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.data.repository.AuthRepository
import com.bsnutrition.app.core.model.ApiException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.isAuthenticated.collectLatest { authenticated ->
                _uiState.update { it.copy(isAuthenticated = authenticated) }
            }
        }
        viewModelScope.launch {
            authRepository.currentUser.collectLatest { user ->
                _uiState.update { it.copy(user = user) }
            }
        }
    }

    fun login(email: String, password: String) {
        val fieldErrors = mutableMapOf<String, String>()
        if (email.isBlank()) {
            fieldErrors["email"] = "El correo electrónico es requerido"
        }
        if (password.isBlank()) {
            fieldErrors["password"] = "La contraseña es requerida"
        }

        if (fieldErrors.isNotEmpty()) {
            _uiState.update { it.copy(fieldErrors = fieldErrors, errorMessage = null) }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null, fieldErrors = emptyMap()) }

        viewModelScope.launch {
            when (val result = authRepository.login(email.trim(), password)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            user = result.data,
                            isAuthenticated = true,
                            isSuccess = true,
                            errorMessage = null,
                            fieldErrors = emptyMap()
                        )
                    }
                }
                is Result.Error -> {
                    val fieldMap = if (result.exception is ApiException) {
                        result.exception.fields.mapValues { it.value.firstOrNull() ?: "" }
                    } else {
                        emptyMap()
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message,
                            fieldErrors = fieldMap,
                            isSuccess = false
                        )
                    }
                }
                is Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun register(
        name: String,
        email: String,
        password: String,
        passwordConfirmation: String
    ) {
        val fieldErrors = mutableMapOf<String, String>()
        if (name.isBlank()) {
            fieldErrors["name"] = "El nombre es requerido"
        }
        if (email.isBlank()) {
            fieldErrors["email"] = "El correo electrónico es requerido"
        }
        if (password.isBlank()) {
            fieldErrors["password"] = "La contraseña es requerida"
        }
        if (password != passwordConfirmation) {
            fieldErrors["password_confirmation"] = "Las contraseñas no coinciden"
        }

        if (fieldErrors.isNotEmpty()) {
            _uiState.update { it.copy(fieldErrors = fieldErrors, errorMessage = null) }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null, fieldErrors = emptyMap()) }

        viewModelScope.launch {
            when (
                val result = authRepository.register(
                    name = name.trim(),
                    email = email.trim(),
                    password = password,
                    passwordConfirmation = passwordConfirmation
                )
            ) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            user = result.data,
                            isAuthenticated = true,
                            isSuccess = true,
                            errorMessage = null,
                            fieldErrors = emptyMap()
                        )
                    }
                }
                is Result.Error -> {
                    val fieldMap = if (result.exception is ApiException) {
                        result.exception.fields.mapValues { it.value.firstOrNull() ?: "" }
                    } else {
                        emptyMap()
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message,
                            fieldErrors = fieldMap,
                            isSuccess = false
                        )
                    }
                }
                is Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun logout() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update {
                AuthUiState(
                    isLoading = false,
                    user = null,
                    isAuthenticated = false
                )
            }
        }
    }

    fun clearErrors() {
        _uiState.update { it.copy(errorMessage = null, fieldErrors = emptyMap()) }
    }
}
