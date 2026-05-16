package com.example.milista.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.milista.data.ItemType
import com.example.milista.data.UnifiedItem
import com.example.milista.ui.theme.*
import com.example.milista.ui.utils.getTranslatedText
import com.example.milista.ui.utils.getLocaleCode
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: MiListaViewModel = viewModel(),
    onBack: () -> Unit
) {
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val unifiedItems by viewModel.unifiedItems.collectAsState()
    
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedTab by remember { mutableStateOf("Mes") }

    Scaffold(
        containerColor = AmoledBlack,
        bottomBar = {
            // BottomNavigationBar handled in MainActivity
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Nuevo evento */ },
                containerColor = NeonGreen,
                contentColor = AmoledBlack,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 100.dp)
                    .size(60.dp)
                    .shadow(12.dp, CircleShape, spotColor = NeonGreen)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear evento", modifier = Modifier.size(32.dp))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(20.dp)
        ) {
            // Header
            item {
                CalendarHeader(
                    selectedDate = selectedDate,
                    onBack = onBack,
                    selectedLanguage = selectedLanguage
                )
            }

            // Tabs Mes/Semana/Día/Agenda
            item {
                CalendarViewTabs(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    language = selectedLanguage
                )
            }

            // Calendario Mensual
            item {
                CalendarGridCard(
                    selectedDate = selectedDate,
                    onDateSelected = { selectedDate = it },
                    unifiedItems = unifiedItems
                )
            }

            // Header Agenda del día
            item {
                val dateFormat = SimpleDateFormat("EEEE, d 'de' MMMM", Locale(getLocaleCode(selectedLanguage)))
                Text(
                    text = dateFormat.format(selectedDate.time).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Eventos del día
            val dayItems = unifiedItems.filter { item ->
                val cal = Calendar.getInstance().apply { timeInMillis = item.timestamp }
                cal.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == selectedDate.get(Calendar.DAY_OF_YEAR)
            }

            if (dayItems.isEmpty()) {
                item {
                    Text(
                        text = "Sin eventos para hoy",
                        color = GrayText.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                items(dayItems) { item ->
                    EventAgendaCard(item)
                }
            }
        }
    }
}

@Composable
fun CalendarHeader(
    selectedDate: Calendar,
    onBack: () -> Unit,
    selectedLanguage: String
) {
    val monthName = SimpleDateFormat("MMMM", Locale(getLocaleCode(selectedLanguage)))
        .format(selectedDate.time).replaceFirstChar { it.uppercase() }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            HeaderCircleButtonSmall(Icons.AutoMirrored.Filled.ArrowBack, onClick = onBack)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "$monthName ✨",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                )
                Text(
                    text = selectedDate.get(Calendar.YEAR).toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = NeonGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Row {
            HeaderCircleButtonSmall(Icons.Default.Search)
            Spacer(modifier = Modifier.width(10.dp))
            HeaderCircleButtonSmall(Icons.Default.Tune)
            Spacer(modifier = Modifier.width(10.dp))
            HeaderCircleButtonSmall(Icons.Default.MoreVert)
        }
    }
}

@Composable
fun HeaderCircleButtonSmall(icon: ImageVector, onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, BorderGlow, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun CalendarViewTabs(selectedTab: String, onTabSelected: (String) -> Unit, language: String) {
    val tabs = listOf("Mes", "Semana", "Día", "Agenda")
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = CardDark.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, BorderGlow)
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            tabs.forEach { tab ->
                val isSelected = selectedTab == tab
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (isSelected) NeonGreen.copy(alpha = 0.1f) else Color.Transparent)
                        .clickable { onTabSelected(tab) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = getTranslatedText(tab, language),
                        color = if (isSelected) NeonGreen else GrayText,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.drawBehind {
                            if (isSelected) {
                                drawCircle(
                                    color = NeonGreen.copy(alpha = 0.2f),
                                    radius = size.maxDimension,
                                    center = center
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarGridCard(
    selectedDate: Calendar,
    onDateSelected: (Calendar) -> Unit,
    unifiedItems: List<UnifiedItem>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(28.dp), spotColor = Color.Black),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = BorderStroke(1.dp, BorderGlow)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Dias de la semana
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val weekDays = listOf("LUN", "MAR", "MIÉ", "JUE", "VIE", "SÁB", "DOM")
                weekDays.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 10.sp,
                        color = GrayText.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Grid de días (Simulado para el mes actual de la selectedDate)
            val cal = selectedDate.clone() as Calendar
            cal.set(Calendar.DAY_OF_MONTH, 1)
            val firstDayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Ajuste a Lunes inicio
            val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            
            val totalCells = 42
            val days = mutableListOf<Calendar?>()
            for (i in 0 until totalCells) {
                if (i < firstDayOfWeek || i >= firstDayOfWeek + daysInMonth) {
                    days.add(null)
                } else {
                    val dayCal = cal.clone() as Calendar
                    dayCal.set(Calendar.DAY_OF_MONTH, i - firstDayOfWeek + 1)
                    days.add(dayCal)
                }
            }

            Column {
                for (week in 0 until 6) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (dayIndex in 0 until 7) {
                            val currentDay = days[week * 7 + dayIndex]
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                if (currentDay != null) {
                                    val isSelected = currentDay.get(Calendar.DAY_OF_YEAR) == selectedDate.get(Calendar.DAY_OF_YEAR) &&
                                                     currentDay.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR)
                                    val isToday = currentDay.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR) &&
                                                  currentDay.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR)
                                    
                                    val hasEvents = unifiedItems.any { item ->
                                        val itemCal = Calendar.getInstance().apply { timeInMillis = item.timestamp }
                                        itemCal.get(Calendar.DAY_OF_YEAR) == currentDay.get(Calendar.DAY_OF_YEAR) &&
                                        itemCal.get(Calendar.YEAR) == currentDay.get(Calendar.YEAR)
                                    }

                                    Surface(
                                        onClick = { onDateSelected(currentDay) },
                                        modifier = Modifier.size(36.dp),
                                        shape = CircleShape,
                                        color = if (isSelected) NeonGreen else Color.Transparent,
                                        border = BorderStroke(1.dp, if (isToday) NeonGreen else Color.Transparent)
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = currentDay.get(Calendar.DAY_OF_MONTH).toString(),
                                                fontSize = 14.sp,
                                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) AmoledBlack else if (isToday) NeonGreen else Color.White
                                            )
                                            if (hasEvents && !isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(4.dp)
                                                        .background(NeonGreen, CircleShape)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EventAgendaCard(item: UnifiedItem) {
    val color = when(item.type) {
        ItemType.EVENT -> NeonGreen
        ItemType.REMINDER -> Blue
        ItemType.TASK -> Orange
        ItemType.NOTE -> Purple
        else -> NeonGreen
    }
    
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeStr = timeFormat.format(Date(item.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark.copy(alpha = 0.6f)),
        border = BorderStroke(1.dp, BorderGlow)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .background(color, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.AccessTime, null, tint = GrayText, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = timeStr, fontSize = 12.sp, color = GrayText)
                    if (!item.location.isNullOrBlank()) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(Icons.Outlined.LocationOn, null, tint = GrayText, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = item.location!!, fontSize = 12.sp, color = GrayText)
                    }
                }
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = color.copy(alpha = 0.1f)
            ) {
                Text(
                    text = item.type.name.lowercase().replaceFirstChar { it.uppercase() },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 10.sp,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
