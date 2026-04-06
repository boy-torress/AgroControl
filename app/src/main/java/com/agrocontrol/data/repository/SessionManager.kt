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

    private val USER_ID = longPreferencesKey("user_id")
    private val USER_ROL = stringPreferencesKey("user_rol")
    private val USER_NOMBRE = stringPreferencesKey("user_nombre")
    private val LAST_ACTIVE = longPreferencesKey("last_active")

    val userId: Flow<Long?> = context.dataStore.data.map { prefs ->
        val id = prefs[USER_ID] ?: return@map null
        val lastActive = prefs[LAST_ACTIVE] ?: 0L
        // Sesión expira en 30 minutos
        if (System.currentTimeMillis() - lastActive > 30 * 60 * 1000L) null else id
    }

    val userRol: Flow<String?> = context.dataStore.data.map { it[USER_ROL] }
    val userName: Flow<String?> = context.dataStore.data.map { it[USER_NOMBRE] }

    suspend fun saveSession(userId: Long, rol: String, nombre: String) {
        context.dataStore.edit { prefs ->
            prefs[USER_ID] = userId
            prefs[USER_ROL] = rol
            prefs[USER_NOMBRE] = nombre
            prefs[LAST_ACTIVE] = System.currentTimeMillis()
        }
    }

    suspend fun refreshActivity() {
        context.dataStore.edit { it[LAST_ACTIVE] = System.currentTimeMillis() }
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}
