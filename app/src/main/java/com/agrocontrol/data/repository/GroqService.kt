package com.agrocontrol.data.repository

import com.agrocontrol.BuildConfig
import com.agrocontrol.domain.model.*
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

// ─── Groq API DTOs ────────────────────────────────────────────────────────────

data class GroqMessage(
    @SerializedName("role")    val role: String,
    @SerializedName("content") val content: String
)

data class GroqRequest(
    @SerializedName("model")       val model: String = "llama-3.3-70b-versatile",
    @SerializedName("messages")    val messages: List<GroqMessage>,
    @SerializedName("temperature") val temperature: Double = 0.3,
    @SerializedName("max_tokens")  val maxTokens: Int = 1024,
    @SerializedName("response_format") val responseFormat: Map<String, String>? = mapOf("type" to "json_object")
)

data class GroqChoice(
    @SerializedName("message") val message: GroqMessage
)

data class GroqResponse(
    @SerializedName("choices") val choices: List<GroqChoice>
)

// ─── Retrofit Interface ───────────────────────────────────────────────────────

interface GroqApi {
    @POST("openai/v1/chat/completions")
    suspend fun chatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: GroqRequest
    ): GroqResponse
}

// ─── IA Service con Groq ─────────────────────────────────────────────────────

@Singleton
class IAService @Inject constructor() {

    private val gson = Gson()

    private val api: GroqApi by lazy {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .followRedirects(false)        // evita que POST se convierta en GET en redirects
            .followSslRedirects(false)     // ídem para HTTPS
            .build()

        Retrofit.Builder()
            .baseUrl("https://api.groq.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GroqApi::class.java)
    }

    private val authHeader get() = "Bearer ${BuildConfig.GROQ_API_KEY}"

    // ─── Predicción de rendimiento ────────────────────────────────────────────

    suspend fun predecirRendimiento(
        cultivo: String,
        hectareas: Double,
        etapa: EtapaCultivo
    ): Result<PrediccionRendimiento> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                Eres un agrónomo experto en cultivos chilenos. Analiza y predice el rendimiento.
                
                Datos del cultivo:
                - Tipo: $cultivo
                - Superficie: $hectareas hectáreas
                - Etapa actual: ${etapa.name}
                
                Responde ÚNICAMENTE con este JSON (sin texto adicional):
                {
                  "kgPorHectarea": <número decimal, rendimiento estimado en kg por hectárea>,
                  "confianzaPorcentaje": <entero entre 65 y 95>,
                  "factoresInfluyentes": ["factor1", "factor2", "factor3"]
                }
                
                Basa tu estimación en promedios reales de Chile para este cultivo y etapa.
                Los factores deben ser específicos y concretos (clima, suelo, etapa fenológica, etc).
            """.trimIndent()

            val response = api.chatCompletion(
                authorization = authHeader,
                request = GroqRequest(
                    messages = listOf(
                        GroqMessage("system", "Eres un sistema de predicción agrícola. Responde solo con JSON válido."),
                        GroqMessage("user", prompt)
                    )
                )
            )

            val json = response.choices.first().message.content
            val parsed = gson.fromJson(json, PrediccionGroqResponse::class.java)

            Result.success(
                PrediccionRendimiento(
                    kgPorHectarea       = (parsed.kgPorHectarea * 10).roundToInt() / 10.0,
                    confianzaPorcentaje = parsed.confianzaPorcentaje.coerceIn(60, 98),
                    factoresInfluyentes = parsed.factoresInfluyentes
                )
            )
        } catch (e: Exception) {
            // Fallback con valores base si Groq falla
            Result.failure(Exception("No se pudo obtener predicción: ${e.message}"))
        }
    }

    // ─── Recomendaciones de cultivos ─────────────────────────────────────────

    suspend fun recomendarCultivos(
        region: String,
        tipoSuelo: String,
        mesInicio: Int
    ): Result<List<RecomendacionCultivo>> = withContext(Dispatchers.IO) {
        try {
            val meses = listOf("", "enero", "febrero", "marzo", "abril", "mayo", "junio",
                "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre")
            val mesNombre = meses.getOrElse(mesInicio) { "actual" }

            val prompt = """
                Eres un experto en agronomía chilena. Recomienda cultivos para:
                - Región: $region, Chile
                - Tipo de suelo: $tipoSuelo
                - Mes de inicio: $mesNombre
                
                Responde ÚNICAMENTE con este JSON:
                {
                  "recomendaciones": [
                    {
                      "nombre": "nombre del cultivo",
                      "nivelAdecuacion": "alto" | "medio" | "bajo",
                      "rendimientoEsperado": "X.XXX – Y.YYY kg/ha",
                      "riesgosClimaticos": ["riesgo1", "riesgo2"]
                    }
                  ]
                }
                
                Incluye exactamente 3 cultivos ordenados de mayor a menor adecuación.
                Usa rendimientos reales de Chile. Riesgos específicos para la región y mes dado.
            """.trimIndent()

            val response = api.chatCompletion(
                authorization = authHeader,
                request = GroqRequest(
                    messages = listOf(
                        GroqMessage("system", "Eres un sistema de recomendación agrícola para Chile. Responde solo con JSON válido."),
                        GroqMessage("user", prompt)
                    )
                )
            )

            val json = response.choices.first().message.content
            val parsed = gson.fromJson(json, RecomendacionesGroqResponse::class.java)

            Result.success(parsed.recomendaciones.map {
                RecomendacionCultivo(
                    nombre             = it.nombre,
                    nivelAdecuacion    = it.nivelAdecuacion,
                    rendimientoEsperado = it.rendimientoEsperado,
                    riesgosClimaticos  = it.riesgosClimaticos
                )
            })
        } catch (e: Exception) {
            Result.failure(Exception("No se pudo obtener recomendaciones: ${e.message}"))
        }
    }

    // ─── Generación de alertas basadas en clima real ──────────────────────────

    suspend fun generarAlertasInteligentes(
        cultivo: String,
        region: String,
        temperatura: Double,
        humedad: Int,
        precipitacion: Double,
        viento: Double,
        pronostico: List<PronosticoDia>
    ): Result<List<AlertaGroq>> = withContext(Dispatchers.IO) {
        try {
            val pronosticoTexto = pronostico.take(3).joinToString("\n") { dia ->
                "- Día +${pronostico.indexOf(dia) + 1}: máx ${dia.tempMax}°C, mín ${dia.tempMin}°C, lluvia ${dia.probabilidadLluvia}%, ${dia.descripcion}"
            }

            val prompt = """
                Eres un sistema de alertas agroclimáticas para Chile. Analiza los datos y genera alertas reales.
                
                Cultivo actual: $cultivo en $region
                
                Clima ahora mismo:
                - Temperatura: $temperatura°C
                - Humedad: $humedad%
                - Precipitación: $precipitacion mm
                - Viento: $viento km/h
                
                Pronóstico próximos 3 días:
                $pronosticoTexto
                
                Genera SOLO las alertas que realmente aplican según estos datos.
                Si no hay riesgo real, devuelve lista vacía.
                
                Los tipos válidos son: HELADA, LLUVIA_INTENSA, SEQUIA
                Las severidades válidas son: BAJO, MEDIO, ALTO
                
                Responde ÚNICAMENTE con este JSON:
                {
                  "alertas": [
                    {
                      "tipo": "HELADA|LLUVIA_INTENSA|SEQUIA",
                      "severidad": "BAJO|MEDIO|ALTO",
                      "descripcion": "descripción específica del riesgo",
                      "recomendacion": "acción concreta para el agricultor",
                      "horasHastaEvento": <número entero de horas hasta el evento estimado>
                    }
                  ]
                }
            """.trimIndent()

            val response = api.chatCompletion(
                authorization = authHeader,
                request = GroqRequest(
                    messages = listOf(
                        GroqMessage("system", "Eres un sistema de alertas agroclimáticas preciso. Responde solo con JSON válido. No inventes alertas si los datos no las justifican."),
                        GroqMessage("user", prompt)
                    ),
                    temperature = 0.1  // más determinístico para alertas
                )
            )

            val json = response.choices.first().message.content
            val parsed = gson.fromJson(json, AlertasGroqResponse::class.java)
            Result.success(parsed.alertas ?: emptyList())
        } catch (e: Exception) {
            Result.failure(Exception("No se pudieron generar alertas: ${e.message}"))
        }
    }
}

// ─── Response data classes para parseo ───────────────────────────────────────

private data class PrediccionGroqResponse(
    val kgPorHectarea: Double,
    val confianzaPorcentaje: Int,
    val factoresInfluyentes: List<String>
)

private data class RecomendacionesGroqResponse(
    val recomendaciones: List<RecomendacionGroqItem>
)

private data class RecomendacionGroqItem(
    val nombre: String,
    val nivelAdecuacion: String,
    val rendimientoEsperado: String,
    val riesgosClimaticos: List<String>
)

data class AlertasGroqResponse(
    val alertas: List<AlertaGroq>?
)

data class AlertaGroq(
    val tipo: String,
    val severidad: String,
    val descripcion: String,
    val recomendacion: String,
    val horasHastaEvento: Int
)
