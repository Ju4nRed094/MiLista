package com.example.milista.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ListaDao {
    @Query("SELECT * FROM listas")
    fun obtenerTodas(): Flow<List<Lista>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(lista: Lista)

    @Delete
    suspend fun borrar(lista: Lista)
}

@Dao
interface RecordatorioDao {
    @Query("SELECT * FROM recordatorios ORDER BY fecha ASC")
    fun obtenerTodos(): Flow<List<Recordatorio>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(recordatorio: Recordatorio)

    @Update
    suspend fun actualizar(recordatorio: Recordatorio)

    @Delete
    suspend fun borrar(recordatorio: Recordatorio)
}

@Dao
interface TareaDao {
    @Query("SELECT * FROM tareas WHERE listaId = :listaId")
    fun obtenerPorLista(listaId: Int): Flow<List<Tarea>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(tarea: Tarea)

    @Update
    suspend fun actualizar(tarea: Tarea)

    @Delete
    suspend fun borrar(tarea: Tarea)
}
