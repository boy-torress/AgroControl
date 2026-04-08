package com.agrocontrol.domain.util

import android.util.Base64

/**
 * KeyProvider ofusca la API Key en el binario sin usar BuildConfig.
 * Almacena la llave ofuscada en fragmentos para evadir análisis de strings simple
 * y la reconstruye en caliente usando Base64.
 *
 * (Nota: Para seguridad enterprise real, se recomienda descargarla desde Firebase
 * Remote Config a un EncryptedSharedPreferences).
 */
object KeyProvider {

    // Groq API Key ofuscada (separada artificialmente y en Base64 para prevenir reversing de STR)
    // "YOUR_GROQ_API_KEY" -> en Base64 es:
    // WU9VUl9HUk9RX0FQSV9LRVk=
    
    // Lo separamos en chunks
    private const val CHUNK_1 = "WU9V"
    private const val CHUNK_2 = "Ul9H"
    private const val CHUNK_3 = "Uk9R"
    private const val CHUNK_4 = "X0FQ"
    private const val CHUNK_5 = "SV9LRVk="

    val groqApiKey: String
        get() {
            val encodedString = "$CHUNK_1$CHUNK_2$CHUNK_3$CHUNK_4$CHUNK_5"
            val decodedBytes = Base64.decode(encodedString, Base64.DEFAULT)
            return String(decodedBytes)
        }
}
