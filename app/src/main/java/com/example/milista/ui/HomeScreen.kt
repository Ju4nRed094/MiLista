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
import androidx.compose.material.icons.rounded.*
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
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

    Box(modifier = Modifier.fillMaxSize().background(AmoledBlack)) {
        // Fondo con iluminación verde ambiental dinámica
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        Brush.radialGradient(
                            colors = listOf(NeonGreen.copy(alpha = 0.05f), Color.Transparent),
                            center = Offset(size.width * 0.8f, size.height * 0.1f),
                            radius = size.width * 1.2f
                        )
                    )
                }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp)
        ) {
            // 1. Header Principal
            item { NoctraMainHeader(selectedLanguage, onNavigateToSettings) }

            // 2. Tarjeta Principal de Productividad
            item { ProductivityDashboardCard(selectedLanguage) }

            // 3. Calendario Compacto + Próximo Evento (Grid 2 columnas simulado)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CompactCalendarCard(modifier = Modifier.weight(1.2f), onNavigateToCalendar)
                    NextEventCard(modifier = Modifier.weight(0.8f))
                }
            }

            // 4. Notas Rápidas
            item { QuickNotesDashboard(onNavigateToNotes) }

            // 5. Resumen del día (Grid 2x2)
            item { DaySummaryGrid() }

            // 6. Accesos Rápidos
            item {
                QuickAccessDashboard(
                    onAddNote = { onNavigateToNotes() },
                    onAddReminder = { onAddReminder("Otro") },
                    onAddList = { onNavigateToListas() }
                )
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun NoctraMainHeader(language: String, onMenuClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, BorderGlow, CircleShape)
                    .clickable { onMenuClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Tune, null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
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
                    text = buildAnnotatedString {
                        append("Organiza tu día. ")
                        withStyle(style = SpanStyle(color = NeonGreen, fontWeight = FontWeight.Bold)) {
                            append("Maximiza")
                        }
                        append(" tu potencial.")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = GrayText.copy(alpha = 0.7f)
                )
            }
        }
        Row {
            HeaderActionBtn(Icons.Rounded.Search)
            Spacer(modifier = Modifier.width(12.dp))
            Box {
                HeaderActionBtn(Icons.Rounded.Notifications)
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(NeonGreen, CircleShape)
                        .align(Alignment.TopEnd)
                        .offset(x = 2.dp, y = (-2).dp)
                        .border(2.dp, AmoledBlack, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("3", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = AmoledBlack)
                }
            }
        }
    }
}

@Composable
fun HeaderActionBtn(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(52.dp)
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
fun ProductivityDashboardCard(language: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(24.dp, RoundedCornerShape(28.dp), spotColor = NeonGreen.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(28.dp),
        color = CardDark,
        border = BorderStroke(1.dp, BorderGlow)
    ) {
        Row(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
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
                    text = "Estás haciendo un gran trabajo hoy.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GrayText.copy(alpha = 0.8f)
                )
            }

            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 20.dp)) {
                CircularProgressIndicator(
                    progress = { 0.78f },
                    modifier = Modifier.size(68.dp),
                    color = NeonGreen,
                    strokeWidth = 6.dp,
                    trackColor = Color.White.copy(alpha = 0.05f)
                )
                Text(
                    "78%",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NeonGreen
                )
                // Glow del progreso
                Box(modifier = Modifier.size(68.dp).blur(12.dp).background(NeonGreen.copy(alpha = 0.1f), CircleShape))
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("Productividad", fontSize = 12.sp, color = NeonGreen, fontWeight = FontWeight.Bold)
                Text("Hoy", fontSize = 11.sp, color = GrayText)
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Vas por buen camino", fontSize = 10.sp, color = NeonGreen)
                    Icon(Icons.Rounded.ArrowUpward, null, tint = NeonGreen, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
fun CompactCalendarCard(modifier: Modifier = Modifier, onNavigate: () -> Unit) {
    Surface(
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(28.dp),
        color = CardDark,
        border = BorderStroke(1.dp, BorderGlow)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Mayo 2025", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 16.sp)
                Row {
                    Icon(Icons.Rounded.ChevronLeft, null, tint = GrayText, modifier = Modifier.size(20.dp))
                    Icon(Icons.Rounded.ChevronRight, null, tint = GrayText, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val days = listOf("12" to "LUN", "13" to "MAR", "14" to "MIÉ", "15" to "JUE", "16" to "VIE", "17" to "SÁB", "18" to "DOM")
                days.forEach { (num, day) ->
                    val isSelected = num == "14"
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(day, fontSize = 9.sp, color = GrayText, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) NeonGreen else Color.Transparent)
                                .border(1.dp, if (isSelected) NeonGreen else Color.Transparent, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(num, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = if (isSelected) AmoledBlack else Color.White)
                            if (isSelected) {
                                Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 2.dp).size(4.dp).background(AmoledBlack, CircleShape))
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            DashboardEventItem(Color(0xFF8CFF2F), "Diseño UI Noctra", "10:00 AM – 12:00 PM", "Trabajo")
            Spacer(modifier = Modifier.height(12.dp))
            DashboardEventItem(Color(0xFF9D4DFF), "Reunión con equipo", "2:30 PM – 3:30 PM", "Trabajo")
            
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
}

@Composable
fun DashboardEventItem(color: Color, title: String, time: String, category: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.width(3.dp).height(28.dp).background(color, RoundedCornerShape(2.dp)).shadow(4.dp, spotColor = color))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
            Text(time, fontSize = 11.sp, color = GrayText)
        }
        Box(
            modifier = Modifier.background(color.copy(alpha = 0.1f), CircleShape).border(1.dp, color.copy(alpha = 0.2f), CircleShape).padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(category, fontSize = 9.sp, color = color, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun NextEventCard(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFF130D1F), // Morado muy oscuro
        border = BorderStroke(1.dp, Purple.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Próximo evento", color = Purple, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Purple.copy(alpha = 0.1f), CircleShape)
                    .border(1.dp, Purple.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.CalendarToday, null, tint = Purple, modifier = Modifier.size(26.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Reunión con equipo", fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center, fontSize = 14.sp)
            Text("Mañana • 2:30 PM", fontSize = 11.sp, color = GrayText, textAlign = TextAlign.Center)
            
            Spacer(modifier = Modifier.weight(1f))
            
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                TimeBlock("16", "Hrs")
                Text(":", color = Color.White, fontWeight = FontWeight.Bold)
                TimeBlock("48", "Min")
                Text(":", color = Color.White, fontWeight = FontWeight.Bold)
                TimeBlock("23", "Seg")
            }
        }
    }
}

@Composable
fun TimeBlock(value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
        Text(unit, fontSize = 8.sp, color = GrayText, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun QuickNotesDashboard(onNavigate: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = CardDark,
        border = BorderStroke(1.dp, BorderGlow)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Notas rápidas", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = Color.White)
                Box(
                    modifier = Modifier.size(32.dp).background(NeonGreen.copy(alpha = 0.1f), CircleShape).clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Add, null, tint = NeonGreen, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            DashboardNoteItem(Icons.Rounded.Description, "Ideas para el nuevo diseño", "Hoy, 8:47 PM")
            Spacer(modifier = Modifier.height(12.dp))
            DashboardNoteItem(Icons.Rounded.Checklist, "Comprar materiales", "Hoy, 6:12 PM")
            
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                "Ver todas >",
                color = NeonGreen,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onNavigate() }
            )
        }
    }
}

@Composable
fun DashboardNoteItem(icon: ImageVector, title: String, time: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp).background(NeonGreen.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = NeonGreen, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
            Text(time, fontSize = 11.sp, color = GrayText.copy(alpha = 0.6f))
        }
    }
}

@Composable
fun DaySummaryGrid() {
    Column {
        Text("Resumen del día", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SummarySmallCard(Icons.Rounded.CheckCircle, "8", "Tareas", "4 completadas", NeonGreen, Modifier.weight(1f))
            SummarySmallCard(Icons.Rounded.Timer, "3h 45m", "Enfoque", "Tiempo total", Purple, Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SummarySmallCard(Icons.Rounded.Flag, "5/8", "Objetivos", "En progreso", Orange, Modifier.weight(1f))
            SummarySmallCard(Icons.Rounded.Whatshot, "264", "Racha", "Días seguidos", Blue, Modifier.weight(1f))
        }
    }
}

@Composable
fun SummarySmallCard(icon: ImageVector, value: String, label: String, subtext: String, color: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = CardDark,
        border = BorderStroke(1.dp, color.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text(label, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Text(subtext, fontSize = 10.sp, color = GrayText.copy(alpha = 0.6f))
        }
    }
}

@Composable
fun QuickAccessDashboard(onAddNote: () -> Unit, onAddReminder: () -> Unit, onAddList: () -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Accesos rápidos", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = Color.White)
            Text("Personalizar", color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { })
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            QuickDashboardBtn(Icons.Rounded.Description, "Nueva nota", Modifier.weight(1f), onAddNote)
            QuickDashboardBtn(Icons.Rounded.Notifications, "Nuevo recordatorio", Modifier.weight(1f), onAddReminder)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            QuickDashboardBtn(Icons.Rounded.Checklist, "Nueva lista", Modifier.weight(1f), onAddList)
            QuickDashboardBtn(Icons.Rounded.TrackChanges, "Bloque de enfoque", Modifier.weight(1f), {})
        }
    }
}

@Composable
fun QuickDashboardBtn(icon: ImageVector, label: String, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = CardDark,
        border = BorderStroke(1.dp, BorderGlow)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(26.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text(label, fontSize = 11.sp, color = Color.White, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium)
        }
    }
}
