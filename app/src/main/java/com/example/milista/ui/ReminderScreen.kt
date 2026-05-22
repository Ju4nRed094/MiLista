package com.example.milista.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.milista.R
import com.example.milista.data.Recordatorio
import com.example.milista.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreen(
    viewModel: MiListaViewModel = viewModel(),
    onBack: () -> Unit,
    onNavigateToAdd: (String) -> Unit
) {
    val recordatorios by viewModel.recordatorios.collectAsState()
    var selectedFilterId by remember { mutableIntStateOf(R.string.all_filter) }
    
    val pendingCount = remember(recordatorios) { recordatorios.count { !it.isCompleted } }

    Box(modifier = Modifier.fillMaxSize().background(AmoledBlack)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        Brush.radialGradient(
                            colors = listOf(NeonGreen.copy(alpha = 0.03f), Color.Transparent),
                            center = Offset(size.width * 0.9f, size.height * 0.1f),
                            radius = size.width * 1.5f
                        )
                    )
                }
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                ReminderTopBarPremium(pendingCount)
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Botón Agregar Elegante
                item {
                    NewReminderButtonPremium { onNavigateToAdd("Otro") }
                }

                // Filtros Chips
                item {
                    ReminderFilterChipsPremium(selectedFilterId) { selectedFilterId = it }
                }

                // Lista de Recordatorios con Orden Inteligente
                val now = System.currentTimeMillis()
                val sortedList = recordatorios.filter { reminder ->
                    when (selectedFilterId) {
                        R.string.today_filter -> isSameDayLocal(reminder.fecha, now)
                        R.string.overdue_filter -> reminder.fecha < now && !reminder.isCompleted
                        R.string.completed_filter -> reminder.isCompleted
                        else -> true // Todos
                    }
                }.sortedWith(
                    compareByDescending<Recordatorio> { it.prioridad }
                        .thenBy { it.isCompleted }
                        .thenBy { it.fecha }
                )

                if (sortedList.isEmpty()) {
                    item { EmptyReminderStatePremium() }
                } else {
                    items(sortedList, key = { it.id }) { reminder ->
                        ReminderCardAdvanced(
                            reminder = reminder,
                            onToggle = { viewModel.toggleRecordatorio(reminder) },
                            onDelete = { viewModel.borrarRecordatorio(reminder) },
                            onClick = { onNavigateToAdd("${reminder.tipo}?reminderId=${reminder.id}") }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }
}

@Composable
fun ReminderTopBarPremium(pendingCount: Int) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
        Text(
            text = stringResource(R.string.reminders),
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
            color = Color.White
        )
        Text(
            text = "$pendingCount pendientes hoy ✨",
            style = MaterialTheme.typography.bodyMedium,
            color = NeonGreen,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun NewReminderButtonPremium(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = NeonGreen.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Rounded.Add, null, tint = NeonGreen)
            Spacer(modifier = Modifier.width(12.dp))
            Text("Nuevo recordatorio", color = NeonGreen, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ReminderFilterChipsPremium(selectedId: Int, onSelect: (Int) -> Unit) {
    val filters = listOf(
        R.string.all_filter,
        R.string.today_filter,
        R.string.overdue_filter,
        R.string.completed_filter
    )
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(filters) { filterId ->
            val isSelected = selectedId == filterId
            Surface(
                onClick = { onSelect(filterId) },
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) NeonGreen else Color.White.copy(alpha = 0.05f),
                border = BorderStroke(1.dp, if (isSelected) NeonGreen else Color.White.copy(alpha = 0.1f))
            ) {
                Text(
                    text = stringResource(filterId),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = if (isSelected) AmoledBlack else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun ReminderCardAdvanced(
    reminder: Recordatorio,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val isOverdue = reminder.fecha < System.currentTimeMillis() && !reminder.isCompleted
    val priorityColor = when(reminder.prioridad) {
        2 -> SamsungRed
        1 -> Color(0xFFFFD600)
        else -> NeonGreen
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().alpha(if (reminder.isCompleted) 0.5f else 1f),
        shape = RoundedCornerShape(28.dp),
        color = CardDark,
        border = BorderStroke(1.dp, if (reminder.isCompleted) Color.White.copy(alpha = 0.05f) else BorderGlow)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox Custom
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (reminder.isCompleted) NeonGreen else Color.Transparent)
                    .border(2.dp, if (reminder.isCompleted) NeonGreen else GrayText.copy(alpha = 0.4f), CircleShape)
                    .clickable { onToggle() },
                contentAlignment = Alignment.Center
            ) {
                if (reminder.isCompleted) Icon(Icons.Default.Check, null, tint = AmoledBlack, modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.nombreCustom ?: reminder.tipo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White,
                    textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else null
                )
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(
                        if (isOverdue) Icons.Rounded.PriorityHigh else Icons.Rounded.Schedule, 
                        null, 
                        tint = if (isOverdue) SamsungRed else GrayText, 
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = SimpleDateFormat("d MMM, HH:mm", Locale.getDefault()).format(Date(reminder.fecha)),
                        fontSize = 12.sp,
                        color = if (isOverdue) SamsungRed else GrayText
                    )
                    if (reminder.prioridad > 0) {
                        Text(" • ", color = GrayText)
                        Box(modifier = Modifier.size(8.dp).background(priorityColor, CircleShape).shadow(4.dp, CircleShape, spotColor = priorityColor))
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, null, tint = GrayText.copy(alpha = 0.3f), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun EmptyReminderStatePremium() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Rounded.NotificationsNone, null, tint = Color.White.copy(alpha = 0.05f), modifier = Modifier.size(100.dp))
        Spacer(modifier = Modifier.height(20.dp))
        Text("No hay recordatorios", color = GrayText.copy(alpha = 0.4f), fontSize = 16.sp)
    }
}

fun isSameDayLocal(t1: Long, t2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = t1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = t2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
