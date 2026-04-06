package com.agrocontrol.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.agrocontrol.domain.model.*

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val correo: String,
    val passwordHash: String,
    val rol: String,
    val agronomoAsignadoId: Long? = null
) {
    fun toDomain() = User(id, nombre, correo, passwordHash, UserRole.valueOf(rol), agronomoAsignadoId)
}

fun User.toEntity() = UserEntity(id, nombre, correo, passwordHash, rol.name, agronomoAsignadoId)

@Entity(tableName = "cultivos")
data class CultivoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val agricultorId: Long,
    val tipoCultivo: String,
    val hectareas: Double,
    val fechaSiembra: Long,
    val region: String,
    val etapaActual: String,
    val activo: Boolean = true
) {
    fun toDomain() = Cultivo(id, agricultorId, tipoCultivo, hectareas, fechaSiembra, region, EtapaCultivo.valueOf(etapaActual), activo)
}

fun Cultivo.toEntity() = CultivoEntity(id, agricultorId, tipoCultivo, hectareas, fechaSiembra, region, etapaActual.name, activo)

@Entity(tableName = "historial_etapas")
data class HistorialEtapaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cultivoId: Long,
    val etapa: String,
    val fechaCambio: Long,
    val notas: String
) {
    fun toDomain() = HistorialEtapa(id, cultivoId, EtapaCultivo.valueOf(etapa), fechaCambio, notas)
}

fun HistorialEtapa.toEntity() = HistorialEtapaEntity(id, cultivoId, etapa.name, fechaCambio, notas)

@Entity(tableName = "insumos")
data class InsumoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val agricultorId: Long,
    val nombre: String,
    val cantidadActual: Double,
    val unidad: String,
    val cantidadMinima: Double
) {
    fun toDomain() = Insumo(id, agricultorId, nombre, cantidadActual, unidad, cantidadMinima, cantidadActual < cantidadMinima)
}

fun Insumo.toEntity() = InsumoEntity(id, agricultorId, nombre, cantidadActual, unidad, cantidadMinima)

@Entity(tableName = "movimientos_inventario")
data class MovimientoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val insumoId: Long,
    val tipo: String,
    val cantidad: Double,
    val fecha: Long,
    val notas: String
) {
    fun toDomain() = MovimientoInventario(id, insumoId, tipo, cantidad, fecha, notas)
}

fun MovimientoInventario.toEntity() = MovimientoEntity(id, insumoId, tipo, cantidad, fecha, notas)

@Entity(tableName = "alertas")
data class AlertaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val agricultorId: Long,
    val tipo: String,
    val severidad: String,
    val descripcion: String,
    val recomendacion: String,
    val fechaEstimada: Long,
    val leida: Boolean = false,
    val fechaCreacion: Long
) {
    fun toDomain() = Alerta(id, agricultorId, TipoAlerta.valueOf(tipo), SeveridadAlerta.valueOf(severidad), descripcion, recomendacion, fechaEstimada, leida, fechaCreacion)
}

fun Alerta.toEntity() = AlertaEntity(id, agricultorId, tipo.name, severidad.name, descripcion, recomendacion, fechaEstimada, leida, fechaCreacion)
