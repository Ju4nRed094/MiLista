package com.example.milista.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import com.example.milista.ui.utils.getTranslatedText
import com.example.milista.ui.utils.getLocaleCode
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.milista.ui.theme.*
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderScreen(
    viewModel: MiListaViewModel,
    tipo: String,
    onBack: () -> Unit
) {
    val isOtro = tipo == "Otro"
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val localeCode = remember(selectedLanguage) { getLocaleCode(selectedLanguage) }
    
    var customNombre by remember { mutableStateOf("") }
    var customEmoji by remember { mutableStateOf("") }
    var customColor by remember { mutableStateOf(SamsungBlue) }
    var showEmojiPicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState()
    var currentStep by remember { mutableIntStateOf(if (isOtro) 0 else 1) }

    val colorsList = listOf(
        SamsungBlue, SamsungGreen, SamsungRed, SamsungOrange, SamsungPurple,
        Color(0xFFF472B6), Color(0xFFFBBF24), Color(0xFF2DD4BF),
        Color(0xFF60A5FA), Color(0xFF34D399), Color(0xFFA78BFA), Color(0xFF38BDF8)
    )

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = { Text(getTranslatedText("Configurar Aviso", selectedLanguage), fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
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
            
            if (currentStep == 0 && isOtro) {
                Text(
                    getTranslatedText("Personaliza tu aviso", selectedLanguage),
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = customNombre,
                    onValueChange = { customNombre = it },
                    label = { Text(getTranslatedText("Nombre del evento", selectedLanguage), color = GrayText) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = customColor,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.03f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(hintLocales = LocaleList(Locale(localeCode)))
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth().height(64.dp).clickable { showEmojiPicker = true },
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, if(customEmoji.isEmpty()) Color.White.copy(alpha = 0.1f) else customColor)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (customEmoji.isEmpty()) getTranslatedText("Seleccionar Icono", selectedLanguage) else getTranslatedText("Icono seleccionado", selectedLanguage),
                            color = if (customEmoji.isEmpty()) GrayText else Color.White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(if (customEmoji.isEmpty()) "🔘" else customEmoji, fontSize = 28.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Text(getTranslatedText("Color de categoría", selectedLanguage), color = GrayText, modifier = Modifier.align(Alignment.Start), fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    items(colorsList) { color ->
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(if (customColor == color) BorderStroke(3.dp, Color.White) else BorderStroke(0.dp, Color.Transparent), CircleShape)
                                .clickable { customColor = color }
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { currentStep = 1 },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    enabled = customNombre.isNotBlank() && customEmoji.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = customColor),
                    shape = RoundedCornerShape(30.dp)
                ) {
                    Text(getTranslatedText("Siguiente", selectedLanguage), fontWeight = FontWeight.Bold, color = if(customColor == SamsungGreen) Color.Black else Color.White)
                }
            } else if (currentStep == 1) {
                val currentColor = if (isOtro) customColor else SamsungBlue
                
                Text(
                    getTranslatedText("Selecciona la fecha", selectedLanguage),
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(20.dp))
                DatePicker(
                    state = datePickerState,
                    showModeToggle = false,
                    colors = DatePickerDefaults.colors(
                        containerColor = BackgroundDark,
                        titleContentColor = Color.White,
                        headlineContentColor = Color.White,
                        selectedDayContainerColor = currentColor,
                        todayContentColor = currentColor,
                        dayContentColor = Color.White,
                        weekdayContentColor = GrayText
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { currentStep = 2 },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    enabled = datePickerState.selectedDateMillis != null,
                    colors = ButtonDefaults.buttonColors(containerColor = currentColor),
                    shape = RoundedCornerShape(30.dp)
                ) {
                    Text(getTranslatedText("Siguiente", selectedLanguage), fontWeight = FontWeight.Bold, color = if(currentColor == SamsungGreen) Color.Black else Color.White)
                }
            } else {
                val currentColor = if (isOtro) customColor else SamsungBlue
                
                Text(
                    getTranslatedText("Selecciona la hora", selectedLanguage),
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(40.dp))
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = Color.White.copy(alpha = 0.05f),
                        selectorColor = currentColor,
                        periodSelectorSelectedContainerColor = currentColor.copy(alpha = 0.2f),
                        timeSelectorSelectedContainerColor = currentColor.copy(alpha = 0.2f),
                        timeSelectorSelectedContentColor = Color.White,
                        timeSelectorUnselectedContainerColor = Color.White.copy(0.05f),
                        timeSelectorUnselectedContentColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        val calendar = Calendar.getInstance().apply {
                            timeInMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                            set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                            set(Calendar.MINUTE, timePickerState.minute)
                        }
                        
                        if (isOtro) {
                            viewModel.agregarRecordatorio(
                                tipo = "Otro",
                                fecha = calendar.timeInMillis,
                                nombreCustom = customNombre,
                                emojiCustom = customEmoji,
                                colorCustom = customColor.toArgb()
                            )
                        } else {
                            viewModel.agregarRecordatorio(tipo, calendar.timeInMillis)
                        }
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = currentColor),
                    shape = RoundedCornerShape(30.dp)
                ) {
                    Text(getTranslatedText("Guardar Evento", selectedLanguage), fontWeight = FontWeight.Bold, color = if(currentColor == SamsungGreen) Color.Black else Color.White)
                }
            }
        }
    }

    if (showEmojiPicker) {
        EmojiPickerDialog(
            viewModel = viewModel,
            onEmojiSelected = { selected ->
                customEmoji = selected
                showEmojiPicker = false
            },
            onDismiss = { showEmojiPicker = false }
        )
    }
}

data class EmojiCategory(val name: String, val icon: String, val emojis: List<String>)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EmojiPickerDialog(
    viewModel: MiListaViewModel,
    onEmojiSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val categories = remember {
        listOf(
            EmojiCategory("Recientes", "🕒", listOf("🎯", "🏀", "🎮", "🎸", "🍕", "🍔", "🍦", "🎬", "🎨", "🎭", "🎤", "❤️", "🔥", "✨", "🌟")),
            EmojiCategory("Caras", "😀", listOf("😀", "😃", "😄", "😁", "😆", "😅", "😂", "😊", "😇", "🙂", "😉", "😍", "🥰", "😘", "😋")),
            EmojiCategory("Animales", "🐶", listOf("🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼", "🐨", "🐯", "🦁", "🐮", "🐷", "🐽", "🐸")),
            EmojiCategory("Actividad", "⚽", listOf("⚽", "🏀", "🏈", "⚾", "🎾", "🏐", "🏉", "🎱", "🏓", "🏸", "🥅", "🏒", "⛳", "🏹", "🎣"))
        )
    }

    var searchQuery by remember { mutableStateOf("") }
    val pagerState = rememberPagerState(pageCount = { categories.size })
    val scope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = CardDark,
            modifier = Modifier.fillMaxWidth().height(550.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            val localeCode = remember(selectedLanguage) { getLocaleCode(selectedLanguage) }
            Column(modifier = Modifier.padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(getTranslatedText("Elige un Icono", selectedLanguage), style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null, tint = Color.White) }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(getTranslatedText("Buscar...", selectedLanguage), color = GrayText) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = GrayText) },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        unfocusedContainerColor = Color.White.copy(alpha = 0.03f)
                    ),
                    keyboardOptions = KeyboardOptions(hintLocales = LocaleList(Locale(localeCode)))
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (searchQuery.isEmpty()) {
                    ScrollableTabRow(
                        selectedTabIndex = pagerState.currentPage,
                        edgePadding = 0.dp,
                        containerColor = Color.Transparent,
                        contentColor = SamsungGreen,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]), color = SamsungGreen)
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

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                        EmojiGrid(emojis = categories[page].emojis, onEmojiSelected)
                    }
                } else {
                    val filteredEmojis = remember(searchQuery) {
                        categories.flatMap { it.emojis }.distinct().take(100)
                    }
                    Box(modifier = Modifier.weight(1f)) { EmojiGrid(emojis = filteredEmojis, onEmojiSelected) }
                }
            }
        }
    }
}

@Composable
fun EmojiGrid(emojis: List<String>, onEmojiSelected: (String) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 48.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(emojis) { emoji ->
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .clickable { onEmojiSelected(emoji) },
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 24.sp)
            }
        }
    }
}
