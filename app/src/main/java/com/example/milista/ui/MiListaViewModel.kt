package com.example.milista.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.milista.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MiListaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MiListaRepository
    val listas: StateFlow<List<Lista>>
    val recordatorios: StateFlow<List<Recordatorio>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = MiListaRepository(database.tareaDao(), database.listaDao(), database.recordatorioDao())
        listas = repository.obtenerTodasLasListas().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        recordatorios = repository.obtenerTodosLosRecordatorios().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }

    fun agregarLista(nombre: String) {
        viewModelScope.launch {
            repository.insertarLista(Lista(nombre = nombre))
        }
    }

    fun borrarLista(lista: Lista) {
        viewModelScope.launch {
            repository.borrarLista(lista)
        }
    }

    fun agregarRecordatorio(
        tipo: String, 
        fecha: Long,
        nombreCustom: String? = null,
        emojiCustom: String? = null,
        colorCustom: Int? = null
    ) {
        viewModelScope.launch {
            repository.insertarRecordatorio(
                Recordatorio(
                    tipo = tipo, 
                    fecha = fecha,
                    nombreCustom = nombreCustom,
                    emojiCustom = emojiCustom,
                    colorCustom = colorCustom
                )
            )
        }
    }

    fun borrarRecordatorio(recordatorio: Recordatorio) {
        viewModelScope.launch {
            repository.borrarRecordatorio(recordatorio)
        }
    }

    fun agregarTarea(titulo: String, listaId: Int) {
        viewModelScope.launch {
            repository.insertarTarea(Tarea(titulo = titulo, listaId = listaId))
        }
    }

    fun toggleTarea(tarea: Tarea) {
        viewModelScope.launch {
            repository.actualizarTarea(tarea.copy(estaCompletada = !tarea.estaCompletada))
        }
    }

    fun borrarTarea(tarea: Tarea) {
        viewModelScope.launch {
            repository.borrarTarea(tarea)
        }
    }

    fun obtenerTareasPorLista(listaId: Int): StateFlow<List<Tarea>> {
        return repository.obtenerTareasPorLista(listaId).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }
}
