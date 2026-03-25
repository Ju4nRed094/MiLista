package com.example.milista.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.milista.ui.theme.FondoCard
import com.example.milista.ui.theme.FondoPrincipal
import com.example.milista.ui.theme.TextoPrincipal
import com.example.milista.ui.theme.TextoSecundario
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    viewModel: MiListaViewModel,
    listaId: Int,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Nueva Tarea") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("¿Qué quieres hacer?") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        viewModel.agregarTarea(title, listaId)
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Tarea")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderScreen(
    viewModel: MiListaViewModel,
    tipo: String,
    onBack: () -> Unit
) {
    val isOtro = tipo == "Otro"
    
    var customNombre by remember { mutableStateOf("") }
    var customEmoji by remember { mutableStateOf("") }
    var customColor by remember { mutableStateOf(Color(0xFF38BDF8)) }
    var showEmojiPicker by remember { mutableStateOf(false) }

    val typeInfo = ReminderConstants.getByType(tipo)
    
    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                return utcTimeMillis >= calendar.timeInMillis
            }
        }
    )
    
    val timePickerState = rememberTimePickerState()
    var currentStep by remember { mutableIntStateOf(if (isOtro) 0 else 1) }

    val colorsList = listOf(
        Color(0xFFF472B6), Color(0xFF818CF8), Color(0xFFFBBF24),
        Color(0xFFFB7185), Color(0xFF2DD4BF), Color(0xFF60A5FA),
        Color(0xFF34D399), Color(0xFFA78BFA), Color(0xFF38BDF8)
    )

    Scaffold(
        containerColor = FondoPrincipal,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Configurar Aviso", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = FondoPrincipal,
                    titleContentColor = TextoPrincipal
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            if (currentStep == 0 && isOtro) {
                Text(
                    "Personaliza tu aviso",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = customNombre,
                    onValueChange = { customNombre = it },
                    label = { Text("Nombre del evento") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = customColor,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // Selector de Emoji (Botón en lugar de TextField)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clickable { showEmojiPicker = true },
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, if(customEmoji.isEmpty()) Color.White.copy(alpha = 0.3f) else customColor)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (customEmoji.isEmpty()) "Seleccionar Emoji" else "Emoji: $customEmoji",
                            color = if (customEmoji.isEmpty()) TextoSecundario else Color.White,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(if (customEmoji.isEmpty()) "🔘" else customEmoji, fontSize = 24.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Text("Elige un color:", color = TextoSecundario, modifier = Modifier.align(Alignment.Start))
                Spacer(modifier = Modifier.height(12.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(colorsList) { color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    if (customColor == color) BorderStroke(3.dp, Color.White) else BorderStroke(0.dp, Color.Transparent),
                                    CircleShape
                                )
                                .clickable { customColor = color }
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { currentStep = 1 },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = customNombre.isNotBlank() && customEmoji.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = customColor),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Siguiente: Fecha", fontWeight = FontWeight.Bold)
                }
            } else if (currentStep == 1) {
                val currentColor = if (isOtro) customColor else typeInfo.color
                
                Text(
                    "1. Selecciona la fecha",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(16.dp))
                DatePicker(
                    state = datePickerState,
                    showModeToggle = false,
                    colors = DatePickerDefaults.colors(
                        containerColor = FondoPrincipal,
                        titleContentColor = TextoPrincipal,
                        headlineContentColor = TextoPrincipal,
                        selectedDayContainerColor = currentColor,
                        todayContentColor = currentColor,
                        disabledDayContentColor = Color.Gray
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { currentStep = 2 },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = datePickerState.selectedDateMillis != null,
                    colors = ButtonDefaults.buttonColors(containerColor = currentColor),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Siguiente: Elegir Hora", fontWeight = FontWeight.Bold)
                }
                if (isOtro) {
                    TextButton(onClick = { currentStep = 0 }) {
                        Text("Volver a Personalizar", color = TextoSecundario)
                    }
                }
            } else {
                val currentColor = if (isOtro) customColor else typeInfo.color
                
                Text(
                    "2. Selecciona la hora",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(32.dp))
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = Color.White.copy(alpha = 0.05f),
                        selectorColor = currentColor,
                        periodSelectorSelectedContainerColor = currentColor.copy(alpha = 0.2f)
                    )
                )
                Spacer(modifier = Modifier.height(40.dp))
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
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = currentColor),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Guardar Evento", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = { currentStep = 1 }) {
                    Text("Volver a Fecha", color = TextoSecundario)
                }
            }
        }
    }

    if (showEmojiPicker) {
        EmojiPickerDialog(
            onEmojiSelected = { 
                customEmoji = it
                showEmojiPicker = false
            },
            onDismiss = { showEmojiPicker = false }
        )
    }
}

@Composable
fun EmojiPickerDialog(onEmojiSelected: (String) -> Unit, onDismiss: () -> Unit) {
    val emojis = listOf(
        "🎯", "🏀", "🎮", "🎸", "🍕", "🍔", "🍦", "🎬",
        "🚗", "🚀", "💡", "📚", "💻", "🔥", "🌈", "⭐",
        "⚽", "🎾", "🏋️", "🧘", "🚲", "🎨", "🎭", "🎤",
        "🎧", "📸", "🎁", "🎈", "🎉", "🍹", "🍣", "🍩",
        "🏝️", "🏖️", "🏔️", "🏕️", "🏠", "🏡", "🏢", "🏫",
        "🦴", "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻",
        "🧸", "🐼", "🐨", "🐯", "🦁", "🐮", "🐷", "🐸",
        "🐵", "🐒", "🐔", "🐧", "🐦", "🐤", "🦆", "🦅",
        "🦉", "🦇", "🐺", "🐗", "🐴", "🦄", "🐝", "🐛",
        "🦋", "🐌", "🐞", "🐜", "🦟", "🦗", "🕷️", "🦂",
        "🐢", "🐍", "🦎", "🐙", "🦑", "🦞", "🦀", "🐡",
        "🐠", "🐟", "🐬", "🐳", "🐋", "🦈", "🐊", "🐅",
        "🐆", "🦓", "🦍", "🦧", "🐘", "🦛", "🦏", "🐪",
        "🐫", "🦒", "🦘", "🦬", "🐃", "🐂", "🐄", "🐎"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = FondoCard,
            modifier = Modifier.fillMaxWidth().height(450.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Elige un Emoji",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextoPrincipal,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 50.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(emojis) { emoji ->
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .clickable { onEmojiSelected(emoji) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emoji, fontSize = 24.sp)
                        }
                    }
                }
            }
        }
    }
}
