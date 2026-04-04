package com.example.milista.ui

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.milista.data.*
import com.example.milista.receiver.AlarmReceiver
import com.example.milista.ui.theme.*
import com.example.milista.ui.utils.SettingsDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class MiListaViewModel(application: Application) : AndroidViewModel(application) {
    val repository: MiListaRepository
    private val settingsDataStore = SettingsDataStore(application)
    val listas: StateFlow<List<Lista>>
    val recordatorios: StateFlow<List<Recordatorio>>
    val alarmas: StateFlow<List<Alarma>>
    val unifiedItems: StateFlow<List<UnifiedItem>>

    private val _quickNotesId = MutableStateFlow<Int?>(null)
    val quickNotesId = _quickNotesId.asStateFlow()

    private val _selectedFont = MutableStateFlow<com.example.milista.ui.theme.AppFont>(com.example.milista.ui.theme.AppFonts[0])
    val selectedFont: StateFlow<com.example.milista.ui.theme.AppFont> = _selectedFont.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("Español (español)")
    val selectedLanguage = _selectedLanguage.asStateFlow()

    private val _selectedTheme = MutableStateFlow("Oscuro")
    val selectedTheme = _selectedTheme.asStateFlow()

    private val _selectedFontSize = MutableStateFlow(16f)
    val selectedFontSize = _selectedFontSize.asStateFlow()

    private val _isApplyingChanges = MutableStateFlow(false)
    val isApplyingChanges = _isApplyingChanges.asStateFlow()

    fun updateFont(fontName: String) {
        viewModelScope.launch {
            _isApplyingChanges.value = true
            settingsDataStore.saveFont(fontName)
            kotlinx.coroutines.delay(1000)
            _isApplyingChanges.value = false
        }
    }

    fun updateLanguage(language: String) {
        viewModelScope.launch {
            _isApplyingChanges.value = true
            settingsDataStore.saveLanguage(language)
            kotlinx.coroutines.delay(1000)
            _isApplyingChanges.value = false
        }
    }

    fun updateTheme(theme: String) {
        viewModelScope.launch {
            _isApplyingChanges.value = true
            settingsDataStore.saveTheme(theme)
            kotlinx.coroutines.delay(1000)
            _isApplyingChanges.value = false
        }
    }

    fun updateFontSize(size: Float) {
        viewModelScope.launch {
            settingsDataStore.saveFontSize(size)
        }
    }

    init {
        val database = AppDatabase.getDatabase(application)
        repository = MiListaRepository(
            database.tareaDao(), 
            database.listaDao(), 
            database.recordatorioDao(), 
            database.alarmaDao(),
            database.unifiedItemDao()
        )
        
        viewModelScope.launch {
            settingsDataStore.selectedLanguage.collectLatest { language ->
                _selectedLanguage.value = language
            }
        }

        viewModelScope.launch {
            settingsDataStore.selectedTheme.collectLatest { theme ->
                _selectedTheme.value = theme
            }
        }
        
        viewModelScope.launch {
            settingsDataStore.selectedFont.collectLatest { fontName ->
                val font = com.example.milista.ui.theme.AppFonts.find { it.name == fontName } ?: com.example.milista.ui.theme.AppFonts[0]
                _selectedFont.value = font
            }
        }

        viewModelScope.launch {
            settingsDataStore.selectedFontSize.collectLatest { size ->
                _selectedFontSize.value = size
            }
        }
        viewModelScope.launch {
            _quickNotesId.value = obtenerIdListaRapida()
        }
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
        alarmas = repository.obtenerTodasLasAlarmas().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        unifiedItems = repository.getAllUnifiedItems().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }

    fun addUnifiedItem(item: UnifiedItem) {
        viewModelScope.launch {
            repository.insertUnifiedItem(item)
        }
    }

    fun updateUnifiedItem(item: UnifiedItem) {
        viewModelScope.launch {
            repository.updateUnifiedItem(item)
        }
    }

    fun deleteUnifiedItem(item: UnifiedItem) {
        viewModelScope.launch {
            repository.deleteUnifiedItem(item)
        }
    }

    fun agregarLista(nombre: String, onResult: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            val id = repository.insertarLista(Lista(nombre = nombre))
            onResult?.invoke(id)
        }
    }

    suspend fun obtenerIdListaRapida(): Int {
        val nombre = "Notas Rápidas"
        val existente = repository.obtenerListaPorNombre(nombre)
        return existente?.id ?: repository.insertarLista(Lista(nombre = nombre)).toInt()
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
            addUnifiedItem(UnifiedItem(
                type = ItemType.REMINDER,
                title = nombreCustom ?: tipo,
                timestamp = fecha,
                color = colorCustom,
                icon = emojiCustom
            ))
        }
    }

    fun borrarRecordatorio(recordatorio: Recordatorio) {
        viewModelScope.launch {
            repository.borrarRecordatorio(recordatorio)
        }
    }

    fun agregarTarea(titulo: String, listaId: Int, contenido: String = "", color: Int? = null, dibujoData: String? = null, imagenPath: String? = null, audioPath: String? = null) {
        viewModelScope.launch {
            repository.insertarTarea(Tarea(
                titulo = titulo, 
                listaId = listaId, 
                contenido = contenido, 
                color = color, 
                dibujoData = dibujoData,
                imagenPath = imagenPath,
                audioPath = audioPath
            ))
            addUnifiedItem(UnifiedItem(
                type = ItemType.TASK,
                title = titulo,
                content = contenido,
                color = color,
                drawingData = dibujoData,
                imagePath = imagenPath,
                audioPath = audioPath,
                categoryId = listaId
            ))
        }
    }

    fun actualizarTarea(tarea: Tarea) {
        viewModelScope.launch {
            repository.actualizarTarea(tarea)
        }
    }

    fun actualizarCantidadTarea(tarea: Tarea, nuevaCantidad: Double) {
        viewModelScope.launch {
            repository.actualizarTarea(tarea.copy(cantidad = nuevaCantidad))
        }
    }

    fun finalizarCompra(lista: Lista, total: Double) {
        viewModelScope.launch {
            repository.actualizarLista(lista.copy(
                esCompletada = true,
                totalEstimado = total,
                fechaFinalizacion = System.currentTimeMillis()
            ))
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

    fun agregarAlarma(hora: Int, minuto: Int, dias: String, etiqueta: String, tonoUri: String? = null) {
        viewModelScope.launch {
            val id = repository.insertarAlarma(Alarma(hora = hora, minuto = minuto, dias = dias, etiqueta = etiqueta, tonoUri = tonoUri))
            val nuevaAlarma = Alarma(id = id.toInt(), hora = hora, minuto = minuto, dias = dias, etiqueta = etiqueta, tonoUri = tonoUri, activa = true)
            programarAlarma(nuevaAlarma)
            
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hora)
                set(Calendar.MINUTE, minuto)
                set(Calendar.SECOND, 0)
            }
            addUnifiedItem(UnifiedItem(
                type = ItemType.ALARM,
                title = etiqueta.ifEmpty { "Alarma" },
                timestamp = cal.timeInMillis,
                recurrence = dias,
                reminderTonoUri = tonoUri
            ))
        }
    }

    fun borrarAlarma(alarma: Alarma) {
        viewModelScope.launch {
            cancelarAlarma(alarma)
            repository.borrarAlarma(alarma)
        }
    }

    fun actualizarAlarma(alarma: Alarma) {
        viewModelScope.launch {
            repository.actualizarAlarma(alarma)
            if (alarma.activa) {
                programarAlarma(alarma)
            } else {
                cancelarAlarma(alarma)
            }
        }
    }

    private fun programarAlarma(alarma: Alarma) {
        val alarmManager = getApplication<Application>().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                getApplication<Application>().startActivity(intent)
                return
            }
        }

        val intent = Intent(getApplication(), AlarmReceiver::class.java).apply {
            putExtra("alarm_id", alarma.id)
            putExtra("title", alarma.etiqueta)
            putExtra("tono_uri", alarma.tonoUri)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            getApplication(),
            alarma.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarma.hora)
            set(Calendar.MINUTE, alarma.minuto)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DATE, 1)
            }
        }

        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun cancelarAlarma(alarma: Alarma) {
        val alarmManager = getApplication<Application>().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(getApplication(), AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            getApplication(),
            alarma.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
