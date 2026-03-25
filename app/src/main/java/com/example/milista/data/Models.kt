package com.example.milista.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "listas")
data class Lista(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val icono: String = "List" // Nombre del icono o recurso
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
    val colorCustom: Int? = null // Almacenado como ARGB Int
)

@Entity(tableName = "tareas")
data class Tarea(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val listaId: Int,
    val titulo: String,
    val estaCompletada: Boolean = false
)
