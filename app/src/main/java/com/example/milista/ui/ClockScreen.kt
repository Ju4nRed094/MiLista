package com.example.milista.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.milista.data.Alarma
import com.example.milista.ui.utils.getTranslatedText
import com.example.milista.ui.theme.*
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockScreen(
    viewModel: MiListaViewModel,
    onBack: () -> Unit
) {
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 5 })
    val scope = rememberCoroutineScope()

    val tabs = listOf(
        "Alarmas" to Icons.Default.Alarm,
        "Reloj mundial" to Icons.Default.Public,
        "Cronómetro" to Icons.Default.Timer,
        "Temporizador" to Icons.Default.HourglassEmpty,
        "Descanso" to Icons.Default.Bedtime
    )

    Box(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
        // Fondo con iluminación ambiental dinámica
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        Brush.radialGradient(
                            colors = listOf(SamsungGreen.copy(alpha = 0.1f), Color.Transparent),
                            center = Offset(size.width * 0.8f, 0f),
                            radius = size.width * 1.5f
                        )
                    )
                }
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column(modifier = Modifier.background(Color.Transparent)) {
                    CenterAlignedTopAppBar(
                        title = { },
                        navigationIcon = {
                            HeaderCircleButtonClock(Icons.AutoMirrored.Filled.ArrowBack, onClick = onBack)
                        },
                        actions = {
                            HeaderCircleButtonClock(Icons.Default.Search, onClick = { /* Buscar */ })
                            Spacer(modifier = Modifier.width(8.dp))
                            HeaderCircleButtonClock(Icons.Default.MoreVert, onClick = { /* Menú */ })
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                    )
                    
                    Text(
                        text = getTranslatedText(tabs[pagerState.currentPage].first, selectedLanguage),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 34.sp,
                            letterSpacing = (-1).sp
                        ),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )

                    ScrollableTabRow(
                        selectedTabIndex = pagerState.currentPage,
                        containerColor = Color.Transparent,
                        contentColor = SamsungGreen,
                        edgePadding = 24.dp,
                        divider = {},
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                                color = SamsungGreen,
                                height = 3.dp
                            )
                        }
                    ) {
                        tabs.forEachIndexed { index, (label, icon) ->
                            Tab(
                                selected = pagerState.currentPage == index,
                                onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            icon, 
                                            null, 
                                            modifier = Modifier.size(18.dp),
                                            tint = if (pagerState.currentPage == index) SamsungGreen else GrayText.copy(alpha = 0.6f)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            getTranslatedText(label, selectedLanguage),
                                            fontSize = 14.sp,
                                            fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal,
                                            color = if (pagerState.currentPage == index) SamsungGreen else GrayText.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (page) {
                        0 -> AlarmsTabNoctra(viewModel, selectedLanguage)
                        1 -> WorldClockTabNoctra(selectedLanguage)
                        2 -> StopwatchTabNoctra(selectedLanguage)
                        3 -> TimerTabNoctra(selectedLanguage)
                        4 -> SleepTabNoctra(selectedLanguage)
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderCircleButtonClock(icon: ImageVector, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.size(44.dp).padding(4.dp),
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun AlarmsTabNoctra(viewModel: MiListaViewModel, language: String) {
    val alarms by viewModel.alarmas.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var alarmToEdit by remember { mutableStateOf<Alarma?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 120.dp)
        ) {
            item {
                AlarmSummaryCardNoctra(alarms, language)
            }
            
            if (alarms.isEmpty()) {
                item {
                    EmptyClockStateNoctra(
                        icon = Icons.Default.AlarmOn,
                        text = getTranslatedText("No hay alarmas activas", language),
                        subtext = getTranslatedText("Todo está bajo control ✨", language)
                    )
                }
            } else {
                items(alarms) { alarm ->
                    AlarmPremiumCardNoctra(
                        alarm = alarm,
                        onToggle = { viewModel.actualizarAlarma(alarm.copy(activa = it)) },
                        onClick = { alarmToEdit = alarm },
                        onDelete = { viewModel.borrarAlarma(alarm) },
                        language = language
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = SamsungGreen,
            contentColor = Color.Black,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(6.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Nueva Alarma", modifier = Modifier.size(28.dp))
        }

        if (showAddDialog || alarmToEdit != null) {
            AlarmEditDialogNoctra(
                alarm = alarmToEdit,
                onDismiss = { 
                    showAddDialog = false
                    alarmToEdit = null
                },
                onSave = { h, m, d, e, t ->
                    if (alarmToEdit == null) {
                        viewModel.agregarAlarma(h, m, d, e, t)
                    } else {
                        viewModel.actualizarAlarma(alarmToEdit!!.copy(hora = h, minuto = m, dias = d, etiqueta = e, tonoUri = t))
                    }
                    showAddDialog = false
                    alarmToEdit = null
                },
                language = language
            )
        }
    }
}

@Composable
fun AlarmSummaryCardNoctra(alarms: List<Alarma>, language: String) {
    val nextAlarm = alarms.filter { it.activa }.minByOrNull { it.hora * 60 + it.minuto }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = Color.White.copy(alpha = 0.04f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(32.dp).background(SamsungGreen.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Alarm, null, tint = SamsungGreen, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(getTranslatedText("Próxima alarma", language), fontSize = 12.sp, color = SamsungGreen, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = nextAlarm?.let { String.format(Locale.getDefault(), "%02d:%02d", it.hora, it.minuto) } ?: "--:--",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = nextAlarm?.dias ?: getTranslatedText("Sin alarmas", language),
                    fontSize = 12.sp,
                    color = GrayText.copy(alpha = 0.7f)
                )
            }
            
            VerticalDivider(modifier = Modifier.height(60.dp).padding(horizontal = 16.dp), color = Color.White.copy(alpha = 0.05f))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(32.dp).background(SamsungBlue.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Bedtime, null, tint = SamsungBlue, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(getTranslatedText("Hora de dormir", language), fontSize = 12.sp, color = SamsungBlue, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "22:30",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = getTranslatedText("Sugerencia IA", language),
                    fontSize = 12.sp,
                    color = GrayText.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlarmPremiumCardNoctra(
    alarm: Alarma,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    language: String
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = { showDeleteConfirm = true })
    ) {
        Row(
            modifier = Modifier.padding(vertical = 20.dp, horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        String.format(Locale.getDefault(), "%02d:%02d", alarm.hora, alarm.minuto),
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (alarm.activa) Color.White else GrayText.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (alarm.hora < 12) "AM" else "PM",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (alarm.activa) Color.White.copy(alpha = 0.7f) else GrayText.copy(alpha = 0.3f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                if (alarm.etiqueta.isNotEmpty()) {
                    Text(alarm.etiqueta, color = if (alarm.activa) Color.White.copy(alpha = 0.9f) else GrayText.copy(alpha = 0.5f), fontSize = 14.sp)
                }
                Text(
                    text = alarm.dias.ifEmpty { getTranslatedText("Sin repetir", language) },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (alarm.activa) SamsungGreen else GrayText.copy(alpha = 0.4f),
                    fontWeight = if (alarm.activa) FontWeight.Bold else FontWeight.Normal
                )
            }
            
            Switch(
                checked = alarm.activa,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = SamsungGreen,
                    uncheckedThumbColor = GrayText,
                    uncheckedTrackColor = Color.White.copy(alpha = 0.1f),
                    uncheckedBorderColor = Color.Transparent
                )
            )
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = CardDark,
            title = { Text(getTranslatedText("Eliminar alarma", language), color = Color.White) },
            text = { Text(getTranslatedText("¿Deseas eliminar esta alarma?", language), color = GrayText) },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteConfirm = false }) {
                    Text(getTranslatedText("Eliminar", language), color = SamsungRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(getTranslatedText("Cancelar", language), color = Color.White)
                }
            }
        )
    }
}

@Composable
fun WorldClockTabNoctra(language: String) {
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(1000)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 120.dp)
        ) {
            item { LocalTimeCardNoctra(currentTime, language) }
            item { 
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(getTranslatedText("Mis Ciudades", language), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { }) { Icon(Icons.AutoMirrored.Filled.Sort, null, tint = GrayText, modifier = Modifier.size(18.dp)) }
                }
            }
            items(listOf(
                Triple("Madrid", "+5h", "21:45"),
                Triple("Tokyo", "+12h", "04:45"),
                Triple("New York", "-1h", "14:45")
            )) { (city, diff, time) ->
                WorldCityItemNoctra(city, diff, time)
            }
        }

        FloatingActionButton(
            onClick = { /* Add city */ },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = SamsungGreen,
            contentColor = Color.Black,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
fun LocalTimeCardNoctra(time: Long, language: String) {
    val df = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateDf = SimpleDateFormat("EEEE, d MMM", Locale.getDefault())
    
    Surface(
        shape = RoundedCornerShape(32.dp),
        color = Color.White.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Santiago", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(dateDf.format(Date(time)).uppercase(), fontSize = 14.sp, color = SamsungGreen, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(24.dp))
            Text(df.format(Date(time)), fontSize = 80.sp, fontWeight = FontWeight.ExtraLight, color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            AnalogClockNoctra(time)
        }
    }
}

@Composable
fun WorldCityItemNoctra(city: String, diff: String, time: String) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.04f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(city, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(diff, fontSize = 14.sp, color = GrayText.copy(alpha = 0.7f))
            }
            Text(time, fontSize = 32.sp, fontWeight = FontWeight.Light, color = Color.White)
        }
    }
}

@Composable
fun AnalogClockNoctra(time: Long) {
    val calendar = Calendar.getInstance().apply { timeInMillis = time }
    val seconds = calendar.get(Calendar.SECOND)
    val minutes = calendar.get(Calendar.MINUTE)
    val hours = calendar.get(Calendar.HOUR)
    Canvas(modifier = Modifier.size(180.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f
        drawCircle(color = Color.White.copy(alpha = 0.03f), radius = radius, center = center)
        for (i in 0 until 12) {
            val angle = i * 30f - 90f
            val start = center + offsetFromAngleNoctra(angle, radius * 0.85f)
            val end = center + offsetFromAngleNoctra(angle, radius * 0.95f)
            drawLine(color = Color.White.copy(alpha = 0.2f), start = start, end = end, strokeWidth = 2.dp.toPx())
        }
        val secondEnd = center + offsetFromAngleNoctra((seconds * 6f) - 90f, radius * 0.85f)
        drawLine(color = SamsungGreen, start = center, end = secondEnd, strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
        val minuteEnd = center + offsetFromAngleNoctra((minutes * 6f) - 90f, radius * 0.7f)
        drawLine(color = Color.White, start = center, end = minuteEnd, strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
        val hourEnd = center + offsetFromAngleNoctra((hours * 30f + minutes * 0.5f) - 90f, radius * 0.5f)
        drawLine(color = Color.White, start = center, end = hourEnd, strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(color = Color.White, radius = 5.dp.toPx(), center = center)
        drawCircle(color = SamsungGreen, radius = 2.dp.toPx(), center = center)
    }
}

fun offsetFromAngleNoctra(angle: Float, radius: Float): Offset {
    val rad = Math.toRadians(angle.toDouble())
    return Offset((radius * cos(rad)).toFloat(), (radius * sin(rad)).toFloat())
}

@Composable
fun StopwatchTabNoctra(language: String) {
    var timeInMillis by remember { mutableLongStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }
    val laps = remember { mutableStateListOf<Long>() }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            val startTime = System.currentTimeMillis() - timeInMillis
            while (isRunning) {
                timeInMillis = System.currentTimeMillis() - startTime
                delay(10)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(280.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(color = Color.White.copy(alpha = 0.05f), style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round))
                val progress = (timeInMillis % 60000) / 60000f
                drawArc(
                    color = SamsungGreen,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            Text(
                String.format(Locale.getDefault(), "%02d:%02d.%02d", (timeInMillis / 60000) % 60, (timeInMillis / 1000) % 60, (timeInMillis / 10) % 100),
                fontSize = 48.sp, fontWeight = FontWeight.Bold, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(60.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            ClockActionBtnNoctra(
                label = getTranslatedText(if (isRunning) "Vuelta" else "Reiniciar", language),
                color = Color.White.copy(0.08f),
                onClick = { if (isRunning) laps.add(0, timeInMillis) else { timeInMillis = 0; laps.clear() } },
                modifier = Modifier.weight(1f)
            )
            ClockActionBtnNoctra(
                label = getTranslatedText(if (isRunning) "Pausar" else "Iniciar", language),
                color = if (isRunning) SamsungRed else SamsungGreen,
                textColor = if (isRunning) Color.White else Color.Black,
                onClick = { isRunning = !isRunning },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
            items(laps.size) { index ->
                val lapTime = laps[index]
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(getTranslatedText("Vuelta", language) + " ${laps.size - index}", color = GrayText.copy(alpha = 0.7f))
                    Text(
                        String.format(Locale.getDefault(), "%02d:%02d.%02d", (lapTime / 60000) % 60, (lapTime / 1000) % 60, (lapTime / 10) % 100),
                        fontWeight = FontWeight.SemiBold, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, color = Color.White
                    )
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
            }
        }
    }
}

@Composable
fun TimerTabNoctra(language: String) {
    var timeInSeconds by remember { mutableLongStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }
    var initialTime by remember { mutableLongStateOf(0L) }

    LaunchedEffect(isRunning) {
        if (isRunning && timeInSeconds > 0) {
            while (timeInSeconds > 0 && isRunning) {
                delay(1000)
                timeInSeconds--
            }
            if (timeInSeconds == 0L) isRunning = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(280.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(color = Color.White.copy(alpha = 0.05f), style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round))
                if (initialTime > 0) {
                    drawArc(
                        color = SamsungGreen,
                        startAngle = -90f,
                        sweepAngle = 360f * (timeInSeconds.toFloat() / initialTime),
                        useCenter = false,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
            Text(
                String.format(Locale.getDefault(), "%02d:%02d:%02d", timeInSeconds / 3600, (timeInSeconds % 3600) / 60, timeInSeconds % 60),
                fontSize = 54.sp, fontWeight = FontWeight.Light, color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(60.dp))

        if (!isRunning && timeInSeconds == 0L) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickTimerBtnNoctra("+1m") { timeInSeconds += 60; initialTime = timeInSeconds }
                QuickTimerBtnNoctra("+5m") { timeInSeconds += 300; initialTime = timeInSeconds }
                QuickTimerBtnNoctra("+10m") { timeInSeconds += 600; initialTime = timeInSeconds }
                QuickTimerBtnNoctra("+30m") { timeInSeconds += 1800; initialTime = timeInSeconds }
            }
            Spacer(modifier = Modifier.weight(1f))
            ClockActionBtnNoctra(getTranslatedText("Iniciar", language), SamsungGreen, Color.Black, { if(timeInSeconds > 0) isRunning = true }, Modifier.fillMaxWidth())
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                ClockActionBtnNoctra(getTranslatedText("Cancelar", language), Color.White.copy(0.08f), Color.White, { timeInSeconds = 0; initialTime = 0; isRunning = false }, Modifier.weight(1f))
                ClockActionBtnNoctra(getTranslatedText(if (isRunning) "Pausar" else "Iniciar", language), if (isRunning) SamsungRed else SamsungGreen, if (isRunning) Color.White else Color.Black, { isRunning = !isRunning }, Modifier.weight(1f))
            }
        }
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun SleepTabNoctra(language: String) {
    var bedtime by remember { mutableStateOf("23:00") }
    var wakeup by remember { mutableStateOf("07:00") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 120.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = Color.White.copy(alpha = 0.05f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(getTranslatedText("Objetivo de sueño", language), color = SamsungBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        SleepTimeItem(getTranslatedText("Hora de dormir", language), bedtime, SamsungBlue)
                        SleepTimeItem(getTranslatedText("Hora de despertar", language), wakeup, SamsungGreen)
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = SamsungBlue),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(getTranslatedText("Configurar rutina", language), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text(getTranslatedText("Sonidos relajantes", language), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
        }

        items(listOf(
            "Lluvia nocturna" to "🌧️",
            "Bosque profundo" to "🌲",
            "Océano zen" to "🌊",
            "Ruido blanco" to "🤍"
        )) { (name, emoji) ->
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.04f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
                modifier = Modifier.fillMaxWidth().clickable { }
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(emoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(getTranslatedText(name, language), color = Color.White, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(Icons.Default.PlayArrow, null, tint = SamsungGreen)
                }
            }
        }
    }
}

@Composable
fun SleepTimeItem(label: String, time: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = GrayText, fontSize = 12.sp)
        Text(time, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
        Box(modifier = Modifier.size(4.dp).background(color, CircleShape).padding(top = 4.dp))
    }
}

@Composable
fun ClockActionBtnNoctra(label: String, color: Color, textColor: Color = Color.White, onClick: () -> Unit, modifier: Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(32.dp),
        color = color,
        shadowElevation = 4.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, color = textColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun QuickTimerBtnNoctra(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(0.08f),
        border = BorderStroke(1.dp, Color.White.copy(0.1f))
    ) {
        Text(label, color = Color.White, modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun EmptyClockStateNoctra(icon: ImageVector, text: String, subtext: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(top = 80.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.size(140.dp).background(SamsungGreen.copy(alpha = 0.05f), CircleShape).blur(40.dp))
            Icon(icon, contentDescription = null, modifier = Modifier.size(100.dp), tint = Color.White.copy(alpha = 0.08f))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(text, style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.Bold)
        Text(subtext, style = MaterialTheme.typography.bodySmall, color = GrayText.copy(alpha = 0.5f))
    }
}

@Composable
fun AlarmEditDialogNoctra(
    alarm: Alarma?,
    onDismiss: () -> Unit,
    onSave: (Int, Int, String, String, String?) -> Unit,
    language: String
) {
    var hora by remember { mutableIntStateOf(alarm?.hora ?: 8) }
    var minuto by remember { mutableIntStateOf(alarm?.minuto ?: 0) }
    var etiqueta by remember { mutableStateOf(alarm?.etiqueta ?: "") }
    var tonoUri by remember { mutableStateOf(alarm?.tonoUri) }
    var selectedDays by remember { mutableStateOf(alarm?.dias?.split(",")?.filter { it.isNotBlank() } ?: emptyList()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = CardDark,
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(28.dp)) {
                Text(
                    if (alarm == null) getTranslatedText("Nueva Alarma", language) else getTranslatedText("Editar Alarma", language),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NumberPickerLocalClock(value = hora, range = 0..23, onValueChange = { hora = it })
                    Text(":", fontSize = 48.sp, color = Color.White, modifier = Modifier.padding(horizontal = 12.dp))
                    NumberPickerLocalClock(value = minuto, range = 0..59, onValueChange = { minuto = it })
                }

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = etiqueta,
                    onValueChange = { etiqueta = it },
                    label = { Text(getTranslatedText("Nombre de la alarma", language)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = SamsungGreen
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(getTranslatedText("Repetir", language), style = MaterialTheme.typography.labelMedium, color = GrayText)
                Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    val days = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
                    days.forEach { day ->
                        val isSelected = selectedDays.contains(day)
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) SamsungGreen else Color.White.copy(alpha = 0.05f))
                                .clickable {
                                    selectedDays = if (isSelected) selectedDays - day else selectedDays + day
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                day.first().toString(),
                                color = if (isSelected) Color.Black else GrayText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(getTranslatedText("Cancelar", language), color = Color.White) }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { onSave(hora, minuto, selectedDays.joinToString(","), etiqueta, tonoUri) },
                        colors = ButtonDefaults.buttonColors(containerColor = SamsungGreen),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(getTranslatedText("Guardar", language), color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun NumberPickerLocalClock(value: Int, range: IntRange, onValueChange: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = { if (value < range.last) onValueChange(value + 1) else onValueChange(range.first) }) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, tint = Color.White)
        }
        Text(String.format(Locale.getDefault(), "%02d", value), fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color.White)
        IconButton(onClick = { if (value > range.first) onValueChange(value - 1) else onValueChange(range.last) }) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.White)
        }
    }
}
