package com.agrocontrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.rememberNavController
import com.agrocontrol.data.repository.SessionManager
import com.agrocontrol.presentation.navigation.AgroControlNavGraph
import com.agrocontrol.presentation.navigation.Screen
import com.agrocontrol.presentation.theme.AgroControlTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AgroControlTheme {
                val navController = rememberNavController()
                val mainViewModel: MainViewModel = hiltViewModel()
                val startDestination by mainViewModel.startDestination.collectAsState()

                startDestination?.let { dest ->
                    AgroControlNavGraph(
                        navController    = navController,
                        startDestination = dest
                    )
                }
            }
        }
    }
}

@HiltViewModel
class MainViewModel @Inject constructor(private val session: SessionManager) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    init {
        viewModelScope.launch {
            val userId           = session.userId.firstOrNull()
            val rol              = session.userRol.firstOrNull()
            val hasSeenOnboarding = session.hasSeenOnboarding.firstOrNull() ?: false

            _startDestination.value = when {
                // Usuario ya logueado → va directo a su pantalla
                userId != null -> when (rol) {
                    "AGRONOMO"      -> Screen.Agronomo.route
                    "ADMINISTRADOR" -> Screen.AdminPanel.route
                    else            -> Screen.Dashboard.route
                }
                // Primera vez → Splash → Onboarding → Login
                !hasSeenOnboarding -> Screen.Splash.route
                // Ya vio onboarding → Splash → Login
                else               -> Screen.Splash.route
            }
        }
    }
}
