package com.agrocontrol.presentation.navigation

import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.agrocontrol.presentation.ui.admin.AdminPanelScreen
import com.agrocontrol.presentation.ui.agronomo.AgronomoScreen
import com.agrocontrol.presentation.ui.alertas.AlertasScreen
import com.agrocontrol.presentation.ui.auth.LoginScreen
import com.agrocontrol.presentation.ui.auth.RegisterScreen
import com.agrocontrol.presentation.ui.clima.ClimaScreen
import com.agrocontrol.presentation.ui.cultivo.CultivoScreen
import com.agrocontrol.presentation.ui.cultivo.RegistroCultivoScreen
import com.agrocontrol.presentation.ui.dashboard.DashboardScreen
import com.agrocontrol.presentation.ui.inventario.InventarioScreen

sealed class Screen(val route: String) {
    object Login           : Screen("login")
    object Register        : Screen("register")
    object Dashboard       : Screen("dashboard")
    object Cultivo         : Screen("cultivo")
    object RegistroCultivo : Screen("registro_cultivo")
    object Clima           : Screen("clima")
    object Inventario      : Screen("inventario")
    object Alertas         : Screen("alertas")
    object Agronomo        : Screen("agronomo")
    object AdminPanel      : Screen("admin_panel")
}

@Composable
fun AgroControlNavGraph(navController: NavHostController, startDestination: String) {
    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { rol ->
                    val dest = when (rol) {
                        "AGRONOMO"       -> Screen.Agronomo.route
                        "ADMINISTRADOR"  -> Screen.AdminPanel.route
                        else             -> Screen.Dashboard.route
                    }
                    navController.navigate(dest) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToCultivo    = { navController.navigate(Screen.Cultivo.route) },
                onNavigateToClima      = { navController.navigate(Screen.Clima.route) },
                onNavigateToInventario = { navController.navigate(Screen.Inventario.route) },
                onNavigateToAlertas    = { navController.navigate(Screen.Alertas.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Cultivo.route) {
            CultivoScreen(
                onNavigateToRegistro = { navController.navigate(Screen.RegistroCultivo.route) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.RegistroCultivo.route) {
            RegistroCultivoScreen(
                onSuccess = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Clima.route) {
            ClimaScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Inventario.route) {
            InventarioScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Alertas.route) {
            AlertasScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Agronomo.route) {
            AgronomoScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.AdminPanel.route) {
            AdminPanelScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
