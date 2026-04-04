package com.example.milista.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.milista.data.ItemType
import com.example.milista.data.UnifiedItem
import com.example.milista.ui.theme.*
import com.example.milista.ui.utils.getTranslatedText
import com.example.milista.ui.utils.getLocaleCode
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun CalendarScreen(
    viewModel: MiListaViewModel,
    onBack: () -> Unit
) {
    val unifiedItems by viewModel.unifiedItems.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedView by remember { mutableStateOf("Mes") }
    
    val initialPage = 500
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { 1000 })
    
    val currentMonth = remember(pagerState.currentPage) {
        Calendar.getInstance().apply {
            add(Calendar.MONTH, pagerState.currentPage - initialPage)
        }
    }
    
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Box(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
        // Iluminación Verde Superior Derecha (Referencia visual)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        Brush.radialGradient(
                            colors = listOf(SamsungGreen.copy(alpha = 0.12f), Color.Transparent),
                            center = Offset(size.width * 0.9f, 0f),
                            radius = size.width * 1.2f
                        )
                    )
                }
        )

        Scaffold(
            containerColor = Color.Transparent,
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                Column(modifier = Modifier.background(Color.Transparent)) {
                    CalendarTopBarLocal(
                        selectedLanguage = selectedLanguage,
                        currentMonth = currentMonth,
                        onBack = onBack
                    )
                    
                    CalendarTabsPremium(
                        selectedView = selectedView,
                        onViewSelected = { selectedView = it },
                        language = selectedLanguage
                    )
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { /* Nuevo */ },
                    containerColor = SamsungGreen,
                    contentColor = Color.Black,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(12.dp),
                    modifier = Modifier.padding(bottom = 80.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(32.dp))
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                item {
                    CalendarContainerPremium(
                        pagerState = pagerState,
                        initialPage = initialPage,
                        selectedDate = selectedDate,
                        unifiedItems = unifiedItems,
                        onDateSelected = { selectedDate = it },
                        language = selectedLanguage
                    )
                }

                item {
                    AgendaSectionHeader(
                        selectedDate = selectedDate,
                        unifiedItems = unifiedItems,
                        language = selectedLanguage
                    )
                }

                val itemsDelDia = unifiedItems.filter { item ->
                    if (item.timestamp == 0L) return@filter false
                    val cal = Calendar.getInstance().apply { timeInMillis = item.timestamp }
                    isSameDayLocalCalendar(cal, selectedDate)
                }.sortedBy { it.timestamp }

                if (itemsDelDia.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                            Text(getTranslatedText("No hay eventos programados ✨", selectedLanguage), color = GrayText.copy(0.4f), fontSize = 14.sp)
                        }
                    }
                } else {
                    items(itemsDelDia) { item ->
                        EventAgendaCard(item, selectedLanguage)
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarTopBarLocal(selectedLanguage: String, currentMonth: Calendar, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        HeaderIconButtonLocal(Icons.AutoMirrored.Filled.ArrowBack, onClick = onBack)
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = SimpleDateFormat("MMMM", Locale(getLocaleCode(selectedLanguage))).format(currentMonth.time).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 24.sp),
                    color = Color.White
                )
                Icon(Icons.Default.ArrowDropDown, null, tint = SamsungGreen, modifier = Modifier.size(20.dp))
            }
            Text(currentMonth.get(Calendar.YEAR).toString(), fontSize = 12.sp, color = GrayText.copy(0.6f))
        }
        
        Row {
            HeaderIconButtonLocal(Icons.Default.Search)
            Spacer(modifier = Modifier.width(8.dp))
            HeaderIconButtonLocal(Icons.Default.Tune)
            Spacer(modifier = Modifier.width(8.dp))
            HeaderIconButtonLocal(Icons.Default.MoreVert)
        }
    }
}

@Composable
fun HeaderIconButtonLocal(icon: ImageVector, onClick: () -> Unit = {}) {
    Surface(
        modifier = Modifier.size(42.dp),
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp)) }
    }
}

@Composable
fun CalendarTabsPremium(selectedView: String, onViewSelected: (String) -> Unit, language: String) {
    Surface(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp).fillMaxWidth(),
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            listOf("Mes", "Semana", "Día", "Agenda").forEach { view ->
                val isSelected = selectedView == view
                val glowAlpha by animateFloatAsState(if (isSelected) 0.15f else 0f)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) Color(0xFF141A22) else Color.Transparent)
                        .drawBehind { if(isSelected) drawRect(Brush.radialGradient(listOf(SamsungGreen.copy(alpha = glowAlpha), Color.Transparent))) }
                        .clickable { onViewSelected(view) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(getTranslatedText(view, language), color = if (isSelected) SamsungGreen else GrayText, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                }
            }
        }
    }
}

@androidx.compose.foundation.ExperimentalFoundationApi
@Composable
fun CalendarContainerPremium(
    pagerState: androidx.compose.foundation.pager.PagerState,
    initialPage: Int,
    selectedDate: Calendar,
    unifiedItems: List<UnifiedItem>,
    onDateSelected: (Calendar) -> Unit,
    language: String
) {
    Surface(
        modifier = Modifier.padding(20.dp).fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = Color.White.copy(alpha = 0.02f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                val weekDays = listOf("LUN", "MAR", "MIÉ", "JUE", "VIE", "SÁB", "DOM")
                weekDays.forEach { day ->
                    Text(day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 10.sp, color = GrayText.copy(0.4f), fontWeight = FontWeight.Bold)
                }
            }
            
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth().height(260.dp)) { page ->
                val monthToShow = remember(page) { Calendar.getInstance().apply { add(Calendar.MONTH, page - initialPage) } }
                CalendarGridPremium(monthToShow, selectedDate, unifiedItems, onDateSelected)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                DotIndicatorPremium(SamsungGreen, getTranslatedText("Eventos", language))
                DotIndicatorPremium(SamsungBlue, getTranslatedText("Recordatorios", language))
                DotIndicatorPremium(SamsungOrange, getTranslatedText("Tareas", language))
                DotIndicatorPremium(SamsungPurple, getTranslatedText("Notas", language))
            }
        }
    }
}

@Composable
fun DotIndicatorPremium(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 9.sp, color = GrayText.copy(0.5f))
    }
}

@Composable
fun CalendarGridPremium(currentMonth: Calendar, selectedDate: Calendar, unifiedItems: List<UnifiedItem>, onDateSelected: (Calendar) -> Unit) {
    val daysInMonth = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfMonth = (currentMonth.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
    val firstDayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK)
    val startOffset = if (firstDayOfWeek == Calendar.SUNDAY) 6 else firstDayOfWeek - 2

    val days = (0 until 42).map { index ->
        val dayNum = index - startOffset + 1
        if (dayNum in 1..daysInMonth) (currentMonth.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, dayNum) } else null
    }

    LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.fillMaxSize(), userScrollEnabled = false) {
        items(days) { date ->
            if (date != null) {
                val dayItems = unifiedItems.filter { if(it.timestamp == 0L) false else isSameDayLocalCalendar(Calendar.getInstance().apply { timeInMillis = it.timestamp }, date) }
                DayCellFuturistic(date, isSameDayLocalCalendar(date, selectedDate), dayItems, onClick = { onDateSelected(date) })
            } else Spacer(modifier = Modifier.aspectRatio(1f))
        }
    }
}

@Composable
fun DayCellFuturistic(date: Calendar, isSelected: Boolean, dayItems: List<UnifiedItem>, onClick: () -> Unit) {
    val isToday = isSameDayLocalCalendar(Calendar.getInstance(), date)
    Box(modifier = Modifier.aspectRatio(1f).padding(2.dp).clip(CircleShape).background(if (isSelected) SamsungGreen.copy(alpha = 0.15f) else Color.Transparent).drawBehind { if(isSelected) drawCircle(SamsungGreen, radius = size.minDimension/2, style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx())) }.clickable { onClick() }, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(date.get(Calendar.DAY_OF_MONTH).toString(), fontSize = 14.sp, fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isToday) SamsungGreen else Color.White)
            Row(modifier = Modifier.padding(top = 2.dp), horizontalArrangement = Arrangement.spacedBy(1.5.dp)) {
                val types = dayItems.map { it.type }.distinct().take(4)
                types.forEach { type ->
                    val color = when(type) {
                        ItemType.EVENT -> SamsungGreen
                        ItemType.REMINDER -> SamsungBlue
                        ItemType.TASK -> SamsungOrange
                        ItemType.NOTE -> SamsungPurple
                        else -> SamsungGreen
                    }
                    Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(color))
                }
            }
        }
    }
}

@Composable
fun AgendaSectionHeader(selectedDate: Calendar, unifiedItems: List<UnifiedItem>, language: String) {
    val dateStr = SimpleDateFormat("EEEE, d 'de' MMMM", Locale(getLocaleCode(language))).format(selectedDate.time).replaceFirstChar { it.uppercase() }
    val count = unifiedItems.count { if(it.timestamp == 0L) false else isSameDayLocalCalendar(Calendar.getInstance().apply { timeInMillis = it.timestamp }, selectedDate) }
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Filled.EventNote, null, tint = SamsungGreen, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(dateStr, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
        }
        Surface(color = Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))) {
            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(SamsungGreen))
                Spacer(modifier = Modifier.width(6.dp))
                Text("$count " + getTranslatedText("eventos", language), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Icon(Icons.Default.KeyboardArrowDown, null, tint = GrayText, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun EventAgendaCard(item: UnifiedItem, language: String) {
    val color = when(item.type) {
        ItemType.EVENT -> SamsungGreen
        ItemType.REMINDER -> SamsungBlue
        ItemType.TASK -> SamsungOrange
        ItemType.NOTE -> SamsungPurple
        else -> SamsungGreen
    }
    Surface(modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.04f), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.width(50.dp)) {
                Text(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(item.timestamp)), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(item.timestamp + 3600000)), fontSize = 11.sp, color = GrayText.copy(0.6f))
            }
            Box(modifier = Modifier.width(2.5.dp).height(32.dp).clip(RoundedCornerShape(1.dp)).background(color))
            Spacer(modifier = Modifier.width(16.dp))
            Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(color.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                val icon = when(item.type) {
                    ItemType.EVENT -> Icons.Default.CalendarToday
                    ItemType.REMINDER -> Icons.Default.NotificationsActive
                    ItemType.TASK -> Icons.Default.CheckBox
                    ItemType.NOTE -> Icons.Default.NoteAlt
                    else -> Icons.Default.Event
                }
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(if(item.content.isNullOrBlank()) "Oficina" else item.content!!, fontSize = 12.sp, color = GrayText.copy(0.6f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(10.dp), border = BorderStroke(1.dp, color.copy(alpha = 0.2f))) {
                Text(getTranslatedText(item.type.name, language), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = color)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.MoreVert, null, tint = GrayText.copy(0.4f), modifier = Modifier.size(18.dp))
        }
    }
}

fun isSameDayLocalCalendar(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
