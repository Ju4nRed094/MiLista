package com.example.milista.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.milista.data.Tarea
import com.example.milista.ui.theme.*
import com.example.milista.ui.utils.getTranslatedText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: MiListaViewModel,
    listaId: Int,
    onBack: () -> Unit
) {
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val tareas by viewModel.repository.obtenerTareasPorLista(listaId).collectAsState(initial = emptyList())
    val listas by viewModel.listas.collectAsState()
    val currentLista = listas.find { it.id == listaId }

    val completedTasks = tareas.count { it.estaCompletada }
    val totalTasks = tareas.size
    val progress = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                LargeTopAppBar(
                    title = {
                        Column {
                            Text(
                                currentLista?.nombre ?: getTranslatedText("Lista", selectedLanguage),
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 32.sp),
                                color = Color.White
                            )
                            if (totalTasks > 0) {
                                Text(
                                    text = "$completedTasks/$totalTasks " + getTranslatedText("completadas", selectedLanguage),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = GrayText
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        HeaderCircleButtonTaskLocal(Icons.AutoMirrored.Filled.ArrowBack, onClick = onBack)
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = BackgroundDark.copy(alpha = 0.9f),
                        titleContentColor = Color.White
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddTaskDialog = true },
                    containerColor = SamsungGreen,
                    contentColor = Color.Black,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(6.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(28.dp))
                }
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                if (totalTasks > 0) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                            .height(6.dp)
                            .clip(CircleShape),
                        color = SamsungGreen,
                        trackColor = Color.White.copy(alpha = 0.05f)
                    )
                }

                if (tareas.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(getTranslatedText("No hay tareas pendientes", selectedLanguage), color = GrayText.copy(alpha = 0.5f))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(tareas, key = { it.id }) { tarea ->
                            TaskItemPremiumLocal(
                                tarea = tarea,
                                onToggle = { viewModel.toggleTarea(tarea) },
                                onDelete = { viewModel.borrarTarea(tarea) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(100.dp)) }
                    }
                }
            }
        }

        if (showAddTaskDialog) {
            AlertDialog(
                onDismissRequest = { showAddTaskDialog = false },
                containerColor = CardDark,
                shape = RoundedCornerShape(28.dp),
                title = { Text(getTranslatedText("Nueva Tarea", selectedLanguage), color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = SamsungGreen
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newTaskTitle.isNotBlank()) {
                                viewModel.agregarTarea(newTaskTitle, listaId)
                                newTaskTitle = ""
                                showAddTaskDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SamsungGreen),
                        enabled = newTaskTitle.isNotBlank()
                    ) {
                        Text(getTranslatedText("Añadir", selectedLanguage), color = Color.Black)
                    }
                }
            )
        }
    }
}

@Composable
fun TaskItemPremiumLocal(
    tarea: Tarea,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = if (tarea.estaCompletada) Color.White.copy(alpha = 0.02f) else Color.White.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = tarea.estaCompletada,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = SamsungGreen,
                    uncheckedColor = GrayText
                )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = tarea.titulo,
                modifier = Modifier.weight(1f),
                color = if (tarea.estaCompletada) GrayText else Color.White,
                style = TextStyle(
                    textDecoration = if (tarea.estaCompletada) TextDecoration.LineThrough else TextDecoration.None,
                    fontSize = 16.sp
                )
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, null, tint = GrayText.copy(alpha = 0.4f), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun HeaderCircleButtonTaskLocal(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.size(44.dp),
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
