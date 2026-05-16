package com.example.milista.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.milista.data.Alarma
import com.example.milista.ui.theme.*
import com.example.milista.ui.utils.getTranslatedText
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockScreen(
    viewModel: MiListaViewModel = viewModel(),
    onBack: () -> Unit
) {
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val alarmas by viewModel.alarmas.collectAsState()
    
    var selectedTab by remember { mutableStateOf("Alarmas") }

    Scaffold(
        containerColor = AmoledBlack,
        bottomBar = {
            // BottomNavigationBar handled in MainActivity
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
                ClockHeader(onBack, selectedLanguage)
            }

            // Tabs superiores
            item {
                ClockTabs(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    language = selectedLanguage
                )
            }

            when (selectedTab) {
                "Alarmas" -> {
                    // Próxima alarma card
                    item {
                        NextAlarmCard(alarmas, selectedLanguage)
                    }

                    // Lista de alarmas
                    items(alarmas) { alarma ->
                        AlarmItemCard(
                            alarma = alarma,
                            onToggle = { viewModel.actualizarAlarma(alarma.copy(activa = it)) }
                        )
                    }
                }
                "Reloj mundial" -> {
                    item {
                        WorldClockSection(selectedLanguage)
                    }
                }
                "Cronómetro" -> {
                    item {
                        StopwatchSection(selectedLanguage)
                    }
                }
                "Temporizador" -> {
                    item {
                        TimerSection(selectedLanguage)
                    }
                }
                "Descanso" -> {
                    item {
                        SleepSection(selectedLanguage)
                    }
                }
            }
        }
    }
}

@Composable
fun ClockHeader(onBack: () -> Unit, language: String) {
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
                    text = "Reloj 🕒",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                )
                Text(
                    text = "Todo en su tiempo",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeonGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Row {
            HeaderCircleButtonSmall(Icons.Default.Search)
            Spacer(modifier = Modifier.width(10.dp))
            HeaderCircleButtonSmall(Icons.Default.MoreVert)
        }
    }
}

@Composable
fun ClockTabs(selectedTab: String, onTabSelected: (String) -> Unit, language: String) {
    val tabs = listOf("Alarmas", "Reloj mundial", "Cronómetro", "Temporizador", "Descanso")
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(tabs) { tab ->
            val isSelected = selectedTab == tab
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (isSelected) NeonGreen.copy(alpha = 0.15f) else CardDark)
                    .border(1.dp, if (isSelected) NeonGreen else Color.Transparent, RoundedCornerShape(18.dp))
                    .clickable { onTabSelected(tab) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
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

@Composable
fun NextAlarmCard(alarmas: List<Alarma>, language: String) {
    val nextAlarm = alarmas.filter { it.activa }.minByOrNull { it.hora * 60 + it.minuto }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = BorderStroke(1.dp, BorderGlow)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Próxima alarma",
                    fontSize = 14.sp,
                    color = NeonGreen,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = nextAlarm?.let { String.format(Locale.getDefault(), "%02d:%02d", it.hora, it.minuto) } ?: "--:--",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = "Mañana, Lun 16 de mayo",
                    style = MaterialTheme.typography.bodySmall,
                    color = GrayText
                )
            }
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(NeonGreen.copy(alpha = 0.05f), CircleShape)
                    .blur(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Alarm, null, tint = NeonGreen, modifier = Modifier.size(48.dp))
            }
        }
    }
}

@Composable
fun AlarmItemCard(alarma: Alarma, onToggle: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark.copy(alpha = 0.6f)),
        border = BorderStroke(1.dp, BorderGlow)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = String.format("%02d:%02d", alarma.hora, alarma.minuto),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (alarma.activa) Color.White else GrayText
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (alarma.hora < 12) "AM" else "PM",
                        fontSize = 12.sp,
                        color = GrayText,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                Text(
                    text = alarma.dias.ifBlank { "Sin repetición" },
                    fontSize = 12.sp,
                    color = if (alarma.activa) NeonGreen else GrayText
                )
            }
            Switch(
                checked = alarma.activa,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AmoledBlack,
                    checkedTrackColor = NeonGreen,
                    uncheckedThumbColor = GrayText,
                    uncheckedTrackColor = CardDark
                )
            )
        }
    }
}

@Composable
fun WorldClockSection(language: String) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        WorldClockCard("Ciudad de México", "Local", "09:41 AM", true)
        WorldClockCard("Tokio", "+15 horas", "12:41 AM", false)
    }
}

@Composable
fun WorldClockCard(city: String, diff: String, time: String, isLocal: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = BorderStroke(1.dp, if (isLocal) NeonGreen.copy(alpha = 0.3f) else BorderGlow)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(city, fontWeight = FontWeight.Bold, color = Color.White)
                Text(diff, fontSize = 11.sp, color = GrayText)
            }
            Text(time, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = if (isLocal) NeonGreen else Color.White)
        }
    }
}

@Composable
fun StopwatchSection(language: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = BorderStroke(1.dp, BorderGlow)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "00:00:00",
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                HeaderCircleButtonSmall(Icons.Default.Refresh)
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(NeonGreen)
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PlayArrow, null, tint = AmoledBlack)
                }
            }
        }
    }
}

@Composable
fun TimerSection(language: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = BorderStroke(1.dp, BorderGlow)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "00:30:00",
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraLight,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                HeaderCircleButtonSmall(Icons.Default.Add)
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(NeonGreen)
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PlayArrow, null, tint = AmoledBlack)
                }
            }
        }
    }
}

@Composable
fun SleepSection(language: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = BorderStroke(1.dp, Purple.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Descanso", color = Purple, fontWeight = FontWeight.Bold)
                Text("Establece tu rutina de sueño", fontSize = 12.sp, color = GrayText)
                Spacer(modifier = Modifier.height(12.dp))
                Text("23:00 - 07:00", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Icon(Icons.Default.Bedtime, null, tint = Purple, modifier = Modifier.size(48.dp))
        }
    }
}
