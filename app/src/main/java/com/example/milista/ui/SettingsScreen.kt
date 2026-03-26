package com.example.milista.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.milista.ui.theme.AcentoVerde
import com.example.milista.ui.theme.FondoCard
import com.example.milista.ui.theme.FondoPrincipal
import com.example.milista.ui.theme.TextoPrincipal
import com.example.milista.ui.theme.TextoSecundario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    var selectedTheme by remember { mutableStateOf("Por defecto") }
    var selectedLanguage by remember { mutableStateOf("Español") }
    var selectedFont by remember { mutableStateOf("Moderna") }
    var fontSize by remember { mutableFloatStateOf(16f) }
    var notificationsEnabled by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = FondoPrincipal,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Configuración", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = FondoPrincipal,
                    titleContentColor = TextoPrincipal
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                SettingsSectionTitle("Apariencia")
                SettingsItem(
                    icon = Icons.Default.Edit,
                    title = "Tema de la app",
                    subtitle = selectedTheme,
                    onClick = { /* Lógica para cambiar tema */ }
                )
                SettingsItem(
                    icon = Icons.Default.Menu,
                    title = "Tipo de letra",
                    subtitle = selectedFont,
                    onClick = { /* Lógica para cambiar fuente */ }
                )
                
                Text(
                    "Tamaño de letra: ${fontSize.toInt()}sp",
                    color = TextoSecundario,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                Slider(
                    value = fontSize,
                    onValueChange = { fontSize = it },
                    valueRange = 12f..24f,
                    colors = SliderDefaults.colors(
                        thumbColor = AcentoVerde,
                        activeTrackColor = AcentoVerde
                    )
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                SettingsSectionTitle("General")
                SettingsItem(
                    icon = Icons.Default.DateRange,
                    title = "Idioma",
                    subtitle = selectedLanguage,
                    onClick = { /* Lógica para cambiar idioma */ }
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = AcentoVerde)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Notificaciones", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Avisos sonoros y vibración", color = TextoSecundario, fontSize = 12.sp)
                        }
                    }
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = AcentoVerde)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                SettingsSectionTitle("Información")
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "Versión de la app",
                    subtitle = "1.4.0-Pro",
                    onClick = {}
                )
                SettingsItem(
                    icon = Icons.Default.Star,
                    title = "Calificar app",
                    subtitle = "¡Danos 5 estrellas!",
                    onClick = {}
                )
            }
            
            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        color = AcentoVerde,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        modifier = Modifier.padding(bottom = 16.dp)
    )
}

@Composable
fun SettingsItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        color = FondoCard,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = AcentoVerde, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(subtitle, color = TextoSecundario, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextoSecundario)
        }
    }
}
