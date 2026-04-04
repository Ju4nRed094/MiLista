package com.example.milista.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UnifiedItemDao {
    @Query("SELECT * FROM unified_items ORDER BY timestamp ASC")
    fun getAll(): Flow<List<UnifiedItem>>

    @Query("SELECT * FROM unified_items WHERE type = :type ORDER BY timestamp DESC")
    fun getByType(type: ItemType): Flow<List<UnifiedItem>>

    @Query("SELECT * FROM unified_items WHERE categoryId = :categoryId")
    fun getByCategory(categoryId: Int): Flow<List<UnifiedItem>>

    @Query("SELECT * FROM unified_items WHERE isFavorite = 1")
    fun getFavorites(): Flow<List<UnifiedItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: UnifiedItem): Long

    @Update
    suspend fun update(item: UnifiedItem)

    @Delete
    suspend fun delete(item: UnifiedItem)

    @Query("SELECT * FROM unified_items WHERE id = :id")
    suspend fun getById(id: Int): UnifiedItem?
}

@Dao
interface ListaDao {
    @Query("SELECT * FROM listas")
    fun obtenerTodas(): Flow<List<Lista>>

    @Query("SELECT * FROM listas WHERE nombre = :nombre LIMIT 1")
    suspend fun obtenerPorNombre(nombre: String): Lista?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(lista: Lista): Long

    @Update
    suspend fun actualizar(lista: Lista)

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
    @Query("SELECT * FROM tareas WHERE listaId = :listaId ORDER BY fechaCreacion DESC")
    fun obtenerPorLista(listaId: Int): Flow<List<Tarea>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(tarea: Tarea)

    @Update
    suspend fun actualizar(tarea: Tarea)

    @Delete
    suspend fun borrar(tarea: Tarea)
}

@Dao
interface AlarmaDao {
    @Query("SELECT * FROM alarmas ORDER BY hora ASC, minuto ASC")
    fun obtenerTodas(): Flow<List<Alarma>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(alarma: Alarma): Long

    @Update
    suspend fun actualizar(alarma: Alarma)

    @Delete
    suspend fun borrar(alarma: Alarma)
}
