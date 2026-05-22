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
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.milista.R
import com.example.milista.ui.theme.*
import com.example.milista.ui.utils.AppLanguage
import com.example.milista.ui.utils.AppLanguages

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MiListaViewModel = viewModel()
) {
    val selectedFontState = viewModel.selectedFont.collectAsState()
    val selectedFont: com.example.milista.ui.theme.AppFont = selectedFontState.value
    
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    
    val selectedThemeState = viewModel.selectedTheme.collectAsState()
    val selectedTheme: String = selectedThemeState.value
    
    val selectedFontSizeState = viewModel.selectedFontSize.collectAsState()
    val selectedFontSize: Float = selectedFontSizeState.value
    
    var showFontSheet by remember { mutableStateOf(false) }
    var showLanguageSheet by remember { mutableStateOf(false) }
    var showThemeSheet by remember { mutableStateOf(false) }
    var doNotDisturb by rememberSaveable { mutableStateOf(false) }

    val fontSheetState = rememberModalBottomSheetState()
    val languageSheetState = rememberModalBottomSheetState()
    val themeSheetState = rememberModalBottomSheetState()

    Box(modifier = Modifier.fillMaxSize().background(AmoledBlack)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        Brush.radialGradient(
                            colors = listOf(NeonGlow.copy(alpha = 0.05f), Color.Transparent),
                            center = Offset(size.width * 0.8f, size.height * 0.1f),
                            radius = size.width * 1.2f
                        )
                    )
                }
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                SettingsHeader()
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // Sección Apariencia
                item {
                    SettingsSection(
                        title = stringResource(R.string.appearance),
                        icon = Icons.Rounded.Palette
                    ) {
                        SettingsItem(
                            icon = Icons.Rounded.DarkMode,
                            title = stringResource(R.string.app_theme),
                            subtitle = when(selectedTheme) {
                                "Oscuro" -> stringResource(R.string.dark)
                                "Claro" -> stringResource(R.string.light)
                                else -> stringResource(R.string.system)
                            },
                            onClick = { showThemeSheet = true }
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsItem(
                            icon = Icons.Rounded.TextFields,
                            title = stringResource(R.string.font_type),
                            subtitle = selectedFont.name,
                            onClick = { showFontSheet = true }
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 16.dp))
                        
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.font_size),
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = selectedFontSize.toInt().toString(),
                                    color = NeonGreen,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            GlowSlider(
                                value = selectedFontSize,
                                onValueChange = { viewModel.updateFontSize(it) }
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("A", color = GrayTextSettings, fontSize = 12.sp)
                                Text("A", color = GrayTextSettings, fontSize = 20.sp)
                            }
                        }
                    }
                }

                // Sección General
                item {
                    SettingsSection(
                        title = stringResource(R.string.general),
                        icon = Icons.Rounded.Settings
                    ) {
                        SettingsItem(
                            icon = Icons.Rounded.Language,
                            title = stringResource(R.string.language),
                            subtitle = AppLanguages.find { it.code == selectedLanguage }?.name ?: selectedLanguage,
                            onClick = { showLanguageSheet = true }
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsItem(
                            icon = Icons.Rounded.Notifications,
                            title = stringResource(R.string.notifications),
                            subtitle = stringResource(R.string.sound_vibration)
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsItem(
                            icon = Icons.Rounded.DoNotDisturbOn,
                            title = stringResource(R.string.do_not_disturb),
                            subtitle = stringResource(R.string.silence_notifications),
                            trailing = {
                                NeonSwitch(
                                    checked = doNotDisturb,
                                    onCheckedChange = { doNotDisturb = it }
                                )
                            }
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsItem(
                            icon = Icons.Rounded.PrivacyTip,
                            title = stringResource(R.string.privacy),
                            subtitle = stringResource(R.string.manage_privacy)
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsItem(
                            icon = Icons.Rounded.CloudUpload,
                            title = stringResource(R.string.backup),
                            subtitle = stringResource(R.string.sync_data)
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsItem(
                            icon = Icons.Rounded.DeleteSweep,
                            title = stringResource(R.string.delete_data),
                            subtitle = stringResource(R.string.delete_all_data),
                            tint = Color(0xFFFF5252).copy(alpha = 0.8f)
                        )
                    }
                }

                // Sección Más
                item {
                    SettingsSection(
                        title = stringResource(R.string.more),
                        icon = Icons.Rounded.MoreHoriz
                    ) {
                        SettingsItem(
                            icon = Icons.Rounded.HelpCenter,
                            title = stringResource(R.string.help_support),
                            subtitle = stringResource(R.string.help_center)
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsItem(
                            icon = Icons.Rounded.Info,
                            title = stringResource(R.string.about_noctra),
                            subtitle = stringResource(R.string.version)
                        )
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
                    selectedFontName = selectedFont.name
                )
            }
        }
        if (showLanguageSheet) {
            ModalBottomSheet(onDismissRequest = { showLanguageSheet = false }, sheetState = languageSheetState, containerColor = CardDark) {
                LanguageSelectorSheet(
                    selectedLanguageCode = selectedLanguage,
                    onLanguageSelected = { code -> viewModel.updateLanguage(code); showLanguageSheet = false },
                    onDismiss = { showLanguageSheet = false }
                )
            }
        }
        if (showThemeSheet) {
            ModalBottomSheet(onDismissRequest = { showThemeSheet = false }, sheetState = themeSheetState, containerColor = CardDark) {
                ThemeSelectorSheet(
                    selectedTheme = selectedTheme,
                    onThemeSelected = { themeName -> viewModel.updateTheme(themeName); showThemeSheet = false },
                    onDismiss = { showThemeSheet = false }
                )
            }
        }
    }
}

@Composable
fun SettingsHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = stringResource(R.string.settings),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 42.sp,
                    letterSpacing = (-1.5).sp
                ),
                color = Color.White
            )
            Text(
                text = stringResource(R.string.see_all).replace(" >", ""), // Generic subtitle fallback or add a new string
                style = MaterialTheme.typography.bodyLarge,
                color = GrayTextSettings
            )
        }

        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .size(64.dp)
                .shadow(16.dp, CircleShape, spotColor = NeonGreen)
                .background(Color.White.copy(alpha = 0.05f), CircleShape)
                .border(1.5.dp, NeonGreen.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.logo_app),
                contentDescription = "Noctra Logo",
                modifier = Modifier.size(40.dp).clip(CircleShape)
            )
            Box(modifier = Modifier.size(64.dp).blur(12.dp).background(NeonGreen.copy(alpha = 0.1f), CircleShape))
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)) {
            Icon(icon, null, tint = NeonGreen, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                color = NeonGreen,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp),
            color = Color(0xFF0B0B0B).copy(alpha = 0.85f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.03f))
        ) {
            Column(modifier = Modifier.padding(8.dp), content = content)
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color = NeonGreen,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(tint.copy(alpha = 0.08f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = GrayTextSettings, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
        if (trailing != null) {
            trailing()
        } else if (onClick != null) {
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = GrayTextSettings, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun NeonSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = NeonGreen,
            uncheckedThumbColor = GrayTextSettings,
            uncheckedTrackColor = Color.White.copy(alpha = 0.1f),
            uncheckedBorderColor = Color.Transparent
        )
    )
}

@Composable
fun GlowSlider(value: Float, onValueChange: (Float) -> Unit) {
    var sliderValue by remember(value) { mutableFloatStateOf(value) }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .height(6.dp)
                    .blur(12.dp)
                    .background(NeonGreen.copy(alpha = 0.15f), CircleShape)
            )

            Slider(
                value = sliderValue,
                onValueChange = { 
                    sliderValue = it
                    onValueChange(it) 
                },
                valueRange = 12f..40f,
                steps = 13,
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = NeonGreen,
                    inactiveTrackColor = Color.White.copy(alpha = 0.1f),
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ThemeSelectorSheet(selectedTheme: String, onThemeSelected: (String) -> Unit, onDismiss: () -> Unit) {
    val themes = listOf(
        "Oscuro" to "🌙", 
        "Claro" to "☀️", 
        "Sistema" to "📱"
    )
    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
        Text(text = stringResource(R.string.app_theme), style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        themes.forEach { (name, icon) ->
            val isSelected = name == selectedTheme
            Surface(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onThemeSelected(name) },
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) NeonGreen.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.05f),
                border = BorderStroke(1.dp, if (isSelected) NeonGreen else Color.Transparent)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(icon, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = when(name) {
                            "Oscuro" -> stringResource(R.string.dark)
                            "Claro" -> stringResource(R.string.light)
                            else -> stringResource(R.string.system)
                        }, 
                        color = if (isSelected) NeonGreen else Color.White, 
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Rounded.Check, contentDescription = null, tint = NeonGreen)
                    }
                }
            }
        }
    }
}

@Composable
fun LanguageSelectorSheet(selectedLanguageCode: String, onLanguageSelected: (String) -> Unit, onDismiss: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
        Text(text = stringResource(R.string.language), style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
            items(AppLanguages) { language ->
                val isSelected = language.code == selectedLanguageCode
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onLanguageSelected(language.code) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) NeonGreen.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, if (isSelected) NeonGreen else Color.Transparent)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (language.imageRes != null) androidx.compose.foundation.Image(painter = painterResource(id = language.imageRes), contentDescription = null, modifier = Modifier.size(24.dp))
                        else if (language.flag != null) Text(text = language.flag, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = language.name, color = if (isSelected) NeonGreen else Color.White, style = MaterialTheme.typography.bodyLarge)
                        if (isSelected) {
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.Rounded.Check, contentDescription = null, tint = NeonGreen)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FontSelectorSheet(onFontSelected: (String) -> Unit, onDismiss: () -> Unit, selectedFontName: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
        Text(text = stringResource(R.string.font_type), style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
            items(com.example.milista.ui.theme.AppFonts) { font ->
                val isSelected = font.name == selectedFontName
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onFontSelected(font.name) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) NeonGreen.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, if (isSelected) NeonGreen else Color.Transparent)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "ABC", fontSize = 18.sp, color = if (isSelected) NeonGreen else Color.White, fontFamily = font.fontFamily)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = font.name, color = if (isSelected) NeonGreen else Color.White, style = MaterialTheme.typography.bodyLarge, fontFamily = font.fontFamily)
                    }
                }
            }
        }
    }
}
