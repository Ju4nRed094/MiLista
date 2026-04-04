package com.example.milista.ui.utils

import androidx.compose.ui.graphics.Path
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

@Serializable
data class PointData(val x: Float, val y: Float, val type: Int) // 0 for moveTo, 1 for lineTo

@Serializable
data class DrawPathData(val points: List<PointData>, val color: Int)

object DrawingUtils {
    private val json = Json { ignoreUnknownKeys = true }

    fun serializePaths(paths: List<Pair<Path, Int>>, pathDataList: List<List<PointData>>): String? {
        if (pathDataList.isEmpty()) return null
        val serializableData = pathDataList.mapIndexed { index, points ->
            DrawPathData(points, paths[index].second)
        }
        return json.encodeToString(serializableData)
    }

    fun deserializePaths(data: String?): List<Pair<Path, Int>> {
        if (data.isNullOrBlank()) return emptyList()
        return try {
            val decoded = json.decodeFromString<List<DrawPathData>>(data)
            decoded.map { pathData ->
                val path = Path()
                pathData.points.forEach { point ->
                    if (point.type == 0) path.moveTo(point.x, point.y)
                    else path.lineTo(point.x, point.y)
                }
                path to pathData.color
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // Función auxiliar para obtener los puntos serializables mientras se dibuja
    fun deserializeToPointData(data: String?): List<List<PointData>> {
        if (data.isNullOrBlank()) return emptyList()
        return try {
            val decoded = json.decodeFromString<List<DrawPathData>>(data)
            decoded.map { it.points }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
