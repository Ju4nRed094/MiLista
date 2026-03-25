package com.example.milista.data

import kotlinx.coroutines.flow.Flow

class MiListaRepository(
    private val tareaDao: TareaDao,
    private val listaDao: ListaDao,
    private val recordatorioDao: RecordatorioDao
) {
    fun obtenerTodasLasListas(): Flow<List<Lista>> = listaDao.obtenerTodas()
    fun obtenerTodosLosRecordatorios(): Flow<List<Recordatorio>> = recordatorioDao.obtenerTodos()
    fun obtenerTareasPorLista(listaId: Int): Flow<List<Tarea>> = tareaDao.obtenerPorLista(listaId)

    suspend fun insertarLista(lista: Lista) = listaDao.insertar(lista)
    suspend fun borrarLista(lista: Lista) = listaDao.borrar(lista)

    suspend fun insertarRecordatorio(recordatorio: Recordatorio) = recordatorioDao.insertar(recordatorio)
    suspend fun actualizarRecordatorio(recordatorio: Recordatorio) = recordatorioDao.actualizar(recordatorio)
    suspend fun borrarRecordatorio(recordatorio: Recordatorio) = recordatorioDao.borrar(recordatorio)

    suspend fun insertarTarea(tarea: Tarea) = tareaDao.insertar(tarea)
    suspend fun actualizarTarea(tarea: Tarea) = tareaDao.actualizar(tarea)
    suspend fun borrarTarea(tarea: Tarea) = tareaDao.borrar(tarea)
}
