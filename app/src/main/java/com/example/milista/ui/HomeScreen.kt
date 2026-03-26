package com.example.milista.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.milista.data.Recordatorio
import com.example.milista.ui.theme.*
import java.util.concurrent.TimeUnit
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    viewModel: MiListaViewModel,
    onAddLista: () -> Unit,
    onAddReminder: (String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    var showReminderPanel by remember { mutableStateOf(false) }
    val recordatorios by viewModel.recordatorios.collectAsState()
    
    var isExpanded by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(FondoPrincipal)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .blur(if (showReminderPanel || showMenu) 20.dp else 0.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 24.dp)
            ) {
                item { 
                    HeaderSection() 
                }
                
                item {
                    SectionTitle("Próximos Avisos")
                    AvisosCard(onClick = { showReminderPanel = true })
                    
                    if (recordatorios.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        val rotation by animateFloatAsState(if (isExpanded) 180f else 0f, label = "rotation")
                        val scale by animateFloatAsState(if (isExpanded) 1.02f else 1f, label = "scale")
                        
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .scale(scale)
                                .clickable { isExpanded = !isExpanded },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isExpanded) AcentoVerde.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
                            border = BorderStroke(
                                width = 2.dp, 
                                color = if (isExpanded) AcentoVerde else Color.White.copy(alpha = 0.1f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = if (isExpanded) AcentoVerde else Color.White,
                                    modifier = Modifier.size(24.dp).graphicsLayer(rotationZ = rotation)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = if (isExpanded) "Ocultar recordatorios" else "Mostrar recordatorios",
                                    color = if (isExpanded) AcentoVerde else Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    } else {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                item {
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeIn(),
                        exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeOut()
                    ) {
                        Column {
                            recordatorios.forEach { recordatorio ->
                                RecordatorioItem(
                                    recordatorio = recordatorio,
                                    onDelete = { viewModel.borrarRecordatorio(recordatorio) }
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }

                item {
                    SectionTitle("Tus Listas")
                    AgregarListaCard(onClick = onAddLista)
                }
            }
        }

        if (showMenu) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { showMenu = false }
                    .zIndex(10f)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 48.dp)
                .zIndex(11f),
            contentAlignment = Alignment.BottomCenter
        ) {
            CircularMenuOptions(
                visible = showMenu,
                onOptionClick = { option ->
                    showMenu = false
                    when (option) {
                        "configuracion" -> onNavigateToSettings()
                        "notas" -> onAddLista()
                    }
                }
            )

            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val pulsateScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.12f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulsate"
            )

            FloatingActionButton(
                onClick = { showMenu = !showMenu },
                containerColor = if (showMenu) Color(0xFFEF4444) else AcentoVerde,
                contentColor = if (showMenu) Color.White else FondoPrincipal,
                shape = CircleShape,
                modifier = Modifier
                    .size(68.dp)
                    .scale(if (showReminderPanel || showMenu) 1f else pulsateScale)
            ) {
                Icon(
                    imageVector = if (showMenu) Icons.Default.Close else Icons.Default.Home,
                    contentDescription = if (showMenu) "Cerrar" else "Menu",
                    modifier = Modifier.size(34.dp)
                )
            }
        }

        if (showReminderPanel) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .zIndex(20f)
                    .clickable { showReminderPanel = false }
            )
        }

        AnimatedVisibility(
            visible = showReminderPanel,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(animationSpec = tween(400)),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(animationSpec = tween(400)),
            modifier = Modifier.align(Alignment.BottomCenter).zIndex(21f)
        ) {
            AddReminderBottomSheet(
                onDismiss = { showReminderPanel = false },
                onTypeSelected = { tipo ->
                    showReminderPanel = false
                    onAddReminder(tipo)
                }
            )
        }
    }
}

@Composable
fun CircularMenuOptions(
    visible: Boolean,
    onOptionClick: (String) -> Unit
) {
    val options = listOf(
        MenuOption("Calendario", Icons.Default.DateRange, "calendario", Color(0xFFFBBF24)),
        MenuOption("Reloj", Icons.Default.Notifications, "reloj", Color(0xFF818CF8)),
        MenuOption("Notas", Icons.AutoMirrored.Filled.List, "notas", Color(0xFF34D399)),
        MenuOption("Ajustes", Icons.Default.Settings, "configuracion", Color(0xFF94A3B8))
    )

    options.forEachIndexed { index, option ->
        val angle = 180f + (index.toFloat() * (180f / (options.size - 1)))
        val angleRad = Math.toRadians(angle.toDouble())
        val radius = 100.dp

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + expandIn(expandFrom = Alignment.Center) + scaleIn(),
            exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.Center) + scaleOut(),
            modifier = Modifier.offset(
                x = (radius.value * cos(angleRad)).dp,
                y = (radius.value * sin(angleRad)).dp
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onOptionClick(option.id) }
            ) {
                Surface(
                    shape = CircleShape,
                    color = option.color,
                    modifier = Modifier.size(50.dp),
                    tonalElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(option.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }
                Text(
                    option.label,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

data class MenuOption(val label: String, val icon: ImageVector, val id: String, val color: Color)

@Composable
fun RecordatorioItem(recordatorio: Recordatorio, onDelete: () -> Unit) {
    val isOtro = recordatorio.tipo == "Otro"
    
    val emoji = if (isOtro) recordatorio.emojiCustom ?: "✨" else ReminderConstants.getByType(recordatorio.tipo).emoji
    val nombre = if (isOtro) recordatorio.nombreCustom ?: "Otro" else recordatorio.tipo
    val baseColor = if (isOtro && recordatorio.colorCustom != null) Color(recordatorio.colorCustom) else ReminderConstants.getByType(recordatorio.tipo).color
    
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

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInHorizontally()
    ) {
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
}

@Composable
fun HeaderSection() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 48.dp, bottom = 32.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Espaciado
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
