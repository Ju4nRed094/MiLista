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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.milista.R
import com.example.milista.ui.theme.*
import com.example.milista.ui.utils.getTranslatedText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: MiListaViewModel = viewModel()
) {
    val selectedFontState = viewModel.selectedFont.collectAsState()
    val selectedFont: com.example.milista.ui.theme.AppFont = selectedFontState.value
    
    val selectedLanguageState = viewModel.selectedLanguage.collectAsState()
    val selectedLanguage: String = selectedLanguageState.value
    
    val selectedThemeState = viewModel.selectedTheme.collectAsState()
    val selectedTheme: String = selectedThemeState.value
    
    val selectedFontSizeState = viewModel.selectedFontSize.collectAsState()
    val selectedFontSize: Float = selectedFontSizeState.value
    
    var showFontSheet by remember { mutableStateOf(false) }
    var showLanguageSheet by remember { mutableStateOf(false) }
    var showThemeSheet by remember { mutableStateOf(false) }

    val fontSheetState = rememberModalBottomSheetState()
    val languageSheetState = rememberModalBottomSheetState()
    val themeSheetState = rememberModalBottomSheetState()

    Box(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
        // Fondo Atmosférico (Consistente con Home)
        Box(modifier = Modifier.fillMaxSize()) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.mapuche),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().blur(60.dp).graphicsLayer(alpha = 0.3f)
            )
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black.copy(0.7f), BackgroundDark.copy(0.95f)))))
        }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                LargeTopAppBar(
                    title = {
                        Column {
                            Text(getTranslatedText("Ajustes", selectedLanguage), style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 34.sp, color = Color.White))
                            Text(getTranslatedText("Personaliza tu experiencia", selectedLanguage), style = MaterialTheme.typography.bodyMedium, color = GrayText)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White) }
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = Color.Transparent, scrolledContainerColor = BackgroundDark, titleContentColor = Color.White)
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Sección Apariencia
                item { SettingsSectionHeader(getTranslatedText("Apariencia", selectedLanguage)) }
                item {
                    SettingsCard {
                        SettingsItemPremium(icon = Icons.Default.Palette, title = getTranslatedText("Tema de la app", selectedLanguage), subtitle = getTranslatedText(selectedTheme, selectedLanguage), onClick = { showThemeSheet = true })
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsItemPremium(icon = Icons.Default.TextFields, title = getTranslatedText("Tipo de letra", selectedLanguage), subtitle = selectedFont.name, onClick = { showFontSheet = true })
                        
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("${getTranslatedText("Tamaño de letra", selectedLanguage)}: ${selectedFontSize.toInt()}", color = GrayText, fontSize = 12.sp)
                            Slider(
                                value = selectedFontSize,
                                onValueChange = { viewModel.updateFontSize(it) },
                                valueRange = 12f..24f,
                                steps = 2,
                                colors = SliderDefaults.colors(thumbColor = SamsungGreen, activeTrackColor = SamsungGreen)
                            )
                        }
                    }
                }

                // Sección General
                item { SettingsSectionHeader(getTranslatedText("General", selectedLanguage)) }
                item {
                    SettingsCard {
                        SettingsItemPremium(icon = Icons.Default.Translate, title = getTranslatedText("Idioma", selectedLanguage), subtitle = selectedLanguage, onClick = { showLanguageSheet = true })
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsItemPremium(icon = Icons.Default.Notifications, title = getTranslatedText("Notificaciones", selectedLanguage), subtitle = getTranslatedText("Avisos sonoros y vibración", selectedLanguage), onClick = {})
                    }
                }

                // Sección Sincronización
                item { SettingsSectionHeader("Ecosistema") }
                item {
                    SettingsCard {
                        SettingsItemPremium(icon = Icons.Default.CloudSync, title = "Cloud Sync", subtitle = "Sincroniza tus datos", onClick = {})
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsItemPremium(icon = Icons.Default.Backup, title = "Copia de seguridad", subtitle = "Última copia: hoy", onClick = {})
                    }
                }

                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }

        if (showFontSheet) {
            ModalBottomSheet(onDismissRequest = { showFontSheet = false }, sheetState = fontSheetState, containerColor = CardDark) {
                FontSelectorSheet(
                    onFontSelected = { fontName -> viewModel.updateFont(fontName); showFontSheet = false },
                    onDismiss = { showFontSheet = false },
                    selectedFontName = selectedFont.name,
                    selectedLanguage = selectedLanguage
                )
            }
        }
        if (showLanguageSheet) {
            ModalBottomSheet(onDismissRequest = { showLanguageSheet = false }, sheetState = languageSheetState, containerColor = CardDark) {
                LanguageSelectorSheet(
                    selectedLanguage = selectedLanguage,
                    onLanguageSelected = { lang -> viewModel.updateLanguage(lang); showLanguageSheet = false },
                    onDismiss = { showLanguageSheet = false }
                )
            }
        }
        if (showThemeSheet) {
            ModalBottomSheet(onDismissRequest = { showThemeSheet = false }, sheetState = themeSheetState, containerColor = CardDark) {
                ThemeSelectorSheet(
                    selectedTheme = selectedTheme,
                    selectedLanguage = selectedLanguage,
                    onThemeSelected = { themeName -> viewModel.updateTheme(themeName); showThemeSheet = false },
                    onDismiss = { showThemeSheet = false }
                )
            }
        }
    }
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = Color.White.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(content = content)
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(text = title, color = SamsungGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp, bottom = 8.dp, top = 16.dp))
}

@Composable
fun SettingsItemPremium(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(40.dp).background(Color.White.copy(0.06f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = GrayText, fontSize = 12.sp)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = GrayText, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun ThemeSelectorSheet(selectedTheme: String, selectedLanguage: String, onThemeSelected: (String) -> Unit, onDismiss: () -> Unit) {
    val themes = listOf("Oscuro" to "🌙", "Claro" to "☀️", "Sistema" to "📱")
    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
        Text(text = getTranslatedText("Tema de la app", selectedLanguage), style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        themes.forEach { (name, icon) ->
            val isSelected = name == selectedTheme
            Surface(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onThemeSelected(name) },
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) SamsungGreen.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.05f),
                border = BorderStroke(1.dp, if (isSelected) SamsungGreen else Color.Transparent)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(icon, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = getTranslatedText(name, selectedLanguage), color = if (isSelected) SamsungGreen else Color.White, style = MaterialTheme.typography.bodyLarge)
                    if (isSelected) {
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.Check, contentDescription = null, tint = SamsungGreen)
                    }
                }
            }
        }
    }
}

@Composable
fun LanguageSelectorSheet(selectedLanguage: String, onLanguageSelected: (String) -> Unit, onDismiss: () -> Unit) {
    val languages = listOf(
        Triple("Español (español)", "🇨🇱", null),
        Triple("Inglés (English)", "🇺🇸", null),
        Triple("Portugués (português)", "🇵🇹", null),
        Triple("Japonés (日本語)", "🇯🇵", null),
        Triple("Coreano (한국인)", "🇰🇷", null),
        Triple("Ruso (Pусский)", "🇷🇺", null),
        Triple("Árabe (العربية)", "🇸🇦", null),
        Triple("Mapudungun (Mapudungun)", null, R.drawable.mapuche)
    )
    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
        Text(text = getTranslatedText("Seleccionar Idioma", selectedLanguage), style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
            items(languages) { (name, flag, imageRes) ->
                val isSelected = name == selectedLanguage
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onLanguageSelected(name) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) SamsungGreen.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, if (isSelected) SamsungGreen else Color.Transparent)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (imageRes != null) androidx.compose.foundation.Image(painter = painterResource(id = imageRes), contentDescription = null, modifier = Modifier.size(24.dp))
                        else if (flag != null) Text(flag, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = name, color = if (isSelected) SamsungGreen else Color.White, style = MaterialTheme.typography.bodyLarge)
                        if (isSelected) {
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.Default.Check, contentDescription = null, tint = SamsungGreen)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FontSelectorSheet(onFontSelected: (String) -> Unit, onDismiss: () -> Unit, selectedFontName: String, selectedLanguage: String = "Español") {
    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
        Text(text = getTranslatedText("Tipo de letra", selectedLanguage), style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
            items(com.example.milista.ui.theme.AppFonts) { font ->
                val isSelected = font.name == selectedFontName
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onFontSelected(font.name) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) SamsungGreen.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, if (isSelected) SamsungGreen else Color.Transparent)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "ABC", fontSize = 18.sp, color = if (isSelected) SamsungGreen else Color.White, fontFamily = font.fontFamily)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = font.name, color = if (isSelected) SamsungGreen else Color.White, style = MaterialTheme.typography.bodyLarge, fontFamily = font.fontFamily)
                    }
                }
            }
        }
    }
}
