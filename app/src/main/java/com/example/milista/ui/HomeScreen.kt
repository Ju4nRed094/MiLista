package com.example.milista.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.milista.data.Recordatorio
import com.example.milista.ui.theme.*
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    viewModel: MiListaViewModel,
    onAddLista: () -> Unit,
    onAddReminder: (String) -> Unit
) {
    var showReminderPanel by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val recordatorios by viewModel.recordatorios.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(FondoPrincipal)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .blur(if (showReminderPanel) 20.dp else 0.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 24.dp)
            ) {
                item { HeaderSection() }
                
                item {
                    SectionTitle("Próximos Avisos")
                    // Botón para crear siempre visible arriba
                    AvisosCard(onClick = { showReminderPanel = true })
                    Spacer(modifier = Modifier.height(16.dp))
                }

                items(recordatorios) { recordatorio ->
                    RecordatorioItem(
                        recordatorio = recordatorio,
                        onDelete = { viewModel.borrarRecordatorio(recordatorio) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                item {
                    SectionTitle("Tus Listas")
                    AgregarListaCard(onClick = onAddLista)
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize().blur(if (showReminderPanel) 20.dp else 0.dp)) {
            FloatingActionButton(
                onClick = { showReminderPanel = true },
                containerColor = AcentoVerde,
                contentColor = FondoPrincipal,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp) 
                    .size(64.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo", modifier = Modifier.size(32.dp))
            }
        }

        if (showReminderPanel) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .zIndex(1f)
                    .clickable { showReminderPanel = false }
            )
        }

        AnimatedVisibility(
            visible = showReminderPanel,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).zIndex(2f)
        ) {
            AddReminderBottomSheet(
                onDismiss = { showReminderPanel = false },
                onTypeSelected = { tipo ->
                    showReminderPanel = false
                    onAddReminder(tipo)
                }
            )
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter).zIndex(3f)) {
            MiListaBottomNavigation(selectedTab) { selectedTab = it }
        }
    }
}

@Composable
fun RecordatorioItem(recordatorio: Recordatorio, onDelete: () -> Unit) {
    val isOtro = recordatorio.tipo == "Otro"
    
    val emoji = if (isOtro) recordatorio.emojiCustom ?: "✨" else ReminderConstants.getByType(recordatorio.tipo).emoji
    val nombre = if (isOtro) recordatorio.nombreCustom ?: "Otro" else recordatorio.tipo
    val baseColor = if (isOtro && recordatorio.colorCustom != null) Color(recordatorio.colorCustom) else ReminderConstants.getByType(recordatorio.tipo).color
    
    // Timer reactivo para el contador
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(key1 = recordatorio.fecha) {
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(1000)
        }
    }

    val remainingTime = remember(recordatorio.fecha, currentTime) {
        val diff = recordatorio.fecha - currentTime
        if (diff <= 0) "¡Es ahora!"
        else {
            val days = TimeUnit.MILLISECONDS.toDays(diff)
            val hours = TimeUnit.MILLISECONDS.toHours(diff) % 24
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
            val seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60
            
            when {
                days > 0 -> "Quedan $days días y $hours h"
                hours > 0 -> "Quedan $hours h y $minutes m"
                minutes > 0 -> "Quedan $minutes m y $seconds s"
                else -> "Queda $seconds s"
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = baseColor.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, baseColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(baseColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nombre,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
                Text(
                    text = remainingTime,
                    color = baseColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Borrar",
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun HeaderSection() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 48.dp, bottom = 32.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "MiLista", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Icon(Icons.Default.Search, contentDescription = "Buscar", tint = Color.White, modifier = Modifier.size(30.dp))
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(text = title, color = TextoSecundario, fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 12.dp, top = 20.dp))
}

@Composable
fun AvisosCard(onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }, shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = FondoCard)) {
        Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(52.dp).clip(CircleShape).background(FondoPrincipal.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                Text("🔔", fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Nuevo Recordatorio", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Toca para crear uno nuevo", color = TextoSecundario, fontSize = 14.sp)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextoSecundario, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun AgregarListaCard(onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).clickable { onClick() }, shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = FondoCard)) {
        Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(52.dp).clip(CircleShape).background(FondoPrincipal.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                Text("✍️", fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Agregar Nueva Lista", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Crea un nuevo espacio", color = TextoSecundario, fontSize = 14.sp)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextoSecundario, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun MiListaBottomNavigation(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    Surface(color = Color(0xFF07111D), modifier = Modifier.height(60.dp).fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
            NavigationIcon(Icons.Default.Home, selectedTab == 0) { onTabSelected(0) }
            NavigationIcon(Icons.Default.Search, selectedTab == 1) { onTabSelected(1) }
            Spacer(modifier = Modifier.width(64.dp))
            NavigationIcon(Icons.AutoMirrored.Filled.List, selectedTab == 2) { onTabSelected(2) }
            NavigationIcon(Icons.Default.Settings, selectedTab == 3) { onTabSelected(3) }
        }
    }
}

@Composable
fun NavigationIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(imageVector = icon, contentDescription = null, tint = if (isSelected) AcentoVerde else TextoSecundario, modifier = Modifier.size(24.dp))
    }
}
