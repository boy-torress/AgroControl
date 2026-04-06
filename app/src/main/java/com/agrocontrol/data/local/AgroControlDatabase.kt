package com.agrocontrol.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.agrocontrol.data.local.dao.*
import com.agrocontrol.data.local.entities.*

@Database(
    entities = [
        UserEntity::class,
        CultivoEntity::class,
        HistorialEtapaEntity::class,
        InsumoEntity::class,
        MovimientoEntity::class,
        AlertaEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AgroControlDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun cultivoDao(): CultivoDao
    abstract fun historialEtapaDao(): HistorialEtapaDao
    abstract fun insumoDao(): InsumoDao
    abstract fun movimientoDao(): MovimientoDao
    abstract fun alertaDao(): AlertaDao
}
