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
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Note
import androidx.compose.material.icons.rounded.*
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.milista.R
import com.example.milista.data.ItemType
import com.example.milista.data.UnifiedItem
import com.example.milista.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: MiListaViewModel,
    onAddReminder: (String) -> Unit,
    onNavigateToClock: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToListas: () -> Unit,
    onNavigateToNotes: () -> Unit
) {
    val recordatorios by viewModel.recordatorios.collectAsState()
    val todasLasTareas by viewModel.todasLasTareas.collectAsState()
    val quickNotesId by viewModel.quickNotesId.collectAsState()

    // 1. Saludo dinámico
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val greetingRes = when {
        hour in 5..11 -> R.string.good_morning
        hour in 12..18 -> R.string.good_afternoon
        else -> R.string.good_evening
    }

    // 2. Productividad Real (Hoy)
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val tomorrow = today + (24 * 60 * 60 * 1000)
    
    val todayItems = recordatorios.filter { it.fecha in today until tomorrow }
    val totalTasks = todayItems.size
    val completedTasks = todayItems.count { it.isCompleted }
    val productivityRatio = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f
    val productivityPercent = (productivityRatio * 100).toInt()

    // 3. Próximo Evento Real
    val currentMillis = remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentMillis.longValue = System.currentTimeMillis()
            delay(1000)
        }
    }
    
    val nextEvent = recordatorios
        .filter { it.fecha > currentMillis.longValue }
        .minByOrNull { it.fecha }

    // 4. Notas Recientes Reales (Directo de la tabla de tareas/notas)
    val recentNotes = todasLasTareas
        .filter { it.listaId == quickNotesId }
        .sortedByDescending { it.fechaCreacion }
        .take(2)

    Box(modifier = Modifier.fillMaxSize().background(AmoledBlack)) {
        // Fondo ambiental
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        Brush.radialGradient(
                            colors = listOf(NeonGreen.copy(alpha = 0.05f), Color.Transparent),
                            center = Offset(size.width * 0.8f, size.height * 0.1f),
                            radius = size.width * 1.2f
                        )
                    )
                }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp)
        ) {
            // Header
            item { NoctraMainHeader() }

            // Bienvenida y Productividad
            item {
                ProductivityDashboardCard(
                    greeting = stringResource(greetingRes),
                    percent = productivityPercent,
                    hasData = totalTasks > 0
                )
            }

            // Calendario Dinámico + Evento
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CompactCalendarCard(
                        modifier = Modifier.weight(1.2f),
                        onNavigate = onNavigateToCalendar,
                        recordatorios = recordatorios
                    )
                    NextEventCard(
                        modifier = Modifier.weight(0.8f),
                        nextEvent = nextEvent,
                        currentMillis = currentMillis.longValue
                    )
                }
            }

            // Notas Rápidas
            item { QuickNotesDashboard(recentNotes, onNavigateToNotes) }

            // Resumen del día
            item { 
                DaySummaryGrid(
                    completedCount = completedTasks,
                    totalCount = totalTasks
                ) 
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun NoctraMainHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Noctra ✨",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = (-1).sp
                )
            )
        }
        HeaderActionBtn(Icons.Rounded.Notifications)
    }
}

@Composable
fun HeaderActionBtn(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, BorderGlow, CircleShape)
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(22.dp))
    }
}

@Composable
fun ProductivityDashboardCard(greeting: String, percent: Int, hasData: Boolean) {
    val animatedProgress by animateFloatAsState(
        targetValue = percent / 100f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "productivityProgress"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(24.dp, RoundedCornerShape(28.dp), spotColor = NeonGreen.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(28.dp),
        color = CardDark,
        border = BorderStroke(1.dp, BorderGlow)
    ) {
        Row(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = buildAnnotatedString {
                        append(stringResource(R.string.organize_day))
                        append(" ")
                        withStyle(style = SpanStyle(color = NeonGreen, fontWeight = FontWeight.Bold)) {
                            append(stringResource(R.string.maximize_potential))
                        }
                        append(stringResource(R.string.your_potential))
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = GrayText.copy(alpha = 0.8f)
                )
            }

            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 20.dp)) {
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(68.dp),
                    color = NeonGreen,
                    strokeWidth = 6.dp,
                    trackColor = Color.White.copy(alpha = 0.05f)
                )
                Text(
                    "$percent%",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NeonGreen
                )
                Box(modifier = Modifier.size(68.dp).blur(12.dp).background(NeonGreen.copy(alpha = 0.1f), CircleShape))
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(stringResource(R.string.productivity), fontSize = 12.sp, color = NeonGreen, fontWeight = FontWeight.Bold)
                Text(if (hasData) stringResource(R.string.today) else stringResource(R.string.no_recent_notes), fontSize = 11.sp, color = GrayText)
                if (hasData) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.ArrowUpward, null, tint = NeonGreen, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CompactCalendarCard(modifier: Modifier = Modifier, onNavigate: () -> Unit, recordatorios: List<com.example.milista.data.Recordatorio>) {
    val dayNameFormat = SimpleDateFormat("EEE", Locale.getDefault())

    // Hoy + próximos 4 días (Total 5 días)
    val daysList = remember {
        val list = mutableListOf<Calendar>()
        val tempCal = Calendar.getInstance()
        repeat(5) {
            list.add(tempCal.clone() as Calendar)
            tempCal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    // Inicializar con hoy (primer día de la lista)
    var selectedDayCal by remember { mutableStateOf<Calendar?>(daysList[0]) }

    Surface(
        modifier = modifier.wrapContentHeight(),
        shape = RoundedCornerShape(28.dp),
        color = CardDark.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, BorderGlow)
    ) {
        Column(modifier = Modifier.padding(vertical = 18.dp)) {
            // Fila de Días Uniforme e Interactiva
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                daysList.forEach { cal ->
                    val dayNum = cal.get(Calendar.DAY_OF_MONTH)
                    // isSelected ahora es la única condición para el resaltado verde
                    val isSelected = selectedDayCal?.let { 
                        it.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR) && 
                        it.get(Calendar.YEAR) == cal.get(Calendar.YEAR) 
                    } ?: false
                    
                    val dayName = dayNameFormat.format(cal.time).uppercase()
                    
                    val dayEvents = recordatorios.filter { item ->
                        val itemCal = Calendar.getInstance().apply { timeInMillis = item.fecha }
                        itemCal.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR) &&
                        itemCal.get(Calendar.YEAR) == cal.get(Calendar.YEAR)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { 
                                // Selección única: al tocar un día se vuelve el único activo
                                selectedDayCal = if (isSelected) null else cal 
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = dayName,
                            fontSize = 8.sp,
                            color = if (isSelected) NeonGreen else GrayText.copy(alpha = 0.6f),
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSelected -> NeonGreen.copy(alpha = 0.25f)
                                        else -> Color.White.copy(alpha = 0.03f)
                                    }
                                )
                                .border(
                                    1.dp, 
                                    if (isSelected) NeonGreen else Color.White.copy(alpha = 0.06f), 
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayNum.toString(),
                                fontSize = 14.sp,
                                color = if (isSelected) Color.White else GrayText,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                            )
                            if (isSelected) {
                                Box(modifier = Modifier.fillMaxSize().blur(8.dp).background(NeonGreen.copy(alpha = 0.05f), CircleShape))
                            }
                        }
                        
                        // Punto indicador de eventos (Elegante ✦)
                        if (dayEvents.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "✦", 
                                color = if (isSelected) NeonGreen else GrayText.copy(alpha = 0.4f),
                                fontSize = 8.sp
                            )
                        } else {
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }
                }
            }

            // Panel Expandible de Eventos Reales
            AnimatedVisibility(
                visible = selectedDayCal != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                selectedDayCal?.let { cal ->
                    val eventsForSelected = recordatorios.filter { item ->
                        val itemCal = Calendar.getInstance().apply { timeInMillis = item.fecha }
                        itemCal.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR) &&
                        itemCal.get(Calendar.YEAR) == cal.get(Calendar.YEAR)
                    }.sortedBy { it.fecha }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        color = Color.White.copy(alpha = 0.03f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (eventsForSelected.isEmpty()) {
                                Text(
                                    stringResource(R.string.no_events),
                                    color = GrayText.copy(alpha = 0.5f),
                                    fontSize = 12.sp,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                eventsForSelected.take(2).forEach { event ->
                                    DashboardReminderItem(
                                        color = if (event.colorCustom != null) Color(event.colorCustom) else NeonGreen,
                                        title = event.nombreCustom ?: event.tipo,
                                        time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(event.fecha))
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                if (eventsForSelected.size > 2) {
                                    Text(
                                        "+${eventsForSelected.size - 2} ${stringResource(R.string.reminders).lowercase()}",
                                        color = NeonGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            // Footer del Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.see_full_calendar),
                    color = NeonGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigate() }
                )
            }
        }
    }
}

@Composable
fun DashboardReminderItem(color: Color, title: String, time: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.width(3.dp).height(24.dp).background(color, RoundedCornerShape(2.dp)))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White, maxLines = 1)
            Text(time, fontSize = 10.sp, color = GrayText)
        }
    }
}

@Composable
fun NextEventCard(modifier: Modifier = Modifier, nextEvent: com.example.milista.data.Recordatorio?, currentMillis: Long) {
    val diff = nextEvent?.let { it.fecha - currentMillis } ?: 0L
    
    val hours = (diff / (1000 * 60 * 60))
    val minutes = (diff / (1000 * 60)) % 60
    val seconds = (diff / 1000) % 60

    Surface(
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(28.dp),
        color = if (nextEvent != null) Color(0xFF130D1F) else CardDark,
        border = BorderStroke(1.dp, if (nextEvent != null) Purple.copy(alpha = 0.2f) else BorderGlow)
    ) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.next_event), color = if (nextEvent != null) Purple else GrayText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(20.dp))
            
            if (nextEvent != null) {
                val isOtro = nextEvent.tipo == "Otro"
                val color = if (isOtro && nextEvent.colorCustom != null) Color(nextEvent.colorCustom) else {
                    when(nextEvent.tipo) {
                        "Trabajo" -> Purple
                        "Salud" -> SamsungRed
                        "Estudio" -> Blue
                        else -> NeonGreen
                    }
                }

                Box(
                    modifier = Modifier.size(56.dp).background(color.copy(alpha = 0.1f), CircleShape).border(1.dp, color.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isOtro) (nextEvent.emojiCustom ?: "✨") else getEmojiLocal(nextEvent.tipo),
                        fontSize = 26.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(if (isOtro) nextEvent.nombreCustom ?: "" else nextEvent.tipo, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center, fontSize = 14.sp, maxLines = 2)
                Text(SimpleDateFormat("EEE · HH:mm", Locale.getDefault()).format(Date(nextEvent.fecha)), fontSize = 11.sp, color = GrayText)
                
                Spacer(modifier = Modifier.weight(1f))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    TimeBlock(String.format("%02d", hours), "H")
                    Text(":", color = Color.White, fontWeight = FontWeight.Bold)
                    TimeBlock(String.format("%02d", minutes), "M")
                    Text(":", color = Color.White, fontWeight = FontWeight.Bold)
                    TimeBlock(String.format("%02d", seconds), "S")
                }
            } else {
                Icon(Icons.Outlined.CalendarToday, null, tint = GrayText.copy(alpha = 0.1f), modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.no_pending_reminders), fontSize = 12.sp, color = GrayText.copy(alpha = 0.4f), textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun TimeBlock(value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
        Text(unit, fontSize = 7.sp, color = GrayText, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun QuickNotesDashboard(notes: List<com.example.milista.data.Tarea>, onNavigate: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = CardDark,
        border = BorderStroke(1.dp, BorderGlow)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.recent_notes), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = Color.White)
                Icon(Icons.Rounded.ChevronRight, null, tint = NeonGreen, modifier = Modifier.clickable { onNavigate() })
            }
            Spacer(modifier = Modifier.height(20.dp))
            
            if (notes.isEmpty()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.Note, null, tint = GrayText.copy(alpha = 0.1f), modifier = Modifier.size(40.dp))
                    Text(stringResource(R.string.no_recent_notes), fontSize = 12.sp, color = GrayText.copy(alpha = 0.4f))
                }
            } else {
                notes.forEach { note ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(36.dp).background(NeonGreen.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Description, null, tint = NeonGreen, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(note.titulo.ifEmpty { stringResource(R.string.new_note) }, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                            Text(SimpleDateFormat("d MMM, HH:mm", Locale.getDefault()).format(Date(note.fechaCreacion)), fontSize = 10.sp, color = GrayText.copy(alpha = 0.6f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DaySummaryGrid(completedCount: Int, totalCount: Int) {
    Column {
        Text(stringResource(R.string.day_summary), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SummarySmallCard(Icons.Rounded.CheckCircle, "$completedCount/$totalCount", stringResource(R.string.tasks), NeonGreen, Modifier.weight(1f))
            SummarySmallCard(Icons.Rounded.Timer, "0h 0m", stringResource(R.string.focus), Purple, Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SummarySmallCard(Icons.Rounded.Flag, "0/0", stringResource(R.string.goals), Orange, Modifier.weight(1f))
            SummarySmallCard(Icons.Rounded.Whatshot, "0 " + stringResource(R.string.streak), stringResource(R.string.streak), Blue, Modifier.weight(1f))
        }
    }
}

@Composable
fun SummarySmallCard(icon: ImageVector, value: String, label: String, color: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = CardDark,
        border = BorderStroke(1.dp, color.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text(label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
        }
    }
}
