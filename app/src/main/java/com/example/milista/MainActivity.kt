package com.example.milista

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.milista.receiver.AlarmReceiver
import com.example.milista.ui.*
import com.example.milista.ui.theme.MiListaTheme
import com.example.milista.ui.theme.BackgroundDark
import com.example.milista.ui.utils.getLocaleCode
import com.example.milista.ui.utils.getTranslatedText
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : ComponentActivity() {
    private val viewModel: MiListaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val alarmId = intent.getIntExtra("alarm_id", -1)
        val alarmTitle = intent.getStringExtra("title") ?: ""
        
        enableEdgeToEdge()
        setContent {
            var showAlarmScreen by remember { mutableStateOf(alarmId != -1) }
            
            val selectedFont by viewModel.selectedFont.collectAsState()
            val selectedLanguage by viewModel.selectedLanguage.collectAsState()
            val selectedTheme by viewModel.selectedTheme.collectAsState()
            val selectedFontSize by viewModel.selectedFontSize.collectAsState()
            val isApplyingChanges by viewModel.isApplyingChanges.collectAsState()
            
            MiListaTheme(
                selectedFontFamily = selectedFont.fontFamily,
                selectedTheme = selectedTheme,
                selectedFontSize = selectedFontSize
            ) {
                if (showAlarmScreen) {
                    AlarmScreen(
                        title = alarmTitle,
                        onSnooze = {
                            val intent = Intent(this@MainActivity, AlarmReceiver::class.java).apply {
                                action = AlarmReceiver.ACTION_SNOOZE
                                putExtra("alarm_id", alarmId)
                                putExtra("title", alarmTitle)
                            }
                            sendBroadcast(intent)
                            showAlarmScreen = false
                            finish()
                        },
                        onDismiss = {
                            val intent = Intent(this@MainActivity, AlarmReceiver::class.java).apply {
                                action = AlarmReceiver.ACTION_DISMISS
                                putExtra("alarm_id", alarmId)
                            }
                            sendBroadcast(intent)
                            showAlarmScreen = false
                            finish()
                        }
                    )
                } else {
                    val layoutDirection = if (selectedLanguage == "Árabe (العربية)") LayoutDirection.Rtl else LayoutDirection.Ltr
                    val context = LocalContext.current

                    LaunchedEffect(selectedLanguage) {
                        val locale = Locale(getLocaleCode(selectedLanguage))
                        Locale.setDefault(locale)
                        val resources = context.resources
                        val configuration = resources.configuration
                        configuration.setLocale(locale)
                        resources.updateConfiguration(configuration, resources.displayMetrics)
                    }
                    
                    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                        val navController = rememberNavController()
                        val navBackStackEntry: NavBackStackEntry? by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route ?: "home"

                        Box(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
                            // Fondo Noctra One UI
                            Box(modifier = Modifier.fillMaxSize()) {
                                androidx.compose.foundation.Image(
                                    painter = painterResource(id = R.drawable.mapuche), 
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize().blur(60.dp).graphicsLayer(alpha = 0.3f)
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .drawBehind {
                                            drawRect(
                                                Brush.radialGradient(
                                                    colors = listOf(Color(0xFF67E36C).copy(alpha = 0.12f), Color.Transparent),
                                                    center = Offset(size.width / 2, -size.height * 0.1f),
                                                    radius = size.width * 1.2f
                                                )
                                            )
                                        }
                                )
                                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black.copy(0.75f), BackgroundDark.copy(0.98f)))))
                            }

                            Scaffold(
                                containerColor = Color.Transparent,
                                modifier = Modifier.blur(if (isApplyingChanges) 15.dp else 0.dp),
                                bottomBar = {
                                    val hideBarRoutes = listOf("note_editor", "add_reminder", "task_list")
                                    if (hideBarRoutes.none { currentRoute.startsWith(it) }) {
                                        PremiumBottomBar(
                                            selectedLanguage = selectedLanguage,
                                            onNavigateToHome = { navController.navigate("home") { launchSingleTop = true } },
                                            onNavigateToCalendar = { navController.navigate("calendar") { launchSingleTop = true } },
                                            onNavigateToPlus = { navController.navigate("reminders") { launchSingleTop = true } },
                                            onNavigateToFocus = { navController.navigate("productivity") { launchSingleTop = true } },
                                            onNavigateToSettings = { navController.navigate("settings") { launchSingleTop = true } },
                                            currentRoute = currentRoute
                                        )
                                    }
                                }
                            ) { padding ->
                                NavHost(
                                    navController = navController, 
                                    startDestination = "home",
                                    modifier = Modifier.padding(padding),
                                    enterTransition = { fadeIn(animationSpec = tween(400)) },
                                    exitTransition = { fadeOut(animationSpec = tween(400)) }
                                ) {
                                    composable("home") {
                                        HomeScreen(
                                            viewModel = viewModel,
                                            onAddReminder = { tipo -> navController.navigate("add_reminder/$tipo") },
                                            onNavigateToSettings = { navController.navigate("settings") },
                                            onNavigateToClock = { navController.navigate("clock") },
                                            onNavigateToCalendar = { navController.navigate("calendar") },
                                            onNavigateToListas = { navController.navigate("listas") },
                                            onNavigateToNotes = { navController.navigate("notes") }
                                        )
                                    }
                                    composable("clock") {
                                        ClockScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                                    }
                                    composable("listas") {
                                        ListasScreen(viewModel = viewModel, onNavigateToListDetails = { listaId -> navController.navigate("task_list/$listaId") })
                                    }
                                    composable("notes") {
                                        val id by viewModel.quickNotesId.collectAsState()
                                        if (id != null) {
                                            NotesScreen(
                                                viewModel = viewModel,
                                                listaId = id!!,
                                                onBack = { navController.popBackStack() },
                                                onNavigateToEditor = { lId, tId -> navController.navigate("note_editor/$lId/$tId") }
                                            )
                                        }
                                    }
                                    composable("productivity") {
                                        ProductivityScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                                    }
                                    composable(
                                        "task_list/{listaId}",
                                        arguments = listOf(navArgument("listaId") { type = NavType.IntType })
                                    ) { backStackEntry ->
                                        val listaId = backStackEntry.arguments?.getInt("listaId") ?: 0
                                        TaskListScreen(
                                            viewModel = viewModel,
                                            listaId = listaId,
                                            onBack = { navController.popBackStack() }
                                        )
                                    }
                                    composable(
                                        "note_editor/{listaId}/{tareaId}",
                                        arguments = listOf(navArgument("listaId") { type = NavType.IntType }, navArgument("tareaId") { type = NavType.IntType })
                                    ) { backStackEntry ->
                                        val listaId = backStackEntry.arguments?.getInt("listaId") ?: 0
                                        val tareaId = backStackEntry.arguments?.getInt("tareaId") ?: -1
                                        NoteEditorScreen(viewModel = viewModel, listaId = listaId, tareaId = tareaId, onBack = { navController.popBackStack() })
                                    }
                                    composable(
                                        "add_reminder/{tipo}",
                                        arguments = listOf(navArgument("tipo") { type = NavType.StringType })
                                    ) { backStackEntry ->
                                        val tipo = backStackEntry.arguments?.getString("tipo") ?: "Otro"
                                        AddReminderScreen(viewModel = viewModel, tipo = tipo, onBack = { navController.popBackStack() })
                                    }
                                    composable("settings") {
                                        SettingsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                                    }
                                    composable("calendar") {
                                        CalendarScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                                    }
                                    composable("reminders") {
                                        ReminderScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                                    }
                                }
                            }

                            if (isApplyingChanges) {
                                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                                    Surface(color = Color(0xFF1C1C1E).copy(alpha = 0.9f), shape = RoundedCornerShape(20.dp), modifier = Modifier.size(200.dp)) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.padding(20.dp)) {
                                            CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp, modifier = Modifier.size(45.dp))
                                            Spacer(modifier = Modifier.height(20.dp))
                                            Text(getTranslatedText("Aplicando cambios", selectedLanguage), color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
