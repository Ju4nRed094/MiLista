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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.milista.ui.theme.*
import com.example.milista.ui.utils.getTranslatedText

@Composable
fun HomeScreen(
    viewModel: MiListaViewModel,
    onAddReminder: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToClock: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToListas: () -> Unit,
    onNavigateToNotes: () -> Unit
) {
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()

    Scaffold(
        containerColor = AmoledBlack,
        bottomBar = {
            // Se asume que el BottomBar premium ya está manejado en MainActivity
            // Si no, aquí se implementaría una versión local
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Header Superior
            item {
                NoctraHeader(selectedLanguage)
            }

            // Card Principal de Productividad
            item {
                ProductivityMainCard(selectedLanguage)
            }

            // Sección Calendario + Eventos
            item {
                CalendarAndNextEventRow(onNavigateToCalendar, selectedLanguage)
            }

            // Notas Rápidas
            item {
                QuickNotesSection(onNavigateToNotes, selectedLanguage)
            }

            // Resumen del día
            item {
                DaySummarySection(selectedLanguage)
            }

            // Accesos Rápidos
            item {
                QuickActionsSection(
                    onAddNote = { onNavigateToNotes() },
                    onAddReminder = { onAddReminder("Otro") },
                    onAddList = { onNavigateToListas() },
                    onFocus = { /* onNavigateToFocus? */ },
                    selectedLanguage = selectedLanguage
                )
            }
        }
    }
}

@Composable
fun NoctraHeader(language: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            HeaderCircleButton(Icons.Default.Menu)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Noctra ✨",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = (-1).sp
                    )
                )
                Text(
                    text = getTranslatedText("Organiza tu día. Maximiza tu potencial.", language),
                    style = MaterialTheme.typography.bodySmall,
                    color = GrayText.copy(alpha = 0.7f)
                )
            }
        }
        Row {
            HeaderCircleButton(Icons.Default.Search)
            Spacer(modifier = Modifier.width(12.dp))
            Box {
                HeaderCircleButton(Icons.Default.Notifications)
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(NeonGreen, CircleShape)
                        .align(Alignment.TopEnd)
                        .border(2.dp, AmoledBlack, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("3", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AmoledBlack)
                }
            }
        }
    }
}

@Composable
fun HeaderCircleButton(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, BorderGlow, CircleShape)
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(22.dp))
    }
}

@Composable
fun ProductivityMainCard(language: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(20.dp, RoundedCornerShape(28.dp), spotColor = NeonGreen.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = BorderStroke(1.dp, BorderGlow)
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "¡Buenas noches, Juan! 🌙",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = getTranslatedText("Estás haciendo un gran trabajo hoy.", language),
                    style = MaterialTheme.typography.bodyMedium,
                    color = GrayText
                )
            }

            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
                CircularProgressIndicator(
                    progress = { 0.78f },
                    modifier = Modifier.size(64.dp),
                    color = NeonGreen,
                    strokeWidth = 6.dp,
                    trackColor = Color.White.copy(alpha = 0.05f)
                )
                Text(
                    "78%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonGreen
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("Productividad", fontSize = 12.sp, color = NeonGreen, fontWeight = FontWeight.Bold)
                Text("Hoy", fontSize = 11.sp, color = GrayText)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Vas por buen camino", fontSize = 10.sp, color = NeonGreen)
                    Icon(Icons.Default.ArrowUpward, null, tint = NeonGreen, modifier = Modifier.size(12.dp))
                }
            }
        }
    }
}

@Composable
fun CalendarAndNextEventRow(onNavigate: () -> Unit, language: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Columna Izquierda: Calendario
        Card(
            modifier = Modifier
                .weight(1.3f)
                .fillMaxHeight(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            border = BorderStroke(1.dp, BorderGlow)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Mayo 2025", fontWeight = FontWeight.Bold, color = Color.White)
                    Row {
                        Icon(Icons.Default.ChevronLeft, null, tint = GrayText, modifier = Modifier.size(20.dp))
                        Icon(Icons.Default.ChevronRight, null, tint = GrayText, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    val days = listOf("12" to "LUN", "13" to "MAR", "14" to "MIÉ", "15" to "JUE", "16" to "VIE", "17" to "SÁB", "18" to "DOM")
                    days.forEach { (num, day) ->
                        val isSelected = num == "14"
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(day, fontSize = 9.sp, color = GrayText)
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) NeonGreen else Color.Transparent)
                                    .border(1.dp, if (isSelected) NeonGreen else Color.Transparent, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(num, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isSelected) AmoledBlack else Color.White)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                EventMiniItem(Color(0xFFB4F16C), "Diseño UI Noctra", "10:00 AM - 12:00 PM", "Trabajo")
                Spacer(modifier = Modifier.height(12.dp))
                EventMiniItem(Color(0xFFA78BFA), "Reunión con equipo", "2:30 PM - 3:30 PM", "Trabajo")
                
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Ver calendario completo >",
                    color = NeonGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigate() }
                )
            }
        }

        // Columna Derecha: Próximo Evento
        Card(
            modifier = Modifier
                .weight(0.7f)
                .fillMaxHeight(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1225)), // Morado oscuro
            border = BorderStroke(1.dp, Purple.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Próximo evento", color = Purple, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Purple.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CalendarToday, null, tint = Purple)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Reunión con equipo", fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center, fontSize = 13.sp)
                Text("Mañana • 2:30 PM", fontSize = 11.sp, color = GrayText)
                
                Spacer(modifier = Modifier.weight(1f))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CountdownUnit("16", "Hrs")
                    CountdownUnit("48", "Min")
                }
            }
        }
    }
}

@Composable
fun EventMiniItem(color: Color, title: String, time: String, category: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.width(3.dp).height(24.dp).background(color, RoundedCornerShape(2.dp)))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
            Text(time, fontSize = 10.sp, color = GrayText)
        }
        Box(
            modifier = Modifier.background(color.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(category, fontSize = 8.sp, color = color, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CountdownUnit(value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        Text(unit, fontSize = 8.sp, color = GrayText)
    }
}

@Composable
fun QuickNotesSection(onNavigate: () -> Unit, language: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = BorderStroke(1.dp, BorderGlow)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Notas rápidas", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                HeaderCircleButton(Icons.Default.Add)
            }
            Spacer(modifier = Modifier.height(16.dp))
            QuickNoteItem(Icons.Default.Description, "Ideas para el nuevo diseño", "Hoy, 8:47 PM")
            Spacer(modifier = Modifier.height(12.dp))
            QuickNoteItem(Icons.Default.Checklist, "Comprar materiales", "Hoy, 6:12 PM")
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Ver todas >",
                color = NeonGreen,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onNavigate() }
            )
        }
    }
}

@Composable
fun QuickNoteItem(icon: ImageVector, title: String, time: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = NeonGreen, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
            Text(time, fontSize = 10.sp, color = GrayText)
        }
    }
}

@Composable
fun DaySummarySection(language: String) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text("Resumen del día", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard(Icons.Default.CheckCircle, "8", "Tareas", NeonGreen, "4 completadas", Modifier.weight(1f))
            SummaryCard(Icons.Default.Timer, "3h 45m", "Enfoque", Purple, "Tiempo total", Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard(Icons.Default.Flag, "5/8", "Objetivos", Orange, "En progreso", Modifier.weight(1f))
            SummaryCard(Icons.Default.Whatshot, "264", "Racha", Blue, "Días seguidos", Modifier.weight(1f))
        }
    }
}

@Composable
fun SummaryCard(icon: ImageVector, value: String, label: String, color: Color, subtext: String, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Text(label, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Text(subtext, fontSize = 10.sp, color = GrayText)
        }
    }
}

@Composable
fun QuickActionsSection(onAddNote: () -> Unit, onAddReminder: () -> Unit, onAddList: () -> Unit, onFocus: () -> Unit, selectedLanguage: String) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Accesos rápidos", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Tune, null, tint = NeonGreen, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Personalizar", color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionButton(Icons.Outlined.NoteAdd, "Nueva nota", Modifier.weight(1f), onAddNote)
            QuickActionButton(Icons.Outlined.Notifications, "Nuevo recordatorio", Modifier.weight(1f), onAddReminder)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionButton(Icons.Outlined.ShoppingCart, "Nueva lista", Modifier.weight(1f), onAddList)
            QuickActionButton(Icons.Outlined.Adjust, "Bloque de enfoque", Modifier.weight(1f), onFocus)
        }
    }
}

@Composable
fun QuickActionButton(icon: ImageVector, label: String, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = CardDark,
        border = BorderStroke(1.dp, BorderGlow)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, fontSize = 11.sp, color = Color.White, textAlign = TextAlign.Center)
        }
    }
}

// Bottom Bar Premium logic removed as it's now in NavigationUtils.kt

// Bottom Bar Premium logic remains in MainActivity but we define the look here if needed.
