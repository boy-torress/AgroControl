package com.agrocontrol.data.repository

import com.agrocontrol.domain.model.*
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt


// ─── Open-Meteo API DTOs ──────────────────────────────────────────────────────

data class OpenMeteoResponse(
    @SerializedName("current") val current: CurrentWeather?,
    @SerializedName("daily")   val daily: DailyWeather?
)

data class CurrentWeather(
    @SerializedName("temperature_2m")        val temperature: Double?,
    @SerializedName("relative_humidity_2m")  val humidity: Int?,
    @SerializedName("wind_speed_10m")        val windSpeed: Double?,
    @SerializedName("precipitation")          val precipitation: Double?
)

data class DailyWeather(
    @SerializedName("time")                          val time: List<String>?,
    @SerializedName("temperature_2m_max")            val tempMax: List<Double?>?,
    @SerializedName("temperature_2m_min")            val tempMin: List<Double?>?,
    @SerializedName("precipitation_probability_max") val precipProbability: List<Int?>?
)

// ─── Retrofit Interface ───────────────────────────────────────────────────────

interface OpenMeteoApi {
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude")               latitude: Double,
        @Query("longitude")              longitude: Double,
        @Query("current")                current: String = "temperature_2m,relative_humidity_2m,wind_speed_10m,precipitation",
        @Query("daily")                  daily: String   = "temperature_2m_max,temperature_2m_min,precipitation_probability_max",
        @Query("timezone")               timezone: String = "America/Santiago",
        @Query("forecast_days")          forecastDays: Int = 7
    ): OpenMeteoResponse
}

// ─── Coordenadas por región de Chile ─────────────────────────────────────────

private data class Coordenadas(val lat: Double, val lon: Double)

private val REGIONES_CHILE: Map<String, Coordenadas> = mapOf(
    // Valparaíso y alrededores
    "valparaíso"        to Coordenadas(-33.04, -71.62),
    "valparaiso"        to Coordenadas(-33.04, -71.62),
    "viña del mar"      to Coordenadas(-33.02, -71.55),
    "vina del mar"      to Coordenadas(-33.02, -71.55),
    "quilpué"           to Coordenadas(-33.05, -71.44),
    "quilpue"           to Coordenadas(-33.05, -71.44),
    "quillota"          to Coordenadas(-32.88, -71.25),
    "san antonio"       to Coordenadas(-33.59, -71.61),
    "los andes"         to Coordenadas(-32.83, -70.60),
    // Región Metropolitana
    "santiago"          to Coordenadas(-33.45, -70.65),
    "maipú"             to Coordenadas(-33.51, -70.76),
    "maipu"             to Coordenadas(-33.51, -70.76),
    "puente alto"       to Coordenadas(-33.61, -70.58),
    // O'Higgins
    "rancagua"          to Coordenadas(-34.17, -70.74),
    "san fernando"      to Coordenadas(-34.59, -71.00),
    // Maule
    "talca"             to Coordenadas(-35.43, -71.66),
    "curicó"            to Coordenadas(-34.99, -71.24),
    "curico"            to Coordenadas(-34.99, -71.24),
    "linares"           to Coordenadas(-35.85, -71.60),
    // Ñuble
    "chillán"           to Coordenadas(-36.61, -72.10),
    "chillan"           to Coordenadas(-36.61, -72.10),
    // Biobío
    "concepción"        to Coordenadas(-36.82, -73.04),
    "concepcion"        to Coordenadas(-36.82, -73.04),
    "los ángeles"       to Coordenadas(-37.47, -72.35),
    "los angeles"       to Coordenadas(-37.47, -72.35),
    // La Araucanía
    "temuco"            to Coordenadas(-38.74, -72.59),
    "villarrica"        to Coordenadas(-39.28, -72.23),
    // Los Lagos
    "puerto montt"      to Coordenadas(-41.47, -72.94),
    "osorno"            to Coordenadas(-40.57, -73.14),
    // Coquimbo
    "la serena"         to Coordenadas(-29.91, -71.25),
    "ovalle"            to Coordenadas(-30.60, -71.20),
    // Atacama
    "copiapó"           to Coordenadas(-27.37, -70.33),
    "copiapo"           to Coordenadas(-27.37, -70.33),
    // Default fallback
    "chile"             to Coordenadas(-33.04, -71.62)
)

private fun resolverCoordenadas(region: String): Coordenadas {
    val key = region.trim().lowercase()
    // Exact match first
    REGIONES_CHILE[key]?.let { return it }
    // Partial match
    for ((nombre, coords) in REGIONES_CHILE) {
        if (key.contains(nombre) || nombre.contains(key)) return coords
    }
    // Default: Valparaíso (coordenadas del proyecto)
    return Coordenadas(-33.04, -71.62)
}

// ─── Mapeo de condiciones climáticas ─────────────────────────────────────────

private fun inferirDescripcionEIcono(
    temp: Double,
    precipitacion: Double,
    humedad: Int,
    viento: Double
): Pair<String, String> {
    return when {
        precipitacion > 10.0 -> Pair("Lluvia intensa", "⛈️")
        precipitacion > 3.0  -> Pair("Lluvia moderada", "🌧️")
        precipitacion > 0.5  -> Pair("Lluvia ligera", "🌦️")
        viento > 50.0        -> Pair("Viento fuerte", "💨")
        temp < 5.0           -> Pair("Frío extremo", "🥶")
        temp < 10.0          -> Pair("Frío", "❄️")
        temp > 35.0          -> Pair("Calor extremo", "🔥")
        temp > 28.0          -> Pair("Caluroso", "☀️")
        humedad > 80         -> Pair("Nublado y húmedo", "☁️")
        humedad > 60         -> Pair("Parcialmente nublado", "⛅")
        else                 -> Pair("Despejado", "☀️")
    }
}

private fun inferirIconoDia(tempMax: Double, precipProb: Int): String = when {
    precipProb >= 70 -> "🌧️"
    precipProb >= 40 -> "🌦️"
    precipProb >= 20 -> "⛅"
    tempMax > 30     -> "☀️"
    tempMax > 20     -> "⛅"
    else             -> "☁️"
}

private fun descripcionDiaDesde(precipProb: Int, tempMax: Double): String = when {
    precipProb >= 70            -> "Lluvia probable"
    precipProb in 40..69        -> "Posible lluvia"
    precipProb in 20..39        -> "Nubosidad variable"
    tempMax > 30                -> "Caluroso y despejado"
    tempMax > 20                -> "Templado"
    tempMax < 10                -> "Frío"
    else                        -> "Despejado"
}

// ─── ClimaService con Open-Meteo ─────────────────────────────────────────────

@Singleton
class ClimaService @Inject constructor() {

    private val api: OpenMeteoApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenMeteoApi::class.java)
    }

    /** Caché simple en memoria: evita múltiples llamadas dentro de la misma hora */
    private var cache: CachedClima? = null

    private data class CachedClima(
        val region: String,
        val response: OpenMeteoResponse,
        val timestamp: Long = System.currentTimeMillis()
    ) {
        fun isValid(forRegion: String) =
            region == forRegion && (System.currentTimeMillis() - timestamp) < 3_600_000L // 1 hora
    }

    suspend fun getClimaActual(region: String): Result<ClimaActual> =
        withContext(Dispatchers.IO) {
            try {
                val response = fetchWithCache(region)
                val c = response.current
                    ?: return@withContext Result.failure(Exception("Sin datos actuales"))

                val temp  = c.temperature  ?: 15.0
                val hum   = c.humidity     ?: 60
                val wind  = c.windSpeed    ?: 10.0
                val prec  = c.precipitation ?: 0.0

                val (desc, icono) = inferirDescripcionEIcono(temp, prec, hum, wind)

                Result.success(
                    ClimaActual(
                        temperatura        = temp,
                        humedad            = hum,
                        viento             = wind,
                        precipitacion      = prec,
                        descripcion        = desc,
                        icono              = icono,
                        ultimaActualizacion = System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getPronostico7Dias(region: String): Result<List<PronosticoDia>> =
        withContext(Dispatchers.IO) {
            try {
                val response = fetchWithCache(region)
                val daily = response.daily
                    ?: return@withContext Result.failure(Exception("Sin pronóstico diario"))

                val times   = daily.time            ?: emptyList()
                val maxs    = daily.tempMax         ?: emptyList()
                val mins    = daily.tempMin         ?: emptyList()
                val probs   = daily.precipProbability ?: emptyList()

                val diasMs = 24L * 60 * 60 * 1000
                val hoy    = System.currentTimeMillis()

                val pronostico = times.indices.take(7).map { i ->
                    val tempMax  = maxs.getOrNull(i)  ?: 20.0
                    val tempMin  = mins.getOrNull(i)  ?: 10.0
                    val probLl   = probs.getOrNull(i) ?: 0

                    PronosticoDia(
                        fecha               = hoy + (i * diasMs),
                        tempMax             = tempMax,
                        tempMin             = tempMin,
                        probabilidadLluvia  = probLl,
                        descripcion         = descripcionDiaDesde(probLl, tempMax),
                        icono               = inferirIconoDia(tempMax, probLl)
                    )
                }

                Result.success(pronostico)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private suspend fun fetchWithCache(region: String): OpenMeteoResponse {
        cache?.takeIf { it.isValid(region) }?.let { return it.response }
        val coords   = resolverCoordenadas(region)
        val response = api.getForecast(latitude = coords.lat, longitude = coords.lon)
        cache        = CachedClima(region = region, response = response)
        return response
    }
}



