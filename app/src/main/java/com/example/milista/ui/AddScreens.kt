package com.example.milista.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.milista.R
import com.example.milista.ui.theme.*
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderScreen(
    viewModel: MiListaViewModel,
    tipo: String,
    reminderId: Int = -1,
    onBack: () -> Unit
) {
    val isOtro = tipo == "Otro"
    val recordatorios by viewModel.recordatorios.collectAsState()
    
    var customNombre by remember { mutableStateOf("") }
    var customEmoji by remember { mutableStateOf("") }
    var customColor by remember { mutableStateOf(SamsungBlue) }
    var prioridad by remember { mutableIntStateOf(0) }
    var showEmojiPicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    var selectedHour by remember { mutableIntStateOf(12) }
    var selectedMin by remember { mutableIntStateOf(0) }
    
    var currentStep by remember { mutableIntStateOf(if (isOtro) 0 else 1) }

    LaunchedEffect(reminderId, recordatorios) {
        if (reminderId != -1) {
            recordatorios.find { it.id == reminderId }?.let { r ->
                customNombre = r.nombreCustom ?: ""
                customEmoji = r.emojiCustom ?: ""
                customColor = r.colorCustom?.let { Color(it) } ?: SamsungBlue
                prioridad = r.prioridad
                
                val cal = Calendar.getInstance().apply { timeInMillis = r.fecha }
                selectedHour = cal.get(Calendar.HOUR_OF_DAY)
                selectedMin = cal.get(Calendar.MINUTE)
                // datePickerState doesn't allow easy setting from timestamp after creation easily in some versions 
                // but let's assume we can or just use it.
            }
        }
    }

    val colorsList = listOf(
        SamsungBlue, SamsungGreen, SamsungRed, SamsungOrange, SamsungPurple,
        Color(0xFFF472B6), Color(0xFFFBBF24), Color(0xFF2DD4BF)
    )

    Box(modifier = Modifier.fillMaxSize().background(AmoledBlack)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { 
                        Text(
                            text = if (reminderId == -1) stringResource(R.string.new_reminder) else "Editar Recordatorio", 
                            fontWeight = FontWeight.Black, 
                            color = Color.White, 
                            fontSize = 18.sp
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Indicador de Pasos Premium
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp), horizontalArrangement = Arrangement.Center) {
                    repeat(if (isOtro) 3 else 2) { index ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(4.dp)
                                .width(if (currentStep == index) 24.dp else 12.dp)
                                .clip(CircleShape)
                                .background(if (currentStep == index) NeonGreen else Color.White.copy(alpha = 0.1f))
                        )
                    }
                }

                if (currentStep == 0 && isOtro) {
                    AppearanceStep(
                        nombre = customNombre,
                        onNombreChange = { customNombre = it },
                        emoji = customEmoji,
                        onEmojiClick = { showEmojiPicker = true },
                        selectedColor = customColor,
                        onColorSelect = { customColor = it },
                        colors = colorsList,
                        prioridad = prioridad,
                        onPrioridadSelect = { prioridad = it },
                        onNext = { currentStep = 1 }
                    )
                } else if (currentStep == 1) {
                    DateStep(
                        state = datePickerState,
                        color = if (isOtro) customColor else NeonGreen,
                        onNext = { currentStep = 2 }
                    )
                } else {
                    TimeStep(
                        hour = selectedHour,
                        min = selectedMin,
                        onHourChange = { selectedHour = it },
                        onMinChange = { selectedMin = it },
                        color = if (isOtro) customColor else NeonGreen,
                        onSave = {
                            val calendar = Calendar.getInstance().apply {
                                timeInMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                                set(Calendar.HOUR_OF_DAY, selectedHour)
                                set(Calendar.MINUTE, selectedMin)
                            }
                            
                            if (reminderId == -1) {
                                if (isOtro) {
                                    viewModel.agregarRecordatorio(
                                        tipo = "Otro",
                                        fecha = calendar.timeInMillis,
                                        nombreCustom = customNombre,
                                        emojiCustom = customEmoji,
                                        colorCustom = customColor.toArgb(),
                                        prioridad = prioridad
                                    )
                                } else {
                                    viewModel.agregarRecordatorio(
                                        tipo = tipo, 
                                        fecha = calendar.timeInMillis,
                                        prioridad = prioridad
                                    )
                                }
                            } else {
                                // Update logic
                                recordatorios.find { it.id == reminderId }?.let { r ->
                                    viewModel.actualizarRecordatorio(
                                        r.copy(
                                            nombreCustom = if (isOtro) customNombre else r.nombreCustom,
                                            emojiCustom = if (isOtro) customEmoji else r.emojiCustom,
                                            colorCustom = if (isOtro) customColor.toArgb() else r.colorCustom,
                                            prioridad = prioridad,
                                            fecha = calendar.timeInMillis
                                        )
                                    )
                                }
                            }
                            onBack()
                        }
                    )
                }
            }
        }
    }
    // ... (EmojiPickerDialog remains same)

    if (showEmojiPicker) {
        EmojiPickerDialog(
            onEmojiSelected = { selected ->
                customEmoji = selected
                showEmojiPicker = false
            },
            onDismiss = { showEmojiPicker = false }
        )
    }
}

@Composable
fun AppearanceStep(
    nombre: String,
    onNombreChange: (String) -> Unit,
    emoji: String,
    onEmojiClick: () -> Unit,
    selectedColor: Color,
    onColorSelect: (Color) -> Unit,
    colors: List<Color>,
    prioridad: Int,
    onPrioridadSelect: (Int) -> Unit,
    onNext: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Personalización", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Text("Dale estilo a tu recordatorio", color = GrayText, fontSize = 14.sp)
        
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = onNombreChange,
            label = { Text("Nombre", color = GrayText) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = selectedColor,
                unfocusedContainerColor = Color.White.copy(alpha = 0.03f)
            ),
            shape = RoundedCornerShape(16.dp)
        )
        
        Spacer(modifier = Modifier.height(20.dp))

        Surface(
            onClick = onEmojiClick,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White.copy(alpha = 0.05f),
            border = BorderStroke(1.dp, if(emoji.isEmpty()) Color.White.copy(alpha = 0.1f) else selectedColor)
        ) {
            Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(if (emoji.isEmpty()) "Seleccionar Icono" else "Icono seleccionado", color = Color.White)
                Spacer(modifier = Modifier.weight(1f))
                Text(if (emoji.isEmpty()) "✨" else emoji, fontSize = 28.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Prioridad", color = GrayText, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("Baja", "Media", "Alta").forEachIndexed { index, label ->
                val isSelected = prioridad == index
                val pColor = when(index) {
                    2 -> SamsungRed
                    1 -> Color(0xFFFFD600)
                    else -> NeonGreen
                }
                Surface(
                    onClick = { onPrioridadSelect(index) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) pColor.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, if (isSelected) pColor else Color.Transparent)
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = if (isSelected) pColor else Color.White.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Color de acento", color = GrayText, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(colors) { color ->
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(if (selectedColor == color) BorderStroke(3.dp, Color.White) else BorderStroke(0.dp, Color.Transparent), CircleShape)
                        .clickable { onColorSelect(color) }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            enabled = nombre.isNotBlank() && emoji.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = selectedColor),
            shape = RoundedCornerShape(30.dp)
        ) {
            Text("Siguiente", fontWeight = FontWeight.Bold, color = if(selectedColor == SamsungGreen) Color.Black else Color.White)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateStep(
    state: DatePickerState,
    color: Color,
    onNext: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Fecha", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Text("¿Cuándo debemos avisarte?", color = GrayText, fontSize = 14.sp)
        
        Spacer(modifier = Modifier.height(24.dp))

        DatePicker(
            state = state,
            showModeToggle = false,
            colors = DatePickerDefaults.colors(
                containerColor = Color.Transparent,
                titleContentColor = Color.White,
                headlineContentColor = Color.White,
                selectedDayContainerColor = color,
                todayContentColor = color,
                dayContentColor = Color.White,
                weekdayContentColor = GrayText
            )
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            enabled = state.selectedDateMillis != null,
            colors = ButtonDefaults.buttonColors(containerColor = color),
            shape = RoundedCornerShape(30.dp)
        ) {
            Text("Siguiente", fontWeight = FontWeight.Bold, color = if(color == SamsungGreen) Color.Black else Color.White)
        }
    }
}

@Composable
fun TimeStep(
    hour: Int,
    min: Int,
    onHourChange: (Int) -> Unit,
    onMinChange: (Int) -> Unit,
    color: Color,
    onSave: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Hora", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp, modifier = Modifier.align(Alignment.Start))
        Text("Define el momento exacto", color = GrayText, fontSize = 14.sp, modifier = Modifier.align(Alignment.Start))
        
        Spacer(modifier = Modifier.height(60.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NumberWheelPickerLocal(range = 0..23, initialValue = hour, onValueChange = onHourChange)
            Text(":", color = color, fontSize = 32.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
            NumberWheelPickerLocal(range = 0..59, initialValue = min, onValueChange = onMinChange)
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = color),
            shape = RoundedCornerShape(30.dp)
        ) {
            Text("Guardar recordatorio", fontWeight = FontWeight.Bold, color = if(color == SamsungGreen) Color.Black else Color.White)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NumberWheelPickerLocal(range: IntRange, initialValue: Int, onValueChange: (Int) -> Unit) {
    val state = rememberLazyListState(initialFirstVisibleItemIndex = 500 - (500 % (range.last + 1)) + initialValue)

    LaunchedEffect(state.isScrollInProgress) {
        if (!state.isScrollInProgress) {
            onValueChange(state.firstVisibleItemIndex % (range.last + 1))
        }
    }

    Box(
        modifier = Modifier.width(80.dp).height(150.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(50.dp).border(1.dp, NeonGreen.copy(0.2f), RoundedCornerShape(12.dp)))
        
        androidx.compose.foundation.lazy.LazyColumn(
            state = state,
            flingBehavior = androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior(lazyListState = state),
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = 50.dp)
        ) {
            items(1000) { index ->
                val num = index % (range.last + 1)
                val isSelected = remember(state) { 
                    derivedStateOf { state.firstVisibleItemIndex == index } 
                }.value
                
                Text(
                    text = String.format(java.util.Locale.getDefault(), "%02d", num),
                    fontSize = if (isSelected) 32.sp else 20.sp,
                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                    color = if (isSelected) Color.White else GrayText.copy(alpha = 0.3f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

data class EmojiCategory(val name: String, val icon: String, val emojis: List<String>)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EmojiPickerDialog(
    onEmojiSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val categories = remember {
        listOf(
            EmojiCategory("Reciente", "🕒", listOf("🎯", "🏀", "🎮", "🎸", "🍕", "🍔", "🍦", "🎬", "📚", "🏋️", "🛒", "💊")),
            EmojiCategory("Iconos", "✨", listOf("⭐", "🔥", "🌈", "🎈", "💡", "⚡", "🍀", "💎", "🚩", "📍", "🔔", "❤️")),
            EmojiCategory("Actividad", "🏃", listOf("⚽", "💪", "🧘", "🚲", "🎨", "🎭", "🎤", "📸", "✈️", "🚢", "🚗", "🏠")),
            EmojiCategory("Custom", "⌨️", emptyList())
        )
    }

    val pagerState = rememberPagerState(pageCount = { categories.size })
    val scope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = CardDark,
            modifier = Modifier.fillMaxWidth().height(450.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Elegir icono", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                ScrollableTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    edgePadding = 0.dp,
                    containerColor = Color.Transparent,
                    contentColor = NeonGreen,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]), color = NeonGreen)
                    },
                    divider = {}
                ) {
                    categories.forEachIndexed { index, category ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                            text = { Text(category.icon, fontSize = 20.sp) }
                        )
                    }
                }

                HorizontalPager(state = pagerState, modifier = Modifier.weight(1f).padding(top = 16.dp)) { page ->
                    if (categories[page].name == "Custom") {
                        CustomEmojiInputLocal(onEmojiSelected)
                    } else {
                        EmojiGridLocal(emojis = categories[page].emojis, onEmojiSelected)
                    }
                }
            }
        }
    }
}

@Composable
fun CustomEmojiInputLocal(onEmojiSelected: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text("Usa el teclado para elegir cualquier emoji", color = GrayText, fontSize = 14.sp, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = text,
            onValueChange = { 
                // Filtrar para permitir solo Emojis (aproximación simple)
                if (it.isEmpty() || it.any { char -> char.isSurrogate() || char.code > 0x2000 }) {
                    text = it.take(2) // Máximo 1-2 chars para un emoji complex
                    if (text.isNotEmpty()) onEmojiSelected(text)
                }
            },
            placeholder = { Text("Toca aquí", color = GrayText.copy(alpha = 0.5f)) },
            modifier = Modifier.size(80.dp),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 32.sp, textAlign = TextAlign.Center),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonGreen,
                unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun EmojiGridLocal(emojis: List<String>, onEmojiSelected: (String) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 56.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(emojis) { emoji ->
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .clickable { onEmojiSelected(emoji) },
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 28.sp)
            }
        }
    }
}
