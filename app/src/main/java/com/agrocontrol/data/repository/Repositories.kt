package com.agrocontrol.data.repository

import com.agrocontrol.data.local.dao.*
import com.agrocontrol.data.local.entities.*
import com.agrocontrol.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// ─── Auth Repository ──────────────────────────────────────────────────────────
@Singleton
class AuthRepository @Inject constructor(private val userDao: UserDao) {

    suspend fun registrar(nombre: String, correo: String, password: String, rol: UserRole): Result<User> {
        return try {
            val existente = userDao.findByCorreo(correo)
            if (existente != null) return Result.failure(Exception("Este correo ya tiene una cuenta registrada"))
            val hash = password.hashCode().toString()
            val entity = UserEntity(nombre = nombre, correo = correo, passwordHash = hash, rol = rol.name)
            val id = userDao.insert(entity)
            Result.success(entity.copy(id = id).toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(correo: String, password: String): Result<User> {
        return try {
            val entity = userDao.findByCorreo(correo)
                ?: return Result.failure(Exception("Correo o contraseña incorrectos"))
            val hash = password.hashCode().toString()
            if (entity.passwordHash != hash) return Result.failure(Exception("Correo o contraseña incorrectos"))
            Result.success(entity.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserById(id: Long): User? = userDao.findById(id)?.toDomain()

    fun getAllUsers(): Flow<List<User>> = userDao.getAllUsers().map { list -> list.map { it.toDomain() } }

    fun getAgricultoresByAgronomo(agronomoId: Long): Flow<List<User>> =
        userDao.getAgricultoresByAgronomo(agronomoId).map { list -> list.map { it.toDomain() } }
}

// ─── Cultivo Repository ───────────────────────────────────────────────────────
@Singleton
class CultivoRepository @Inject constructor(
    private val cultivoDao: CultivoDao,
    private val historialDao: HistorialEtapaDao
) {
    fun getCultivoActivo(agricultorId: Long): Flow<Cultivo?> =
        cultivoDao.getCultivoActivo(agricultorId).map { it?.toDomain() }

    fun getCultivosByAgricultor(agricultorId: Long): Flow<List<Cultivo>> =
        cultivoDao.getCultivosByAgricultor(agricultorId).map { list -> list.map { it.toDomain() } }

    fun getAllCultivos(): Flow<List<Cultivo>> =
        cultivoDao.getAllCultivos().map { list -> list.map { it.toDomain() } }

    suspend fun registrarCultivo(cultivo: Cultivo): Long {
        cultivoDao.desactivarCultivosActivos(cultivo.agricultorId)
        val id = cultivoDao.insert(cultivo.toEntity())
        historialDao.insert(HistorialEtapa(cultivoId = id, etapa = cultivo.etapaActual).toEntity())
        return id
    }

    suspend fun actualizarEtapa(cultivoId: Long, agricultorId: Long, nuevaEtapa: EtapaCultivo, notas: String = "") {
        val cultivoEntity = cultivoDao.getCultivoActivoOnce(agricultorId) ?: return
        cultivoDao.update(cultivoEntity.copy(etapaActual = nuevaEtapa.name))
        historialDao.insert(HistorialEtapa(cultivoId = cultivoId, etapa = nuevaEtapa, notas = notas).toEntity())
    }

    fun getHistorialEtapas(cultivoId: Long): Flow<List<HistorialEtapa>> =
        historialDao.getHistorialByCultivo(cultivoId).map { list -> list.map { it.toDomain() } }
}

// ─── Inventario Repository ────────────────────────────────────────────────────
@Singleton
class InventarioRepository @Inject constructor(
    private val insumoDao: InsumoDao,
    private val movimientoDao: MovimientoDao
) {
    fun getInsumos(agricultorId: Long): Flow<List<Insumo>> =
        insumoDao.getInsumosByAgricultor(agricultorId).map { list -> list.map { it.toDomain() } }

    fun countStockCritico(agricultorId: Long): Flow<Int> =
        insumoDao.countStockCritico(agricultorId)

    suspend fun agregarInsumo(insumo: Insumo): Long = insumoDao.insert(insumo.toEntity())

    suspend fun actualizarCantidad(insumoId: Long, nuevaCantidad: Double, tipo: String, notas: String = "") {
        val entity = insumoDao.findById(insumoId) ?: return
        val diff = if (tipo == "entrada") nuevaCantidad else nuevaCantidad
        insumoDao.update(entity.copy(cantidadActual = if (tipo == "entrada") entity.cantidadActual + diff else entity.cantidadActual - diff))
        movimientoDao.insert(MovimientoEntity(insumoId = insumoId, tipo = tipo, cantidad = nuevaCantidad, fecha = System.currentTimeMillis(), notas = notas))
    }

    suspend fun eliminarInsumo(insumo: Insumo) = insumoDao.delete(insumo.toEntity())

    fun getMovimientos(insumoId: Long): Flow<List<MovimientoInventario>> =
        movimientoDao.getMovimientosByInsumo(insumoId).map { list -> list.map { it.toDomain() } }
}

// ─── Alerta Repository ────────────────────────────────────────────────────────
@Singleton
class AlertaRepository @Inject constructor(private val alertaDao: AlertaDao) {
    fun getAlertas(agricultorId: Long): Flow<List<Alerta>> =
        alertaDao.getAlertasByAgricultor(agricultorId).map { list -> list.map { it.toDomain() } }

    fun countAlertasNoLeidas(agricultorId: Long): Flow<Int> =
        alertaDao.countAlertasNoLeidas(agricultorId)

    suspend fun crearAlerta(alerta: Alerta) = alertaDao.insert(alerta.toEntity())

    suspend fun marcarLeida(alertaId: Long) = alertaDao.marcarLeida(alertaId)

    suspend fun marcarTodasLeidas(agricultorId: Long) = alertaDao.marcarTodasLeidas(agricultorId)
}
