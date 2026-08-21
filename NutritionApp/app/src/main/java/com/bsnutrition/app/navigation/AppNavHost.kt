package com.bsnutrition.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bsnutrition.app.feature.auth.AuthViewModel
import com.bsnutrition.app.feature.auth.LoginScreen
import com.bsnutrition.app.feature.auth.RegisterScreen
import com.bsnutrition.app.feature.onboarding.OnboardingScreen
import com.bsnutrition.app.feature.onboarding.OnboardingViewModel

@Composable
fun AppNavHost(
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val uiState by authViewModel.uiState.collectAsState()

    val startDestination: Any = if (uiState.isAuthenticated) {
        Route.Main
    } else {
        Route.Login
    }

    LaunchedEffect(uiState.isAuthenticated) {
        if (!uiState.isAuthenticated) {
            navController.navigate(Route.Login) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable<Route.Login> {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = {
                    navController.navigate(Route.Register)
                },
                onLoginSuccess = {
                    navController.navigate(Route.Main) {
                        popUpTo(Route.Login) { inclusive = true }
                    }
                }
            )
        }

        composable<Route.Register> {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(Route.Onboarding) {
                        popUpTo(Route.Login) { inclusive = true }
                    }
                }
            )
        }

        composable<Route.Onboarding> {
            val onboardingViewModel: OnboardingViewModel = hiltViewModel()
            OnboardingScreen(
                viewModel = onboardingViewModel,
                onOnboardingFinished = {
                    navController.navigate(Route.Main) {
                        popUpTo(Route.Login) { inclusive = true }
                    }
                }
            )
        }

        composable<Route.Main> {
            MainTabScreen(
                user = uiState.user,
                onLogout = {
                    authViewModel.logout()
                }
            )
        }
    }
}

