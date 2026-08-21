package com.bsnutrition.app.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bsnutrition.app.core.designsystem.component.BsnCard
import com.bsnutrition.app.core.designsystem.component.BsnPrimaryButton
import com.bsnutrition.app.core.designsystem.component.BsnTextField

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    if (uiState.isAuthenticated && uiState.isSuccess) {
        onLoginSuccess()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Bienvenido a BSNutrition",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Inicia sesión para gestionar tu plan nutricional",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        BsnCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                BsnTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        viewModel.clearErrors()
                    },
                    label = "Correo electrónico",
                    errorMessage = uiState.fieldErrors["email"],
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )

                BsnTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        viewModel.clearErrors()
                    },
                    label = "Contraseña",
                    isPassword = true,
                    errorMessage = uiState.fieldErrors["password"],
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                BsnPrimaryButton(
                    text = "Iniciar Sesión",
                    onClick = { viewModel.login(email, password) },
                    isLoading = uiState.isLoading
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "¿No tienes cuenta?",
                style = MaterialTheme.typography.bodyMedium
            )
            TextButton(onClick = onNavigateToRegister) {
                Text(
                    text = "Regístrate aquí",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
