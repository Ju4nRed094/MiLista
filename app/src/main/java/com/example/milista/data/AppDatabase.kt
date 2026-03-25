package com.example.milista.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Tarea::class, Lista::class, Recordatorio::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tareaDao(): TareaDao
    abstract fun listaDao(): ListaDao
    abstract fun recordatorioDao(): RecordatorioDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "milista_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
