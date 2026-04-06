package com.agrocontrol.data.local.dao

import androidx.room.*
import com.agrocontrol.data.local.entities.*
import kotlinx.coroutines.flow.Flow

// ─── UserDao ─────────────────────────────────────────────────────────────────
@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE correo = :correo LIMIT 1")
    suspend fun findByCorreo(correo: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE rol = 'AGRICULTOR' AND agronomoAsignadoId = :agronomoId")
    fun getAgricultoresByAgronomo(agronomoId: Long): Flow<List<UserEntity>>

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>
}

// ─── CultivoDao ──────────────────────────────────────────────────────────────
@Dao
interface CultivoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cultivo: CultivoEntity): Long

    @Update
    suspend fun update(cultivo: CultivoEntity)

    @Query("SELECT * FROM cultivos WHERE agricultorId = :agricultorId AND activo = 1 LIMIT 1")
    fun getCultivoActivo(agricultorId: Long): Flow<CultivoEntity?>

    @Query("SELECT * FROM cultivos WHERE agricultorId = :agricultorId")
    fun getCultivosByAgricultor(agricultorId: Long): Flow<List<CultivoEntity>>

    @Query("SELECT * FROM cultivos")
    fun getAllCultivos(): Flow<List<CultivoEntity>>

    @Query("UPDATE cultivos SET activo = 0 WHERE agricultorId = :agricultorId AND activo = 1")
    suspend fun desactivarCultivosActivos(agricultorId: Long)

    @Query("SELECT * FROM cultivos WHERE agricultorId = :agricultorId AND activo = 1 LIMIT 1")
    suspend fun getCultivoActivoOnce(agricultorId: Long): CultivoEntity?
}

// ─── HistorialEtapaDao ───────────────────────────────────────────────────────
@Dao
interface HistorialEtapaDao {
    @Insert
    suspend fun insert(historial: HistorialEtapaEntity)

    @Query("SELECT * FROM historial_etapas WHERE cultivoId = :cultivoId ORDER BY fechaCambio DESC")
    fun getHistorialByCultivo(cultivoId: Long): Flow<List<HistorialEtapaEntity>>
}

// ─── InsumoDao ───────────────────────────────────────────────────────────────
@Dao
interface InsumoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(insumo: InsumoEntity): Long

    @Update
    suspend fun update(insumo: InsumoEntity)

    @Delete
    suspend fun delete(insumo: InsumoEntity)

    @Query("SELECT * FROM insumos WHERE agricultorId = :agricultorId")
    fun getInsumosByAgricultor(agricultorId: Long): Flow<List<InsumoEntity>>

    @Query("SELECT * FROM insumos WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): InsumoEntity?

    @Query("SELECT COUNT(*) FROM insumos WHERE agricultorId = :agricultorId AND cantidadActual < cantidadMinima")
    fun countStockCritico(agricultorId: Long): Flow<Int>
}

// ─── MovimientoDao ───────────────────────────────────────────────────────────
@Dao
interface MovimientoDao {
    @Insert
    suspend fun insert(movimiento: MovimientoEntity)

    @Query("SELECT * FROM movimientos_inventario WHERE insumoId = :insumoId ORDER BY fecha DESC")
    fun getMovimientosByInsumo(insumoId: Long): Flow<List<MovimientoEntity>>
}

// ─── AlertaDao ───────────────────────────────────────────────────────────────
@Dao
interface AlertaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alerta: AlertaEntity): Long

    @Query("SELECT * FROM alertas WHERE agricultorId = :agricultorId ORDER BY fechaCreacion DESC")
    fun getAlertasByAgricultor(agricultorId: Long): Flow<List<AlertaEntity>>

    @Query("SELECT COUNT(*) FROM alertas WHERE agricultorId = :agricultorId AND leida = 0")
    fun countAlertasNoLeidas(agricultorId: Long): Flow<Int>

    @Query("UPDATE alertas SET leida = 1 WHERE id = :alertaId")
    suspend fun marcarLeida(alertaId: Long)

    @Query("UPDATE alertas SET leida = 1 WHERE agricultorId = :agricultorId")
    suspend fun marcarTodasLeidas(agricultorId: Long)
}
