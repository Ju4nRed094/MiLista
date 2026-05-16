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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.milista.data.Recordatorio
import com.example.milista.ui.theme.*
import com.example.milista.ui.utils.getTranslatedText
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreen(
    viewModel: MiListaViewModel = viewModel(),
    onBack: () -> Unit
) {
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val recordatorios by viewModel.recordatorios.collectAsState()
    
    var selectedTab by remember { mutableStateOf("Próximos") }

    Scaffold(
        containerColor = AmoledBlack,
        bottomBar = {
            // BottomNavigationBar handled in MainActivity
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Nuevo recordatorio */ },
                containerColor = NeonGreen,
                contentColor = AmoledBlack,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 100.dp)
                    .size(60.dp)
                    .shadow(12.dp, CircleShape, spotColor = NeonGreen)
            ) {
                Icon(Icons.Default.NotificationsActive, contentDescription = "Crear recordatorio", modifier = Modifier.size(32.dp))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(20.dp)
        ) {
            // Header
            item {
                ReminderHeader(onBack)
            }

            // Tabs Próximos/Hoy/Completados
            item {
                ReminderTabs(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    language = selectedLanguage
                )
            }

            // Lista de recordatorios filtrada
            val filteredList = when (selectedTab) {
                "Hoy" -> recordatorios.filter { 
                    val cal = Calendar.getInstance().apply { timeInMillis = it.fecha }
                    cal.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR) &&
                    cal.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR)
                }
                "Completados" -> emptyList() // TODO: Implement state for completion
                else -> recordatorios.filter { it.fecha > System.currentTimeMillis() }
            }.sortedBy { it.fecha }

            if (filteredList.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text("No hay recordatorios", color = GrayText.copy(alpha = 0.5f))
                    }
                }
            } else {
                items(filteredList) { reminder ->
                    ReminderItemCard(reminder, selectedLanguage)
                }
            }
        }
    }
}

@Composable
fun ReminderHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            HeaderCircleButtonSmall(Icons.AutoMirrored.Filled.ArrowBack, onClick = onBack)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Recordatorios 🔔",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                )
                Text(
                    text = "No olvides lo importante",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeonGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Row {
            HeaderCircleButtonSmall(Icons.Default.Search)
            Spacer(modifier = Modifier.width(10.dp))
            HeaderCircleButtonSmall(Icons.Default.FilterList)
        }
    }
}

@Composable
fun ReminderTabs(selectedTab: String, onTabSelected: (String) -> Unit, language: String) {
    val tabs = listOf("Próximos", "Hoy", "Completados")
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = CardDark,
        border = BorderStroke(1.dp, BorderGlow)
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            tabs.forEach { tab ->
                val isSelected = selectedTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (isSelected) NeonGreen.copy(alpha = 0.15f) else Color.Transparent)
                        .clickable { onTabSelected(tab) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = getTranslatedText(tab, language),
                        color = if (isSelected) NeonGreen else GrayText,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun ReminderItemCard(reminder: Recordatorio, language: String) {
    val isOtro = reminder.tipo == "Otro"
    val color = if (isOtro && reminder.colorCustom != null) Color(reminder.colorCustom) else Blue
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormat = SimpleDateFormat("d MMM", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark.copy(alpha = 0.7f)),
        border = BorderStroke(1.dp, BorderGlow)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = timeFormat.format(Date(reminder.fecha)),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = dateFormat.format(Date(reminder.fecha)),
                    fontSize = 10.sp,
                    color = GrayText
                )
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = if (isOtro) (reminder.emojiCustom ?: "🔔") else "📅", fontSize = 24.sp)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isOtro) reminder.nombreCustom ?: "Recordatorio" else getTranslatedText(reminder.tipo, language),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
                Text(
                    text = if (reminder.estado) "Activo" else "Pausado",
                    fontSize = 12.sp,
                    color = if (reminder.estado) NeonGreen else GrayText
                )
            }
            
            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = GrayText.copy(alpha = 0.5f)
            )
        }
    }
}
