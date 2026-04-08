package com.agrocontrol.domain.model

import java.util.Date

// ─── Roles ───────────────────────────────────────────────────────────────────
enum class UserRole { AGRICULTOR, AGRONOMO, ADMINISTRADOR }

// ─── Usuario ─────────────────────────────────────────────────────────────────
data class User(
    val id: Long = 0,
    val nombre: String,
    val correo: String,
    val passwordHash: String,
    val rol: UserRole,
    val agronomoAsignadoId: Long? = null   // solo para AGRICULTOR
)

// ─── Cultivo ─────────────────────────────────────────────────────────────────
enum class EtapaCultivo {
    SIEMBRA, GERMINACION, CRECIMIENTO, FLORACION, COSECHA
}

data class Cultivo(
    val id: Long = 0,
    val agricultorId: Long,
    val tipoCultivo: String,
    val hectareas: Double,
    val fechaSiembra: Long,        // epoch millis
    val region: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val etapaActual: EtapaCultivo = EtapaCultivo.SIEMBRA,
    val activo: Boolean = true
)

data class HistorialEtapa(
    val id: Long = 0,
    val cultivoId: Long,
    val etapa: EtapaCultivo,
    val fechaCambio: Long = System.currentTimeMillis(),
    val notas: String = ""
)

// ─── Clima ───────────────────────────────────────────────────────────────────
data class ClimaActual(
    val temperatura: Double,
    val humedad: Int,
    val viento: Double,
    val precipitacion: Double,
    val descripcion: String,
    val icono: String,
    val ultimaActualizacion: Long = System.currentTimeMillis()
)

data class PronosticoDia(
    val fecha: Long,
    val tempMax: Double,
    val tempMin: Double,
    val probabilidadLluvia: Int,
    val descripcion: String,
    val icono: String
)

// ─── Predicción IA ───────────────────────────────────────────────────────────
data class PrediccionRendimiento(
    val kgPorHectarea: Double,
    val confianzaPorcentaje: Int,
    val factoresInfluyentes: List<String>,
    val fechaCalculo: Long = System.currentTimeMillis()
)

data class RecomendacionCultivo(
    val nombre: String,
    val nivelAdecuacion: String,      // "alto" | "medio" | "bajo"
    val rendimientoEsperado: String,
    val riesgosClimaticos: List<String>
)

// ─── Inventario ──────────────────────────────────────────────────────────────
data class Insumo(
    val id: Long = 0,
    val agricultorId: Long,
    val nombre: String,
    val cantidadActual: Double,
    val unidad: String,
    val cantidadMinima: Double,
    val enStockCritico: Boolean = false
)

data class MovimientoInventario(
    val id: Long = 0,
    val insumoId: Long,
    val tipo: String,                  // "entrada" | "salida"
    val cantidad: Double,
    val fecha: Long = System.currentTimeMillis(),
    val notas: String = ""
)

// ─── Alertas ─────────────────────────────────────────────────────────────────
enum class TipoAlerta { HELADA, LLUVIA_INTENSA, SEQUIA, STOCK_CRITICO }
enum class SeveridadAlerta { BAJO, MEDIO, ALTO }

data class Alerta(
    val id: Long = 0,
    val agricultorId: Long,
    val tipo: TipoAlerta,
    val severidad: SeveridadAlerta,
    val descripcion: String,
    val recomendacion: String,
    val fechaEstimada: Long,
    val leida: Boolean = false,
    val fechaCreacion: Long = System.currentTimeMillis()
)
