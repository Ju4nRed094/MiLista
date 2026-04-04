package com.example.milista.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.milista.data.ItemType
import com.example.milista.data.UnifiedItem
import com.example.milista.ui.theme.*
import com.example.milista.ui.utils.getTranslatedText
import com.example.milista.ui.utils.getLocaleCode
import java.text.SimpleDateFormat
import java.util.*

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
    val unifiedItems by viewModel.unifiedItems.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val recordatorios by viewModel.recordatorios.collectAsState()
    val alarmas by viewModel.alarmas.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 32.dp)
    ) {
        item { GreetingHeaderPremium(selectedLanguage) }

        item {
            Spacer(modifier = Modifier.height(28.dp))
            SummaryDashboardCard(unifiedItems, alarmas, selectedLanguage)
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            MiniHorizontalCalendar(selectedLanguage, onNavigateToCalendar)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 32.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(getTranslatedText("Próximos eventos", selectedLanguage), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp), color = Color.White)
                Surface(
                    onClick = { onNavigateToCalendar() },
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        getTranslatedText("Ver todo", selectedLanguage) + " >", 
                        style = MaterialTheme.typography.labelMedium, 
                        color = GrayText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        item {
            UpcomingEventsCardContainer(recordatorios, alarmas, selectedLanguage)
        }

        item {
            SectionTitleLocal(getTranslatedText("Accesos rápidos", selectedLanguage))
            QuickAccessRow(
                selectedLanguage = selectedLanguage,
                onNavigateToCalendar = onNavigateToCalendar,
                onNavigateToClock = onNavigateToClock,
                onNavigateToNotes = onNavigateToNotes,
                onNavigateToListas = onNavigateToListas,
                onNavigateToReminders = { onAddReminder("Otro") }
            )
        }

        item { Spacer(modifier = Modifier.height(120.dp)) }
    }
}

@Composable
fun SectionTitleLocal(title: String) {
    Text(text = title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Color.White, fontSize = 20.sp, modifier = Modifier.padding(top = 32.dp, bottom = 16.dp))
}

@Composable
fun GreetingHeaderPremium(language: String) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 5..11 -> "¡Buenos días!"
        in 12..19 -> "¡Buenas tardes!"
        else -> "¡Buenas noches!"
    }
    val dateFormat = SimpleDateFormat("EEEE, d 'de' MMMM", Locale(getLocaleCode(language)))
    val dateStr = dateFormat.format(Date()).replaceFirstChar { it.uppercase() }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = getTranslatedText(greeting, language), style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 32.sp, color = Color.White))
                Spacer(modifier = Modifier.width(8.dp))
                Text("👋", fontSize = 28.sp)
            }
            Text(text = dateStr, style = MaterialTheme.typography.titleMedium, color = GrayText.copy(0.7f), modifier = Modifier.padding(top = 4.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            HeaderIconButton(Icons.Default.Search)
            Spacer(modifier = Modifier.width(12.dp))
            HeaderIconButton(Icons.Default.Notifications, hasBadge = true)
        }
    }
}

@Composable
fun HeaderIconButton(icon: ImageVector, hasBadge: Boolean = false) {
    Surface(
        onClick = {},
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.06f),
        modifier = Modifier.size(46.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            if (hasBadge) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(SamsungGreen, CircleShape)
                        .align(Alignment.TopEnd)
                        .offset(x = (-4).dp, y = 4.dp)
                )
            }
        }
    }
}

@Composable
fun SummaryDashboardCard(items: List<UnifiedItem>, alarmas: List<com.example.milista.data.Alarma>, language: String) {
    val pendingTasks = items.count { it.type == ItemType.TASK && !it.isCompleted }
    val nextAlarm = alarmas.filter { it.activa }.minByOrNull { it.hora * 60 + it.minuto }
    val todayEvents = items.count { 
        val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
        val today = Calendar.getInstance()
        cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) && it.type == ItemType.EVENT
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = Color.White.copy(alpha = 0.04f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = SamsungGreen, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(getTranslatedText("Resumen del día", language), style = MaterialTheme.typography.labelLarge, color = Color.White.copy(0.9f), fontWeight = FontWeight.SemiBold)
                }
                Icon(Icons.Default.MoreHoriz, contentDescription = null, tint = GrayText.copy(0.4f))
            }
            Spacer(modifier = Modifier.height(28.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                SummaryDashboardItem(Icons.Default.CheckCircle, pendingTasks.toString(), getTranslatedText("Pendientes", language), SamsungBlue)
                Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.White.copy(alpha = 0.05f)).align(Alignment.CenterVertically))
                SummaryDashboardItem(Icons.Default.Alarm, nextAlarm?.let { "1" } ?: "0", getTranslatedText("Alarma", language), SamsungRed)
                Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.White.copy(alpha = 0.05f)).align(Alignment.CenterVertically))
                SummaryDashboardItem(Icons.Default.CalendarToday, todayEvents.toString(), getTranslatedText("Eventos hoy", language), SamsungGreen)
            }
        }
    }
}

@Composable
fun SummaryDashboardItem(icon: ImageVector, value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(42.dp).background(color.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(value, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        Text(label, fontSize = 11.sp, color = GrayText.copy(0.6f), fontWeight = FontWeight.Medium)
    }
}

@Composable
fun MiniHorizontalCalendar(language: String, onNavigate: () -> Unit) {
    val cal = Calendar.getInstance()
    val monthName = SimpleDateFormat("MMMM yyyy", Locale(getLocaleCode(language))).format(cal.time).replaceFirstChar { it.uppercase() }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = Color.White.copy(alpha = 0.04f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(monthName, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                IconButton(onClick = onNavigate, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = GrayText, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val days = listOf("L", "M", "M", "J", "V", "S", "D")
                val today = cal.get(Calendar.DAY_OF_MONTH)
                val startDay = today - cal.get(Calendar.DAY_OF_WEEK) + 2 
                
                days.forEachIndexed { index, d ->
                    val dayNum = startDay + index
                    val isToday = dayNum == today
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(d, fontSize = 11.sp, color = GrayText.copy(0.5f), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isToday) SamsungGreen else Color.Transparent)
                                .clickable { },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(dayNum.toString(), fontSize = 15.sp, color = if (isToday) Color.Black else Color.White, fontWeight = FontWeight.Bold)
                                if (isToday) {
                                    Box(modifier = Modifier.size(3.dp).background(Color.Black, CircleShape).offset(y = 2.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UpcomingEventsCardContainer(recordatorios: List<com.example.milista.data.Recordatorio>, alarmas: List<com.example.milista.data.Alarma>, language: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = Color.White.copy(alpha = 0.04f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            if (recordatorios.isEmpty() && alarmas.none { it.activa }) {
                Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    Text(getTranslatedText("Nada programado para hoy", language), color = GrayText.copy(0.5f), style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                recordatorios.take(4).forEachIndexed { index, rec ->
                    UpcomingEventItemPremium(rec, language)
                    if (index < 3 && index < recordatorios.size - 1) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.White.copy(alpha = 0.05f))
                    }
                }
            }
        }
    }
}

@Composable
fun UpcomingEventItemPremium(reminder: com.example.milista.data.Recordatorio, language: String) {
    val isOtro = reminder.tipo == "Otro"
    val color = if (isOtro && reminder.colorCustom != null) Color(reminder.colorCustom) else SamsungBlue
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(48.dp).background(color.copy(alpha = 0.15f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Event, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(reminder.fecha)), fontSize = 12.sp, color = color.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
            Text(
                text = if (isOtro) reminder.nombreCustom ?: "Evento" else getTranslatedText(reminder.tipo, language), 
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 17.sp), 
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(text = if (isOtro) "Personal" else getTranslatedText(reminder.tipo, language), fontSize = 13.sp, color = GrayText.copy(0.6f))
        }
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
    }
}

@Composable
fun QuickAccessRow(
    selectedLanguage: String,
    onNavigateToCalendar: () -> Unit,
    onNavigateToClock: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToListas: () -> Unit,
    onNavigateToReminders: () -> Unit
) {
    val items = listOf(
        QuickAccessItemData(getTranslatedText("Calendario", selectedLanguage), Icons.Default.CalendarMonth, SamsungBlue, onNavigateToCalendar),
        QuickAccessItemData(getTranslatedText("Reloj", selectedLanguage), Icons.Default.AccessTime, SamsungRed, onNavigateToClock),
        QuickAccessItemData(getTranslatedText("Notas", selectedLanguage), Icons.Default.Description, SamsungOrange, onNavigateToNotes),
        QuickAccessItemData(getTranslatedText("Listas", selectedLanguage), Icons.AutoMirrored.Filled.List, SamsungGreen, onNavigateToListas),
        QuickAccessItemData(getTranslatedText("Avisos", selectedLanguage), Icons.Default.NotificationsActive, SamsungPurple, onNavigateToReminders)
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(items) { item ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(76.dp)) {
                Surface(
                    onClick = item.onClick,
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.05f),
                    modifier = Modifier.size(76.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(item.icon, contentDescription = null, tint = item.color, modifier = Modifier.size(28.dp))
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(item.label, color = GrayText, fontSize = 11.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
            }
        }
    }
}

data class QuickAccessItemData(val label: String, val icon: ImageVector, val color: Color, val onClick: () -> Unit)

@Composable
fun PremiumBottomBar(
    selectedLanguage: String,
    onNavigateToSettings: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToProductivity: () -> Unit,
    onNavigateToCategories: () -> Unit,
    currentRoute: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .height(76.dp),
        color = Color(0xFF141A22).copy(alpha = 0.9f),
        shape = RoundedCornerShape(38.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItemPremium(Icons.Default.Home, getTranslatedText("Inicio", selectedLanguage), currentRoute == "home", onClick = onNavigateToHome)
            BottomNavItemPremium(Icons.Default.BarChart, getTranslatedText("Productividad", selectedLanguage), currentRoute == "productivity", onClick = onNavigateToProductivity)
            BottomNavItemPremium(Icons.Default.GridView, getTranslatedText("Apps", selectedLanguage), currentRoute == "listas", onClick = onNavigateToCategories)
            BottomNavItemPremium(Icons.Default.Settings, getTranslatedText("Ajustes", selectedLanguage), currentRoute == "settings", onClick = onNavigateToSettings)
        }
    }
}

@Composable
fun BottomNavItemPremium(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit = {}) {
    val color = if (isSelected) SamsungGreen else GrayText.copy(0.4f)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clip(RoundedCornerShape(16.dp)).clickable { onClick() }.padding(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(26.dp))
        if (isSelected) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.size(4.dp).background(SamsungGreen, CircleShape))
        }
    }
}
