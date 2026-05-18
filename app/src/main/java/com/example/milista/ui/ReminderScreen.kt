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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.withStyle
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
    var showAddSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF000000))) {
        // Glow verde sutil de fondo
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        Brush.radialGradient(
                            colors = listOf(NeonGreen.copy(alpha = 0.04f), Color.Transparent),
                            center = Offset(size.width * 0.1f, size.height * 0.1f),
                            radius = size.width * 1.5f
                        )
                    )
                }
        )

        Scaffold(
            containerColor = Color.Transparent,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddSheet = true },
                    containerColor = NeonGreen,
                    contentColor = Color.Black,
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(bottom = 100.dp)
                        .size(64.dp)
                        .shadow(16.dp, CircleShape, spotColor = NeonGreen)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nuevo recordatorio", modifier = Modifier.size(32.dp))
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                item {
                    ReminderHeaderV2(onBack)
                }

                // Tabs
                item {
                    ReminderTabsV2(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it },
                        language = selectedLanguage
                    )
                }

                // Subheader dinámico
                item {
                    Text(
                        text = "$selectedTab recordatorios",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Lista Filtrada
                val filteredList = when (selectedTab) {
                    "Hoy" -> recordatorios.filter { isSameDay(it.fecha, System.currentTimeMillis()) }
                    "Completados" -> recordatorios.filter { it.isCompleted }
                    else -> recordatorios.filter { !it.isCompleted }
                }.sortedBy { it.fecha }

                if (filteredList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No hay recordatorios pendientes ✨",
                                color = GrayText.copy(alpha = 0.5f),
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    items(filteredList) { reminder ->
                        ReminderPremiumCard(reminder, selectedLanguage)
                    }
                }

                // Recordatorios Inteligentes Section
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Recordatorios inteligentes", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Ver todos >", color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SmartReminderCard(
                            icon = Icons.Rounded.AccessTime,
                            title = "Tienes 3 recordatorios importantes hoy",
                            subtitle = "Mantén tu productividad",
                            modifier = Modifier.weight(1f)
                        )
                        SmartReminderCard(
                            icon = Icons.Rounded.NotificationsActive,
                            title = "Activa las notificaciones inteligentes",
                            subtitle = "Nunca olvides nada",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }

        if (showAddSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAddSheet = false },
                sheetState = sheetState,
                containerColor = CardDark
            ) {
                AddReminderBottomSheet(
                    viewModel = viewModel,
                    onDismiss = { showAddSheet = false },
                    onTypeSelected = { tipo ->
                        showAddSheet = false
                        // Handle navigation to details if needed
                    }
                )
            }
        }
    }
}

@Composable
fun ReminderHeaderV2(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Recordatorios",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                )
                Text(
                    text = buildAnnotatedString {
                        append("Organiza tu día, nunca olvides lo ")
                        withStyle(style = SpanStyle(color = NeonGreen, fontWeight = FontWeight.Bold)) {
                            append("importante")
                        }
                        append(".")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = GrayText.copy(alpha = 0.8f)
                )
            }
        }
        Row {
            HeaderActionIcon(Icons.Rounded.Search)
            Spacer(modifier = Modifier.width(10.dp))
            HeaderActionIcon(Icons.Rounded.Tune)
            Spacer(modifier = Modifier.width(10.dp))
            HeaderActionIcon(Icons.Rounded.MoreVert)
        }
    }
}

@Composable
fun HeaderActionIcon(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.03f))
            .border(1.dp, Color.White.copy(alpha = 0.05f), CircleShape)
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(18.dp))
    }
}

@Composable
fun ReminderTabsV2(selectedTab: String, onTabSelected: (String) -> Unit, language: String) {
    val tabs = listOf("Próximos", "Hoy", "Completados")
    
    Surface(
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(28.dp),
        color = Color.White.copy(alpha = 0.03f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            tabs.forEach { tab ->
                val isSelected = selectedTab == tab
                val bgColor = if (isSelected) NeonGreen else Color.Transparent
                val contentColor = if (isSelected) Color.Black else GrayText
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(24.dp))
                        .background(bgColor)
                        .clickable { onTabSelected(tab) },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (tab == "Próximos") Icon(Icons.Rounded.CalendarToday, null, tint = contentColor, modifier = Modifier.size(14.dp))
                        if (tab == "Hoy") Icon(Icons.Rounded.WbSunny, null, tint = contentColor, modifier = Modifier.size(14.dp))
                        if (tab == "Completados") Icon(Icons.Rounded.CheckCircle, null, tint = contentColor, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = getTranslatedText(tab, language),
                            color = contentColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReminderPremiumCard(reminder: Recordatorio, language: String) {
    val isOtro = reminder.tipo == "Otro"
    val categoryColor = if (isOtro && reminder.colorCustom != null) Color(reminder.colorCustom) else {
        when(reminder.tipo) {
            "Trabajo" -> Purple
            "Salud" -> Orange
            "Estudio" -> Blue
            "Personal" -> NeonGreen
            "Evento" -> Color(0xFFD48BFF)
            else -> NeonGreen
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(28.dp), spotColor = Color.Black),
        shape = RoundedCornerShape(28.dp),
        color = CardDark.copy(alpha = 0.8f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono Izquierda
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(categoryColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .blur(10.dp)
                        .background(categoryColor.copy(alpha = 0.15f), CircleShape)
                )
                Text(
                    text = if (isOtro) (reminder.emojiCustom ?: "🔔") else getCategoryEmoji(reminder.tipo),
                    fontSize = 24.sp
                )
                // Dot status indicator
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-4).dp, y = 4.dp)
                        .size(8.dp)
                        .background(categoryColor, CircleShape)
                        .border(1.5.dp, CardDark, CircleShape)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Centro
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isOtro) reminder.nombreCustom ?: "Recordatorio" else reminder.tipo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
                Text(
                    text = SimpleDateFormat("EEEE, d MMM, HH:mm", Locale.getDefault()).format(Date(reminder.fecha)),
                    fontSize = 12.sp,
                    color = categoryColor,
                    fontWeight = FontWeight.Medium
                )
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(Icons.Rounded.Refresh, null, tint = GrayText, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = reminder.repeatMode, fontSize = 11.sp, color = GrayText)
                }
            }
            
            // Derecha
            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = categoryColor.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, categoryColor.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = reminder.tipo,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        color = categoryColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Icon(
                        if (reminder.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        null,
                        tint = if (reminder.isFavorite) NeonGreen else GrayText.copy(alpha = 0.3f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Rounded.MoreVert, null, tint = GrayText.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun SmartReminderCard(icon: ImageVector, title: String, subtitle: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.03f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Box(
                modifier = Modifier.size(40.dp).background(NeonGreen.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = NeonGreen, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 16.sp)
                Text(subtitle, color = GrayText, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

fun getCategoryEmoji(tipo: String): String {
    return when(tipo) {
        "Trabajo" -> "💼"
        "Salud" -> "💊"
        "Estudio" -> "📚"
        "Personal" -> "🧘"
        "Evento" -> "🎁"
        else -> "🔔"
    }
}

fun isSameDay(t1: Long, t2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = t1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = t2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
