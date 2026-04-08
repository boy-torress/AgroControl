package com.agrocontrol.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.agrocontrol.data.local.AgroControlDatabase
import com.agrocontrol.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AgroControlDatabase =
        Room.databaseBuilder(context, AgroControlDatabase::class.java, "agrocontrol.db")
            .fallbackToDestructiveMigration()
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    super.onCreate(db)
                    val adminHash = "admin123".hashCode().toString()
                    val agroHash = "agro123".hashCode().toString()

                    // Insertar Administrador por defecto
                    db.execSQL("""
                        INSERT INTO users (nombre, correo, passwordHash, rol) 
                        VALUES ('Super Admin', 'admin@agrocontrol.com', '${adminHash}', 'ADMINISTRADOR')
                    """)
                    // Insertar Agrónomo por defecto
                    db.execSQL("""
                        INSERT INTO users (nombre, correo, passwordHash, rol) 
                        VALUES ('Ing. Agrónomo Central', 'agronomo@agrocontrol.com', '${agroHash}', 'AGRONOMO')
                    """)
                }
            })
            .build()

    @Provides fun provideUserDao(db: AgroControlDatabase): UserDao = db.userDao()
    @Provides fun provideCultivoDao(db: AgroControlDatabase): CultivoDao = db.cultivoDao()
    @Provides fun provideHistorialDao(db: AgroControlDatabase): HistorialEtapaDao = db.historialEtapaDao()
    @Provides fun provideInsumoDao(db: AgroControlDatabase): InsumoDao = db.insumoDao()
    @Provides fun provideMovimientoDao(db: AgroControlDatabase): MovimientoDao = db.movimientoDao()
    @Provides fun provideAlertaDao(db: AgroControlDatabase): AlertaDao = db.alertaDao()
    @Provides fun provideChatDao(db: AgroControlDatabase): ChatDao = db.chatDao()
}
