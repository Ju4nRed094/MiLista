package com.example.milista.ui

import android.media.MediaPlayer
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.milista.R
import com.example.milista.data.Alarma
import com.example.milista.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockScreen(
    viewModel: MiListaViewModel = viewModel()
) {
    val alarmas by viewModel.alarmas.collectAsState()
    var selectedTab by remember { mutableStateOf("Alarmas") }
    var showAddAlarm by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = AmoledBlack,
        floatingActionButton = {
            if (selectedTab == "Alarmas") {
                FloatingActionButton(
                    onClick = { showAddAlarm = true },
                    containerColor = NeonGreen,
                    contentColor = AmoledBlack,
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(bottom = 100.dp)
                        .size(64.dp)
                        .shadow(16.dp, CircleShape, spotColor = NeonGreen)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(32.dp))
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ClockHeaderV2()

            ClockTabsV2(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    "Alarmas" -> AlarmListSection(alarmas, viewModel)
                    "Reloj mundial" -> WorldClockSectionV2()
                    "Cronómetro" -> StopwatchSectionV2()
                    "Temporizador" -> TimerSectionV2()
                    "Descanso" -> SleepSectionV2()
                }
            }
        }

        if (showAddAlarm) {
            ModalBottomSheet(
                onDismissRequest = { showAddAlarm = false },
                containerColor = CardDark,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(0.1f)) }
            ) {
                AddAlarmSheet(
                    onDismiss = { showAddAlarm = false },
                    onSave = { hora, min, dias, etiqueta, tono ->
                        viewModel.agregarAlarma(hora, min, dias, etiqueta, tono)
                        showAddAlarm = false
                    }
                )
            }
        }
    }
}

@Composable
fun ClockHeaderV2() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = stringResource(R.string.clock),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Text(
                text = stringResource(R.string.all_time),
                color = NeonGreen,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
        HeaderCircleActionBtn(Icons.Rounded.MoreVert)
    }
}

@Composable
fun HeaderCircleActionBtn(icon: ImageVector) {
    Surface(
        modifier = Modifier.size(48.dp),
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, Color.White.copy(0.08f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
fun ClockTabsV2(selectedTab: String, onTabSelected: (String) -> Unit) {
    val tabs = listOf("Alarmas", "Reloj mundial", "Cronómetro", "Temporizador", "Descanso")
    ScrollableTabRow(
        selectedTabIndex = tabs.indexOf(selectedTab),
        containerColor = Color.Transparent,
        contentColor = NeonGreen,
        edgePadding = 24.dp,
        divider = {},
        indicator = { tabPositions ->
            if (tabs.indexOf(selectedTab) != -1) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[tabs.indexOf(selectedTab)]),
                    color = NeonGreen
                )
            }
        }
    ) {
        tabs.forEach { tab ->
            val isSelected = selectedTab == tab
            Tab(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        text = tab,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) NeonGreen else GrayText
                    )
                }
            )
        }
    }
}

@Composable
fun AlarmListSection(alarmas: List<Alarma>, viewModel: MiListaViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(24.dp)
    ) {
        item {
            val nextAlarm = alarmas.filter { it.activa }.minByOrNull { it.hora * 60 + it.minuto }
            DashboardNextAlarm(nextAlarm)
        }

        items(alarmas) { alarma ->
            AlarmItemCardV2(
                alarma = alarma,
                onToggle = { viewModel.actualizarAlarma(alarma.copy(activa = it)) },
                onDelete = { viewModel.borrarAlarma(alarma) }
            )
        }
    }
}

@Composable
fun DashboardNextAlarm(alarma: Alarma?) {
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(1000)
        }
    }

    val remainingTimeText = remember(alarma, currentTime) {
        alarma?.let {
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, it.hora)
                set(Calendar.MINUTE, it.minuto)
                set(Calendar.SECOND, 0)
                if (before(now)) add(Calendar.DATE, 1)
            }
            val diff = target.timeInMillis - now.timeInMillis
            val hours = (diff / 3600000)
            val minutes = (diff % 3600000) / 60000
            val seconds = (diff % 60000) / 1000
            "Faltan: ${hours}h ${minutes}m ${seconds}s"
        } ?: "No hay alarmas activas"
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        shape = RoundedCornerShape(28.dp),
        color = CardDark,
        border = BorderStroke(1.dp, BorderGlow)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Próxima alarma", color = NeonGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = alarma?.let { String.format("%02d:%02d", it.hora, it.minuto) } ?: "--:--",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = remainingTimeText,
                    color = NeonGreen.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(NeonGreen.copy(0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Alarm, null, tint = NeonGreen, modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Composable
fun AlarmItemCardV2(alarma: Alarma, onToggle: (Boolean) -> Unit, onDelete: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = if (alarma.activa) Color.White.copy(0.05f) else Color.White.copy(0.02f),
        border = BorderStroke(1.dp, if (alarma.activa) BorderGlow else Color.White.copy(0.05f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = String.format("%02d:%02d", alarma.hora, alarma.minuto),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (alarma.activa) Color.White else GrayText
                )
                Text(
                    text = alarma.dias.ifEmpty { "Sin repetición" },
                    fontSize = 13.sp,
                    color = if (alarma.activa) NeonGreen.copy(0.8f) else GrayText.copy(0.5f)
                )
                if (alarma.etiqueta.isNotEmpty()) {
                    Text(alarma.etiqueta, color = GrayText, fontSize = 12.sp)
                }
            }
            
            Switch(
                checked = alarma.activa,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = NeonGreen,
                    uncheckedThumbColor = GrayText,
                    uncheckedTrackColor = Color.White.copy(0.1f)
                )
            )

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, null, tint = GrayText.copy(0.3f))
            }
        }
    }
}

@Composable
fun AddAlarmSheet(onDismiss: () -> Unit, onSave: (Int, Int, String, String, String?) -> Unit) {
    var selectedHour by remember { mutableIntStateOf(12) }
    var selectedMin by remember { mutableIntStateOf(0) }
    var label by remember { mutableStateOf("") }
    val selectedDays = remember { mutableStateListOf<String>() }
    val days = listOf("L", "M", "M", "J", "V", "S", "D")
    val fullDays = listOf("Lun", "Mar", "Mie", "Jue", "Vie", "Sab", "Dom")
    
    var selectedToneUri by remember { mutableStateOf<String?>(null) }
    var toneName by remember { mutableStateOf("Predeterminado") }
    
    val tonePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedToneUri = it.toString()
            toneName = it.lastPathSegment ?: "Personalizado"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Nueva Alarma", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NumberWheelPicker(range = 0..23, onValueChange = { selectedHour = it })
            Text(":", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
            NumberWheelPicker(range = 0..59, onValueChange = { selectedMin = it })
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            days.forEachIndexed { index, day ->
                val dayKey = fullDays[index]
                val isSelected = selectedDays.contains(dayKey)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) NeonGreen else Color.White.copy(0.05f))
                        .clickable {
                            if (isSelected) selectedDays.remove(dayKey) else selectedDays.add(dayKey)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(day, color = if (isSelected) AmoledBlack else Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = label,
            onValueChange = { label = it },
            label = { Text("Nombre de la alarma", color = GrayText) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = NeonGreen
            ),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { tonePicker.launch("audio/*") },
            color = Color.White.copy(0.05f),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color.White.copy(0.1f))
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.MusicNote, null, tint = NeonGreen)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Sonido de alarma", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(toneName, color = GrayText, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Rounded.ChevronRight, null, tint = GrayText)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                val diasStr = selectedDays.joinToString(",")
                onSave(selectedHour, selectedMin, diasStr, label, selectedToneUri)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
            shape = RoundedCornerShape(30.dp)
        ) {
            Text("Guardar", color = AmoledBlack, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun NumberWheelPicker(range: IntRange, initialValue: Int = 0, onValueChange: (Int) -> Unit) {
    val state = rememberLazyListState(initialFirstVisibleItemIndex = 500 - (500 % (range.last + 1)) + initialValue)

    LaunchedEffect(state.isScrollInProgress) {
        if (!state.isScrollInProgress) {
            onValueChange(state.firstVisibleItemIndex % (range.last + 1))
        }
    }

    Box(
        modifier = Modifier
            .width(80.dp)
            .height(150.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(50.dp).border(1.dp, NeonGreen.copy(0.3f), RoundedCornerShape(12.dp)))
        
        LazyColumn(
            state = state,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = state),
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = 50.dp)
        ) {
            items(1000) { index ->
                val num = index % (range.last + 1)
                val isSelected = remember(state) {
                    derivedStateOf { state.firstVisibleItemIndex == index }
                }.value
                val scale by animateFloatAsState(if (isSelected) 1.2f else 0.8f, label = "wheelScale")
                
                Text(
                    text = String.format("%02d", num),
                    fontSize = if (isSelected) 32.sp else 20.sp,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                    color = if (isSelected) Color.White else GrayText.copy(0.2f),
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                )
            }
        }
    }
}

@Composable
fun WorldClockSectionV2() {
    val localTz = TimeZone.getDefault()
    val localCity = localTz.id.split("/").last().replace("_", " ")
    
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    var currentTime by remember { mutableStateOf(timeFormat.format(Date())) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = timeFormat.format(Date())
            delay(1000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Ubicación Actual", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(16.dp))
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = Color.White.copy(0.05f),
            border = BorderStroke(1.dp, BorderGlow)
        ) {
            Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(localCity, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("Hora local (GMT${localTz.rawOffset / 3600000})", color = GrayText, fontSize = 12.sp)
                }
                Text(currentTime, color = NeonGreen, fontSize = 32.sp, fontWeight = FontWeight.Black)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedButton(
            onClick = { /* Buscar ciudad */ },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color.White.copy(0.1f))
        ) {
            Icon(Icons.Default.Add, null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Añadir Ciudad", color = Color.White)
        }
    }
}

@Composable
fun StopwatchSectionV2() {
    var timeMillis by remember { mutableLongStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }
    val laps = remember { mutableStateListOf<Long>() }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            val start = System.currentTimeMillis() - timeMillis
            while (isRunning) {
                timeMillis = System.currentTimeMillis() - start
                delay(10)
            }
        }
    }

    val formatTime = { millis: Long ->
        val mins = (millis / 60000) % 60
        val secs = (millis / 1000) % 60
        val ms = (millis / 10) % 100
        String.format("%02d:%02d.%02d", mins, secs, ms)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(240.dp)
                .drawBehind {
                    drawCircle(Color.White.copy(0.05f), style = Stroke(4.dp.toPx()))
                    if (isRunning) {
                        drawArc(
                            color = NeonGreen,
                            startAngle = -90f,
                            sweepAngle = (timeMillis % 60000) / 60000f * 360f,
                            useCenter = false,
                            style = Stroke(8.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = formatTime(timeMillis),
                fontSize = 44.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { if (isRunning) laps.add(0, timeMillis) else { timeMillis = 0; laps.clear() } }) {
                Icon(if (isRunning) Icons.Rounded.Flag else Icons.Rounded.Refresh, null, tint = Color.White)
            }
            
            Surface(
                onClick = { isRunning = !isRunning },
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                color = if (isRunning) Color.White.copy(0.1f) else NeonGreen
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (isRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        null,
                        tint = if (isRunning) Color.White else AmoledBlack,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            itemsIndexed(laps) { index, lapTime ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Vuelta ${laps.size - index}", color = GrayText)
                    Text(formatTime(lapTime), color = Color.White, fontWeight = FontWeight.Bold)
                }
                HorizontalDivider(color = Color.White.copy(0.05f))
            }
        }
    }
}

@Composable
fun TimerSectionV2() {
    var selectedHours by remember { mutableIntStateOf(0) }
    var selectedMinutes by remember { mutableIntStateOf(0) }
    var selectedSeconds by remember { mutableIntStateOf(0) }

    var durationSecs by remember { mutableLongStateOf(0L) }
    var remainingSecs by remember { mutableLongStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }

    val totalSelectedSecs = (selectedHours * 3600L) + (selectedMinutes * 60L) + selectedSeconds

    LaunchedEffect(isRunning, remainingSecs) {
        if (isRunning && remainingSecs > 0) {
            delay(1000)
            remainingSecs -= 1
        } else if (remainingSecs == 0L) {
            isRunning = false
        }
    }

    val formatTime = { totalSecs: Long ->
        val h = totalSecs / 3600
        val m = (totalSecs % 3600) / 60
        val s = totalSecs % 60
        String.format("%02d:%02d:%02d", h, m, s)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Preview de Tiempo Grande y Elegante
        Text(
            text = if (remainingSecs > 0 || isRunning) formatTime(remainingSecs) else formatTime(totalSelectedSecs),
            fontSize = 54.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Box(
            modifier = Modifier
                .size(260.dp)
                .drawBehind {
                    drawCircle(Color.White.copy(0.03f), style = Stroke(4.dp.toPx()))
                    if (durationSecs > 0) {
                        drawArc(
                            color = NeonGreen,
                            startAngle = -90f,
                            sweepAngle = (remainingSecs.toFloat() / durationSecs) * 360f,
                            useCenter = false,
                            style = Stroke(10.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (!isRunning && remainingSecs == 0L) {
                // Ruedas de selección premium
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NumberWheelPicker(range = 0..23, onValueChange = { selectedHours = it })
                    Text(":", color = NeonGreen, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                    NumberWheelPicker(range = 0..59, onValueChange = { selectedMinutes = it })
                    Text(":", color = NeonGreen, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                    NumberWheelPicker(range = 0..59, onValueChange = { selectedSeconds = it })
                }
            } else {
                // Icono de estado
                Icon(
                    if (isRunning) Icons.Rounded.HourglassTop else Icons.Rounded.HourglassEmpty,
                    null,
                    tint = NeonGreen.copy(0.2f),
                    modifier = Modifier.size(120.dp).blur(4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Presets Rápidos
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(300L, 600L, 1500L, 3600L).forEach { secs ->
                val label = when(secs) {
                    300L -> "5m"
                    600L -> "10m"
                    1500L -> "25m"
                    else -> "1h"
                }
                AssistChip(
                    onClick = { 
                        selectedHours = (secs / 3600).toInt()
                        selectedMinutes = ((secs % 3600) / 60).toInt()
                        selectedSeconds = (secs % 60).toInt()
                        isRunning = false
                        remainingSecs = 0
                    },
                    label = { Text(label, fontWeight = FontWeight.Bold) },
                    colors = AssistChipDefaults.assistChipColors(labelColor = Color.White),
                    border = BorderStroke(1.dp, Color.White.copy(0.1f)),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Controles Modernos
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { 
                    remainingSecs = 0
                    durationSecs = 0
                    isRunning = false 
                },
                modifier = Modifier.size(56.dp).background(Color.White.copy(0.05f), CircleShape)
            ) {
                Icon(Icons.Rounded.Refresh, null, tint = Color.White)
            }

            Surface(
                onClick = { 
                    if (isRunning) {
                        isRunning = false
                    } else {
                        if (remainingSecs == 0L) {
                            durationSecs = totalSelectedSecs
                            remainingSecs = totalSelectedSecs
                        }
                        if (remainingSecs > 0) isRunning = true
                    }
                },
                modifier = Modifier.size(80.dp).shadow(12.dp, CircleShape, spotColor = NeonGreen),
                shape = CircleShape,
                color = if (isRunning) Color.White.copy(0.1f) else NeonGreen
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (isRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        null,
                        tint = if (isRunning) Color.White else AmoledBlack,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SmallControlButton("+1m") {
                        if (isRunning || remainingSecs > 0) {
                            remainingSecs += 60
                            durationSecs += 60
                        }
                    }
                    SmallControlButton("+10s") {
                        if (isRunning || remainingSecs > 0) {
                            remainingSecs += 10
                            durationSecs += 10
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SmallControlButton(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(0.05f),
        border = BorderStroke(1.dp, Color.White.copy(0.1f)),
        modifier = Modifier.height(40.dp).width(56.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SleepSectionV2() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        shape = RoundedCornerShape(32.dp),
        color = Color(0xFF130D1F),
        border = BorderStroke(1.dp, Purple.copy(0.2f))
    ) {
        Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Rounded.Bedtime, null, tint = Purple, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Modo Descanso", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Establece tu rutina de sueño ideal", color = GrayText, fontSize = 13.sp, textAlign = TextAlign.Center)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("DORMIR", color = GrayText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("23:00", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("DESPERTAR", color = GrayText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("07:00", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { /* Configurar descanso */ },
                colors = ButtonDefaults.buttonColors(containerColor = Purple),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Editar Rutina", fontWeight = FontWeight.Bold)
            }
        }
    }
}
