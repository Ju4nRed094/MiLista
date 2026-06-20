package com.example.milista.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.alpha
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
import androidx.compose.ui.window.Dialog
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
        containerColor = AmoledBlack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ClockHeaderPremium(alarmas)

            ClockTabsPremium(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        fadeIn(tween(300)) + scaleIn(initialScale = 0.98f) togetherWith 
                        fadeOut(tween(300))
                    },
                    label = "clockContent"
                ) { targetTab ->
                    when (targetTab) {
                        "Alarmas" -> AlarmListSectionPremium(alarmas, viewModel, onAddNew = { showAddAlarm = true })
                        "Temporizador" -> TimerSectionPremium()
                        "Cronómetro" -> StopwatchSectionPremium()
                        "Reloj mundial" -> WorldClockSectionPremium()
                    }
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
                AddAlarmSheetPremium(
                    onDismiss = { showAddAlarm = false },
                    onSave = { hora, min, seg, dias, vibrar ->
                        viewModel.agregarAlarma(hora, min, dias, "")
                        showAddAlarm = false
                    }
                )
            }
        }
    }
}

@Composable
fun ClockHeaderPremium(alarmas: List<Alarma>) {
    val activeAlarms = alarmas.filter { it.activa }
    val nextAlarm = activeAlarms.minByOrNull { it.hora * 60 + it.minuto }
    
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while(true) {
            currentTime = System.currentTimeMillis()
            delay(1000)
        }
    }

    val remainingText = remember(nextAlarm, currentTime) {
        nextAlarm?.let {
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, it.hora)
                set(Calendar.MINUTE, it.minuto)
                set(Calendar.SECOND, 0)
                if (before(now)) add(Calendar.DATE, 1)
            }
            val diff = target.timeInMillis - now.timeInMillis
            val h = diff / 3600000
            val m = (diff % 3600000) / 60000
            "Faltan: ${h}h ${m}min"
        } ?: "Crea tu primera alarma"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Text(
            text = "Alarmas",
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = (-1).sp
            )
        )
        Text(
            text = "${activeAlarms.size} alarmas activas",
            color = NeonGreen,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        if (nextAlarm != null) {
            Text(
                text = "Próxima: ${String.format("%02d:%02d", nextAlarm.hora, nextAlarm.minuto)} • $remainingText",
                color = GrayText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        } else {
            Text(
                text = remainingText,
                color = GrayText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ClockTabsPremium(selectedTab: String, onTabSelected: (String) -> Unit) {
    val tabs = listOf("Alarmas", "Temporizador", "Cronómetro", "Reloj mundial")
    
    ScrollableTabRow(
        selectedTabIndex = tabs.indexOf(selectedTab),
        containerColor = Color.Transparent,
        contentColor = NeonGreen,
        edgePadding = 24.dp,
        divider = {},
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[tabs.indexOf(selectedTab)]),
                color = NeonGreen
            )
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
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) NeonGreen else GrayText
                    )
                }
            )
        }
    }
}

@Composable
fun AlarmListSectionPremium(alarmas: List<Alarma>, viewModel: MiListaViewModel, onAddNew: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 100.dp, start = 24.dp, end = 24.dp, top = 16.dp)
    ) {
        item {
            Button(
                onClick = onAddNew,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.2f))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, null, tint = NeonGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Nueva alarma", color = NeonGreen, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(alarmas) { alarma ->
            AlarmItemCardPremium(
                alarma = alarma,
                onToggle = { viewModel.actualizarAlarma(alarma.copy(activa = it)) },
                onDelete = { viewModel.borrarAlarma(alarma) }
            )
        }
    }
}

@Composable
fun AlarmItemCardPremium(alarma: Alarma, onToggle: (Boolean) -> Unit, onDelete: () -> Unit) {
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(alarma.activa) {
        while(alarma.activa) {
            currentTime = System.currentTimeMillis()
            delay(1000)
        }
    }

    val remainingText = remember(alarma, currentTime) {
        if (!alarma.activa) "" else {
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, alarma.hora)
                set(Calendar.MINUTE, alarma.minuto)
                set(Calendar.SECOND, 0)
                if (before(now)) add(Calendar.DATE, 1)
            }
            val diff = target.timeInMillis - now.timeInMillis
            val h = diff / 3600000
            val m = (diff % 3600000) / 60000
            "Faltan ${h}h ${m}min"
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = CardDark.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, if (alarma.activa) BorderGlow else Color.White.copy(0.05f))
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = String.format("%02d:%02d", alarma.hora, alarma.minuto),
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    color = if (alarma.activa) Color.White else GrayText
                )
                Text(
                    text = alarma.dias.ifEmpty { "Sin repetición" }.replace(",", " • "),
                    fontSize = 13.sp,
                    color = if (alarma.activa) NeonGreen.copy(0.8f) else GrayText.copy(0.5f)
                )
                if (alarma.activa) {
                    Text(remainingText, color = GrayText, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Sonido: Alarma predeterminada (alarma.mp3)",
                    fontSize = 10.sp,
                    color = GrayText.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Bold
                )
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

            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, null, tint = Color.White.copy(alpha = 0.1f))
            }
        }
    }
}

@Composable
fun AddAlarmSheetPremium(onDismiss: () -> Unit, onSave: (Int, Int, Int, String, Boolean) -> Unit) {
    var h by remember { mutableIntStateOf(7) }
    var m by remember { mutableIntStateOf(30) }
    var s by remember { mutableIntStateOf(0) }
    
    val selectedDays = remember { mutableStateListOf<String>() }
    val days = listOf("L", "M", "M", "J", "V", "S", "D")
    val fullDays = listOf("Lun", "Mar", "Mie", "Jue", "Vie", "Sab", "Dom")
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Configurar Alarma", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
        
        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            WheelPickerPremium(range = 0..23, initialValue = h, onValueChange = { h = it })
            Text(":", color = NeonGreen, fontSize = 32.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 24.dp))
            WheelPickerPremium(range = 0..59, initialValue = m, onValueChange = { m = it })
            Text(":", color = NeonGreen, fontSize = 32.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 24.dp))
            WheelPickerPremium(range = 0..59, initialValue = s, onValueChange = { s = it })
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
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) NeonGreen else Color.White.copy(alpha = 0.05f))
                        .clickable { if (isSelected) selectedDays.remove(dayKey) else selectedDays.add(dayKey) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(day, color = if (isSelected) AmoledBlack else Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White.copy(alpha = 0.03f),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Vibration, null, tint = NeonGreen)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Vibración", color = Color.White, modifier = Modifier.weight(1f))
                Switch(checked = true, onCheckedChange = {})
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = { onSave(h, m, s, selectedDays.joinToString(","), true) },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
            shape = RoundedCornerShape(32.dp)
        ) {
            Text("Guardar alarma", color = AmoledBlack, fontWeight = FontWeight.Black, fontSize = 16.sp)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WheelPickerPremium(
    modifier: Modifier = Modifier,
    range: IntRange,
    initialValue: Int,
    onValueChange: (Int) -> Unit
) {
    val itemHeight = 72.dp
    val totalHeight = 280.dp
    val paddingValues = PaddingValues(vertical = (totalHeight - itemHeight) / 2)
    
    val listItems = remember(range) { range.toList() }
    val factor = 1000
    val totalItems = listItems.size * factor
    val initialIndex = (totalItems / 2) - ((totalItems / 2) % listItems.size) + initialValue
    
    val state = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = state)
    
    val currentSelection by remember {
        derivedStateOf {
            val layoutInfo = state.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) listItems[initialValue % listItems.size]
            else {
                val center = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                val closest = visibleItems.minByOrNull { Math.abs((it.offset + it.size / 2) - center) }
                listItems[(closest?.index ?: 0) % listItems.size]
            }
        }
    }
    
    LaunchedEffect(currentSelection) {
        onValueChange(currentSelection)
    }

    Box(
        modifier = modifier
            .height(totalHeight)
            .width(72.dp),
        contentAlignment = Alignment.Center
    ) {
        // Selection Box (PASO 6: Pure Box, no shadow, no surface)
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF8EFF2A).copy(alpha = 0.12f))
                .border(1.dp, Color(0xFF8EFF2A), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {}

        LazyColumn(
            state = state,
            flingBehavior = snapBehavior,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = paddingValues
        ) {
            items(totalItems) { index ->
                val num = listItems[index % listItems.size]
                val isSelected = remember {
                    derivedStateOf {
                        val layoutInfo = state.layoutInfo
                        val visibleItems = layoutInfo.visibleItemsInfo
                        if (visibleItems.isEmpty()) false
                        else {
                            val center = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                            val closest = visibleItems.minByOrNull { Math.abs((it.offset + it.size / 2) - center) }
                            closest?.index == index
                        }
                    }
                }.value
                
                val scale by animateFloatAsState(if (isSelected) 1.3f else 0.8f, label = "scale")
                val alpha by animateFloatAsState(if (isSelected) 1f else 0.35f, label = "alpha")

                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = String.format("%02d", num),
                        fontSize = 38.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                        color = Color.White,
                        modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha)
                    )
                }
            }
        }
    }
}

@Composable
fun TimerSectionPremium() {
    var h by remember { mutableIntStateOf(0) }
    var m by remember { mutableIntStateOf(10) }
    var s by remember { mutableIntStateOf(0) }
    
    var isRunning by remember { mutableStateOf(false) }
    var totalSecs by remember { mutableLongStateOf(0L) }
    var remainingSecs by remember { mutableLongStateOf(0L) }

    LaunchedEffect(isRunning, remainingSecs) {
        if (isRunning && remainingSecs > 0) {
            delay(1000)
            remainingSecs -= 1
        } else if (remainingSecs == 0L) {
            isRunning = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(300.dp),
            contentAlignment = Alignment.Center
        ) {
            val progress = if (totalSecs > 0) remainingSecs.toFloat() / totalSecs else 1f
            val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(1000, easing = LinearEasing), label = "timerProgress")

            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(Color.White.copy(alpha = 0.05f), style = Stroke(6.dp.toPx()))
                drawArc(
                    color = NeonGreen,
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    style = Stroke(8.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (!isRunning && remainingSecs == 0L) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        WheelPickerPremium(range = 0..23, initialValue = h, onValueChange = { h = it })
                        Text(":", color = NeonGreen, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                        WheelPickerPremium(range = 0..59, initialValue = m, onValueChange = { m = it })
                        Text(":", color = NeonGreen, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                        WheelPickerPremium(range = 0..59, initialValue = s, onValueChange = { s = it })
                    }
                } else {
                    Text(
                        text = String.format("%02d:%02d:%02d", remainingSecs / 3600, (remainingSecs % 3600) / 60, remainingSecs % 60),
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text("Restante", color = GrayText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            ClockButtonLarge(
                label = "Reiniciar", 
                icon = Icons.Rounded.Refresh, 
                color = Color.White.copy(alpha = 0.05f),
                onClick = { 
                    isRunning = false
                    remainingSecs = 0
                    totalSecs = 0
                }
            )

            ClockButtonLarge(
                label = if (isRunning) "Pausar" else "Iniciar",
                icon = if (isRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                color = if (isRunning) Color.White.copy(alpha = 0.1f) else NeonGreen,
                onClick = { 
                    if (isRunning) {
                        isRunning = false
                    } else {
                        if (remainingSecs == 0L) {
                            totalSecs = (h * 3600L) + (m * 60L) + s
                            remainingSecs = totalSecs
                        }
                        if (remainingSecs > 0) isRunning = true
                    }
                }
            )
        }
    }
}

@Composable
fun ClockButtonLarge(label: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(height = 64.dp, width = 140.dp).shadow(if (color == NeonGreen) 12.dp else 0.dp, CircleShape, spotColor = NeonGreen),
        shape = RoundedCornerShape(32.dp),
        color = color
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = if (color == NeonGreen) AmoledBlack else Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, color = if (color == NeonGreen) AmoledBlack else Color.White, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun StopwatchSectionPremium() {
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
        val m = (millis / 60000) % 60
        val s = (millis / 1000) % 60
        val ms = (millis / 10) % 100
        String.format("%02d:%02d", m, s) to String.format("%02d", ms)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(260.dp).drawBehind {
                drawCircle(Color.White.copy(alpha = 0.03f), style = Stroke(4.dp.toPx()))
                if (isRunning) {
                    drawArc(
                        color = NeonGreen,
                        startAngle = -90f,
                        sweepAngle = (timeMillis % 60000) / 60000f * 360f,
                        useCenter = false,
                        style = Stroke(6.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            },
            contentAlignment = Alignment.Center
        ) {
            val (main, ms) = formatTime(timeMillis)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = main, fontSize = 72.sp, fontWeight = FontWeight.Black, color = Color.White)
                Text(text = ms, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = NeonGreen, modifier = Modifier.padding(bottom = 14.dp, start = 4.dp))
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { if (isRunning) laps.add(0, timeMillis) else { timeMillis = 0; laps.clear() } },
                modifier = Modifier.size(56.dp).background(Color.White.copy(alpha = 0.05f), CircleShape)
            ) {
                Icon(if (isRunning) Icons.Rounded.Flag else Icons.Rounded.Refresh, null, tint = Color.White)
            }
            
            Surface(
                onClick = { isRunning = !isRunning },
                modifier = Modifier.size(80.dp).shadow(16.dp, CircleShape, spotColor = NeonGreen),
                shape = CircleShape,
                color = if (isRunning) Color.White.copy(alpha = 0.1f) else NeonGreen
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(if (isRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, null, tint = if (isRunning) Color.White else AmoledBlack, modifier = Modifier.size(36.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(laps.size) { index ->
                val lapTime = laps[index]
                val (lMain, lMs) = formatTime(lapTime)
                Surface(
                    modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.03f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Vuelta ${laps.size - index}", color = GrayText, fontWeight = FontWeight.Bold)
                        Text("$lMain.$lMs", color = Color.White, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun WorldClockSectionPremium() {
    val tz = TimeZone.getDefault()
    val city = tz.id.split("/").last().replace("_", " ")
    val country = Locale.getDefault().displayCountry
    val gmt = "GMT " + (tz.rawOffset / 3600000).let { if (it >= 0) "+$it" else it.toString() }
    
    var time by remember { mutableStateOf(SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())) }
    var date by remember { mutableStateOf(SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())) }
    
    var showCitySearch by remember { mutableStateOf(false) }
    val addedCities = remember { mutableStateListOf<TimeZone>() }

    LaunchedEffect(Unit) {
        while (true) {
            time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            date = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())
            delay(1000)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Tu ubicación", color = NeonGreen, fontWeight = FontWeight.Black, fontSize = 14.sp)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        WorldClockCard(city, country, gmt, time.substring(0, 5), date, true)
        
        if (addedCities.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text("Mis ciudades", color = GrayText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(addedCities) { zone ->
                    val cTime = SimpleDateFormat("HH:mm", Locale.getDefault()).apply { timeZone = zone }.format(Date())
                    val cName = zone.id.split("/").last().replace("_", " ")
                    val cGmt = "GMT " + (zone.rawOffset / 3600000).let { if (it >= 0) "+$it" else it.toString() }
                    WorldClockCard(cName, "", cGmt, cTime, "", false)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = { showCitySearch = true },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
            shape = RoundedCornerShape(30.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Icon(Icons.Default.Add, null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Añadir ciudad", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }

    if (showCitySearch) {
        CitySearchDialog(onDismiss = { showCitySearch = false }, onCitySelected = { addedCities.add(it); showCitySearch = false })
    }
}

@Composable
fun WorldClockCard(city: String, country: String, gmt: String, time: String, date: String, isLocal: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = if (isLocal) CardDark else Color.White.copy(alpha = 0.03f),
        border = BorderStroke(1.dp, if (isLocal) BorderGlow else Color.White.copy(0.05f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(city, fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
                    if (country.isNotEmpty()) Text(country, fontSize = 14.sp, color = GrayText)
                    Text(gmt, fontSize = 12.sp, color = NeonGreen, fontWeight = FontWeight.Bold)
                }
                Text(time, fontSize = 48.sp, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = (-2).sp)
            }
            if (date.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(date.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrayText, letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
fun CitySearchDialog(onDismiss: () -> Unit, onCitySelected: (TimeZone) -> Unit) {
    val cities = listOf(
        "Asia/Tokyo", "Europe/London", "America/New_York", "Europe/Paris", 
        "America/Argentina/Buenos_Aires", "Europe/Madrid", "America/Santiago"
    ).map { TimeZone.getTimeZone(it) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = CardDark,
            modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Buscar ciudad", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(cities) { zone ->
                        Surface(
                            onClick = { onCitySelected(zone) },
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White.copy(alpha = 0.05f)
                        ) {
                            Text(
                                zone.id.split("/").last().replace("_", " "), 
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
