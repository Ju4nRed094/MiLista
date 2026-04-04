package com.example.milista.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ItemType {
    NOTE,
    TASK,
    REMINDER,
    EVENT,
    ALARM,
    LIST
}

@Entity(tableName = "unified_items")
data class UnifiedItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: ItemType,
    val title: String,
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val isCompleted: Boolean = false,
    val isActive: Boolean = true,
    val priority: Int = 0, // 0: None, 1: Low, 2: Medium, 3: High
    val color: Int? = null,
    val icon: String? = null,
    val categoryId: Int? = null,
    val recurrence: String? = null,
    val location: String? = null,
    val imagePath: String? = null,
    val audioPath: String? = null,
    val drawingData: String? = null,
    val isFavorite: Boolean = false,
    val reminderTonoUri: String? = null
)

@Entity(tableName = "listas")
data class Lista(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val icono: String = "List",
    val esCompletada: Boolean = false,
    val totalEstimado: Double = 0.0,
    val fechaFinalizacion: Long? = null
)

@Entity(tableName = "recordatorios")
data class Recordatorio(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tipo: String,    // Cumpleaños, Doctor, etc. o "Otro"
    val fecha: Long,     // Timestamp
    val estado: Boolean = true,
    // Campos para personalización si el tipo es "Otro"
    val nombreCustom: String? = null,
    val emojiCustom: String? = null,
    val colorCustom: Int? = null, // Almacenado como ARGB Int
    val tonoUri: String? = null    // URI del tono de notificación/alarma
)

@Entity(tableName = "tareas")
data class Tarea(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val listaId: Int,
    val titulo: String,
    val contenido: String = "",
    val estaCompletada: Boolean = false,
    val color: Int? = null,
    val fechaCreacion: Long = System.currentTimeMillis(),
    val esFavorita: Boolean = false,
    val imagenPath: String? = null,
    val audioPath: String? = null,
    val esNotaEnriquecida: Boolean = true,
    val dibujoData: String? = null,
    // Nuevos campos para Listas Inteligentes (Compras)
    val cantidad: Double = 1.0,
    val unidad: String = "ud",
    val precio: Double = 0.0,
    val categoriaProducto: String = "Otros"
)

@Entity(tableName = "alarmas")
data class Alarma(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hora: Int,
    val minuto: Int,
    val dias: String, // Guardado como "Lun,Mar,..."
    val etiqueta: String = "",
    val activa: Boolean = true,
    val tonoUri: String? = null,
    val vibrar: Boolean = true
)
