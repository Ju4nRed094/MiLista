package com.example.milista.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.milista.ui.theme.*
import com.example.milista.ui.utils.getTranslatedText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductivityScreen(
    viewModel: MiListaViewModel,
    onBack: () -> Unit
) {
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val unifiedItems by viewModel.unifiedItems.collectAsState()
    
    val completedTasks = unifiedItems.count { it.isCompleted }
    val totalTasks = unifiedItems.count { it.type == com.example.milista.data.ItemType.TASK }
    val progress = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            LargeTopAppBar(
                title = { 
                    Column {
                        Text(getTranslatedText("Productividad", selectedLanguage), style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 32.sp), color = Color.White)
                        Text(getTranslatedText("Tu progreso semanal", selectedLanguage), style = MaterialTheme.typography.bodyMedium, color = GrayText)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = Color.Transparent, scrolledContainerColor = BackgroundDark.copy(alpha = 0.9f))
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                ProductivityCard {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(getTranslatedText("Resumen", selectedLanguage), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(20.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(12.dp).background(Color.White.copy(0.05f), CircleShape),
                            color = SamsungGreen,
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${(progress * 100).toInt()}% " + getTranslatedText("completado", selectedLanguage), color = GrayText, fontSize = 14.sp)
                            Text("$completedTasks/$totalTasks " + getTranslatedText("tareas", selectedLanguage), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatBox(Modifier.weight(1f), Icons.Default.CheckCircle, "$completedTasks", getTranslatedText("Finalizadas", selectedLanguage), SamsungGreen)
                    StatBox(Modifier.weight(1f), Icons.Default.Pending, "${totalTasks - completedTasks}", getTranslatedText("Pendientes", selectedLanguage), SamsungBlue)
                }
            }

            item {
                ProductivityCard {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(getTranslatedText("Actividad reciente", selectedLanguage), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(getTranslatedText("Próximamente: Gráficos de rendimiento One UI", selectedLanguage), color = GrayText, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ProductivityCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = Color.White.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        content = { Column(content = content) }
    )
}

@Composable
fun StatBox(modifier: Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String, color: Color) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = Color.White.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(label, color = GrayText, fontSize = 12.sp)
        }
    }
}
