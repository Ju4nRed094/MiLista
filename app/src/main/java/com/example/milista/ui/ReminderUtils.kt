package com.example.milista.ui

import androidx.compose.ui.graphics.Color

data class ReminderType(val name: String, val emoji: String, val color: Color)

object ReminderConstants {
    val types = listOf(
        ReminderType("Cumpleaños", "🎂", Color(0xFFF472B6)),
        ReminderType("Diplomado", "🎓", Color(0xFF818CF8)),
        ReminderType("Entrevista", "💼", Color(0xFFFBBF24)),
        ReminderType("Doctor", "🏥", Color(0xFFFB7185)),
        ReminderType("Gimnasio", "💪", Color(0xFF2DD4BF)),
        ReminderType("Examen", "📝", Color(0xFF60A5FA)),
        ReminderType("Viaje", "✈️", Color(0xFF34D399)),
        ReminderType("Pago", "💰", Color(0xFFFACC15)),
        ReminderType("Cena", "🍽️", Color(0xFFF87171)),
        ReminderType("Reunión", "🤝", Color(0xFFA78BFA)),
        ReminderType("Boda", "💍", Color(0xFFEC4899)),
        ReminderType("Concierto", "🎸", Color(0xFF8B5CF6)),
        ReminderType("Deporte", "⚽", Color(0xFFFB923C)),
        ReminderType("Limpieza", "🧹", Color(0xFF94A3B8)),
        ReminderType("Medicamento", "💊", Color(0xFFF43F5E)),
        ReminderType("Cine", "🎬", Color(0xFF3B82F6)),
        ReminderType("Compra", "🛒", Color(0xFF10B981)),
        ReminderType("Cita", "📅", Color(0xFF6366F1)),
        ReminderType("Otro", "✨", Color(0xFF38BDF8))
    )

    fun getByType(name: String): ReminderType {
        return types.find { it.name == name } ?: types.last()
    }
}
