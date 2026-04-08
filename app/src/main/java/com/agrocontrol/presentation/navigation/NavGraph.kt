package com.agrocontrol.presentation.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.agrocontrol.presentation.ui.admin.AdminPanelScreen
import com.agrocontrol.presentation.ui.agronomo.AgronomoScreen
import com.agrocontrol.presentation.ui.alertas.AlertasScreen
import com.agrocontrol.presentation.ui.auth.LoginScreen
import com.agrocontrol.presentation.ui.auth.RegisterScreen
import com.agrocontrol.presentation.ui.calculadora.CalculadoraScreen
import com.agrocontrol.presentation.ui.chat.ChatScreen
import com.agrocontrol.presentation.ui.clima.ClimaScreen
import com.agrocontrol.presentation.ui.cultivo.CultivoScreen
import com.agrocontrol.presentation.ui.cultivo.RegistroCultivoScreen
import com.agrocontrol.presentation.ui.dashboard.DashboardScreen
import com.agrocontrol.presentation.ui.inventario.InventarioScreen
import com.agrocontrol.presentation.ui.main.MainScaffold
import com.agrocontrol.presentation.ui.onboarding.OnboardingScreen
import com.agrocontrol.presentation.ui.perfil.PerfilScreen
import com.agrocontrol.presentation.ui.splash.SplashScreen

sealed class Screen(val route: String) {
    object Splash          : Screen("splash")
    object Onboarding      : Screen("onboarding")
    object Login           : Screen("login")
    object Register        : Screen("register")
    object Dashboard       : Screen("dashboard")
    object Cultivo         : Screen("cultivo")
    object RegistroCultivo : Screen("registro_cultivo")
    object Clima           : Screen("clima")
    object Inventario      : Screen("inventario")
    object Alertas         : Screen("alertas")
    object ChatIA          : Screen("chat_ia")
    object Perfil          : Screen("perfil")
    object Calculadora     : Screen("calculadora")
    object Agronomo        : Screen("agronomo")
    object AdminPanel      : Screen("admin_panel")
    object MapaParcelas    : Screen("mapa_parcelas")
}

@Composable
fun AgroControlNavGraph(navController: NavHostController, startDestination: String) {

    val alertasCount by remember { mutableIntStateOf(0) } // conectar si se desea badge global

    // Rutas donde se muestra Bottom Nav
    val mainRoutes = setOf(
        Screen.Dashboard.route, Screen.Cultivo.route,
        Screen.Clima.route, Screen.Inventario.route, Screen.ChatIA.route
    )

    NavHost(navController = navController, startDestination = startDestination) {

        // ── Pre-login ──────────────────────────────────────────────────────────
        composable(Screen.Splash.route) {
            SplashScreen(onFinished = {
                navController.navigate(Screen.Onboarding.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            })
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(onFinished = {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                }
            })
        }

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

        // ── Pantallas principales con Bottom Nav ───────────────────────────────
        mainRoutes.forEach { route ->
            composable(route) {
                MainScaffold(
                    navController  = navController,
                    alertasCount   = alertasCount
                ) { padding ->
                    Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                        when (route) {
                            Screen.Dashboard.route ->
                                DashboardScreen(
                                    onNavigateToCultivo    = { navController.navigate(Screen.Cultivo.route) },
                                    onNavigateToClima      = { navController.navigate(Screen.Clima.route) },
                                    onNavigateToInventario = { navController.navigate(Screen.Inventario.route) },
                                    onNavigateToAlertas    = { navController.navigate(Screen.Alertas.route) },
                                    onNavigateToCalculadora = { navController.navigate(Screen.Calculadora.route) },
                                    onNavigateToPerfil     = { navController.navigate(Screen.Perfil.route) },
                                    onLogout = {
                                        navController.navigate(Screen.Login.route) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                )
                            Screen.Cultivo.route ->
                                CultivoScreen(
                                    onNavigateToRegistro = { navController.navigate(Screen.RegistroCultivo.route) },
                                    onNavigateToMapa = { navController.navigate(Screen.MapaParcelas.route) },
                                    onBack = { navController.popBackStack() }
                                )
                            Screen.Clima.route ->
                                ClimaScreen(onBack = { navController.popBackStack() })
                            Screen.Inventario.route ->
                                InventarioScreen(onBack = { navController.popBackStack() })
                            Screen.ChatIA.route ->
                                ChatScreen()
                            else -> {}
                        }
                    }
                }
            }
        }

        // ── Pantallas secundarias (sin Bottom Nav) ─────────────────────────────
        composable(Screen.MapaParcelas.route) {
            com.agrocontrol.presentation.ui.cultivo.MapaParcelasScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.RegistroCultivo.route) {
            RegistroCultivoScreen(
                onSuccess = { navController.popBackStack() },
                onBack    = { navController.popBackStack() }
            )
        }

        composable(Screen.Alertas.route) {
            AlertasScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Perfil.route) {
            PerfilScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Calculadora.route) {
            CalculadoraScreen()
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
