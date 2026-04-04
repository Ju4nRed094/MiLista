package com.example.milista.data

import kotlinx.coroutines.flow.Flow

class MiListaRepository(
    private val tareaDao: TareaDao,
    private val listaDao: ListaDao,
    private val recordatorioDao: RecordatorioDao,
    private val alarmaDao: AlarmaDao,
    private val unifiedItemDao: UnifiedItemDao
) {
    // Unified Items
    fun getAllUnifiedItems(): Flow<List<UnifiedItem>> = unifiedItemDao.getAll()
    fun getUnifiedItemsByType(type: ItemType): Flow<List<UnifiedItem>> = unifiedItemDao.getByType(type)
    suspend fun insertUnifiedItem(item: UnifiedItem): Long = unifiedItemDao.insert(item)
    suspend fun updateUnifiedItem(item: UnifiedItem) = unifiedItemDao.update(item)
    suspend fun deleteUnifiedItem(item: UnifiedItem) = unifiedItemDao.delete(item)
    suspend fun getUnifiedItemById(id: Int) = unifiedItemDao.getById(id)

    fun obtenerTodasLasListas(): Flow<List<Lista>> = listaDao.obtenerTodas()
    fun obtenerTodosLosRecordatorios(): Flow<List<Recordatorio>> = recordatorioDao.obtenerTodos()
    fun obtenerTareasPorLista(listaId: Int): Flow<List<Tarea>> = tareaDao.obtenerPorLista(listaId)
    fun obtenerTodasLasAlarmas(): Flow<List<Alarma>> = alarmaDao.obtenerTodas()

    suspend fun insertarLista(lista: Lista): Long = listaDao.insertar(lista)
    suspend fun actualizarLista(lista: Lista) = listaDao.actualizar(lista)
    suspend fun obtenerListaPorNombre(nombre: String): Lista? = listaDao.obtenerPorNombre(nombre)
    suspend fun borrarLista(lista: Lista) = listaDao.borrar(lista)

    suspend fun insertarRecordatorio(recordatorio: Recordatorio) = recordatorioDao.insertar(recordatorio)
    suspend fun actualizarRecordatorio(recordatorio: Recordatorio) = recordatorioDao.actualizar(recordatorio)
    suspend fun borrarRecordatorio(recordatorio: Recordatorio) = recordatorioDao.borrar(recordatorio)

    suspend fun insertarTarea(tarea: Tarea) = tareaDao.insertar(tarea)
    suspend fun actualizarTarea(tarea: Tarea) = tareaDao.actualizar(tarea)
    suspend fun borrarTarea(tarea: Tarea) = tareaDao.borrar(tarea)

    suspend fun insertarAlarma(alarma: Alarma): Long = alarmaDao.insertar(alarma)
    suspend fun actualizarAlarma(alarma: Alarma) = alarmaDao.actualizar(alarma)
    suspend fun borrarAlarma(alarma: Alarma) = alarmaDao.borrar(alarma)
}
