package com.agrocontrol.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

@Singleton
class SessionManager @Inject constructor(@ApplicationContext private val context: Context) {

    private val USER_ID          = longPreferencesKey("user_id")
    private val USER_ROL         = stringPreferencesKey("user_rol")
    private val USER_NOMBRE      = stringPreferencesKey("user_nombre")
    private val LAST_ACTIVE      = longPreferencesKey("last_active")
    private val SEEN_ONBOARDING  = booleanPreferencesKey("seen_onboarding")

    val userId: Flow<Long?> = context.dataStore.data.map { prefs ->
        val id = prefs[USER_ID] ?: return@map null
        val lastActive = prefs[LAST_ACTIVE] ?: 0L
        // Sesión expira en 30 minutos de inactividad
        if (System.currentTimeMillis() - lastActive > 30 * 60 * 1000L) null else id
    }

    val userRol: Flow<String?>     = context.dataStore.data.map { it[USER_ROL] }
    val userName: Flow<String?>    = context.dataStore.data.map { it[USER_NOMBRE] }
    val hasSeenOnboarding: Flow<Boolean> = context.dataStore.data.map {
        it[SEEN_ONBOARDING] ?: false
    }

    suspend fun saveSession(userId: Long, rol: String, nombre: String) {
        context.dataStore.edit { prefs ->
            prefs[USER_ID]     = userId
            prefs[USER_ROL]    = rol
            prefs[USER_NOMBRE] = nombre
            prefs[LAST_ACTIVE] = System.currentTimeMillis()
        }
    }

    suspend fun refreshActivity() {
        context.dataStore.edit { it[LAST_ACTIVE] = System.currentTimeMillis() }
    }

    suspend fun markOnboardingSeen() {
        context.dataStore.edit { it[SEEN_ONBOARDING] = true }
    }

    /** Cierra sesión del usuario (mantiene el flag de onboarding) */
    suspend fun clear() {
        context.dataStore.edit { prefs ->
            prefs.remove(USER_ID)
            prefs.remove(USER_ROL)
            prefs.remove(USER_NOMBRE)
            prefs.remove(LAST_ACTIVE)
        }
    }

    /** Limpia todo incluido el onboarding (factory reset) */
    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
