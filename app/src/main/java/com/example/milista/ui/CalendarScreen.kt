package com.example.milista.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.milista.R
import com.example.milista.data.ItemType
import com.example.milista.data.UnifiedItem
import com.example.milista.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: MiListaViewModel = viewModel()
) {
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val recordatorios by viewModel.recordatorios.collectAsState()
    
    var currentMonthDate by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    // Usamos el ID del recurso para el estado de la pestaña para que sea dinámico
    var selectedTabId by remember { mutableIntStateOf(R.string.month) }

    val today = Calendar.getInstance()
    
    Scaffold(
        containerColor = AmoledBlack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 1. Header (Navegación de tiempo)
            CalendarHeaderPremium(
                currentMonthDate = currentMonthDate,
                selectedTabId = selectedTabId,
                onPrev = {
                    val newDate = currentMonthDate.clone() as Calendar
                    if (selectedTabId == R.string.month) newDate.add(Calendar.MONTH, -1)
                    else newDate.add(Calendar.DAY_OF_YEAR, -7)
                    currentMonthDate = newDate
                },
                onNext = {
                    val newDate = currentMonthDate.clone() as Calendar
                    if (selectedTabId == R.string.month) newDate.add(Calendar.MONTH, 1)
                    else newDate.add(Calendar.DAY_OF_YEAR, 7)
                    currentMonthDate = newDate
                },
                selectedLanguage = selectedLanguage
            )

            // 2. Tabs de vista Premium
            CalendarViewTabsPremium(
                selectedTabId = selectedTabId,
                onTabSelected = { selectedTabId = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Contenido Dinámico con Transición
            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = selectedTabId,
                    transitionSpec = {
                        fadeIn(tween(400)) + slideInHorizontally() togetherWith 
                        fadeOut(tween(400)) + slideOutHorizontally()
                    },
                    label = "calendarContentTransition"
                ) { targetTabId ->
                    when (targetTabId) {
                        R.string.month -> MonthViewContent(
                            currentMonthDate = currentMonthDate,
                            selectedDate = selectedDate,
                            today = today,
                            onDateSelected = { selectedDate = it },
                            recordatorios = recordatorios,
                            selectedLanguage = selectedLanguage
                        )
                        R.string.week -> WeekViewContent(
                            startDate = currentMonthDate,
                            selectedDate = selectedDate,
                            today = today,
                            onDateSelected = { selectedDate = it },
                            recordatorios = recordatorios,
                            selectedLanguage = selectedLanguage
                        )
                        R.string.day -> DayViewContent(
                            selectedDate = selectedDate,
                            recordatorios = recordatorios
                        )
                        R.string.agenda -> AgendaViewContent(
                            recordatorios = recordatorios,
                            today = today
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthViewContent(
    currentMonthDate: Calendar,
    selectedDate: Calendar,
    today: Calendar,
    onDateSelected: (Calendar) -> Unit,
    recordatorios: List<com.example.milista.data.Recordatorio>,
    selectedLanguage: String
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            CalendarGridCardPremium(
                currentMonthDate = currentMonthDate,
                selectedDate = selectedDate,
                today = today,
                onDateSelected = onDateSelected,
                recordatorios = recordatorios,
                selectedLanguage = selectedLanguage
            )
        }

        item {
            val dateFormat = SimpleDateFormat("EEEE, d MMMM", Locale(selectedLanguage))
            Text(
                text = dateFormat.format(selectedDate.time).replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = Color.White
            )
        }

        val dayItems = recordatorios.filter { item ->
            val cal = Calendar.getInstance().apply { timeInMillis = item.fecha }
            cal.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
            cal.get(Calendar.DAY_OF_YEAR) == selectedDate.get(Calendar.DAY_OF_YEAR)
        }

        if (dayItems.isEmpty()) {
            item {
                SmartEmptyState(message = if (isDateInPast(selectedDate, today)) stringResource(R.string.no_events_day) else stringResource(R.string.no_events))
            }
        } else {
            items(dayItems) { item ->
                EventAgendaCardPremium(item)
            }
        }
    }
}

@Composable
fun WeekViewContent(
    startDate: Calendar,
    selectedDate: Calendar,
    today: Calendar,
    onDateSelected: (Calendar) -> Unit,
    recordatorios: List<com.example.milista.data.Recordatorio>,
    selectedLanguage: String
) {
    val weekDays = remember(startDate) {
        val list = mutableListOf<Calendar>()
        val cal = startDate.clone() as Calendar
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        repeat(7) {
            list.add(cal.clone() as Calendar)
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                weekDays.forEach { day ->
                    val isSelected = isSameDay(day, selectedDate)
                    val isToday = isSameDay(day, today)
                    val dayFormat = SimpleDateFormat("EEE", Locale(selectedLanguage))
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) NeonGreen.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable { onDateSelected(day) }
                            .padding(vertical = 12.dp)
                    ) {
                        Text(dayFormat.format(day.time).uppercase(), fontSize = 10.sp, color = if (isSelected) NeonGreen else GrayText)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = day.get(Calendar.DAY_OF_MONTH).toString(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) NeonGreen else if (isToday) NeonGreen.copy(alpha = 0.6f) else Color.White
                        )
                    }
                }
            }
        }

        item { HorizontalDivider(color = Color.White.copy(alpha = 0.05f)) }

        val weekItems = recordatorios.filter { item ->
            val cal = Calendar.getInstance().apply { timeInMillis = item.fecha }
            weekDays.any { isSameDay(it, cal) }
        }.sortedBy { it.fecha }

        if (weekItems.isEmpty()) {
            item { SmartEmptyState(stringResource(R.string.no_events)) }
        } else {
            items(weekItems) { item ->
                EventAgendaCardPremium(item)
            }
        }
    }
}

@Composable
fun DayViewContent(selectedDate: Calendar, recordatorios: List<com.example.milista.data.Recordatorio>) {
    val dayItems = recordatorios.filter { item ->
        val cal = Calendar.getInstance().apply { timeInMillis = item.fecha }
        isSameDay(cal, selectedDate)
    }.sortedBy { it.fecha }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(stringResource(R.string.schedule_day), color = GrayText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (dayItems.isEmpty()) {
            item { SmartEmptyState(stringResource(R.string.clean_agenda)) }
        } else {
            items(dayItems) { item ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(item.fecha)),
                        modifier = Modifier.width(60.dp),
                        color = NeonGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    EventAgendaCardPremium(item)
                }
            }
        }
    }
}

@Composable
fun AgendaViewContent(recordatorios: List<com.example.milista.data.Recordatorio>, today: Calendar) {
    val upcomingItems = recordatorios
        .filter { it.fecha >= today.timeInMillis && !it.isCompleted }
        .sortedBy { it.fecha }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (upcomingItems.isEmpty()) {
            item { SmartEmptyState(stringResource(R.string.no_pending_reminders)) }
        } else {
            items(upcomingItems) { item ->
                Column {
                    Text(
                        SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date(item.fecha)).uppercase(),
                        color = NeonGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    EventAgendaCardPremium(item)
                }
            }
        }
    }
}

@Composable
fun CalendarHeaderPremium(
    currentMonthDate: Calendar,
    selectedTabId: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    selectedLanguage: String
) {
    val title = if (selectedTabId == R.string.month) {
        SimpleDateFormat("MMMM", Locale(selectedLanguage)).format(currentMonthDate.time).replaceFirstChar { it.uppercase() }
    } else {
        stringResource(R.string.week) + " ${currentMonthDate.get(Calendar.WEEK_OF_YEAR)}"
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrev) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$title ✨",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = (-1).sp
                ),
                textAlign = TextAlign.Center
            )
            Text(
                text = currentMonthDate.get(Calendar.YEAR).toString(),
                style = MaterialTheme.typography.bodyLarge,
                color = NeonGreen,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White)
        }
    }
}

@Composable
fun CalendarViewTabsPremium(selectedTabId: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf(
        R.string.month,
        R.string.week,
        R.string.day,
        R.string.agenda
    )
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(24.dp),
        color = CardDark.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, BorderGlow)
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            tabs.forEach { tabId ->
                val isSelected = selectedTabId == tabId
                val scale by animateFloatAsState(if (isSelected) 1.05f else 1.0f, label = "tabScale")
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) NeonGreen.copy(alpha = 0.15f) else Color.Transparent)
                        .border(1.dp, if (isSelected) NeonGreen.copy(alpha = 0.3f) else Color.Transparent, RoundedCornerShape(20.dp))
                        .clickable { onTabSelected(tabId) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(tabId),
                        color = if (isSelected) NeonGreen else GrayText,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 6.dp)
                                .size(4.dp)
                                .background(NeonGreen, CircleShape)
                                .shadow(8.dp, CircleShape, spotColor = NeonGreen)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarGridCardPremium(
    currentMonthDate: Calendar,
    selectedDate: Calendar,
    today: Calendar,
    onDateSelected: (Calendar) -> Unit,
    recordatorios: List<com.example.milista.data.Recordatorio>,
    selectedLanguage: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = CardDark,
        border = BorderStroke(1.dp, BorderGlow)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            val weekDayFormat = SimpleDateFormat("EEE", Locale(selectedLanguage))
            val tempCal = Calendar.getInstance()
            tempCal.set(Calendar.DAY_OF_WEEK, tempCal.firstDayOfWeek)
            
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) {
                    Text(
                        text = weekDayFormat.format(tempCal.time).uppercase(),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 10.sp,
                        color = GrayText.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Bold
                    )
                    tempCal.add(Calendar.DAY_OF_WEEK, 1)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val cal = currentMonthDate.clone() as Calendar
            cal.set(Calendar.DAY_OF_MONTH, 1)
            val firstDayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) - cal.firstDayOfWeek + 7) % 7
            val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            
            val days = mutableListOf<Calendar?>()
            for (i in 0 until 42) {
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
                                modifier = Modifier.weight(1f).aspectRatio(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                if (currentDay != null) {
                                    val isSelected = isSameDay(currentDay, selectedDate)
                                    val isToday = isSameDay(currentDay, today)
                                    val isPast = isDateInPast(currentDay, today)
                                    
                                    val hasEvents = recordatorios.any { item ->
                                        isSameDay(Calendar.getInstance().apply { timeInMillis = item.fecha }, currentDay)
                                    }

                                    Surface(
                                        onClick = { onDateSelected(currentDay) },
                                        modifier = Modifier.size(38.dp),
                                        shape = CircleShape,
                                        color = when {
                                            isSelected -> NeonGreen
                                            isToday -> NeonGreen.copy(alpha = 0.15f)
                                            else -> Color.Transparent
                                        },
                                        border = BorderStroke(1.dp, if (isToday && !isSelected) NeonGreen else Color.Transparent)
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                            modifier = Modifier.alpha(if (isPast && !isSelected) 0.35f else 1f)
                                        ) {
                                            Text(
                                                text = currentDay.get(Calendar.DAY_OF_MONTH).toString(),
                                                fontSize = 15.sp,
                                                fontWeight = if (isSelected || isToday) FontWeight.ExtraBold else FontWeight.Medium,
                                                color = if (isSelected) AmoledBlack else if (isToday) NeonGreen else Color.White
                                            )
                                            if (hasEvents && !isSelected) {
                                                Box(modifier = Modifier.size(4.dp).background(if (isPast) GrayText else NeonGreen, CircleShape))
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
fun EventAgendaCardPremium(item: com.example.milista.data.Recordatorio) {
    val isOtro = item.tipo == "Otro"
    val color = if (isOtro && item.colorCustom != null) Color(item.colorCustom) else {
        when(item.tipo) {
            "Trabajo" -> Purple
            "Salud" -> SamsungRed
            "Estudio" -> Blue
            else -> NeonGreen
        }
    }
    
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = CardDark.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isOtro) (item.emojiCustom ?: "✨") else getEmojiLocal(item.tipo),
                    fontSize = 24.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isOtro) item.nombreCustom ?: "" else item.tipo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color.White,
                    textDecoration = if (item.isCompleted) TextDecoration.LineThrough else null
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = timeFormat.format(Date(item.fecha)), 
                        fontSize = 13.sp, 
                        color = GrayText
                    )
                }
            }
            
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = color.copy(alpha = 0.15f)
            ) {
                Text(
                    text = item.tipo,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    fontSize = 11.sp,
                    color = color,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun SmartEmptyState(message: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.size(80.dp).blur(30.dp).background(NeonGreen.copy(alpha = 0.05f), CircleShape))
            Icon(
                Icons.Outlined.CalendarToday, 
                null, 
                tint = Color.White.copy(alpha = 0.05f), 
                modifier = Modifier.size(64.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            color = GrayText.copy(alpha = 0.4f),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

fun isDateInPast(target: Calendar, today: Calendar): Boolean {
    if (target.get(Calendar.YEAR) < today.get(Calendar.YEAR)) return true
    if (target.get(Calendar.YEAR) > today.get(Calendar.YEAR)) return false
    return target.get(Calendar.DAY_OF_YEAR) < today.get(Calendar.DAY_OF_YEAR)
}
